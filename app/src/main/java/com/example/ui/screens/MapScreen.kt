package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PhotoEntity
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.CyberBadge
import com.example.ui.components.PhotoDetailDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundDeep
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LocStampViewModel
import com.example.util.ShareHelper
import java.io.File
import java.util.Locale

@Composable
fun MapScreen(
    viewModel: LocStampViewModel,
    onNavigateToTimeline: () -> Unit,
    onNavigateToExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val locData by viewModel.currentLocation.collectAsStateWithLifecycle()
    val selectedPhoto by viewModel.selectedMapPhoto.collectAsStateWithLifecycle()

    var showPhotoDetailDialog by remember { mutableStateOf(false) }

    // Map Pan & Zoom Transformation State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Auto-select first photo if none selected
    LaunchedEffect(photos) {
        if (selectedPhoto == null && photos.isNotEmpty()) {
            viewModel.selectMapPhoto(photos.first())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        // Interactive Map Surface
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            val center = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)

            // Grid lines
            val gridSize = 60f * zoomScale
            var x = center.x % gridSize
            while (x < size.width) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSize
            }

            var y = center.y % gridSize
            while (y < size.height) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }

            // Radar Ring Grid
            val maxRadius = (size.width.coerceAtLeast(size.height)) * zoomScale
            val step = 100f * zoomScale
            var r = step
            while (r <= maxRadius) {
                drawCircle(
                    color = Color(0xFF334155).copy(alpha = 0.35f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1f)
                )
                r += step
            }

            // User Current Location Center Node
            drawCircle(
                color = PrimaryCyan.copy(alpha = 0.2f),
                radius = 24f * zoomScale,
                center = center
            )
            drawCircle(
                color = PrimaryCyan,
                radius = 8f * zoomScale,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f * zoomScale,
                center = center
            )

            // Base lat/long
            val baseLat = locData?.latitude ?: -6.175392
            val baseLng = locData?.longitude ?: 106.827153
            val geoScale = 4500f * zoomScale

            // Draw Photo Location Pins
            for (photo in photos) {
                val dLat = (photo.latitude - baseLat).toFloat()
                val dLng = (photo.longitude - baseLng).toFloat()

                val pinX = center.x + (dLng * geoScale)
                val pinY = center.y - (dLat * geoScale)
                val pinPos = Offset(pinX, pinY)

                val isSelected = selectedPhoto?.id == photo.id

                // Pin colors by category
                val pinColor = when (photo.category) {
                    "Survey" -> PrimaryCyan
                    "Proyek" -> AccentViolet
                    "Inspeksi" -> AccentAmber
                    "Audit" -> AccentPink
                    else -> AccentEmerald
                }

                // Connecting line if selected
                if (isSelected) {
                    drawLine(
                        brush = Brush.linearGradient(listOf(PrimaryCyan, AccentViolet)),
                        start = center,
                        end = pinPos,
                        strokeWidth = 2.5f
                    )
                }

                // Outer Glow
                drawCircle(
                    color = pinColor.copy(alpha = if (isSelected) 0.45f else 0.25f),
                    radius = if (isSelected) 22f * zoomScale else 14f * zoomScale,
                    center = pinPos
                )

                // Pin Point
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 10f * zoomScale else 6.5f * zoomScale,
                    center = pinPos
                )

                // Pin Core
                drawCircle(
                    color = if (isSelected) Color.White else Color(0xFF0F172A),
                    radius = if (isSelected) 4f * zoomScale else 2.5f * zoomScale,
                    center = pinPos
                )
            }
        }

        // Top Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLogoIcon(size = 36.dp, showGlow = false)
                    Column {
                        Text(
                            text = "PETA LOKASI DOKUMENTASI",
                            color = PrimaryCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${photos.size} Pin Titik Foto Terpasang",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }


                // Center on GPS button
                IconButton(
                    onClick = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1.0f
                        viewModel.refreshLocation()
                        Toast.makeText(context, "Posisi GPS Direset", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceDarkElevated, CircleShape)
                        .border(1.dp, PrimaryCyan.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Pusatkan GPS", tint = PrimaryCyan)
                }
            }

            // Quick Pin Selector Strip
            if (photos.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundDark.copy(alpha = 0.9f))
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    photos.forEach { photo ->
                        val isSel = selectedPhoto?.id == photo.id
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) PrimaryCyan else SurfaceDarkElevated,
                            modifier = Modifier.clickable { viewModel.selectMapPhoto(photo) }
                        ) {
                            Text(
                                text = "#${photo.id} ${photo.category}",
                                color = if (isSel) Color(0xFF0F172A) else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Map Zoom Controls (Floating Right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) },
                modifier = Modifier
                    .size(38.dp)
                    .background(SurfaceDarkElevated.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextPrimary)
            }

            IconButton(
                onClick = { zoomScale = (zoomScale * 0.8f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(38.dp)
                    .background(SurfaceDarkElevated.copy(alpha = 0.9f), CircleShape)
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextPrimary)
            }
        }

        // Bottom Detail Card (When pin is selected)
        AnimatedVisibility(
            visible = selectedPhoto != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 86.dp)
        ) {
            selectedPhoto?.let { photo ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCard.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CyberBadge(text = "#${photo.id} • ${photo.category}")
                                Text(
                                    text = photo.timestamp,
                                    color = PrimaryCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectMapPhoto(null) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Content Row: Thumbnail + Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { showPhotoDetailDialog = true }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(photo.filePath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Pin",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = photo.location,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )

                                Text(
                                    text = "🌐 %.6f, %.6f (Alt: %.1fm)".format(Locale.US, photo.latitude, photo.longitude, photo.altitude),
                                    color = AccentPink,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                if (photo.note.isNotBlank()) {
                                    Text(
                                        text = "📝 ${photo.note}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Action Buttons: "Buka Google Maps" and "Lihat Detail"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:${photo.latitude},${photo.longitude}?q=${photo.latitude},${photo.longitude}(${Uri.encode(photo.location)})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Aplikasi peta tidak tersedia", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentEmerald),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showPhotoDetailDialog = true },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "Lihat Detail", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { ShareHelper.sharePhoto(context, photo) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(SurfaceDarkElevated, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Full Photo Detail Dialog
        if (showPhotoDetailDialog && selectedPhoto != null) {
            PhotoDetailDialog(
                photo = selectedPhoto!!,
                onDismiss = { showPhotoDetailDialog = false },
                onDelete = {
                    viewModel.deletePhoto(selectedPhoto!!)
                    showPhotoDetailDialog = false
                }
            )
        }
    }
}
