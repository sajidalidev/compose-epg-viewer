package dev.sajidali.tvguide

import dev.sajidali.tvguide.data.Event
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.byUnicodePattern
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

fun generateEvents(channel: Int, start: Long, stop: Long): List<Event> {
    var startTime = start
    var i = 1
    return buildList {
        while (startTime < stop) {
            val endTime =
                if (startTime.plus(30.minutes.inWholeMilliseconds) >= stop) stop else startTime.plus(
                    Random.nextInt(30, 120).minutes.inWholeMilliseconds
                )
            add(
                Event(
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
    }
}

@OptIn(ExperimentalTime::class)
val now
    get() = kotlin.time.Clock.System.now().toEpochMilliseconds()

@OptIn(ExperimentalTime::class)
fun Long.formatToPattern(pattern: String): String {
    return Instant.fromEpochMilliseconds(this)
        .format(DateTimeComponents.Format {
            byUnicodePattern(pattern)
        })
}