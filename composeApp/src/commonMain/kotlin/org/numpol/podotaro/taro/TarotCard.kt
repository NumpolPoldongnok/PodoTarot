package org.numpol.podotaro.taro

import FortuneResultScreen
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val debugMode = false

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

// -------------------------------------------------------------------------------------
// 8) COMPOSABLE: CardShuffleScreen
// -------------------------------------------------------------------------------------

@Composable
fun CardShuffleScreen(
    tarotCards: List<TarotCard> = allTarotCards
) {
    val cardWidth = 150f
    val cardHeight = 220f
    val spacing = 20f

    var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }
    var showControlButton by remember { mutableStateOf(true) }
    var finalCardScale by remember { mutableStateOf(1f) }
    var revealButtonVisible by remember { mutableStateOf(true) }
    var showPulse by remember { mutableStateOf(false) }
    var pulseProgress by remember { mutableStateOf(0f) }

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

    var currentStep by remember { mutableStateOf(ShuffleStep.REVEAL) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }
    var cameraScale by remember { mutableStateOf(2f) }
    val scope = rememberCoroutineScope()

    var showFortuneScreen by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var autoFlipped by remember { mutableStateOf(false) }
    var fullScreenCard by remember { mutableStateOf<CardState?>(null) }

    // ----- HISTORY STATE VARIABLES -----
    val fortuneHistory = remember { mutableStateListOf<FortuneRecord>() }
    var fortuneRecordToShow by remember { mutableStateOf<FortuneRecord?>(null) }
    var showHistoryScreen by remember { mutableStateOf(false) }

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

    fun convertPointerOffset(offset: Offset): Offset {
        val pivot = Offset(canvasSize.width / 2, canvasSize.height / 2)
        return pivot + (offset - cameraOffset - pivot) / cameraScale
    }

    var pointerModifier = Modifier.pointerInput(currentStep) {
        detectDragGestures { change, dragAmount ->
            cameraOffset += dragAmount
            change.consume()
        }
    }

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
                            if (!state.selected && cardStates.count { it.selected } < 5) {
                                updateCard(i) { it.copy(selected = true) }
                                val selectedIndices = cardStates.withIndex()
                                    .filter { it.value.selected }
                                    .map { it.index }
                                val selectedCount = selectedIndices.size
                                val bottomCenterPivot = Offset(canvasSize.width / 2, canvasSize.height * 0.8f)
                                val targetPosition = bottomCenterPivot - Offset(cardWidth / 2, cardHeight)
                                val totalFanAngle = 40f
                                for ((j, idx) in selectedIndices.withIndex()) {
                                    val targetRotation = if (selectedCount == 1) {
                                        0f
                                    } else {
                                        -totalFanAngle / 2 + (totalFanAngle * j / (selectedCount - 1))
                                    }
                                    scope.launch {
                                        val card = cardStates[idx]
                                        val startX = card.x
                                        val startY = card.y
                                        val startRotation = card.rotation
                                        animateValue(500) { progress ->
                                            val newX = startX + (targetPosition.x - startX) * progress
                                            val newY = startY + (targetPosition.y - startY) * progress
                                            val newRotation = startRotation + (targetRotation - startRotation) * progress
                                            updateCard(idx) { it.copy(x = newX, y = newY, rotation = newRotation) }
                                        }
                                    }
                                }
                            }
                            break
                        }
                    }
                }
            }
        )
    }

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

    val headerTitle = when (currentStep) {
        ShuffleStep.REVEAL -> "Card Preview"
        ShuffleStep.SHUFFLE -> "Shuffling Cards"
        ShuffleStep.DEAL -> "Select 1-5 Cards"
        ShuffleStep.REVEAL_SELECTED -> "Cards Revealed"
    }

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
                    colors = listOf(Color(0xFFFFF176), Color(0xFFFFA000))
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
                                val imageToDraw = if (flipAngle < 90f) frontImages[state.card.id]!! else backImage
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
                                val imageToDraw = if (flipAngle < 90f) frontImages[state.card.id]!! else backImage
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
        }

        // Header Bar with language toggle.
        HeaderAppBar(headerTitle, currentLanguage, onChangeLanguage = {
            currentLanguage = it
        })

        // Control Buttons.
        if (showControlButton) {
            Column(
                modifier = Modifier
                    .align(controlButtonAlignment)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStep == ShuffleStep.REVEAL) {
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
                                // Record history for Quick Fortune (1 card)
                                fullScreenCard?.let { finalCard ->
                                    val record = FortuneRecord(
                                        id = fortuneHistory.size,
                                        timestamp = Clock.System.now(),
                                        cardStates = listOf(finalCard.copy())
                                    )
                                    fortuneHistory.add(record)
                                    fortuneRecordToShow = record
                                    showFortuneScreen = true
                                }
                            }
                        }
                    ) {
                        Text("Quick Fortune")
                    }
                    Button(onClick = { showHistoryScreen = true }) {
                        Text("History")
                    }
                } else if (currentStep == ShuffleStep.DEAL) {
                    val selectedCount = cardStates.count { it.selected }
                    val buttonText = if (selectedCount > 0) "Reveal Selected" else "Select 1-5 Cards"
                    if (revealButtonVisible) {
                        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        scope.launch {
                                            if (selectedCount > 0) {
                                                showPulse = true
                                                animateValue(500) { progress ->
                                                    pulseProgress = progress
                                                }
                                                showPulse = false
                                                revealButtonVisible = false

                                                if (cardStates.any { it.selected }) {
                                                    val selectedIndices = cardStates.withIndex()
                                                        .filter { it.value.selected }
                                                        .map { it.index }
                                                    val unselectedIndices = cardStates.withIndex()
                                                        .filter { !it.value.selected }
                                                        .map { it.index }
                                                    val offScreenJobs = unselectedIndices.map { i ->
                                                        launch {
                                                            val startX = cardStates[i].x
                                                            val startY = cardStates[i].y
                                                            val angle = Random.nextDouble(0.0, 2.0 * PI)
                                                            val distance = 800f
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

                                                    val centerX = canvasSize.width / 2 - cardWidth / 2
                                                    val centerY = canvasSize.height / 2 - cardHeight / 2
                                                    val targetPositions = when (selectedIndices.size) {
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
                                                                updateCard(i) { it.copy(x = newX, y = newY, rotation = 0f, handIndex = index) }
                                                            }
                                                        }
                                                    }
                                                    repositionJobs.forEach { it.join() }

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

                                                    // Record history for Card Revealed (multiple cards)
                                                    val revealedStates = cardStates.filter { it.selected }
                                                    val record = FortuneRecord(
                                                        id = fortuneHistory.size,
                                                        timestamp = Clock.System.now(),
                                                        cardStates = revealedStates.map { it.copy() }
                                                    )
                                                    fortuneHistory.add(record)
                                                    fortuneRecordToShow = record

                                                    currentStep = ShuffleStep.REVEAL_SELECTED
                                                }
                                            }
                                            isProcessing = false
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = buttonText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (showPulse) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val baseRadius = size.minDimension / 2
                                    val currentRadius = baseRadius * (1f + pulseProgress * 2f)
                                    drawCircle(
                                        color = Color.White.copy(alpha = (1f - pulseProgress) * 0.5f),
                                        radius = currentRadius,
                                        center = center,
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }
                        }
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
                                revealButtonVisible = true
                                showPulse = false
                                pulseProgress = 0f
                                autoFlipped = false
                            }
                        ) {
                            Text(
                                text = "Restart",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ------------------------------
        // FortuneResultScreen Overlay
        // ------------------------------
        if (showFortuneScreen) {
            val cardStatesToShow = fortuneRecordToShow?.cardStates
                ?: cardStates.filter { it.selected && it.handIndex != null }
                    .sortedBy { it.handIndex }
            FortuneResultScreen(
                cardStates = cardStatesToShow,
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
                    revealButtonVisible = true
                    showPulse = false
                    pulseProgress = 0f
                    autoFlipped = false
                    fortuneRecordToShow = null
                }
            )
        }
        // ------------------------------
        // Full Screen Card View Overlay.
        // ------------------------------
        else if (fullScreenCard != null) {
            FullScreenCardView(
                fullScreenCard!!,
                currentLanguage = currentLanguage,
                onClick = { fullScreenCard = null }
            )
        }

        // ------------------------------
        // History Screen Overlay.
        // ------------------------------
        if (showHistoryScreen) {
            HistoryScreen(
                history = fortuneHistory,
                onSelect = { record ->
                    fortuneRecordToShow = record
                    showFortuneScreen = true
                    showHistoryScreen = false
                },
                onClose = {
                    showHistoryScreen = false
                }
            )
        }
    }
}
