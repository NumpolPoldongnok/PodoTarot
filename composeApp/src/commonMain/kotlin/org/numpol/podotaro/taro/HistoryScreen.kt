package org.numpol.podotaro.taro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.imageResource

@Composable
fun HistoryScreen(
    history: List<FortuneRecord>,
    onSelect: (FortuneRecord) -> Unit,
    onClose: () -> Unit
) {
    // Group the history records by date ("yyyy-MM-dd").
    val groupedHistory = history.groupBy { record ->
        val localDateTime = record.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }
    // Sort the dates in descending order.
    val sortedDates = groupedHistory.keys.sortedDescending()

    // Remember a map of expansion states for each date section.
    val sectionExpanded = remember { mutableStateMapOf<String, Boolean>() }
    // Initialize each section as expanded if not set.
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White)
        ) {
            Text(
                text = "Fortune History",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn {
                sortedDates.forEach { date ->
                    // Section header with toggle on click.
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .clickable { sectionExpanded[date] = !(sectionExpanded[date] ?: true) }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = date,
                                fontSize = 16.sp,
                                color = Color.Blue
                            )
                        }
                    }
                    // Only display the records if the section is expanded.
                    if (sectionExpanded[date] == true) {
                        itemsIndexed(groupedHistory[date]!!) { index, record ->
                            // Alternate row background colors.
                            val rowColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color.White
                            // Format the time as HH:mm.
                            val localDateTime = record.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                            val formattedTime = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                            // Count of cards in this record.
                            val cardCount = record.cardStates.size

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor)
                                    .clickable { onSelect(record) }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Display the time.
                                    Text(
                                        text = formattedTime,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    // Display the number of cards.
                                    Text(
                                        text = "$cardCount card${if (cardCount > 1) "s" else ""}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    // Display card icons.
                                    record.cardStates.forEach { cardState ->
                                        val imageBitmap = imageResource(cardState.card.drawable)
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = cardState.card.description,
                                            modifier = Modifier.size(48.dp).padding(end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = onClose,
                modifier = Modifier.align(Alignment.End).padding(8.dp)
            ) {
                Text("Close")
            }
        }
    }
}
