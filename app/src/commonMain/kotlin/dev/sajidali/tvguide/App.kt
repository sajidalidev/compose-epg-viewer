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
import dev.sajidali.tvguide.data.Channel
import dev.sajidali.tvguide.data.Event
import dev.sajidali.tvguide.utils.rememberGuideState
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

            Header(height = 20.dp, modifier = Modifier.background(Color.LightGray)) {

                CurrentDay(
                    width = 250.dp,
                    modifier = Modifier
                        .padding(vertical = 2.dp, horizontal = 4.dp)
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
                ) { time ->
                    Text(
                        text = time.formatToPattern("HH:mm"),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }

            Channels(
                width = 250.dp,
                channels = events,
                key = { it?.id ?: 0 },
                modifier = Modifier
            ) { channel: Channel?, isSelected ->

                val channelEvents = remember(position) {
                    events[position].events
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
                                    position,
                                    events[position].events.indexOf(event)
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