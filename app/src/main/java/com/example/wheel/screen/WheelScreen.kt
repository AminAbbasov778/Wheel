package com.example.wheel.screen

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wheel.R
import com.example.wheel.util.SoundManager
import com.example.wheel.model.WheelModel
import com.example.wheel.components.BottomSheetContent
import com.example.wheel.components.FixedIndicator
import com.example.wheel.components.ResultDialog
import com.example.wheel.components.SpinButton
import com.example.wheel.components.WheelCanvas
import com.example.wheel.components.WheelItems
import com.example.wheel.util.calculateWinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen() {
    val context = LocalContext.current
    var rotation by remember { mutableStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var isStarted by remember { mutableStateOf(false) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState { true }
    var showResult by remember { mutableStateOf(false) }
    var winnerModel by remember { mutableStateOf<WheelModel?>(null) }
    var skipNextAnimation by remember { mutableStateOf(false) }


    val soundManager = remember { SoundManager(context, R.raw.wheel_tick) }



    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }


    val segments = remember {
        mutableStateListOf(
            WheelModel(null, "Jane", 100, 0),
            WheelModel(null, "Jack", 50, 1),
            WheelModel(null, "James", 50, 2),
            WheelModel(null, "Brown", 50, 3),
            WheelModel(null, "Anna", 50, 4),
            WheelModel(null, "Michel", 50, 5),
        )
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = if (skipNextAnimation) {
            snap()
        } else {
            tween(durationMillis = 20000, easing = FastOutSlowInEasing)
        },
        label = "WheelRotation",
        finishedListener = {
            if (!skipNextAnimation) {
                isStarted = false
                winnerModel = segments.getOrNull(selectedIndex)
                showResult = true
            } else {
                skipNextAnimation = false
            }
        }
    )


    LaunchedEffect(segments.size) {
        if (rotation != 0f && !isStarted) {
            skipNextAnimation = true
            rotation = 0f
        }
        if (!isStarted) {
            showResult = false
        }
    }





    LaunchedEffect(animatedRotation) {
        soundManager.playTickIfNeeded(animatedRotation, segments.size)
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0xFF0B1440), RoundedCornerShape(12.dp))
                        .clickable { isBottomSheetVisible = true }
                        .padding(15.dp)
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_edit), // Standart ikon
                        modifier = Modifier.size(20.dp),
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
        }
    ) { paddingValues ->
        paddingValues


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black,
                            Color(0xFF030714),
                            Color(0xFF090F2F),
                            Color(0xFF0B1440),
                            Color.Black
                        )
                    )
                )
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenWidth = constraints.maxWidth.toFloat()
                val screenHeight = constraints.maxHeight.toFloat()

                val centerX = screenWidth / 2
                val centerY = screenHeight / 2
                val outerRadius = screenWidth / 2.3f
                val innerRadius = screenWidth / 4f

                val strokeWidth = outerRadius * 0.06f
                val indicatorSize = outerRadius * 0.1f
                val spinBtnSize = (screenWidth * 0.25f).dp

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedRotation)
                ) {
                    WheelCanvas(
                        centerX,
                        centerY,
                        outerRadius,
                        innerRadius,
                        segments.size,
                        strokeWidth
                    )
                    WheelItems(segments, centerX, centerY, outerRadius, innerRadius)
                }

                FixedIndicator(
                    centerX,
                    centerY,
                    outerRadius,
                    innerRadius,
                    segments.size,
                    strokeWidth,
                    indicatorSize
                )

                SpinButton(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(spinBtnSize),
                    onClick = {
                        if (!isStarted && segments.isNotEmpty()) {
                            isStarted = true
                            selectedIndex = calculateWinner(segments)
                            Log.d("selectedIndex", selectedIndex.toString())
                            val anglePerSegment = 360f / segments.size
                            val targetAngle = (selectedIndex + 1) * anglePerSegment
                            val currentMod = rotation % 360f
                            var angularDistance = targetAngle - currentMod
                            if (angularDistance <= 0) angularDistance += 360f

                            Log.d("rotation_before", rotation.toString())

                            rotation += (angularDistance + (360f * 8))

                            Log.d("rotation_after", rotation.toString())
                        }
                    },
                    density = LocalDensity.current,

                )
            }
        }

        if (isBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isBottomSheetVisible = false },
                sheetState = sheetState,
                containerColor = Color(0xFF030714),
            ) {
                BottomSheetContent(
                    segments = segments,
                    onAdd = { segments.add(it) },
                    onEdit = { newList ->
                        segments.clear()
                        segments.addAll(newList)
                    }, onRemove = { newList ->
                        segments.clear()
                        segments.addAll(newList)
                    }
                )
            }
        }
    }


    ResultDialog(
        model = winnerModel,
        isVisible = showResult,
        onDismiss = { showResult = false }
    )
}