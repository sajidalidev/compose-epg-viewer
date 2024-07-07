package dev.sajidali.jctvguide

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.data.EventWithIndex
import dev.sajidali.jctvguide.utils.findVisibleEvents
import dev.sajidali.jctvguide.utils.now
import dev.sajidali.jctvguide.utils.pxToDp
import dev.sajidali.jctvguide.utils.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A composable function that creates a TV guide.
 *
 * @param startTime The start time of the guide in milliseconds.
 * @param endTime The end time of the guide in milliseconds.
 * @param hoursInViewport The duration of the viewport in hours.
 * @param timeSpacing The spacing between time cells.
 * @param modifier The modifier to be applied to the guide.
 * @param scrollToNow Whether to scroll to the current time when the guide is displayed.
 * @param fastScroll Whether to enable fast scrolling.
 * @param stopAtNow Whether to stop scrolling at the current time.
 * @param onEventSelected The callback function to be called when an event is selected.
 * @param onEventClicked The callback function to be called when an event is clicked.
 * @param content The content of the guide.
 */
@Composable
fun TvGuide(
    startTime: Long,
    endTime: Long,
    hoursInViewport: Duration,
    timeSpacing: Duration,
    modifier: Modifier = Modifier,
    scrollToNow: Boolean = false,
    fastScroll: Boolean = false,
    stopAtNow: Boolean = false,
    onEventSelected: (channel: Int, event: Int) -> Unit = { _, _ -> },
    onEventClicked: (channel: Int, event: Int) -> Unit = { _, _ -> },
    content: @Composable TvGuideScope.() -> Unit,
) {
    val state = remember {
        GuideState()
    }

    state.update {
        this.startTime = startTime
        this.endTime = endTime
        this.hoursInViewport = hoursInViewport
        this.timeSpacing.value = timeSpacing
        this.fastScroll.value = fastScroll
        this.stopAtNow.value = stopAtNow
    }

    val verticalScrollState = rememberScrollableState { delta ->
        val maxChannelsVisible =
            ((state.viewportHeight.intValue - state.timeBarHeight.floatValue - state.selectedChannelHeight) / state.channelHeight.floatValue) + 1

        val maxValue =
            (state.channelCount.intValue - maxChannelsVisible - 1) * state.channelHeight.floatValue + state.selectedChannelHeight
        val newDelta =
            delta.coerceIn(-state.yOffset.floatValue, maxValue - state.yOffset.floatValue)
        state.yOffset.floatValue += newDelta
        newDelta
    }

    val horizontalScrollState = rememberScrollableState { delta ->
        val maxValue = ((endTime - startTime) / state.millisPerPixel).toFloat()
        val newDelta =
            delta.coerceIn(-state.xOffset.floatValue, maxValue - state.xOffset.floatValue)
        state.xOffset.floatValue += newDelta
        newDelta
    }

    val scope = rememberCoroutineScope()

    val scrollToSelectedChannel = {
        scope.launch {
            verticalScrollState.scrollBy(
                state.channelHeight.floatValue * state.selectedChannel.intValue - state.yOffset.floatValue
            )
        }
    }

    var shouldSkipNow by remember {
        mutableStateOf(false)
    }

    var keyPressTime by remember {
        mutableLongStateOf(0)
    }
    var keyDown by remember {
        mutableStateOf(false)
    }

    var downTime by remember {
        mutableLongStateOf(0)
    }

    var currentTime by remember {
        mutableLongStateOf(now)
    }

    LaunchedEffect(key1 = keyDown) {
        while (keyDown) {
            downTime = now - keyPressTime
            delay(16L)
        }
    }

    LaunchedEffect(state.viewportWidth.intValue, state.viewportHeight.intValue) {
        if (state.viewportWidth.intValue > 0 && state.viewportHeight.intValue > 0) {
            state.xOffset.floatValue = 0f
        }
    }

    LaunchedEffect(scrollToNow, state.viewportWidth.intValue, state.viewportHeight.intValue) {
        if (scrollToNow && state.viewportWidth.intValue > 0 && state.viewportHeight.intValue > 0) {
            state.selectionTime.longValue = now
            scope.launch {
                horizontalScrollState.scrollBy(state.nowOffset)
            }
        }
    }

    LaunchedEffect(currentTime) {
        delay(5.seconds)
        currentTime = now
    }

    LaunchedEffect(state.selectedChannel.intValue, state.selectedEvent.intValue) {
        onEventSelected(state.selectedChannel.intValue, state.selectedEvent.intValue)
    }

    Box(modifier = modifier
        .onGloballyPositioned {
            state.viewportWidth.intValue = it.size.width
            state.viewportHeight.intValue = it.size.height
        }
        .onFocusChanged {
            if (it.isFocused && state.selectedEvent.intValue == -1) {
                state.selectedEvent.intValue = 0
            }
        }
        .focusable()
        .onKeyEvent {
            if (it.type == KeyEventType.KeyDown) {
                if (!keyDown) {
                    keyPressTime = now
                    keyDown = true

                }
            }
            if (it.type == KeyEventType.KeyUp && keyDown) {
                keyDown = false
                return@onKeyEvent false
            }
            when (it.key) {
                Key.DirectionUp -> {
                    if (fastScroll && downTime > 1500) {
                        if (state.selectedChannel.intValue > 5) {
                            state.selectedChannel.intValue -= 5
                        } else {
                            state.selectedChannel.intValue = 0
                        }
                        scrollToSelectedChannel()
                    } else {
                        if (state.selectedChannel.intValue > 0) {
                            state.selectedChannel.intValue--
                            scrollToSelectedChannel()
                        }
                    }
                    true
                }

                Key.DirectionDown -> {
                    if (fastScroll && downTime > 1500) {
                        if (state.selectedChannel.intValue < state.channelCount.intValue - 5) {
                            state.selectedChannel.intValue += 5
                        } else {
                            state.selectedChannel.intValue = state.channelCount.intValue - 1
                        }
                        scrollToSelectedChannel()
                    } else {
                        if (state.selectedChannel.intValue < state.channelCount.intValue - 1) {
                            state.selectedChannel.intValue++
                            scrollToSelectedChannel()
                        }
                    }
                    true
                }

                Key.DirectionLeft -> {
//                    if (keyDown && downTime in 500..1000 && state.selectionTime.longValue == state.roundedNow) {
//                        state.stopAtNow.value = false
//                    }
                    val newSelectionTime =
                        state.selectionTime.longValue - state.timeIncrement.value.inWholeMilliseconds
                    val minSelectionTime =
                        if (state.stopAtNow.value) state.roundedNow else state.roundedStartTime
                    val maxSelectionTime = state.roundedEndTime

                    state.update {
                        selectionTime.longValue =
                            newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)

                        xOffset.floatValue =
                            (selectionTime.longValue - state.roundedStartTime) / state.millisPerPixel.toFloat()
                    }

                    true
                }

                Key.DirectionRight -> {
                    val newSelectionTime =
                        state.selectionTime.longValue + state.timeIncrement.value.inWholeMilliseconds
                    val minSelectionTime = state.roundedStartTime
                    val maxSelectionTime = state.roundedEndTime

                    state.update {
                        selectionTime.longValue =
                            newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)

                        xOffset.floatValue =
                            (newSelectionTime - state.roundedStartTime) / state.millisPerPixel.toFloat()
                    }
                    if (newSelectionTime > state.roundedNow) {
                        state.stopAtNow.value = true
                    }
                    true
                }

                Key.DirectionCenter, Key.Enter, Key.Spacebar -> {
                    onEventClicked(state.selectedChannel.intValue, state.selectedEvent.intValue)
                    true
                }

                else -> false
            }
        }
        .fillMaxSize()
        .scrollable(verticalScrollState, Orientation.Vertical, reverseDirection = true)
        .scrollable(horizontalScrollState, Orientation.Horizontal, reverseDirection = true)
    ) {


        TvGuideScopeImpl(state).content()

    }

}

/**
 *  Implementation of the [TvGuideScope] interface.
 *  @param state The state of the TV guide.
 */

class TvGuideScopeImpl(
    private val state: GuideState
) : TvGuideScope {

    val roundedStartTime =
        state.startTime - (state.startTime % state.timeSpacing.value.inWholeMilliseconds)
    val scrollTime = (roundedStartTime + state.xOffset.floatValue * state.millisPerPixel).toLong()
    val maxScrollTime = (scrollTime + state.programAreaWidth * state.millisPerPixel)
    val centerOfViewport = scrollTime + (state.programAreaWidth / 2) * state.millisPerPixel

    /**
     *  Composable function to display the time bar with the specified height, modifier, and content.
     *  @param height The height of the time bar.
     *  @param modifier The modifier for the time bar.
     *  @param content The content of the time bar.
      */
    @Composable
    override fun Timebar(
        height: Dp,
        modifier: Modifier,
        content: @Composable (TimebarScope.() -> Unit)
    ) {
        state.timeBarHeight.floatValue = height.toPx()

        Box(
            modifier = Modifier
                .height(height)
                .width(state.programAreaWidth.pxToDp())
                .offset(x = state.channelAreaWidth.floatValue.pxToDp())
                .clipToBounds()
                .then(modifier)
        ) {
            DrawTimebar(content)
        }
    }

    /**
     *  Composable function to draw the time bar.
     *  @param content The content of the time bar.
     */
    @Composable
    fun DrawTimebar(content: @Composable (TimebarScope.() -> Unit)) {
        var currentTime =
            scrollTime - (scrollTime % state.timeSpacing.value.inWholeMilliseconds) // Align to nearest increment
        while (currentTime < maxScrollTime) {
            val xOffset = ((currentTime - scrollTime) / state.millisPerPixel).toInt()
            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffset, 0) }
                    .width(state.timeCellWidth.pxToDp())
                    .fillMaxHeight(),
            ) {
                content(TimebarScopeImpl(state, currentTime))
            }
            currentTime += state.timeSpacing.value.inWholeMilliseconds
        }
    }

    /**
     *  Composable function to display the current day with the specified height, modifier, and content.
     *  @param modifier The modifier for the current day.
     *  @param content The content of the current day.
     */
    @Composable
    override fun CurrentDay(
        modifier: Modifier,
        content: @Composable (BoxScope.(time: Long) -> Unit)
    ) {
        Box(
            modifier = Modifier
                .width(state.channelAreaWidth.floatValue.pxToDp())
                .height(state.timeBarHeight.floatValue.pxToDp())
                .then(modifier)
        ) {
            content(scrollTime)
        }
    }

    /**
     *  Composable function to display the channels with the specified width, height, selection scale, item count, modifier, and content.
     *  @param width The width of the channels area.
     *  @param height The height of a single channel row.
     *  @param selectionScale The selected channel's row scale.
     *  @param itemCount The item count of the channels.
     *  @param modifier The modifier for the channels.
     *  @param content The content of the channels.
     */
    @Composable
    override fun Channels(
        width: Dp,
        height: Dp,
        selectionScale: Float,
        itemCount: Int,
        modifier: Modifier,
        content: @Composable (ChannelScope.(channel: Int, isSelected: Boolean) -> Unit)
    ) {

        val widthPx = width.toPx()
        val heightPx = height.toPx()

        state.update {
            channelAreaWidth.floatValue = widthPx
            this.channelHeight.floatValue = heightPx
            this.selectionScale.floatValue = selectionScale
            channelCount.intValue = itemCount
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset {
                    IntOffset(
                        x = 0,
                        y = (state.timeBarHeight.floatValue + state.yOffset.floatValue).toInt()
                    )
                }
                .then(modifier)
        ) {

            val first =
                (state.yOffset.floatValue.toInt() / state.channelHeight.floatValue.toInt()).let {
                    if (it < 0) 0 else it
                }
            val last =
                (((state.yOffset.floatValue.toInt() + state.viewportHeight.intValue - state.selectedChannelHeight.toInt()) / state.channelHeight.floatValue.toInt()) + 1).let {
                    if (it > itemCount - 1) itemCount - 1 else it
                }

            for (pos in first..last) {
                val yOffset =
                    if (pos <= state.selectedChannel.intValue)
                        (pos - first) * state.channelHeight.floatValue - state.yOffset.floatValue
                    else
                        (pos - first - 1) * state.channelHeight.floatValue + state.selectedChannelHeight - state.yOffset.floatValue
                key(pos) {
                    Box(
                        modifier = Modifier
                            .height(
                                state
                                    .channelHeight(pos)
                                    .pxToDp()
                            )
                            .fillMaxWidth()
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = yOffset.toInt()
                                )
                            }
                            .clickable {
                                state.selectedChannel.intValue = pos
                            }
                    ) {
                        content(
                            ChannelScopeImpl(state, pos),
                            pos,
                            pos == state.selectedChannel.intValue
                        )
                    }
                }
            }


        }

    }

    /**
     *  Composable function to display the current time indicator with the specified modifier and content.
     *  @param modifier The modifier for the current time.
     *  @param content The content of the current time indicator.
     */
    @Composable
    override fun Now(modifier: Modifier, content: @Composable (BoxScope.(time: Long) -> Unit)) {
        val nowOffset = (now - scrollTime) / state.millisPerPixel
        if (now in state.scrollTime..state.maxScrollTime) {
            Box(modifier = Modifier
                .offset {
                    IntOffset(
                        (state.channelAreaWidth.floatValue + nowOffset).toInt(),
                        0
                    )
                }
                .then(modifier)) {
                content(now)
            }
        }
    }

}

class EventScopeImpl(
    private val state: GuideState,
    private val channel: Int,
    private val event: EventWithIndex,
) : EventScope {

    @Composable
    override fun EventCell(
        modifier: Modifier,
        content: @Composable (BoxScope.() -> Unit)
    ) {
        if (state.selectedChannel.intValue == channel && state.selectionTime.longValue in event.event.start..event.event.end) {
            state.selectedEvent.intValue = event.index
        }
        val xOffset = maxOf(
            (event.event.start - state.roundedStartTime) / state.millisPerPixel,
            state.xOffset.floatValue.toLong()
        )
        val endVisibleTime = minOf(event.event.end, state.maxScrollTime)
        val startVisibleTime = maxOf(event.event.start, state.scrollTime)
        val width = (endVisibleTime - startVisibleTime) / state.millisPerPixel

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (xOffset - state.xOffset.floatValue).toInt(),
                        y = 0
                    )
                }
                .width(
                    width
                        .toInt()
                        .pxToDp()
                )
                .fillMaxHeight()
                .clickable {
                    state.update {
                        selectedChannel.intValue = channel
                        selectionTime.longValue = (endVisibleTime - startVisibleTime) / 2
                    }
                }
                .then(modifier)
        ) {
            content()
        }

    }


    override fun Modifier.progressBackground(
        color: Color,
        shape: Shape
    ): Modifier {
        return this then Modifier.drawBehind {
            val endVisibleTime = minOf(event.event.end, now)
            val startVisibleTime = maxOf(event.event.start, state.scrollTime)
            val visibleEventWidth = (endVisibleTime - startVisibleTime) / state.millisPerPixel
            if (visibleEventWidth < 0) {
                return@drawBehind
            }
            val size = Size(visibleEventWidth.toFloat(), size.height)
            when (shape) {
                RectangleShape -> {
                    drawRect(
                        color = color,
                        size = size
                    )
                }

                else -> {
                    drawOutline(shape.createOutline(size, layoutDirection, this), color)
                }

            }
        }
    }
}

/**
 * Implementation of [ChannelScope] for managing channel-related operations in the TV Guide.
 *
 * @property state Current state of the TV Guide.
 * @property channel The channel index.
 */

class ChannelScopeImpl(
    private val state: GuideState,
    private val channel: Int
) : ChannelScope {


    @Composable
    override fun ChannelCell(
        modifier: Modifier,
        content: @Composable (BoxScope.() -> Unit)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(state.channelAreaWidth.floatValue.pxToDp())
                .then(modifier)
        ) {
            content()
        }
    }

    @Composable
    override fun Events(
        modifier: Modifier,
        events: List<Event>,
        content: @Composable (EventScope.(event: Event, isSelected: Boolean) -> Unit)
    ) {

        val visibleEvents = /*remember(channel, state.scrollTime, state.maxScrollTime) {*/
            events.findVisibleEvents(state.scrollTime, state.maxScrollTime)
        /*}*/

        Box(modifier = Modifier
            .offset {
                IntOffset(
                    x = state.channelAreaWidth.floatValue.toInt(),
                    y = 0
                )
            }
            .width(state.programAreaWidth.pxToDp())
            .then(modifier)) {
            for (event in visibleEvents) {
                content(
                    EventScopeImpl(state, channel, event),
                    event.event,
                    state.selectedChannel.intValue == channel && event.index == state.selectedEvent.intValue
                )
            }
        }

    }

}

/**
 * Implementation of [TimebarScope] for managing timebar-related operations in the TV Guide.
 *
 * @property state Current state of the TV Guide.
 * @property time The time for the time cell.
 */

class TimebarScopeImpl(
    private val state: GuideState,
    private val time: Long
) : TimebarScope {

    @Composable
    override fun TimeCell(modifier: Modifier, content: @Composable BoxScope.(time: Long) -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(state.timeBarHeight.floatValue.pxToDp())
                .then(modifier)
        ) {
            content(time)
        }
    }
}
