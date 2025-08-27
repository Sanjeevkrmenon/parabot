package com.example.parabot

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/* ----------------- Root Screen ----------------- */
@Composable
fun RobotScreen(viewModel: RobotViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Robot Face — bigger background box
        RobotFaceUI(
            viewModel = viewModel,
            modifier = Modifier
                .size(400.dp, 300.dp)  // enlarged for thicker border
        )

        Spacer(Modifier.height(12.dp))

        // Emotion buttons
        EmotionControls(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ----------------- Face Box ----------------- */
@Composable
fun RobotFaceUI(
    viewModel: RobotViewModel,
    modifier: Modifier = Modifier
) {
    val faceState = viewModel.state.collectAsState().value

    val faceShape = remember {
        GenericShape { size, _ ->
            val r = size.minDimension * 0.18f
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset.Zero, size),
                    topLeft = CornerRadius(r, r),
                    topRight = CornerRadius(r, r),
                    bottomRight = CornerRadius(r, r),
                    bottomLeft = CornerRadius(r, r)
                )
            )
        }
    }

    Box(
        modifier = modifier
            .clip(faceShape)
            .background(Color(0xFF0F1116)),
        contentAlignment = Alignment.Center
    ) {
        when (faceState.emotion) {
            RobotEmotion.HAPPY -> LoonaEyes(faceState.isBlinking)
            else -> NeutralOrangeEyes(faceState.isBlinking)
        }
    }
}

/* ----------------- Neutral Eyes ----------------- */
@Composable
fun NeutralOrangeEyes(isBlinking: Boolean) {
    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val aspect = if (portrait) 0.95f else 0.78f
    val spacing = if (portrait) 28.dp else 20.dp

    val colors = listOf(
        Color(0xFFFFF0E0),
        Color(0xFFFFC37A),
        Color(0xFFFFA64F),
        Color(0xFFF27A20)
    )
    val aura = Color(0xFFFFA54F)

    val open by animateFloatAsState(
        targetValue = if (isBlinking) 0f else 0.9f,
        animationSpec = tween(200),
        label = "openNeutral"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)   // smaller inside face
            .fillMaxHeight(0.6f), // shorter so border is thicker
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EyeBase(open, colors, aura, aspect)
        EyeBase(open, colors, aura, aspect)
    }
}

/* ----------------- Loona-style Eyes ----------------- */
@Composable
fun LoonaEyes(isBlinking: Boolean) {
    val aspect = 1.05f
    val eyeSpacing = 36.dp

    val open by animateFloatAsState(
        targetValue = if (isBlinking) 0f else 1f,
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label = "loonaOpen"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .fillMaxHeight(0.6f)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(eyeSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EyeLoona(open, aspect, isLeft = true)
        EyeLoona(open, aspect, isLeft = false)
    }
}

/* ----------------- Loona glossy Eye ----------------- */
@Composable
fun RowScope.EyeLoona(
    openFraction: Float,
    aspect: Float,
    isLeft: Boolean
) {
    val top = Color(0xFFFFF3E6)
    val light = Color(0xFFFFD49A)
    val mid = Color(0xFFFFB460)
    val bottom = Color(0xFFF5892E)

    val inf = rememberInfiniteTransition(label = "idleSway")
    val t by inf.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "sway"
    )
    val swayY = sin(t.toDouble()).toFloat() * 1f

    Canvas(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(aspect)
    ) {
        val w = size.width
        val rawH = size.height * openFraction
        val h = max(rawH, size.height * 0.12f)

        // 👇 shrink more: 0.75f
        val rectW = w * 0.75f
        val rectH = h * 0.75f
        val rectTopLeft = Offset(
            (w - rectW) / 2f,
            (size.height - rectH) / 2f + swayY
        )

        val cr = min(rectW, rectH) * 0.34f
        val rr = RoundRect(Rect(rectTopLeft, Size(rectW, rectH)),
            topLeft = CornerRadius(cr),
            topRight = CornerRadius(cr),
            bottomRight = CornerRadius(cr),
            bottomLeft = CornerRadius(cr)
        )
        val shape = Path().apply { addRoundRect(rr) }
        val centerY = rr.top + rectH / 2f

        clipPath(shape) {
            drawRect(
                brush = Brush.verticalGradient(listOf(top, light, mid, bottom)),
                topLeft = rectTopLeft,
                size = Size(rectW, rectH)
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0x33000000))),
                topLeft = rectTopLeft,
                size = Size(rectW, rectH)
            )
        }

        drawCircle(
            color = Color.White,
            radius = rectW * 0.06f,
            center = Offset(rr.left + rectW * 0.22f, rr.top + rectH * 0.22f)
        )
        drawPath(
            path = shape,
            color = Color.Black.copy(alpha = 0.1f),
            style = Stroke(width = rectW * 0.028f)
        )
    }
}

/* ----------------- Neutral Eye Shape ----------------- */
@Composable
fun RowScope.EyeBase(
    openFraction: Float,
    colors: List<Color>,
    auraColor: Color,
    aspect: Float
) {
    Canvas(
        Modifier
            .weight(1f)
            .aspectRatio(aspect)
    ) {
        // 👇 shrink to 0.75f for thick border
        val w = size.width * 0.75f
        val h = size.height * openFraction * 0.75f
        val cx = size.width / 2
        val cy = size.height / 2
        val topLeft = Offset(cx - w / 2, cy - h / 2)

        drawOval(
            brush = Brush.radialGradient(
                listOf(auraColor.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.9f
            ),
            topLeft = topLeft,
            size = Size(w, h)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(colors),
            topLeft = topLeft,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.25f, h * 0.28f)
        )
    }
}

/* ----------------- Control Buttons ----------------- */
@Composable
fun EmotionControls(
    viewModel: RobotViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .horizontalScroll(rememberScrollState())
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { viewModel.setEmotion(RobotEmotion.HAPPY) }) { Text("Happy") }
        Spacer(Modifier.width(8.dp))
        Button(onClick = { viewModel.setEmotion(RobotEmotion.SAD) }) { Text("Sad") }
        Spacer(Modifier.width(8.dp))
        Button(onClick = { viewModel.setEmotion(RobotEmotion.SURPRISED) }) { Text("Surprised") }
        Spacer(Modifier.width(8.dp))
        Button(onClick = { viewModel.setEmotion(RobotEmotion.SLEEPY) }) { Text("Sleepy") }
        Spacer(Modifier.width(8.dp))
        Button(onClick = { viewModel.setEmotion(RobotEmotion.NEUTRAL) }) { Text("Neutral") }
    }
}