package com.example.looksy.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreenPermission(onImageCaptured: (Uri) -> Unit) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    if (cameraPermissionState.status.isGranted) {
        CameraScreen(onImageCaptured = onImageCaptured)
    } else {
        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
            Text("Kamera-Zugriff erlauben")
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit // Callback, um die URI des Bildes zurückzugeben
) {
    // Berechtigungen prüfen
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // UI basierend auf dem Berechtigungsstatus
    if (cameraPermissionState.status.isGranted) {
        // Wenn die Berechtigung erteilt ist, zeige die Kamera-Vorschau
        CameraView(
            onImageCaptured = onImageCaptured
        )
    } else {
        // Ansonsten, zeige einen Button, um die Berechtigung anzufordern
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Kamera-Zugriff erlauben")
            }
        }
    }
}

@Composable
private fun CameraView(
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Use cases für die Kamera
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Kamera binden, wenn die Composable gestartet wird
    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.await()
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Bestehende Bindungen aufheben und neu binden
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            // Fehlerbehandlung
        }
    }

    // UI-Layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Kamera-Vorschau
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Auslöser-Button
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .size(80.dp)
                .border(2.dp, Color.White, CircleShape),
            onClick = {
                // Foto aufnehmen
                lifecycleOwner.lifecycleScope.launch {
                    takePhoto(context, imageCapture, onImageCaptured)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Foto aufnehmen",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

private suspend fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    // ✅ ÄNDERUNG: Speichere im Cache-Verzeichnis statt im externen Medienverzeichnis.
    // Das ist sicherer und erfordert keine speziellen Berechtigungen.
    val photoFile = File(
        context.cacheDir,
        "temp_${System.currentTimeMillis()}.jpg"
    )

    try {
        // Nutze die eingebaute suspend-Funktion von CameraX
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions) // Diese Funktion wartet, bis das Bild gespeichert ist

        // Wenn die obige Zeile ohne Fehler durchläuft, ist das Bild gespeichert.
        val savedUri = Uri.fromFile(photoFile)
        onImageCaptured(savedUri)

    } catch (exc: ImageCaptureException) {
        Log.e("Camera", "Fotoaufnahme fehlgeschlagen: ${exc.message}", exc)
        // Hier könntest du dem Nutzer eine Fehlermeldung anzeigen.
        // Wichtig: Führe UI-Änderungen im Main-Thread aus
        // withContext(Dispatchers.Main) {
        //     Toast.makeText(context, "Fehler: ${exc.message}", Toast.LENGTH_SHORT).show()
        // }
    }
}

private suspend fun <T> ListenableFuture<T>.await(): T = suspendCoroutine { continuation ->
    addListener(
        {
            try {
                continuation.resume(get())
            } catch (e: Exception) {
                // Bei Fehlern die Coroutine mit einer Exception fortsetzen
                continuation.resumeWithException(e.cause ?: e)
            }
        },
        java.util.concurrent.Executors.newSingleThreadExecutor()
        )
}

@Preview
@Composable
fun PreviewCameraScreenPermission() {
    CameraScreenPermission(onImageCaptured = {})
}