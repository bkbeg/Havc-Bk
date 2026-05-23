package com.example.data

import kotlinx.coroutines.flow.Flow

class DuctCalculationRepository(private val ductCalculationDao: DuctCalculationDao) {
    val allCalculations: Flow<List<DuctCalculation>> = ductCalculationDao.getAllCalculations()

    suspend fun insert(calculation: DuctCalculation) {
        ductCalculationDao.insertCalculation(calculation)
    }

    suspend fun deleteById(id: Int) {
        ductCalculationDao.deleteCalculationById(id)
    }

    suspend fun delete(calculation: DuctCalculation) {
        ductCalculationDao.deleteCalculation(calculation)
    }

    suspend fun clearAll() {
        ductCalculationDao.clearAll()
    }
}
