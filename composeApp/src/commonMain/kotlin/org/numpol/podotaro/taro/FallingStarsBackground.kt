package org.numpol.podotaro.taro

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
