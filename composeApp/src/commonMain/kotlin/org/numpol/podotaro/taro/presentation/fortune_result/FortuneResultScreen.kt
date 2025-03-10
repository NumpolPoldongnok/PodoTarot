package org.numpol.podotaro.taro.presentation.fortune_result

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.components.FullScreenCardView
import org.numpol.podotaro.taro.presentation.components.HeaderAppBar
import org.numpol.podotaro.taro.presentation.majorArcanaCards
import kotlin.math.abs

// Helper object to provide localized strings for the fortune result screen.
object FortuneLocalizedStrings {
    fun headerTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> "Your Fortune"
            AppLanguage.TH -> "ดวงชะตาของคุณ"
        }
    }

    fun nextButton(language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> "Next"
            AppLanguage.TH -> "ถัดไป"
        }
    }

    fun restartButton(language: AppLanguage): String {
        return when (language) {
            AppLanguage.EN -> "Restart"
            AppLanguage.TH -> "เริ่มใหม่"
        }
    }

    fun spreadMeanings(language: AppLanguage, cardCount: Int): List<String> {
        return if (language == AppLanguage.EN) {
            when (cardCount) {
                1 -> listOf("Overall daily fortune")
                2 -> listOf("Your journey ahead", "Your health and vitality")
                3 -> listOf("Travel and adventure", "Health and well-being", "Career and work")
                4 -> listOf("Travel and adventure", "Health and well-being", "Career and work", "Relationships and love")
                5 -> listOf("Past influences", "Present situation", "Future outlook", "Advice", "Outcome")
                else -> emptyList()
            }
        } else {
            when (cardCount) {
                1 -> listOf("โชคชะตารายวันโดยรวม")
                2 -> listOf("เส้นทางข้างหน้า", "สุขภาพและความมีชีวิตชีวา")
                3 -> listOf("การเดินทางและการผจญภัย", "สุขภาพและความเป็นอยู่ที่ดี", "อาชีพและการทำงาน")
                4 -> listOf("การเดินทางและการผจญภัย", "สุขภาพและความเป็นอยู่ที่ดี", "อาชีพและการทำงาน", "ความสัมพันธ์และความรัก")
                5 -> listOf("อิทธิพลในอดีต", "สถานการณ์ปัจจุบัน", "แนวโน้มในอนาคต", "คำแนะนำ", "ผลลัพธ์")
                else -> emptyList()
            }
        }
    }
}

@Composable
fun FortuneResultScreen(
    cardIds: List<String>,
    currentLanguage: AppLanguage,
    onChangeLanguage: (AppLanguage) -> Unit,
    onRestart: () -> Unit,
    tarotCards: List<TarotCard> = majorArcanaCards
) {
    var fullScreenCard by remember { mutableStateOf<TarotCard?>(null) }
    // Pager state with total pages equal to the number of cards.
    val pagerState = rememberPagerState(initialPage = 0) { cardIds.size }
    val coroutineScope = rememberCoroutineScope()

    // Use the localized spread meanings.
    val spreadMeanings = FortuneLocalizedStrings.spreadMeanings(currentLanguage, cardIds.size)

    // Decide the bottom button text based on the current page.
    val isLastPage = pagerState.currentPage == cardIds.size - 1
    val buttonText = if (isLastPage) {
        FortuneLocalizedStrings.restartButton(currentLanguage)
    } else {
        FortuneLocalizedStrings.nextButton(currentLanguage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background falling stars (implementation assumed to exist).
        FallingStarsBackground(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(48.dp)) }
            item {
                // HorizontalPager wrapped in LazyColumn item.
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    // Set horizontal padding so that adjacent cards are visible.
                    contentPadding = PaddingValues(horizontal = 60.dp),
                    pageSpacing = 0.dp
                ) { page ->
                    val cardId = cardIds[page]
                    // Compute page offset for scaling/transition effects.
                    val pageOffset = abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    val tarotCard = tarotCards.first { it.id.toString() == cardId }
                    FortunePage(
                        page = page,
                        tarotCard = tarotCard,
                        pageOffset = pageOffset,
                        spreadMeaning = spreadMeanings.getOrElse(page) { "" },
                        onClickCard = { fullScreenCard = tarotCard },
                        localLanguage = currentLanguage
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                // Bottom button: "Next" if not last card, "Restart" if last.
                Button(
                    onClick = {
                        if (!isLastPage) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onRestart()
                        }
                    }
                ) {
                    Text(buttonText, fontSize = 18.sp)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        // Localized header title.
        val headerTitle = FortuneLocalizedStrings.headerTitle(currentLanguage)
        HeaderAppBar(
            title = headerTitle,
            currentLanguage = currentLanguage,
            onChangeLanguage = onChangeLanguage
        )

        // Full-screen overlay: show the card image in full screen when tapped.
        if (fullScreenCard != null) {
            FullScreenCardView(
                tarotCard = fullScreenCard!!,
                cardCount = cardIds.size,
                currentLanguage = currentLanguage,
                onClick = { fullScreenCard = null }
            )
        }
    }
}
