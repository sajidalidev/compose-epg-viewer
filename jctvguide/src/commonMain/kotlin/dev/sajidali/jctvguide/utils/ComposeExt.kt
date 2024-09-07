package dev.sajidali.jctvguide.utils

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sajidali.jctvguide.GuideState
import dev.sajidali.jctvguide.data.Event
import kotlin.time.Duration

fun Event.isInViewPort(current: Long, max: Long): Boolean {
    return current in start..end || start in current..max
}

suspend fun ScrollableState.animateScrollTo(offset: Float, newOffset: Float) {
    animateScrollBy(newOffset - offset)
}

val KeyEvent.pressedDuration
    get() = 0 //if (this.type == KeyEventType.KeyDown) nativeKeyEvent.eventTime - nativeKeyEvent.downTime else 0

@Composable
fun Float.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.dpToPx() = LocalDensity.current.run { dp.toPx() }

@Composable
fun Dp.toPx() = LocalDensity.current.run { toPx() }

@Composable
fun rememberGuideState(
    startTime: Long,
    endTime: Long,
    hoursInViewport: Duration,
    timeSpacing: Duration,
    initialOffset: Long,
    key: String? = null,
) = rememberSaveable(key = key, saver = GuideState.Saver) {
    GuideState().apply {
        update {
            this.startTime = startTime
            this.endTime = endTime
            this.hoursInViewport = hoursInViewport
            this.timeSpacing.value = timeSpacing
            if (selectionTime.floatValue == 0f)
                selectionTime.floatValue = initialOffset.roundToNearest(timeSpacing).toFloat()
        }
    }
}
