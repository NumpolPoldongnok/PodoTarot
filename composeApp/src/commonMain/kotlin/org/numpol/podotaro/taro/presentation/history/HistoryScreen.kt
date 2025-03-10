package org.numpol.podotaro.taro.presentation.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.imageResource
import org.numpol.podotaro.taro.domain.FortuneRecord
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.components.HeaderAppBar
import org.numpol.podotaro.taro.presentation.majorArcanaCards

@Composable
fun HistoryScreen(
    history: List<FortuneRecord>,
    onSelect: (FortuneRecord) -> Unit,
    onClose: () -> Unit,
    currentLanguage: AppLanguage,
    onChangeLanguage: (AppLanguage) -> Unit,
    tarotCards: List<TarotCard> = majorArcanaCards
) {
    fun headerTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> "Fortune History"
            AppLanguage.TH -> "ประวัติดวงชะตา"
        }
    }
    // Group history records by date ("yyyy-MM-dd").
    val groupedHistory = history.groupBy { record ->
        val localDateTime = record.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }
    // Sort the dates in descending order so latest appears on top.
    val sortedDates = groupedHistory.keys.sortedDescending()

    // Remember a map of expansion states for each date section.
    val sectionExpanded = remember { mutableStateMapOf<String, Boolean>() }
    sortedDates.forEach { date ->
        if (sectionExpanded[date] == null) {
            sectionExpanded[date] = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
    ) {
        HeaderAppBar(
            title = headerTitle(language = currentLanguage),
            onClickBack = onClose,
            currentLanguage = currentLanguage,
            onChangeLanguage = onChangeLanguage)

        // Main content container with a white background
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    sortedDates.forEach { date ->
                        // Section header with toggle icon on right.
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray)
                                    .clickable {
                                        sectionExpanded[date] = !(sectionExpanded[date] ?: true)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Section header font now black.
                                Text(
                                    text = date,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            }
                        }
                        // Only display the records if the section is expanded.
                        if (sectionExpanded[date] == true) {
                            val items = groupedHistory[date]!!
                            itemsIndexed(items.sortedByDescending { it.timestamp }) { index, record ->
                                val rowColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color.White
                                val localDateTime = record.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                                val formattedTime = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                                val cardCount = record.cards.size
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowColor)
                                        .clickable { onSelect(record) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Display the time.
                                        Text(
                                            text = formattedTime,
                                            fontSize = 16.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        // Display the number of cards.
                                        Text(
                                            text = "$cardCount card${if (cardCount > 1) "s" else ""}",
                                            fontSize = 16.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        // Display card icons.
                                        record.cards.forEach { id ->
                                            val card = tarotCards.find { it.id.toString() == id }
                                            if (card != null) {
                                                val imageBitmap = imageResource(card.drawable)
                                                Image(
                                                    bitmap = imageBitmap,
                                                    contentDescription = card.description,
                                                    modifier = Modifier.size(48.dp).padding(end = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
