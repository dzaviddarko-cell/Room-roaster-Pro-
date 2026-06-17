package com.example.data

import kotlinx.coroutines.flow.Flow

class RoastRepository(private val dao: RoastDao) {
    val allRoasts: Flow<List<RoastRecord>> = dao.getAllRoasts()

    suspend fun insert(roast: RoastRecord) {
        dao.insertRoast(roast)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteRoastById(id)
    }
}
