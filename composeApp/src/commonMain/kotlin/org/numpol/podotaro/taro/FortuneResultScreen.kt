import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import org.numpol.podotaro.taro.AppLanguage
import org.numpol.podotaro.taro.CardState
import org.numpol.podotaro.taro.FortunePage
import org.numpol.podotaro.taro.FullScreenCardView
import org.numpol.podotaro.taro.HeaderAppBar

// FallingStarsBackground: draws a canvas with animated falling stars.
@Composable
fun FallingStarsBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 50,
    starColor: Color = Color.White,
    starRadius: Float = 3f,
    animationDuration: Int = 5000
) {
    // Define a simple data class for a star.
    data class Star(val x: Float, val y: Float, val speed: Float)

    // Generate a list of random stars once.
    val stars = remember {
        List(starCount) {
            Star(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                speed = (1..3).random() / 100f
            )
        }
    }
    // Animate a progress value from 0f to 1f in an infinite loop.
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing)
        )
    )
    // Draw the stars using Canvas.
    Canvas(modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f))) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        stars.forEach { star ->
            // Each star falls vertically; when reaching the bottom it wraps around.
            val yPos = ((star.y + progress * star.speed) % 1f) * canvasHeight
            val xPos = star.x * canvasWidth
            drawCircle(color = starColor, radius = starRadius, center = Offset(xPos, yPos))
        }
    }
}

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Falling stars animation in the background.
        FallingStarsBackground(modifier = Modifier.fillMaxSize())

        HeaderAppBar("Your Fortune",
            currentLanguage = localLanguage,
            onChangeLanguage = { localLanguage = it})
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
                    Text(buttonText, fontSize = 18.sp)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        // Full-screen overlay: show the card image in full screen when tapped.
        if (fullScreenCard != null) {
            FullScreenCardView(fullScreenCard!!,
                currentLanguage = localLanguage,
                onClick = {
                fullScreenCard = null
            })
        }
    }
}
