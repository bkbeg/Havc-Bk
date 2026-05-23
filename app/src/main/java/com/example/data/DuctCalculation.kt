package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duct_calculations")
data class DuctCalculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ductType: String,
    val width: Double,
    val height: Double,
    val length: Double,
    val extra1: Double = 0.0, // Used for offset, radius, secondary width
    val extra2: Double = 0.0, // Used for taper height2, etc.
    val gaugeThickness: String,
    val quantity: Int = 1,
    val calculatedArea: Double,
    val calculatedWeight: Double,
    val timestamp: Long = System.currentTimeMillis()
)
