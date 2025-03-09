package org.numpol.podotaro.taro.presentation.fortune_result

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

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
