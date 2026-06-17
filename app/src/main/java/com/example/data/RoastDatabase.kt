package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "roast_history")
data class RoastRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomType: String,
    val roastLevel: String,
    val roastText: String,
    val timestampMs: Long = System.currentTimeMillis()
)

@Dao
interface RoastDao {
    @Query("SELECT * FROM roast_history ORDER BY timestampMs DESC")
    fun getAllRoasts(): Flow<List<RoastRecord>>

    @Insert
    suspend fun insertRoast(roast: RoastRecord)

    @Query("DELETE FROM roast_history WHERE id = :id")
    suspend fun deleteRoastById(id: Int)
}
