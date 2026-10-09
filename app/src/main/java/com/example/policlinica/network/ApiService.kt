package com.example.policlinica.network

import com.example.policlinica.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTENTIFICARE (existing) ====================
    
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
    
    // ==================== DOCTORI ====================
    
    @GET("api/policlinica/doctors")
    suspend fun getDoctors(): Response<ApiResponse<List<DoctorDto>>>
    
    @POST("api/policlinica/doctors")
    suspend fun createDoctor(@Body request: CreateDoctorRequest): Response<ApiResponse<DoctorDto>>
    
    @DELETE("api/policlinica/doctors/{id}")
    suspend fun deleteDoctor(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    // ==================== PACIENTI ====================
    
    @GET("api/policlinica/patients")
    suspend fun getPatients(): Response<ApiResponse<List<PatientDto>>>
    
    @POST("api/policlinica/patients")
    suspend fun createPatient(@Body request: CreatePatientRequest): Response<ApiResponse<PatientDto>>
    
    @GET("api/policlinica/patient-id")
    suspend fun getPatientIdByEmail(@Query("email") email: String): Response<Map<String, Any>>
    
    // ==================== PROGRAMĂRI ====================
    
    @GET("api/policlinica/appointments")
    suspend fun getAppointments(
        @Query("pacient_id") pacientId: Int? = null,
        @Query("doctor_id") doctorId: Int? = null
    ): Response<ApiResponse<List<AppointmentDto>>>
    
    @POST("api/policlinica/appointments")
    suspend fun createAppointment(@Body request: CreateAppointmentRequest): Response<ApiResponse<AppointmentDto>>
    
    @PUT("api/policlinica/appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: Int,
        @Body request: UpdateAppointmentRequest
    ): Response<ApiResponse<AppointmentDto>>
    
    @DELETE("api/policlinica/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    // ==================== ISTORIC MEDICAL ====================
    
    @GET("api/policlinica/medical-history/{pacientId}")
    suspend fun getMedicalHistory(@Path("pacientId") pacientId: Int): Response<ApiResponse<List<MedicalHistoryDto>>>
    
    @POST("api/policlinica/medical-history")
    suspend fun createMedicalHistory(@Body request: CreateMedicalHistoryRequest): Response<ApiResponse<MedicalHistoryDto>>
    
    // ==================== STATISTICI ====================
    
    @GET("api/policlinica/stats")
    suspend fun getStats(): Response<ApiResponse<StatsDto>>
    
    // ==================== UTILITĂȚI ====================
    
    @GET("/docs")
    suspend fun checkHealth(): Response<Unit>
}
