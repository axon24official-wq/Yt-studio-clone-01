package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    private const val TAG = "ImageStorageHelper"

    /**
     * Copies an image from a content URI provided by the system photo/gallery picker
     * into the app's internal persistent files directory.
     * Returns a persistent file:// URI string that survives app reboots and does not expire.
     */
    fun saveUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "img_"): String {
        return try {
            val contentResolver = context.contentResolver
            val extension = when (contentResolver.getType(sourceUri)) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val fileName = "${prefix}${System.currentTimeMillis()}${extension}"
            val imagesDir = File(context.filesDir, "studio_images").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(imagesDir, fileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destinationFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy image to internal storage", e)
            sourceUri.toString()
        }
    }
}
