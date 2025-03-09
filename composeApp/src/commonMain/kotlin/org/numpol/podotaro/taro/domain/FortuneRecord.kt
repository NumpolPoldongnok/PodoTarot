package org.numpol.podotaro.taro.domain

import kotlinx.datetime.Instant

data class FortuneRecord(
    val id: Long? = null,
    val type: Int,
    val timestamp: Instant,
    val cards: List<String>,
)