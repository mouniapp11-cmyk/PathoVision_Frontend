package com.simats.pathovision.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    private const val TAG = "FileUtils"
    
    /**
     * Convert a content URI to a temporary file that can be used for ML inference
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            Log.d(TAG, "Converting URI to file: $uri")
            
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: $uri")
                return null
            }
            
            val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
            val tempFile = File(context.cacheDir, fileName)
            
            Log.d(TAG, "Creating temp file: ${tempFile.absolutePath}")
            
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            Log.d(TAG, "File created successfully. Size: ${tempFile.length()} bytes")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file: ${e.message}", e)
            null
        }
    }
}
