package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LocStampApplication
import com.example.data.model.PhotoEntity
import com.example.ui.theme.AppThemeMode
import com.example.util.LocationHelper
import com.example.util.LocStampLocationData
import com.example.util.ReportExporter
import com.example.util.WatermarkRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocStampViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as LocStampApplication).repository

    private val _rawPhotos = repository.allPhotos
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory = _selectedCategory.asStateFlow()

    val photos: StateFlow<List<PhotoEntity>> = combine(
        _rawPhotos,
        _searchQuery,
        _selectedCategory
    ) { photoList, query, cat ->
        photoList.filter { photo ->
            val matchesQuery = query.isBlank() ||
                    photo.location.contains(query, ignoreCase = true) ||
                    photo.note.contains(query, ignoreCase = true) ||
                    photo.category.contains(query, ignoreCase = true) ||
                    photo.timestamp.contains(query, ignoreCase = true)

            val matchesCategory = cat == "Semua" || photo.category.equals(cat, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPhotoCount: StateFlow<Int> = _rawPhotos.combine(MutableStateFlow(0)) { list, _ ->
        list.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Hardware GPS Location
    private val _deviceGpsLocation = MutableStateFlow<LocStampLocationData?>(null)
    
    // Custom / Overridden Location for Settings
    private val _useCustomLocation = MutableStateFlow(false)
    val useCustomLocation = _useCustomLocation.asStateFlow()

    private val _customLocation = MutableStateFlow(
        LocStampLocationData(
            latitude = -6.175392,
            longitude = 106.827153,
            altitude = 28.5,
            accuracy = 3.0f,
            address = "Monumen Nasional (Monas), Gambir, Jakarta Pusat",
            timestamp = LocationHelper.getCurrentFormattedTimestamp(),
            city = "Jakarta Pusat",
            country = "Indonesia"
        )
    )
    val customLocation = _customLocation.asStateFlow()

    // Combined Effective Location (Either custom location or device GPS)
    val currentLocation: StateFlow<LocStampLocationData?> = combine(
        _deviceGpsLocation,
        _customLocation,
        _useCustomLocation
    ) { devGps, custLoc, isCustom ->
        if (isCustom) custLoc else devGps
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading = _isLocationLoading.asStateFlow()

    private val _selectedStampStyle = MutableStateFlow(WatermarkRenderer.StampStyle.CYBER_NEON)
    val selectedStampStyle = _selectedStampStyle.asStateFlow()

    private val _selectedMapPhoto = MutableStateFlow<PhotoEntity?>(null)
    val selectedMapPhoto = _selectedMapPhoto.asStateFlow()

    // Theme state
    private val _appTheme = MutableStateFlow(AppThemeMode.CYBER_NEON)
    val appTheme: StateFlow<AppThemeMode> = _appTheme.asStateFlow()

    // Coordinate Format (false = DD.DDDDD, true = DMS)
    private val _isDmsFormat = MutableStateFlow(false)
    val isDmsFormat: StateFlow<Boolean> = _isDmsFormat.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    private val _lastExportResult = MutableStateFlow<ReportExporter.ExportResult?>(null)
    val lastExportResult = _lastExportResult.asStateFlow()

    init {
        refreshLocation()
    }

    fun setAppTheme(theme: AppThemeMode) {
        _appTheme.value = theme
    }

    fun setCoordinateFormat(dms: Boolean) {
        _isDmsFormat.value = dms
    }

    fun setUseCustomLocation(useCustom: Boolean) {
        _useCustomLocation.value = useCustom
    }

    fun setCustomLocation(
        lat: Double,
        lng: Double,
        address: String,
        altitude: Double = 25.0,
        accuracy: Float = 3.5f,
        city: String = "Indonesia"
    ) {
        _customLocation.value = LocStampLocationData(
            latitude = lat,
            longitude = lng,
            altitude = altitude,
            accuracy = accuracy,
            address = address,
            timestamp = LocationHelper.getCurrentFormattedTimestamp(),
            city = city,
            country = "Indonesia"
        )
        _useCustomLocation.value = true
    }

    fun resetToDeviceGps() {
        _useCustomLocation.value = false
        refreshLocation()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onStampStyleChanged(style: WatermarkRenderer.StampStyle) {
        _selectedStampStyle.value = style
    }

    fun selectMapPhoto(photo: PhotoEntity?) {
        _selectedMapPhoto.value = photo
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isLocationLoading.value = true
            try {
                val loc = LocationHelper.getCurrentLocation(getApplication())
                _deviceGpsLocation.value = loc
            } finally {
                _isLocationLoading.value = false
            }
        }
    }

    fun saveNewStampPhoto(
        sourceBitmap: Bitmap,
        note: String,
        category: String,
        style: WatermarkRenderer.StampStyle,
        customAddress: String? = null,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val locData = currentLocation.value ?: LocationHelper.getCurrentLocation(getApplication())
            val address = if (!customAddress.isNullOrBlank()) customAddress else locData.address
            val timestamp = LocationHelper.getCurrentFormattedTimestamp()

            val savedFilePath = WatermarkRenderer.createWatermarkedPhoto(
                context = getApplication(),
                sourceBitmap = sourceBitmap,
                timestamp = timestamp,
                locationAddress = address,
                latitude = locData.latitude,
                longitude = locData.longitude,
                note = note,
                category = category,
                altitude = locData.altitude,
                accuracy = locData.accuracy,
                style = style
            )

            val newEntity = PhotoEntity(
                filePath = savedFilePath,
                timestamp = timestamp,
                location = address,
                latitude = locData.latitude,
                longitude = locData.longitude,
                note = note,
                category = category,
                altitude = locData.altitude,
                accuracy = locData.accuracy,
                styleName = style.displayName
            )

            val insertedId = repository.insertPhoto(newEntity)
            onComplete(insertedId)
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
            if (_selectedMapPhoto.value?.id == photo.id) {
                _selectedMapPhoto.value = null
            }
        }
    }

    fun exportPdf(
        title: String,
        company: String,
        inspector: String,
        onResult: (ReportExporter.ExportResult) -> Unit
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val currentList = _rawPhotos.stateIn(viewModelScope).value
                val result = ReportExporter.generatePdfReport(
                    context = getApplication(),
                    photos = currentList,
                    reportTitle = title.ifBlank { "Laporan Dokumentasi Lapangan LocStamp" },
                    companyName = company.ifBlank { "PT. Geospasial Nusantara" },
                    inspectorName = inspector.ifBlank { "LocStamp Verified Agent" }
                )
                _lastExportResult.value = result
                onResult(result)
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportExcel(
        onResult: (ReportExporter.ExportResult) -> Unit
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val currentList = _rawPhotos.stateIn(viewModelScope).value
                val result = ReportExporter.generateExcelCsvReport(
                    context = getApplication(),
                    photos = currentList
                )
                _lastExportResult.value = result
                onResult(result)
            } finally {
                _isExporting.value = false
            }
        }
    }
}
