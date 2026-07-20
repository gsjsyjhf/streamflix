package com.streamflix.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflix.app.presentation.theme.BrandPurple

/**
 * عنوان قسم محسن - مع شريط جانبي ملون + زر "عرض الكل" اختياري
 * @param title عنوان القسم
 * @param emoji رمز تعبيري يظهر قبل العنوان (اختياري)
 * @param accentColor لون الشريط الجانبي
 * @param onSeeAll callback لزر "عرض الكل"
 */
@Composable
fun SectionHeader(
    title: String,
    emoji: String? = null,
    accentColor: Color = BrandPurple,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Accent bar (colored side strip)
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        AnimatedVisibility(
            visible = onSeeAll != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (onSeeAll != null) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عرض الكل",
                        color = BrandPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}
