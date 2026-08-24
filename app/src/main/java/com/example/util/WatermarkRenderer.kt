package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object WatermarkRenderer {

    enum class StampStyle(val displayName: String) {
        CYBER_NEON("Cyber Neon"),
        TACTICAL_HUD("Tactical HUD"),
        CLEAN_MINIMAL("Clean Minimal"),
        OFFICIAL_AUDIT("Official Audit")
    }

    /**
     * Applies the burned-in high resolution watermark overlay on top of the input bitmap
     * with vector-sharp anti-aliasing and subpixel precision, saving at 100% lossless JPEG quality.
     */
    fun createWatermarkedPhoto(
        context: Context,
        sourceBitmap: Bitmap,
        timestamp: String,
        locationAddress: String,
        latitude: Double,
        longitude: Double,
        note: String,
        category: String = "Survey",
        altitude: Double = 32.5,
        accuracy: Float = 3.2f,
        style: StampStyle = StampStyle.CYBER_NEON
    ): String {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val outputBitmap: Bitmap = try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (oom: OutOfMemoryError) {
            // Fallback in low RAM environments
            try {
                Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            } catch (oom2: OutOfMemoryError) {
                // Scale down 50% if still OOM
                Bitmap.createScaledBitmap(sourceBitmap, width / 2, height / 2, true)
            }
        }
        val canvas = Canvas(outputBitmap)

        // Draw the base photo with high quality anti-aliasing & filtering
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        if (outputBitmap != sourceBitmap) {
            val srcRect = android.graphics.Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            val dstRect = android.graphics.Rect(0, 0, outputBitmap.width, outputBitmap.height)
            canvas.drawBitmap(sourceBitmap, srcRect, dstRect, basePaint)
        }

        val renderWidth = outputBitmap.width
        val renderHeight = outputBitmap.height

        // Proportional scale factor relative to 1080px short edge.
        // Guarantees vector-crisp, non-pixelated typography on all resolutions (1080p, 2K, 4K, 8K)
        val minDim = min(renderWidth, renderHeight).toFloat()
        val scale = (minDim / 1080f).coerceAtLeast(1.0f)
        val padding = 32f * scale

        when (style) {
            StampStyle.CYBER_NEON -> drawCyberNeonOverlay(
                canvas, renderWidth, renderHeight, scale, padding,
                timestamp, locationAddress, latitude, longitude, altitude, accuracy, note, category
            )
            StampStyle.TACTICAL_HUD -> drawTacticalHudOverlay(
                canvas, renderWidth, renderHeight, scale, padding,
                timestamp, locationAddress, latitude, longitude, altitude, accuracy, note, category
            )
            StampStyle.CLEAN_MINIMAL -> drawCleanMinimalOverlay(
                canvas, renderWidth, renderHeight, scale, padding,
                timestamp, locationAddress, latitude, longitude, note
            )
            StampStyle.OFFICIAL_AUDIT -> drawOfficialAuditOverlay(
                canvas, renderWidth, renderHeight, scale, padding,
                timestamp, locationAddress, latitude, longitude, altitude, accuracy, note, category
            )
        }

        // Save output to disk with 100% pristine JPEG compression quality
        val filename = "LOCSTAMP_${System.currentTimeMillis()}.jpg"
        val storageDir = context.getExternalFilesDir("stamped_photos") ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs()
        val file = File(storageDir, filename)

        FileOutputStream(file).use { out ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        return file.absolutePath
    }

    private fun createPaint(
        color: Int = Color.WHITE,
        size: Float = 16f,
        typeface: Typeface = Typeface.DEFAULT,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 0f
    ): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            this.color = color
            this.textSize = size
            this.typeface = typeface
            this.style = style
            this.isAntiAlias = true
            this.isSubpixelText = true
            this.isFilterBitmap = true
            if (strokeWidth > 0f) this.strokeWidth = strokeWidth
        }
    }

    private fun wrapTextToLines(text: String, paint: Paint, maxWidth: Float, maxLines: Int = 2): List<String> {
        if (text.isBlank()) return emptyList()
        if (paint.measureText(text) <= maxWidth) return listOf(text)
        
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                    if (lines.size == maxLines - 1) {
                        break
                    }
                } else {
                    lines.add(truncateWithEllipsis(word, paint, maxWidth))
                    if (lines.size == maxLines - 1) break
                }
            }
        }
        
        if (lines.size < maxLines && currentLine.isNotEmpty()) {
            val usedWordsCount = lines.sumOf { it.split(" ").size }
            val remainingWords = words.drop(usedWordsCount).joinToString(" ")
            val finalLine = if (remainingWords.isNotBlank()) remainingWords else currentLine.toString()
            lines.add(truncateWithEllipsis(finalLine, paint, maxWidth))
        }
        return lines
    }

    private fun truncateWithEllipsis(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + "...") > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + "..." else "..."
    }

    private fun drawCyberNeonOverlay(
        canvas: Canvas,
        w: Int,
        h: Int,
        scale: Float,
        pad: Float,
        timestamp: String,
        location: String,
        lat: Double,
        lng: Double,
        alt: Double,
        acc: Float,
        note: String,
        category: String
    ) {
        val bannerHeight = (if (note.isNotBlank()) 310f else 265f) * scale
        val top = h - bannerHeight - pad
        val left = pad
        val right = w - pad
        val bottom = h - pad
        val usableWidth = right - left - (48f * scale)

        // Drop shadow backdrop for maximum readability on bright backgrounds
        val shadowPaint = createPaint(Color.argb(120, 0, 0, 0))
        val shadowRect = RectF(left + (4f * scale), top + (4f * scale), right + (4f * scale), bottom + (4f * scale))
        canvas.drawRoundRect(shadowRect, 20f * scale, 20f * scale, shadowPaint)

        // Solid High-Contrast Background Glass Panel (Deep Dark Onyx)
        val bgPaint = createPaint(Color.argb(245, 8, 12, 28))
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 20f * scale, 20f * scale, bgPaint)

        // Neon Glow Border (Sharp & High Resolution)
        val borderPaint = createPaint(
            style = Paint.Style.STROKE,
            strokeWidth = 3.5f * scale
        ).apply {
            shader = LinearGradient(
                left, top, right, bottom,
                intArrayOf(
                    Color.rgb(0, 245, 255),  // Neon Cyan
                    Color.rgb(168, 85, 247), // Neon Purple
                    Color.rgb(255, 0, 127)   // Neon Pink
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, 20f * scale, 20f * scale, borderPaint)

        // Top Cyber Accent Strip
        val accentPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            style = Paint.Style.STROKE,
            strokeWidth = 5f * scale
        )
        canvas.drawLine(left + 28f * scale, top, left + 160f * scale, top, accentPaint)

        var curY = top + 42f * scale

        // Header Row: Category Badge + GPS Lock Tag
        val badgeBgPaint = createPaint(Color.rgb(147, 51, 234))
        val badgeRect = RectF(left + 24f * scale, top + 16f * scale, left + 220f * scale, top + 52f * scale)
        canvas.drawRoundRect(badgeRect, 8f * scale, 8f * scale, badgeBgPaint)

        val badgeTextPaint = createPaint(
            color = Color.WHITE,
            size = 17f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        canvas.drawText("LOCSTAMP • $category", left + 32f * scale, top + 40f * scale, badgeTextPaint)

        val gpsLockPaint = createPaint(
            color = Color.rgb(0, 255, 157),
            size = 17f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        ).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("⚡ GPS LOCK (±%.1fm)".format(Locale.US, acc), right - 24f * scale, top + 40f * scale, gpsLockPaint)

        curY += 38f * scale

        // 1. Crystal Clear Timestamp Text
        val timePaint = createPaint(
            color = Color.rgb(0, 245, 255),
            size = 28f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        canvas.drawText("📅 $timestamp", left + 24f * scale, curY, timePaint)
        curY += 38f * scale

        // 2. Clear Readable Location Address Text
        val locPaint = createPaint(
            color = Color.rgb(255, 255, 255),
            size = 20f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        val locLines = wrapTextToLines(location, locPaint, usableWidth, maxLines = 2)
        if (locLines.isNotEmpty()) {
            canvas.drawText("📍 ${locLines[0]}", left + 24f * scale, curY, locPaint)
            curY += 30f * scale
            if (locLines.size > 1) {
                canvas.drawText("    ${locLines[1]}", left + 24f * scale, curY, locPaint)
                curY += 30f * scale
            }
        }

        // 3. High-Contrast Monospace Coordinates & Altitude (Zero Blur)
        val coordPaint = createPaint(
            color = Color.rgb(255, 110, 180),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        val latDir = if (lat >= 0) "N" else "S"
        val lngDir = if (lng >= 0) "E" else "W"
        val coordStr = "🌐 %.6f° %s, %.6f° %s | ALT: %.1f m".format(Locale.US, Math.abs(lat), latDir, Math.abs(lng), lngDir, alt)
        canvas.drawText(coordStr, left + 24f * scale, curY, coordPaint)
        curY += 34f * scale

        // 4. Field Memo / Note if present
        if (note.isNotBlank()) {
            val notePaint = createPaint(
                color = Color.rgb(255, 215, 0),
                size = 18f * scale,
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            )
            val noteLines = wrapTextToLines(note, notePaint, usableWidth, maxLines = 1)
            if (noteLines.isNotEmpty()) {
                canvas.drawText("📝 ${noteLines[0]}", left + 24f * scale, curY, notePaint)
            }
        }

        // Top Right Corner Tag
        val brandTopPaint = createPaint(
            color = Color.argb(220, 0, 245, 255),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        ).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("LOCSTAMP PRO HD", w - pad - 12f * scale, pad + 32f * scale, brandTopPaint)
    }

    private fun drawTacticalHudOverlay(
        canvas: Canvas,
        w: Int,
        h: Int,
        scale: Float,
        pad: Float,
        timestamp: String,
        location: String,
        lat: Double,
        lng: Double,
        alt: Double,
        acc: Float,
        note: String,
        category: String
    ) {
        val bannerHeight = (if (note.isNotBlank()) 290f else 250f) * scale
        val top = h - bannerHeight - pad
        val left = pad
        val right = w - pad
        val bottom = h - pad
        val usableWidth = right - left - (44f * scale)

        // Backdrop Shadow
        val shadowPaint = createPaint(Color.argb(120, 0, 0, 0))
        canvas.drawRect(RectF(left + (4f * scale), top + (4f * scale), right + (4f * scale), bottom + (4f * scale)), shadowPaint)

        val bgPaint = createPaint(Color.argb(248, 10, 15, 28))
        val rect = RectF(left, top, right, bottom)
        canvas.drawRect(rect, bgPaint)

        // Tactical Emerald Grid Border
        val borderPaint = createPaint(
            color = Color.rgb(0, 255, 157),
            style = Paint.Style.STROKE,
            strokeWidth = 3f * scale
        )
        canvas.drawRect(rect, borderPaint)

        // Crosshairs in corners
        val chLen = 24f * scale
        canvas.drawLine(left, top, left + chLen, top, borderPaint)
        canvas.drawLine(left, top, left, top + chLen, borderPaint)
        canvas.drawLine(right, top, right - chLen, top, borderPaint)
        canvas.drawLine(right, top, right, top + chLen, borderPaint)

        var curY = top + 38f * scale

        val titlePaint = createPaint(
            color = Color.rgb(0, 255, 157),
            size = 20f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        canvas.drawText("[LOCSTAMP TACTICAL AUDIT • $category]", left + 22f * scale, curY, titlePaint)
        curY += 34f * scale

        val whitePaint = createPaint(
            color = Color.WHITE,
            size = 21f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        canvas.drawText("TIME: $timestamp", left + 22f * scale, curY, whitePaint)
        curY += 32f * scale

        val locPaint = createPaint(
            color = Color.rgb(241, 245, 249),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        val locLines = wrapTextToLines(location, locPaint, usableWidth, maxLines = 2)
        if (locLines.isNotEmpty()) {
            canvas.drawText("LOC : ${locLines[0]}", left + 22f * scale, curY, locPaint)
            curY += 30f * scale
            if (locLines.size > 1) {
                canvas.drawText("      ${locLines[1]}", left + 22f * scale, curY, locPaint)
                curY += 30f * scale
            }
        }

        val coordPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        canvas.drawText("POS : %.6f, %.6f (ALT: %.1f m ±%.1f m)".format(Locale.US, lat, lng, alt, acc), left + 22f * scale, curY, coordPaint)
        curY += 32f * scale
        
        if (note.isNotBlank()) {
            val memoPaint = createPaint(
                color = Color.rgb(255, 190, 0),
                size = 18f * scale,
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            )
            val noteLines = wrapTextToLines(note, memoPaint, usableWidth, maxLines = 1)
            if (noteLines.isNotEmpty()) {
                canvas.drawText("MEMO: ${noteLines[0]}", left + 22f * scale, curY, memoPaint)
            }
        }
    }

    private fun drawCleanMinimalOverlay(
        canvas: Canvas,
        w: Int,
        h: Int,
        scale: Float,
        pad: Float,
        timestamp: String,
        location: String,
        lat: Double,
        lng: Double,
        note: String
    ) {
        val bannerHeight = (if (note.isNotBlank()) 220f else 190f) * scale
        val top = h - bannerHeight - pad
        val left = pad
        val right = w - pad
        val bottom = h - pad
        val usableWidth = right - left - (48f * scale)

        val bgPaint = createPaint(Color.argb(225, 0, 0, 0))
        canvas.drawRoundRect(RectF(left, top, right, bottom), 18f * scale, 18f * scale, bgPaint)

        var curY = top + 42f * scale

        val textPaint = createPaint(
            color = Color.WHITE,
            size = 26f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        canvas.drawText(timestamp, left + 24f * scale, curY, textPaint)
        curY += 36f * scale

        val locPaint = createPaint(
            color = Color.rgb(241, 245, 249),
            size = 20f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        val locLines = wrapTextToLines(location, locPaint, usableWidth, maxLines = 2)
        if (locLines.isNotEmpty()) {
            canvas.drawText(locLines[0], left + 24f * scale, curY, locPaint)
            curY += 28f * scale
            if (locLines.size > 1) {
                canvas.drawText(locLines[1], left + 24f * scale, curY, locPaint)
                curY += 28f * scale
            }
        }

        val subPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            size = 18f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        val noteSuffix = if (note.isNotBlank()) " • " + truncateWithEllipsis(note, subPaint, 240f * scale) else ""
        val coordStr = "%.6f, %.6f%s".format(Locale.US, lat, lng, noteSuffix)
        canvas.drawText(coordStr, left + 24f * scale, curY, subPaint)
    }

    private fun drawOfficialAuditOverlay(
        canvas: Canvas,
        w: Int,
        h: Int,
        scale: Float,
        pad: Float,
        timestamp: String,
        location: String,
        lat: Double,
        lng: Double,
        alt: Double,
        acc: Float,
        note: String,
        category: String
    ) {
        val barH = (if (note.isNotBlank()) 280f else 240f) * scale
        val top = h - barH
        val usableWidth = w - (56f * scale)

        val bgPaint = createPaint(Color.argb(250, 10, 15, 30))
        canvas.drawRect(0f, top, w.toFloat(), h.toFloat(), bgPaint)

        // Top divider line
        val linePaint = createPaint(
            color = Color.rgb(255, 0, 127),
            strokeWidth = 5f * scale,
            style = Paint.Style.STROKE
        )
        canvas.drawLine(0f, top, w.toFloat(), top, linePaint)

        var curY = top + 40f * scale

        val headerPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            size = 23f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        canvas.drawText("DOKUMENTASI RESMI LAPANGAN • $category", 28f * scale, curY, headerPaint)
        curY += 36f * scale

        val bodyPaint = createPaint(
            color = Color.WHITE,
            size = 20f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        canvas.drawText("Waktu: $timestamp", 28f * scale, curY, bodyPaint)
        curY += 30f * scale

        val locPaint = createPaint(
            color = Color.rgb(241, 245, 249),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        )
        val locLines = wrapTextToLines(location, locPaint, usableWidth, maxLines = 2)
        if (locLines.isNotEmpty()) {
            canvas.drawText("Lokasi: ${locLines[0]}", 28f * scale, curY, locPaint)
            curY += 28f * scale
            if (locLines.size > 1) {
                canvas.drawText("        ${locLines[1]}", 28f * scale, curY, locPaint)
                curY += 28f * scale
            }
        }
        
        val coordPaint = createPaint(
            color = Color.rgb(255, 190, 0),
            size = 19f * scale,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        )
        canvas.drawText("Koordinat: %.6f, %.6f (Alt: %.1f m, Akurasi: ±%.1f m)".format(Locale.US, lat, lng, alt, acc), 28f * scale, curY, coordPaint)
        curY += 30f * scale

        if (note.isNotBlank()) {
            val notePaint = createPaint(
                color = Color.rgb(226, 232, 240),
                size = 18f * scale,
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            )
            val noteLines = wrapTextToLines(note, notePaint, usableWidth, maxLines = 1)
            if (noteLines.isNotEmpty()) {
                canvas.drawText("Catatan: ${noteLines[0]}", 28f * scale, curY, notePaint)
            }
        }
    }

    /**
     * Generates a high-definition sample photo bitmap (1920x1440) for initial preview.
     */
    fun createSamplePhotoBitmap(title: String, category: String, hueColor: Int): Bitmap {
        val width = 1920
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Backdrop
        val bgPaint = createPaint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(Color.rgb(15, 23, 42), hueColor, Color.rgb(30, 41, 59)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Grid Lines for Blueprint/Field Texture
        val gridPaint = createPaint(
            color = Color.argb(45, 255, 255, 255),
            strokeWidth = 2.5f,
            style = Paint.Style.STROKE
        )
        for (x in 0 until width step 120) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
        }
        for (y in 0 until height step 120) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
        }

        // Geometric field scene elements
        val shapePaint = createPaint(
            color = Color.argb(60, 255, 255, 255)
        )
        canvas.drawCircle(width / 2f, height / 2f - 80f, 300f, shapePaint)

        val markerPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            style = Paint.Style.STROKE,
            strokeWidth = 8f
        )
        canvas.drawCircle(width / 2f, height / 2f - 80f, 340f, markerPaint)

        // Title text on the photo
        val textPaint = createPaint(
            color = Color.WHITE,
            size = 54f,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        ).apply {
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, width / 2f, height / 2f - 90f, textPaint)

        val subPaint = createPaint(
            color = Color.rgb(0, 245, 255),
            size = 36f,
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        ).apply {
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CATEGORY: $category", width / 2f, height / 2f - 30f, subPaint)

        return bitmap
    }
}
