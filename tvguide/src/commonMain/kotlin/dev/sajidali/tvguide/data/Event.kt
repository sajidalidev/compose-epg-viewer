package dev.sajidali.tvguide.data

import dev.sajidali.tvguide.utils.now


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

    fun isAroundTime(time: Long): Boolean {
        return time in start..end
    }

}
