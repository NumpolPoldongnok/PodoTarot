package org.numpol.podotaro.taro.data.repository

import androidx.sqlite.SQLiteException

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.numpol.podotaro.core.domain.DataError
import org.numpol.podotaro.core.domain.EmptyResult
import org.numpol.podotaro.core.domain.Result
import org.numpol.podotaro.taro.data.database.FortuneRecordDao
import org.numpol.podotaro.taro.data.mappers.toFortuneRecord
import org.numpol.podotaro.taro.data.mappers.toFortuneRecordEntity
import org.numpol.podotaro.taro.domain.FortuneRecordRepository
import org.numpol.podotaro.taro.domain.FortuneRecord

class DefaultFortuneRecordRepository(
    private val fortuneRecordDao: FortuneRecordDao
): FortuneRecordRepository {

    override fun getFortuneRecords(): Flow<List<FortuneRecord>> {
        return fortuneRecordDao
            .getFortuneRecords()
            .map { entity ->
                entity.map { it.toFortuneRecord() }
            }
    }

    override suspend fun upsertRecord(fortuneRecord: FortuneRecord): EmptyResult<DataError.Local> {
        return try {
            fortuneRecordDao.upsert(fortuneRecord.toFortuneRecordEntity())
            Result.Success(Unit)
        } catch(e: SQLiteException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun deleteFromRecords(id: String) {
        fortuneRecordDao.deleteFortuneRecord(id)
    }
}