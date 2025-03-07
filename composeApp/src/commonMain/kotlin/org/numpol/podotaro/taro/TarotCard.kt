package org.numpol.podotaro.taro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import podotaro.composeapp.generated.resources.Res
import podotaro.composeapp.generated.resources.card_back
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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

// -------------------------------------------------------------------------------------
// 5) LANGUAGE ENUM
// -------------------------------------------------------------------------------------

enum class AppLanguage { EN, TH }

// -------------------------------------------------------------------------------------
// 6) COMPOSABLE: CardShuffleScreen
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

@Composable
fun CardShuffleScreen(
    tarotCards: List<TarotCard> = allTarotCards
) {
    val cardWidth = 150f
    val cardHeight = 220f
    val spacing = 20f

    // In-app language state.
    var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }

    // Control button visibility.
    var showControlButton by remember { mutableStateOf(true) }

    // Additional state for special scaling effect on final card.
    var finalCardScale by remember { mutableStateOf(1f) }

    // Initial grid layout – cards start with their back showing.
    var cardStates by remember {
        mutableStateOf(
            tarotCards.mapIndexed { index, card ->
                val columns = 5
                val col = index % columns
                val row = index / columns
                CardState(
                    card = card,
                    id = index,
                    x = col * (cardWidth + spacing),
                    y = row * (cardHeight + spacing),
                    rotation = 0f,
                    faceUp = false,
                    flipAngle = 180f
                )
            }
        )
    }

    // State machine.
    var currentStep by remember { mutableStateOf(ShuffleStep.REVEAL) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }
    var cameraScale by remember { mutableStateOf(2f) } // initial value; will be animated based on state
    val scope = rememberCoroutineScope()

    var showFortuneScreen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Flag to ensure auto-flip runs only once per REVEAL state.
    var autoFlipped by remember { mutableStateOf(false) }

    // Full-screen card state for individual card view.
    var fullScreenCard by remember { mutableStateOf<CardState?>(null) }

    // Pre-calculate images.
    val frontImages = tarotCards.associate { card ->
        card.id to loadFrontImage(card.drawable)
    }
    val backImage = loadBackImage()

    fun updateCard(index: Int, transform: (CardState) -> CardState) {
        val list = cardStates.toMutableList()
        list[index] = transform(list[index])
        cardStates = list
    }

    // Compute hand slot for a selected card.
    fun computeHandSlot(
        handIndex: Int,
        canvasSize: Size,
        cardWidth: Float,
        cardHeight: Float,
        spacing: Float
    ): Offset {
        val handY = if (canvasSize.height != 0f) canvasSize.height * 0.8f - cardHeight - 20f else 200f
        val startX = canvasSize.width * 0.2f
        val targetX = startX + handIndex * (cardWidth - spacing)
        return Offset(targetX, handY)
    }

    // Helper for converting pointer offsets.
    fun convertPointerOffset(offset: Offset): Offset {
        val pivot = Offset(canvasSize.width / 2, canvasSize.height / 2)
        return pivot + (offset - cameraOffset - pivot) / cameraScale
    }

    // Base pointer input for camera dragging.
    var pointerModifier = Modifier.pointerInput(currentStep) {
        detectDragGestures { change, dragAmount ->
            cameraOffset += dragAmount
            change.consume()
        }
    }

    // In REVEAL state: tap on a face‑up card to view it full‑screen.
    if (currentStep == ShuffleStep.REVEAL) {
        pointerModifier = pointerModifier.then(
            Modifier.pointerInput(currentStep) {
                detectTapGestures { offset ->
                    val transformedOffset = convertPointerOffset(offset)
                    for (i in cardStates.indices.reversed()) {
                        val state = cardStates[i]
                        if (transformedOffset.x in state.x..(state.x + cardWidth) &&
                            transformedOffset.y in state.y..(state.y + cardHeight) &&
                            state.faceUp
                        ) {
                            fullScreenCard = state
                            break
                        }
                    }
                }
            }
        )
    }

    // In DEAL state: tap on a card to select it (max 5 selections).
    if (currentStep == ShuffleStep.DEAL) {
        pointerModifier = pointerModifier.then(
            Modifier.pointerInput(currentStep) {
                detectTapGestures { offset ->
                    val transformedOffset = convertPointerOffset(offset)
                    for (i in cardStates.indices.reversed()) {
                        val state = cardStates[i]
                        if (transformedOffset.x in state.x..(state.x + cardWidth) &&
                            transformedOffset.y in state.y..(state.y + cardHeight)
                        ) {
                            if (!state.selected) {
                                val currentSelectedCount = cardStates.count { it.selected }
                                if (currentSelectedCount < 5) {
                                    val target = computeHandSlot(
                                        currentSelectedCount,
                                        canvasSize,
                                        cardWidth,
                                        cardHeight,
                                        spacing
                                    )
                                    scope.launch {
                                        val startX = state.x
                                        val startY = state.y
                                        animateValue(500) { progress ->
                                            val newX = startX + (target.x - startX) * progress
                                            val newY = startY + (target.y - startY) * progress
                                            updateCard(i) {
                                                it.copy(
                                                    x = newX,
                                                    y = newY,
                                                    handIndex = currentSelectedCount,
                                                    rotation = 0f
                                                )
                                            }
                                        }
                                    }
                                    updateCard(i) { it.copy(selected = true) }
                                }
                            }
                            break
                        }
                    }
                }
            }
        )
    }

    // In REVEAL_SELECTED state: tap on a revealed card to open FortuneResultScreen.
    if (currentStep == ShuffleStep.REVEAL_SELECTED) {
        pointerModifier = pointerModifier.then(
            Modifier.pointerInput(currentStep) {
                detectTapGestures { offset ->
                    val transformedOffset = convertPointerOffset(offset)
                    for (i in cardStates.indices.reversed()) {
                        val state = cardStates[i]
                        if (transformedOffset.x in state.x..(state.x + cardWidth) &&
                            transformedOffset.y in state.y..(state.y + cardHeight) &&
                            state.faceUp
                        ) {
                            fullScreenCard = null
                            showFortuneScreen = true
                            break
                        }
                    }
                }
            }
        )
    }

    // Animate camera reset on state changes.
    LaunchedEffect(currentStep) {
        val startOffset = cameraOffset
        val startScale = cameraScale
        val targetScale = when (currentStep) {
            ShuffleStep.REVEAL -> 1f
            ShuffleStep.SHUFFLE -> 1.5f
            else -> 2f
        }
        animateValue(500) { progress ->
            cameraOffset = Offset(
                x = startOffset.x * (1 - progress),
                y = startOffset.y * (1 - progress)
            )
            cameraScale = startScale + (targetScale - startScale) * progress
        }
    }

    // Once canvas size is known and in REVEAL state, re-center the grid.
    LaunchedEffect(canvasSize) {
        if (canvasSize != Size.Zero && currentStep == ShuffleStep.REVEAL) {
            val columns = 5
            val rows = (tarotCards.size + columns - 1) / columns
            val gridWidth = columns * cardWidth + (columns - 1) * spacing
            val gridHeight = rows * cardHeight + (rows - 1) * spacing
            val offsetX = (canvasSize.width - gridWidth) / 2
            val offsetY = (canvasSize.height - gridHeight) / 2
            cardStates = tarotCards.mapIndexed { i, card ->
                val col = i % columns
                val row = i / columns
                CardState(
                    card = card,
                    id = i,
                    x = offsetX + col * (cardWidth + spacing),
                    y = offsetY + row * (cardHeight + spacing),
                    rotation = 0f,
                    faceUp = false,
                    flipAngle = 180f
                )
            }
        }
    }

    // Sequential auto-flip in REVEAL state.
    LaunchedEffect(currentStep) {
        if (currentStep == ShuffleStep.REVEAL && !autoFlipped) {
            delay(500)
            val jobs = mutableListOf<Job>()
            for (i in cardStates.indices) {
                val job = launch {
                    animateValue(500) { progress ->
                        val angle = 180f - progress * 180f
                        updateCard(i) { it.copy(flipAngle = angle) }
                        if (progress >= 0.5f) {
                            updateCard(i) { it.copy(faceUp = true) }
                        }
                    }
                }
                jobs.add(job)
                delay(25)
            }
            jobs.forEach { it.join() }
            autoFlipped = true
        } else if (currentStep != ShuffleStep.REVEAL) {
            autoFlipped = false
        }
    }

    // Auto-run shuffle animation when in SHUFFLE state.
    LaunchedEffect(currentStep) {
        if (currentStep == ShuffleStep.SHUFFLE) {
            showControlButton = false
            val centerX = canvasSize.width / 2 - cardWidth / 2
            val centerY = canvasSize.height / 2 - cardHeight / 2
            repeat(10) {
                val jobs = cardStates.indices.map { i ->
                    launch {
                        val startX = cardStates[i].x
                        val startY = cardStates[i].y
                        val angle = Random.nextDouble(0.0, 2.0 * PI)
                        val radius = 50f
                        val targetX = centerX + (radius * cos(angle)).toFloat()
                        val targetY = centerY + (radius * sin(angle)).toFloat()
                        animateValue(100) { p ->
                            val nx = startX + (targetX - startX) * p
                            val ny = startY + (targetY - startY) * p
                            updateCard(i) { it.copy(x = nx, y = ny) }
                        }
                        animateValue(100) { p ->
                            val nx = targetX + (centerX - targetX) * p
                            val ny = targetY + (centerY - targetY) * p
                            updateCard(i) { it.copy(x = nx, y = ny) }
                        }
                    }
                }
                jobs.forEach { it.join() }
            }
            val circleRadius = 200f
            val circleJobs = cardStates.indices.map { i ->
                launch {
                    val angle = 2.0 * PI * i / cardStates.size
                    val targetX = centerX + (circleRadius * cos(angle)).toFloat()
                    val targetY = centerY + (circleRadius * sin(angle)).toFloat()
                    val targetRotation = (angle * 180.0 / PI).toFloat()
                    val startX = cardStates[i].x
                    val startY = cardStates[i].y
                    val startRot = cardStates[i].rotation
                    animateValue(1000) { p ->
                        val nx = startX + (targetX - startX) * p
                        val ny = startY + (targetY - startY) * p
                        val nr = startRot + (targetRotation - startRot) * p
                        updateCard(i) { it.copy(x = nx, y = ny, rotation = nr) }
                    }
                }
            }
            circleJobs.forEach { it.join() }
            currentStep = ShuffleStep.DEAL
        }
    }

    LaunchedEffect(currentStep) {
        showControlButton = currentStep != ShuffleStep.SHUFFLE
    }

    // Update header title based on state.
    val headerTitle = when (currentStep) {
        ShuffleStep.REVEAL -> "Card Preview"
        ShuffleStep.SHUFFLE -> "Shuffling Cards"
        ShuffleStep.DEAL -> "Select 1-5 Cards"
        ShuffleStep.REVEAL_SELECTED -> "Cards Revealed"
    }

    // --- Alignment Logic ---
    // In DEAL state, the "Reveal Selected" button is centered.
    // In REVEAL_SELECTED state, the "See all" and "Restart" buttons are at the bottom.
    val controlButtonAlignment = when (currentStep) {
        ShuffleStep.DEAL -> Alignment.Center
        ShuffleStep.REVEAL_SELECTED -> Alignment.BottomCenter
        else -> Alignment.BottomCenter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F))
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(pointerModifier)
        ) {
            canvasSize = size
            withTransform({
                translate(left = cameraOffset.x, top = cameraOffset.y)
                scale(scale = cameraScale, pivot = Offset(canvasSize.width / 2, canvasSize.height / 2))
            }) {
                cardStates.forEachIndexed { i, state ->
                    withTransform({
                        translate(left = state.x, top = state.y)
                        rotate(degrees = state.rotation, pivot = Offset(cardWidth / 2, cardHeight / 2))
                    }) {
                        val flipAngle = state.flipAngle
                        val scaleXFactor = abs(cos(flipAngle * PI / 180).toFloat())
                        withTransform({
                            scale(scaleXFactor, 1f, pivot = Offset(cardWidth / 2, cardHeight / 2))
                        }) {
                            val imageToDraw = if (flipAngle < 90f) frontImages[state.card.id]!! else backImage
                            drawScaledImage(
                                image = imageToDraw,
                                x = cardWidth * 0.1f,
                                y = cardHeight * 0.1f,
                                targetWidth = cardWidth,
                                targetHeight = cardHeight
                            )
                            if (state.selected) {
                                drawRect(
                                    color = Color.Yellow,
                                    topLeft = Offset.Zero,
                                    size = Size(cardWidth, cardHeight),
                                    style = Stroke(width = 4f)
                                )
                            } else {
                                drawRect(
                                    color = Color.Green,
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

        // Header Bar with language toggle.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.TopCenter)
                .background(Color.DarkGray)
                .shadow(4.dp)
        ) {
            Text(
                text = headerTitle,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (currentLanguage == AppLanguage.EN) "EN" else "TH",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .clickable {
                        currentLanguage = if (currentLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN
                    },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Control Buttons.
        if (showControlButton) {
            Column(
                modifier = Modifier
                    .align(controlButtonAlignment)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentStep == ShuffleStep.REVEAL) {
                    Button(
                        onClick = {
                            scope.launch {
                                val shuffledIndices = cardStates.indices.shuffled()
                                for ((i, index) in shuffledIndices.withIndex()) {
                                    fullScreenCard = cardStates[index]
                                    delay(150)
                                    if (i == shuffledIndices.lastIndex) {
                                        animateValue(150) { progress ->
                                            finalCardScale = 1f + 0.3f * progress
                                        }
                                        animateValue(150) { progress ->
                                            finalCardScale = 1.3f - 0.3f * progress
                                        }
                                        finalCardScale = 1f
                                    } else {
                                        fullScreenCard = null
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Quick Fortune")
                    }
                    Button(
                        onClick = {
                            if (!isProcessing) {
                                isProcessing = true
                                scope.launch {
                                    if (currentStep == ShuffleStep.REVEAL) {
                                        delay(300)
                                        val flipJobs = cardStates.indices.map { i ->
                                            launch {
                                                animateValue(500) { progress ->
                                                    val angle = progress * 180f
                                                    updateCard(i) { it.copy(flipAngle = angle) }
                                                    if (progress >= 0.5f) {
                                                        updateCard(i) { it.copy(faceUp = false) }
                                                    }
                                                }
                                            }
                                        }
                                        flipJobs.forEach { it.join() }
                                        val centerX = canvasSize.width / 2 - cardWidth / 2
                                        val centerY = canvasSize.height / 2 - cardHeight / 2
                                        val mergeJobs = cardStates.indices.map { i ->
                                            launch {
                                                val startX = cardStates[i].x
                                                val startY = cardStates[i].y
                                                animateValue(500) { progress ->
                                                    val newX = startX + (centerX - startX) * progress
                                                    val newY = startY + (centerY - startY) * progress
                                                    updateCard(i) { it.copy(x = newX, y = newY) }
                                                }
                                            }
                                        }
                                        mergeJobs.forEach { it.join() }
                                        currentStep = ShuffleStep.SHUFFLE
                                    }
                                    isProcessing = false
                                }
                            }
                        }
                    ) {
                        Text("Merge")
                    }
                } else if (currentStep == ShuffleStep.DEAL) {
                    val selectedCount = cardStates.count { it.selected }
                    val buttonText = if (selectedCount > 0) "Reveal Selected" else "Select 1-5 Cards"
                    Button(
                        onClick = {
                            if (!isProcessing) {
                                isProcessing = true
                                scope.launch {
                                    if (cardStates.any { it.selected }) {
                                        val selectedIndices = cardStates.withIndex()
                                            .filter { it.value.selected }
                                            .map { it.index }
                                        val unselectedIndices = cardStates.withIndex()
                                            .filter { !it.value.selected }
                                            .map { it.index }

                                        // Animate unselected cards off-screen by scattering them in random directions.
                                        val offScreenJobs = unselectedIndices.map { i ->
                                            launch {
                                                val startX = cardStates[i].x
                                                val startY = cardStates[i].y
                                                // Random angle in radians.
                                                val angle = Random.nextDouble(0.0, 2.0 * PI)
                                                // Distance to move the card off-screen.
                                                val distance = 400f
                                                val targetX = startX + (distance * cos(angle)).toFloat()
                                                val targetY = startY + (distance * sin(angle)).toFloat()
                                                animateValue(500) { progress ->
                                                    val newX = startX + (targetX - startX) * progress
                                                    val newY = startY + (targetY - startY) * progress
                                                    updateCard(i) { it.copy(x = newX, y = newY) }
                                                }
                                            }
                                        }
                                        offScreenJobs.forEach { it.join() }

                                        // Reposition selected cards.
                                        val selectedCount = selectedIndices.size
                                        val centerX = canvasSize.width / 2 - cardWidth / 2
                                        val centerY = canvasSize.height / 2 - cardHeight / 2
                                        val targetPositions = when (selectedCount) {
                                            1 -> listOf(Offset(centerX, centerY))
                                            2 -> listOf(
                                                Offset(centerX, centerY - cardHeight / 2 - spacing),
                                                Offset(centerX, centerY + cardHeight / 2 + spacing)
                                            )
                                            3 -> listOf(
                                                Offset(centerX, centerY - cardHeight),
                                                Offset(centerX - cardWidth - spacing, centerY + cardHeight / 4),
                                                Offset(centerX + cardWidth + spacing, centerY + cardHeight / 4)
                                            )
                                            4 -> {
                                                val offsetX = cardWidth / 2 + spacing / 2
                                                val offsetY = cardHeight / 2 + spacing / 2
                                                listOf(
                                                    Offset(centerX - offsetX, centerY - offsetY),
                                                    Offset(centerX + offsetX, centerY - offsetY),
                                                    Offset(centerX - offsetX, centerY + offsetY),
                                                    Offset(centerX + offsetX, centerY + offsetY)
                                                )
                                            }
                                            5 -> listOf(
                                                Offset(centerX, centerY),
                                                Offset(centerX, centerY - cardHeight - spacing),
                                                Offset(centerX, centerY + cardHeight + spacing),
                                                Offset(centerX - cardWidth - spacing, centerY),
                                                Offset(centerX + cardWidth + spacing, centerY)
                                            )
                                            else -> emptyList()
                                        }
                                        val repositionJobs = selectedIndices.mapIndexed { index, i ->
                                            launch {
                                                val startX = cardStates[i].x
                                                val startY = cardStates[i].y
                                                val target = targetPositions.getOrElse(index) { Offset(centerX, centerY) }
                                                animateValue(500) { progress ->
                                                    val newX = startX + (target.x - startX) * progress
                                                    val newY = startY + (target.y - startY) * progress
                                                    updateCard(i) { it.copy(x = newX, y = newY, rotation = 0f) }
                                                }
                                            }
                                        }
                                        repositionJobs.forEach { it.join() }

                                        // Flip selected cards from back to front.
                                        val flipJobs = selectedIndices.map { i ->
                                            launch {
                                                animateValue(500) { progress ->
                                                    val angle = 180f - progress * 180f
                                                    updateCard(i) { it.copy(flipAngle = angle) }
                                                }
                                                updateCard(i) { it.copy(faceUp = true) }
                                            }
                                        }
                                        flipJobs.forEach { it.join() }

                                        currentStep = ShuffleStep.REVEAL_SELECTED
                                    }
                                    isProcessing = false
                                }
                            }
                        }
                    ) {
                        Text(buttonText)
                    }
                } else if (currentStep == ShuffleStep.REVEAL_SELECTED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { fullScreenCard = null; showFortuneScreen = true }) {
                            Text("See all")
                        }
                        Button(
                            onClick = {
                                val columns = 5
                                cardStates = tarotCards.mapIndexed { i, card ->
                                    val rows = (tarotCards.size + columns - 1) / columns
                                    val gridWidth = columns * cardWidth + (columns - 1) * spacing
                                    val gridHeight = rows * cardHeight + (rows - 1) * spacing
                                    val offsetX = (canvasSize.width - gridWidth) / 2
                                    val offsetY = (canvasSize.height - gridHeight) / 2
                                    val col = i % columns
                                    val row = i / columns
                                    CardState(
                                        card = card,
                                        id = i,
                                        x = offsetX + col * (cardWidth + spacing),
                                        y = offsetY + row * (cardHeight + spacing),
                                        rotation = 0f,
                                        faceUp = false,
                                        selected = false,
                                        handIndex = null,
                                        flipAngle = 180f
                                    )
                                }
                                currentStep = ShuffleStep.REVEAL
                            }
                        ) {
                            Text("Restart")
                        }
                    }
                }
            }
        }

        // Display FortuneResultScreen overlay if requested.
        if (showFortuneScreen) {
            val revealedCards = cardStates.filter { it.selected && it.handIndex != null }
                .sortedBy { it.handIndex }
            FortuneResultScreen(
                cardStates = revealedCards,
                language = currentLanguage,
                onRestart = {
                    val columns = 5
                    cardStates = tarotCards.mapIndexed { i, card ->
                        val rows = (tarotCards.size + columns - 1) / columns
                        val gridWidth = columns * cardWidth + (columns - 1) * spacing
                        val gridHeight = rows * cardHeight + (rows - 1) * spacing
                        val offsetX = (canvasSize.width - gridWidth) / 2
                        val offsetY = (canvasSize.height - gridHeight) / 2
                        val col = i % columns
                        val row = i / columns
                        CardState(
                            card = card,
                            id = i,
                            x = offsetX + col * (cardWidth + spacing),
                            y = offsetY + row * (cardHeight + spacing),
                            rotation = 0f,
                            faceUp = false,
                            selected = false,
                            handIndex = null,
                            flipAngle = 180f
                        )
                    }
                    currentStep = ShuffleStep.REVEAL
                    showFortuneScreen = false
                }
            )
        }
        // Else show individual full-screen card view if set.
        else if (fullScreenCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { fullScreenCard = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        bitmap = loadFrontImage(fullScreenCard!!.card.drawable),
                        contentDescription = fullScreenCard!!.card.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .scale(finalCardScale)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        val details = if (currentLanguage == AppLanguage.EN)
                            getCardDetailsEnglish(fullScreenCard!!.card)
                        else
                            getCardDetailsThai(fullScreenCard!!.card)
                        Text(
                            text = details.firstOrNull() ?: fullScreenCard!!.card.description,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 7) COMPOSABLE: FortuneResultScreen (with language toggle)
// -------------------------------------------------------------------------------------

@Composable
fun FortuneResultScreen(cardStates: List<CardState>, language: AppLanguage, onRestart: () -> Unit) {
    var localLanguage by remember { mutableStateOf(language) }
    var fullScreenCard by remember { mutableStateOf<CardState?>(null) }
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

    val headerText = if (localLanguage == AppLanguage.EN) "Your Fortune" else "โชคชะตาของคุณ"
    val restartText = if (localLanguage == AppLanguage.EN) "Restart" else "เริ่มใหม่"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (localLanguage == AppLanguage.EN) "EN" else "TH",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        localLanguage = if (localLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(cardStates) { index, state ->
                    val details = if (localLanguage == AppLanguage.EN)
                        getCardDetailsEnglish(state.card)
                    else
                        getCardDetailsThai(state.card)
                    val detailText = details.getOrElse(index) { details.first() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val frontImage = loadFrontImage(state.card.drawable)
                        Image(
                            bitmap = frontImage,
                            contentDescription = state.card.description,
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { fullScreenCard = state }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Position ${index + 1}: ${spreadMeanings.getOrElse(index) { "" }}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Card: ${state.card.description}",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = detailText,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(restartText)
            }
        }
        if (fullScreenCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { fullScreenCard = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val fullImage = loadFrontImage(fullScreenCard!!.card.drawable)
                    Image(
                        bitmap = fullImage,
                        contentDescription = fullScreenCard!!.card.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        val details = if (localLanguage == AppLanguage.EN)
                            getCardDetailsEnglish(fullScreenCard!!.card)
                        else
                            getCardDetailsThai(fullScreenCard!!.card)
                        Text(
                            text = details.firstOrNull() ?: fullScreenCard!!.card.description,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
