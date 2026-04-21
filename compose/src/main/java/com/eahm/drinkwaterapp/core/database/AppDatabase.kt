package com.eahm.drinkwaterapp.core.database

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eahm.drinkwaterapp.core.database.converter.InstantConverter
import com.eahm.drinkwaterapp.core.database.converter.LocalDateTimeConverter
import com.eahm.drinkwaterapp.core.database.dao.HistoryDao
import kotlinx.coroutines.Dispatchers

@Database(
    version = 1,
    entities = [
        HistoryEntity::class,
    ],
)
@TypeConverters(
    LocalDateTimeConverter::class,
    InstantConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getHistoryDao(): HistoryDao

    companion object {
        const val DATABASE_NAME = "local_database"
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

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
