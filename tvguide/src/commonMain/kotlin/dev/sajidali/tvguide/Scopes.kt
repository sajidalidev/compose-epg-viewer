package dev.sajidali.tvguide

import androidx.compose.ui.unit.IntSize
import dev.sajidali.tvguide.data.EventWithIndex

@DslMarker
annotation class TvGuideDsl
sealed class TvGuideScope {
    abstract val size: IntSize

    internal data class Impl(override val size: IntSize) : TvGuideScope()
}

@TvGuideDsl
sealed class HeaderScope {
    internal object Impl : HeaderScope()
}

@TvGuideDsl
sealed class ChannelScope {

    abstract val position: Int

    internal class Impl(override val position: Int) : ChannelScope()
}

@TvGuideDsl
sealed class ChannelRowScope {
    abstract val position: Int

    internal data class Impl(override val position: Int) : ChannelRowScope()
}

@TvGuideDsl
sealed class EventScope {
    abstract val position: Int
    abstract val event: EventWithIndex

    internal class Impl(override val position: Int, override val event: EventWithIndex) : EventScope()
}