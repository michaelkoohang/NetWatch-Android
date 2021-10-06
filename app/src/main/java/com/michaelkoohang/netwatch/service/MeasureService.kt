package com.michaelkoohang.netwatch.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.model.db.Feature
import com.michaelkoohang.netwatch.model.db.Recording
import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.ui.main.MainActivity
import com.michaelkoohang.netwatch.utils.FormatManager
import io.realm.Realm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MeasureService : Service() {

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val RECORD_INTERVAL: Long = 30 * 1000
        private const val LOCATION_UPDATE_INTERVAL: Long = 5 * 1000
        private const val MAX_WAIT_TIME: Long = 10 * 1000
        var recordingInProgress = false
        var testInProgress = false
    }

    // Measurement variables
    private var timestamp: String? = null
    private var distance = 0.0
    private var battery: Int? = null
    private var network: String? = ""
    private var service: Boolean? = null
    private var connected: Boolean? = null
    private var http: Boolean? = null
    private var lat: Double? = null
    private var lon: Double? = null
    private var accuracy: Double? = null
    private var speed: Double? = null
    private var carrier: String? = null
    private val points = ArrayList<Location>()
    private var firstLocationCall = true

    // Network and device API managers
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var batteryManager: BatteryManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    // HTTP test client and request
    private val okHttpClient = OkHttpClient()
    private val request = Request.Builder()
            .url("https://www.google.com")
            .build()

    // Database
    private lateinit var recording: Recording
    private lateinit var realm: Realm

    // Listens for service state, connectivity, and network type to change
    private val phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onServiceStateChanged(serviceState: ServiceState) {
            service = serviceState.state == ServiceState.STATE_IN_SERVICE
        }

        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            super.onDataConnectionStateChanged(state, networkType)
            connected = state == TelephonyManager.DATA_CONNECTED
            network = getNetworkType(networkType)
        }
    }

    // Updates the state of the device location
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                lat = locationResult.lastLocation.latitude
                lon = locationResult.lastLocation.longitude
                accuracy = locationResult.lastLocation.accuracy.toDouble()
                speed = locationResult.lastLocation.speed.toDouble()
                calcDistance()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Get network and location information from APIs
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE or PhoneStateListener.LISTEN_SERVICE_STATE)
        batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        // Get initial network data
        network = getNetworkType(telephonyManager.dataNetworkType)
        service = telephonyManager.serviceState.state == ServiceState.STATE_IN_SERVICE
        connected = telephonyManager.dataState == TelephonyManager.DATA_CONNECTED

        // Set up location updates every 5 seconds
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = LOCATION_UPDATE_INTERVAL
        locationRequest.maxWaitTime = MAX_WAIT_TIME
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Get carrier from data store
        carrier = runBlocking { DataStoreManager.carrierFlow.first() }

        // Acquire database and create recording
        realm = Realm.getDefaultInstance()
        startRecording()

        // Create notification for NetWatch
        createNotification()

        return START_STICKY
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NetWatch::MeasureWakeLock").apply {
                acquire()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recordingInProgress = false
        firstLocationCall = true
        wakeLock.release()
        uiHandler.removeCallbacks(updateUI)
        featureHandler.removeCallbacks(updateFeature)
        recordingHandler.removeCallbacks(updateRecording)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Creates notification for NetWatch saying that app is tracking location.
    private fun createNotification() {
        val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Measuring network")
                .setContentText("NetWatch is measuring your network connection. Tap to return to the app.")
                .setSmallIcon(R.drawable.ic_mountain_signal)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
    }

    /** Handlers for repeated tasks such as updating the ui, saving the features, and
     *  updating the current recording
     */

    // Updates the UI every second
    private val uiHandler = Handler()
    private val updateUI = object : Runnable {
        override fun run() {
            uiHandler.postDelayed(this, 1000)
            val elapsedTime = FormatManager.getCurrentTimeElapsed(recording.start)
            sendTime(elapsedTime)
            sendDistanceAndConnection()
        }
    }

    // Saves a new feature every 30 seconds.
    private val featureHandler = Handler()
    private val updateFeature = object : Runnable {
        override fun run() {
            featureHandler.postDelayed(this, RECORD_INTERVAL)
            timestamp = FormatManager.getIsoDate(Date())
            battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            if (firstLocationCall) {
                getFirstLocation()
            } else {
                saveFeature()
            }
        }
    }

    // Updates the distance and duration of the recording
    private val recordingHandler = Handler()
    private val updateRecording = object : Runnable {
        override fun run() {
            recordingHandler.postDelayed(this, 1000)
            updateRecording()
        }
    }

    /** Recording methods
     */

    // Creates recording in database.
    private fun startRecording() {
        realm.executeTransaction { realm ->
            recording = realm.createObject(Recording::class.java, UUID.randomUUID().toString())
            recording.start = FormatManager.getIsoDate(Date())
            recording.os = Build.VERSION.RELEASE
            recording.carrier = carrier!!
            recording.manufacturer = Build.MANUFACTURER.substring(0, 1).toUpperCase(Locale.ROOT) + Build.MANUFACTURER.substring(1)
        }
        recordingInProgress = true
        uiHandler.post(updateUI)
        featureHandler.post(updateFeature)
        recordingHandler.post(updateRecording)
    }

    // Update recording information
    private fun updateRecording() {
        realm.executeTransaction {
            recording.end = FormatManager.getIsoDate(Date())
            recording.distance = distance
            recording.duration = FormatManager.getTimeElapsed(recording.start, recording.end)
        }
    }

    // Saves a feature to the database
    private fun saveFeature() {
        testInProgress = true
        realm.executeTransaction { realm ->
            val feature = realm.createObject(Feature::class.java, UUID.randomUUID().toString())
            feature.timestamp = timestamp!!
            feature.battery = battery!!
            feature.network = network!!
            feature.service = service!!
            feature.connected = connected!!
            feature.lat = lat!!
            feature.lon = lon!!
            feature.accuracy = accuracy!!
            feature.speed = speed!!
            feature.http = runBlocking {
                GlobalScope.launch {
                    http = try {
                        okHttpClient.newCall(request).execute().isSuccessful
                    } catch (err: Exception) {
                        false
                    }
                }.join()
                http!!
            }
            recording.features.add(feature)
        }
        testInProgress = false
    }

    // Accumulate distance
    private fun calcDistance() {
        if (lat != 0.0 && lon != 0.0) {
            val nextLocation = Location("next_location")
            nextLocation.latitude = lat!!
            nextLocation.longitude = lon!!
            if (points.size > 0) {
                val lastLocation = Location("last_location")
                lastLocation.latitude = points[points.size - 1].latitude
                lastLocation.longitude = points[points.size - 1].longitude
                distance += (lastLocation.distanceTo(nextLocation) / 1000)
            }
            points.add(nextLocation)
        }
    }

    /** Broadcast methods
     */

    // Sends time to the UI.
    private fun sendTime(duration: Long) {
        val intent = Intent("ServiceTimeUpdates")
        intent.putExtra("Seconds", duration)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    // Sends accumulated distance and connectivity status to the UI.
    private fun sendDistanceAndConnection() {
        val intent = Intent("ServiceDistanceConnectionUpdates")
        intent.putExtra("Distance", distance)
        intent.putExtra("Connected", connected)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    /** Helper functions
     */

    @SuppressLint("MissingPermission")
    private fun getFirstLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            lat = location.latitude
            lon = location.longitude
            accuracy = location.accuracy.toDouble()
            speed = location.speed.toDouble()
            saveFeature()
            firstLocationCall = false
        }
    }

    // Get network type from enums
    private fun getNetworkType(type: Int): String {
        when (type) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "UNKNOWN"
            TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "EVDO_REV_0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> return "EVDO_REV_A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> return "1xRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> return "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> return "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> return "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> return "IDEN"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> return "EVDO_REV_B"
            TelephonyManager.NETWORK_TYPE_LTE -> return "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> return "EHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM -> return "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> return "IWLAN"
            TelephonyManager.NETWORK_TYPE_NR -> return "NEW_RADIO_5G"
        }
        return ""
    }
}