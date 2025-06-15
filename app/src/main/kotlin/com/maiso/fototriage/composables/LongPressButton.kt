package com.maiso.fototriage.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LongPressButton(
    onLongPress: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            progress = 0f
            while (progress < 1f) {
                delay(10) // Adjust the delay for smoother progress
                progress += 0.01f // Adjust the increment for speed
            }
            onLongPress()
            isPressed = false
        } else {
            progress = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        // Draw the filling circle
        Canvas(modifier = Modifier.size(52.dp)) {
//            drawCircle(
//                color = Color.Gray,
//                radius = size.minDimension / 2,
//                style = Stroke(width = 8f)
//            )
            drawArc(
                color = Color.Red,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                size = size,
                style = Stroke(width = 8f)
            )
        }

        content()
    }
}

