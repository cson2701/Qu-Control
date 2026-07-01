package com.scrapps.qucontroller

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val QU_TCP_PORT = 51325
private const val MAIN_LR_CHANNEL = 0x67
private const val FADER_PARAMETER_ID = 0x17
private const val NRPN_TRACK_INDEX = 0x07

class QuTcpMidiController : MixerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
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
            message = "Disconnected",
        ),
    )

    private var socket: Socket? = null
    private var output: BufferedOutputStream? = null
    private var input: BufferedInputStream? = null
    private var readJob: Job? = null
    private var activeSenseJob: Job? = null
    private var midiChannel: Int? = null
    private val nrpnState = NrpnState()

    override val channels: StateFlow<List<MixerChannelState>> = channelState.asStateFlow()
    override val connectionState: StateFlow<MixerConnectionState> = connection.asStateFlow()

    override suspend fun connect(endpoint: MixerEndpoint) {
        connection.value = MixerConnectionState(
            phase = MixerConnectionPhase.Connecting,
            message = "Connecting to ${endpoint.host}:${endpoint.port}",
            endpoint = endpoint,
        )

        runCatching {
            val newSocket = Socket()
            newSocket.connect(InetSocketAddress(endpoint.host, endpoint.port), 3_000)
            newSocket.tcpNoDelay = true

            connectionMutex.withLock {
                disconnectInternal()
                socket = newSocket
                input = BufferedInputStream(newSocket.getInputStream())
                output = BufferedOutputStream(newSocket.getOutputStream())
            }

            startActiveSensing()
            startReader(endpoint)
            sendSystemStateRequest()
        }.onFailure { error ->
            connectionMutex.withLock {
                disconnectInternal()
            }
            connection.value = MixerConnectionState(
                phase = MixerConnectionPhase.Error,
                message = "Connection failed: ${error.message ?: "Unknown error"}",
                endpoint = endpoint,
            )
        }
    }

    override fun disconnect() {
        scope.launch {
            connectionMutex.withLock {
                disconnectInternal()
                connection.value = MixerConnectionState(
                    phase = MixerConnectionPhase.Disconnected,
                    message = "Disconnected",
                )
            }
        }
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

        if (channelId != MixerChannelId.MainLr) {
            return
        }

        val currentMidiChannel = midiChannel ?: return
        scope.launch {
            sendNrpn(
                midiChannel = currentMidiChannel,
                targetChannel = MAIN_LR_CHANNEL,
                parameterId = FADER_PARAMETER_ID,
                value = level.toMidiValue(),
                index = NRPN_TRACK_INDEX,
            )
        }
    }

    private fun startReader(endpoint: MixerEndpoint) {
        readJob?.cancel()
        readJob = scope.launch {
            val localInput = input ?: return@launch
            val sysexBuffer = ByteArrayOutputStream()
            var inSysex = false

            try {
                while (isActive) {
                    val nextByte = localInput.read()
                    if (nextByte < 0) {
                        error("Connection closed by mixer")
                    }

                    when {
                        nextByte == 0xFE -> Unit
                        nextByte == 0xF0 -> {
                            inSysex = true
                            sysexBuffer.reset()
                            sysexBuffer.write(nextByte)
                        }
                        inSysex -> {
                            sysexBuffer.write(nextByte)
                            if (nextByte == 0xF7) {
                                inSysex = false
                                handleSysex(sysexBuffer.toByteArray(), endpoint)
                            }
                        }
                        nextByte in 0xB0..0xBF -> {
                            val data1 = localInput.read()
                            val data2 = localInput.read()
                            if (data1 >= 0 && data2 >= 0) {
                                handleControlChange(nextByte, data1, data2)
                            }
                        }
                    }
                }
            } catch (error: Throwable) {
                connection.value = MixerConnectionState(
                    phase = MixerConnectionPhase.Error,
                    message = "Connection lost: ${error.message ?: "Unknown error"}",
                    endpoint = endpoint,
                )
                disconnectInternal()
            }
        }
    }

    private fun startActiveSensing() {
        activeSenseJob?.cancel()
        activeSenseJob = scope.launch {
            while (isActive) {
                sendBytes(byteArrayOf(0xFE.toByte()))
                delay(1_000)
            }
        }
    }

    private suspend fun sendSystemStateRequest() {
        // All-call header + Get System State + QuPad flag.
        sendBytes(
            byteArrayOf(
                0xF0.toByte(), 0x00, 0x00, 0x1A, 0x50, 0x11, 0x01, 0x00, 0x7F.toByte(),
                0x10, 0x01, 0xF7.toByte(),
            ),
        )
    }

    private suspend fun sendNrpn(
        midiChannel: Int,
        targetChannel: Int,
        parameterId: Int,
        value: Int,
        index: Int,
    ) {
        val status = (0xB0 or midiChannel).toByte()
        sendBytes(
            byteArrayOf(
                status, 0x63, targetChannel.toByte(),
                status, 0x62, parameterId.toByte(),
                status, 0x06, value.toByte(),
                status, 0x26, index.toByte(),
            ),
        )
    }

    private suspend fun sendBytes(bytes: ByteArray) {
        connectionMutex.withLock {
            val localOutput = output ?: return
            localOutput.write(bytes)
            localOutput.flush()
        }
    }

    private fun handleSysex(bytes: ByteArray, endpoint: MixerEndpoint) {
        if (bytes.size < 10) {
            return
        }

        val isQuHeader = bytes[0] == 0xF0.toByte() &&
            bytes[1] == 0x00.toByte() &&
            bytes[2] == 0x00.toByte() &&
            bytes[3] == 0x1A.toByte() &&
            bytes[4] == 0x50.toByte() &&
            bytes[5] == 0x11.toByte()

        if (!isQuHeader) {
            return
        }

        val receivedMidiChannel = bytes[8].toInt() and 0x0F
        val command = bytes[9].toInt() and 0x7F

        if (command == 0x11) {
            midiChannel = receivedMidiChannel
            connection.value = MixerConnectionState(
                phase = MixerConnectionPhase.Connected,
                message = "Connected to ${endpoint.host}:${endpoint.port} on MIDI channel ${receivedMidiChannel + 1}",
                endpoint = endpoint,
            )
        }
    }

    private fun handleControlChange(status: Int, controller: Int, value: Int) {
        val statusChannel = status and 0x0F
        if (midiChannel != null && statusChannel != midiChannel) {
            return
        }

        when (controller) {
            0x63 -> nrpnState.channel = value
            0x62 -> nrpnState.parameterId = value
            0x06 -> nrpnState.dataMsb = value
            0x26 -> {
                val targetChannel = nrpnState.channel
                val parameterId = nrpnState.parameterId
                val dataMsb = nrpnState.dataMsb
                if (targetChannel == MAIN_LR_CHANNEL && parameterId == FADER_PARAMETER_ID && value == NRPN_TRACK_INDEX && dataMsb != null) {
                    val level = FaderLevel.fromNormalized(dataMsb / 127f)
                    channelState.update { channels ->
                        channels.map { channel ->
                            if (channel.id == MixerChannelId.MainLr) {
                                channel.copy(level = level)
                            } else {
                                channel
                            }
                        }
                    }
                }
                nrpnState.clear()
            }
        }
    }

    private fun disconnectInternal() {
        activeSenseJob?.cancel()
        activeSenseJob = null
        readJob?.cancel()
        readJob = null
        output?.close()
        output = null
        input?.close()
        input = null
        socket?.close()
        socket = null
        midiChannel = null
        nrpnState.clear()
    }

    private class NrpnState {
        var channel: Int? = null
        var parameterId: Int? = null
        var dataMsb: Int? = null

        fun clear() {
            channel = null
            parameterId = null
            dataMsb = null
        }
    }
}

private fun FaderLevel.toMidiValue(): Int = (normalized.coerceIn(0f, 1f) * 127f).toInt()

actual fun createMixerController(): MixerController = QuTcpMidiController()
