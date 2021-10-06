package com.michaelkoohang.netwatch.ui.recordings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.view.MotionEventCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.utils.ColorUtils
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentRecordingDetailBinding
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.model.viewmodel.RecordingsViewModel
import com.michaelkoohang.netwatch.utils.CalcManager
import com.michaelkoohang.netwatch.utils.FormatManager
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class RecordingDetailFragment : Fragment() {
    private var _binding: FragmentRecordingDetailBinding? = null
    private val binding get() = _binding!!

    private val model: RecordingsViewModel by activityViewModels()
    private lateinit var recording: RecordingResponse
    private lateinit var position: CameraPosition

    private var lastY: Float = 0f
    private var cardPositionY: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        _binding = FragmentRecordingDetailBinding.inflate(inflater, container, false)
        cardPositionY = binding.recordingDetailCard.translationY / requireContext().resources.displayMetrics.density

        binding.mapViewDetail.onCreate(savedInstanceState)
        model.getSelectedRecording().observe(viewLifecycleOwner, { recording ->
            this.recording = recording
            setupMap(recording)
            setupUI()
            updateCardInfo()
        })
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.recordingDetailCard.setOnTouchListener { _, event ->
            val displayMetrics = requireContext().resources.displayMetrics
            val topHeight = (cardPositionY - 150) * displayMetrics.density
            val bottomHeight = cardPositionY * displayMetrics.density

            when (MotionEventCompat.getActionMasked(event)) {
                MotionEvent.ACTION_DOWN -> {
                    lastY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (lastY > event.y) {
                        SpringAnimation(binding.recordingDetailCard, DynamicAnimation.TRANSLATION_Y, topHeight).apply {
                            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                            spring.stiffness = SpringForce.STIFFNESS_LOW
                            start()
                        }
                    } else {
                        SpringAnimation(binding.recordingDetailCard, DynamicAnimation.TRANSLATION_Y, bottomHeight).apply {
                            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                            spring.stiffness = SpringForce.STIFFNESS_LOW
                            start()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMap(recording: RecordingResponse) {
        binding.mapViewDetail.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.OUTDOORS) { style ->
                position = CameraPosition.Builder()
                        .target(LatLng(recording.features!![0].lat!!, recording.features[0].lon!!))
                        .zoom(14.0)
                        .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                val circleManager = CircleManager(binding.mapViewDetail, mapboxMap, style)
                val circleOptionsList = ArrayList<CircleOptions>()
                for (i in 0 until recording.features.size) {
                    val connected = recording.features[i].connected
                    val strokeColor = resources.getColor(R.color.white, resources.newTheme())
                    val circleColor = if (connected != 0) {
                        resources.getColor(R.color.blue, resources.newTheme())
                    } else {
                        resources.getColor(R.color.red, resources.newTheme())
                    }
                    circleOptionsList.add(CircleOptions()
                        .withLatLng(LatLng(recording.features[i].lat!!, recording.features[i].lon!!))
                        .withCircleColor(ColorUtils.colorToRgbaString(circleColor))
                        .withCircleStrokeColor(ColorUtils.colorToRgbaString(strokeColor))
                        .withCircleStrokeWidth(2f)
                        .withCircleRadius(5f)
                        .withDraggable(false)
                    )
                }
                circleManager.create(circleOptionsList)
            }
        }
    }

    private fun updateCardInfo() {
        binding.dateText.text = FormatManager.getDisplayDate(recording.start!!)
        binding.descriptionText.text = FormatManager.getRecordingDescription(recording.start!!)
        binding.carrierText.text = recording.carrier
        binding.timeText.text = FormatManager.getStopWatchTime(recording.duration!!)
        binding.distanceText.text = FormatManager.getDisplayDistance(recording.distance!!)
        updateConnectivity(CalcManager.calcConnectivity(recording))
    }

    private fun updateConnectivity(percentage: Double) {
        val bar = binding.connectivityView.connectivityBar
        val displayMetrics = requireContext().resources.displayMetrics
        val currentWidth = displayMetrics.widthPixels / displayMetrics.density
        val newWidth = (currentWidth - CONNECTIVITY_BAR_SPACING) * percentage
        val disconnectedBar = binding.connectivityView.connectivityBarDisconnected
        disconnectedBar.layoutParams.width = ((currentWidth - CONNECTIVITY_BAR_SPACING) * displayMetrics.density).roundToInt()

        if (newWidth > 0) {
            bar.visibility = View.VISIBLE
            bar.layoutParams.width = (newWidth * displayMetrics.density).roundToInt()
        } else {
            bar.visibility = View.GONE
        }

        binding.connectivityView.connectivityPercentage.text = FormatManager.getDisplayCoverage(percentage)
        if (percentage >= 0.5) {
            binding.connectivityView.connectivityPercentage.setTextColor(
                resources.getColor(R.color.green, resources.newTheme())
            )
        } else {
            binding.connectivityView.connectivityPercentage.setTextColor(
                resources.getColor(R.color.red, resources.newTheme())
            )
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewDetail.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewDetail.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapViewDetail.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapViewDetail.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewDetail.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapViewDetail.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapViewDetail.onSaveInstanceState(outState)
    }

    companion object {
        private const val CONNECTIVITY_BAR_SPACING: Int = 64
    }
}

