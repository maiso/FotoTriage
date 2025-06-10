package com.maiso.fototriage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthRow(month: String, nrOfFotos: Int, percentageTriaged: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 15.dp)
            .clickable {
                onClick()
            },
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$nrOfFotos fotos ($percentageTriaged%)",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

fun Month.toDutchString(): String {
    return getDisplayName(TextStyle.FULL, Locale.getDefault()) // Dutch locale
}