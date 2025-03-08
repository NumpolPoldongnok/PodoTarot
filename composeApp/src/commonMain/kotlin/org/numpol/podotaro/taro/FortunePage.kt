package org.numpol.podotaro.taro
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun FortunePage(
    page: Int,
    cardState: CardState,
    pageOffset: Float,
    spreadMeaning: String,
    onClickCard: () -> Unit,
    localLanguage: AppLanguage,
) {
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
            text = "Position: ${page + 1}",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = spreadMeaning,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Card image.
        Image(
            bitmap = loadFrontImage(cardState.card.drawable),
            contentDescription = cardState.card.description,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickCard() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Card title and detail.
        Text(
            text = cardState.card.description,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        val details = if (localLanguage == AppLanguage.EN)
            getCardDetailsEnglish(cardState.card)
        else
            getCardDetailsThai(cardState.card)
        Text(
            text = details.firstOrNull() ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
