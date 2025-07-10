package org.numpol.podotaro.taro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.numpol.podotaro.taro.presentation.AppLanguage

@Composable
fun BoxScope.HeaderAppBar(
    title: String,
    currentLanguage: AppLanguage? = null,
    onChangeLanguage: ((AppLanguage) -> Unit)? = null,
    onClickBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .align(Alignment.TopCenter)
            .background(MaterialTheme.colorScheme.primary)
            .shadow(4.dp)
    ) {
        if (onClickBack != null) {
            IconButton(
                onClick = onClickBack,
                colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (currentLanguage != null && onChangeLanguage != null) {
            Text(
                text = if (currentLanguage == AppLanguage.EN) "EN" else "TH",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .clickable {
                        onChangeLanguage(if (currentLanguage == AppLanguage.EN) AppLanguage.TH else AppLanguage.EN)
                    },
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}