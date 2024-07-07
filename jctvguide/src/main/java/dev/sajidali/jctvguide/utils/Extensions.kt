package dev.sajidali.jctvguide.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.res.getDrawableOrThrow
import dev.sajidali.jctvguide.data.DataProvider
import dev.sajidali.jctvguide.data.Event
import dev.sajidali.jctvguide.data.EventWithIndex
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

fun Context.getThemedAttribute(id: Int): Int {
    val tv = TypedValue()
    theme.resolveAttribute(id, tv, true)
    return tv.data
}

fun Long.formatToPattern(pattern: String): String {

    return DateTimeFormat.forPattern(pattern).print(this)
}

fun Long.formatToPattern(pattern: String, timezone: String): String {

    return DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(timezone)).print(this)
}

fun TypedArray.getColorOrDrawable(index: Int, default: Int = -1): Drawable? {
    return try {
        getDrawableOrThrow(index)
    } catch (e: Throwable) {
        ColorDrawable(getColor(index, default))

    }
}

fun List<Event>.findVisibleEvents(viewportStart: Long, viewportEnd: Long): List<EventWithIndex> {

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

fun Long.toDayName(): String {
    val date = LocalDate(this)
    return date.dayOfWeek().asShortText + " " + date.dayOfMonth + "/" + date.monthOfYear
}

val <T> Collection<T>?.itemCount
    get() = this?.size ?: 0

val DataProvider?.itemCount
    get() = this?.size() ?: 0

val now: Long
    get() = System.currentTimeMillis()

fun DataProvider.getEventAtTime(channel: Int, time: Long): Int {
    return eventsOfChannel(channel).indexOfFirst {
        time in it.start..it.end
    }
}