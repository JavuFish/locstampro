package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberBadge
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
import com.example.util.ReportExporter
import com.example.util.ShareHelper

enum class ExportType {
    PDF_REPORT,
    EXCEL_CSV
}

@Composable
fun ExportScreen(
    viewModel: LocStampViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastExportResult.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()

    var selectedExportType by remember { mutableStateOf(ExportType.PDF_REPORT) }
    var reportTitle by remember { mutableStateOf("Laporan Dokumentasi Lapangan") }
    var companyName by remember { mutableStateOf("PT. Geospasial Proyek Mandiri") }
    var inspectorName by remember { mutableStateOf("Surveyor Lapangan") }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceDarkElevated)
                    ) {
                        Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentPink.copy(alpha = 0.2f))
                        .border(1.dp, AccentPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = AccentPink, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EKSPOR & BERBAGI LAPORAN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hasilkan Dokumen Resmi PDF & Tabel Excel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Step 1: Format Selector Cards
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. PILIH FORMAT DOKUMEN LAPORAN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF Card
                        val isPdf = selectedExportType == ExportType.PDF_REPORT
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedExportType = ExportType.PDF_REPORT },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPdf) AccentPink.copy(alpha = 0.15f) else SurfaceDarkElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isPdf) AccentPink else SurfaceCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = AccentPink, modifier = Modifier.size(28.dp))
                                Text(text = "Dokumen PDF", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Foto lengkap dengan watermark & metadata",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        // Excel CSV Card
                        val isExcel = selectedExportType == ExportType.EXCEL_CSV
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedExportType = ExportType.EXCEL_CSV },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExcel) AccentEmerald.copy(alpha = 0.15f) else SurfaceDarkElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isExcel) AccentEmerald else SurfaceCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(28.dp))
                                Text(text = "Excel / CSV", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Tabel spreadsheet koordinat GPS & log data",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Metadata Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. RINCIAN INFORMASI LAPORAN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        label = { Text("Judul Laporan", color = PrimaryCyan, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Nama Proyek / Perusahaan / Instansi", color = PrimaryCyan, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = { inspectorName = it },
                        label = { Text("Nama Petugas / Surveyor Lapangan", color = PrimaryCyan, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkElevated)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Jumlah Foto Tercakup:", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "${photos.size} Foto Terpilih", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Step 3: Big Export Button
            Button(
                onClick = {
                    if (photos.isEmpty()) {
                        Toast.makeText(context, "Belum ada foto tersimpan untuk diekspor", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedExportType == ExportType.PDF_REPORT) {
                        viewModel.exportPdf(
                            title = reportTitle,
                            company = companyName,
                            inspector = inspectorName
                        ) { res ->
                            if (res.success) {
                                Toast.makeText(context, "Laporan PDF berhasil dibuat!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF: ${res.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        viewModel.exportExcel { res ->
                            if (res.success) {
                                Toast.makeText(context, "File Excel/CSV berhasil dibuat!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Gagal membuat Excel: ${res.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedExportType == ExportType.PDF_REPORT) AccentPink else AccentEmerald
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sedang Memproses Laporan...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedExportType == ExportType.PDF_REPORT) "BUAT & UNDUH LAPORAN PDF" else "BUAT & UNDUH FILE EXCEL (CSV)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            // Export Result Card & Quick Sharing
            AnimatedVisibility(visible = lastResult != null && lastResult?.success == true && lastResult?.file != null) {
                lastResult?.let { res ->
                    val file = res.file
                    if (file != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentEmerald)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                                    Text(
                                        text = "LAPORAN SIAP DIBAGIKAN!",
                                        color = AccentEmerald,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Text(
                                    text = "File tersimpan: ${file.name}",
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = "Pilih platform untuk mengirim laporan:",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )

                                val mimeType = if (file.name.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "text/csv"

                                // 1-Click Share Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            ShareHelper.shareReportFile(
                                                context = context,
                                                file = file,
                                                mimeType = mimeType,
                                                targetApp = ShareHelper.ShareTarget.WHATSAPP,
                                                title = reportTitle
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            ShareHelper.shareReportFile(
                                                context = context,
                                                file = file,
                                                mimeType = mimeType,
                                                targetApp = ShareHelper.ShareTarget.TELEGRAM,
                                                title = reportTitle
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Telegram", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            ShareHelper.shareReportFile(
                                                context = context,
                                                file = file,
                                                mimeType = mimeType,
                                                targetApp = ShareHelper.ShareTarget.EMAIL,
                                                title = "[LOCSTAMP] $reportTitle - $companyName",
                                                summaryText = "Berikut kami lampirkan dokumen laporan verifikasi foto dan koordinat GPS lapangan dari LocStamp.\n\nTotal foto: ${photos.size} titik.\nPetugas: $inspectorName"
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Email", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            ShareHelper.shareReportFile(
                                                context = context,
                                                file = file,
                                                mimeType = mimeType,
                                                targetApp = ShareHelper.ShareTarget.GENERAL,
                                                title = reportTitle
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Lainnya", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
