package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricAmethyst
import com.example.ui.theme.ListeningRose
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RadiantOrchid
import com.example.ui.theme.SoftLavender
import com.example.viewmodel.AssistantMood

@Composable
fun SiyaAvatar(
    mood: AssistantMood,
    rmsLevel: Float = 0f,
    size: Dp = 148.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "siya_avatar_orbit")

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mood) {
                    AssistantMood.LISTENING -> 3200
                    AssistantMood.THINKING -> 2000
                    AssistantMood.SPEAKING -> 4000
                    AssistantMood.IDLE -> 9000
                },
                easing = LinearEasing
            )
        ),
        label = "orbit_angle"
    )

    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val smoothRms by animateFloatAsState(
        targetValue = if (mood == AssistantMood.LISTENING) rmsLevel.coerceIn(0.1f, 1f) else 0f,
        animationSpec = spring(),
        label = "rms_smooth"
    )

    val ringPrimaryColor = when (mood) {
        AssistantMood.LISTENING -> ListeningRose
        AssistantMood.THINKING -> CyberCyan
        AssistantMood.SPEAKING -> RadiantOrchid
        AssistantMood.IDLE -> ElectricAmethyst
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("siya_animated_avatar")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val auraRadius = radius * (0.92f + smoothRms * 0.12f) * (if (mood == AssistantMood.IDLE) breathing * 0.96f else breathing)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringPrimaryColor.copy(alpha = 0.42f),
                        ElectricAmethyst.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    radius = auraRadius
                ),
                radius = auraRadius
            )

            // Outer rotating arc
            rotate(degrees = orbitAngle) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            ringPrimaryColor,
                            SoftLavender,
                            Color.Transparent
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 210f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Counter-rotating inner orbital arc
            rotate(degrees = -orbitAngle * 1.35f) {
                val inset = 8.dp.toPx()
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            CyberCyan.copy(alpha = 0.85f),
                            RadiantOrchid.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    startAngle = 45f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        this.size.width - inset * 2,
                        this.size.height - inset * 2
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(CircleShape)
                .background(ObsidianBlack)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(ringPrimaryColor, SoftLavender.copy(alpha = 0.6f))
                    ),
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_siya_avatar),
                contentDescription = stringResource(R.string.cd_siya_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
