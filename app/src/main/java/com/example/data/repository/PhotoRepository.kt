package com.example.data.repository

import android.content.Context
import android.graphics.Color
import com.example.data.local.PhotoDao
import com.example.data.model.PhotoEntity
import com.example.util.LocationHelper
import com.example.util.WatermarkRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PhotoRepository(
    private val photoDao: PhotoDao,
    private val context: Context
) {
    val allPhotos: Flow<List<PhotoEntity>> = photoDao.getAllPhotos()

    fun getPhotoById(id: Long): Flow<PhotoEntity?> = photoDao.getPhotoById(id)

    fun searchPhotos(query: String): Flow<List<PhotoEntity>> = photoDao.searchPhotos(query)

    suspend fun insertPhoto(photo: PhotoEntity): Long = withContext(Dispatchers.IO) {
        photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: PhotoEntity) = withContext(Dispatchers.IO) {
        photoDao.deletePhoto(photo)
    }

    suspend fun deletePhotoById(id: Long) = withContext(Dispatchers.IO) {
        photoDao.deletePhotoById(id)
    }

    suspend fun initializeSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = photoDao.getPhotoCount()
        if (count == 0) {
            val samples = listOf(
                SampleItem(
                    title = "Proyek Jembatan Layang Semanggi",
                    category = "Proyek",
                    location = "Simpang Susun Semanggi, Jl. Jend. Sudirman, Jakarta Selatan",
                    latitude = -6.220194,
                    longitude = 106.815944,
                    note = "Pemeriksaan pilar beton struktur utama zona B3 dalam kondisi kokoh.",
                    timestamp = "2026-08-20 09:15:22",
                    hueColor = Color.rgb(16, 50, 95)
                ),
                SampleItem(
                    title = "Inspeksi Gardu Induk Listrik",
                    category = "Inspeksi",
                    location = "Jl. Rasuna Said Kav. 62, Kuningan, Jakarta Selatan",
                    latitude = -6.228945,
                    longitude = 106.831512,
                    note = "Suhu transformator terpantau stabil pada 48°C dengan voltase normal.",
                    timestamp = "2026-08-19 14:40:10",
                    hueColor = Color.rgb(75, 20, 90)
                ),
                SampleItem(
                    title = "Survey Lapangan Menara Telekomunikasi",
                    category = "Survey",
                    location = "Jl. M.H. Thamrin No. 28, Menteng, Jakarta Pusat",
                    latitude = -6.192534,
                    longitude = 106.823901,
                    note = "Verifikasi azimuth antena sektor 1 dan pengecekan grounding tower.",
                    timestamp = "2026-08-18 11:20:45",
                    hueColor = Color.rgb(15, 70, 50)
                ),
                SampleItem(
                    title = "Audit Fasilitas Pelabuhan Tanjung Priok",
                    category = "Audit",
                    location = "Dermaga Kontainer 2, Pelabuhan Tanjung Priok, Jakarta Utara",
                    latitude = -6.103822,
                    longitude = 106.882194,
                    note = "Pemeriksaan integritas kontainer reefer suhu dingin zona impor.",
                    timestamp = "2026-08-17 16:05:30",
                    hueColor = Color.rgb(90, 40, 20)
                )
            )

            for (sample in samples) {
                val sampleBitmap = WatermarkRenderer.createSamplePhotoBitmap(
                    title = sample.title,
                    category = sample.category,
                    hueColor = sample.hueColor
                )
                val filePath = WatermarkRenderer.createWatermarkedPhoto(
                    context = context,
                    sourceBitmap = sampleBitmap,
                    timestamp = sample.timestamp,
                    locationAddress = sample.location,
                    latitude = sample.latitude,
                    longitude = sample.longitude,
                    note = sample.note,
                    category = sample.category,
                    altitude = 24.0,
                    accuracy = 2.8f,
                    style = WatermarkRenderer.StampStyle.CYBER_NEON
                )

                val entity = PhotoEntity(
                    filePath = filePath,
                    timestamp = sample.timestamp,
                    location = sample.location,
                    latitude = sample.latitude,
                    longitude = sample.longitude,
                    note = sample.note,
                    category = sample.category,
                    altitude = 24.0,
                    accuracy = 2.8f,
                    styleName = "Cyber Neon"
                )
                photoDao.insertPhoto(entity)
            }
        }
    }

    private data class SampleItem(
        val title: String,
        val category: String,
        val location: String,
        val latitude: Double,
        val longitude: Double,
        val note: String,
        val timestamp: String,
        val hueColor: Int
    )
}
