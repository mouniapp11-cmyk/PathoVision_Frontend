package com.simats.pathovision.network

import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.models.CreateCaseRequest
import com.simats.pathovision.models.ChangePasswordRequest
import com.simats.pathovision.models.ChangePasswordResponse
import com.simats.pathovision.models.LoginRequest
import com.simats.pathovision.models.LoginResponse
import com.simats.pathovision.models.Message
import com.simats.pathovision.models.MessageRequest
import com.simats.pathovision.models.Conversation
import com.simats.pathovision.models.ProfileResponse
import com.simats.pathovision.models.RegisterRequest
import com.simats.pathovision.models.RegisterResponse
import com.simats.pathovision.models.UserData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("cases")
    suspend fun getCases(): Response<List<CaseItem>>

    @POST("cases")
    suspend fun createCase(@Body request: CreateCaseRequest): Response<CaseItem>

    @GET("auth/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @PUT("auth/profile")
    suspend fun updateProfile(
        @PartMap profileData: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePicture: MultipartBody.Part?
    ): Response<Map<String, Any>>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

    @GET("messages/inbox")
    suspend fun getInbox(): Response<List<Conversation>>

    @GET("messages/{caseId}")
    suspend fun getMessages(@Path("caseId") caseId: String): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(@Body request: MessageRequest): Response<Message>

    @GET("auth/doctors")
    suspend fun getDoctors(): Response<List<UserData>>

    @GET("auth/patients")
    suspend fun getPatients(): Response<List<UserData>>
}