package com.eahm.drinkwaterapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.datetime.LocalDateTime

@Dao
interface HistoryDao {

    @Insert
    fun insertAll(vararg records: HistoryEntity)

    @Delete
    fun delete(record: HistoryEntity)

    @Query("SELECT * FROM HistoryEntity")
    fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<HistoryEntity>

    @Query(
        """
            SELECT * FROM HistoryEntity
            WHERE status LIKE :status LIMIT 1       
        """
    )
    fun findByStatus(status: Status): HistoryEntity

    @Query(
        """
            SELECT * FROM HistoryEntity
            WHERE timestamp BETWEEN :start AND :end       
        """
    )
    fun findByTimestamp(start: LocalDateTime, end: LocalDateTime): List<HistoryEntity>
}
