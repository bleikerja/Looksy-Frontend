package com.example.looksy.screens

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import kotlin.coroutines.resume
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
    val context = LocalContext.current
    // Berechtigungen prüfen
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // UI basierend auf dem Berechtigungsstatus
    if (cameraPermissionState.status.isGranted) {
        // Wenn die Berechtigung erteilt ist, zeige die Kamera-Vorschau
        CameraView(
            context = context,
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
    context: Context,
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
        val cameraProvider = cameraProviderFuture.await(context)
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
                takePhoto(context, imageCapture, onImageCaptured)
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

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val file = File(context.externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                // Fehler behandeln
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(file)
                // URI zurückgeben, um zum nächsten Screen zu navigieren
                onImageCaptured(savedUri)
            }
        }
    )
}

private suspend fun <T> ListenableFuture<T>.await(context: Context): T =
    suspendCoroutine { continuation ->
        // 'this' now correctly refers to the ListenableFuture instance
        addListener(
            {
                try {
                    // .get() is called on the future ('this')
                    continuation.resume(get())
                } catch (e: Exception) {
                    continuation.resumeWith(Result.failure(e))
                }
            },
            // The executor is now retrieved correctly
            ContextCompat.getMainExecutor(context)
        )
    }


@Preview
@Composable
fun PreviewCameraScreenPermission() {
    CameraScreenPermission(onImageCaptured = {})
}