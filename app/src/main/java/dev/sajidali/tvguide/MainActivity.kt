package dev.sajidali.tvguide

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sajidali.demo.databinding.ActivityMainBinding
import dev.sajidali.jctvguide.TvGuide
import dev.sajidali.jctvguide.data.Channel
import dev.sajidali.jctvguide.data.DataProvider
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.utils.formatToPattern
import dev.sajidali.jctvguide.utils.now
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val startTime = now - TimeUnit.DAYS.toMillis(3)
    private val stopTime = now + TimeUnit.DAYS.toMillis(3)

    private val provider = object : DataProvider {

        val channelCount = 1000

        val events = HashMap<Int, List<Event>>()
        val channels = (0..channelCount).map { position ->
            Channel(position, "Channel $position", "").also {
                generateEvents(position)
            }
        }

        private fun generateEvents(channel: Int) {
            var startTime = this@MainActivity.startTime
            val stopTime = this@MainActivity.stopTime
            var i = 1
            val events = mutableListOf<Event>()
            while (startTime < stopTime) {
                val endTime =
                    if (startTime.plus(30.minutes.inWholeMilliseconds) >= stopTime) stopTime else startTime.plus(
                        Random.nextInt(30, 120).minutes.inWholeMilliseconds
                    )
                events.add(Event(
                    i,
                    "Event $i",
                    "Description of event $i",
                    startTime,
                    endTime
                ).also {
                    startTime = endTime
                })
                i++
            }
            this.events[channel] = events
        }

        override fun onDataUpdated(block: () -> Unit) {

        }

        override fun channelAt(position: Int): Channel {
            return channels[position]
        }

        override fun eventsOfChannel(position: Int): List<Event> {
            return events[position] ?: emptyList()
        }

        override fun eventOfChannelAt(channel: Int, position: Int): Event? {
            return events[channel]?.getOrNull(position)
        }

        override fun size(): Int {
            return channelCount
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.guideView.setContent {
            TvGuide(
                startTime = startTime,
                endTime = stopTime,
                hoursInViewport = 2.hours,
                fastScroll = true,
                timeSpacing = 30.minutes,
                scrollToNow = true,
                onEventSelected = { channel, event ->
                    binding.txtChannel.text = provider.channelAt(channel).title
                    binding.txtEvent.text = provider.eventOfChannelAt(channel, event)?.title
                },
                onEventClicked = { channel, event ->
                    Toast.makeText(
                        this,
                        "Event $event of channel $channel clicked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {

                CurrentDay(
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
                    height = 20.dp, modifier = Modifier
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

                Channels(
                    width = 250.dp,
                    height = 30.dp,
                    itemCount = provider.channelCount,
                    selectionScale = 2f,
                    modifier = Modifier
                ) { channel: Int, isSelected ->
                    ChannelCell(
                        modifier = Modifier
                            .padding(
                                horizontal = 4.dp,
                                vertical = 1.dp
                            )
                            .background(
                                color = if (isSelected) Color.Red else Color.Gray
                            )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = provider.channelAt(channel).title,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Events(
                        modifier = Modifier.padding(start = 4.dp),
                        events = provider.eventsOfChannel(channel)
                    ) { event: Event, isEventSelected ->
                        EventCell(
                            modifier = Modifier
                                .padding(1.dp)
                                .background(
                                    if (isEventSelected) Color.Red else Color.Gray
                                )
                                .padding(start = 8.dp)
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

                Now(modifier = Modifier) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 3f
                        )
                    }
                }

            }
        }

        binding.guideView.requestFocus()

    }


}