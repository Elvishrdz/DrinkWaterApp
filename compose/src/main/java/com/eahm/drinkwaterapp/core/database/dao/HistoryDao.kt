package com.eahm.drinkwaterapp.core.database.dao

import HistoryEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT * FROM HistoryEntity")
    fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<HistoryEntity>

    @Query(
        "SELECT * FROM HistoryEntity " +
                "WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1"
    )
    fun findByName(first: String, last: String): HistoryEntity

    @Insert
    fun insertAll(vararg records: HistoryEntity)

    @Delete
    fun delete(record: HistoryEntity)
}
