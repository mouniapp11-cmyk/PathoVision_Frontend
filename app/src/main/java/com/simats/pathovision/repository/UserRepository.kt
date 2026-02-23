package com.simats.pathovision.repository

import com.simats.pathovision.models.UserData
import com.simats.pathovision.network.ApiService
import com.simats.pathovision.utils.Resource
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDoctors(): Resource<List<UserData>> {
        return try {
            val response = apiService.getDoctors()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to load doctors")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPatients(): Resource<List<UserData>> {
        return try {
            val response = apiService.getPatients()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to load patients")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
