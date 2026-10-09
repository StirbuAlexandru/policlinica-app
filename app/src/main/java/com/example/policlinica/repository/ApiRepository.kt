package com.example.policlinica.repository

import android.util.Log
import com.example.policlinica.model.*
import com.example.policlinica.network.RetrofitClient
import com.example.policlinica.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    
    private val apiService = RetrofitClient.apiService
    private val TAG = "ApiRepository"
    
    // ==================== AUTENTIFICARE ====================
    
    suspend fun login(email: String, password: String): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                Log.d(TAG, "Login successful: role=${loginData.role}")
                
                // Construim UserData din răspunsul API
                val userData = UserData(
                    id = loginData.employee_id ?: loginData.firma_id ?: 0,
                    email = email,
                    name = email, // Numele îl putem lua mai târziu din profil
                    role = loginData.role
                )
                Result.success(userData)
            } else {
                Log.e(TAG, "Login failed: ${response.code()}")
                Result.failure(Exception("Email sau parola incorecta"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Result.failure(Exception("Email sau parola incorecta"))
        }
    }
    
    suspend fun register(email: String, password: String, name: String): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                Log.d(TAG, "Registration successful: role=${loginData.role}")
                
                val userData = UserData(
                    id = loginData.employee_id ?: loginData.firma_id ?: 0,
                    email = email,
                    name = name,
                    role = loginData.role
                )
                Result.success(userData)
            } else {
                Log.e(TAG, "Registration failed: ${response.code()}")
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPatientIdByEmail(email: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting patient ID for email: $email")
            val response = apiService.getPatientIdByEmail(email)
            Log.d(TAG, "Response code: ${response.code()}, success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "Response body: $body")
                val success = body["success"] as? Boolean
                if (success == true) {
                    val pacientIdDouble = body["pacient_id"] as? Double
                    val pacientId = pacientIdDouble?.toInt()
                    if (pacientId != null) {
                        Log.d(TAG, "Patient ID found: $pacientId")
                        Result.success(pacientId)
                    } else {
                        Log.e(TAG, "Patient ID is null in response")
                        Result.failure(Exception("Patient ID not found"))
                    }
                } else {
                    Log.e(TAG, "Success is false in response")
                    Result.failure(Exception("Patient not found"))
                }
            } else {
                Log.e(TAG, "Response not successful or body is null")
                Result.failure(Exception("Failed to get patient ID"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get patient ID error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== PROGRAMĂRI ====================
    
    suspend fun getAppointments(): Result<List<AppointmentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppointments()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load appointments"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get appointments error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPatientAppointments(patientId: Int): Result<List<AppointmentDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppointments(pacientId = patientId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load patient appointments"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get patient appointments error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createAppointment(
        pacientId: Int,
        doctorId: Int,
        data: String,
        ora: String
    ): Result<AppointmentDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createAppointment(
                CreateAppointmentRequest(pacientId, doctorId, data, ora)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create appointment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create appointment error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateAppointment(appointment: AppointmentDto): Result<AppointmentDto> = withContext(Dispatchers.IO) {
        try {
            val updateRequest = UpdateAppointmentRequest(
                status = appointment.status,
                proposedDate = appointment.data_propusa ?: appointment.proposedDate,
                proposedTime = appointment.ora_propusa ?: appointment.proposedTime
            )
            val response = apiService.updateAppointment(appointment.id, updateRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update appointment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update appointment error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteAppointment(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAppointment(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete appointment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete appointment error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== DOCTORI ====================
    
    suspend fun getDoctors(): Result<List<DoctorDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDoctors()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load doctors"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get doctors error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createDoctor(name: String, specialty: String): Result<DoctorDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateDoctorRequest(
                nume = name,
                specialitate = specialty,
                camera = "",
                telefon = "",
                email = ""
            )
            val response = apiService.createDoctor(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create doctor"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create doctor error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteDoctor(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteDoctor(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete doctor"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete doctor error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== PACIENTI ====================
    
    suspend fun getPatients(): Result<List<PatientDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPatients()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load patients"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get patients error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createPatient(patient: CreatePatientRequest): Result<PatientDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPatient(patient)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create patient"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create patient error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== ISTORIC MEDICAL ====================
    
    suspend fun getMedicalHistory(pacientId: Int): Result<List<MedicalHistoryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMedicalHistory(pacientId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load medical history"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get medical history error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createMedicalHistory(request: CreateMedicalHistoryRequest): Result<MedicalHistoryDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createMedicalHistory(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create medical record"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create medical history error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== STATISTICI ====================
    
    suspend fun getStats(): Result<StatsDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStats()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load stats"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get stats error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== UTILITĂȚI ====================
    
    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkHealth()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed: ${e.message}", e)
            false
        }
    }
    
    // ==================== CONVERTORI ====================
    
    fun convertUserRole(role: String): UserRole {
        return when (role.lowercase()) {
            "admin" -> UserRole.Admin
            "patient", "pacient" -> UserRole.Patient
            else -> UserRole.None
        }
    }
    
    fun convertToAppointment(dto: AppointmentDto): com.example.policlinica.Appointment {
        return com.example.policlinica.Appointment(
            id = dto.id,
            date = dto.data.ifEmpty { dto.date },
            time = dto.ora.ifEmpty { dto.time },
            patientName = dto.pacient_nume.ifEmpty { dto.patientName },
            doctorName = dto.doctor_nume.ifEmpty { dto.doctorName },
            status = if (dto.status == "pending_change") 
                com.example.policlinica.AppointmentStatus.PendingChange 
            else 
                com.example.policlinica.AppointmentStatus.Confirmed,
            proposedDate = dto.data_propusa ?: dto.proposedDate,
            proposedTime = dto.ora_propusa ?: dto.proposedTime
        )
    }
    
    fun convertToDoctor(dto: DoctorDto): com.example.policlinica.Doctor {
        return com.example.policlinica.Doctor(
            id = dto.id,
            name = dto.nume,
            specialty = dto.specialitate
        )
    }
}
