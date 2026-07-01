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

    override val channels: StateFlow<List<MixerChannelState>> = channelState.asStateFlow()

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
