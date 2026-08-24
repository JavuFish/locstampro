package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.max

object ImageCaptureHelper {

    private const val PREFS_NAME = "locstamp_camera_prefs"
    private const val KEY_LAST_PHOTO_PATH = "last_temp_photo_path"

    /**
     * Prepares a temporary file and content Uri for device camera capture.
     * Stores the path in SharedPreferences so it survives Activity recreation across all OEM ROMs.
     */
    fun createCameraTempUri(context: Context): Pair<Uri, File> {
        val storageDir = context.getExternalFilesDir("camera_captures")
            ?: File(context.cacheDir, "camera_captures").apply { if (!exists()) mkdirs() }
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Clean up old temporary raw captures (older than 1 hour) to keep device storage clean
        cleanupOldTempFiles(storageDir)

        val file = File(storageDir, "capture_raw_${System.currentTimeMillis()}.jpg")
        if (!file.exists()) {
            file.createNewFile()
        }

        // Persist to SharedPreferences as a fallback for Activity recreation
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LAST_PHOTO_PATH, file.absolutePath).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(uri, file)
    }

    fun getLastSavedTempFile(context: Context): File? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val path = prefs.getString(KEY_LAST_PHOTO_PATH, null) ?: return null
            val file = File(path)
            if (file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes photo taken by native phone camera or picked from gallery in maximum crystal-clear resolution.
     * Preserves ultra-sharp details (up to 4096px / 4K native sensor quality) with safe fallback.
     */
    fun loadNativeCameraBitmap(
        context: Context,
        uri: Uri? = null,
        fallbackFilePath: String? = null,
        targetMaxDimension: Int = 4096
    ): Bitmap? {
        // Priority 1: From File Path if valid
        if (!fallbackFilePath.isNullOrBlank()) {
            val file = File(fallbackFilePath)
            if (file.exists() && file.length() > 0) {
                val bmp = decodeFromFileWithExif(file.absolutePath, targetMaxDimension)
                if (bmp != null) return bmp
            }
        }

        // Priority 2: From Uri
        if (uri != null) {
            val bmp = decodeFromUriWithExif(context, uri, targetMaxDimension)
            if (bmp != null) return bmp
        }

        // Priority 3: Fallback from last saved temp file in preferences
        val lastFile = getLastSavedTempFile(context)
        if (lastFile != null && lastFile.exists() && lastFile.length() > 0) {
            return decodeFromFileWithExif(lastFile.absolutePath, targetMaxDimension)
        }

        return null
    }

    private fun decodeFromFileWithExif(filePath: String, maxDim: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)

            val width = options.outWidth
            val height = options.outHeight
            if (width <= 0 || height <= 0) return null

            // Determine sample size only if image exceeds 4K maxDim (e.g. 4096px)
            var sampleSize = 1
            val maxDimension = max(width, height)
            while (maxDimension / (sampleSize * 2) >= maxDim) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = true
            }
            val decoded = BitmapFactory.decodeFile(filePath, decodeOpts) ?: return null

            // Correct EXIF orientation
            val degrees = getExifRotation(filePath)
            if (degrees != 0) {
                val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else {
                decoded
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            // In case of high memory pressure, fallback gracefully
            try {
                val fallbackOpts = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = BitmapFactory.decodeFile(filePath, fallbackOpts) ?: return null
                val degrees = getExifRotation(filePath)
                if (degrees != 0) {
                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }
            } catch (ex: Throwable) {
                null
            }
        }
    }

    private fun decodeFromUriWithExif(context: Context, uri: Uri, maxDim: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val width = options.outWidth
            val height = options.outHeight
            if (width <= 0 || height <= 0) return null

            var sampleSize = 1
            val maxDimension = max(width, height)
            while (maxDimension / (sampleSize * 2) >= maxDim) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = true
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val degrees = getExifRotationFromUri(context, uri)
            if (degrees != 0) {
                val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else {
                decoded
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            // In case of memory pressure, retry with sampleSize 2
            try {
                val fallbackOpts = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, fallbackOpts)
                } ?: return null
                val degrees = getExifRotationFromUri(context, uri)
                if (degrees != 0) {
                    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }
            } catch (ex: Throwable) {
                null
            }
        }
    }

    private fun getExifRotation(filePath: String): Int {
        return try {
            val exif = ExifInterface(filePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun getExifRotationFromUri(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun cleanupOldTempFiles(storageDir: File) {
        try {
            val oneHourAgo = System.currentTimeMillis() - 3600000L
            storageDir.listFiles()?.forEach { f ->
                if (f.isFile && f.lastModified() < oneHourAgo) {
                    f.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup failures
        }
    }
}
