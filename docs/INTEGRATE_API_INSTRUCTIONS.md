# 🔄 Instrucțiuni: Integrare ViewModel în MainActivity

## 📍 Ce trebuie să modifici în MainActivity.kt

Deschide fișierul: `app/src/main/java/com/example/policlinica/MainActivity.kt`

### Pasul 1: Adaugă import-urile necesare

La linia 7 (după `enableEdgeToEdge`), adaugă:

```kotlin
import androidx.activity.viewModels
```

La linia 39 (după `PoliclinicaTheme`), adaugă:

```kotlin
import com.example.policlinica.viewmodel.MainViewModel
```

### Pasul 2: Modifică clasa MainActivity

Înlocuiește liniile 69-79 cu:

```kotlin
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
```

### Pasul 3: Modifică funcția MainApp

Înlocuiește liniile 81-132 cu:

```kotlin
@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    // Afișează mesaj de eroare dacă există
    viewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    if (!viewModel.isLoggedIn) {
        AuthScreenWithAPI(
            viewModel = viewModel
        )
    } else {
        DashboardScreenWithAPI(
            viewModel = viewModel
        )
    }
}
```

### Pasul 4: Adaugă ecranul de Auth cu API

Adaugă după funcția `AuthScreen` existentă (la linia 215):

```kotlin
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
```

### Pasul 5: Adaugă Dashboard cu API

Adaugă după funcția `DashboardScreen` existentă:

```kotlin
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
```

### Pasul 6: Adaugă funcțiile helper pentru Dashboard

```kotlin
@Composable
fun AdminDashboardContentWithAPI(viewModel: MainViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { 
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                SummaryCard("Total Programări", viewModel.appointments.size.toString(), Icons.Default.DateRange, Color(0xFF2196F3), Modifier.weight(1f)) {}
                SummaryCard("Doctori Activi", viewModel.doctors.size.toString(), Icons.Default.Face, Color(0xFF5C6BC0), Modifier.weight(1f)) {} 
            } 
        }
        item { FutureAppointments(viewModel.appointments, true, "") }
        item { AdminStatistics() }
    }
}

@Composable
fun PatientDashboardContentWithAPI(viewModel: MainViewModel, onMakeAppointmentClick: () -> Unit, onViewDoctorsClick: () -> Unit) {
    val context = LocalContext.current
    val pendingChanges = viewModel.appointments.filter { it.status == AppointmentStatus.PendingChange && it.patientName == viewModel.currentUserName }
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Bună ziua, ${viewModel.currentUserName}!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)) }
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
        item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))) { Column(modifier = Modifier.padding(20.dp)) { Text("Ai nevoie de o consultație?", color = Color.White, fontWeight = FontWeight.Bold); Button(onClick = onMakeAppointmentClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.padding(top = 8.dp)) { Text("Programează-te", color = Color(0xFF1E88E5)) } } } }
        item { PatientMedicalHistorySummary() }
    }
    
    val appToReschedule = appointmentToReschedule
    if (appToReschedule != null) {
        EditAppointmentDialog(appToReschedule, viewModel.appointments.toMutableList(), "Alege altă oră", { appointmentToReschedule = null }) { d, t ->
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
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(if(isPending) Color(0xFFFFFDE7) else Color.Transparent), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) { Text(app.date, fontWeight = FontWeight.Bold); Text(if(viewModel.currentUserRole == UserRole.Admin) app.patientName else app.doctorName, fontSize = 14.sp); if(isPending) Text("Așteaptă confirmare", color = Color.Orange, fontSize = 10.sp) }
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
                            Toast.makeText(LocalContext.current, "Doctor adăugat!", Toast.LENGTH_SHORT).show()
                        }
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
```

---

## ✅ Rezumat Modificări

| Ce | Unde |
|---|---|
| Adaugă import-uri | Liniile 8 și 40 |
| Modifică MainActivity | Liniile 69-79 |
| Modifică MainApp | Liniile 81-132 |
| Adaugă AuthScreenWithAPI | După linia 215 |
| Adaugă DashboardScreenWithAPI | După DashboardScreen |
| Adaugă funcții helper | La finalul fișierului |

---

## 🚀 După Modificări

1. **Sync Project** în Android Studio
2. **Rulează aplicația**
3. **Testează login** cu datele de pe server
4. **Verifică Logcat** pentru erori

---

## 🐛 Depistare Probleme

Dacă apare vreo eroare, trimite-mi:
1. Screenshot din Logcat
2. Ce ecran nu funcționează
3. Mesajul de eroare
