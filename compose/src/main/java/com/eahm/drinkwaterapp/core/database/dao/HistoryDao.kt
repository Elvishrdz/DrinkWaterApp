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

    /**
     * Soft-deletes a record so the deletion can be synced as a tombstone (the cloud merge
     * needs to know the record was removed, not just see it missing). Physical removal of
     * expired tombstones is handled by the backup purge policy.
     */
    @Query(
        "UPDATE HistoryEntity SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id"
    )
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)

    @Query("SELECT * FROM HistoryEntity WHERE deleted_at IS NULL ORDER BY timestamp ASC LIMIT 1")
    fun getEarliestRecord(): Flow<HistoryEntity?>

    /** Returns ALL records including tombstones — used by the backup layer, not the UI. */
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
            WHERE timestamp BETWEEN :start AND :end AND deleted_at IS NULL
        """
    )
    fun findByTimestamp(start: LocalDateTime, end: LocalDateTime): Flow<List<HistoryEntity>>

    @Query(
        """
        SELECT SUM(drink_amount)
        FROM HistoryEntity
        WHERE timestamp BETWEEN :start AND :end AND deleted_at IS NULL
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
        WHERE drink_amount > 0 AND deleted_at IS NULL
        GROUP BY (timestamp / 86400000)
        ORDER BY total_drink_amount DESC
        LIMIT 1
        """
    )
    fun getHighestDrinkDay(): Flow<HighestDrinkDay?>

    @Query("DELETE FROM HistoryEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM HistoryEntity WHERE timestamp BETWEEN :start AND :end")
    suspend fun deleteAllBetween(start: LocalDateTime, end: LocalDateTime)
}
