import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.numpol.podotaro.taro.AppLanguage
import org.numpol.podotaro.taro.CardState
import org.numpol.podotaro.taro.FortunePage
import org.numpol.podotaro.taro.FullScreenCardView
import kotlin.math.abs

@Composable
fun FortuneResultScreen(
    cardStates: List<CardState>,
    language: AppLanguage,
    onRestart: () -> Unit
) {
    var localLanguage by remember { mutableStateOf(language) }
    var fullScreenCard by remember { mutableStateOf<CardState?>(null) }
    // Pager state with total pages equal to the number of cards.
    val pagerState = rememberPagerState(initialPage = 0) { cardStates.size }
    val coroutineScope = rememberCoroutineScope()

    // Define spread meanings based on language and card count.
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
    // Decide button text based on current page.
    val isLastPage = pagerState.currentPage == cardStates.size - 1
    val buttonText = if (isLastPage) {
        if (localLanguage == AppLanguage.EN) "Restart" else "เริ่มใหม่"
    } else {
        if (localLanguage == AppLanguage.EN) "Next" else "ถัดไป"
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(Color.Black)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            //verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Header: centered title "Your Fortune" with language toggle on the top-right.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Your Fortune",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Text(
                        text = if (localLanguage == AppLanguage.EN) "EN" else "TH",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                localLanguage = if (localLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN
                            }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                // HorizontalPager wrapped in LazyColumn item.
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    // Set horizontal padding so that adjacent cards are visible.
                    contentPadding = PaddingValues(horizontal = 60.dp),
                    pageSpacing = 0.dp
                ) { page ->
                    val cardState = cardStates[page]
                    // Compute page offset for any scaling/transition effects.
                    val pageOffset = abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    FortunePage(
                        page = page,
                        cardState = cardState,
                        pageOffset = pageOffset,
                        spreadMeaning = spreadMeanings.getOrElse(page) { "" },
                        onClickCard = { fullScreenCard = cardState },
                        localLanguage = localLanguage
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
                    Text(buttonText)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        // Full-screen overlay: show the card image in full screen when tapped.
        if (fullScreenCard != null) {
            FullScreenCardView(fullScreenCard!!, onClick = {
                fullScreenCard = null
            })
        }
    }
}
