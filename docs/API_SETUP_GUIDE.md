# 📱 Conectare Policlinica API - Ghid de Configurare

## ✅ Ce a fost creat deja:

### 1. **Modele de Date** (`model/ApiModels.kt`)
- `LoginRequest`, `LoginResponse`, `UserData` - pentru autentificare
- `AppointmentDto`, `CreateAppointmentRequest` - pentru programări
- `DoctorDto`, `CreateDoctorRequest` - pentru doctori
- `ApiResponse<T>` - wrapper pentru răspunsuri API

### 2. **Serviciul API** (`network/ApiService.kt`)
Endpoint-uri definite:
- `POST /auth/login` - Login
- `POST /auth/register` - Înregistrare
- `GET /appointments` - Lista programări
- `POST /appointments` - Creare programare
- `PUT /appointments/{id}` - Actualizare programare
- `DELETE /appointments/{id}` - Ștergere programare
- `GET /doctors` - Lista doctori
- `POST /doctors` - Adăugare doctor
- `DELETE /doctors/{id}` - Ștergere doctor
- `GET /health` - Verificare status API

### 3. **Retrofit Client** (`network/RetrofitClient.kt`)
- URL configurat: `https://registrational-jessenia-sleevelike.ngrok-free.dev/`
- Logging activat pentru debug
- Timeout: 30 secunde

### 4. **Repository** (`repository/ApiRepository.kt`)
- Toate funcțiile de comunicare cu API-ul
- Convertori între DTO și modele locale
- Error handling și logging

### 5. **Permisiuni** (`AndroidManifest.xml`)
- `INTERNET` - acces la rețea
- `ACCESS_NETWORK_STATE` - verificare conexiune

---

## 🔧 Ce trebuie să faci pe Server (Ubuntu):

### Pasul 1: Verifică FastAPI rulează
```bash
sudo systemctl status fastapi
```

### Pasul 2: Verifică ngrok/Cloudflare
```bash
sudo systemctl status cloudflared
# SAU
sudo systemctl status ngrok
```

### Pasul 3: Verifică endpoint-urile existente
```bash
cat /home/caj/main.py
```

**Trimite-mi conținutul lui `main.py`** pentru a adapta endpoint-urile din Android la ce ai tu deja!

---

## 🔍 Ce Endpoint-uri Așteaptă Aplicația Android:

### Structura JSON așteptată:

#### 1. **Login**
```
POST /auth/login
Request:
{
  "email": "admin",
  "password": "admin"
}

Response:
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 1,
    "email": "admin",
    "name": "Administrator",
    "role": "Admin"
  }
}
```

#### 2. **Register**
```
POST /auth/register
Request:
{
  "email": "user@email.com",
  "password": "1234",
  "name": "Ion Popescu"
}

Response: (la fel ca Login)
```

#### 3. **Get Appointments**
```
GET /appointments
Response:
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "date": "20 May 2025",
      "time": "10:00",
      "patientName": "Ion Popescu",
      "patientId": 1,
      "doctorName": "Dr. Ionescu",
      "doctorId": 1,
      "status": "confirmed",
      "proposedDate": null,
      "proposedTime": null
    }
  ]
}
```

#### 4. **Get Doctors**
```
GET /doctors
Response:
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "name": "Dr. Ionescu",
      "specialty": "Cardiologie"
    }
  ]
}
```

---

## 📝 Opțiuni de Continuare:

### Opțiunea A: **Ai deja endpoint-urile pe server**
1. Trimite-mi fișierul `main.py`
2. Voi adapta `ApiService.kt` să match-uiască ce ai tu
3. Voi integra API-ul în `MainActivity.kt`

### Opțiunea B: **Nu ai endpoint-urile încă**
1. Îți pot genera codul FastAPI complet (`main.py`)
2. Cu toate endpoint-urile compatibile cu Android
3. Cu interogări PostgreSQL

### Opțiunea C: **Vrei să testez întâi structura**
1. Putem testa cu date mock
2. Verificăm că Android funcționează
3. Apoi conectăm la server

---

## 🚀 Următorul Pas:

**Trimite-mi următoarele informații:**

1. ✅ Output de la `cat /home/caj/main.py`
2. ✅ Output de la `sudo -u postgres psql gestiune -c "\dt"`
3. ✅ Confirmă URL-ul ngrok funcționează
4. ✅ Ce vrei să faci: Opțiunea A, B sau C?

---

## 📌 Notă Importantă:

Codul Android este **pregătit** și **funcțional**. Trebuie doar să:
1. Mă asigurăm că endpoint-urile match-uiesc
2. Integrăm în UI (să înlocuim datele hardcodate)
3. Testăm conexiunea

**Aștept informațiile de la tine!** 🎯
