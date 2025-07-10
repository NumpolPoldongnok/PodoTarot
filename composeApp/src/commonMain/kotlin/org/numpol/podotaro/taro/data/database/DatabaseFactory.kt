package org.numpol.podotaro.taro.data.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<FortuneRecordDatabase>
}