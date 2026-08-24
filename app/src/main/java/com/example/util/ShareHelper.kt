package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.PhotoEntity
import java.io.File
import java.net.URLEncoder

object ShareHelper {

    /**
     * Gets a shareable content Uri using the FileProvider.
     */
    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Shares a single stamped photo with its location-timestamp summary.
     */
    fun sharePhoto(
        context: Context,
        photo: PhotoEntity,
        targetAppPackage: String? = null
    ) {
        val file = File(photo.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "Berkas foto tidak ditemukan di penyimpanan", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = getFileUri(context, file)
        val textBody = buildString {
            append("📍 *DOKUMENTASI LOCSTAMP*\n")
            append("📅 Waktu: ${photo.timestamp}\n")
            append("🏢 Lokasi: ${photo.location}\n")
            append("🌐 Koordinat: ${photo.latitude}, ${photo.longitude}\n")
            if (photo.note.isNotBlank()) {
                append("📝 Catatan: ${photo.note}\n")
            }
            append("\n_Diverifikasi via LocStamp Mobile_")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, textBody)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (!targetAppPackage.isNullOrBlank()) {
                setPackage(targetAppPackage)
            }
        }

        try {
            if (targetAppPackage != null) {
                context.startActivity(shareIntent)
            } else {
                val chooser = Intent.createChooser(shareIntent, "Bagikan Foto LocStamp")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            // Fallback to standard chooser if target app is not installed
            val fallbackChooser = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, textBody)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Bagikan Foto LocStamp"
            )
            context.startActivity(fallbackChooser)
        }
    }

    /**
     * Shares an exported report (PDF / CSV) via WhatsApp, Telegram, Email, or Chooser.
     */
    fun shareReportFile(
        context: Context,
        file: File,
        mimeType: String,
        targetApp: ShareTarget = ShareTarget.GENERAL,
        title: String = "Laporan LocStamp",
        summaryText: String = "Berikut laporan dokumentasi lapangan hasil ekspor aplikasi LocStamp."
    ) {
        if (!file.exists()) {
            Toast.makeText(context, "Berkas laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = getFileUri(context, file)

        when (targetApp) {
            ShareTarget.WHATSAPP -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, summaryText)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                tryLaunch(context, intent, fallbackMime = mimeType, fileUri = uri, text = summaryText)
            }
            ShareTarget.TELEGRAM -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, summaryText)
                    setPackage("org.telegram.messenger")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                tryLaunch(context, intent, fallbackMime = mimeType, fileUri = uri, text = summaryText)
            }
            ShareTarget.EMAIL -> {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, summaryText)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(emailIntent, "Kirim Laporan via Email")
                context.startActivity(chooser)
            }
            ShareTarget.GENERAL -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, summaryText)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Bagikan Laporan")
                context.startActivity(chooser)
            }
        }
    }

    private fun tryLaunch(context: Context, specificIntent: Intent, fallbackMime: String, fileUri: Uri, text: String) {
        try {
            context.startActivity(specificIntent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = fallbackMime
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Bagikan Dokumen"))
        }
    }

    enum class ShareTarget {
        GENERAL,
        WHATSAPP,
        TELEGRAM,
        EMAIL
    }
}
