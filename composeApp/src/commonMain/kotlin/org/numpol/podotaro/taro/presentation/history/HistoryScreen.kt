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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.imageResource
import org.numpol.podotaro.taro.domain.FortuneRecord
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.components.HeaderAppBar
import org.numpol.podotaro.taro.presentation.majorArcanaCards

// Helper function to format a date string ("yyyy-MM-dd") to a full date string.
private fun formatFullDate(dateString: String, language: AppLanguage): String {
    val parts = dateString.split("-")
    if (parts.size != 3) return dateString
    val year = parts[0].toIntOrNull() ?: return dateString
    val month = parts[1].toIntOrNull() ?: return dateString
    val day = parts[2].toIntOrNull() ?: return dateString
    val localDate = LocalDate(year, month, day)
    return when (language) {
        AppLanguage.EN -> {
            val dayNames = mapOf(
                DayOfWeek.MONDAY to "Monday",
                DayOfWeek.TUESDAY to "Tuesday",
                DayOfWeek.WEDNESDAY to "Wednesday",
                DayOfWeek.THURSDAY to "Thursday",
                DayOfWeek.FRIDAY to "Friday",
                DayOfWeek.SATURDAY to "Saturday",
                DayOfWeek.SUNDAY to "Sunday"
            )
            val monthNames = mapOf(
                1 to "January",
                2 to "February",
                3 to "March",
                4 to "April",
                5 to "May",
                6 to "June",
                7 to "July",
                8 to "August",
                9 to "September",
                10 to "October",
                11 to "November",
                12 to "December"
            )
            val weekday = dayNames[localDate.dayOfWeek] ?: localDate.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }
            val monthName = monthNames[month] ?: month.toString()
            "$weekday, $monthName $day, $year"
        }
        AppLanguage.TH -> {
            val dayNames = mapOf(
                DayOfWeek.MONDAY to "จันทร์",
                DayOfWeek.TUESDAY to "อังคาร",
                DayOfWeek.WEDNESDAY to "พุธ",
                DayOfWeek.THURSDAY to "พฤหัสบดี",
                DayOfWeek.FRIDAY to "ศุกร์",
                DayOfWeek.SATURDAY to "เสาร์",
                DayOfWeek.SUNDAY to "อาทิตย์"
            )
            val monthNames = mapOf(
                1 to "มกราคม",
                2 to "กุมภาพันธ์",
                3 to "มีนาคม",
                4 to "เมษายน",
                5 to "พฤษภาคม",
                6 to "มิถุนายน",
                7 to "กรกฎาคม",
                8 to "สิงหาคม",
                9 to "กันยายน",
                10 to "ตุลาคม",
                11 to "พฤศจิกายน",
                12 to "ธันวาคม"
            )
            val weekday = dayNames[localDate.dayOfWeek] ?: localDate.dayOfWeek.toString()
            val monthName = monthNames[month] ?: month.toString()
            "วัน$weekday ที่ $day $monthName $year"
        }
    }
}

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
    // Sort the dates in descending order so the latest appears on top.
    val sortedDates = groupedHistory.keys.sortedDescending()

    // Get current date in the same format.
    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDateString = "${localNow.year}-${localNow.monthNumber.toString().padStart(2, '0')}-${localNow.dayOfMonth.toString().padStart(2, '0')}"

    // Remember a map of expansion states for each date section.
    val sectionExpanded = remember { mutableStateMapOf<String, Boolean>() }
    sortedDates.forEach { date ->
        if (sectionExpanded[date] == null) {
            // Only expand today's section by default.
            sectionExpanded[date] = date == currentDateString
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
    ) {
        HeaderAppBar(
            title = headerTitle(language = currentLanguage),
            onClickBack = onClose,
            currentLanguage = currentLanguage,
            onChangeLanguage = onChangeLanguage
        )

        // Main content container using MaterialTheme colors.
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    sortedDates.forEach { date ->
                        // Prepare a friendly header.
                        val displayDate = if (date == currentDateString) {
                            when (currentLanguage) {
                                AppLanguage.EN -> "Today"
                                AppLanguage.TH -> "วันนี้"
                            }
                        } else {
                            // For non-today sections, display the full date.
                            formatFullDate(date, currentLanguage)
                        }
                        // Section header with toggle icon on right.
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .clickable {
                                        sectionExpanded[date] = !(sectionExpanded[date] ?: false)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayDate,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        // Only display the records if the section is expanded.
                        if (sectionExpanded[date] == true) {
                            val items = groupedHistory[date]!!
                            itemsIndexed(items.sortedByDescending { it.timestamp }) { index, record ->
                                val rowColor = if (index % 2 == 0)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    MaterialTheme.colorScheme.surface
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        // Display the number of cards.
                                        Text(
                                            text = "$cardCount card${if (cardCount > 1) "s" else ""}",
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
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
