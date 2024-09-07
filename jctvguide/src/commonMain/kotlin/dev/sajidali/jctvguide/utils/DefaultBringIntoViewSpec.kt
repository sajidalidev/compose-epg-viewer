package dev.sajidali.jctvguide.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec

@OptIn(ExperimentalFoundationApi::class)
internal class DefaultBringIntoViewSpec(
    private val minOffset: Float = 0f,
) :
    BringIntoViewSpec {

    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        return 0f
    }

}