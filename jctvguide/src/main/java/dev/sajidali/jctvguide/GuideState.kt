package dev.sajidali.jctvguide

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import dev.sajidali.jctvguide.utils.now
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Stable
class GuideState {

    var startTime = now.minus(2.days.inWholeMilliseconds)
    var endTime = startTime.plus(3.days.inWholeMilliseconds)
    var hoursInViewport = 2.hours
    var xOffset = mutableFloatStateOf(0f)
    var yOffset = mutableFloatStateOf(0f)
    var selectedChannel = mutableIntStateOf(0)
    var selectedEvent = mutableIntStateOf(-1)
    var viewportWidth = mutableIntStateOf(0)
    var viewportHeight = mutableIntStateOf(0)
    var stopAtNow = mutableStateOf(true)
    var channelAreaWidth = mutableFloatStateOf(250f)
    var channelHeight = mutableFloatStateOf(50f)
    var selectionScale = mutableFloatStateOf(1f)
    var timeBarHeight = mutableFloatStateOf(25f)
    var timeSpacing = mutableStateOf(30.minutes)
    var fastScroll = mutableStateOf(false)
    var timeIncrement = mutableStateOf(30.minutes)
    var channelCount = mutableIntStateOf(0)
    var selectionTime = mutableLongStateOf(0)
    var state = ScrollState(0)

    val roundedStartTime: Long
        get() = startTime - (startTime % timeSpacing.value.inWholeMilliseconds)

    val roundedEndTime: Long
        get() = endTime - (endTime % timeSpacing.value.inWholeMilliseconds)

    val scrollTime: Long
        get() = (roundedStartTime + xOffset.floatValue * millisPerPixel).toLong()

    val maxScrollTime: Long
        get() = (scrollTime + programAreaWidth * millisPerPixel).toLong()

    val programAreaWidth: Float
        get() = viewportWidth.intValue - channelAreaWidth.floatValue

    val millisPerPixel: Long
        get() = hoursInViewport.inWholeMilliseconds / programAreaWidth.toLong()

    val timeCellWidth: Float
        get() = timeSpacing.value.inWholeMilliseconds.toFloat() / millisPerPixel

    val roundedNow: Long
        get() = now - (now % timeIncrement.value.inWholeMilliseconds)

    val nowOffset: Float
        get() = (xOffset.floatValue * millisPerPixel + (roundedNow - roundedStartTime)) / millisPerPixel

    val selectedChannelHeight: Float
        get() = channelHeight.floatValue * selectionScale.floatValue

    fun calculatedYOffset(position: Int): Float {
        return if (position > selectedChannel.intValue) {
            (position - 1) * channelHeight.floatValue + selectedChannelHeight
        } else {
            position * channelHeight.floatValue
        }
    }

    fun channelHeight(position: Int): Float {
        return if (position == selectedChannel.intValue) {
            selectedChannelHeight
        } else {
            channelHeight.floatValue
        }
    }

    fun update(block: GuideState.() -> Unit) {
        block()
    }

}