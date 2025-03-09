package org.numpol.podotaro.taro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import podotaro.composeapp.generated.resources.Res
import podotaro.composeapp.generated.resources.card_back_01
import podotaro.composeapp.generated.resources.death
import podotaro.composeapp.generated.resources.judgment
import podotaro.composeapp.generated.resources.justice
import podotaro.composeapp.generated.resources.strength
import podotaro.composeapp.generated.resources.temperance
import podotaro.composeapp.generated.resources.the_chariot
import podotaro.composeapp.generated.resources.the_devil
import podotaro.composeapp.generated.resources.the_emperor
import podotaro.composeapp.generated.resources.the_empress
import podotaro.composeapp.generated.resources.the_fool
import podotaro.composeapp.generated.resources.the_hanged_man
import podotaro.composeapp.generated.resources.the_hermit
import podotaro.composeapp.generated.resources.the_hierophant
import podotaro.composeapp.generated.resources.the_high_priestess
import podotaro.composeapp.generated.resources.the_lovers
import podotaro.composeapp.generated.resources.the_magician
import podotaro.composeapp.generated.resources.the_moon
import podotaro.composeapp.generated.resources.the_star
import podotaro.composeapp.generated.resources.the_sun
import podotaro.composeapp.generated.resources.the_tower
import podotaro.composeapp.generated.resources.the_world
import podotaro.composeapp.generated.resources.wheel_of_fortune

const val debugMode = true

// -------------------------------------------------------------------------------------
// 1) SHUFFLE STEPS
// -------------------------------------------------------------------------------------

enum class ShuffleStep { REVEAL, SHUFFLE, DEAL, REVEAL_SELECTED }

// -------------------------------------------------------------------------------------
// 2) TAROT DATA WITH DRAWABLE RESOURCE
// -------------------------------------------------------------------------------------

data class TarotCard(
    val id: Int,
    val drawable: DrawableResource, // e.g. Res.drawable.icon_the_fool
    val description: String
)

// -------------------------------------------------------------------------------------
// 5) LANGUAGE ENUM
// -------------------------------------------------------------------------------------

enum class AppLanguage { EN, TH }

// -------------------------------------------------------------------------------------
// 6) HISTORY RECORD DATA CLASS (using kotlinx-datetime)
// -------------------------------------------------------------------------------------

data class FortuneRecord(
    val id: Int,
    val timestamp: Instant,
    val cardStates: List<CardState>
)

// -------------------------------------------------------------------------------------
// 7) CARD STATE DATA CLASS
// -------------------------------------------------------------------------------------

data class CardState(
    val card: TarotCard,
    val id: Int,
    var x: Float,
    var y: Float,
    var rotation: Float,
    var faceUp: Boolean,
    var selected: Boolean = false,
    var handIndex: Int? = null,
    var flipAngle: Float = 0f  // 0 = front, 180 = back
)


val majorArcanaCards: List<TarotCard> = listOf(
    TarotCard(1, Res.drawable.the_fool, "The Fool"),
    TarotCard(2, Res.drawable.the_magician, "The Magician"),
    TarotCard(3, Res.drawable.the_high_priestess, "The High Priestess"),
    TarotCard(4, Res.drawable.the_empress, "The Empress"),
    TarotCard(5, Res.drawable.the_emperor, "The Emperor"),
    TarotCard(6, Res.drawable.the_hierophant, "The Hierophant"),
    TarotCard(7, Res.drawable.the_lovers, "The Lovers"),
    TarotCard(8, Res.drawable.the_chariot, "The Chariot"),
    TarotCard(9, Res.drawable.strength, "Strength"),
    TarotCard(10, Res.drawable.the_hermit, "The Hermit"),
    TarotCard(11, Res.drawable.wheel_of_fortune, "Wheel of Fortune"),
    TarotCard(12, Res.drawable.justice, "Justice"),
    TarotCard(13, Res.drawable.the_hanged_man, "The Hanged Man"),
    TarotCard(14, Res.drawable.death, "Death"),
    TarotCard(15, Res.drawable.temperance, "Temperance"),
    TarotCard(16, Res.drawable.the_devil, "The Devil"),
    TarotCard(17, Res.drawable.the_tower, "The Tower"),
    TarotCard(18, Res.drawable.the_star, "The Star"),
    TarotCard(19, Res.drawable.the_moon, "The Moon"),
    TarotCard(20, Res.drawable.the_sun, "The Sun"),
    TarotCard(21, Res.drawable.judgment, "Judgment"),
    TarotCard(22, Res.drawable.the_world, "The World")
)

val allTarotCards: List<TarotCard> = majorArcanaCards

// -------------------------------------------------------------------------------------
// 3) CUSTOM ANIMATION HELPERS
// -------------------------------------------------------------------------------------

suspend fun withFrameMillis(): Long {
    var ms = 0L
    withFrameNanos { nanos -> ms = nanos / 1_000_000 }
    return ms
}

suspend fun animateValue(durationMillis: Long, onUpdate: (progress: Float) -> Unit) {
    val start = withFrameMillis()
    while (true) {
        val now = withFrameMillis()
        val elapsed = (now - start).coerceAtLeast(0)
        val fraction = (elapsed.toFloat() / durationMillis).coerceIn(0f, 1f)
        onUpdate(fraction)
        if (fraction >= 1f) break
    }
}

// -------------------------------------------------------------------------------------
// 4) IMAGE LOADING & DRAW SCALED IMAGE HELPER
// -------------------------------------------------------------------------------------

@Composable
fun loadFrontImage(drawable: DrawableResource): ImageBitmap {
    return imageResource(drawable)
}

@Composable
fun loadBackImage(): ImageBitmap {
    return imageResource(Res.drawable.card_back_01)
}

fun DrawScope.drawScaledImage(
    image: ImageBitmap, x: Float, y: Float, targetWidth: Float, targetHeight: Float
) {
    val imageWidth = image.width.toFloat()
    val imageHeight = image.height.toFloat()
    val scaleX = targetWidth / imageWidth
    val scaleY = targetHeight / imageHeight

    drawIntoCanvas { canvas ->
        scale(scaleX, scaleY, pivot = Offset(x, y)) {
            drawImage(
                image = image,
                topLeft = Offset(x - targetWidth / 2, y - targetHeight / 2)
            )
        }
    }
}

