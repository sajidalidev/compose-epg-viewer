package dev.sajidali.jctvguide.utils

import android.util.Log
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sajidali.jctvguide.data.Event

fun Event.isInViewPort(current: Long, max: Long): Boolean {
    return current in start..end || start in current..max
}

suspend fun ScrollableState.animateScrollTo(offset: Float, newOffset: Float) {
    animateScrollBy(newOffset - offset)
}

fun Modifier.onKeyLongPress(action: (event: KeyEvent) -> Boolean): Modifier = composed {
    var time by remember {
        mutableLongStateOf(0)
    }
    onKeyEvent {
        if (it.type == KeyEventType.KeyDown) {
            time = System.currentTimeMillis()
        } else if (it.type == KeyEventType.KeyUp) {
            time = 0
        }
        Log.d("ComposeTvGuide", "onKeyLongPress: ${now - time}")
        if (time > 0 && now - time > 500) {
            Log.d("ComposeTvGuide", "onKeyLongPress: Long Press Called ${now - time}")
            action(it)
            true
        } else false
    }
}

@Composable
fun Float.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.pxToDp() = LocalDensity.current.run { toDp() }

@Composable
fun Int.dpToPx() = LocalDensity.current.run { dp.toPx() }

@Composable
fun Dp.toPx() = LocalDensity.current.run { toPx() }
