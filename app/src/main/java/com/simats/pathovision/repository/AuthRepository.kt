package com.simats.pathovision.repository

import com.simats.pathovision.models.ChangePasswordRequest
import com.simats.pathovision.models.LoginRequest
import com.simats.pathovision.models.LoginResponse
import com.simats.pathovision.models.RegisterRequest
import com.simats.pathovision.models.RegisterResponse
import com.simats.pathovision.network.ApiService
import com.simats.pathovision.utils.Resource
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Resource<RegisterResponse> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password, role))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Registration failed. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred.")
        }
    }

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Login failed. Check your credentials.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to connect. Is the server running?")
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<String> {
        return try {
            val response = apiService.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.message)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Unable to update password.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to connect. Is the server running?")
        }
    }
}
