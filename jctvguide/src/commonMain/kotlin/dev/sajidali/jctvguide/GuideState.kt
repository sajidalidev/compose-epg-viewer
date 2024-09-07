package dev.sajidali.jctvguide

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import dev.sajidali.jctvguide.utils.now
import dev.sajidali.jctvguide.utils.roundToNearest
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Stable
class GuideState {

    companion object {
        val Saver = mapSaver(
            save = { state ->
                mapOf(
                    "startTime" to state.startTime,
                    "endTime" to state.endTime,
                    "hoursInViewport" to state.hoursInViewport.inWholeMilliseconds,
                    "xOffset" to state.xOffset.floatValue,
                    "selectedChannel" to state.selectedChannel.intValue,
                    "selectedEvent" to state.selectedEvent.intValue,
                    "viewportWidth" to state.viewportWidth.intValue,
                    "viewportHeight" to state.viewportHeight.intValue,
                    "stopAtNow" to state.stopAtNow.value,
                    "channelAreaWidth" to state.channelAreaWidth.floatValue,
                    "timeBarHeight" to state.timeBarHeight.floatValue,
                    "timeSpacing" to state.timeSpacing.value.inWholeMilliseconds,
                    "timeIncrement" to state.timeIncrement.value.inWholeMilliseconds,
                    "selectionTime" to state.selectionTime.floatValue,
                    "channelCount" to state.channelCount.intValue
                )
            },
            restore = { savedState ->
                GuideState().apply {
                    update {
                        startTime = savedState["startTime"] as Long
                        endTime = savedState["endTime"] as Long
                        hoursInViewport = (savedState["hoursInViewport"] as Long).milliseconds
                        xOffset.floatValue = savedState["xOffset"] as Float
                        selectedChannel.intValue = savedState["selectedChannel"] as Int
                        selectedEvent.intValue = savedState["selectedEvent"] as Int
                        viewportWidth.intValue = savedState["viewportWidth"] as Int
                        viewportHeight.intValue = savedState["viewportHeight"] as Int
                        stopAtNow.value = savedState["stopAtNow"] as Boolean
                        channelAreaWidth.floatValue = savedState["channelAreaWidth"] as Float
                        timeBarHeight.floatValue = savedState["timeBarHeight"] as Float
                        timeSpacing.value = (savedState["timeSpacing"] as Long).milliseconds
                        timeIncrement.value = (savedState["timeIncrement"] as Long).milliseconds
                        selectionTime.floatValue = savedState["selectionTime"] as Float
                        channelCount.intValue = savedState["channelCount"] as Int
                    }
                }
            }
        )

    }

    var startTime = now.minus(2.days.inWholeMilliseconds)
    var endTime = startTime.plus(3.days.inWholeMilliseconds)
    var hoursInViewport = 2.hours
    var xOffset = mutableFloatStateOf(0f)
    var selectedChannel = mutableIntStateOf(0)
    var selectedEvent = mutableIntStateOf(-1)
    var viewportWidth = mutableIntStateOf(0)
    var viewportHeight = mutableIntStateOf(0)
    var stopAtNow = mutableStateOf(true)
    var channelAreaWidth = mutableFloatStateOf(250f)
    var timeBarHeight = mutableFloatStateOf(25f)
    var timeSpacing = mutableStateOf(30.minutes)
    var timeIncrement = mutableStateOf(30.minutes)
    var selectionTime = mutableFloatStateOf(0f)
    var channelCount = mutableIntStateOf(0)

    val roundedStartTime: Float
        get() = startTime.toFloat() - (startTime % timeSpacing.value.inWholeMilliseconds)

    val roundedEndTime: Float
        get() = endTime.toFloat() - (endTime % timeSpacing.value.inWholeMilliseconds)

    val scrollTime: Float
        get() = roundedStartTime + xOffset.floatValue * millisPerPixel

    val maxScrollTime: Float
        get() = scrollTime + programAreaWidth * millisPerPixel

    val programAreaWidth: Float
        get() = viewportWidth.intValue - channelAreaWidth.floatValue

    val millisPerPixel: Float
        get() = hoursInViewport.inWholeMilliseconds / programAreaWidth

    val timeCellWidth: Float
        get() = timeSpacing.value.inWholeMilliseconds.toFloat() / millisPerPixel

    val roundedNow: Float
        get() = now.toFloat() - (now % timeIncrement.value.inWholeMilliseconds)

    val selectionOffset: Float
        get() = (selectionTime.floatValue - scrollTime) / millisPerPixel

    val nowOffset: Float
        get() = (xOffset.floatValue * millisPerPixel + (roundedNow - roundedStartTime)) / millisPerPixel

    fun update(block: GuideState.() -> Unit) {
        block()
    }

    fun reset() {
        update {
            selectedChannel.intValue = 0
            selectedEvent.intValue = -1
            selectionTime.floatValue = now.roundToNearest(timeSpacing.value).toFloat()
            channelCount.intValue = 0
        }
    }


}