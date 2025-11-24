@file:OptIn(ExperimentalFoundationApi::class)

package dev.sajidali.tvguide

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import dev.sajidali.tvguide.data.Event
import dev.sajidali.tvguide.data.EventWithIndex
import dev.sajidali.tvguide.utils.*
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal val LocalHorizontalScrollState = compositionLocalOf<ScrollableState> {
    error("No HorizontalScrollState Provided")
}

@Composable
fun TvGuide(
    state: TvGuideState,
    modifier: Modifier = Modifier,
    onStartReached: () -> Unit = {},
    onEndReached: () -> Unit = {},
    nowIndicator: @Composable BoxScope.() -> Unit = {},
    content: @Composable TvGuideScope.() -> Unit,
) {

    val horizontalScrollState = rememberScrollableState { delta ->
        val maxValue = ((state.endTime - state.startTime) / state.millisPerPixel)
        if (maxValue < 0) return@rememberScrollableState 0f
        val newDelta =
            delta.coerceIn(-state.xOffset, maxValue - state.xOffset)
        state.xOffset += newDelta
        newDelta
    }



    LaunchedEffect(state.selectionTime) {
        horizontalScrollState.animateScrollBy(
            state.selectionOffset,
        )
    }


    CompositionLocalProvider(
        LocalTvGuideState provides state,
        LocalHorizontalScrollState provides horizontalScrollState,
        LocalBringIntoViewSpec provides DefaultBringIntoViewSpec(),
    ) {

        val size = remember { mutableStateOf(IntSize(1920, 1080)) }

        Box(
            modifier = modifier
                .keyEvent(onStartReached, onEndReached)
                .onFocusChanged {
                    if (it.hasFocus && state.selectedEvent == -1) {
                        state.selectedEvent = 0
                    }
                }
                .onSizeChanged {
                    size.value = it
                }
                .fillMaxSize()
                .scrollable(
                    horizontalScrollState,
                    Orientation.Horizontal,
                    reverseDirection = true,
                )
//            .draggable(orientation = Orientation.Horizontal, state = draggableState)
                .focusable()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                val guideScopeImpl = remember(size.value) {
                    TvGuideScope.Impl(size.value)
                }

                guideScopeImpl.content()
            }

            Now(modifier = Modifier) {
                if (state.channelCount > 0)
                    nowIndicator()
            }

        }
    }

}

val LocalTvGuideState = compositionLocalOf<TvGuideState> { error("No GuideState Provided") }

@Composable
fun TvGuideScope.Header(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable HeaderScope.() -> Unit
) {
    val state = LocalTvGuideState.current
    val heightPx = height.toPx()

    LaunchedEffect(heightPx) {
        state.timeBarHeight = heightPx
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds()
            .then(modifier)
    ) {
        val headerScopeImpl = remember { HeaderScope.Impl }
        content(headerScopeImpl)
    }
}

@Composable
fun HeaderScope.Timebar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(time: Long) -> Unit
) {
    val state = LocalTvGuideState.current
    val timeCellWidth by remember { derivedStateOf { state.timeCellWidth } }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .clipToBounds()
            .then(modifier)
    ) {
        for (time in state.scrollTime.toLong()..state.scrollTime.toLong() + state.hoursInViewport.inWholeMilliseconds step state.timeSpacing.inWholeMilliseconds) {
            val roundTime = time - time % state.timeSpacing.inWholeMilliseconds
            val xOffset =
                ((roundTime - state.scrollTime) / state.millisPerPixel).toInt() - 40 // Find a better way to align time labels in center
            Box(
                modifier = Modifier
                    .width(timeCellWidth.pxToDp())
                    .absoluteOffset { IntOffset(x = xOffset, y = 0) }
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                content(roundTime)
            }
        }
    }
}

@Composable
fun HeaderScope.CurrentDay(
    width: Dp,
    modifier: Modifier,
    content: @Composable (BoxScope.(time: Long) -> Unit)
) {
    val state = LocalTvGuideState.current
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
fun <T : Any> TvGuideScope.Channels(
    width: Dp,
    channels: SnapshotStateList<T>,
    key: (T?) -> Any,
    modifier: Modifier,
    content: @Composable ChannelRowScope.(channel: T?, isSelected: Boolean) -> Unit
) = Channels(
    width = width,
    itemsCount = channels.size,
    key = { key(channels[it]) },
    modifier = modifier
) { isSelected ->
    val channel = channels[position]
    content(channel, isSelected)
}

@Composable
fun TvGuideScope.Channels(
    width: Dp,
    itemsCount: Int,
    key: (index: Int) -> Any = { it },
    modifier: Modifier = Modifier,
    focusedChannelRow: Int = 4,
    content: @Composable ChannelRowScope.(isSelected: Boolean) -> Unit
) {
    val state = LocalTvGuideState.current
    val horizontalState = LocalHorizontalScrollState.current


    val widthPx = width.toPx()
    val programAreaWidth = remember(size, width) { (size.width - widthPx) }
    val height = remember(size) { size.height }

    val channelState = rememberLazyListState(
        state.selectedChannel
    )

    val selectedChannelOffset by remember {
        derivedStateOf {
            val layoutInfo = channelState.layoutInfo
            val contentPadding = layoutInfo.beforeContentPadding
            val itemSpacing = layoutInfo.mainAxisItemSpacing
            val itemHeight = layoutInfo.visibleItemsInfo.getOrNull(1)?.size ?: 0
            -(contentPadding + (itemHeight + itemSpacing) * (focusedChannelRow - 1))
        }
    }

    LaunchedEffect(programAreaWidth) {
        state.programAreaWidth = programAreaWidth
        horizontalState.scrollBy(state.selectionOffset)
    }

    LaunchedEffect(itemsCount) {
        state.channelCount = itemsCount
    }

    LaunchedEffect(widthPx) {
        state.channelAreaWidth = widthPx
    }

    LaunchedEffect(state.selectedChannel) {
        val difference = abs(channelState.firstVisibleItemIndex - state.selectedChannel)
        if (difference < 5)
            channelState.animateScrollToItem(state.selectedChannel, selectedChannelOffset)
        else
            channelState.requestScrollToItem(state.selectedChannel, selectedChannelOffset)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        state = channelState
    ) {

        items(
            count = itemsCount,
            key = key,
        ) { pos ->
            val isSelected by remember {
                derivedStateOf {
                    state.selectedChannel == pos
                }
            }

            val channelScopeImpl = remember(pos) { ChannelRowScope.Impl(pos) }

            channelScopeImpl.content(isSelected)

        }
    }

}

@Composable
private fun Now(modifier: Modifier, content: @Composable (BoxScope.(time: Long) -> Unit)) {
    val state = LocalTvGuideState.current

    var currentTime by remember {
        mutableLongStateOf(now)
    }

    val offset by remember {
        derivedStateOf {
            val nowOffset = (currentTime - state.scrollTime) / state.millisPerPixel
            state.channelAreaWidth + nowOffset
        }
    }

    val animateOffset by animateIntOffsetAsState(IntOffset(x = offset.toInt(), y = 0), label = "")

    val shouldDisplay by remember {
        derivedStateOf {
            currentTime.toFloat() in state.scrollTime..state.maxScrollTime
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            currentTime = now
        }
    }

    if (shouldDisplay) {
        Box(
            modifier = Modifier
                .offset {
                    animateOffset
                }
                .then(modifier)) {
            content(currentTime)
        }
    }
}


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
        val channelScopeImpl = remember(position) { ChannelScope.Impl(position) }
        channelScopeImpl.content(position)
    }
}


/*class EventScopeImpl(
    channel: Int,
    event: EventWithIndex,
) : EventScope(channel, event) {

    @Composable
    override fun Modifier.progressBackground(
        color: Color,
        shape: Shape
    ): Modifier {
        var currentTime by remember {
            mutableLongStateOf(now)
        }

        LaunchedEffect(Unit) {
            while (true) {
                delay(30.seconds)
                currentTime = now
            }
        }

        val state = LocalTvGuideState.current
        return this then Modifier
            .drawBehind {
                val endVisibleTime = minOf(event.event.end, currentTime)
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

}*/

@Composable
fun EventScope.EventCell(
    modifier: Modifier = Modifier,
    requestFocus: Boolean = false,
    onClick: () -> Unit,
    onSelected: (event: Event) -> Unit,
    content: @Composable (BoxScope.() -> Unit)
) {
    val horizontalScrollState = LocalHorizontalScrollState.current
    val scope = rememberCoroutineScope()
    val state = LocalTvGuideState.current
    val endVisibleTime = remember(state.maxScrollTime) {
        minOf(event.event.end.toFloat(), state.maxScrollTime)
    }
    val startVisibleTime = remember(state.scrollTime) {
        maxOf(event.event.start.toFloat(), state.scrollTime)
    }

    val width = (endVisibleTime - startVisibleTime) / state.millisPerPixel

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.selectedChannel, state.selectionTime) {
        if (state.selectedChannel == position
            && state.selectionTime in event.event.start.toFloat()..event.event.end.toFloat()
        ) {
            state.selectedEvent = event.index
            onSelected(event.event)
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
                        onClick()
                        true
                    }

                    else -> false
                }
            }
            .pointerInput(position, event.event.id) {
                detectTapGestures {
                    scope.launch {
                        horizontalScrollState.animateScrollBy((state.scrollTime - startVisibleTime) / state.millisPerPixel)
                    }
                    state.selectedChannel = position
                    state.selectionTime = event.event.start.toFloat()
                    onClick()
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

    val state = LocalTvGuideState.current

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(state.channelAreaWidth.pxToDp())
            .pointerInput(position) {
                detectTapGestures(onPress = {
                    state.selectedChannel = position
                    onClick()
                })
            }
            .then(modifier)
    ) {
        content()
    }
}

@Composable
fun Modifier.keyEvent(onStartReached: () -> Unit = {}, onEndReached: () -> Unit = {}): Modifier {
    val state = LocalTvGuideState.current

    var shouldSkipNow by remember {
        mutableStateOf(false)
    }

    var shouldTrySkipping by remember {
        mutableStateOf(false)
    }

    var shouldLoop by remember {
        mutableStateOf(false)
    }

    return this then Modifier.onKeyEvent {
        if (it.type == KeyEventType.KeyUp) {
            val newSelectionTime =
                state.selectionTime - state.timeIncrement.inWholeMilliseconds
            if (it.key == Key.DirectionLeft && shouldTrySkipping && newSelectionTime < now) {
                onStartReached()
                return@onKeyEvent true
            } else if (it.key == Key.DirectionDown && shouldLoop && state.selectedChannel == state.channelCount - 1) {
                state.selectedChannel = 0
            } else if (it.key == Key.DirectionUp && shouldLoop && state.selectedChannel == 0) {
                state.selectedChannel = state.channelCount - 1
            } else if (it.key == Key.Back || it.key == Key.Escape) {
                if (now.toFloat() in state.scrollTime..state.maxScrollTime) {
                    onStartReached()
                    return@onKeyEvent true
                } else {
                    state.gotoNow()
                    return@onKeyEvent true
                }
            }
            shouldTrySkipping = true
            shouldLoop = true
            return@onKeyEvent false
        }
        when (it.key) {

            Key.Back, Key.Escape -> {
                true
            }

            Key.DirectionLeft -> {
                val newSelectionTime =
                    state.selectionTime - state.timeIncrement.inWholeMilliseconds
                val minSelectionTime =
                    if (state.stopAtNow && !shouldSkipNow) state.roundedNow else state.roundedStartTime
                val maxSelectionTime = state.roundedEndTime
                state.selectionTime = newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)

                true
            }

            Key.DirectionRight -> {

                val newSelectionTime =
                    state.selectionTime + state.timeIncrement.inWholeMilliseconds
                val minSelectionTime = state.roundedStartTime
                val maxSelectionTime = state.roundedEndTime

                state.selectionTime = newSelectionTime.coerceIn(minSelectionTime, maxSelectionTime)
                if (newSelectionTime > state.roundedNow) {
                    shouldSkipNow = false
                }
                true
            }

            Key.DirectionUp -> {
                state.selectedChannel = (state.selectedChannel - 1)
                    .coerceAtLeast(
                        0
                    )
                true
            }

            Key.DirectionDown -> {
                state.selectedChannel = (state.selectedChannel + 1)
                    .coerceAtMost(
                        state.channelCount - 1
                    )
                true
            }

            else -> false
        }
    }
}


@Composable
fun ChannelScope.Events(
    modifier: Modifier,
    events: List<Event>,
    content: @Composable EventScope.(event: Event, isSelected: Boolean) -> Unit
) {
    val state = LocalTvGuideState.current
    var previousXOffset = remember {
        state.xOffset
    }

    val allEvents = remember {
        events
    }

    var visibleEvents by remember {
        mutableStateOf(listOf<EventWithIndex>())
    }

    var visibleRange by remember {
        mutableStateOf(allEvents.indices)
    }

    LaunchedEffect(state.xOffset, allEvents) {
        withContext(Dispatchers.IO) {
            // Get it working. Some sort of issue when scrolling left, events vanish

//            val (start, end) = if (previousXOffset.toInt() == 0) {
//                0 to allEvents.lastIndex.coerceAtLeast(0)
//            } else if (previousXOffset < state.xOffset) {
//                visibleRange.first to allEvents.lastIndex.coerceAtLeast(0)
//            } else {
//                0 to visibleRange.last.coerceAtLeast(0)
//            }
            previousXOffset = state.xOffset
            visibleEvents =
                allEvents.findVisibleEvents(
                    state.scrollTime - 30.minutes.inWholeMilliseconds,
                    state.maxScrollTime + 30.minutes.inWholeMilliseconds,
//                    start,
//                    end
                )
            visibleRange = (visibleEvents.firstOrNull()?.index ?: 0)..allEvents.lastIndex
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        if (visibleEvents.isNotEmpty()) {
            for (event in visibleEvents) {
                key(event.event.id) {
                    val isSelected by remember {
                        derivedStateOf {
                            state.selectedChannel == position && state.selectedEvent == event.index
                        }
                    }

                    val eventScopeImpl =
                        remember(position, event.event.id) { EventScope.Impl(position, event) }
                    eventScopeImpl.content(
                        event.event,
                        isSelected
                    )
                }
            }
        } else {
            val event = EventWithIndex(
                0,
                Event(
                    0,
                    "Loading...",
                    "",
                    state.scrollTime.toLong(),
                    state.maxScrollTime.toLong()
                )
            )
            val isSelected by remember {
                derivedStateOf {
                    state.selectedChannel == position && state.selectedEvent == event.index
                }
            }
            val eventScopeImpl =
                remember(position, event.event.id) { EventScope.Impl(position, event) }
            content(
                eventScopeImpl,
                event.event,
                isSelected
            )
        }
    }

}
