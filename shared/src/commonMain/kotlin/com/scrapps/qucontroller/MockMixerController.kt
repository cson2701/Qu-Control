package com.scrapps.qucontroller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockMixerController : MixerController {
    private val channelState = MutableStateFlow(
        listOf(
            MixerChannelState(
                id = MixerChannelId.MainLr,
                level = FaderLevel.fromNormalized(0.72f),
            ),
        ),
    )
    private val connection = MutableStateFlow(
        MixerConnectionState(
            phase = MixerConnectionPhase.Disconnected,
            message = "Mock controller disconnected",
        ),
    )

    override val channels: StateFlow<List<MixerChannelState>> = channelState.asStateFlow()
    override val connectionState: StateFlow<MixerConnectionState> = connection.asStateFlow()

    override suspend fun connect(endpoint: MixerEndpoint) {
        connection.value = MixerConnectionState(
            phase = MixerConnectionPhase.Connected,
            message = "Mock controller connected",
            endpoint = endpoint,
        )
    }

    override fun disconnect() {
        connection.value = MixerConnectionState(
            phase = MixerConnectionPhase.Disconnected,
            message = "Mock controller disconnected",
        )
    }

    override fun setLevel(channelId: MixerChannelId, level: FaderLevel) {
        channelState.update { channels ->
            channels.map { channel ->
                if (channel.id == channelId) {
                    channel.copy(level = level)
                } else {
                    channel
                }
            }
        }
    }
}
