package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LocStampViewModel
import com.example.util.WatermarkRenderer
import java.util.Locale

data class LocationPreset(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val alt: Double,
    val icon: String
)

@Composable
fun SettingScreen(
    viewModel: LocStampViewModel,
    onNavigateToExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appTheme by viewModel.appTheme.collectAsState()
    val useCustomLocation by viewModel.useCustomLocation.collectAsState()
    val customLocationData by viewModel.customLocation.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isDms by viewModel.isDmsFormat.collectAsState()
    val currentStampStyle by viewModel.selectedStampStyle.collectAsState()

    // Form inputs for custom location
    var inputLat by remember(customLocationData) { mutableStateOf(customLocationData.latitude.toString()) }
    var inputLng by remember(customLocationData) { mutableStateOf(customLocationData.longitude.toString()) }
    var inputAddress by remember(customLocationData) { mutableStateOf(customLocationData.address) }
    var inputAlt by remember(customLocationData) { mutableStateOf(customLocationData.altitude.toString()) }

    var showResetDialog by remember { mutableStateOf(false) }

    val presets = remember {
        listOf(
            LocationPreset(
                name = "Monas, Jakarta",
                address = "Monumen Nasional (Monas), Gambir, Jakarta Pusat",
                lat = -6.175392,
                lng = 106.827153,
                alt = 28.5,
                icon = "🏛️"
            ),
            LocationPreset(
                name = "IKN Nusantara",
                address = "Kawasan Inti Pusat Pemerintahan (KIPP), IKN Nusantara, Kaltim",
                lat = -0.963478,
                lng = 116.702951,
                alt = 85.0,
                icon = "🏗️"
            ),
            LocationPreset(
                name = "GBK Senayan",
                address = "Stadion Utama Gelora Bung Karno, Tanah Abang, Jakarta Pusat",
                lat = -6.218567,
                lng = 106.802315,
                alt = 15.0,
                icon = "🏟️"
            ),
            LocationPreset(
                name = "Tanjung Priok",
                address = "Pelabuhan Tanjung Priok, Jakarta Utara",
                lat = -6.104231,
                lng = 106.883721,
                alt = 5.0,
                icon = "🚢"
            ),
            LocationPreset(
                name = "Bandara Soetta",
                address = "Bandara Internasional Soekarno-Hatta, Tangerang, Banten",
                lat = -6.127500,
                lng = 106.653700,
                alt = 10.0,
                icon = "✈️"
            ),
            LocationPreset(
                name = "Puncak Bogor",
                address = "Puncak Pass, Cisarua, Kabupaten Bogor, Jawa Barat",
                lat = -6.702621,
                lng = 106.994231,
                alt = 1450.0,
                icon = "🏔️"
            ),
            LocationPreset(
                name = "Kuta, Bali",
                address = "Pantai Kuta, Badung, Bali",
                lat = -8.718490,
                lng = 115.168630,
                alt = 6.0,
                icon = "🏖️"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppLogoIcon(size = 40.dp, showGlow = true)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Setting & Konfigurasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Atur lokasi map kustom, theme, koordinat & stempel",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }
        }


        // 2. SECTION: ATUR LOKASI MAP (Map & Location Settings)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ATUR LOKASI MAP & KOORDINAT",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pilih mode GPS otomatis atau set lokasi manual untuk stempel & peta",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Mode Switch: GPS vs Custom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (useCustomLocation) AccentAmber.copy(alpha = 0.5f) else AccentEmerald.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (useCustomLocation) "Mode Lokasi: Kustom / Manual" else "Mode Lokasi: GPS Perangkat Otomatis",
                                    color = if (useCustomLocation) AccentAmber else AccentEmerald,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (useCustomLocation)
                                        "Kamera & stempel akan menggunakan koordinat kustom di bawah."
                                    else
                                        "Menggunakan sinyal sensor GPS hardware asli perangkat.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = useCustomLocation,
                                onCheckedChange = { viewModel.setUseCustomLocation(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentAmber,
                                    checkedTrackColor = AccentAmber.copy(alpha = 0.3f),
                                    uncheckedThumbColor = AccentEmerald,
                                    uncheckedTrackColor = AccentEmerald.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    // Preset Buttons (Quick Picker)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Preset Titik Lokasi Populer Indonesia:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presets) { preset ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            inputLat = preset.lat.toString()
                                            inputLng = preset.lng.toString()
                                            inputAddress = preset.address
                                            inputAlt = preset.alt.toString()
                                            viewModel.setCustomLocation(
                                                lat = preset.lat,
                                                lng = preset.lng,
                                                address = preset.address,
                                                altitude = preset.alt
                                            )
                                            Toast.makeText(context, "Lokasi diubah ke: ${preset.name}", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(preset.icon, fontSize = 12.sp)
                                        Text(
                                            text = preset.name,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input Form Fields
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Address Field
                        OutlinedTextField(
                            value = inputAddress,
                            onValueChange = { inputAddress = it },
                            label = { Text("Nama Alamat / Titik Proyek", fontSize = 12.sp) },
                            placeholder = { Text("Contoh: Kawasan Industri Cikarang, Blok B") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = false,
                            maxLines = 2
                        )

                        // Lat & Lng Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputLat,
                                onValueChange = { inputLat = it },
                                label = { Text("Latitude", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = SurfaceCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = inputLng,
                                onValueChange = { inputLng = it },
                                label = { Text("Longitude", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = SurfaceCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Altitude Row
                        OutlinedTextField(
                            value = inputAlt,
                            onValueChange = { inputAlt = it },
                            label = { Text("Ketinggian / Elevasi (Meter)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Save & Apply Custom Location Button
                        Button(
                            onClick = {
                                val lat = inputLat.toDoubleOrNull() ?: -6.175392
                                val lng = inputLng.toDoubleOrNull() ?: 106.827153
                                val alt = inputAlt.toDoubleOrNull() ?: 25.0
                                viewModel.setCustomLocation(
                                    lat = lat,
                                    lng = lng,
                                    address = inputAddress.ifBlank { "Lokasi Kustom Terpantau" },
                                    altitude = alt
                                )
                                Toast.makeText(context, "Lokasi map kustom berhasil disimpan & diaktifkan!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "💾 Simpan & Terapkan Lokasi Kustom Ini",
                                color = Color(0xFF04060C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Reset to Hardware GPS
                        if (useCustomLocation) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetToDeviceGps()
                                    Toast.makeText(context, "Kembali ke mode GPS Sensor Asli", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AccentEmerald)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset & Gunakan GPS Asli Perangkat",
                                    color = AccentEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. SECTION: PENGATURAN TEMA (Theme Settings)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "PENGATURAN TEMA & TAMPILAN",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pilih skema warna & gaya antarmuka aplikasi",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Theme Options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = appTheme == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.setAppTheme(mode)
                                        Toast.makeText(context, "Tema diubah ke: ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) mode.primaryColor else SurfaceCardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Color Palette dots preview
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(mode.primaryColor))
                                        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(mode.secondaryColor))
                                        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(mode.backgroundColor))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.displayName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = mode.description,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Terpilih",
                                            tint = mode.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Format Koordinat Option (DD vs DMS)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, SurfaceCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Format Notasi Koordinat",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isDms) "Format Derajat Menit Detik (DMS - 6°10'31\"S)" else "Format Desimal (DD.DDDDDD°)",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isDms,
                                onCheckedChange = { viewModel.setCoordinateFormat(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. SECTION: EKSPOR & DOKUMEN CEPAT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EKSPOR LAPORAN & DOKUMEN",
                        color = AccentPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unduh seluruh foto dan catatan ke dalam dokumen PDF resmi dengan watermark & data tabular Excel CSV.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = onNavigateToExport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Halaman Ekspor Laporan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. BANNER ADMOB
        item {
            AdMobBanner(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        }

        // 6. SECTION: INFO APLIKASI
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "LocStamp • Geospatial Field Camera v1.2.0 Pro",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keamanan Data & Integritas Kriptografis GPS Aktif",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
