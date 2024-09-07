package dev.sajidali.jctvguide.utils

import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.data.EventWithIndex
import kotlinx.datetime.Clock
import kotlin.time.Duration

fun Long.roundToNearest(timeSpacing: Duration): Long {
    return this - (this % timeSpacing.inWholeMilliseconds)
}

fun List<Event>.findVisibleEvents(viewportStart: Float, viewportEnd: Float): List<EventWithIndex> {

    val firstVisibleIndex = binarySearch {
        when {
            it.end < viewportStart -> -1
            it.start > viewportEnd || it.start > viewportStart -> 1
            else -> 0
        }
    }


    val startIndex = if (firstVisibleIndex < 0) -firstVisibleIndex - 1 else firstVisibleIndex

    val visibleEvents = mutableListOf<EventWithIndex>()

    for (i in startIndex until size) {
        val event = get(i)
        if (event.start > viewportEnd) {
            break
        }
        if (event.end >= viewportStart) {
            visibleEvents.add(EventWithIndex(i, event))
        }
    }

    return visibleEvents

}

val now: Long
    get() = Clock.System.now().toEpochMilliseconds()
