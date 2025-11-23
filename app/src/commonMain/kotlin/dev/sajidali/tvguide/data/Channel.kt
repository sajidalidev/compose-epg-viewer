package dev.sajidali.tvguide.data

data class Channel(val id: Int, val title: String, val icon: String, var events: List<Event> = emptyList())