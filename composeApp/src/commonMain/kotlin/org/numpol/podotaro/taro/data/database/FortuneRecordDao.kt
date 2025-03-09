package org.numpol.podotaro.taro.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FortuneRecordDao {

    @Upsert
    suspend fun upsert(fortuneRecordEntity: FortuneRecordEntity)

    @Query("SELECT * FROM FortuneRecordEntity")
    fun getFortuneRecords(): Flow<List<FortuneRecordEntity>>

    @Query("DELETE FROM FortuneRecordEntity WHERE id = :id")
    suspend fun deleteFortuneRecord(id: String)
}