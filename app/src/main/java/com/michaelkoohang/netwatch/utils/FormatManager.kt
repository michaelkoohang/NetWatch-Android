package com.michaelkoohang.netwatch.utils

import android.icu.util.Calendar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatManager {
    private val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    private val displayIsoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val displayDateFormatter = SimpleDateFormat("MM/dd/yyyy - h:mm aa", Locale.US)
    private val distanceFormatter = DecimalFormat("0.00")
    private val coverageFormatter = DecimalFormat("0")
    
    init {
        isoDateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        displayIsoDateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        displayDateFormatter.timeZone = TimeZone.getDefault()
    }

    // Time functions 
    
    fun getEpochTime(date: String): Long {
        return displayIsoDateFormatter.parse(date)!!.time / 1000
    }
    
    fun getIsoDate(date: Date): String {
        return isoDateFormatter.format(date)
    }

    fun getTimeElapsed(startDate: String, endDate: String): Long {
        val startTime = isoDateFormatter.parse(startDate)!!.time
        val endTime = isoDateFormatter.parse(endDate)!!.time
        return (endTime - startTime) / 1000
    }

    fun getCurrentTimeElapsed(startTime: String): Long {
        val epoch = isoDateFormatter.parse(startTime)!!.time
        return (System.currentTimeMillis() - epoch) / 1000
    }
    
    // Display formats for time, distance, and coverage

    fun getStopWatchTime(duration: Long): String {
        val seconds = duration % 60
        val minutes = duration % 3600 / 60
        val hours = duration / 3600

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun getDisplayDate(date: String): String {
        val newDate = displayIsoDateFormatter.parse(date)
        return displayDateFormatter.format(newDate!!)
    }

    fun getDisplayDistance(distance: Double): String {
        return "${distanceFormatter.format(distance)} km"
    }
    
    fun getDisplayCoverage(coverage: Double): String {
        return "${coverageFormatter.format(coverage * 100)}%"
    }

    fun getRecordingDescription(date: String): String {
        val cal = Calendar.getInstance()
        cal.time = displayIsoDateFormatter.parse(date)

        val timeOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        var timeOfDayString = ""
        var dayOfWeekString = ""

        timeOfDayString = when {
            timeOfDay < 12 -> {
                "Morning"
            }
            timeOfDay < 18 -> {
                "Afternoon"
            }
            else -> {
                "Evening"
            }
        }

        dayOfWeekString = when {
            dayOfWeek == 1 ->  "Sunday"
            dayOfWeek == 2 -> "Monday"
            dayOfWeek == 3 -> "Tuesday"
            dayOfWeek == 4 -> "Wednesday"
            dayOfWeek == 5 -> "Thursday"
            dayOfWeek == 6 -> "Friday"
            dayOfWeek == 7 -> "Saturday"
            else -> ""
        }

        return "$dayOfWeekString $timeOfDayString"
    }
}