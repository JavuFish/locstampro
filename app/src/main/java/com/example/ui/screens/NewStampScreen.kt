package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NoteCategoryRegistry
import com.example.ui.components.AdMobBanner
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.CyberBadge
import com.example.ui.components.PulsingGlowDot
import com.example.ui.components.QuickHashtagChip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LocStampViewModel
import com.example.util.ImageCaptureHelper
import com.example.util.LocationHelper
import com.example.util.WatermarkRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Locale

@Composable
fun NewStampScreen(
    viewModel: LocStampViewModel,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locData by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isLocLoading by viewModel.isLocationLoading.collectAsStateWithLifecycle()

    var noteText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Visit") }
    var selectedStyle by remember { mutableStateOf(WatermarkRenderer.StampStyle.OFFICIAL_AUDIT) }
    var customAddress by remember { mutableStateOf("") }
    var isEditingAddress by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Active captured or generated photo bitmap
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isPreviewingWithWatermark by remember { mutableStateOf(false) }
    var watermarkedPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val categories = NoteCategoryRegistry.allCategories
    val styles = WatermarkRenderer.StampStyle.values()

    // Initialize with a clean sample photo if none
    LaunchedEffect(Unit) {
        if (currentBitmap == null) {
            val bmp = WatermarkRenderer.createSamplePhotoBitmap(
                title = "Objek Verifikasi Lapangan",
                category = selectedCategory,
                hueColor = android.graphics.Color.rgb(18, 48, 86)
            )
            currentBitmap = bmp
        }
    }

    // Native Camera photo pickers & Camera capture with Lifecycle Survival
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val nativeBmp = ImageCaptureHelper.loadNativeCameraBitmap(context, uri = uri)
                withContext(Dispatchers.Main) {
                    if (nativeBmp != null) {
                        currentBitmap = nativeBmp
                        isPreviewingWithWatermark = false
                        Toast.makeText(context, "Foto galeri berhasil dimuat", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal memproses gambar galeri", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uriToUse = pendingCameraUri ?: pendingCameraUriString?.let { Uri.parse(it) }
            val pathToUse = pendingCameraPath
            scope.launch(Dispatchers.IO) {
                val nativeBmp = ImageCaptureHelper.loadNativeCameraBitmap(
                    context = context,
                    uri = uriToUse,
                    fallbackFilePath = pathToUse
                )
                withContext(Dispatchers.Main) {
                    if (nativeBmp != null) {
                        currentBitmap = nativeBmp
                        isPreviewingWithWatermark = false
                        Toast.makeText(context, "Foto kamera berhasil diambil!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal memproses foto dari kamera", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val (tempUri, tempFile) = ImageCaptureHelper.createCameraTempUri(context)
                pendingCameraUri = tempUri
                pendingCameraPath = tempFile.absolutePath
                pendingCameraUriString = tempUri.toString()
                cameraLauncher.launch(tempUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Kamera tidak dapat dibuka: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_LONG).show()
        }
    }

    // Recompute watermarked preview
    fun generateWatermarkPreview() {
        val baseBmp = currentBitmap ?: return
        val loc = locData
        val lat = loc?.latitude ?: -6.175392
        val lng = loc?.longitude ?: 106.827153
        val alt = loc?.altitude ?: 25.0
        val acc = loc?.accuracy ?: 3.5f
        val addr = if (customAddress.isNotBlank()) customAddress else (loc?.address ?: "Jakarta Pusat")
        val time = LocationHelper.getCurrentFormattedTimestamp()

        scope.launch(Dispatchers.Default) {
            val path = WatermarkRenderer.createWatermarkedPhoto(
                context = context,
                sourceBitmap = baseBmp,
                timestamp = time,
                locationAddress = addr,
                latitude = lat,
                longitude = lng,
                note = noteText,
                category = selectedCategory,
                altitude = alt,
                accuracy = acc,
                style = selectedStyle
            )
            val generatedBmp = BitmapFactory.decodeFile(path)
            withContext(Dispatchers.Main) {
                watermarkedPreviewBitmap = generatedBmp
                isPreviewingWithWatermark = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(SurfaceDarkElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                    AppLogoIcon(size = 36.dp, showGlow = false)
                    Column {
                        Text(
                            text = "AMBIL & CAP FOTO",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryCyan,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Dokumentasi Geospasial Ber-Watermark",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Refresh GPS button
                IconButton(
                    onClick = {
                        viewModel.refreshLocation()
                        Toast.makeText(context, "Memperbarui koordinat GPS...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceDarkElevated, CircleShape)
                ) {
                    if (isLocLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = AccentEmerald)
                    }
                }
            }


            // Step 1: Photo Preview & Capture Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. PRATINJAU FOTO & WATERMARK",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Viewfinder box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayBitmap = if (isPreviewingWithWatermark && watermarkedPreviewBitmap != null) {
                            watermarkedPreviewBitmap
                        } else {
                            currentBitmap
                        }

                        if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap.asImageBitmap(),
                                contentDescription = "Pratinjau Foto",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Live Watermark HUD Banner (when not showing final burned bitmap)
                        if (!isPreviewingWithWatermark) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "📅 ${locData?.timestamp ?: LocationHelper.getCurrentFormattedTimestamp()}",
                                            color = PrimaryCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "#$selectedCategory",
                                            color = AccentViolet,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    val addr = if (customAddress.isNotBlank()) customAddress else (locData?.address ?: "Jakarta Pusat")
                                    val shortAddr = if (addr.length > 40) addr.take(38) + "..." else addr
                                    Text(
                                        text = "📍 $shortAddr",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )

                                    val lat = locData?.latitude ?: -6.175392
                                    val lng = locData?.longitude ?: 106.827153
                                    Text(
                                        text = "🌐 %.6f, %.6f (Alt: %.1fm)".format(Locale.US, lat, lng, locData?.altitude ?: 24.0),
                                        color = AccentPink,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Direct Action Buttons for Photo Source
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val hasCameraPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasCameraPerm) {
                                    try {
                                        val (tempUri, tempFile) = ImageCaptureHelper.createCameraTempUri(context)
                                        pendingCameraUri = tempUri
                                        pendingCameraPath = tempFile.absolutePath
                                        pendingCameraUriString = tempUri.toString()
                                        cameraLauncher.launch(tempUri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Kamera tidak dapat dibuka: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Buka Kamera", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentViolet),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentViolet.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pilih Galeri", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Step 2: Choose Watermark Stamp Style
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. PILIHAN TAMPILAN CAP / WATERMARK",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        styles.forEach { style ->
                            val isSelected = selectedStyle == style
                            val styleLabel = when (style) {
                                WatermarkRenderer.StampStyle.OFFICIAL_AUDIT -> "🏢 Standar Proyek"
                                WatermarkRenderer.StampStyle.CYBER_NEON -> "⚡ Cyber Modern"
                                WatermarkRenderer.StampStyle.TACTICAL_HUD -> "🎯 Taktikal Lapangan"
                                WatermarkRenderer.StampStyle.CLEAN_MINIMAL -> "📄 Minimalis Bersih"
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else SurfaceDarkElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) PrimaryCyan else SurfaceCardBorder
                                ),
                                modifier = Modifier.clickable {
                                    selectedStyle = style
                                    isPreviewingWithWatermark = false
                                }
                            ) {
                                Text(
                                    text = styleLabel,
                                    color = if (isSelected) PrimaryCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Step 3: Category & Note Fields
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. KATEGORI & CATATAN DOKUMENTASI",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "#$selectedCategory",
                            color = PrimaryCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Category Chips with icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true) || selectedCategory.equals(cat.hashtag, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) cat.color.copy(alpha = 0.25f) else SurfaceDarkElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) cat.color else SurfaceCardBorder
                                ),
                                modifier = Modifier.clickable {
                                    selectedCategory = cat.name
                                    isPreviewingWithWatermark = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(text = cat.icon, fontSize = 12.sp)
                                    Text(
                                        text = cat.hashtag,
                                        color = if (isSelected) cat.color else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Field Note input
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            isPreviewingWithWatermark = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Tuliskan catatan kunjungan, tugas luar, kondisi aset, atau temuan...",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Quick Note Hashtags Bar (#visit, #tugas luar, #liburan, #santai, #nongkrong, #cafe, #warkop)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "TAMBAH HASHTAG CEPAT KE CATATAN:",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NoteCategoryRegistry.requestedHashtags.forEach { tag ->
                                val categoryMatch = NoteCategoryRegistry.allCategories.find { it.hashtag.equals(tag, ignoreCase = true) }
                                val tagColor = categoryMatch?.color ?: AccentViolet
                                val isContained = noteText.contains(tag, ignoreCase = true)

                                QuickHashtagChip(
                                    tag = tag,
                                    isSelected = isContained,
                                    activeColor = tagColor,
                                    onClick = {
                                        if (!isContained) {
                                            noteText = if (noteText.isBlank()) "$tag " else "$noteText $tag "
                                        }
                                        if (categoryMatch != null) {
                                            selectedCategory = categoryMatch.name
                                        }
                                        isPreviewingWithWatermark = false
                                    }
                                )
                            }
                        }
                    }
                }
            }


            // Step 4: GPS & Location Verification
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentPink, modifier = Modifier.size(18.dp))
                            Text(text = "4. INFORMASI LOKASI GPS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { isEditingAddress = !isEditingAddress },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.EditLocation, contentDescription = "Edit Alamat", tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (isEditingAddress) {
                        OutlinedTextField(
                            value = customAddress,
                            onValueChange = {
                                customAddress = it
                                isPreviewingWithWatermark = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(locData?.address ?: "Ketik nama alamat khusus...", color = TextSecondary, fontSize = 12.sp) },
                            label = { Text("Kustom Alamat", color = PrimaryCyan, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = if (customAddress.isNotBlank()) customAddress else (locData?.address ?: "Mendeteksi posisi GPS..."),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lat: %.6f, Lng: %.6f".format(Locale.US, locData?.latitude ?: -6.175392, locData?.longitude ?: 106.827153),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Akurasi: ±%.1fm".format(Locale.US, locData?.accuracy ?: 4.0f),
                            color = AccentEmerald,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Bottom Save & Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "BATAL", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val base = currentBitmap
                        if (base == null) {
                            Toast.makeText(context, "Silakan ambil atau pilih foto terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        viewModel.saveNewStampPhoto(
                            sourceBitmap = base,
                            note = noteText,
                            category = selectedCategory,
                            style = selectedStyle,
                            customAddress = if (customAddress.isNotBlank()) customAddress else null
                        ) { insertedId ->
                            isSaving = false
                            Toast.makeText(context, "Foto berhasil dicap & disimpan ke database!", Toast.LENGTH_SHORT).show()
                            onSaved(insertedId)
                        }
                    },
                    modifier = Modifier.weight(1.8f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "SIMPAN FOTO STEMPEL", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }

            AdMobBanner(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
