package dev.sajidali.jctvguide.utils

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sajidali.jctvguide.TvGuideState
import dev.sajidali.jctvguide.data.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun Event.isInViewPort(current: Long, max: Long): Boolean {
    return current in start..end || start in current..max
}

suspend fun ScrollableState.animateScrollTo(offset: Float, newOffset: Float) {
    animateScrollBy(newOffset - offset)
}

@Composable
fun Float.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.dpToPx() = LocalDensity.current.run { dp.toPx() }

@Composable
fun Dp.toPx() = LocalDensity.current.run { toPx() }

@Composable
fun <T : Any> T.useDebounce(
    delayMillis: Long = 500L,
    // 1. couroutine scope
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit
): T {
    // 2. updating state
    val state by rememberUpdatedState(this)

    // 3. launching the side-effect handler
    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}


@Composable
fun rememberGuideState(
    startTime: Long,
    endTime: Long,
    hoursInViewport: Duration,
    timeSpacing: Duration,
    initialOffset: Long,
    key: String? = null,
) = rememberSaveable(key = key, saver = TvGuideState.Saver) {
    TvGuideState().apply {
        update {
            this.startTime = startTime
            this.endTime = endTime
            this.hoursInViewport = hoursInViewport
            this.timeSpacing = timeSpacing
            if (selectionTime == 0f)
                selectionTime = initialOffset.roundToNearest(timeSpacing).toFloat()
        }
    }
}
