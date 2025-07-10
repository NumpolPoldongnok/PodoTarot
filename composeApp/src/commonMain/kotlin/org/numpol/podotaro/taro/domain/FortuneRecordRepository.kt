package org.numpol.podotaro.taro.domain

import kotlinx.coroutines.flow.Flow
import org.numpol.podotaro.core.domain.DataError
import org.numpol.podotaro.core.domain.EmptyResult

interface FortuneRecordRepository {

    fun getFortuneRecords(): Flow<List<FortuneRecord>>
    suspend fun upsertRecord(fortuneRecord: FortuneRecord): EmptyResult<DataError.Local>
    suspend fun deleteFromRecords(id: String)
}