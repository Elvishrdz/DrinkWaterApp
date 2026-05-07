package com.eahm.drinkwaterapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow

const val DEFAULT_GOAL_ML = 500f

@Dao
interface DailyGoalDao {

    @Query("SELECT * FROM daily_goal WHERE effective_date <= :epochDay ORDER BY effective_date DESC LIMIT 1")
    fun getGoalForDate(epochDay: Long): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goal WHERE effective_date BETWEEN :startEpoch AND :endEpoch ORDER BY effective_date ASC")
    fun getGoalsInRange(startEpoch: Long, endEpoch: Long): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goal WHERE effective_date > :epochDay ORDER BY effective_date ASC LIMIT 1")
    fun getFirstGoalAfter(epochDay: Long): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goal WHERE effective_date = :epochDay LIMIT 1")
    fun getGoalOnDate(epochDay: Long): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goal ORDER BY effective_date ASC")
    fun getAllGoals(): Flow<List<DailyGoalEntity>>

    @Query("DELETE FROM daily_goal")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(entity: DailyGoalEntity)

    @Update
    suspend fun update(entity: DailyGoalEntity)

    @Delete
    suspend fun delete(entity: DailyGoalEntity)
}
