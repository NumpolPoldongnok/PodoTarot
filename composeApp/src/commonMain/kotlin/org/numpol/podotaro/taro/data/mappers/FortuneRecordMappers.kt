package org.numpol.podotaro.taro.data.mappers

import kotlinx.datetime.Instant
import org.numpol.podotaro.taro.data.database.FortuneRecordEntity
import org.numpol.podotaro.taro.domain.FortuneRecord

fun FortuneRecordEntity.toFortuneRecord(): FortuneRecord {
    val epochMillis = timestamp.toLong()
    val timestamp = Instant.fromEpochMilliseconds(epochMillis)
    return FortuneRecord(
        id = id,
        timestamp = timestamp,
        type = cards.size,
        cards = cards,
    )
}

fun FortuneRecord.toFortuneRecordEntity(): FortuneRecordEntity {
    return FortuneRecordEntity(
        id = id ?: 0,
        timestamp = timestamp.toEpochMilliseconds().toString(),
        cards = cards,
    )
}