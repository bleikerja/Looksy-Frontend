package com.example.looksy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.ClothesColor
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

/**
 * Manages seeding and clearing demo clothing data.
 *
 * ─── HOW TO SWITCH BETWEEN PLACEHOLDER AND REAL IMAGES ───────────────────
 *
 *  Set [DEMO_USE_REAL_IMAGES] to `true` AND drop your 35 PNG/JPG files into
 *  `app/src/main/assets/demo_images/` using the exact file names defined in
 *  `demo_clothes.json` (e.g. `tshirt_1.png`, `shoes_3.png`).
 *
 *  Set [DEMO_USE_REAL_IMAGES] to `false` (default) to use programmatically
 *  generated color-block placeholders — no image files required.
 * ─────────────────────────────────────────────────────────────────────────
 */
object DemoDataManager {

    /**
     * Switch between real photos and programmatic color-block placeholders.
     *
     * `false` (default) → colored rectangles are generated in-process.
     * `true`            → copies PNG/JPG files from assets/demo_images/ to filesDir.
     */
    const val DEMO_USE_REAL_IMAGES: Boolean = true

    // ── Internal JSON model ──────────────────────────────────────────────

    private data class DemoClothesEntry(
        val type: String,
        val size: String,
        val season: String,
        val material: String?,
        val color: String?,
        val brand: String?,
        val washingNotes: List<String>,
        val clean: Boolean,
        val imageName: String
    )

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Parses `assets/demo_clothes.json` and returns a ready-to-insert list
     * of [Clothes] objects with [Clothes.isDemoData] = `true`.
     *
     * Images are either copied from `assets/demo_images/` (when
     * [DEMO_USE_REAL_IMAGES] is `true`) or generated as colored placeholders.
     */
    fun loadDemoClothes(context: Context): List<Clothes> {
        val json = context.assets.open("demo_clothes.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object : TypeToken<List<DemoClothesEntry>>() {}.type
        val entries: List<DemoClothesEntry> = Gson().fromJson(json, listType)

        return entries.mapIndexed { index, entry ->
            val imagePath = if (DEMO_USE_REAL_IMAGES) {
                copyRealImage(context, entry.imageName)
            } else {
                generatePlaceholderImage(
                    context = context,
                    imageName = entry.imageName,
                    type = Type.valueOf(entry.type),
                    color = entry.color?.let { runCatching { ClothesColor.valueOf(it) }.getOrNull() }
                )
            }

            Clothes(
                type = Type.valueOf(entry.type),
                size = Size.valueOf(entry.size),
                seasonUsage = Season.valueOf(entry.season),
                material = entry.material?.let { runCatching { Material.valueOf(it) }.getOrNull() },
                color = entry.color?.let { runCatching { ClothesColor.valueOf(it) }.getOrNull() },
                brand = entry.brand,
                comment = null,
                clean = entry.clean,
                washingNotes = entry.washingNotes.mapNotNull { name ->
                    runCatching { WashingNotes.valueOf(name) }.getOrNull()
                },
                imagePath = imagePath,
                isDemoData = true,
                wornClothes = 0
            )
        }
    }

    // ── Image helpers ────────────────────────────────────────────────────

    /**
     * Copies `assets/demo_images/[imageName]` to `filesDir/images/demo_[imageName]`
     * and returns the absolute path. Call only when [DEMO_USE_REAL_IMAGES] is `true`.
     */
    private fun copyRealImage(context: Context, imageName: String): String {
        val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
        val dest = File(imagesDir, "demo_$imageName")
        if (!dest.exists()) {
            context.assets.open(imageName).use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
        }
        return dest.absolutePath
    }

    /**
     * Generates a solid-color rectangle Bitmap with the clothing type name
     * drawn in white/dark text in the centre. Saves the result as a PNG to
     * `filesDir/images/demo_[imageName]` and returns the absolute path.
     *
     * The background colour is chosen from [ClothesColor]; neutral colours
     * receive a medium-grey tint so text remains visible.
     */
    private fun generatePlaceholderImage(
        context: Context,
        imageName: String,
        type: Type,
        color: ClothesColor?
    ): String {
        val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
        val dest = File(imagesDir, "demo_$imageName")

        if (!dest.exists()) {
            val (bgColor, textColor) = colorPair(color)

            val width = 400
            val height = 500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            val bgPaint = Paint().apply {
                style = Paint.Style.FILL
                this.color = bgColor
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Label
            val textPaint = Paint().apply {
                this.color = textColor
                textSize = 52f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val cx = width / 2f
            val cy = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(type.displayName, cx, cy, textPaint)

            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            bitmap.recycle()
        }

        return dest.absolutePath
    }

    /** Returns (background ARGB int, text ARGB int) for a given [ClothesColor]. */
    private fun colorPair(clothesColor: ClothesColor?): Pair<Int, Int> {
        val white = android.graphics.Color.WHITE
        val darkText = android.graphics.Color.parseColor("#1A1A1A")
        val lightText = android.graphics.Color.parseColor("#F5F5F5")

        val bg = when (clothesColor) {
            ClothesColor.Black   -> android.graphics.Color.parseColor("#212121")
            ClothesColor.White   -> android.graphics.Color.parseColor("#F0F0F0")
            ClothesColor.Grey    -> android.graphics.Color.parseColor("#9E9E9E")
            ClothesColor.Navy    -> android.graphics.Color.parseColor("#1A237E")
            ClothesColor.Beige   -> android.graphics.Color.parseColor("#D7CCC8")
            ClothesColor.Brown   -> android.graphics.Color.parseColor("#6D4C41")
            ClothesColor.Olive   -> android.graphics.Color.parseColor("#827717")
            ClothesColor.Blue    -> android.graphics.Color.parseColor("#1565C0")
            ClothesColor.LightBlue -> android.graphics.Color.parseColor("#29B6F6")
            ClothesColor.Green   -> android.graphics.Color.parseColor("#2E7D32")
            ClothesColor.Red     -> android.graphics.Color.parseColor("#C62828")
            ClothesColor.Burgundy -> android.graphics.Color.parseColor("#4A148C")
            ClothesColor.Pink    -> android.graphics.Color.parseColor("#F48FB1")
            ClothesColor.Purple  -> android.graphics.Color.parseColor("#6A1B9A")
            ClothesColor.Yellow  -> android.graphics.Color.parseColor("#F9A825")
            ClothesColor.Orange  -> android.graphics.Color.parseColor("#E65100")
            null                 -> android.graphics.Color.parseColor("#B0BEC5")
        }

        // Use dark text on light backgrounds, light text on dark ones
        val isLight = clothesColor in listOf(
            ClothesColor.White, ClothesColor.Beige, ClothesColor.LightBlue,
            ClothesColor.Yellow, ClothesColor.Pink
        )
        val fg = if (isLight) darkText else lightText

        return bg to fg
    }
}
