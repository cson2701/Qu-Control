package com.scrapps.qucontroller

import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.JvmInline

@JvmInline
value class FaderLevel(val normalized: Float) {
    init {
        require(normalized in 0f..1f) { "Fader level must be between 0.0 and 1.0." }
    }

    fun percentage(): Int = (normalized * 100).toInt()

    companion object {
        fun fromNormalized(value: Float): FaderLevel = FaderLevel(value.coerceIn(0f, 1f))
    }
}

enum class MixerChannelId(val displayName: String) {
    MainLr("Main LR"),
}

data class MixerChannelState(
    val id: MixerChannelId,
    val level: FaderLevel,
)

interface MixerController {
    val channels: StateFlow<List<MixerChannelState>>

    fun setLevel(channelId: MixerChannelId, level: FaderLevel)
}
