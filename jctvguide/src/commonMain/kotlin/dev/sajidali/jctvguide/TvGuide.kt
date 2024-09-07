@file:OptIn(ExperimentalFoundationApi::class)

package dev.sajidali.jctvguide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.data.EventWithIndex
import dev.sajidali.jctvguide.utils.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

internal val LocalHorizontalScrollState = compositionLocalOf<ScrollableState> {
    error("No HorizontalScrollState Provided")
}

@Composable
fun TvGuide(
    state: GuideState,
    modifier: Modifier = Modifier,
    fastScroll: Boolean = false,
    onStartReached: () -> Unit = {},
    onEndReached: () -> Unit = {},
    onEventSelected: (channel: Int, event: Int) -> Unit = { _, _ -> },
    nowIndicator: @Composable BoxScope.() -> Unit = {},
    content: @Composable TvGuideScope.() -> Unit,
) {

    val scope = rememberCoroutineScope()

    val horizontalScrollState = rememberScrollableState { delta ->
        val maxValue = ((state.endTime - state.startTime) / state.millisPerPixel)
        if (maxValue < 0) return@rememberScrollableState 0f
        val newDelta =
            delta.coerceIn(-state.xOffset.floatValue, maxValue - state.xOffset.floatValue)
        state.xOffset.floatValue += newDelta
        newDelta
    }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            horizontalScrollState.scrollBy(-delta)
        }
    }

    LaunchedEffect(state.viewportWidth.intValue, state.viewportHeight.intValue) {
        if (state.viewportWidth.intValue > 0 && state.viewportHeight.intValue > 0) {
            horizontalScrollState.scrollBy(state.selectionOffset)
        }
    }

    LaunchedEffect(state.selectionTime.floatValue) {
        horizontalScrollState.animateScrollBy(state.selectionOffset)
    }

    LaunchedEffect(state.selectedChannel.intValue, state.selectedEvent.intValue) {
        onEventSelected(state.selectedChannel.intValue, state.selectedEvent.intValue)
    }


    CompositionLocalProvider(
        LocalGuideState provides state,
        LocalHorizontalScrollState provides horizontalScrollState,
        LocalBringIntoViewSpec provides DefaultBringIntoViewSpec()
    ) {
        Box(modifier = modifier
            .keyEvent(onStartReached, onEndReached)
            .onGloballyPositioned {
                state.viewportWidth.intValue = it.size.width
                state.viewportHeight.intValue = it.size.height
            }
            .onFocusChanged {
                if (it.hasFocus && state.selectedEvent.intValue == -1) {
                    state.selectedEvent.intValue = 0
                }
            }
            .fillMaxSize()
            .scrollable(
                horizontalScrollState,
                Orientation.Horizontal,
                reverseDirection = true,
            )
            .draggable(orientation = Orientation.Horizontal, state = draggableState)
            .focusable()
        ) {

            Column(modifier = Modifier.fillMaxSize()) {
                TvGuideScopeImpl().content()
            }

            Now(modifier = Modifier) {
                nowIndicator()
            }

        }
    }

}

val LocalGuideState = compositionLocalOf<GuideState> { error("No GuideState Provided") }

@Composable
fun TvGuideScope.Header(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable HeaderScope.() -> Unit
) {
    val state = LocalGuideState.current
    state.timeBarHeight.floatValue = height.toPx()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
            .then(modifier)
    ) {
        content(HeaderScopeImpl())
    }
}

@Composable
fun HeaderScope.Timebar(
    modifier: Modifier = Modifier,
    content: @Composable (TimeCellScope.() -> Unit)
) {
    val state = LocalGuideState.current
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .clipToBounds()
            .then(modifier)
    ) {
        for (time in state.scrollTime.toLong()..state.scrollTime.toLong() + state.hoursInViewport.inWholeMilliseconds step state.timeSpacing.value.inWholeMilliseconds) {
            val roundTime = time - time % state.timeSpacing.value.inWholeMilliseconds
            val xOffset =
                ((roundTime - state.scrollTime) / state.millisPerPixel).toInt() - 40 // Find a better way to align time labels in center
            Box(
                modifier = Modifier
                    .width(state.timeCellWidth.pxToDp())
                    .absoluteOffset { IntOffset(x = xOffset, y = 0) }
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                content(TimeCellScopeImpl(roundTime))
            }
        }
    }
}

class TvGuideScopeImpl : TvGuideScope()

class HeaderScopeImpl : HeaderScope()

@Composable
fun HeaderScope.CurrentDay(
    width: Dp,
    modifier: Modifier,
    content: @Composable (BoxScope.(time: Long) -> Unit)
) {
    val state = LocalGuideState.current
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .then(modifier)
    ) {
        content(state.scrollTime.toLong())
    }
}

@Composable
fun TvGuideScope.Channels(
    width: Dp,
    itemsCount: Int,
    modifier: Modifier,
    content: @Composable (ChannelRowScope.(channel: Int, isSelected: Boolean) -> Unit)
) {
    val state = LocalGuideState.current

    val widthPx = width.toPx()
    state.update {
        channelAreaWidth.floatValue = widthPx
        channelCount.intValue = itemsCount
    }
    var height by remember { mutableIntStateOf(0) }
    val channelState = rememberLazyListState(state.selectedChannel.intValue, -height / 4)

    LaunchedEffect(state.viewportWidth.intValue, state.viewportHeight.intValue) {
        channelState.scrollToItem(state.selectedChannel.intValue, -height / 4)
    }

    LaunchedEffect(state.selectedChannel.intValue) {
        channelState.animateScrollToItem(state.selectedChannel.intValue, -height / 4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                height = it.size.height
            }
            .then(modifier),
        state = channelState
    ) {

        items(state.channelCount.intValue) { pos ->
            content(
                ChannelRowScopeImpl(pos),
                pos,
                state.selectedChannel.intValue == pos
            )
        }

    }

}

@Composable
private fun Now(modifier: Modifier, content: @Composable (BoxScope.(time: Long) -> Unit)) {
    val state = LocalGuideState.current

    val nowOffset = (now - state.scrollTime) / state.millisPerPixel
    if (now.toFloat() in state.scrollTime..state.maxScrollTime) {
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


class ChannelRowScopeImpl(
    position: Int
) : ChannelRowScope(position)


@Composable
fun ChannelRowScope.ChannelRow(
    modifier: Modifier,
    content: @Composable ChannelScope.(position: Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        ChannelScopeImpl(position).content(position)
    }
}


class EventScopeImpl(
    channel: Int,
    event: EventWithIndex,
) : EventScope(channel, event) {

    @Composable
    override fun Modifier.progressBackground(
        color: Color,
        shape: Shape
    ): Modifier {
        val state = LocalGuideState.current
        return this then Modifier
            .drawBehind {
                val endVisibleTime = minOf(event.event.end, now)
                val startVisibleTime = maxOf(event.event.start.toFloat(), state.scrollTime)
                val visibleEventWidth = if (endVisibleTime == event.event.end)
                    size.width
                else
                    (endVisibleTime - startVisibleTime) / state.millisPerPixel
                if (visibleEventWidth < 0) {
                    return@drawBehind
                }

                val size = size.copy(width = visibleEventWidth)
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

@Composable
fun EventScope.EventCell(
    modifier: Modifier = Modifier,
    requestFocus: Boolean = false,
    onClick: (channel: Int, event: Int) -> Unit,
    content: @Composable (BoxScope.() -> Unit)
) {

    val horizontalScrollState = LocalHorizontalScrollState.current
    val scope = rememberCoroutineScope()
    val state = LocalGuideState.current
    val endVisibleTime = minOf(event.event.end.toFloat(), state.maxScrollTime)
    val startVisibleTime = maxOf(event.event.start.toFloat(), state.scrollTime)
    val width = (endVisibleTime - startVisibleTime) / state.millisPerPixel

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.selectedChannel.intValue, state.selectionTime.floatValue) {
        if (state.selectedChannel.intValue == channel && state.selectionTime.floatValue in event.event.start.toFloat()..event.event.end.toFloat()) {
            state.selectedEvent.intValue = event.index
            if (requestFocus)
                focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .width(
                width
                    .toInt()
                    .pxToDp()
            )
            .focusRequester(focusRequester)
            .fillMaxHeight()
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) return@onKeyEvent false
                when (it.key) {
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                        onClick(state.selectedChannel.intValue, state.selectedEvent.intValue)
                        true
                    }

                    else -> false
                }
            }
            .pointerInput(channel, event.index) {
                detectTapGestures {
                    scope.launch {
                        horizontalScrollState.animateScrollBy((state.scrollTime - startVisibleTime) / state.millisPerPixel)
                    }
                    state.update {
                        selectedChannel.intValue = channel
                        selectedEvent.intValue = event.index
                        onClick(channel, event.index)
                    }
                }
            }
            .focusable()
            .then(modifier)
    ) {
        content()
    }

}


@Composable
fun ChannelScope.ChannelCell(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable (BoxScope.() -> Unit)
) {

    val state = LocalGuideState.current

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(state.channelAreaWidth.floatValue.pxToDp())
            .pointerInput(channel) {
                detectTapGestures(onPress = {
                    state.update {
                        selectedChannel.intValue = channel
                        onClick()
                    }
                })
            }
            .then(modifier)
    ) {
        content()
    }
}

@Composable
fun Modifier.keyEvent(onStartReached: () -> Unit = {}, onEndReached: () -> Unit = {}): Modifier {
    val state = LocalGuideState.current

    var shouldSkipNow by remember {
        mutableStateOf(false)
    }

    var shouldTrySkipping by remember {
        mutableStateOf(false)
    }

    return this then Modifier.onKeyEvent {
        if (it.type == KeyEventType.KeyUp) {
            val newSelectionTime =
                state.selectionTime.floatValue - state.timeIncrement.value.inWholeMilliseconds
            if (it.key == Key.DirectionLeft && shouldTrySkipping && newSelectionTime < now) {
                onStartReached()
                return@onKeyEvent true
            }
            shouldTrySkipping = true
            return@onKeyEvent false
        }
        when (it.key) {

            Key.Back, Key.Escape -> {
                if (now.toFloat() in state.scrollTime..state.maxScrollTime) {
                    onStartReached()
                    true
                } else {
                    state.selectionTime.floatValue =
                        now.roundToNearest(state.timeSpacing.value).toFloat()
                    true
                }
            }

            Key.DirectionLeft -> {
                val newSelectionTime =
                    state.selectionTime.floatValue - state.timeIncrement.value.inWholeMilliseconds
                val minSelectionTime =
                    if (state.stopAtNow.value && !shouldSkipNow) state.roundedNow else state.roundedStartTime
                val maxSelectionTime = state.roundedEndTime
                state.update {
                    selectionTime.floatValue =
                        newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)
                }
                if (newSelectionTime - 1.hours.inWholeMilliseconds > now && it.pressedDuration == 0) {
                    shouldTrySkipping = false
                }
                if (shouldTrySkipping && newSelectionTime < now && it.pressedDuration > 1500) {
                    shouldSkipNow = true
                }

                true
            }

            Key.DirectionRight -> {

                val newSelectionTime =
                    state.selectionTime.floatValue + state.timeIncrement.value.inWholeMilliseconds
                val minSelectionTime = state.roundedStartTime
                val maxSelectionTime = state.roundedEndTime

                state.update {
                    selectionTime.floatValue =
                        newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)
                }
                if (newSelectionTime > state.roundedNow) {
                    shouldSkipNow = false
                }
                true
            }

            Key.DirectionUp -> {
                state.selectedChannel.intValue =
                    (state.selectedChannel.intValue - 1).coerceAtLeast(0)
                true
            }

            Key.DirectionDown -> {
                state.selectedChannel.intValue =
                    (state.selectedChannel.intValue + 1).coerceAtMost(state.channelCount.intValue - 1)
                true
            }

            else -> false
        }
    }
}

class ChannelScopeImpl(
    channel: Int
) : ChannelScope(channel)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelScope.Events(
    modifier: Modifier,
    events: List<Event>,
    content: @Composable (EventScope.(event: Event, isSelected: Boolean) -> Unit)
) {

    val state = LocalGuideState.current
    val visibleEvents = /*remember(channel, state.scrollTime, state.maxScrollTime) {*/
        events.findVisibleEvents(state.scrollTime, state.maxScrollTime)
    /*}*/

//    LaunchedEffect(state.selectedEvent.intValue) {
//        if (state.selectedEvent.intValue == -1) {
//            state.selectedEvent.intValue = events.indexOfFirst { it.isCurrent }
//        }
//    }
//
//    LaunchedEffect(state.selectedChannel.intValue) {
//        if (state.selectedChannel.intValue == channel) {
//            state.selectedEvent.intValue =
//                events.indexOfFirst { it.isAroundTime(state.scrollTime.toLong()) }
//        }
//    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        for (event in visibleEvents) {
            content(
                EventScopeImpl(channel, event),
                event.event,
                state.selectedChannel.intValue == channel && state.selectedEvent.intValue == event.index
            )
        }
    }

}

class TimeCellScopeImpl(
    time: Long
) : TimeCellScope(time)

@Composable
fun TimeCellScope.TimeCell(modifier: Modifier, content: @Composable BoxScope.(time: Long) -> Unit) {
    val state = LocalGuideState.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(state.timeBarHeight.floatValue.pxToDp())
            .then(modifier)
    ) {
        content(time)
    }
}
