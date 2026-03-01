package com.example.looksy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Crops a source image to the given [cropRect] (in source-bitmap pixel coordinates)
 * and saves the result to the cache directory.
 *
 * @param context       Application / activity context.
 * @param sourceUri     URI of the source image (file, content, …).
 * @param cropRect      Rectangle in source-bitmap pixels to crop.
 * @return              [Uri] pointing to the cropped file in cacheDir, or null on failure.
 */
suspend fun cropAndSaveImage(
    context: Context,
    sourceUri: Uri,
    cropRect: Rect
): Uri? {
    return try {
        // 1. Decode source bitmap
        val sourceBitmap: Bitmap = context.contentResolver
            .openInputStream(sourceUri)
            ?.use { stream -> BitmapFactory.decodeStream(stream) }
            ?: return null

        // 2. Clamp rect to bitmap bounds
        val left   = cropRect.left.coerceIn(0, sourceBitmap.width)
        val top    = cropRect.top.coerceIn(0, sourceBitmap.height)
        val right  = cropRect.right.coerceIn(left, sourceBitmap.width)
        val bottom = cropRect.bottom.coerceIn(top, sourceBitmap.height)

        val width  = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) {
            Log.w("ImageCropper", "Crop rect has zero area – returning original URI")
            return sourceUri
        }

        // 3. Crop
        val cropped = Bitmap.createBitmap(sourceBitmap, left, top, width, height)

        // 4. Save to cache
        val outFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { fos ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }

        Uri.fromFile(outFile)
    } catch (e: Exception) {
        Log.e("ImageCropper", "cropAndSaveImage failed: ${e.message}", e)
        null
    }
}
