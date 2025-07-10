package org.numpol.podotaro.taro.data.database

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FortuneRecordDatabaseConstructor: RoomDatabaseConstructor<FortuneRecordDatabase> {
    override fun initialize(): FortuneRecordDatabase
}