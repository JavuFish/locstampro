package com.example

import android.app.Application
import com.example.data.local.LocStampDatabase
import com.example.data.repository.PhotoRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocStampApplication : Application() {
    val database: LocStampDatabase by lazy { LocStampDatabase.getDatabase(this) }
    val repository: PhotoRepository by lazy { PhotoRepository(database.photoDao(), this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@LocStampApplication) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.initializeSampleDataIfNeeded()
        }
    }
}
