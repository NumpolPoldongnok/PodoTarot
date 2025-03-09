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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.numpol.podotaro.taro.domain.getCardDetailsEnglish
import org.numpol.podotaro.taro.domain.getCardDetailsThai
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.presentation.CardState
import org.numpol.podotaro.taro.presentation.loadFrontImage

@Composable
fun FullScreenCardView(
    cardState: CardState,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                bitmap = loadFrontImage(cardState.card.drawable),
                contentDescription = cardState.card.description,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    //.graphicsLayer { }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                val details = if (currentLanguage == AppLanguage.EN)
                    getCardDetailsEnglish(cardState.card)
                else
                    getCardDetailsThai(cardState.card)
                Text(
                    text = details.firstOrNull() ?: cardState.card.description,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

