package dev.sajidali.jctvguide

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import dev.sajidali.jctvguide.data.Event

/**
 *  Interface defining the scope for handling TV Guide operations.
 */
interface TvGuideScope {

    /**
     *  Composable function to render the timebar.
     *
     *  @param height Height of the timebar.
     *  @param modifier Modifier to be applied to the timebar.
     *  @param content Composable content to be displayed within the timebar.
     */
    @Composable
    fun Timebar(height: Dp, modifier: Modifier, content: @Composable TimebarScope.() -> Unit)

    /**
     *  Composable function to render the current day.
     *
     *  @param modifier Modifier to be applied to the current day.
     *  @param content Composable content to be displayed within the current day.
     */
    @Composable
    fun CurrentDay(modifier: Modifier, content: @Composable BoxScope.(time: Long) -> Unit)

    /**
     *  Composable function to render the channels.
     *
     *  @param width Width of the channels area.
     *  @param height Height of the channel row.
     *  @param selectionScale Scale of the selected channel's row.
     *  @param itemCount Number of channels to be rendered.
     *  @param modifier Modifier to be applied to the channels.
     *  @param content Composable content to be displayed within the channels.
     */
    @Composable
    fun Channels(
        width: Dp,
        height: Dp,
        selectionScale: Float,
        itemCount: Int,
        modifier: Modifier,
        content: @Composable ChannelScope.(channel: Int, isSelected: Boolean) -> Unit
    )

    /**
     *  Composable function to render the current time indicator.
     *
     *  @param modifier Modifier to be applied to the current time.
     *  @param content Composable content to be displayed within the current time.
     */

    @Composable
    fun Now(modifier: Modifier, content: @Composable BoxScope.(time: Long) -> Unit)

}

/**
 * Interface defining the scope for handling timebar operations in the TV Guide.
 */

interface TimebarScope {

    /**
     * Composable function to render a time cell.
     *
     * @param modifier Modifier to be applied to the time cell.
     * @param content Composable content to be displayed within the time cell, with the time parameter.
     */

    @Composable
    fun TimeCell(modifier: Modifier, content: @Composable BoxScope.(time: Long) -> Unit)
}

/**
 * Interface defining the scope for handling channel operations in the TV Guide.
 */
interface ChannelScope {

    /**
     * Composable function to render a channel cell.
     *
     * @param modifier Modifier to be applied to the channel cell.
     * @param content Composable content to be displayed within the channel cell.
     */

    @Composable
    fun ChannelCell(
        modifier: Modifier,
        content: @Composable BoxScope.() -> Unit
    )

    /**
     * Composable function to render events for a specific channel.
     *
     * @param modifier Modifier to be applied to the events.
     * @param events List of events to be displayed.
     * @param content Composable content to be displayed for each event, with the event and its selection status.
     */

    @Composable
    fun Events(
        modifier: Modifier,
        events: List<Event>,
        content: @Composable EventScope.(event: Event, isSelected: Boolean) -> Unit
    )
}

/**
 *  Interface defining the scope for handling event operations in the TV Guide.
 */
interface EventScope {

    /**
     *  Composable function to render an event cell.
     *
     *  @param modifier Modifier to be applied to the event cell.
     *  @param content Composable content to be displayed within the event cell.
     */
    @Composable
    fun EventCell(
        modifier: Modifier,
        content: @Composable BoxScope.() -> Unit
    )

    /**
     *  Modifier to draw progress background for the event cell.
     *
     *  @param color Color of the background.
     *  @param shape Shape of the background.
     */
    @Stable
    fun Modifier.progressBackground(
        color: Color, shape: Shape = RectangleShape
    ): Modifier
}