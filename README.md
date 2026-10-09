# Policlinica – Clinic Appointment App

Android app for managing a medical clinic: **patients** book appointments with the doctor of their choice and see their medical history, while **administrators** manage doctors, appointments and clinic statistics.

Built in **Kotlin with Jetpack Compose**, following the **MVVM** architecture, and connected to a **FastAPI + PostgreSQL** backend.

## Features

### For patients
- Register and log in
- Book an appointment with a chosen doctor, at an available date and time
- View and manage upcoming appointments
- **My history** – medical history with diagnoses and treatments
- Dashboard with recommended doctors, health tips and recent activity

### For administrators
- Admin dashboard with clinic **statistics** and an **appointments chart**
- **Forecast** section for upcoming appointment load
- Manage **doctors** (add / remove) and **patients**
- View, edit and delete **appointments**
- Add entries to patients' **medical history**

### UI
- Modern Material 3 interface built entirely with Jetpack Compose
- Splash screen, sidebar navigation and role-based dashboards (admin / patient)

## Architecture

```
UI (Jetpack Compose screens)
        │
        ▼
MainViewModel  ──  state & business logic
        │
        ▼
ApiRepository  ──  data access, error handling (Kotlin coroutines)
        │
        ▼
Retrofit + OkHttp  ──  REST API
        │
        ▼
FastAPI backend  ──  PostgreSQL (doctors, patients, appointments, medical history)
```

## Tech stack

| Layer | Technologies |
|---|---|
| **Android** | Kotlin, Jetpack Compose, Material 3, ViewModel (MVVM), Kotlin Coroutines |
| **Networking** | Retrofit, OkHttp (logging interceptor), Gson |
| **Backend** | Python, FastAPI |
| **Database** | PostgreSQL |

Android: min SDK 26, target SDK 36.

## Project structure

```
app/src/main/java/com/example/policlinica/
├── MainActivity.kt        Screens and navigation (Compose)
├── viewmodel/             MainViewModel
├── repository/            ApiRepository
├── network/               Retrofit client and API service
├── model/                 API data models
└── ui/                    Dashboard components, charts and theme
backend/                   FastAPI endpoints for the clinic
docs/                      Database schema, API endpoints and setup guides
```

## Running the app

1. Open the project in Android Studio.
2. Set the backend address in `BASE_URL` in `network/RetrofitClient.kt`.
3. Run the `app` module on a device or emulator.

The database schema and the full list of API endpoints are documented in [`docs/FASTAPI_POLICLINICA_ENDPOINTS.md`](docs/FASTAPI_POLICLINICA_ENDPOINTS.md).

## Author

**Alexandru Știrbu** – Junior Frontend Developer
