package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DeepRoyalPurple
import com.example.ui.theme.ElectricAmethyst
import com.example.ui.theme.ListeningRose
import com.example.ui.theme.RadiantOrchid
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextPrimary

@Composable
fun GlowingMicButton(
    isListening: Boolean,
    rmsLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val reactiveScale by animateFloatAsState(
        targetValue = if (isListening) 1.06f + (rmsLevel * 0.14f) else 1.0f,
        animationSpec = spring(),
        label = "mic_reactive_scale"
    )

    val coreColor by animateColorAsState(
        targetValue = if (isListening) ListeningRose else ElectricAmethyst,
        label = "mic_core_color"
    )
    val outerColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFFBE123C) else DeepRoyalPurple,
        label = "mic_outer_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(76.dp)
            .minimumInteractiveComponentSize()
    ) {
        // Ambient radial glow + expanding wave ring when microphone is active
        Canvas(modifier = Modifier.size(76.dp)) {
            val baseRadius = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = if (isListening) 0.55f else 0.35f),
                        Color.Transparent
                    ),
                    radius = baseRadius * (if (isListening) 1.25f else 1.0f)
                ),
                radius = baseRadius * 1.25f
            )

            if (isListening) {
                drawCircle(
                    color = ListeningRose.copy(alpha = pulseAlpha),
                    radius = (baseRadius * 0.72f) * pulseScale
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .scale(reactiveScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(coreColor, outerColor)
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SoftLavender.copy(alpha = 0.9f),
                            RadiantOrchid.copy(alpha = 0.35f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = SoftLavender),
                    onClick = onClick
                )
                .testTag(if (isListening) "stop_mic_button" else "mic_button")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = stringResource(
                    if (isListening) R.string.cd_stop_mic_button else R.string.cd_mic_button
                ),
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
