package com.example.wheel.components

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpinButton(
    modifier: Modifier,
    onClick: () -> Unit,
    density: Density,
) {


    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "")


    Box(

        modifier = modifier

            .size(120.dp)

            .scale(scale)

            .pointerInteropFilter {

                when (it.action) {

                    MotionEvent.ACTION_DOWN -> pressed = true

                    MotionEvent.ACTION_UP -> {

                        pressed = false; onClick()

                    }


                    MotionEvent.ACTION_CANCEL -> pressed = false

                }

                true

            }, contentAlignment = Alignment.Center

    ) {
        var fontSize by remember { mutableStateOf(10.sp) }

        Canvas(modifier = Modifier.fillMaxSize()) {
          fontSize = with(density) { (size.minDimension * 0.06f).toSp() }

            val baseColor = Color(0xFF6EE7FF)
            val opacities = listOf(
                 0.6f, 0.6f, 0.5f,
                0.5f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.09f, 0.08f,
                0.07f, 0.06f, 0.05f, 0.04f, 0.038f, 0.035f, 0.035f, 0.035f, 0.049f, 0.0495f,
                0.006f, 0.003f
            )
            val gradientColors = opacities.map { baseColor.copy(it) }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = gradientColors
                ),
                radius = size.minDimension * 0.2f
            )

            drawCircle(Color(0xFF0F1220), radius = size.minDimension * 0.1f)



            drawCircle(

                Brush.linearGradient(listOf(Color(0xFF6EE7FF), Color(0xFF4D7CFF))),

                radius = size.minDimension * 0.2f,

                style = Stroke(8f)

            )


        }

        Text("SPIN", fontSize = fontSize, color = Color.White, fontFamily = FontFamily.Serif)

    }

}
