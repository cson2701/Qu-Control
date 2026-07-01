package com.scrapps.qucontroller

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun VerticalFader(
    channel: MixerChannelState,
    onLevelChange: (FaderLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(132.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = channel.id.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "${channel.level.percentage()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Box(
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val travelHeight = 210.dp
            val thumbSize = 28.dp
            val density = LocalDensity.current
            var travelHeightPx by remember { mutableIntStateOf(0) }

            fun updateLevel(positionY: Float) {
                if (travelHeightPx <= 0) {
                    return
                }
                val normalized = 1f - (positionY / travelHeightPx.toFloat())
                onLevelChange(FaderLevel.fromNormalized(normalized))
            }

            Box(
                modifier = Modifier
                    .height(travelHeight)
                    .width(56.dp)
                    .onSizeChanged { travelHeightPx = it.height }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            updateLevel(offset.y)
                        }
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, _ ->
                            updateLevel(change.position.y)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(travelHeight)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )

                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height((travelHeight * channel.level.normalized.coerceIn(0f, 1f)))
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                val thumbTravelPx = travelHeightPx - with(density) { thumbSize.roundToPx() }
                val thumbOffsetPx = if (thumbTravelPx > 0) {
                    ((1f - channel.level.normalized) * thumbTravelPx).roundToInt()
                } else {
                    0
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(x = 0, y = thumbOffsetPx) }
                        .width(thumbSize)
                        .height(thumbSize)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.onSurface),
                )
            }
        }
    }
}
