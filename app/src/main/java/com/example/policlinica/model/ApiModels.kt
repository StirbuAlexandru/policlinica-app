package com.example.policlinica.model

// Modele pentru autentificare
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val firma_id: Int?,
    val role: String,
    val employee_id: Int?
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class UserData(
    val id: Int,
    val email: String,
    val name: String,
    val role: String // "Admin" sau "Patient"
)

// Modele pentru programări
data class AppointmentDto(
    val id: Int,
    val data: String = "",
    val ora: String = "",
    val pacient_nume: String = "",
    val pacient_id: Int = 0,
    val doctor_nume: String = "",
    val doctor_id: Int = 0,
    val status: String = "confirmed",
    val data_propusa: String? = null,
    val ora_propusa: String? = null,
    val motiv_anulare: String? = null,
    val created_at: String = "",
    val updated_at: String = "",
    // Aliasuri pentru compatibilitate cu codul vechi
    val date: String = "",
    val time: String = "",
    val patientName: String = "",
    val patientId: Int = 0,
    val doctorName: String = "",
    val doctorId: Int = 0,
    val proposedDate: String? = null,
    val proposedTime: String? = null
)

data class CreateAppointmentRequest(
    val pacient_id: Int,
    val doctor_id: Int,
    val data: String,
    val ora: String
)

// Modele pentru doctori
data class DoctorDto(
    val id: Int,
    val nume: String = "",
    val specialitate: String = "",
    val camera: String = "",
    val telefon: String = "",
    val email: String = "",
    val activ: Boolean = true,
    val created_at: String = ""
)

data class CreateDoctorRequest(
    val nume: String,
    val specialitate: String,
    val camera: String = "",
    val telefon: String = "",
    val email: String = ""
)

// Modele pentru răspunsuri generale
data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T?
)

// Modele pentru pacienți
data class PatientDto(
    val id: Int,
    val nume: String,
    val email: String = "",
    val telefon: String = "",
    val cnp: String = "",
    val dataNasterii: String = "",
    val adresa: String = ""
)

data class CreatePatientRequest(
    val nume: String,
    val email: String = "",
    val telefon: String = "",
    val cnp: String = "",
    val dataNasterii: String = "",
    val adresa: String = ""
)

// Modele pentru actualizare programare
data class UpdateAppointmentRequest(
    val status: String? = null,
    val proposedDate: String? = null,
    val proposedTime: String? = null,
    val date: String? = null,
    val time: String? = null
)

// Modele pentru istoric medical
data class MedicalHistoryDto(
    val id: Int,
    val pacientId: Int,
    val doctorNume: String,
    val dataConsultatiei: String,
    val diagnostic: String,
    val tratamente: String,
    val observatii: String = ""
)

data class CreateMedicalHistoryRequest(
    val pacientId: Int,
    val doctorId: Int,
    val dataConsultatiei: String,
    val diagnostic: String,
    val tratament: String,
    val observatii: String = ""
)

// Modele pentru statistici
data class StatsDto(
    val totalDoctors: Int,
    val totalAppointments: Int,
    val totalPatients: Int,
    val upcomingAppointments: Int
)
