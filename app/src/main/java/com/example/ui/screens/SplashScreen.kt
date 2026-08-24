package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundDeep
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stepText by remember { mutableStateOf("Menginisialisasi modul GPS & kamera...") }
    val progress = remember { Animatable(0.05f) }
    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }

    // Infinite transitions for rotating radar and pulsing halo
    val infiniteTransition = rememberInfiniteTransition(label = "splashInfinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringGlowAlpha"
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // Progress step 1
        progress.animateTo(0.35f, animationSpec = tween(500))
        stepText = "Memuat sensor koordinat satelit WGS84..."
        delay(450)

        // Progress step 2
        progress.animateTo(0.70f, animationSpec = tween(500))
        stepText = "Sinkronisasi database stempel & galeri..."
        delay(450)

        // Progress step 3
        progress.animateTo(1f, animationSpec = tween(350))
        stepText = "Siap digunakan! Membuka LocStamp..."
        delay(300)

        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDeep,
                        BackgroundDark,
                        Color(0xFF060D1E)
                    )
                )
            )
            .clickable { onSplashFinished() }, // Allow instant tap-to-skip
        contentAlignment = Alignment.Center
    ) {
        // Background Radar Grid & Coordinates
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)
            val maxRadius = size.width * 0.48f

            // Concentric radar rings
            val ringCount = 4
            for (i in 1..ringCount) {
                val radius = maxRadius * (i.toFloat() / ringCount)
                drawCircle(
                    color = PrimaryCyan.copy(alpha = 0.08f * (1f - (i - 1) * 0.15f)),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Radar Crosshairs
            drawLine(
                color = PrimaryCyan.copy(alpha = 0.12f),
                start = Offset(center.x - maxRadius * 1.15f, center.y),
                end = Offset(center.x + maxRadius * 1.15f, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = PrimaryCyan.copy(alpha = 0.12f),
                start = Offset(center.x, center.y - maxRadius * 1.15f),
                end = Offset(center.x, center.y + maxRadius * 1.15f),
                strokeWidth = 1.dp.toPx()
            )

            // Rotating Radar Sweep Line
            val sweepRad = Math.toRadians(rotation.toDouble())
            val sweepEnd = Offset(
                x = center.x + (maxRadius * 0.95f * cos(sweepRad)).toFloat(),
                y = center.y + (maxRadius * 0.95f * sin(sweepRad)).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        PrimaryCyan.copy(alpha = 0.8f),
                        AccentViolet.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    start = center,
                    end = sweepEnd
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Top Header: Skip hint & App Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 48.dp)
        ) {
            Surface(
                modifier = Modifier.align(Alignment.CenterStart),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDarkElevated.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald)
                    )
                    Text(
                        text = "GPS READY • 3D LOCK",
                        color = PrimaryCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onSplashFinished() },
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDarkElevated.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Text(
                    text = "LEWATI ➔",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Main Center Animated Logo & Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Emblem
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleAnim.value * pulseScale)
                    .alpha(alphaAnim.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Neon Glow Ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryCyan.copy(alpha = ringGlowAlpha * 0.45f),
                                    AccentViolet.copy(alpha = ringGlowAlpha * 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Rotating Dashed Orbit Ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .rotate(rotation)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    PrimaryCyan,
                                    AccentViolet,
                                    AccentPink,
                                    PrimaryCyan
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Central Icon Container (Glassmorphic / Cyber Badge)
                Surface(
                    modifier = Modifier
                        .size(92.dp)
                        .shadow(16.dp, CircleShape, spotColor = PrimaryCyan, ambientColor = AccentViolet),
                    shape = CircleShape,
                    color = Color(0xFF0B1426),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        Brush.linearGradient(listOf(PrimaryCyan, AccentViolet))
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Camera & GPS Combination Icon
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "LocStamp Camera",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(42.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "LocStamp GPS Pin",
                            tint = AccentPink,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Neon Gradient
            Text(
                text = "LOCSTAMP",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif,
                color = PrimaryCyan,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Text(
                text = "GEOSPATIAL CAMERA & VERIFIED TIMESTAMP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                color = AccentViolet,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tag badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(alphaAnim.value)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDarkElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "WATERMARK HUD",
                        color = PrimaryCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDarkElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "EXCEL & PDF EXPORT",
                        color = AccentAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Progress Bar & Status Text
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .alpha(alphaAnim.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryCyan,
                    trackColor = PrimaryCyan.copy(alpha = 0.2f)
                )

                Text(
                    text = stepText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Footer Metadata
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Versi 1.2.0 • Geospatial Field Engine",
                color = TextSecondary.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Kamera Stempel Waktu & Lokasi Presisi",
                color = TextSecondary.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}
