package dev.sajidali.jctvguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntSize
import dev.sajidali.jctvguide.data.EventWithIndex

abstract class TvGuideScope(val size: IntSize)

abstract class HeaderScope

abstract class TimeCellScope(val time: Long)

abstract class ChannelRowScope(val position: Int)

abstract class ChannelScope(val channel: Int)

abstract class EventScope(val channel: Int, val event: EventWithIndex) {
    @Stable
    @Composable
    abstract fun Modifier.progressBackground(
        color: Color, shape: Shape
    ): Modifier
}