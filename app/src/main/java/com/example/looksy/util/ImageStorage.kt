package com.example.looksy.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveImagePermanently(context: Context, imageUri: Uri): String? {
    return try {
        val currentDate = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
        val fileName = "IMG_$timeStamp.jpg"
        val storageDir = File(context.filesDir, "images")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val permanentFile = File(storageDir, fileName)
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(permanentFile).use { output ->
                input.copyTo(output)
            }
        }
        permanentFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
