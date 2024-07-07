package dev.sajidali.jctvguide.data

interface DataProvider {

    fun onDataUpdated(block: () -> Unit)
    fun channelAt(position: Int): Channel?

    fun eventsOfChannel(position: Int): Collection<Event>

    fun eventOfChannelAt(channel: Int, position: Int): Event?

    fun size(): Int

}