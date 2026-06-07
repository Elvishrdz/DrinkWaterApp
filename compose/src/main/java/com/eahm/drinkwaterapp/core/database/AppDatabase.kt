package com.eahm.drinkwaterapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eahm.drinkwaterapp.core.database.converter.InstantConverter
import com.eahm.drinkwaterapp.core.database.converter.LocalDateConverter
import com.eahm.drinkwaterapp.core.database.converter.LocalDateTimeConverter
import com.eahm.drinkwaterapp.core.database.dao.DailyGoalDao
import com.eahm.drinkwaterapp.core.database.dao.HistoryDao
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers

@Database(
    version = 3,
    entities = [
        HistoryEntity::class,
        DailyGoalEntity::class,
    ],
)
@TypeConverters(
    LocalDateTimeConverter::class,
    InstantConverter::class,
    LocalDateConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getHistoryDao(): HistoryDao
    abstract fun getDailyGoalDao(): DailyGoalDao

    companion object {
        const val DATABASE_NAME = "local_database"
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_goal` (
                `id` TEXT NOT NULL,
                `effective_date` INTEGER NOT NULL,
                `goal_ml` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

fun getAndroidRoomDatabaseBuilder(
    applicationContext: Context,
): RoomDatabase.Builder<AppDatabase> {
    return Room
        .databaseBuilder(
            context = applicationContext,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME,
        )
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add sync tracking columns to HistoryEntity
        db.execSQL("ALTER TABLE HistoryEntity ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE HistoryEntity ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE HistoryEntity ADD COLUMN deleted_at INTEGER")

        // Add sync tracking columns to daily_goal
        db.execSQL("ALTER TABLE daily_goal ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE daily_goal ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE daily_goal ADD COLUMN deleted_at INTEGER")
    }
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
