package org.numpol.podotaro.taro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -------------------------------------------------------------------------------------
// 7) COMPOSABLE: FortuneResultScreen (with language toggle)
// -------------------------------------------------------------------------------------

@Composable
fun FortuneResultScreen(cardStates: List<CardState>, language: AppLanguage, onRestart: () -> Unit) {
    var localLanguage by remember { mutableStateOf(language) }
    var fullScreenCard by remember { mutableStateOf<CardState?>(null) }
    val spreadMeanings = if (localLanguage == AppLanguage.EN) {
        when (cardStates.size) {
            1 -> listOf("Overall daily fortune")
            2 -> listOf("Your journey ahead", "Your health and vitality")
            3 -> listOf("Travel and adventure", "Health and well-being", "Career and work")
            4 -> listOf("Travel and adventure", "Health and well-being", "Career and work", "Relationships and love")
            5 -> listOf("Past influences", "Present situation", "Future outlook", "Advice", "Outcome")
            else -> emptyList()
        }
    } else {
        when (cardStates.size) {
            1 -> listOf("โชคชะตารายวันโดยรวม")
            2 -> listOf("เส้นทางข้างหน้า", "สุขภาพและความมีชีวิตชีวา")
            3 -> listOf("การเดินทางและการผจญภัย", "สุขภาพและความเป็นอยู่ที่ดี", "อาชีพและการทำงาน")
            4 -> listOf("การเดินทางและการผจญภัย", "สุขภาพและความเป็นอยู่ที่ดี", "อาชีพและการทำงาน", "ความสัมพันธ์และความรัก")
            5 -> listOf("อิทธิพลในอดีต", "สถานการณ์ปัจจุบัน", "แนวโน้มในอนาคต", "คำแนะนำ", "ผลลัพธ์")
            else -> emptyList()
        }
    }

    val headerText = if (localLanguage == AppLanguage.EN) "Your Fortune" else "โชคชะตาของคุณ"
    val restartText = if (localLanguage == AppLanguage.EN) "Restart" else "เริ่มใหม่"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (localLanguage == AppLanguage.EN) "EN" else "TH",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        localLanguage = if (localLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(cardStates) { index, state ->
                    val details = if (localLanguage == AppLanguage.EN)
                        getCardDetailsEnglish(state.card)
                    else
                        getCardDetailsThai(state.card)
                    val detailText = details.getOrElse(index) { details.first() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val frontImage = loadFrontImage(state.card.drawable)
                        Image(
                            bitmap = frontImage,
                            contentDescription = state.card.description,
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { fullScreenCard = state }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Position ${index + 1}: ${spreadMeanings.getOrElse(index) { "" }}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Card: ${state.card.description}",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = detailText,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(restartText)
            }
        }
        if (fullScreenCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { fullScreenCard = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val fullImage = loadFrontImage(fullScreenCard!!.card.drawable)
                    Image(
                        bitmap = fullImage,
                        contentDescription = fullScreenCard!!.card.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        val details = if (localLanguage == AppLanguage.EN)
                            getCardDetailsEnglish(fullScreenCard!!.card)
                        else
                            getCardDetailsThai(fullScreenCard!!.card)
                        Text(
                            text = details.firstOrNull() ?: fullScreenCard!!.card.description,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
