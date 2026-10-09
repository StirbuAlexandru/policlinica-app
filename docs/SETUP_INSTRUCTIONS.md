# 🚀 Ghid Complet: Conectare Policlinica Android + FastAPI + PostgreSQL

## 📋 Ce ai nevoie:

### ✅ Deja ai:
- Aplicație Android cu Retrofit configurat
- Server Ubuntu cu FastAPI + PostgreSQL
- Conexiune SSH funcțională

### ❌ Trebuie să faci:
1. Adaugă tabele noi în PostgreSQL (doctori, pacienți, programări)
2. Adaugă endpoint-uri noi în `main.py`
3. Repornește FastAPI
4. Testează conexiunea

---

## 🔧 PASUL 1: Conectează-te la server

```bash
ssh caj@100.80.39.59
```

---

## 🔧 PASUL 2: Creează tabelele în PostgreSQL

```bash
sudo -u postgres psql gestiune
```

Apoi copiază și lipește **tot** codul SQL de mai jos:

```sql
-- ═══════════════════════════════════════════════════════════
-- TABELE POLICLINICA
-- ═══════════════════════════════════════════════════════════

-- 1. Tabela Doctori
CREATE TABLE IF NOT EXISTS doctori (
    id SERIAL PRIMARY KEY,
    nume VARCHAR(200) NOT NULL,
    specialitate VARCHAR(100) NOT NULL,
    camera VARCHAR(20),
    telefon VARCHAR(20),
    email VARCHAR(100),
    activ BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela Pacienți
CREATE TABLE IF NOT EXISTS pacienti (
    id SERIAL PRIMARY KEY,
    nume VARCHAR(200) NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefon VARCHAR(20),
    cnp VARCHAR(13),
    data_nasterii VARCHAR(20),
    adresa TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela Programări
CREATE TABLE IF NOT EXISTS programari (
    id SERIAL PRIMARY KEY,
    pacient_id INTEGER REFERENCES pacienti(id) ON DELETE CASCADE,
    doctor_id INTEGER REFERENCES doctori(id) ON DELETE CASCADE,
    data VARCHAR(20) NOT NULL,
    ora VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'confirmed',
    data_propusa VARCHAR(20),
    ora_propusa VARCHAR(10),
    motiv_anulare TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabela Istoric Medical
CREATE TABLE IF NOT EXISTS istoric_medical (
    id SERIAL PRIMARY KEY,
    pacient_id INTEGER REFERENCES pacienti(id) ON DELETE CASCADE,
    doctor_id INTEGER REFERENCES doctori(id),
    data_consultatiei VARCHAR(20),
    diagnostic TEXT,
    tratament TEXT,
    observatii TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_programari_pacient ON programari(pacient_id);
CREATE INDEX IF NOT EXISTS idx_programari_doctor ON programari(doctor_id);
CREATE INDEX IF NOT EXISTS idx_programari_data ON programari(data);
CREATE INDEX IF NOT EXISTS idx_istoric_pacient ON istoric_medical(pacient_id);

-- ═══════════════════════════════════════════════════════════
-- DATE DE TEST (OPȚIONAL)
-- ═══════════════════════════════════════════════════════════

-- Doctori de test
INSERT INTO doctori (nume, specialitate, camera, telefon) VALUES
('Dr. Ionescu Maria', 'Cardiologie', '101', '0721123456'),
('Dr. Vasilescu Andrei', 'Dermatologie', '102', '0722345678'),
('Dr. Popa Elena', 'Pediatrie', '103', '0723456789'),
('Dr. Dumitrescu Cristian', 'Medicină Generală', '104', '0724567890')
ON CONFLICT DO NOTHING;

-- Pacienți de test
INSERT INTO pacienti (nume, email, telefon) VALUES
('Ion Popescu', 'ion@email.com', '0731123456'),
('Maria Ionescu', 'maria@email.com', '0732234567')
ON CONFLICT DO NOTHING;

-- Programări de test
INSERT INTO programari (pacient_id, doctor_id, data, ora, status) VALUES
(1, 1, '20 May 2025', '10:00', 'confirmed'),
(2, 2, '25 May 2025', '11:30', 'confirmed')
ON CONFLICT DO NOTHING;
```

Verifică că s-au creat:
```sql
\dt
SELECT COUNT(*) FROM doctori;
SELECT COUNT(*) FROM pacienti;
SELECT COUNT(*) FROM programari;
```

Ieși din PostgreSQL:
```sql
\q
```

---

## 🔧 PASUL 3: Adaugă endpoint-urile în main.py

### 3.1. Backup la fișierul existent:
```bash
cp /home/caj/main.py /home/caj/main.py.backup
```

### 3.2. Deschide fișierul pentru editare:
```bash
nano /home/caj/main.py
```

### 3.3. Mergi la **SFÂRȘITUL** fișierului și adaugă:

```python
# ═══════════════════════════════════════════════════════════
# POLICLINICA ENDPOINTS - Adăugat la final
# ═══════════════════════════════════════════════════════════

from pydantic import BaseModel
from typing import Optional, List

# ─── Modele Pydantic ─────────────────────────────────────

class DoctorCreate(BaseModel):
    nume: str
    specialitate: str
    camera: Optional[str] = ""
    telefon: Optional[str] = ""
    email: Optional[str] = ""

class PacientCreate(BaseModel):
    nume: str
    email: Optional[str] = ""
    telefon: Optional[str] = ""
    cnp: Optional[str] = ""
    data_nasterii: Optional[str] = ""
    adresa: Optional[str] = ""

class ProgramareCreate(BaseModel):
    pacient_id: int
    doctor_id: int
    data: str
    ora: str

class ProgramareUpdate(BaseModel):
    status: Optional[str] = None
    data_propusa: Optional[str] = None
    ora_propusa: Optional[str] = None
    motiv_anulare: Optional[str] = None

class IstoricMedicalCreate(BaseModel):
    pacient_id: int
    doctor_id: int
    data_consultatiei: str
    diagnostic: str
    tratament: str
    observatii: Optional[str] = ""

# ─── DOCTORI ─────────────────────────────────────────────

@app.get("/api/policlinica/doctors")
def get_doctors():
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT * FROM doctori WHERE activ = TRUE ORDER BY nume")
        doctors = [dict(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": doctors}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/doctors")
def create_doctor(doctor: DoctorCreate):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO doctori (nume, specialitate, camera, telefon, email) VALUES (%s, %s, %s, %s, %s) RETURNING *",
            (doctor.nume, doctor.specialitate, doctor.camera, doctor.telefon, doctor.email)
        )
        new_doctor = dict(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_doctor}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/api/policlinica/doctors/{doctor_id}")
def delete_doctor(doctor_id: int):
    try:
        conn = get_db_conn()
        cur = conn.cursor()
        cur.execute("UPDATE doctori SET activ = FALSE WHERE id = %s", (doctor_id,))
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "message": "Doctor șters"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── PACIENTI ────────────────────────────────────────────

@app.get("/api/policlinica/patients")
def get_patients():
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT * FROM pacienti ORDER BY nume")
        patients = [dict(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": patients}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/patients")
def create_patient(patient: PacientCreate):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO pacienti (nume, email, telefon, cnp, data_nasterii, adresa) VALUES (%s, %s, %s, %s, %s, %s) RETURNING *",
            (patient.nume, patient.email, patient.telefon, patient.cnp, patient.data_nasterii, patient.adresa)
        )
        new_patient = dict(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_patient}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── PROGRAMĂRI ──────────────────────────────────────────

@app.get("/api/policlinica/appointments")
def get_appointments(pacient_id: Optional[int] = None, doctor_id: Optional[int] = None):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        if pacient_id:
            cur.execute("""
                SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
                FROM programari p
                JOIN pacienti pac ON p.pacient_id = pac.id
                JOIN doctori d ON p.doctor_id = d.id
                WHERE p.pacient_id = %s 
                ORDER BY p.data DESC, p.ora DESC
            """, (pacient_id,))
        elif doctor_id:
            cur.execute("""
                SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
                FROM programari p
                JOIN pacienti pac ON p.pacient_id = pac.id
                JOIN doctori d ON p.doctor_id = d.id
                WHERE p.doctor_id = %s 
                ORDER BY p.data DESC, p.ora DESC
            """, (doctor_id,))
        else:
            cur.execute("""
                SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
                FROM programari p
                JOIN pacienti pac ON p.pacient_id = pac.id
                JOIN doctori d ON p.doctor_id = d.id
                ORDER BY p.data DESC, p.ora DESC
            """)
        
        appointments = [dict(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": appointments}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/appointments")
def create_appointment(appt: ProgramareCreate):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        # Verifică dacă ora e disponibilă
        cur.execute(
            "SELECT id FROM programari WHERE doctor_id = %s AND data = %s AND ora = %s AND status != 'cancelled'",
            (appt.doctor_id, appt.data, appt.ora)
        )
        if cur.fetchone():
            cur.close()
            conn.close()
            raise HTTPException(status_code=400, detail="Ora este deja ocupată")
        
        # Creare programare
        cur.execute(
            "INSERT INTO programari (pacient_id, doctor_id, data, ora) VALUES (%s, %s, %s, %s) RETURNING *",
            (appt.pacient_id, appt.doctor_id, appt.data, appt.ora)
        )
        new_id = cur.fetchone()["id"]
        
        # Get full data
        cur.execute("""
            SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
            FROM programari p
            JOIN pacienti pac ON p.pacient_id = pac.id
            JOIN doctori d ON p.doctor_id = d.id
            WHERE p.id = %s
        """, (new_id,))
        
        new_appt = dict(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_appt}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/api/policlinica/appointments/{appt_id}")
def update_appointment(appt_id: int, update: ProgramareUpdate):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        updates = []
        values = []
        
        if update.status is not None:
            updates.append("status = %s")
            values.append(update.status)
        if update.data_propusa is not None:
            updates.append("data_propusa = %s")
            values.append(update.data_propusa)
        if update.ora_propusa is not None:
            updates.append("ora_propusa = %s")
            values.append(update.ora_propusa)
        if update.motiv_anulare is not None:
            updates.append("motiv_anulare = %s")
            values.append(update.motiv_anulare)
        
        if not updates:
            raise HTTPException(status_code=400, detail="Nicio actualizare")
        
        values.append(appt_id)
        query = f"UPDATE programari SET {', '.join(updates)}, updated_at = NOW() WHERE id = %s RETURNING *"
        
        cur.execute(query, values)
        cur.fetchone()
        
        cur.execute("""
            SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
            FROM programari p
            JOIN pacienti pac ON p.pacient_id = pac.id
            JOIN doctori d ON p.doctor_id = d.id
            WHERE p.id = %s
        """, (appt_id,))
        
        result = dict(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": result}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/api/policlinica/appointments/{appt_id}")
def delete_appointment(appt_id: int):
    try:
        conn = get_db_conn()
        cur = conn.cursor()
        cur.execute("UPDATE programari SET status = 'cancelled' WHERE id = %s", (appt_id,))
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "message": "Programare anulată"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── ISTORIC MEDICAL ─────────────────────────────────────

@app.get("/api/policlinica/medical-history/{pacient_id}")
def get_medical_history(pacient_id: int):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("""
            SELECT h.*, d.nume as doctor_nume 
            FROM istoric_medical h
            JOIN doctori d ON h.doctor_id = d.id
            WHERE h.pacient_id = %s 
            ORDER BY h.data_consultatiei DESC
        """, (pacient_id,))
        history = [dict(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": history}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/medical-history")
def create_medical_history(history: IstoricMedicalCreate):
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO istoric_medical (pacient_id, doctor_id, data_consultatiei, diagnostic, tratament, observatii) VALUES (%s, %s, %s, %s, %s, %s) RETURNING *",
            (history.pacient_id, history.doctor_id, history.data_consultatiei, history.diagnostic, history.tratament, history.observatii)
        )
        new_record = dict(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_record}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── STATISTICI ──────────────────────────────────────────

@app.get("/api/policlinica/stats")
def get_stats():
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("SELECT COUNT(*) as total FROM doctori WHERE activ = TRUE")
        total_doctors = cur.fetchone()["total"]
        
        cur.execute("SELECT COUNT(*) as total FROM programari WHERE status = 'confirmed'")
        total_appointments = cur.fetchone()["total"]
        
        cur.execute("SELECT COUNT(*) as total FROM pacienti")
        total_patients = cur.fetchone()["total"]
        
        cur.execute("SELECT COUNT(*) as total FROM programari WHERE data >= CURRENT_DATE")
        upcoming = cur.fetchone()["total"]
        
        cur.close()
        conn.close()
        
        return {
            "success": True,
            "data": {
                "total_doctors": total_doctors,
                "total_appointments": total_appointments,
                "total_patients": total_patients,
                "upcoming_appointments": upcoming
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### 3.4. Salvează și ieși:
- `Ctrl + O` (salvează)
- `Enter` (confirmă)
- `Ctrl + X` (ieși)

---

## 🔧 PASUL 4: Repornește FastAPI

```bash
sudo systemctl restart fastapi
sudo systemctl status fastapi
```

Ar trebui să vezi `active (running)`.

---

## 🔧 PASUL 5: Testează endpoint-urile

```bash
# 1. Testează doctorii
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/doctors

# 2. Testează pacienții
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/patients

# 3. Testează programările
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/appointments

# 4. Testează statisticile
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/stats
```

Ar trebui să primești răspunsuri JSON cu `"success": true`.

---

## 🔧 PASUL 6: Testează din Android

1. Deschide Android Studio
2. Rulează aplicația
3. Verifică Logcat pentru erori de rețea

---

## 🐛 Depanare Probleme Comune

### Problemă: "Connection refused"
**Soluție:**
```bash
sudo systemctl status fastapi
sudo systemctl status cloudflared
```

### Problemă: "Table does not exist"
**Soluție:** Verifică în PostgreSQL:
```bash
sudo -u postgres psql gestiune -c "\dt"
```

### Problemă: "404 Not Found"
**Soluție:** Verifică URL-ul în `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://registrational-jessenia-sleevelike.ngrok-free.dev/"
```

### Problemă: "500 Internal Server Error"
**Soluție:** Verifică log-urile FastAPI:
```bash
sudo journalctl -u fastapi -n 50
```

---

## ✅ Checklist Final

- [ ] Tabele create în PostgreSQL
- [ ] Date de test inserate
- [ ] Endpoint-uri adăugate în `main.py`
- [ ] FastAPI repornit
- [ ] Endpoint-uri testate cu `curl`
- [ ] Aplicația Android rulează
- [ ] Datele se încarcă din API

---

## 📞 Ai Probleme?

Trimite-mi:
1. Output-ul de la `sudo journalctl -u fastapi -n 50`
2. Screenshot din Logcat Android
3. Ce endpoint nu funcționează

**Succes! 🎉**
