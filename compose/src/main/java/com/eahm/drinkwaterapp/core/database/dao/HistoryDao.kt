package com.eahm.drinkwaterapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.database.model.HighestDrinkDay
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface HistoryDao {

    @Insert
    fun insertAll(vararg records: HistoryEntity)

    @Update
    fun update(record: HistoryEntity)

    @Delete
    fun delete(record: HistoryEntity)

    @Query("SELECT * FROM HistoryEntity ORDER BY timestamp ASC LIMIT 1")
    fun getEarliestRecord(): Flow<HistoryEntity?>

    @Query("SELECT * FROM HistoryEntity")
    fun getAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM HistoryEntity WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): Flow<List<HistoryEntity>>

    @Query(
        """
            SELECT * FROM HistoryEntity
            WHERE status LIKE :status LIMIT 1       
        """
    )
    fun findByStatus(status: Status): Flow<HistoryEntity>

    @Query(
        """
            SELECT * FROM HistoryEntity
            WHERE timestamp BETWEEN :start AND :end       
        """
    )
    fun findByTimestamp(start: LocalDateTime, end: LocalDateTime): Flow<List<HistoryEntity>>

    @Query(
        """
        SELECT SUM(drink_amount) 
        FROM HistoryEntity
        WHERE timestamp BETWEEN :start AND :end       
        """
    )
    fun getTotalDrinkAmount(start: LocalDateTime, end: LocalDateTime): Flow<Float>

    /**
     * Returns the single day with the highest total water intake, or null if there are no records.
     *
     * Timestamps are stored as epoch milliseconds (UTC), so dividing by 86400000 converts them
     * to epoch days, the unit expected by LocalDateConverter. Zero-amount entries (skipped
     * notifications) are excluded from the aggregation.
     */
    @Query(
        """
        SELECT
            (timestamp / 86400000) as day,
            SUM(drink_amount) as total_drink_amount
        FROM HistoryEntity
        WHERE drink_amount > 0
        GROUP BY (timestamp / 86400000)
        ORDER BY total_drink_amount DESC
        LIMIT 1
        """
    )
    fun getHighestDrinkDay(): Flow<HighestDrinkDay?>

    @Query("DELETE FROM HistoryEntity")
    suspend fun deleteAll()
}
