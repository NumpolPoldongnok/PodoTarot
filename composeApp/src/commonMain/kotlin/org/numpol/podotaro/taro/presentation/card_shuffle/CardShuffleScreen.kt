package org.numpol.podotaro.taro.presentation.card_shuffle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.CardState
import org.numpol.podotaro.taro.domain.FortuneRecord
import org.numpol.podotaro.taro.presentation.components.FullScreenCardView
import org.numpol.podotaro.taro.presentation.components.HeaderAppBar
import org.numpol.podotaro.taro.presentation.ShuffleStep
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.animateValue
import org.numpol.podotaro.taro.presentation.loadBackImage
import org.numpol.podotaro.taro.presentation.loadFrontImage
import org.numpol.podotaro.ui.theme.backgroundDark
import org.numpol.podotaro.ui.theme.backgroundLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Localization support for English and Thai.
object LocalizedStrings {
    fun headerTitle(step: ShuffleStep, language: AppLanguage): String {
        return when (step) {
            ShuffleStep.REVEAL -> when (language) {
                AppLanguage.EN -> "Card Preview"
                AppLanguage.TH -> "ดูการ์ด"
            }
            ShuffleStep.SHUFFLE -> when (language) {
                AppLanguage.EN -> "Shuffling Cards"
                AppLanguage.TH -> "กำลังสับการ์ด"
            }
            ShuffleStep.DEAL -> when (language) {
                AppLanguage.EN -> "Select 1-5 Cards"
                AppLanguage.TH -> "เลือกการ์ด 1-5 ใบ"
            }
            ShuffleStep.REVEAL_SELECTED -> when (language) {
                AppLanguage.EN -> "Cards Revealed"
                AppLanguage.TH -> "เปิดการ์ดแล้ว"
            }
        }
    }

    fun mergeButton(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> "Merge"
        AppLanguage.TH -> "สับการ์ด"
    }

    fun quickFortuneButton(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> "Quick Fortune"
        AppLanguage.TH -> "ดูดวงด่วน"
    }

    fun historyButton(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> "History"
        AppLanguage.TH -> "ประวัติ"
    }

    fun revealSelectedButton(selectedCount: Int, language: AppLanguage): String =
        if (selectedCount > 0) {
            when (language) {
                AppLanguage.EN -> "Reveal Selected"
                AppLanguage.TH -> "เปิดการ์ดที่เลือก"
            }
        } else {
            when (language) {
                AppLanguage.EN -> "Select 1-5 Cards"
                AppLanguage.TH -> "เลือกการ์ด 1-5 ใบ"
            }
        }

    fun seeAllButton(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> "See all"
        AppLanguage.TH -> "ดูทั้งหมด"
    }

    fun restartButton(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> "Restart"
        AppLanguage.TH -> "เริ่มใหม่"
    }
}

@Composable
fun CardShuffleScreen(
    tarotCards: List<TarotCard>,
    onShowHistory: () -> Unit,
    onShowFortune: (FortuneRecord) -> Unit,
    onRestart: () -> Unit,
    currentLanguage: AppLanguage,
    onChangeLanguage: (AppLanguage) -> Unit
) {
    val cardWidth = 150f
    val cardHeight = 220f
    val spacing = 20f

    var showControlButton by remember { mutableStateOf(true) }
    var finalCardScale by remember { mutableStateOf(1f) }
    var revealButtonVisible by remember { mutableStateOf(true) }
    var showPulse by remember { mutableStateOf(false) }
    var pulseProgress by remember { mutableStateOf(0f) }
    var fortuneRecord by remember { mutableStateOf<FortuneRecord?>(null) }

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

    var isProcessing by remember { mutableStateOf(false) }
    var autoFlipped by remember { mutableStateOf(false) }
    var fullScreenCard by remember { mutableStateOf<TarotCard?>(null) }

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
                            fullScreenCard = state.card
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
                            fortuneRecord?.let { onShowFortune(it) }
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

    val headerTitle = LocalizedStrings.headerTitle(currentStep, currentLanguage)

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
                    //colors = listOf(Color(0xFFFFF176), Color(0xFFFFA000))
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        backgroundDark)
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
                cardCanvas(cardStates, cardWidth, cardHeight, frontImages, backImage)
            }
        }

        // Header Bar with language toggle.
        HeaderAppBar(headerTitle, currentLanguage, onChangeLanguage = onChangeLanguage)

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
                        Text(LocalizedStrings.mergeButton(currentLanguage))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                val shuffledIndices = cardStates.indices.shuffled()
                                for ((i, index) in shuffledIndices.withIndex()) {
                                    fullScreenCard = cardStates[index].card
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
                                        type = 1,
                                        timestamp = Clock.System.now(),
                                        cards = listOf(finalCard.id.toString())
                                    )
                                    onShowFortune(record)
                                }
                            }
                        }
                    ) {
                        Text(LocalizedStrings.quickFortuneButton(currentLanguage))
                    }
                    Button(onClick = onShowHistory) {
                        Text(LocalizedStrings.historyButton(currentLanguage))
                    }
                } else if (currentStep == ShuffleStep.DEAL) {
                    val selectedCount = cardStates.count { it.selected }
                    val buttonText = LocalizedStrings.revealSelectedButton(selectedCount, currentLanguage)
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
                                                                updateCard(i) {
                                                                    it.copy(
                                                                        x = newX,
                                                                        y = newY,
                                                                        rotation = 0f,
                                                                        handIndex = index
                                                                    )
                                                                }
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
                                                    val cards = revealedStates.map { it.card.id.toString() }
                                                    val record = FortuneRecord(
                                                        type = cards.size,
                                                        timestamp = Clock.System.now(),
                                                        cards = cards
                                                    )
                                                    fortuneRecord = record
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
                        Button(onClick = {
                            fortuneRecord?.let { onShowFortune(it) }
                        }) {
                            Text(LocalizedStrings.seeAllButton(currentLanguage))
                        }
                        Button(
                            onClick = onRestart
                        ) {
                            Text(
                                text = LocalizedStrings.restartButton(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        // ------------------------------
        // Full Screen Card View Overlay.
        // ------------------------------
        if (fullScreenCard != null) {
            FullScreenCardView(
                tarotCard = fullScreenCard!!,
                cardCount = null,
                currentLanguage = currentLanguage,
                onClick = { fullScreenCard = null }
            )
        }
    }
}
