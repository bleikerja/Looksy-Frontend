package com.example.looksy.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.looksy.ui.components.Header
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.looksy.util.cropAndSaveImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import android.graphics.Rect as AndroidRect
import android.graphics.Bitmap

// ─────────────────────────────────────────────────────────────────────────────
// Data class used to snapshot crop + transform state for undo/redo
// ─────────────────────────────────────────────────────────────────────────────
private data class EditorSnapshot(
    val cropRect: Rect,
    val userScale: Float,
    val imageOffset: Offset
)

// ─────────────────────────────────────────────────────────────────────────────
// Public entry point
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EditPictureScreen(
    imageUriString: String,
    onSave: (croppedUriString: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // ── Padding for crop frame interaction area ────────────────────────────────
    val framePaddingDp = 30.dp
    val framePaddingPx = with(density) { framePaddingDp.toPx() }

    // ── Bitmap loading ────────────────────────────────────────────────────────
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUriString) {
        withContext(Dispatchers.IO) {
            val uri = imageUriString.toUri()
            val bmp = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            bitmap = bmp
        }
    }

    // ── Canvas size (filled after first layout pass) ──────────────────────────
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // ── Image transform ───────────────────────────────────────────────────────
    var userScale by remember { mutableStateOf(1f) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }

    // ── Crop rect (in canvas pixels) ──────────────────────────────────────────
    var cropRect by remember { mutableStateOf(Rect.Zero) }

    // Initialise crop rect once we have both the canvas size and the bitmap
    LaunchedEffect(canvasSize, bitmap) {
        if (canvasSize != IntSize.Zero && bitmap != null && cropRect == Rect.Zero) {
            val bmp = bitmap!!
            val cw = canvasSize.width.toFloat()
            val ch = canvasSize.height.toFloat()
            val fitScale = minOf(cw / bmp.width, ch / bmp.height)
            val drawW = bmp.width * fitScale
            val drawH = bmp.height * fitScale
            val drawLeft = (cw - drawW) / 2f
            val drawTop = (ch - drawH) / 2f
            // Default crop = full image area, constrained by padding
            val paddedLeft = maxOf(drawLeft, framePaddingPx)
            val paddedTop = maxOf(drawTop, framePaddingPx)
            val paddedRight = minOf(drawLeft + drawW, cw - framePaddingPx)
            val paddedBottom = minOf(drawTop + drawH, ch - framePaddingPx)
            cropRect = Rect(paddedLeft, paddedTop, paddedRight, paddedBottom)
        }
    }

    // ── Two-state undo/redo ───────────────────────────────────────────────────
    // `editedSnapshot` holds the last user-made edits so we can restore them.
    var editedSnapshot by remember { mutableStateOf<EditorSnapshot?>(null) }
    var isShowingOriginal by remember { mutableStateOf(false) }
    // True once the user has made any crop / zoom / pan gesture
    var hasBeenEdited by remember { mutableStateOf(false) }

    /** Reset view to the original (unzoomed, full-image crop). */
    fun showOriginal() {
        if (!isShowingOriginal) {
            // Save current state so the user can redo
            editedSnapshot = EditorSnapshot(cropRect, userScale, imageOffset)
            isShowingOriginal = true
        }
        val bmp = bitmap ?: return
        val cw = canvasSize.width.toFloat()
        val ch = canvasSize.height.toFloat()
        val fitScale = minOf(cw / bmp.width, ch / bmp.height)
        val drawW = bmp.width * fitScale
        val drawH = bmp.height * fitScale
        val drawLeft = (cw - drawW) / 2f
        val drawTop = (ch - drawH) / 2f
        userScale = 1f
        imageOffset = Offset.Zero
        // Constrain by padding
        val paddedLeft = maxOf(drawLeft, framePaddingPx)
        val paddedTop = maxOf(drawTop, framePaddingPx)
        val paddedRight = minOf(drawLeft + drawW, cw - framePaddingPx)
        val paddedBottom = minOf(drawTop + drawH, ch - framePaddingPx)
        cropRect = Rect(paddedLeft, paddedTop, paddedRight, paddedBottom)
    }

    /** Restore the last edited state (redo). */
    fun restoreEdits() {
        val snap = editedSnapshot ?: return
        cropRect = snap.cropRect
        userScale = snap.userScale
        imageOffset = snap.imageOffset
        isShowingOriginal = false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Root layout
    // ─────────────────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = Color(249, 246, 242),
        topBar = {
            Header(
                onNavigateBack = onCancel,
                onNavigateToRightIcon = {},
                clothesData = null,
                headerText = "Foto bearbeiten",
                rightIconContentDescription = null,
                rightIcon = null,
                isFirstHeader = false
            )
        }
    ) { padding ->
        val handleSizeDp = 24.dp
        val handleSizePx = with(density) { handleSizeDp.toPx() }
        val minCropSizePx = with(density) { 60.dp.toPx() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Canvas area (fills remaining space) ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .weight(1f)
            ) {
                // ── Background image + dim overlay + crop border ───────────────
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                userScale = (userScale * zoom).coerceIn(0.5f, 5f)
                                imageOffset += pan
                                hasBeenEdited = true
                            }
                        }

                ) {
                    val bmp = bitmap ?: return@Canvas
                    val cw = size.width
                    val ch = size.height

                    // Draw checkerboard pattern as background
                    val squareSize = 16.dp.toPx()
                    val lightGrey = Color(230, 230, 230)
                    val white = Color.White
                    var isLight = true
                    var y = 0f
                    while (y < ch) {
                        var x = 0f
                        while (x < cw) {
                            drawRect(
                                color = if (isLight) white else lightGrey,
                                topLeft = Offset(x, y),
                                size = Size(squareSize, squareSize)
                            )
                            isLight = !isLight
                            x += squareSize
                        }
                        if ((cw / squareSize).toInt() % 2 == 0) {
                            isLight = !isLight
                        }
                        y += squareSize
                    }

                    // Fit-scale (base, without user zoom)
                    val fitScale = minOf(cw / bmp.width, ch / bmp.height)

                    val scaledW = bmp.width * fitScale * userScale
                    val scaledH = bmp.height * fitScale * userScale
                    val drawLeft = cw / 2f - scaledW / 2f + imageOffset.x
                    val drawTop  = ch / 2f - scaledH / 2f + imageOffset.y

                    // Draw the bitmap
                    drawImage(
                        image = bmp.asImageBitmap(),
                        dstOffset = IntOffset(drawLeft.roundToInt(), drawTop.roundToInt()),
                        dstSize   = IntSize(scaledW.roundToInt(), scaledH.roundToInt())
                    )

                    // Dim the areas outside the crop rect
                    val dimColor = Color.Black.copy(alpha = 0.55f)
                    if (cropRect != Rect.Zero) {
                        // Top
                        drawRect(dimColor, topLeft = Offset.Zero, size = Size(cw, cropRect.top))
                        // Bottom
                        drawRect(dimColor, topLeft = Offset(0f, cropRect.bottom), size = Size(cw, ch - cropRect.bottom))
                        // Left
                        drawRect(dimColor, topLeft = Offset(0f, cropRect.top), size = Size(cropRect.left, cropRect.height))
                        // Right
                        drawRect(dimColor, topLeft = Offset(cropRect.right, cropRect.top), size = Size(cw - cropRect.right, cropRect.height))

                        // Crop border
                        drawRect(
                            color = Color.White,
                            topLeft = cropRect.topLeft,
                            size = cropRect.size,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Rule-of-thirds grid lines
                        val w3 = cropRect.width / 3f
                        val h3 = cropRect.height / 3f
                        val gridColor = Color.White.copy(alpha = 0.35f)
                        for (i in 1..2) {
                            drawLine(gridColor, Offset(cropRect.left + w3 * i, cropRect.top), Offset(cropRect.left + w3 * i, cropRect.bottom), strokeWidth = 1f)
                            drawLine(gridColor, Offset(cropRect.left, cropRect.top + h3 * i), Offset(cropRect.right, cropRect.top + h3 * i), strokeWidth = 1f)
                        }
                    }
                }

                // ── Crop frame interaction layer ──────────────────────────────
                // Positions all overlay elements using the crop rect in canvas px
                if (cropRect != Rect.Zero) {
                    val cropPxLeft   = cropRect.left
                    val cropPxTop    = cropRect.top
                    val cropPxWidth  = cropRect.width
                    val cropPxHeight = cropRect.height

                    // Convert canvas pixels → Dp offsets for Compose layout
                    val leftDp   = with(density) { cropPxLeft.toDp() }
                    val topDp    = with(density) { cropPxTop.toDp() }
                    val widthDp  = with(density) { cropPxWidth.toDp() }
                    val heightDp = with(density) { cropPxHeight.toDp() }
                    val halfHandleDp = handleSizeDp / 2

                    // ── Crop body (drag to move the whole frame) ──────────────
                    Box(
                        modifier = Modifier
                            .offset(x = leftDp, y = topDp)
                            .size(widthDp, heightDp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val canvasW = canvasSize.width.toFloat()
                                    val canvasH = canvasSize.height.toFloat()
                                    val newLeft = (cropRect.left + dragAmount.x)
                                        .coerceIn(framePaddingPx, maxOf(framePaddingPx, canvasW - cropRect.width - framePaddingPx))
                                    val newTop  = (cropRect.top + dragAmount.y)
                                        .coerceIn(framePaddingPx, maxOf(framePaddingPx, canvasH - cropRect.height - framePaddingPx))
                                    cropRect = Rect(
                                        left   = newLeft,
                                        top    = newTop,
                                        right  = newLeft + cropRect.width,
                                        bottom = newTop  + cropRect.height
                                    )
                                    hasBeenEdited = true
                                }
                            }
                    )

                    // ── Top-left handle ────────────────────────────────────────
                    CropHandle(
                        xDp = leftDp - halfHandleDp,
                        yDp = topDp  - halfHandleDp,
                        sizeDp = handleSizeDp
                    ) { dragAmount ->
                        val newLeft = (cropRect.left + dragAmount.x)
                            .coerceIn(framePaddingPx, cropRect.right - minCropSizePx)
                        val newTop  = (cropRect.top + dragAmount.y)
                            .coerceIn(framePaddingPx, cropRect.bottom - minCropSizePx)
                        cropRect = Rect(newLeft, newTop, cropRect.right, cropRect.bottom)
                        hasBeenEdited = true
                    }

                    // ── Top-right handle ───────────────────────────────────────
                    CropHandle(
                        xDp = leftDp + widthDp - halfHandleDp,
                        yDp = topDp  - halfHandleDp,
                        sizeDp = handleSizeDp
                    ) { dragAmount ->
                        val newRight = (cropRect.right + dragAmount.x)
                            .coerceIn(cropRect.left + minCropSizePx, canvasSize.width.toFloat() - framePaddingPx)
                        val newTop   = (cropRect.top  + dragAmount.y)
                            .coerceIn(framePaddingPx, cropRect.bottom - minCropSizePx)
                        cropRect = Rect(cropRect.left, newTop, newRight, cropRect.bottom)
                        hasBeenEdited = true
                    }

                    // ── Bottom-left handle ────────────────────────────────────
                    CropHandle(
                        xDp = leftDp - halfHandleDp,
                        yDp = topDp  + heightDp - halfHandleDp,
                        sizeDp = handleSizeDp
                    ) { dragAmount ->
                        val newLeft   = (cropRect.left   + dragAmount.x)
                            .coerceIn(framePaddingPx, cropRect.right - minCropSizePx)
                        val newBottom = (cropRect.bottom + dragAmount.y)
                            .coerceIn(cropRect.top + minCropSizePx, canvasSize.height.toFloat() - framePaddingPx)
                        cropRect = Rect(newLeft, cropRect.top, cropRect.right, newBottom)
                        hasBeenEdited = true
                    }

                    // ── Bottom-right handle ────────────────────────────────────
                    CropHandle(
                        xDp = leftDp + widthDp  - halfHandleDp,
                        yDp = topDp  + heightDp - halfHandleDp,
                        sizeDp = handleSizeDp
                    ) { dragAmount ->
                        val newRight  = (cropRect.right  + dragAmount.x)
                            .coerceIn(cropRect.left + minCropSizePx, canvasSize.width.toFloat() - framePaddingPx)
                        val newBottom = (cropRect.bottom + dragAmount.y)
                            .coerceIn(cropRect.top + minCropSizePx, canvasSize.height.toFloat() - framePaddingPx)
                        cropRect = Rect(cropRect.left, cropRect.top, newRight, newBottom)
                        hasBeenEdited = true
                    }
                }
            }

            // ── Undo / Redo row ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left arrow = show original (active only after the user has made edits)
                val leftEnabled = hasBeenEdited && !isShowingOriginal
                IconButton(
                    onClick = { showOriginal() },
                    enabled = leftEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Original anzeigen",
                        tint = if (leftEnabled) Color.DarkGray else Color.DarkGray.copy(alpha = 0.35f)
                    )
                }
                // Right arrow = restore edits (active only after left arrow was pressed)
                val rightEnabled = isShowingOriginal && editedSnapshot != null
                IconButton(
                    onClick = { restoreEdits() },
                    enabled = rightEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Änderungen wiederherstellen",
                        tint = if (rightEnabled) Color.DarkGray else Color.DarkGray.copy(alpha = 0.35f)
                    )
                }
            }

            // ── Bottom action buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }
                Button(
                    onClick = {
                        val bmp = bitmap ?: run { onCancel(); return@Button }
                        scope.launch {
                            // Map crop rect (canvas px) → source bitmap px
                            val cw = canvasSize.width.toFloat()
                            val ch = canvasSize.height.toFloat()
                            val fitScale = minOf(cw / bmp.width, ch / bmp.height) * userScale
                            val scaledW = bmp.width * fitScale
                            val scaledH = bmp.height * fitScale
                            val drawLeft = cw / 2f - scaledW / 2f + imageOffset.x
                            val drawTop  = ch / 2f - scaledH / 2f + imageOffset.y
                            val pixPerCanvasPx = 1f / fitScale

                            val bitmapLeft   = ((cropRect.left   - drawLeft) * pixPerCanvasPx).roundToInt()
                            val bitmapTop    = ((cropRect.top    - drawTop ) * pixPerCanvasPx).roundToInt()
                            val bitmapRight  = ((cropRect.right  - drawLeft) * pixPerCanvasPx).roundToInt()
                            val bitmapBottom = ((cropRect.bottom - drawTop ) * pixPerCanvasPx).roundToInt()

                            val androidRect = AndroidRect(bitmapLeft, bitmapTop, bitmapRight, bitmapBottom)
                            val resultUri = cropAndSaveImage(
                                context = context,
                                sourceUri = imageUriString.toUri(),
                                cropRect = androidRect
                            )
                            val output = resultUri ?: imageUriString.toUri()
                            onSave(output.toString())
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Corner handle composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CropHandle(
    xDp: androidx.compose.ui.unit.Dp,
    yDp: androidx.compose.ui.unit.Dp,
    sizeDp: androidx.compose.ui.unit.Dp,
    onDrag: (Offset) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(sizeDp)
            .background(Color.White, shape = RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
    )
}
