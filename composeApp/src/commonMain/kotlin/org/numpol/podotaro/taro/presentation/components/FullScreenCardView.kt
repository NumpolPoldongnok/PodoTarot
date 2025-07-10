package org.numpol.podotaro.taro.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.numpol.podotaro.taro.domain.getCardDetailsEnglish
import org.numpol.podotaro.taro.domain.getCardDetailsThai
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.loadFrontImage

@Composable
fun FullScreenCardView(
    tarotCard: TarotCard,
    cardCount: Int?,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val description: String = getCardDescription(cardCount, currentLanguage, tarotCard)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Using a black scrim is common for modal overlays.
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                bitmap = loadFrontImage(tarotCard.drawable),
                contentDescription = tarotCard.description,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Use a themed container color for the description background.
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .padding(16.dp)
            ) {
                Text(
                    text = description,
                    // Use MaterialTheme typography for consistent styling.
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun getCardDescription(
    cardCount: Int?,
    currentLanguage: AppLanguage,
    tarotCard: TarotCard
): String {
    return if (cardCount != null) {
        val details = if (currentLanguage == AppLanguage.EN)
            getCardDetailsEnglish(tarotCard)
        else
            getCardDetailsThai(tarotCard)
        details[cardCount]
    } else {
        tarotCard.description
    }
}
