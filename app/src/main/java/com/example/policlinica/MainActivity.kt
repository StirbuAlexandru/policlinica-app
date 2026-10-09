package com.example.policlinica

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.policlinica.ui.SplashScreen
import com.example.policlinica.ui.DashboardStatsCard
import com.example.policlinica.ui.InfoBanner
import com.example.policlinica.ui.AppointmentsChart
import com.example.policlinica.ui.RecommendedDoctorsSection
import com.example.policlinica.ui.RecentActivitySection
import com.example.policlinica.ui.ForecastSection
import com.example.policlinica.ui.HealthTipsSection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.policlinica.ui.theme.PoliclinicaTheme
import com.example.policlinica.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// --- MODELE DE DATE ---

enum class UserRole { Admin, Patient, None }
enum class AuthMode { Login, Register }
enum class Screen { Dashboard, Appointments, Doctors, MyHistory }
enum class AppointmentStatus { Confirmed, PendingChange }

data class Appointment(
    val id: Int,
    var date: String = "",
    var time: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    var status: AppointmentStatus = AppointmentStatus.Confirmed,
    var proposedDate: String? = null,
    var proposedTime: String? = null
)

data class Doctor(val id: Int, val name: String, val specialty: String)
data class User(val email: String, val password: String, val name: String, val role: UserRole)

// --- ACTIVITATEA PRINCIPALĂ ---

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoliclinicaTheme {
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    
    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }
    
    // Afișează mesaje de eroare dacă există
    viewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    if (!viewModel.isLoggedIn) {
        AuthScreenWithAPI(viewModel = viewModel)
    } else {
        DashboardScreenWithAPI(viewModel = viewModel)
    }
}

// --- ECRANE AUTENTIFICARE ---

@Composable
fun AuthScreen(
    authMode: AuthMode,
    users: SnapshotStateList<User>,
    onAuthSuccess: (UserRole, String) -> Unit,
    onSwitchMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Favorite, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("POLICLINICA MEDICA", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Text(if (authMode == AuthMode.Login) "Autentificare" else "Creează cont", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                if (authMode == AuthMode.Register) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nume complet") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) })
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("Parolă") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    leadingIcon = { Icon(Icons.Default.Lock, null) }, 
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (authMode == AuthMode.Login) {
                            val user = users.find { it.email == email && it.password == password }
                            if (user != null) onAuthSuccess(user.role, user.name)
                            else Toast.makeText(context, "Email sau parolă incorectă", Toast.LENGTH_SHORT).show()
                        } else {
                            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                                if (users.any { it.email == email }) {
                                    Toast.makeText(context, "Email existent", Toast.LENGTH_SHORT).show()
                                } else {
                                    users.add(User(email, password, name, UserRole.Patient))
                                    onAuthSuccess(UserRole.Patient, name)
                                }
                            } else Toast.makeText(context, "Completați toate câmpurile", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (authMode == AuthMode.Login) "Intră în cont" else "Înregistrare")
                }

                TextButton(onClick = onSwitchMode) {
                    Text(if (authMode == AuthMode.Login) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
                }
            }
        }
    }
}

// --- LOGICĂ VALIDARE ---

fun canPatientModify(dateString: String): Boolean {
    return try {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        val appointmentDate = LocalDate.parse(dateString, formatter)
        ChronoUnit.DAYS.between(LocalDate.now(), appointmentDate) >= 7
    } catch (e: Exception) { true }
}

@Composable
fun DashboardScreen(role: UserRole, userName: String, appointments: SnapshotStateList<Appointment>, doctors: SnapshotStateList<Doctor>, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp), drawerContainerColor = Color(0xFF2C6BBF), drawerShape = RoundedCornerShape(0.dp)) {
                SidebarContent(role, userName, currentScreen, { currentScreen = it; scope.launch { drawerState.close() } }, onLogout)
            }
        }
    ) {
        Scaffold(
            topBar = { TopBar(title = when(currentScreen) { Screen.Dashboard -> "ACASĂ"; Screen.Appointments -> if (role == UserRole.Admin) "TOATE PROGRAMĂRILE" else "PROGRAMĂRILE MELE"; else -> currentScreen.name.uppercase() }, onMenuClick = { scope.launch { drawerState.open() } }) },
            containerColor = Color(0xFFF0F7FF)
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.Dashboard -> if (role == UserRole.Admin) AdminDashboardContent(appointments) else PatientDashboardContent(userName, appointments, doctors, { currentScreen = Screen.Appointments }, { currentScreen = Screen.Doctors })
                    Screen.Appointments -> AppointmentsScreen(appointments, doctors, userName, role)
                    Screen.Doctors -> DoctorsScreen(doctors, appointments, role)
                    Screen.MyHistory -> MyHistoryScreen(userName, appointments)
                }
            }
        }
    }
}

@Composable
fun SidebarContent(role: UserRole, userName: String, currentScreen: Screen, onScreenSelected: (Screen) -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFF1E88E5)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Text("POLICLINICA MEDICA", color = Color.White, fontWeight = FontWeight.Bold)
                Text(userName, color = Color.White.copy(0.9f), fontSize = 14.sp)
                Text(if (role == UserRole.Admin) "ADMIN" else "PACIENT", color = Color.White.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SidebarItem(Icons.Default.Home, "Acasă", currentScreen == Screen.Dashboard) { onScreenSelected(Screen.Dashboard) }
        SidebarItem(Icons.Default.DateRange, if(role == UserRole.Admin) "Programări" else "Programările Mele", currentScreen == Screen.Appointments) { onScreenSelected(Screen.Appointments) }
        SidebarItem(Icons.Default.Person, if(role == UserRole.Admin) "Doctori" else "Echipa Medicală", currentScreen == Screen.Doctors) { onScreenSelected(Screen.Doctors) }
        if (role == UserRole.Patient) SidebarItem(Icons.AutoMirrored.Filled.List, "Istoric Medical", currentScreen == Screen.MyHistory) { onScreenSelected(Screen.MyHistory) }
        Spacer(modifier = Modifier.weight(1f))
        SidebarItem(Icons.AutoMirrored.Filled.ExitToApp, "Deconectare", false) { onLogout() }
    }
}

@Composable
fun AdminDashboardContent(appointments: SnapshotStateList<Appointment>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { SummaryCard("Total Programări", appointments.size.toString(), Icons.Default.DateRange, Color(0xFF2196F3), Modifier.weight(1f)) {}; SummaryCard("Doctori Activi", "18", Icons.Default.Face, Color(0xFF5C6BC0), Modifier.weight(1f)) {} } }
        item { FutureAppointments(appointments, true, "") }
        item { AdminStatistics() }
    }
}

@Composable
fun AdminDashboardContentWithAPI(viewModel: MainViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { 
            Text("Bună ziua, ${viewModel.currentUserName}!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        }
        item { 
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                DashboardStatsCard(
                    icon = "📅",
                    title = "Total Programări",
                    value = viewModel.appointments.size.toString(),
                    gradientColors = listOf(Color(0xFF2196F3), Color(0xFF1976D2)),
                    modifier = Modifier.weight(1f)
                )
                DashboardStatsCard(
                    icon = "👨‍⚕️",
                    title = "Doctori Activi",
                    value = viewModel.doctors.size.toString(),
                    gradientColors = listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)),
                    modifier = Modifier.weight(1f)
                )
            } 
        }
        item {
            InfoBanner(
                icon = "💡",
                title = "Sfat Zilnic",
                message = "Verificați programările zilnice și confirmați modificările rapid!"
            )
        }
        item { AppointmentsChart() }
        item { RecommendedDoctorsSection() }
        item { RecentActivitySection() }
        item { ForecastSection() }
        item { FutureAppointments(viewModel.appointments, true, "") }
        item { HealthTipsSection() }
    }
}

@Composable
fun PatientDashboardContent(userName: String, appointments: SnapshotStateList<Appointment>, doctors: List<Doctor>, onMakeAppointmentClick: () -> Unit, onViewDoctorsClick: () -> Unit) {
    val context = LocalContext.current
    val pendingChanges = appointments.filter { it.status == AppointmentStatus.PendingChange && it.patientName == userName }
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Bună ziua, $userName!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)) }
        items(pendingChanges) { app ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Modificare Programare", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    Text("Admin propune: ${app.proposedDate} - ${app.proposedTime}", fontSize = 14.sp)
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { 
                            val index = appointments.indexOf(app)
                            if (index != -1) appointments[index] = app.copy(date = app.proposedDate!!, time = app.proposedTime!!, status = AppointmentStatus.Confirmed, proposedDate = null, proposedTime = null)
                            Toast.makeText(context, "Confirmat!", Toast.LENGTH_SHORT).show() 
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("Acord") }
                        Button(onClick = { appointmentToReschedule = app }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Refuz") }
                    }
                }
            }
        }
        item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))) { Column(modifier = Modifier.padding(20.dp)) { Text("Ai nevoie de o consultație?", color = Color.White, fontWeight = FontWeight.Bold); Button(onClick = onMakeAppointmentClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.padding(top = 8.dp)) { Text("Programează-te", color = Color(0xFF1E88E5)) } } } }
        item { PatientMedicalHistorySummary() }
    }
    val appToReschedule = appointmentToReschedule
    if (appToReschedule != null) {
        EditAppointmentDialog(appToReschedule, appointments, "Alege altă oră", { appointmentToReschedule = null }) { d, t ->
            val index = appointments.indexOf(appToReschedule)
            if (index != -1) appointments[index] = appToReschedule.copy(date = d, time = t, status = AppointmentStatus.Confirmed, proposedDate = null, proposedTime = null)
            appointmentToReschedule = null
        }
    }
}

@Composable
fun AppointmentsScreen(appointments: SnapshotStateList<Appointment>, doctors: List<Doctor>, userName: String, role: UserRole) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (role == UserRole.Admin) "Toate Programările" else "Programările Mele", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    val filteredApps = if(role == UserRole.Admin) appointments else appointments.filter { it.patientName == userName }
                    items(filteredApps) { app ->
                        val isPending = app.status == AppointmentStatus.PendingChange
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(if(isPending) Color(0xFFFFFDE7) else Color.Transparent), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) { Text(app.date, fontWeight = FontWeight.Bold); Text(if(role == UserRole.Admin) app.patientName else app.doctorName, fontSize = 14.sp); if(isPending) Text("Așteaptă confirmare", color = Color(0xFFFF9800), fontSize = 10.sp) }
                            Row {
                                IconButton(onClick = { if(role == UserRole.Admin || canPatientModify(app.date)) appointmentToEdit = app else Toast.makeText(context, "Minim 7 zile înainte!", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Default.Edit, null, tint = Color.Blue) }
                                IconButton(onClick = { if(role == UserRole.Admin || canPatientModify(app.date)) appointments.remove(app) else Toast.makeText(context, "Minim 7 zile înainte!", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                    if (filteredApps.isEmpty()) item { Text("Nicio programare.", modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray) }
                }
            }
        }
        if (role == UserRole.Patient) FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = Color(0xFF1E88E5)) { Icon(Icons.Default.Add, null, tint = Color.White) }
    }
    if (showAddDialog) AddAppointmentDialog(doctors, appointments, userName, { showAddDialog = false }) { p, d, date, t -> appointments.add(Appointment(appointments.size+1, date, t, p, d)) }
    val appToEdit = appointmentToEdit
    if (appToEdit != null) {
        EditAppointmentDialog(appToEdit, appointments, if (role == UserRole.Admin) "Propune o nouă oră" else "Modifică ora", { appointmentToEdit = null }) { d, t ->
            val index = appointments.indexOf(appToEdit)
            if (index != -1) { if (role == UserRole.Admin) appointments[index] = appToEdit.copy(proposedDate = d, proposedTime = t, status = AppointmentStatus.PendingChange) else appointments[index] = appToEdit.copy(date = d, time = t) }
            appointmentToEdit = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialog(doctors: List<Doctor>, appointments: SnapshotStateList<Appointment>, userName: String, onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    val context = LocalContext.current
    var dName by remember { mutableStateOf(if(doctors.isNotEmpty()) doctors[0].name else "") }
    var date by remember { mutableStateOf("26 May 2025") }
    var time by remember { mutableStateOf("10:00") }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Programare Nouă") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) { OutlinedTextField(value = dName, onValueChange = {}, readOnly = true, label = { Text("Doctor") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()); ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { doctors.forEach { d -> DropdownMenuItem(text = { Text(d.name) }, onClick = { dName = d.name; expanded = false }) } } }; OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Data (ex: 20 May 2025)") }); OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Ora") }) } }, confirmButton = { Button(onClick = { if (appointments.any { it.doctorName == dName && it.date == date && it.time == time }) Toast.makeText(context, "Ora ocupată!", Toast.LENGTH_LONG).show() else { onConfirm(userName, dName, date, time); onDismiss() } }) { Text("Confirmă") } })
}

@Composable
fun EditAppointmentDialog(appointment: Appointment, appointments: List<Appointment>, title: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    val context = LocalContext.current
    var d by remember { mutableStateOf(appointment.proposedDate ?: appointment.date) }
    var t by remember { mutableStateOf(appointment.proposedTime ?: appointment.time) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Column { OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("Data") }); OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("Ora") }) } }, confirmButton = { Button(onClick = { if (appointments.any { it.id != appointment.id && it.doctorName == appointment.doctorName && it.date == d && it.time == t }) Toast.makeText(context, "Ora ocupată!", Toast.LENGTH_SHORT).show() else onConfirm(d, t) }) { Text("Salvează") } })
}

@Composable
fun DoctorsScreen(doctors: MutableList<Doctor>, appointments: List<Appointment>, role: UserRole) {
    var selectedDoc by remember { mutableStateOf<Doctor?>(null) }
    var showAddDocDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) { Column(modifier = Modifier.padding(16.dp)) { Text(if(role == UserRole.Admin) "Gestiune Doctori" else "Echipa Medicală", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); LazyColumn(modifier = Modifier.padding(top = 16.dp)) { items(doctors) { d -> Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedDoc = d }) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color(0xFF1E88E5)) }; Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(d.name, fontWeight = FontWeight.Bold); Text(d.specialty, color = Color.Gray, fontSize = 12.sp) }; if(role == UserRole.Admin) IconButton(onClick = { doctors.remove(d) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) } } } } } } ; if (role == UserRole.Admin) FloatingActionButton(onClick = { showAddDocDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = Color(0xFF1E88E5)) { Icon(Icons.Default.Add, null, tint = Color.White) } }
    if (showAddDocDialog) { var name by remember { mutableStateOf("") }; var spec by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = { showAddDocDialog = false }, title = { Text("Adaugă Doctor") }, text = { Column { OutlinedTextField(name, {name = it}, label = {Text("Nume")}); OutlinedTextField(spec, {spec = it}, label = {Text("Specializare")}) } }, confirmButton = { Button({ if(name.isNotBlank()) doctors.add(Doctor(doctors.size+1, name, spec)); showAddDocDialog = false }) { Text("Adaugă") } }) }
    val currentDoc = selectedDoc
    if (currentDoc != null) { val docApps = appointments.filter { it.doctorName == currentDoc.name }; AlertDialog(onDismissRequest = { selectedDoc = null }, title = { Text("Programări ${currentDoc.name}") }, text = { LazyColumn { items(docApps) { Text("${it.date} - ${it.patientName}") }; if(docApps.isEmpty()) item { Text("Nicio programare.") } } }, confirmButton = { Button(onClick = { selectedDoc = null }) { Text("Închide") } }) }
}

@Composable
fun MyHistoryScreen(userName: String, appointments: List<Appointment>) { Column(modifier = Modifier.padding(16.dp)) { Text("Istoricul Meu Medical", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); Spacer(modifier = Modifier.height(16.dp)); Card(modifier = Modifier.fillMaxWidth()) { LazyColumn(modifier = Modifier.padding(16.dp)) { item { Text("Programări Finalizate", fontWeight = FontWeight.Bold, color = Color.Gray) }; val pastApps = appointments.filter { it.patientName == userName }; items(pastApps) { app -> Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Box(modifier = Modifier.size(10.dp).align(Alignment.CenterVertically).background(Color.Green, CircleShape)); Spacer(modifier = Modifier.width(12.dp)); Column { Text(app.date, fontWeight = FontWeight.Bold); Text("Control la ${app.doctorName}", fontSize = 14.sp) } } ; HorizontalDivider(color = Color(0xFFF5F5F5)) }; if (pastApps.isEmpty()) item { Text("Nu aveți istoric medical.", modifier = Modifier.padding(top = 8.dp), color = Color.Gray) } } } } }

@Composable fun TopBar(title: String, onMenuClick: () -> Unit) { Surface(shadowElevation = 4.dp, color = Color.White) { Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF1565C0)) }; Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), modifier = Modifier.padding(start = 8.dp)) } } }
@Composable fun SidebarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) { NavigationDrawerItem(icon = { Icon(icon, null, tint = Color.White) }, label = { Text(label, color = Color.White) }, selected = selected, onClick = onClick, colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = Color(0x33FFFFFF)), shape = RoundedCornerShape(0.dp)) }
@Composable fun SummaryCard(title: String, value: String, icon: ImageVector, accentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) { Card(onClick = onClick, modifier = modifier.height(100.dp)) { Column(modifier = Modifier.fillMaxSize()) { Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(accentColor)); Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = accentColor); Text(text = title, fontSize = 11.sp, color = Color.Gray); Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold) } } } }
@Composable fun FutureAppointments(apps: List<Appointment>, isAdmin: Boolean, userName: String) { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.padding(16.dp)) { Text("Programări Recente", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); val filtered = if(isAdmin) apps else apps.filter { it.patientName == userName }; filtered.takeLast(3).forEach { app -> Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(app.date, modifier = Modifier.weight(1f)); Text(if(isAdmin) app.patientName else app.doctorName, modifier = Modifier.weight(1f)) } } } } }
@Composable fun AdminStatistics() { Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.padding(16.dp)) { Text("Activitate Săptămânală", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); Row(modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) { listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f).forEach { f -> Box(modifier = Modifier.width(20.dp).fillMaxHeight(f).background(Color.Blue, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))) } } } } }
@Composable fun AdminDoctorActivity() { Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.padding(16.dp)) { Text("Ocupare Cabinete", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); Canvas(modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 8.dp)) { val p = Path().apply { moveTo(0f, size.height*0.7f); lineTo(size.width*0.2f, size.height*0.4f); lineTo(size.width*0.5f, size.height*0.8f); lineTo(size.width, size.height*0.3f) }; drawPath(p, Color.Blue, style = Stroke(4f)) } } } }
@Composable fun PatientMedicalHistorySummary() { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.padding(16.dp)) { Text("Istoric Medical Recent", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)); Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(10.dp).background(Color.Green, CircleShape)); Text("Analize Sânge - 2024", modifier = Modifier.padding(start = 12.dp).weight(1f)); Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) } } } }
@Composable fun PlaceholderScreen(n: String) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulul $n") } }

// ==================== FUNCȚII NOI CU API ====================

@Composable
fun AuthScreenWithAPI(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(AuthMode.Login) }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Favorite, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("POLICLINICA MEDICA", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Text(if (authMode == AuthMode.Login) "Autentificare" else "Creează cont", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                if (authMode == AuthMode.Register) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nume complet") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) })
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("Parolă") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    leadingIcon = { Icon(Icons.Default.Lock, null) }, 
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (viewModel.isLoading) return@Button
                        
                        if (authMode == AuthMode.Login) {
                            viewModel.login(email, password) {
                                Toast.makeText(context, "Autentificare reușită!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                                viewModel.register(email, password, name) {
                                    Toast.makeText(context, "Înregistrare reușită!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Completați toate câmpurile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (authMode == AuthMode.Login) "Intră în cont" else "Înregistrare")
                    }
                }

                TextButton(onClick = { authMode = if (authMode == AuthMode.Login) AuthMode.Register else AuthMode.Login }) {
                    Text(if (authMode == AuthMode.Login) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
                }
            }
        }
    }
}

@Composable
fun DashboardScreenWithAPI(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp), drawerContainerColor = Color(0xFF2C6BBF), drawerShape = RoundedCornerShape(0.dp)) {
                SidebarContent(viewModel.currentUserRole, viewModel.currentUserName, currentScreen, { currentScreen = it; scope.launch { drawerState.close() } }, { viewModel.logout() })
            }
        }
    ) {
        Scaffold(
            topBar = { TopBar(title = when(currentScreen) { Screen.Dashboard -> "ACASĂ"; Screen.Appointments -> if (viewModel.currentUserRole == UserRole.Admin) "TOATE PROGRAMĂRILE" else "PROGRAMĂRILE MELE"; else -> currentScreen.name.uppercase() }, onMenuClick = { scope.launch { drawerState.open() } }) },
            containerColor = Color(0xFFF0F7FF)
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.Dashboard -> {
                        if (viewModel.isLoading) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (viewModel.currentUserRole == UserRole.Admin) {
                                AdminDashboardContentWithAPI(viewModel)
                            } else {
                                PatientDashboardContentWithAPI(viewModel, { currentScreen = Screen.Appointments }, { currentScreen = Screen.Doctors })
                            }
                        }
                    }
                    Screen.Appointments -> AppointmentsScreenWithAPI(viewModel)
                    Screen.Doctors -> DoctorsScreenWithAPI(viewModel)
                    Screen.MyHistory -> MyHistoryScreen(viewModel.currentUserName, viewModel.appointments)
                }
            }
        }
    }
}

@Composable
fun PatientDashboardContentWithAPI(viewModel: MainViewModel, onMakeAppointmentClick: () -> Unit, onViewDoctorsClick: () -> Unit) {
    val context = LocalContext.current
    val pendingChanges = viewModel.appointments.filter { it.status == AppointmentStatus.PendingChange && it.patientName == viewModel.currentUserName }
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }
    val myAppointments = viewModel.appointments.filter { it.patientName == viewModel.currentUserName }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Bună ziua, ${viewModel.currentUserName}!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatsCard(
                    icon = "📋",
                    title = "Programări",
                    value = myAppointments.size.toString(),
                    gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C)),
                    modifier = Modifier.weight(1f)
                )
                DashboardStatsCard(
                    icon = "⚠️",
                    title = "În Așteptare",
                    value = pendingChanges.size.toString(),
                    gradientColors = listOf(Color(0xFFFF9800), Color(0xFFF57C00)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            InfoBanner(
                icon = "ℹ️",
                title = "Informații Utile",
                message = "Puteți programa o consultație nouă sau modifica programările existente!"
            )
        }
        item { AppointmentsChart() }
        item { RecommendedDoctorsSection() }
        item { 
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))) { 
                Column(modifier = Modifier.padding(20.dp)) { 
                    Text("Ai nevoie de o consultație?", color = Color.White, fontWeight = FontWeight.Bold)
                    Button(onClick = onMakeAppointmentClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.padding(top = 8.dp)) { 
                        Text("Programează-te", color = Color(0xFF1E88E5)) 
                    } 
                } 
            } 
        }
        item { HealthTipsSection() }
        items(pendingChanges) { app ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Modificare Programare", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    Text("Admin propune: ${app.proposedDate} - ${app.proposedTime}", fontSize = 14.sp)
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { 
                            viewModel.updateAppointment(app, app.proposedDate!!, app.proposedTime!!, isAdmin = false)
                            Toast.makeText(context, "Confirmat!", Toast.LENGTH_SHORT).show() 
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("Acord") }
                        Button(onClick = { appointmentToReschedule = app }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Refuz") }
                    }
                }
            }
        }
        item { RecentActivitySection() }
        item { ForecastSection() }
        item { PatientMedicalHistorySummary() }
    }
    
    val appToReschedule = appointmentToReschedule
    if (appToReschedule != null) {
        EditAppointmentDialog(appToReschedule, viewModel.appointments, "Alege altă oră", { appointmentToReschedule = null }) { d, t ->
            viewModel.updateAppointment(appToReschedule, d, t, isAdmin = false)
            appointmentToReschedule = null
        }
    }
}

@Composable
fun AppointmentsScreenWithAPI(viewModel: MainViewModel) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (viewModel.currentUserRole == UserRole.Admin) "Toate Programările" else "Programările Mele", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    val filteredApps = if(viewModel.currentUserRole == UserRole.Admin) viewModel.appointments else viewModel.appointments.filter { it.patientName == viewModel.currentUserName }
                    items(filteredApps) { app ->
                        val isPending = app.status == AppointmentStatus.PendingChange
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(if(isPending) Color(0xFFFFDE7) else Color.Transparent), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) { Text(app.date, fontWeight = FontWeight.Bold); Text(if(viewModel.currentUserRole == UserRole.Admin) app.patientName else app.doctorName, fontSize = 14.sp); if(isPending) Text("Așteaptă confirmare", color = Color(0xFFFF9800), fontSize = 10.sp) }
                            Row {
                                IconButton(onClick = { if(viewModel.currentUserRole == UserRole.Admin || canPatientModify(app.date)) appointmentToEdit = app else Toast.makeText(context, "Minim 7 zile înainte!", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Default.Edit, null, tint = Color.Blue) }
                                IconButton(onClick = { viewModel.deleteAppointment(app) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                    if (filteredApps.isEmpty()) item { Text("Nicio programare.", modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray) }
                }
            }
        }
        if (viewModel.currentUserRole == UserRole.Patient) FloatingActionButton(onClick = { showAddDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = Color(0xFF1E88E5)) { Icon(Icons.Default.Add, null, tint = Color.White) }
    }
    
    if (showAddDialog) {
        AddAppointmentDialogWithAPI(viewModel.doctors, viewModel, { showAddDialog = false })
    }
    
    val appToEdit = appointmentToEdit
    if (appToEdit != null) {
        EditAppointmentDialog(appToEdit, viewModel.appointments, if (viewModel.currentUserRole == UserRole.Admin) "Propune o nouă oră" else "Modifică ora", { appointmentToEdit = null }) { d, t ->
            viewModel.updateAppointment(appToEdit, d, t, isAdmin = viewModel.currentUserRole == UserRole.Admin)
            appointmentToEdit = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialogWithAPI(doctors: List<Doctor>, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var dName by remember { mutableStateOf(if(doctors.isNotEmpty()) doctors[0].name else "") }
    var date by remember { mutableStateOf("26 May 2025") }
    var time by remember { mutableStateOf("10:00") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("Programare Nouă") }, 
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { 
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) { 
                    OutlinedTextField(value = dName, onValueChange = {}, readOnly = true, label = { Text("Doctor") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { 
                        doctors.forEach { d -> 
                            DropdownMenuItem(text = { Text(d.name) }, onClick = { dName = d.name; expanded = false }) 
                        } 
                    } 
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Data (ex: 20 May 2025)") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Ora") }) 
            } 
        }, 
        confirmButton = { 
            Button(onClick = { 
                viewModel.createAppointment(viewModel.currentUserName, dName, date, time) {
                    onDismiss()
                    Toast.makeText(context, "Programare creată!", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Confirmă") } 
        }
    )
}

@Composable
fun DoctorsScreenWithAPI(viewModel: MainViewModel) {
    var selectedDoc by remember { mutableStateOf<Doctor?>(null) }
    var showAddDocDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) { 
        Column(modifier = Modifier.padding(16.dp)) { 
            Text(if(viewModel.currentUserRole == UserRole.Admin) "Gestiune Doctori" else "Echipa Medicală", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) { 
                items(viewModel.doctors) { d -> 
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedDoc = d }) { 
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) { 
                                Icon(Icons.Default.Person, null, tint = Color(0xFF1E88E5)) 
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) { 
                                Text(d.name, fontWeight = FontWeight.Bold)
                                Text(d.specialty, color = Color.Gray, fontSize = 12.sp) 
                            }
                            if(viewModel.currentUserRole == UserRole.Admin) {
                                IconButton(onClick = { viewModel.deleteDoctor(d) }) { 
                                    Icon(Icons.Default.Delete, null, tint = Color.Red) 
                                }
                            }
                        } 
                    } 
                } 
            } 
        }
        if (viewModel.currentUserRole == UserRole.Admin) {
            FloatingActionButton(onClick = { showAddDocDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), containerColor = Color(0xFF1E88E5)) { 
                Icon(Icons.Default.Add, null, tint = Color.White) 
            }
        }
    }
    
    if (showAddDocDialog) { 
        var name by remember { mutableStateOf("") }
        var spec by remember { mutableStateOf("") }
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false }, 
            title = { Text("Adaugă Doctor") }, 
            text = { 
                Column { 
                    OutlinedTextField(name, {name = it}, label = {Text("Nume")})
                    OutlinedTextField(spec, {spec = it}, label = {Text("Specializare")})
                } 
            }, 
            confirmButton = { 
                Button({ 
                    if(name.isNotBlank()) {
                        viewModel.createDoctor(name, spec) {
                            showAddDocDialog = false
                        }
                        Toast.makeText(context, "Doctor adăugat!", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Adaugă") } 
            }
        ) 
    }
    
    val currentDoc = selectedDoc
    if (currentDoc != null) { 
        val docApps = viewModel.appointments.filter { it.doctorName == currentDoc.name }
        AlertDialog(
            onDismissRequest = { selectedDoc = null }, 
            title = { Text("Programări ${currentDoc.name}") }, 
            text = { 
                LazyColumn { 
                    items(docApps) { Text("${it.date} - ${it.patientName}") }
                    if(docApps.isEmpty()) item { Text("Nicio programare.") } 
                } 
            }, 
            confirmButton = { Button(onClick = { selectedDoc = null }) { Text("Închide") } }
        ) 
    }
}

