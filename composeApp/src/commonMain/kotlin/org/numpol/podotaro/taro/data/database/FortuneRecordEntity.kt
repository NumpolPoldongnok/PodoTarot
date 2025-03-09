package org.numpol.podotaro.taro.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FortuneRecordEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val timestamp: String,
    val cards: List<String>
)