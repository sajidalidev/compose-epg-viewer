package dev.sajidali.tvguide

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sajidali.jctvguide.ChannelCell
import dev.sajidali.jctvguide.ChannelRow
import dev.sajidali.jctvguide.Channels
import dev.sajidali.jctvguide.CurrentDay
import dev.sajidali.jctvguide.EventCell
import dev.sajidali.jctvguide.Events
import dev.sajidali.jctvguide.Header
import dev.sajidali.jctvguide.TimeCell
import dev.sajidali.jctvguide.Timebar
import dev.sajidali.jctvguide.TvGuide
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.utils.rememberGuideState
import dev.sajidali.tvguide.data.Channel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun App() {

    val startTime = remember {
        now - 3.days.inWholeMilliseconds
    }

    val stopTime = remember {
        now + 3.days.inWholeMilliseconds
    }

    val events = remember {
        (0..1000).map { position ->
            Channel(position, "Channel $position", "").also {
                it.events = generateEvents(position, startTime, stopTime)
            }
        }.toMutableStateList()
    }

    val guideState = rememberGuideState(
        startTime = startTime,
        endTime = stopTime,
        hoursInViewport = 2.hours,
        timeSpacing = 30.minutes,
        initialOffset = now
    )

    var selected by remember {
        mutableStateOf(Selection(0, 0))
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = events.getOrNull(selected.channel)?.title ?: "",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = events.getOrNull(selected.channel)?.events?.getOrNull(selected.event)?.title
                ?: "",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )


        TvGuide(
            state = guideState,
            nowIndicator = {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color.Red,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3f
                    )
                }
            },
        ) {

            Header(height = 20.dp) {

                CurrentDay(
                    width = 250.dp,
                    modifier = Modifier
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                        .background(color = Color.LightGray)
                ) { time: Long ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = time.formatToPattern("dd-MM"),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Timebar(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                        .background(color = Color.LightGray)
                ) {
                    TimeCell(modifier = Modifier) { time ->
                        Text(
                            text = time.formatToPattern("HH:mm"),
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }
            }

            Channels(
                width = 250.dp,
                channels = events,
                key = { it?.id ?: 0 },
                modifier = Modifier
            ) { channelIndex: Int, channel: Channel?, isSelected ->

                val channelEvents = remember(channelIndex) {
                    events[channelIndex].events
                }

                ChannelRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSelected) 40.dp else 32.dp)
                ) { _ ->
                    ChannelCell(
                        modifier = Modifier
                            .padding(
                                horizontal = 4.dp,
                                vertical = 1.dp
                            )
                            .background(
                                color = if (isSelected) Color.Red else Color.Gray
                            ),
                        onClick = {}
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = channel?.title ?: "",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Events(
                        modifier = Modifier,
                        events = channelEvents
                    ) { event: Event, isEventSelected ->
                        EventCell(
                            modifier = Modifier
                                .padding(1.dp)
                                .background(
                                    if (isEventSelected) Color.Red else Color.Gray
                                )
                                .padding(start = 8.dp),
                            onSelected = {
                                selected = Selection(
                                    channelIndex,
                                    events[channelIndex].events.indexOf(event)
                                )
                            },
                            onClick = {

                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = event.title,
                                    color = Color.White
                                )
                            }
                        }

                    }
                }
            }

        }
    }
}