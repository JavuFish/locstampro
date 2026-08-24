package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "filePath")
    val filePath: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
    
    @ColumnInfo(name = "location")
    val location: String,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    
    @ColumnInfo(name = "note")
    val note: String,
    
    // Additional optional metadata for advanced documentation
    @ColumnInfo(name = "category", defaultValue = "Umum")
    val category: String = "Umum",
    
    @ColumnInfo(name = "altitude", defaultValue = "0.0")
    val altitude: Double = 0.0,
    
    @ColumnInfo(name = "accuracy", defaultValue = "5.0")
    val accuracy: Float = 5.0f,
    
    @ColumnInfo(name = "styleName", defaultValue = "Cyber Neon")
    val styleName: String = "Cyber Neon"
)
