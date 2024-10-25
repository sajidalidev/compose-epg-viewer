package dev.sajidali.jctvguide

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import dev.sajidali.jctvguide.utils.now
import dev.sajidali.jctvguide.utils.roundToNearest
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Immutable
class TvGuideState {

    companion object {
        val Saver = Saver<TvGuideState, Map<String, Any?>>(
            save = { state ->
                mapOf(
                    "startTime" to state.startTime,
                    "endTime" to state.endTime,
                    "hoursInViewport" to state.hoursInViewport.inWholeMilliseconds,
                    "xOffset" to state.xOffset,
                    "selectedChannel" to state.selectedChannel,
                    "selectedEvent" to state.selectedEvent,
                    "stopAtNow" to state.stopAtNow,
                    "channelAreaWidth" to state.channelAreaWidth,
                    "timeBarHeight" to state.timeBarHeight,
                    "timeSpacing" to state.timeSpacing.inWholeMilliseconds,
                    "timeIncrement" to state.timeIncrement.inWholeMilliseconds,
                    "selectionTime" to state.selectionTime,
                    "channelCount" to state.channelCount
                )
            },
            restore = { savedState ->
                TvGuideState().apply {

                    startTime = savedState["startTime"] as Long
                    endTime = savedState["endTime"] as Long
                    hoursInViewport = (savedState["hoursInViewport"] as Int).milliseconds
                    xOffset = savedState["xOffset"] as Float
                    selectedChannel = savedState["selectedChannel"] as Int
                    selectedEvent = savedState["selectedEvent"] as Int
                    stopAtNow = savedState["stopAtNow"] as Boolean
                    channelAreaWidth = savedState["channelAreaWidth"] as Float
                    timeBarHeight = savedState["timeBarHeight"] as Float
                    timeSpacing = (savedState["timeSpacing"] as Int).milliseconds
                    timeIncrement = (savedState["timeIncrement"] as Int).milliseconds
                    selectionTime = savedState["selectionTime"] as Float
                    channelCount = savedState["channelCount"] as Int
                }
            }
        )

    }

    internal var startTime = now.minus(2.days.inWholeMilliseconds)
    internal var endTime = startTime.plus(3.days.inWholeMilliseconds)
    internal var hoursInViewport = 2.hours
    internal var xOffset by mutableFloatStateOf(0f)
    internal var selectedChannel by mutableIntStateOf(0)
    internal var selectedEvent by mutableIntStateOf(-1)

    //    internal var viewportWidth by mutableIntStateOf(0)
//    internal var viewportHeight by mutableIntStateOf(0)
    internal var stopAtNow by mutableStateOf(true)
    internal var channelAreaWidth by mutableFloatStateOf(250f)
    internal var timeBarHeight by mutableFloatStateOf(25f)
    internal var timeSpacing by mutableStateOf(30.minutes)
    internal var timeIncrement by mutableStateOf(30.minutes)
    internal var selectionTime by mutableFloatStateOf(0f)
    internal var channelCount by mutableIntStateOf(0)
    internal var programAreaWidth by mutableFloatStateOf(0f)

    val roundedStartTime: Float
        get() = startTime.toFloat() - (startTime % timeSpacing.inWholeMilliseconds)

    val roundedEndTime: Float
        get() = endTime.toFloat() - (endTime % timeSpacing.inWholeMilliseconds)

    val scrollTime: Float
        get() = roundedStartTime + xOffset * millisPerPixel

    val maxScrollTime: Float
        get() = scrollTime + programAreaWidth * millisPerPixel

//    val programAreaWidth: Float
//        get() = viewportWidth - channelAreaWidth

    val millisPerPixel: Float
        get() = hoursInViewport.inWholeMilliseconds / programAreaWidth

    val timeCellWidth: Float
        get() = timeSpacing.inWholeMilliseconds.toFloat() / millisPerPixel

    val roundedNow: Float
        get() = now.toFloat() - (now % timeIncrement.inWholeMilliseconds)

    val selectionOffset: Float
        get() = (selectionTime - scrollTime) / millisPerPixel

    val nowOffset: Float
        get() = (xOffset * millisPerPixel + (roundedNow - roundedStartTime)) / millisPerPixel


    //Change it to actually update all block at once
    fun update(block: TvGuideState.() -> Unit) {
        this.block()
    }

//    fun isEmpty(): Boolean {
//        return channelCount == 0
//    }

    fun gotoNow() {
        selectionTime = now.roundToNearest(timeSpacing).toFloat()
    }

    fun reset() {
        selectedChannel = 0
        selectedEvent = -1
        selectionTime = now.roundToNearest(timeSpacing).toFloat()
//        channelCount = 0
    }


}