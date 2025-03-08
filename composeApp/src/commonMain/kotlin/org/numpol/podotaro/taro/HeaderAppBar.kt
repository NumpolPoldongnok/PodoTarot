package org.numpol.podotaro.taro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BoxScope.HeaderAppBar(
    headerTitle: String,
    currentLanguage: AppLanguage,
    onChangeLanguage: (AppLanguage) -> Unit
) {
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
                    onChangeLanguage(if (currentLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN)
                },
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}