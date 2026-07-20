package com.streamflix.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflix.app.presentation.theme.BrandGold
import com.streamflix.app.presentation.theme.BrandRed

/**
 * شارة ترتيب Top 10 - تظهر على بطاقة الفيلم
 * تستخدم gradient ذهبي/أحمر يوحي بالترتيب العالي
 */
@Composable
fun TopRankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BrandGold, BrandRed)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}
