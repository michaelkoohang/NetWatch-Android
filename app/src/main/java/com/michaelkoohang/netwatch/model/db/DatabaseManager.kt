package com.michaelkoohang.netwatch.model.db

import com.michaelkoohang.netwatch.model.api.upload.FeatureUpload
import com.michaelkoohang.netwatch.model.api.upload.RecordingUpload
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONObject

object DatabaseManager {
    /**
     * Queries the local cache for recordings to upload
     * @return list of recordings to upload
     */
    fun getRecordings(): ArrayList<RecordingUpload> {
        val data = ArrayList<RecordingUpload>()
        val realm = Realm.getDefaultInstance()
        val result = realm.where(Recording::class.java)
                .findAll()
        for (rec in result) {
            val recordingUpload = RecordingUpload()
            recordingUpload.duration = rec.duration
            recordingUpload.distance = rec.distance
            recordingUpload.start = rec.start
            recordingUpload.end = rec.end
            recordingUpload.carrier = rec.carrier
            recordingUpload.manufacturer = rec.manufacturer
            recordingUpload.os = rec.os
            val features = ArrayList<FeatureUpload>()
            for (feature in rec.features) {
                val featureUpload = FeatureUpload()
                featureUpload.timestamp = feature.timestamp
                featureUpload.battery = feature.battery
                featureUpload.network = feature.network
                featureUpload.service = feature.service
                featureUpload.connected = feature.connected
                featureUpload.http = feature.http
                featureUpload.lat = feature.lat
                featureUpload.lon = feature.lon
                featureUpload.accuracy = feature.accuracy
                featureUpload.speed = feature.speed
                features.add(featureUpload)
            }
            recordingUpload.features = features
            data.add(recordingUpload)
        }
        return data
    }

    /**
     * Queries the local cache for recordings
     * @return list of recordings in JSON format
     */
    fun getRecordingsJson(): JSONArray {
        val data = JSONArray()
        val realm = Realm.getDefaultInstance()
        val result = realm.where(Recording::class.java)
                .findAll()
        for (rec in result) {
            val recordingJSON = JSONObject()
            val features = JSONArray()
                recordingJSON.put("duration", rec.duration)
                recordingJSON.put("distance", rec.distance)
                recordingJSON.put("start", rec.start)
                recordingJSON.put("end", rec.end)
                recordingJSON.put("carrier", rec.carrier)
                recordingJSON.put("manufacturer", rec.manufacturer)
                recordingJSON.put("os", rec.os)
                for (feature in rec.features) {
                    val featureJSON = JSONObject()
                    featureJSON.put("timestamp", feature.timestamp)
                    featureJSON.put("battery", feature.battery)
                    featureJSON.put("network", feature.network)
                    featureJSON.put("service", feature.service)
                    featureJSON.put("connected", feature.connected)
                    featureJSON.put("http", feature.http)
                    featureJSON.put("lat", feature.lat)
                    featureJSON.put("lon", feature.lon)
                    featureJSON.put("accuracy", feature.accuracy)
                    featureJSON.put("speed", feature.speed)
                    features.put(featureJSON)
                }
            recordingJSON.put("features", features)
            data.put(recordingJSON)
        }
        return data
    }

    /**
     * Clears local cache of all data
     */
    fun clearCache() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
        realm.close()
    }
}