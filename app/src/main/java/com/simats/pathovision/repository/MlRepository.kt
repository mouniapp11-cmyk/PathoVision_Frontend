package com.simats.pathovision.repository

import android.util.Log
import com.simats.pathovision.models.MlPredictionResponse
import com.simats.pathovision.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "MlRepository"
    }
    
    suspend fun predictImage(imageFile: File): MlPredictionResponse? {
        return try {
            Log.d(TAG, "Preparing image for prediction: ${imageFile.absolutePath}")
            Log.d(TAG, "File size: ${imageFile.length()} bytes")
            
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            Log.d(TAG, "Calling ML API...")
            val response = apiService.predictImage(body)
            
            Log.d(TAG, "ML API response code: ${response.code()}")
            Log.d(TAG, "ML API response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val result = response.body()
                Log.d(TAG, "ML prediction result: $result")
                result
            } else {
                Log.e(TAG, "ML API error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "ML prediction exception: ${e.message}", e)
            null
        }
    }
}
