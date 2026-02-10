package com.example.looksy.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit // Callback, um die URI des Bildes zurückzugeben
) {
    var takePicture by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    if(takePicture && cameraPermissionState.status.isGranted) {
        CameraView(
            onImageCaptured = onImageCaptured
        )
    } else {
        val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                onImageCaptured(uri)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Foto hizufügen",
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp
                )
                Text(
                    text = "aus Galerie wählen oder neu aufnehmen",
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    IconButton(
                        onClick = {
                            takePicture = true
                            if (!cameraPermissionState.status.isGranted) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.size(100.dp)
                    )
                    {
                        Icon(
                            modifier = Modifier.fillMaxSize().padding(5.dp),
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "mit Kamera aufnehmen"
                        )
                    }

                    IconButton(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.size(100.dp)
                    )
                    {
                        Icon(
                            modifier = Modifier.fillMaxSize().padding(5.dp),
                            imageVector = Icons.Default.Photo,
                            contentDescription = "aus Galerie auswählen"
                        )
                    }
                }
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
        Executors.newSingleThreadExecutor()
        )
}

@Preview
@Composable
fun PreviewCameraScreen() {
    CameraScreen (onImageCaptured = {})
}