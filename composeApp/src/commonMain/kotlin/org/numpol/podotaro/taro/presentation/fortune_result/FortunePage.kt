package org.numpol.podotaro.taro.presentation.fortune_result

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import org.numpol.podotaro.taro.domain.getCardDetailsEnglish
import org.numpol.podotaro.taro.domain.getCardDetailsThai
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.loadFrontImage

@Composable
fun FortunePage(
    page: Int,
    tarotCard: TarotCard,
    pageOffset: Float,
    spreadMeaning: String,
    onClickCard: () -> Unit,
    localLanguage: AppLanguage,
) {
    fun headerTitle(language: AppLanguage, page: Int): String {
        return when (language) {
            AppLanguage.EN -> "Position: ${page + 1}"
            AppLanguage.TH -> "ตำแหน่งที่: ${page + 1}"
        }
    }

    // Calculate offset for scaling effect.
    Column(
        modifier = Modifier
            .graphicsLayer {
                val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                scaleX = scale
                scaleY = scale
            }
            // Increase the width of each card info area.
            .fillMaxWidth(0.9f)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = headerTitle(language = localLanguage, page = page),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = spreadMeaning,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Card image.
        Image(
            bitmap = loadFrontImage(tarotCard.drawable),
            contentDescription = tarotCard.description,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickCard() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Card title and detail.
        Text(
            text = tarotCard.description,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        val details = if (localLanguage == AppLanguage.EN)
            getCardDetailsEnglish(tarotCard)
        else
            getCardDetailsThai(tarotCard)
        Text(
            text = details.firstOrNull() ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
