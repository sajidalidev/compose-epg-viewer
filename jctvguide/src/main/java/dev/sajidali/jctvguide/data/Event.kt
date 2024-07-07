package dev.sajidali.jctvguide.data

import dev.sajidali.jctvguide.utils.formatToPattern
import dev.sajidali.jctvguide.utils.now


data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val start: Long,
    val end: Long
) {

    val isCurrent: Boolean
        get() = now in start..end

    val isInPast: Boolean
        get() = now > end

    val isInFuture: Boolean
        get() = now < start

    val duration: Long
        get() = end - start

    override fun toString(): String {
        return "Event(id=$id, title='$title', description='$description', start=${start.formatToPattern("dd/MM/yyyy HH:mm")}, end=${end.formatToPattern("dd/MM/yyyy HH:mm")})"
    }

}
