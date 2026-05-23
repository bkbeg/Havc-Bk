package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DuctCalculationDao {
    @Query("SELECT * FROM duct_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<DuctCalculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: DuctCalculation)

    @Query("DELETE FROM duct_calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Int)

    @Delete
    suspend fun deleteCalculation(calculation: DuctCalculation)

    @Query("DELETE FROM duct_calculations")
    suspend fun clearAll()
}
