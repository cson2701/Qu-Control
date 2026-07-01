package com.scrapps.qucontroller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val DefaultMixerEndpoint = MixerEndpoint(host = "192.168.4.198")

@Composable
@Preview
fun App() {
    val mixerController = remember { createMixerController() }
    val coroutineScope = rememberCoroutineScope()
    val channels by mixerController.channels.collectAsState()
    val connectionState by mixerController.connectionState.collectAsState()
    val mainLrChannel = channels.first { it.id == MixerChannelId.MainLr }
    var host by remember { mutableStateOf(DefaultMixerEndpoint.host) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .safeContentPadding()
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 360.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = "Qu Controller",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Baseline control surface for Main LR",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ConnectionStatusPill(connectionState = connectionState)
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Qu mixer IP") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Port ${DefaultMixerEndpoint.port}")
                        },
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            if (connectionState.phase == MixerConnectionPhase.Connected ||
                                connectionState.phase == MixerConnectionPhase.Connecting
                            ) {
                                mixerController.disconnect()
                            } else {
                                coroutineScope.launch {
                                    mixerController.connect(DefaultMixerEndpoint.copy(host = host))
                                }
                            }
                        },
                    ) {
                        Text(
                            if (connectionState.phase == MixerConnectionPhase.Connected ||
                                connectionState.phase == MixerConnectionPhase.Connecting
                            ) {
                                "Disconnect"
                            } else {
                                "Connect"
                            },
                        )
                    }
                    Text(
                        text = connectionState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    VerticalFader(
                        channel = mainLrChannel,
                        onLevelChange = { mixerController.setLevel(mainLrChannel.id, it) },
                        modifier = Modifier.heightIn(min = 420.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusPill(connectionState: MixerConnectionState) {
    val (label, dotColor, pillColor) = when (connectionState.phase) {
        MixerConnectionPhase.Connected -> Triple(
            "Connected",
            Color(0xFF1B8A3E),
            Color(0xFFE6F6EA),
        )
        MixerConnectionPhase.Connecting -> Triple(
            "Connecting",
            Color(0xFFC27A00),
            Color(0xFFFFF1D6),
        )
        MixerConnectionPhase.Error -> Triple(
            "Error",
            Color(0xFFC62828),
            Color(0xFFFDE1E1),
        )
        MixerConnectionPhase.Disconnected -> Triple(
            "Disconnected",
            Color(0xFF667085),
            Color(0xFFECEFF3),
        )
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(pillColor)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(dotColor),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
