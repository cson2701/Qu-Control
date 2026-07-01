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

data class MixerEndpoint(
    val host: String,
    val port: Int = 51325,
)

enum class MixerConnectionPhase {
    Disconnected,
    Connecting,
    Connected,
    Error,
}

data class MixerConnectionState(
    val phase: MixerConnectionPhase,
    val message: String,
    val endpoint: MixerEndpoint? = null,
)

interface MixerController {
    val channels: StateFlow<List<MixerChannelState>>
    val connectionState: StateFlow<MixerConnectionState>

    suspend fun connect(endpoint: MixerEndpoint)
    fun disconnect()

    fun setLevel(channelId: MixerChannelId, level: FaderLevel)
}

expect fun createMixerController(): MixerController
