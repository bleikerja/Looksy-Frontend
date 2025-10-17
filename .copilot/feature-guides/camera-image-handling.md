# Camera & Image Handling Feature

## Overview

Looksy uses **CameraX** for camera integration, allowing users to take photos of clothing items. Images are stored locally in the app's cache directory, with file paths saved in the database.

## Key Files

- `screens/Kamera.kt` - Camera implementation
- `Routes.kt` - Navigation handling for captured images
- `screens/ScreenAddNewClothes.kt` - Form to save image with metadata

## Camera Implementation

### Permission Handling

**Library**: Accompanist Permissions (0.37.3)

```kotlin
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
```

**Manifest Declaration** (`AndroidManifest.xml`):

```xml
<uses-feature android:name="android.hardware.camera.any" android:required="true" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Camera Setup

**CameraX Configuration:**

```kotlin
val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
val previewView = remember { PreviewView(context) }
val imageCapture = remember { ImageCapture.Builder().build() }

LaunchedEffect(cameraProviderFuture) {
    val cameraProvider = cameraProviderFuture.await()
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture
    )
}
```

**UI Structure:**

- `AndroidView` for camera preview (native PreviewView)
- `IconButton` overlay for capture trigger
- Positioned at bottom center with styled button

### Photo Capture

**Image Storage Location**: `context.cacheDir`

```kotlin
private suspend fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "temp_${System.currentTimeMillis()}.jpg"
    )

    try {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions)

        val savedUri = Uri.fromFile(photoFile)
        onImageCaptured(savedUri)
    } catch (exc: ImageCaptureException) {
        Log.e("Camera", "Fotoaufnahme fehlgeschlagen: ${exc.message}", exc)
    }
}
```

**Why `cacheDir`?**

- No external storage permissions needed
- Temporary storage suitable for preview
- Later moved to persistent storage when saved

### Async Helper (ListenableFuture)

**CameraX returns `ListenableFuture`**, needs conversion to coroutine:

```kotlin
private suspend fun <T> ListenableFuture<T>.await(): T = suspendCoroutine { continuation ->
    addListener(
        {
            try {
                continuation.resume(get())
            } catch (e: Exception) {
                continuation.resumeWithException(e.cause ?: e)
            }
        },
        Executors.newSingleThreadExecutor()
    )
}
```

## Image Storage Strategy

### Temporary Storage Flow

1. **Capture**: Image saved to `context.cacheDir` as `temp_<timestamp>.jpg`
2. **Preview**: User sees image in `AddNewClothesScreen`
3. **Save**: On form submission, image moved to permanent storage

### Permanent Storage

**Function** (from commented code in `ClothesViewModel.kt`):

```kotlin
private fun saveImagePermanently(context: Context, tempUri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(tempUri)
    val fileName = "IMG_${System.currentTimeMillis()}.jpg"

    // context.filesDir is private, internal storage
    val persistentFile = File(context.filesDir, fileName)

    val outputStream = FileOutputStream(persistentFile)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    // Delete temporary file
    val tempFile = File(tempUri.path)
    if (tempFile.exists()) {
        tempFile.delete()
    }

    return persistentFile.absolutePath
}
```

**Current Implementation** (in `Routes.kt`):

```kotlin
composable(Routes.AddNewClothes.route, ...) { backStackEntry ->
    val imageUriString = Uri.decode(backStackEntry.arguments?.getString(RouteArgs.IMAGE_URI))

    AddNewClothesScreen(
        imageUriString = imageUriString,
        onSave = { newItem, imageUri ->
            val path = saveImagePermanently(context, imageUri)
            val clothesWithPath = newItem.copy(imagePath = path)
            viewModel.insert(clothesWithPath)
            navController.navigate(Routes.ChoseClothes.route)
        }
    )
}
```

### Database Storage

Only the **file path** is stored in the database:

```kotlin
data class Clothes(
    // ...
    val imagePath: String = ""
)
```

**Path format**: `/data/user/0/com.example.looksy/files/IMG_1234567890.jpg`

## Image Display

### Loading Library: Coil

**Dependency**: `io.coil-kt:coil-compose` (2.7.0)

**Usage:**

```kotlin
AsyncImage(
    model = clothes.imagePath, // Can be file path, URI, or resource
    contentDescription = "Clothing item",
    modifier = Modifier.size(200.dp)
)
```

**Coil Features Used:**

- Automatic image loading from file paths
- Built-in caching
- Supports `String` paths, `Uri`, and `Int` resource IDs

### Preview Display

In `AddNewClothesScreen`:

```kotlin
AsyncImage(
    model = imageUriString.toUri(), // Temporary URI
    contentDescription = "Preview",
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .clip(RoundedCornerShape(16.dp))
)
```

## Navigation Flow

### Scan → Capture → Save

```
1. User taps "Scan" in bottom navigation
   ↓
2. CameraScreenPermission composable
   ↓
3. If permission granted → CameraView
   ↓
4. User taps capture button
   ↓
5. Photo saved to cache, URI returned
   ↓
6. Navigate to AddNewClothes with URI
   ↓
7. User fills form and taps "Save"
   ↓
8. Image moved to filesDir, path saved in DB
   ↓
9. Navigate back to wardrobe
```

**Navigation Code:**

```kotlin
// In Routes.kt, Scan destination
composable(Routes.Scan.route) {
    CameraScreenPermission(
        onImageCaptured = { imageUri ->
            val encodedUri = Uri.encode(imageUri.toString())
            navController.navigate(Routes.AddNewClothes.createRoute(encodedUri))
        }
    )
}
```

## Error Handling

### Camera Errors

```kotlin
try {
    cameraProvider.bindToLifecycle(...)
} catch (exc: Exception) {
    // Error handling for camera binding failures
}
```

### Image Capture Errors

```kotlin
catch (exc: ImageCaptureException) {
    Log.e("Camera", "Fotoaufnahme fehlgeschlagen: ${exc.message}", exc)
    // Could show Toast or Snackbar to user
}
```

### URI Parsing

In `AddNewClothesScreen`:

```kotlin
val imageUri = try {
    imageUriString.toUri()
} catch (e: IllegalArgumentException) {
    Uri.EMPTY
}

if (imageUri != Uri.EMPTY) {
    onSave(newItem, imageUri)
}
```

## Best Practices

### Memory Management

1. **Use cache for temporary files**: Automatic cleanup by system
2. **Delete temp files after copying**: Prevent cache bloat
3. **Store only paths in DB**: Not actual image data

### Image Quality

- **Default CameraX quality**: Automatic based on device
- **Compression**: None currently applied
- **Consider adding**: JPEG quality control, image resizing

### File Naming

Current pattern: `IMG_${System.currentTimeMillis()}.jpg`

- Timestamp ensures uniqueness
- `.jpg` extension for compatibility

## Future Enhancements

### Potential Improvements

1. **Image Compression**:

   ```kotlin
   bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
   ```

2. **Image Resizing**:

   - Resize large images to max width/height (e.g., 1024px)
   - Reduce storage and improve performance

3. **Multiple Photos**:

   - Store array of image paths
   - Front/back/detail views

4. **Gallery Selection**:

   - Alternative to camera capture
   - Use `rememberLauncherForActivityResult` with `GetContent` contract

5. **Image Editing**:

   - Crop/rotate before saving
   - Background removal for clothing items

6. **Cloud Backup**:
   - Use `isSynced` field
   - Upload to backend storage
   - Store cloud URL alongside local path

## Troubleshooting

### Common Issues

**Camera not opening:**

- Check permission is granted in Manifest
- Verify `rememberPermissionState` is working
- Check device has camera hardware

**Image not displaying:**

- Verify file path is correct
- Check file exists: `File(path).exists()`
- Ensure Coil dependency is included

**OutOfMemoryError:**

- Consider image compression/resizing
- Limit image dimensions during capture
- Use Coil's automatic memory management

**Images lost after app reinstall:**

- Expected behavior (using app-private storage)
- For persistence, consider:
  - MediaStore API
  - Cloud storage
  - Export/import feature
