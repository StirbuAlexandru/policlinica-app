package com.example.policlinica.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.policlinica.Appointment
import com.example.policlinica.AppointmentStatus
import com.example.policlinica.Doctor
import com.example.policlinica.UserRole
import com.example.policlinica.model.UserData
import com.example.policlinica.repository.ApiRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ApiRepository()
    private val TAG = "MainViewModel"
    
    // State pentru autentificare
    var isLoggedIn by mutableStateOf(false)
        private set
    
    var currentUserRole by mutableStateOf(UserRole.None)
        private set
    
    var currentUserName by mutableStateOf("")
        private set
    
    var currentUserEmail by mutableStateOf("")
        private set
    
    var currentUserId by mutableStateOf(0)
        private set
    
    // State pentru date
    var appointments by mutableStateOf(listOf<Appointment>())
        private set
    
    var doctors by mutableStateOf(listOf<Doctor>())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // ==================== AUTENTIFICARE ====================
    
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            repository.login(email, password)
                .onSuccess { userData ->
                    Log.d(TAG, "Login success: ${userData.name}")
                    isLoggedIn = true
                    currentUserRole = repository.convertUserRole(userData.role)
                    currentUserName = userData.name
                    currentUserEmail = userData.email
                    currentUserId = userData.id
                    onSuccess()
                    loadData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Login failed: ${error.message}")
                    errorMessage = error.message ?: "Login failed"
                }
            
            isLoading = false
        }
    }
    
    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            // Notă: RegisterRequest din API așteaptă "company_name", nu "name"
            // Va trebui să adaptăm dacă e necesar
            repository.register(email, password, name)
                .onSuccess { userData ->
                    Log.d(TAG, "Register success: ${userData.name}")
                    isLoggedIn = true
                    currentUserRole = repository.convertUserRole(userData.role)
                    currentUserName = userData.name
                    currentUserEmail = userData.email
                    currentUserId = userData.id
                    onSuccess()
                    loadData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Register failed: ${error.message}")
                    errorMessage = error.message ?: "Register failed"
                }
            
            isLoading = false
        }
    }
    
    fun logout() {
        isLoggedIn = false
        currentUserRole = UserRole.None
        currentUserName = ""
        currentUserId = 0
        appointments = emptyList()
        doctors = emptyList()
    }
    
    // ==================== ÎNCĂRCARE DATE ====================
    
    private fun loadData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                // Încarcă doctori și programări în paralel
                val doctorsResult = repository.getDoctors()
                val appointmentsResult = repository.getAppointments()
                
                // Procesează doctorii
                doctorsResult.onSuccess { doctorDtos ->
                    doctors = doctorDtos.map { dto ->
                        repository.convertToDoctor(dto)
                    }
                    Log.d(TAG, "Loaded ${doctors.size} doctors")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load doctors: ${error.message}")
                    // Folosește date fallback dacă API-ul eșuează
                    doctors = getFallbackDoctors()
                }
                
                // Procesează programările
                appointmentsResult.onSuccess { appointmentDtos ->
                    appointments = appointmentDtos.map { dto ->
                        repository.convertToAppointment(dto)
                    }
                    Log.d(TAG, "Loaded ${appointments.size} appointments")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load appointments: ${error.message}")
                    // Folosește date fallback dacă API-ul eșuează
                    appointments = getFallbackAppointments()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Load data error: ${e.message}", e)
                errorMessage = e.message
                // Folosește date fallback
                doctors = getFallbackDoctors()
                appointments = getFallbackAppointments()
            } finally {
                isLoading = false
            }
        }
    }
    
    // ==================== PROGRAMĂRI ====================
    
    fun createAppointment(patientName: String, doctorName: String, date: String, time: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val doctor = doctors.find { it.name == doctorName }
            if (doctor == null) {
                errorMessage = "Doctor not found"
                return@launch
            }
            
            // Obține pacient_id din server
            val pacientIdResult = repository.getPatientIdByEmail(currentUserEmail)
            if (pacientIdResult.isFailure) {
                errorMessage = "Patient not found in database"
                return@launch
            }
            
            val pacientId = pacientIdResult.getOrNull() ?: 0
            
            repository.createAppointment(
                pacientId = pacientId,
                doctorId = doctor.id,
                data = date,
                ora = time
            ).onSuccess { dto ->
                Log.d(TAG, "Appointment created")
                loadData() // Reîncarcă datele
                onSuccess()
            }.onFailure { error ->
                Log.e(TAG, "Create appointment failed: ${error.message}")
                errorMessage = error.message
            }
        }
    }
    
    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment.id)
                .onSuccess {
                    Log.d(TAG, "Appointment deleted")
                    loadData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Delete appointment failed: ${error.message}")
                    errorMessage = error.message
                }
        }
    }
    
    fun updateAppointment(appointment: Appointment, newDate: String, newTime: String, isAdmin: Boolean) {
        viewModelScope.launch {
            val dto = com.example.policlinica.model.AppointmentDto(
                id = appointment.id,
                data = newDate,
                ora = newTime,
                pacient_nume = appointment.patientName,
                pacient_id = currentUserId,
                doctor_nume = appointment.doctorName,
                doctor_id = doctors.find { it.name == appointment.doctorName }?.id ?: 0,
                status = if (isAdmin) "pending_change" else "confirmed",
                data_propusa = if (isAdmin) newDate else null,
                ora_propusa = if (isAdmin) newTime else null
            )
            
            repository.updateAppointment(dto)
                .onSuccess {
                    Log.d(TAG, "Appointment updated")
                    loadData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Update appointment failed: ${error.message}")
                    errorMessage = error.message
                }
        }
    }
    
    // ==================== DOCTORI ====================
    
    fun createDoctor(name: String, specialty: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createDoctor(name, specialty)
                .onSuccess { dto ->
                    Log.d(TAG, "Doctor created")
                    loadData()
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "Create doctor failed: ${error.message}")
                    errorMessage = error.message
                }
        }
    }
    
    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            repository.deleteDoctor(doctor.id)
                .onSuccess {
                    Log.d(TAG, "Doctor deleted")
                    loadData()
                }
                .onFailure { error ->
                    Log.e(TAG, "Delete doctor failed: ${error.message}")
                    errorMessage = error.message
                }
        }
    }
    
    // ==================== DATE FALLBACK (pentru offline) ====================
    
    private fun getFallbackDoctors(): List<Doctor> {
        return listOf(
            Doctor(1, "Dr. Ionescu", "Cardiologie"),
            Doctor(2, "Dr. Vasilescu", "Dermatologie"),
            Doctor(3, "Dr. Popa", "Pediatrie")
        )
    }
    
    private fun getFallbackAppointments(): List<Appointment> {
        return listOf(
            Appointment(1, "20 May 2025", "10:00", "Ion Popescu", "Dr. Ionescu"),
            Appointment(2, "25 May 2025", "11:30", "Maria Ionescu", "Dr. Vasilescu")
        )
    }
    
    // ==================== REFRESH ====================
    
    fun refreshData() {
        if (isLoggedIn) {
            loadData()
        }
    }
}
