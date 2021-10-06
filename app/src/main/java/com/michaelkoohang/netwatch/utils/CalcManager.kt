package com.michaelkoohang.netwatch.utils

import com.michaelkoohang.netwatch.model.api.download.RecordingResponse

object CalcManager {
    /**
     * Calculates the total distance traveled in a list of recordings.
     * @param recordings a list of recording objects in JSON format.
     * @return the total distance in kilometers.
     */
    fun calcDistance(recordings: ArrayList<RecordingResponse>): Double {
        var distance = 0.0
        for (recording in recordings) {
            distance += recording.distance!!
        }
        return distance
    }

    /**
     * Calculates the total duration of all recordings in a list of recordings.
     * @param recordings a list of recordings objects in JSON format.
     * @return the duration of all recordings in seconds.
     */
    fun calcTime(recordings: ArrayList<RecordingResponse>): Long {
        var seconds: Long = 0
        for (recording in recordings) {
            seconds += recording.duration!!
        }
        return seconds
    }

    /**
     * Caculates the average connectivity of all recordings in a list of recordings.
     * @param recordings a list of recording objects in JSON format.
     * @return the average connectivity across all recordings in the list.
     */
    fun calcConnectivity(recordings: ArrayList<RecordingResponse>): Double {
        var coverage = 0.0
        var featureCount = 0
        for (recording in recordings) {
            val features = recording.features!!
            for (i in 0 until features.size) {
                val feature = features[i]
                if (feature.connected == 1) {
                    coverage++
                }
                featureCount++
            }
        }
        return coverage / featureCount
    }

    /**
     * Caculates the average connectivity of a single recording.
     * @param recordings a recording object in JSON format.
     * @return the average connectivity of the recording.
     */
    fun calcConnectivity(recording: RecordingResponse): Double {
        var coverage = 0.0
        var featureCount = 0
        val features = recording.features!!
        for (i in 0 until features.size) {
            val feature = features[i]
            if (feature.connected == 1) {
                coverage++
            }
            featureCount++
        }
        return coverage / featureCount
    }
}