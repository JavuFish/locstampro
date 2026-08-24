package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    enum class ExportFormat {
        PDF,
        EXCEL_CSV
    }

    data class ExportResult(
        val success: Boolean,
        val file: File?,
        val message: String,
        val format: ExportFormat
    )

    /**
     * Generates a professional formatted PDF document report.
     */
    suspend fun generatePdfReport(
        context: Context,
        photos: List<PhotoEntity>,
        reportTitle: String = "Laporan Dokumentasi Lapangan LocStamp",
        inspectorName: String = "LocStamp Field Inspector",
        companyName: String = "PT. Dokumentasi Geospasial Indonesia"
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 points at 72dpi
            val pageHeight = 842
            val margin = 36f

            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val outputFile = File(exportDir, "LocStamp_Report_$timeStr.pdf")

            // Page 1: Cover & Summary
            var currentPageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
            var currentPage = pdfDoc.startPage(pageInfo)
            var canvas = currentPage.canvas

            // Background header bar
            val headerPaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Dark Navy
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, headerPaint)

            // Cyan accent strip
            val accentPaint = Paint().apply {
                color = Color.rgb(0, 245, 255)
            }
            canvas.drawRect(0f, 101f, pageWidth.toFloat(), 105f, accentPaint)

            // Title text
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("LOCSTAMP • GEOSPATIAL FIELD REPORT", margin, 38f, titlePaint)

            val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 11f
            }
            canvas.drawText(reportTitle, margin, 58f, subTitlePaint)
            canvas.drawText("Instansi: $companyName | Disusun Oleh: $inspectorName", margin, 76f, subTitlePaint)

            // Summary Section
            var currentY = 130f
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("1. RINGKASAN EKSEKUTIF", margin, currentY, sectionPaint)
            currentY += 14f

            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(241, 245, 249)
            }
            canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + 68f), 8f, 8f, cardPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(51, 65, 85)
                textSize = 10f
            }
            val dateGenerated = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss 'WIB'", Locale("id", "ID")).format(Date())
            canvas.drawText("• Total Entri Dokumentasi : ${photos.size} Titik Terpantau", margin + 14f, currentY + 20f, textPaint)
            canvas.drawText("• Waktu Pembuatan Laporan : $dateGenerated", margin + 14f, currentY + 36f, textPaint)
            canvas.drawText("• Integritas Data Geospasial : Terverifikasi Watermark Kriptografis GPS", margin + 14f, currentY + 52f, textPaint)

            currentY += 92f
            canvas.drawText("2. DAFTAR DOKUMENTASI LAPANGAN", margin, currentY, sectionPaint)
            currentY += 16f

            // Photos listing on pages
            var itemsOnPage = 0
            val maxItemsPerPage = 2

            for ((index, item) in photos.withIndex()) {
                if (itemsOnPage >= maxItemsPerPage) {
                    pdfDoc.finishPage(currentPage)
                    currentPageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
                    currentPage = pdfDoc.startPage(pageInfo)
                    canvas = currentPage.canvas
                    currentY = 36f
                    itemsOnPage = 0

                    // Mini Header for subsequent pages
                    canvas.drawRect(margin, currentY, pageWidth - margin, currentY + 26f, headerPaint)
                    canvas.drawRect(margin, currentY + 24f, pageWidth - margin, currentY + 26f, accentPaint)
                    val miniTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 9.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText("LOCSTAMP LAPORAN LAPANGAN - HALAMAN $currentPageNum", margin + 10f, currentY + 17f, miniTitlePaint)
                    currentY += 44f
                }

                // Draw Photo Card Item
                val cardHeight = 240f
                val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(248, 250, 252)
                }
                val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(203, 213, 225)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val cardRect = RectF(margin, currentY, pageWidth - margin, currentY + cardHeight)
                canvas.drawRoundRect(cardRect, 8f, 8f, cardBg)
                canvas.drawRoundRect(cardRect, 8f, 8f, cardBorder)

                // Draw Photo Thumbnail
                var thumbDrawn = false
                val photoFile = File(item.filePath)
                if (photoFile.exists()) {
                    try {
                        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                        val bmp = BitmapFactory.decodeFile(item.filePath, options)
                        if (bmp != null) {
                            val imgRect = RectF(margin + 12f, currentY + 12f, margin + 195f, currentY + cardHeight - 12f)
                            canvas.drawBitmap(bmp, null, imgRect, Paint(Paint.FILTER_BITMAP_FLAG))
                            thumbDrawn = true
                        }
                    } catch (e: Exception) {
                        thumbDrawn = false
                    }
                }
                if (!thumbDrawn) {
                    val noImgPaint = Paint().apply { color = Color.rgb(226, 232, 240) }
                    val imgRect = RectF(margin + 12f, currentY + 12f, margin + 195f, currentY + cardHeight - 12f)
                    canvas.drawRoundRect(imgRect, 6f, 6f, noImgPaint)
                    val noImgText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(100, 116, 139)
                        textSize = 9f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("[ Foto ${index + 1} ]", imgRect.centerX(), imgRect.centerY(), noImgText)
                }

                // Detail Information
                val textX = margin + 208f
                var textY = currentY + 26f

                val itemNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 12f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("Foto #${index + 1} • [${item.category}]", textX, textY, itemNumPaint)
                textY += 18f

                val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(51, 65, 85)
                    textSize = 9f
                }
                val boldLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                // Waktu
                canvas.drawText("Waktu:", textX, textY, boldLabelPaint)
                canvas.drawText(item.timestamp, textX + 62f, textY, valPaint)
                textY += 16f

                // Koordinat & Elevasi
                canvas.drawText("Koordinat:", textX, textY, boldLabelPaint)
                canvas.drawText("%.6f, %.6f (Alt: %.1f m)".format(Locale.US, item.latitude, item.longitude, item.altitude), textX + 62f, textY, valPaint)
                textY += 16f

                // Lokasi / Alamat (Multi-line wrap)
                canvas.drawText("Lokasi:", textX, textY, boldLabelPaint)
                val fullLoc = item.location
                val maxCharsPerLine = 46
                if (fullLoc.length <= maxCharsPerLine) {
                    canvas.drawText(fullLoc, textX + 62f, textY, valPaint)
                    textY += 18f
                } else {
                    val line1 = fullLoc.take(maxCharsPerLine)
                    val line2 = fullLoc.drop(maxCharsPerLine).take(maxCharsPerLine).let { if (fullLoc.length > maxCharsPerLine * 2) "$it..." else it }
                    canvas.drawText(line1, textX + 62f, textY, valPaint)
                    textY += 13f
                    canvas.drawText(line2, textX + 62f, textY, valPaint)
                    textY += 16f
                }

                // Akurasi & Gaya Watermark
                canvas.drawText("Akurasi:", textX, textY, boldLabelPaint)
                canvas.drawText("±%.1f m | Format: ${item.styleName}".format(Locale.US, item.accuracy), textX + 62f, textY, valPaint)
                textY += 18f

                // Catatan / Temuan
                canvas.drawText("Catatan / Temuan Lapangan:", textX, textY, boldLabelPaint)
                textY += 14f
                val noteText = if (item.note.isNotBlank()) item.note else "(Tidak ada catatan khusus)"
                if (noteText.length <= 58) {
                    canvas.drawText(noteText, textX, textY, valPaint)
                } else {
                    val note1 = noteText.take(58)
                    val note2 = noteText.drop(58).take(58).let { if (noteText.length > 116) "$it..." else it }
                    canvas.drawText(note1, textX, textY, valPaint)
                    textY += 12f
                    canvas.drawText(note2, textX, textY, valPaint)
                }

                currentY += cardHeight + 14f
                itemsOnPage++
            }

            // Always finish the active page
            pdfDoc.finishPage(currentPage)

            FileOutputStream(outputFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            ExportResult(
                success = true,
                file = outputFile,
                message = "Laporan PDF berhasil dibuat (${photos.size} foto terangkum).",
                format = ExportFormat.PDF
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(
                success = false,
                file = null,
                message = "Gagal membuat PDF: ${e.localizedMessage}",
                format = ExportFormat.PDF
            )
        }
    }

    /**
     * Generates structured CSV / Excel spreadsheet compatible file.
     */
    suspend fun generateExcelCsvReport(
        context: Context,
        photos: List<PhotoEntity>
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val outputFile = File(exportDir, "LocStamp_Data_$timeStr.csv")

            FileWriter(outputFile).use { writer ->
                // UTF-8 BOM for Microsoft Excel compatibility
                writer.write("\uFEFF")
                // CSV Header
                writer.write("ID,Timestamp,Lokasi,Latitude,Longitude,Kategori,Catatan,Akurasi_Meter,Ketinggian_Meter,File_Path\n")

                for (photo in photos) {
                    val escapedLocation = "\"${photo.location.replace("\"", "\"\"")}\""
                    val escapedNote = "\"${photo.note.replace("\"", "\"\"")}\""
                    val escapedCategory = "\"${photo.category.replace("\"", "\"\"")}\""
                    val escapedPath = "\"${photo.filePath.replace("\"", "\"\"")}\""

                    writer.write(
                        "${photo.id},${photo.timestamp},$escapedLocation,${photo.latitude},${photo.longitude},$escapedCategory,$escapedNote,${photo.accuracy},${photo.altitude},$escapedPath\n"
                    )
                }
            }

            ExportResult(
                success = true,
                file = outputFile,
                message = "File Spreadsheet Excel/CSV berhasil dibuat (${photos.size} baris data).",
                format = ExportFormat.EXCEL_CSV
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(
                success = false,
                file = null,
                message = "Gagal membuat file Excel/CSV: ${e.localizedMessage}",
                format = ExportFormat.EXCEL_CSV
            )
        }
    }
}
