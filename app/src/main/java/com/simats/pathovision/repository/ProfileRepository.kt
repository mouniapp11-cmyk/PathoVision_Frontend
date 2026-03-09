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
        dateOfBirth: String? = null,
        profileImage: MultipartBody.Part? = null
    ): Resource<Map<String, Any>> {
        return try {
            val updateData = mutableMapOf<String, RequestBody>()
            val textType = "text/plain".toMediaType()

            android.util.Log.d("ProfileRepository", "Building update request: name=$name, phone=$phoneNumber, hospital=$hospitalAffiliation, license=$licenseId, dob=$dateOfBirth")

            if (name != null) {
                updateData["name"] = name.toRequestBody(textType)
                android.util.Log.d("ProfileRepository", "Added name: $name")
            }
            if (phoneNumber != null) {
                updateData["phone_number"] = phoneNumber.toRequestBody(textType)
                android.util.Log.d("ProfileRepository", "Added phone_number: $phoneNumber")
            }
            if (hospitalAffiliation != null) {
                updateData["hospital_affiliation"] = hospitalAffiliation.toRequestBody(textType)
                android.util.Log.d("ProfileRepository", "Added hospital_affiliation: $hospitalAffiliation")
            }
            if (licenseId != null) {
                updateData["license_id"] = licenseId.toRequestBody(textType)
                android.util.Log.d("ProfileRepository", "Added license_id: $licenseId")
            }
            if (dateOfBirth != null) {
                updateData["date_of_birth"] = dateOfBirth.toRequestBody(textType)
                android.util.Log.d("ProfileRepository", "Added date_of_birth: $dateOfBirth")
            }

            android.util.Log.d("ProfileRepository", "Calling API with ${updateData.size} fields, hasImage=${profileImage != null}")

            val response = apiService.updateProfile(updateData, profileImage)
            
            android.util.Log.d("ProfileRepository", "API response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ProfileRepository", "Profile update successful")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ProfileRepository", "Profile update failed: $errorBody")
                Resource.Error(errorBody ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepository", "Profile update exception: ${e.message}", e)
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }
}
