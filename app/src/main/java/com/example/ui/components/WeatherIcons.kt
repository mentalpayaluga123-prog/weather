package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.WeatherCondition
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.CloudGray
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.RainCyan
import com.example.ui.theme.StormViolet
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherConditionGraphic(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rainDropOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (condition) {
            WeatherCondition.SUNNY -> {
                Canvas(modifier = Modifier.size(size)) {
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val radius = size.toPx() * 0.25f

                    // Pulsing outer solar aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SunGold.copy(alpha = 0.45f * pulse), Color.Transparent),
                            center = center,
                            radius = radius * 2.2f * pulse
                        ),
                        center = center,
                        radius = radius * 2.2f * pulse
                    )

                    // Core Sun
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(SunGold, SunOrange),
                            start = Offset(center.x - radius, center.y - radius),
                            end = Offset(center.x + radius, center.y + radius)
                        ),
                        center = center,
                        radius = radius
                    )

                    // Rotating Solar Rays
                    val rayCount = 8
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount) + rotation) * (Math.PI / 180f).toFloat()
                        val innerR = radius * 1.35f
                        val outerR = radius * 1.75f
                        val start = Offset(center.x + cos(angle) * innerR, center.y + sin(angle) * innerR)
                        val end = Offset(center.x + cos(angle) * outerR, center.y + sin(angle) * outerR)
                        drawLine(
                            color = SunGold.copy(alpha = 0.85f),
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            WeatherCondition.CLEAR_NIGHT -> {
                Canvas(modifier = Modifier.size(size)) {
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val radius = size.toPx() * 0.28f

                    // Atmospheric night glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AtmosphericBlue.copy(alpha = 0.35f), Color.Transparent),
                            center = center,
                            radius = radius * 2.0f
                        ),
                        center = center,
                        radius = radius * 2.0f
                    )

                    // Crescent moon
                    drawCircle(
                        color = AtmosphericBlue,
                        center = center,
                        radius = radius
                    )
                    drawCircle(
                        color = Color(0xFF0F1A34),
                        center = Offset(center.x + radius * 0.45f, center.y - radius * 0.25f),
                        radius = radius * 0.9f
                    )
                }
            }

            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.CLOUDY -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = size.toPx()
                    val h = size.toPx()

                    if (condition == WeatherCondition.PARTLY_CLOUDY) {
                        // Peeking sun in the corner
                        drawCircle(
                            brush = Brush.linearGradient(listOf(SunGold, SunOrange)),
                            center = Offset(w * 0.72f, h * 0.32f),
                            radius = w * 0.18f
                        )
                    }

                    // Fluffy stylized cloud
                    val cloudColor = if (condition == WeatherCondition.CLOUDY) CloudGray else Color.White
                    drawCircle(
                        color = cloudColor.copy(alpha = 0.95f),
                        center = Offset(w * 0.42f, h * 0.58f),
                        radius = w * 0.24f
                    )
                    drawCircle(
                        color = cloudColor.copy(alpha = 0.95f),
                        center = Offset(w * 0.62f, h * 0.60f),
                        radius = w * 0.20f
                    )
                    drawCircle(
                        color = cloudColor.copy(alpha = 0.95f),
                        center = Offset(w * 0.28f, h * 0.66f),
                        radius = w * 0.16f
                    )
                    drawRoundRect(
                        color = cloudColor.copy(alpha = 0.95f),
                        topLeft = Offset(w * 0.20f, h * 0.62f),
                        size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.09f, h * 0.09f)
                    )
                }
            }

            WeatherCondition.RAINY, WeatherCondition.HEAVY_RAIN -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = size.toPx()
                    val h = size.toPx()

                    // Cloud body
                    drawCircle(
                        color = CloudGray.copy(alpha = 0.9f),
                        center = Offset(w * 0.45f, h * 0.45f),
                        radius = w * 0.24f
                    )
                    drawCircle(
                        color = CloudGray.copy(alpha = 0.9f),
                        center = Offset(w * 0.65f, h * 0.48f),
                        radius = w * 0.18f
                    )
                    drawRoundRect(
                        color = CloudGray.copy(alpha = 0.9f),
                        topLeft = Offset(w * 0.25f, h * 0.48f),
                        size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.09f, h * 0.09f)
                    )

                    // Rain drops
                    val dropCount = if (condition == WeatherCondition.HEAVY_RAIN) 5 else 3
                    for (i in 0 until dropCount) {
                        val startX = w * (0.30f + i * 0.12f)
                        val startY = h * (0.68f + ((rainDropOffset + i * 0.25f) % 1f) * 0.24f)
                        drawLine(
                            color = RainCyan,
                            start = Offset(startX, startY),
                            end = Offset(startX - w * 0.04f, startY + h * 0.08f),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            WeatherCondition.THUNDERSTORM -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = size.toPx()
                    val h = size.toPx()

                    // Dark storm cloud
                    drawCircle(
                        color = Color(0xFF4A5568),
                        center = Offset(w * 0.45f, h * 0.42f),
                        radius = w * 0.24f
                    )
                    drawCircle(
                        color = Color(0xFF3B4455),
                        center = Offset(w * 0.65f, h * 0.46f),
                        radius = w * 0.20f
                    )
                    drawRoundRect(
                        color = Color(0xFF3B4455),
                        topLeft = Offset(w * 0.24f, h * 0.46f),
                        size = androidx.compose.ui.geometry.Size(w * 0.54f, h * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.09f, h * 0.09f)
                    )

                    // Electric lightning bolt
                    val flashAlpha = if (pulse > 1.0f) 1.0f else 0.4f
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.52f, h * 0.58f)
                        lineTo(w * 0.44f, h * 0.74f)
                        lineTo(w * 0.54f, h * 0.74f)
                        lineTo(w * 0.46f, h * 0.94f)
                        lineTo(w * 0.60f, h * 0.70f)
                        lineTo(w * 0.50f, h * 0.70f)
                        close()
                    }
                    drawPath(path, color = SunGold.copy(alpha = flashAlpha))
                }
            }

            WeatherCondition.SNOW -> {
                Canvas(modifier = Modifier.size(size)) {
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val r = size.toPx() * 0.35f
                    // 6-armed ice snowflake
                    for (i in 0 until 6) {
                        val angle = (i * 60f + rotation * 0.4f) * (Math.PI / 180f).toFloat()
                        val armEnd = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
                        drawLine(
                            color = AtmosphericBlue,
                            start = center,
                            end = armEnd,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Sub-branches
                        val midPoint = Offset(center.x + cos(angle) * r * 0.6f, center.y + sin(angle) * r * 0.6f)
                        val bAngle1 = angle + 0.55f
                        val bAngle2 = angle - 0.55f
                        drawLine(
                            color = AtmosphericBlue,
                            start = midPoint,
                            end = Offset(midPoint.x + cos(bAngle1) * r * 0.25f, midPoint.y + sin(bAngle1) * r * 0.25f),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = AtmosphericBlue,
                            start = midPoint,
                            end = Offset(midPoint.x + cos(bAngle2) * r * 0.25f, midPoint.y + sin(bAngle2) * r * 0.25f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            WeatherCondition.WINDY, WeatherCondition.MIST, WeatherCondition.FOG -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = size.toPx()
                    val h = size.toPx()
                    val color = if (condition == WeatherCondition.WINDY) ElectricCyan else CloudGray

                    // Flowing stream ribbons
                    val lines = listOf(0.35f, 0.50f, 0.65f)
                    lines.forEachIndexed { index, yFrac ->
                        val startX = w * (0.2f + (index * 0.08f))
                        val endX = w * (0.85f - (index * 0.04f))
                        val y = h * yFrac
                        drawLine(
                            color = color.copy(alpha = 0.8f),
                            start = Offset(startX, y),
                            end = Offset(endX, y),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherConditionSmallIcon(
    condition: WeatherCondition,
    tint: Color = ElectricCyan,
    modifier: Modifier = Modifier
) {
    val icon = when (condition) {
        WeatherCondition.SUNNY -> Icons.Filled.WbSunny
        WeatherCondition.CLEAR_NIGHT -> Icons.Filled.NightsStay
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.CLOUDY -> Icons.Filled.Cloud
        WeatherCondition.RAINY, WeatherCondition.HEAVY_RAIN -> Icons.Filled.Grain
        WeatherCondition.THUNDERSTORM -> Icons.Filled.Bolt
        WeatherCondition.SNOW -> Icons.Filled.Grain
        WeatherCondition.WINDY, WeatherCondition.MIST, WeatherCondition.FOG -> Icons.Filled.Air
    }
    Icon(
        imageVector = icon,
        contentDescription = condition.displayName,
        tint = tint,
        modifier = modifier
    )
}
