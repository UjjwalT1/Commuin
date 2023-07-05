package com.cyrax.commuin.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Spinner(
    indicatorSize: Dp = 30.dp,
    circleColors: List<Color> = listOf(
        Color(0xFFFF4081),
        BlueBerry,
        Color(0xFF0A5088),
        Color(0xFF0D77CE),
        Color(0xFF4DAEFD),
        Color(0xFF10E3FD),
        Periwinkle,
        Color(0xFFFF4081)
    ),
    animationDuration: Int = 360
) {

    val infiniteTransition = rememberInfiniteTransition()

    val rotateAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        )
    )

    CircularProgressIndicator(
        modifier = Modifier
            .size(size = indicatorSize)
            .rotate(degrees = rotateAnimation)
            .border(
                width = 4.dp,
                brush = Brush.sweepGradient(circleColors),
                shape = CircleShape
            ),
        progress = 1f,
        strokeWidth = 1.dp,
        color = RainForest.background // Set background color
    )
}