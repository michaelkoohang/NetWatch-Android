package com.michaelkoohang.netwatch.ui.record

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentMapBinding
import com.michaelkoohang.netwatch.model.repos.RecordingRepository
import com.michaelkoohang.netwatch.service.MeasureService
import com.michaelkoohang.netwatch.utils.FormatManager
import com.michaelkoohang.netwatch.utils.PermissionManager
import com.michaelkoohang.netwatch.utils.UploadManager

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // Map variables
    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationComponent: LocationComponent

    // Broadcast receivers for UI updates
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val seconds = intent.getLongExtra("Seconds", 0)
            binding.timeText.text = FormatManager.getStopWatchTime(seconds)
        }
    }

    private val distanceConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val distance = intent.getDoubleExtra("Distance", 0.0)
            val isConnected = intent.getBooleanExtra("Connected", false)
            binding.detailDistanceText.text = FormatManager.getDisplayDistance(distance)
            if (isConnected) {
                val green = ContextCompat.getColor(requireContext(), R.color.green)
                binding.connectedImg.setImageResource(R.drawable.ic_connnected)
                binding.connectedImg.imageTintList = ColorStateList.valueOf(green)
            } else {
                val red = ContextCompat.getColor(requireContext(), R.color.red)
                binding.connectedImg.setImageResource(R.drawable.ic_disconnected)
                binding.connectedImg.imageTintList = ColorStateList.valueOf(red)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Setup the map
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.startButton.setOnClickListener { startMeasuring() }
        binding.stopButton.setOnClickListener { stopMeasuring() }
        binding.currentLocationButton.setOnClickListener {
            val lat = locationComponent.lastKnownLocation!!.latitude
            val lon = locationComponent.lastKnownLocation!!.longitude
            val position = CameraPosition.Builder().target(LatLng(lat, lon)).zoom(15.0).build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
        }

        // Register UI updates
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                timeReceiver, IntentFilter("ServiceTimeUpdates"))
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                distanceConnectionReceiver, IntentFilter("ServiceDistanceConnectionUpdates"))

        return binding.root
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) { style -> enableLocationComponent(style) }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Setup location component
        val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .elevation(5f)
                .accuracyAlpha(.4f)
                .accuracyColor(Color.RED)
                .build()
        val locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()
        locationComponent = mapboxMap.locationComponent
        locationComponent.activateLocationComponent(locationComponentActivationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.zoomWhileTracking(15.0)

        // Setup compass
        mapboxMap.uiSettings.setCompassMargins(COMPASS_MARGIN_LEFT, COMPASS_MARGIN_TOP, COMPASS_MARGIN_RIGHT, COMPASS_MARGIN_BOTTOM)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        checkPermission()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(timeReceiver)
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(distanceConnectionReceiver)
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun checkPermission() {
        if (!PermissionManager.checkPermission()) {
            findNavController().navigate(R.id.action_map_to_location_permission)
        } else {
            loadView()
        }
    }

    // Measuring methods
    private fun startMeasuring() {
        binding.timeCard.visibility = View.VISIBLE
        binding.distanceCard.visibility = View.VISIBLE
        binding.connectedCard.visibility = View.VISIBLE
        binding.startButton.visibility = View.GONE
        Handler().postDelayed({
            val intent = Intent(requireActivity(), MeasureService::class.java)
            requireActivity().startService(intent)
            binding.stopButton.visibility = View.VISIBLE
        }, 300)
    }

    private fun stopMeasuring() {
        if (MeasureService.testInProgress) {
            Toast.makeText(requireContext(), "Network test in progress. Please wait a few seconds until it finishes.", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(requireActivity(), MeasureService::class.java)
            requireActivity().stopService(intent)
            binding.stopButton.visibility = View.GONE
            binding.timeCard.visibility = View.GONE
            binding.distanceCard.visibility = View.GONE
            binding.connectedCard.visibility = View.GONE
            Toast.makeText(requireContext(), "Hikes uploading...", Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                binding.timeText.text = ""
                binding.detailDistanceText.text = ""
                binding.startButton.visibility = View.VISIBLE
            }, 300)
            Handler().postDelayed({
                RecordingRepository.uploadRecordingData(UploadManager.uploadCallback)
            }, 2000)
        }
    }

    private fun loadView() {
        if (MeasureService.recordingInProgress) {
            binding.stopButton.visibility = View.VISIBLE
            binding.startButton.visibility = View.GONE
            binding.timeCard.visibility = View.VISIBLE
            binding.distanceCard.visibility = View.VISIBLE
            binding.connectedCard.visibility = View.VISIBLE
        } else {
            binding.stopButton.visibility = View.GONE
            binding.startButton.visibility = View.VISIBLE
            binding.timeCard.visibility = View.GONE
            binding.distanceCard.visibility = View.GONE
            binding.connectedCard.visibility = View.GONE
        }
    }

    companion object {
        private const val COMPASS_MARGIN_TOP = 24
        private const val COMPASS_MARGIN_LEFT = 0
        private const val COMPASS_MARGIN_BOTTOM = 0
        private const val COMPASS_MARGIN_RIGHT = 24
    }
}