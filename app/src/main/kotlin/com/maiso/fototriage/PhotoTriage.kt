package com.maiso.fototriage

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Scale

@Composable
fun PhotoTriage(
    uiState: PhotoTriageUiState,
    onPreviousPhoto: () -> Unit,
    onNextPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Fotos op het apparaat:"
        )
//        Text(
//            modifier = Modifier.fillMaxSize(),
//            text = uiState.photos.toString()
//        )

        Box() {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.photo)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .border(1.dp, Color.Red)
                    .fillMaxSize(),
            )

            Row() {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp) // Set a width for the button container
                        .clickable { onPreviousPhoto() }
                        .border(1.dp, Color.Blue)
                ) {

                }
                Spacer(modifier = Modifier.weight(1.0f))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp) // Set a width for the button container
                        .clickable { onNextPhoto() }
                        .border(1.dp, Color.Blue)
                ) {
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FotoTriageTheme {
        PhotoTriage(PhotoTriageUiState(), {}, {})
    }
}