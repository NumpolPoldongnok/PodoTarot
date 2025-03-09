package org.numpol.podotaro.taro.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FortuneRecordEntity::class],
    version = 1
)
@TypeConverters(
    StringListTypeConverter::class
)
@ConstructedBy(FortuneRecordDatabaseConstructor::class)
abstract class FortuneRecordDatabase: RoomDatabase() {
    abstract val fortuneRecordDao: FortuneRecordDao

    companion object {
        const val DB_NAME = "fortune_record.db"
    }
}