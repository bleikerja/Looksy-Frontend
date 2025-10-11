package com.example.looksy.ViewModels

/*
private fun saveImagePermanently(context: Context, tempUri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(tempUri)
    val fileName = "IMG_${System.currentTimeMillis()}.jpg"

    // context.filesDir ist der private, interne Speicher deiner App.
    val persistentFile = File(context.filesDir, fileName)

    val outputStream = FileOutputStream(persistentFile)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    // Lösche die temporäre Datei aus dem Cache, um Speicherplatz zu sparen.
    val tempFile = File(tempUri.path)
    if (tempFile.exists()) {
        tempFile.delete()
    }

    // Gib den permanenten Pfad als String zurück.
    return persistentFile.absolutePath
}
 */