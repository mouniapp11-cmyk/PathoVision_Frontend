package com.simats.pathovision.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String  // Must be uppercase: "PATHOLOGIST", "PATIENT", "STUDENT"
)

data class RegisterResponse(
    val message: String?,
    val token: String?,
    val user: UserData?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String?,
    val token: String?,
    val user: UserData?
)

data class UserData(
    val id: String,      // UUID from Postgres
    val name: String,
    val email: String,
    val role: String,    // Uppercase: "PATHOLOGIST", "PATIENT", "STUDENT"
    @SerializedName("phone_number")
    val phone_number: String? = null,
    @SerializedName("hospital_affiliation")
    val hospital_affiliation: String? = null,
    @SerializedName("license_id")
    val license_id: String? = null,
    @SerializedName("profile_picture")
    val profile_picture: String? = null
)

data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("phone_number")
    val phone_number: String? = null,
    @SerializedName("hospital_affiliation")
    val hospital_affiliation: String? = null,
    @SerializedName("license_id")
    val license_id: String? = null,
    @SerializedName("profile_picture")
    val profile_picture: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String
)
