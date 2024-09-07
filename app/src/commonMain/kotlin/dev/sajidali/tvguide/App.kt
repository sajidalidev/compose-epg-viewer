package dev.sajidali.tvguide

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sajidali.jctvguide.*
import dev.sajidali.jctvguide.data.Channel
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.utils.now
import dev.sajidali.jctvguide.utils.rememberGuideState
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
        }
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
            fastScroll = true,
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
            onEventSelected = { channel, event ->
                selected = Selection(channel, event)
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
                itemsCount = events.size,
                modifier = Modifier
            ) { channel: Int, isSelected ->

                ChannelRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSelected) 40.dp else 32.dp)
                ) { channelPos ->
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
                                text = events.get(channel).title,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Events(
                        modifier = Modifier,
                        events = events[channel].events
                    ) { event: Event, isEventSelected ->
                        EventCell(
                            modifier = Modifier
                                .padding(1.dp)
                                .background(
                                    if (isEventSelected) Color.Red else Color.Gray
                                )
                                .padding(start = 8.dp),
                            onClick = { channelIndex: Int, eventIndex: Int ->

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