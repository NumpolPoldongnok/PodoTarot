package org.numpol.podotaro.taro.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FortuneRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: String,
    val cards: List<String>
)