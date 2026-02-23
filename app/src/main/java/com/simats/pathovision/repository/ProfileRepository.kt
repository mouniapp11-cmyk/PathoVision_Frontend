package com.simats.pathovision.repository

import com.simats.pathovision.models.ProfileResponse
import com.simats.pathovision.network.ApiService
import com.simats.pathovision.utils.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getProfile(): Resource<ProfileResponse> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Failed to fetch profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }

    suspend fun updateProfile(
        name: String? = null,
        phoneNumber: String? = null,
        hospitalAffiliation: String? = null,
        licenseId: String? = null,
        profileImage: MultipartBody.Part? = null
    ): Resource<Map<String, Any>> {
        return try {
            val updateData = mutableMapOf<String, RequestBody>()
            val textType = "text/plain".toMediaType()

            if (name != null) updateData["name"] = name.toRequestBody(textType)
            if (phoneNumber != null) updateData["phone_number"] = phoneNumber.toRequestBody(textType)
            if (hospitalAffiliation != null) updateData["hospital_affiliation"] = hospitalAffiliation.toRequestBody(textType)
            if (licenseId != null) updateData["license_id"] = licenseId.toRequestBody(textType)

            val response = apiService.updateProfile(updateData, profileImage)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
