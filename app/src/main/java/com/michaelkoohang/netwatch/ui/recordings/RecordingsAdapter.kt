package com.michaelkoohang.netwatch.ui.recordings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.ViewRecordingItemBinding
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.ui.recordings.RecordingsAdapter.RecordingViewHolder
import com.michaelkoohang.netwatch.utils.CalcManager
import com.michaelkoohang.netwatch.utils.FormatManager

class RecordingsAdapter(private var dataset: MutableList<RecordingResponse>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecordingViewHolder>() {
    class RecordingViewHolder(b: ViewRecordingItemBinding) : RecyclerView.ViewHolder(b.root) {
        val binding = b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ViewRecordingItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val startTime = dataset[position].start!!
        val duration = dataset[position].duration!!
        val distance = dataset[position].distance!!
        val connnectivity = CalcManager.calcConnectivity(dataset[position])

        if (connnectivity >= 0.5) {
            holder.binding.connectivityText.setTextColor(
                ContextCompat.getColor(holder.binding.root.context, R.color.green)
            )
        } else {
            holder.binding.connectivityText.setTextColor(
                ContextCompat.getColor(holder.binding.root.context, R.color.red)
            )
        }

        holder.binding.dateText.text = FormatManager.getDisplayDate(startTime)
        holder.binding.descriptionText.text = FormatManager.getRecordingDescription(startTime)
        holder.binding.connectivityText.text = FormatManager.getDisplayCoverage(connnectivity)
        holder.binding.distanceText.text = FormatManager.getDisplayDistance(distance)
        holder.binding.timeText.text = FormatManager.getStopWatchTime(duration)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = dataset.size
}