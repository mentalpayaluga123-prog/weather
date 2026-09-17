package com.example.ui.globe

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.GlobeMarker
import com.example.model.TempUnit
import com.example.model.WeatherCondition
import com.example.model.WeatherLayerType
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepOceanBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HeatRed
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.RainCyan
import com.example.ui.theme.StormViolet
import com.example.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// 3D Point
data class Vec3(val x: Float, val y: Float, val z: Float)

@Composable
fun Globe3DView(
    markers: List<GlobeMarker>,
    activeLayer: WeatherLayerType,
    tempUnit: TempUnit,
    autoRotate: Boolean = true,
    autoRotateSpeed: Float = 0.35f,
    selectedMarker: GlobeMarker? = null,
    onMarkerClick: (GlobeMarker) -> Unit,
    modifier: Modifier = Modifier
) {
    var yaw by remember { mutableFloatStateOf(0.4f) }
    var pitch by remember { mutableFloatStateOf(0.2f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Dynamic animation loop for atmospheric wind streamlines and radar scans
    val infiniteTransition = rememberInfiniteTransition(label = "globe_dynamics")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Continuous auto-rotation
    LaunchedEffect(autoRotate, autoRotateSpeed) {
        if (autoRotate) {
            while (true) {
                kotlinx.coroutines.delay(16)
                yaw += (autoRotateSpeed * 0.005f)
                if (yaw > (2 * PI)) yaw -= (2 * PI).toFloat()
            }
        }
    }

    // Fixed starfield coordinates
    val stars = remember {
        List(70) {
            val sx = (Math.random().toFloat())
            val sy = (Math.random().toFloat())
            val brightness = (Math.random().toFloat() * 0.7f + 0.3f)
            val size = (Math.random().toFloat() * 2f + 1f)
            Triple(Offset(sx, sy), brightness, size)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.7f, 2.5f)
                    yaw += pan.x * 0.005f
                    pitch = (pitch - pan.y * 0.005f).coerceIn(-1.2f, 1.2f)
                }
            }
            .pointerInput(markers, yaw, pitch, zoomScale) {
                detectTapGestures { tapOffset ->
                    // Check if clicked near any visible marker
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val radius = (minOf(w, h) * 0.38f) * zoomScale
                    val center = Offset(w / 2f, h / 2f)

                    var clickedMarker: GlobeMarker? = null
                    var closestDist = Float.MAX_VALUE

                    for (marker in markers) {
                        val p = projectGeo(marker.location.latitude, marker.location.longitude, radius, yaw, pitch)
                        if (p.z > 0.05f) { // Visible hemisphere
                            val screenX = center.x + p.x
                            val screenY = center.y + p.y
                            val dist = (Offset(screenX, screenY) - tapOffset).getDistance()
                            if (dist < 48.dp.toPx() && dist < closestDist) {
                                closestDist = dist
                                clickedMarker = marker
                            }
                        }
                    }

                    if (clickedMarker != null) {
                        onMarkerClick(clickedMarker)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val radius = (minOf(w, h) * 0.38f) * zoomScale

            // 1. Draw Starfield in space
            stars.forEach { (pos, brightness, starSize) ->
                val starX = pos.x * w
                val starY = pos.y * h
                drawCircle(
                    color = Color.White.copy(alpha = brightness * 0.85f),
                    radius = starSize,
                    center = Offset(starX, starY)
                )
            }

            // 2. Atmospheric Outer Glow (Multi-layer Radial Gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.38f),
                        NeonAzure.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.22f
                ),
                center = center,
                radius = radius * 1.22f
            )

            // 3. Globe Oceanic Base Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF13325B),
                        DeepOceanBlue,
                        Color(0xFF04142D)
                    ),
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                    radius = radius * 1.05f
                ),
                center = center,
                radius = radius
            )

            // 4. Latitude and Longitude Graticule Grid (3D Spherical Lines)
            drawGraticules(center, radius, yaw, pitch)

            // 5. Continents / Landmass Polygons
            drawContinents(center, radius, yaw, pitch)

            // 6. Active Weather Layer Overlay
            when (activeLayer) {
                WeatherLayerType.TEMPERATURE -> drawTemperatureLayer(center, radius, yaw, pitch)
                WeatherLayerType.RAIN -> drawRainRadarLayer(center, radius, yaw, pitch, wavePhase)
                WeatherLayerType.WIND -> drawWindStreamlines(center, radius, yaw, pitch, wavePhase)
                WeatherLayerType.CLOUDS -> drawCloudFormations(center, radius, yaw, pitch, wavePhase)
                WeatherLayerType.STORM -> drawStormCells(center, radius, yaw, pitch, wavePhase)
                WeatherLayerType.SATELLITE -> drawSatelliteTerminator(center, radius, yaw, pitch)
            }

            // 7. Atmospheric Specular Rim & Limb Darkening
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0x66000000),
                        ElectricCyan.copy(alpha = 0.45f)
                    ),
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )

            // Outer sphere outline
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.6f),
                center = center,
                radius = radius,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 8. 3D Location Markers with Depth Culling
            drawLocationMarkers(center, radius, yaw, pitch, markers, selectedMarker, tempUnit)
        }
    }
}

// Convert Geo (Lat/Lon) to 3D Sphere coordinates with Yaw and Pitch
fun projectGeo(lat: Double, lon: Double, radius: Float, yaw: Float, pitch: Float): Vec3 {
    val latRad = (lat * PI / 180.0).toFloat()
    val lonRad = (lon * PI / 180.0).toFloat()

    // Spherical coordinates
    val cosLat = cos(latRad)
    val sinLat = sin(latRad)
    val totalYaw = lonRad + yaw

    // 3D Point before pitch
    val x0 = radius * cosLat * sin(totalYaw)
    val y0 = -radius * sinLat
    val z0 = radius * cosLat * cos(totalYaw)

    // Apply Pitch (rotation around X axis)
    val cosP = cos(pitch)
    val sinP = sin(pitch)

    val x = x0
    val y = y0 * cosP - z0 * sinP
    val z = y0 * sinP + z0 * cosP

    return Vec3(x, y, z)
}

// Draw Latitude & Longitude Graticule Lines
private fun DrawScope.drawGraticules(center: Offset, radius: Float, yaw: Float, pitch: Float) {
    val graticuleColor = ElectricCyan.copy(alpha = 0.16f)
    val stroke = Stroke(width = 1.dp.toPx())

    // Parallels (Latitudes: -60, -30, 0, 30, 60)
    val latitudes = listOf(-60.0, -30.0, 0.0, 30.0, 60.0)
    latitudes.forEach { lat ->
        val points = mutableListOf<Offset>()
        for (step in 0..64) {
            val lon = -180.0 + (step * (360.0 / 64))
            val p = projectGeo(lat, lon, radius, yaw, pitch)
            if (p.z > 0f) {
                points.add(Offset(center.x + p.x, center.y + p.y))
            } else {
                if (points.size > 1) {
                    drawPoints(points, PointMode.Polygon, graticuleColor, strokeWidth = stroke.width)
                }
                points.clear()
            }
        }
        if (points.size > 1) {
            drawPoints(points, PointMode.Polygon, graticuleColor, strokeWidth = stroke.width)
        }
    }

    // Meridians (Longitudes every 45 degrees)
    val longitudes = listOf(-180.0, -135.0, -90.0, -45.0, 0.0, 45.0, 90.0, 135.0)
    longitudes.forEach { lon ->
        val points = mutableListOf<Offset>()
        for (step in -16..16) {
            val lat = (step * (90.0 / 16))
            val p = projectGeo(lat, lon, radius, yaw, pitch)
            if (p.z > 0f) {
                points.add(Offset(center.x + p.x, center.y + p.y))
            } else {
                if (points.size > 1) {
                    drawPoints(points, PointMode.Polygon, graticuleColor, strokeWidth = stroke.width)
                }
                points.clear()
            }
        }
        if (points.size > 1) {
            drawPoints(points, PointMode.Polygon, graticuleColor, strokeWidth = stroke.width)
        }
    }
}

// Draw Continent Outlines & Fills in 3D
private fun DrawScope.drawContinents(center: Offset, radius: Float, yaw: Float, pitch: Float) {
    val landColor = Color(0x3348CAE4)
    val coastColor = NeonAzure.copy(alpha = 0.75f)
    val coastStroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)

    // Simplified polygon outlines for continents (Lat, Lon pairs)
    val continents = listOf(
        // North America
        listOf(
            70.0 to -160.0, 70.0 to -120.0, 55.0 to -90.0, 50.0 to -60.0,
            45.0 to -65.0, 30.0 to -80.0, 25.0 to -80.0, 20.0 to -90.0,
            15.0 to -95.0, 20.0 to -105.0, 32.0 to -117.0, 48.0 to -124.0,
            60.0 to -140.0, 65.0 to -165.0
        ),
        // South America
        listOf(
            12.0 to -72.0, 8.0 to -55.0, -5.0 to -35.0, -22.0 to -40.0,
            -34.0 to -53.0, -55.0 to -65.0, -52.0 to -75.0, -35.0 to -72.0,
            -18.0 to -70.0, -5.0 to -80.0, 5.0 to -77.0
        ),
        // Europe
        listOf(
            71.0 to 25.0, 60.0 to 30.0, 45.0 to 35.0, 40.0 to 25.0,
            36.0 to -5.0, 43.0 to -9.0, 48.0 to -4.0, 54.0 to 8.0,
            60.0 to 5.0, 68.0 to 14.0
        ),
        // Africa
        listOf(
            37.0 to 10.0, 32.0 to 32.0, 12.0 to 43.0, -12.0 to 40.0,
            -34.0 to 26.0, -34.0 to 18.0, -15.0 to 12.0, 5.0 to 2.0,
            5.0 to -8.0, 15.0 to -17.0, 30.0 to -10.0
        ),
        // Asia
        listOf(
            75.0 to 100.0, 70.0 to 170.0, 60.0 to 160.0, 40.0 to 130.0,
            30.0 to 120.0, 20.0 to 110.0, 10.0 to 105.0, 15.0 to 80.0,
            25.0 to 65.0, 30.0 to 50.0, 40.0 to 50.0, 55.0 to 60.0,
            65.0 to 70.0
        ),
        // Australia
        listOf(
            -12.0 to 130.0, -15.0 to 140.0, -25.0 to 153.0, -38.0 to 148.0,
            -35.0 to 115.0, -20.0 to 115.0
        ),
        // Antarctica
        listOf(
            -70.0 to -180.0, -68.0 to -90.0, -65.0 to 0.0, -70.0 to 90.0, -70.0 to 180.0
        )
    )

    continents.forEach { contour ->
        val points = mutableListOf<Offset>()
        var allFront = true

        contour.forEach { (lat, lon) ->
            val p = projectGeo(lat, lon, radius, yaw, pitch)
            if (p.z > 0f) {
                points.add(Offset(center.x + p.x, center.y + p.y))
            } else {
                allFront = false
            }
        }

        if (points.size >= 3) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                if (allFront) close()
            }
            if (allFront) {
                drawPath(path, color = landColor, style = Fill)
            }
            drawPath(path, color = coastColor, style = coastStroke)
        }
    }
}

// 1. Temperature Layer (Dynamic Isotherm Heat Map)
private fun DrawScope.drawTemperatureLayer(center: Offset, radius: Float, yaw: Float, pitch: Float) {
    // Latitudes mapped to temperatures: tropics warm red/orange, poles icy blue/violet
    val samplePoints = listOf(
        Triple(0.0, -60.0, HeatRed),
        Triple(0.0, 20.0, HeatRed),
        Triple(0.0, 100.0, SunGold),
        Triple(25.0, 50.0, SunGold),
        Triple(35.0, -120.0, AtmosphericBlue),
        Triple(55.0, 10.0, AtmosphericBlue),
        Triple(65.0, -100.0, Color(0xFF7209B7)),
        Triple(-30.0, 130.0, SunGold),
        Triple(-50.0, -60.0, Color(0xFF7209B7))
    )

    samplePoints.forEach { (lat, lon, color) ->
        val p = projectGeo(lat, lon, radius, yaw, pitch)
        if (p.z > 0.1f) {
            val alpha = (p.z / radius).coerceIn(0.2f, 0.7f)
            val spotCenter = Offset(center.x + p.x, center.y + p.y)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                    center = spotCenter,
                    radius = radius * 0.45f
                ),
                center = spotCenter,
                radius = radius * 0.45f
            )
        }
    }
}

// 2. Rain Radar Layer (Pulsing radar cells)
private fun DrawScope.drawRainRadarLayer(center: Offset, radius: Float, yaw: Float, pitch: Float, phase: Float) {
    val rainStormCoords = listOf(
        51.5 to -0.12, // London rain
        35.6 to 139.6, // Tokyo rain
        1.3 to 103.8,  // Singapore tropical
        -22.9 to -43.1 // Rio
    )

    rainStormCoords.forEach { (lat, lon) ->
        val p = projectGeo(lat, lon, radius, yaw, pitch)
        if (p.z > 0.15f) {
            val screenPos = Offset(center.x + p.x, center.y + p.y)
            val pulseRadius = (radius * 0.22f) * (0.4f + phase * 0.6f)
            val alpha = (1f - phase) * 0.85f

            // Outer radar ripple
            drawCircle(
                color = RainCyan.copy(alpha = alpha),
                center = screenPos,
                radius = pulseRadius,
                style = Stroke(width = 2.dp.toPx())
            )
            // Dense inner core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RainCyan.copy(alpha = 0.6f), Color.Transparent),
                    center = screenPos,
                    radius = radius * 0.12f
                ),
                center = screenPos,
                radius = radius * 0.12f
            )
        }
    }
}

// 3. Wind Streamlines Layer (Animated curved atmospheric currents)
private fun DrawScope.drawWindStreamlines(center: Offset, radius: Float, yaw: Float, pitch: Float, phase: Float) {
    val windLats = listOf(-45.0, -15.0, 15.0, 45.0)

    windLats.forEachIndexed { idx, lat ->
        val direction = if (lat > 0) 1 else -1
        val points = mutableListOf<Offset>()
        for (i in 0..12) {
            val lon = (i * 25.0) + (phase * 60.0 * direction)
            val p = projectGeo(lat + sin(i * 0.8) * 4.0, lon, radius, yaw, pitch)
            if (p.z > 0.05f) {
                points.add(Offset(center.x + p.x, center.y + p.y))
            } else {
                if (points.size > 2) {
                    drawPoints(points, PointMode.Polygon, ElectricCyan.copy(alpha = 0.5f), strokeWidth = 2.5f)
                }
                points.clear()
            }
        }
        if (points.size > 2) {
            drawPoints(points, PointMode.Polygon, ElectricCyan.copy(alpha = 0.5f), strokeWidth = 2.5f)
        }
    }
}

// 4. Cloud Formations Layer
private fun DrawScope.drawCloudFormations(center: Offset, radius: Float, yaw: Float, pitch: Float, phase: Float) {
    val cloudCenters = listOf(
        Pair(40.0, -40.0 + phase * 20.0),
        Pair(-20.0, 70.0 - phase * 15.0),
        Pair(10.0, 150.0 + phase * 25.0),
        Pair(60.0, -120.0 + phase * 10.0)
    )

    cloudCenters.forEach { (lat, lon) ->
        val p = projectGeo(lat, lon, radius, yaw, pitch)
        if (p.z > 0.1f) {
            val pos = Offset(center.x + p.x, center.y + p.y)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                    center = pos,
                    radius = radius * 0.32f
                ),
                center = pos,
                radius = radius * 0.32f
            )
        }
    }
}

// 5. Storm Cells (Convective lightning sparks)
private fun DrawScope.drawStormCells(center: Offset, radius: Float, yaw: Float, pitch: Float, phase: Float) {
    val stormCoords = listOf(
        25.0 to 120.0,  // East Asia
        15.0 to -90.0,  // Caribbean
        0.0 to 30.0     // Central Africa
    )

    stormCoords.forEach { (lat, lon) ->
        val p = projectGeo(lat, lon, radius, yaw, pitch)
        if (p.z > 0.1f) {
            val pos = Offset(center.x + p.x, center.y + p.y)
            // Violet storm corona
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StormViolet.copy(alpha = 0.65f), Color.Transparent),
                    center = pos,
                    radius = radius * 0.28f
                ),
                center = pos,
                radius = radius * 0.28f
            )

            // Flash effect
            if (phase > 0.6f && phase < 0.85f) {
                drawCircle(
                    color = SunGold.copy(alpha = 0.9f),
                    center = pos,
                    radius = 4.dp.toPx()
                )
            }
        }
    }
}

// 6. Satellite Terminator (Solar Day/Night Line)
private fun DrawScope.drawSatelliteTerminator(center: Offset, radius: Float, yaw: Float, pitch: Float) {
    // Shading on the trailing hemisphere
    drawCircle(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xAA000000), Color.Transparent),
            startX = center.x - radius,
            endX = center.x + radius * 0.35f
        ),
        center = center,
        radius = radius
    )
}

// 7. Render 3D Location Markers with Depth-tested Badges
private fun DrawScope.drawLocationMarkers(
    center: Offset,
    radius: Float,
    yaw: Float,
    pitch: Float,
    markers: List<GlobeMarker>,
    selectedMarker: GlobeMarker?,
    tempUnit: TempUnit
) {
    markers.forEach { marker ->
        val p = projectGeo(marker.location.latitude, marker.location.longitude, radius, yaw, pitch)

        // Only draw markers on the front hemisphere (z > 0)
        if (p.z > 0.05f) {
            val screenPos = Offset(center.x + p.x, center.y + p.y)
            val isSelected = selectedMarker?.location?.id == marker.location.id
            val depthRatio = (p.z / radius).coerceIn(0.4f, 1.0f)

            // Outer pulse circle
            val markerRadius = (if (isSelected) 8.dp else 5.5.dp).toPx() * depthRatio
            val ringRadius = (if (isSelected) 18.dp else 12.dp).toPx() * depthRatio

            val markerColor = when {
                isSelected -> SunGold
                marker.tempC > 28 -> HeatRed
                marker.tempC < 10 -> ElectricCyan
                else -> NeonAzure
            }

            // Halo ripple
            drawCircle(
                color = markerColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                center = screenPos,
                radius = ringRadius,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner pin core
            drawCircle(
                color = markerColor,
                center = screenPos,
                radius = markerRadius
            )

            // Vertical indicator needle
            drawLine(
                color = markerColor.copy(alpha = 0.8f),
                start = screenPos,
                end = Offset(screenPos.x, screenPos.y - 14.dp.toPx() * depthRatio),
                strokeWidth = 1.5.dp.toPx()
            )

            // Floating Weather Badge Pill
            val badgeCenter = Offset(screenPos.x, screenPos.y - 22.dp.toPx() * depthRatio)
            val badgeWidth = (if (isSelected) 56.dp else 42.dp).toPx() * depthRatio
            val badgeHeight = (if (isSelected) 22.dp else 18.dp).toPx() * depthRatio

            drawRoundRect(
                color = Color(0xDD0F1A34),
                topLeft = Offset(badgeCenter.x - badgeWidth / 2f, badgeCenter.y - badgeHeight / 2f),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(badgeHeight / 2f, badgeHeight / 2f)
            )

            drawRoundRect(
                color = markerColor.copy(alpha = if (isSelected) 0.9f else 0.45f),
                topLeft = Offset(badgeCenter.x - badgeWidth / 2f, badgeCenter.y - badgeHeight / 2f),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(badgeHeight / 2f, badgeHeight / 2f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Small dot indicator for condition
            val conditionDotColor = when (marker.condition) {
                WeatherCondition.SUNNY -> SunGold
                WeatherCondition.THUNDERSTORM -> StormViolet
                WeatherCondition.RAINY, WeatherCondition.HEAVY_RAIN -> RainCyan
                else -> AtmosphericBlue
            }
            drawCircle(
                color = conditionDotColor,
                center = Offset(badgeCenter.x - badgeWidth * 0.28f, badgeCenter.y),
                radius = 3.dp.toPx() * depthRatio
            )
        }
    }
}
