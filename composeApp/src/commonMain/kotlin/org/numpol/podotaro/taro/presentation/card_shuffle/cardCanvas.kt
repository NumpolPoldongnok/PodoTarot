package org.numpol.podotaro.taro.presentation.card_shuffle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import org.numpol.podotaro.taro.presentation.CardState
import org.numpol.podotaro.taro.presentation.debugMode
import org.numpol.podotaro.taro.presentation.drawScaledImage
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

fun DrawScope.cardCanvas(
    cardStates: List<CardState>,
    cardWidth: Float,
    cardHeight: Float,
    frontImages: Map<Int, ImageBitmap>,
    backImage: ImageBitmap
) {
    cardStates.forEachIndexed { i, state ->
        if (state.selected) {
            withTransform({
                translate(left = state.x, top = state.y)
                rotate(degrees = state.rotation, pivot = Offset(cardWidth / 2, cardHeight))
            }) {
                val flipAngle = state.flipAngle
                val scaleXFactor = abs(cos(flipAngle * PI / 180).toFloat())
                withTransform({
                    scale(scaleXFactor, 1f, pivot = Offset(cardWidth / 2, cardHeight / 2))
                }) {
                    val imageToDraw =
                        if (flipAngle < 90f) frontImages[state.card.id]!! else backImage
                    drawScaledImage(
                        image = imageToDraw,
                        x = cardWidth * 0.15f,
                        y = cardHeight * 0.15f,
                        targetWidth = cardWidth,
                        targetHeight = cardHeight
                    )
                    if (debugMode) {
                        drawRect(
                            color = Color.Yellow,
                            topLeft = Offset.Zero,
                            size = Size(cardWidth, cardHeight),
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }
        } else {
            withTransform({
                translate(left = state.x, top = state.y)
                rotate(degrees = state.rotation, pivot = Offset(cardWidth / 2, cardHeight / 2))
            }) {
                val flipAngle = state.flipAngle
                val scaleXFactor = abs(cos(flipAngle * PI / 180).toFloat())
                withTransform({
                    scale(scaleXFactor, 1f, pivot = Offset(cardWidth / 2, cardHeight / 2))
                }) {
                    val imageToDraw =
                        if (flipAngle < 90f) frontImages[state.card.id]!! else backImage
                    drawScaledImage(
                        image = imageToDraw,
                        x = cardWidth * 0.15f,
                        y = cardHeight * 0.15f,
                        targetWidth = cardWidth,
                        targetHeight = cardHeight
                    )
                    if (debugMode) {
                        drawRect(
                            color = Color.Blue,
                            topLeft = Offset.Zero,
                            size = Size(cardWidth, cardHeight),
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }
        }
    }
}
