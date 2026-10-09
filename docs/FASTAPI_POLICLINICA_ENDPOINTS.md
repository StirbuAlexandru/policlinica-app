# 🏥 Cod FastAPI pentru Policlinica - Endpoint-uri Complete

## 📌 Instrucțiuni de Instalare pe Server

### Pasul 1: Creează tabelele în PostgreSQL
```bash
sudo -u postgres psql gestiune
```

Apoi rulează:
```sql
-- Tabela pentru doctori
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

-- Tabela pentru pacienți
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

-- Tabela pentru programări
CREATE TABLE IF NOT EXISTS programari (
    id SERIAL PRIMARY KEY,
    pacient_id INTEGER REFERENCES pacienti(id) ON DELETE CASCADE,
    doctor_id INTEGER REFERENCES doctori(id) ON DELETE CASCADE,
    data VARCHAR(20) NOT NULL,
    ora VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'confirmed', -- confirmed, pending_change, cancelled
    data_propusa VARCHAR(20),
    ora_propusa VARCHAR(10),
    motiv_anulare TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela pentru istoric medical
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

-- Indexuri pentru performanță
CREATE INDEX IF NOT EXISTS idx_programari_pacient ON programari(pacient_id);
CREATE INDEX IF NOT EXISTS idx_programari_doctor ON programari(doctor_id);
CREATE INDEX IF NOT EXISTS idx_programari_data ON programari(data);
CREATE INDEX IF NOT EXISTS idx_istoric_pacient ON istoric_medical(pacient_id);

-- Date de test (opțional)
INSERT INTO doctori (nume, specialitate, camera, telefon) VALUES
('Dr. Ionescu Maria', 'Cardiologie', '101', '0721123456'),
('Dr. Vasilescu Andrei', 'Dermatologie', '102', '0722345678'),
('Dr. Popa Elena', 'Pediatrie', '103', '0723456789'),
('Dr. Dumitrescu Cristian', 'Medicină Generală', '104', '0724567890')
ON CONFLICT DO NOTHING;

INSERT INTO pacienti (nume, email, telefon) VALUES
('Ion Popescu', 'ion@email.com', '0731123456'),
('Maria Ionescu', 'maria@email.com', '0732234567')
ON CONFLICT DO NOTHING;
```

### Pasul 2: Adaugă endpoint-urile în main.py

Adaugă următorul cod la sfârșitul fișierului `/home/caj/main.py`:

```python
# ═══════════════════════════════════════════════════════════
# POLICLINICA ENDPOINTS - Adaugă asta la final în main.py
# ═══════════════════════════════════════════════════════════

from pydantic import BaseModel
from typing import Optional, List

# ─── Modele pentru Policlinica ─────────────────────────────

class DoctorCreate(BaseModel):
    nume: str
    specialitate: str
    camera: Optional[str] = ""
    telefon: Optional[str] = ""
    email: Optional[str] = ""

class DoctorResponse(BaseModel):
    id: int
    nume: str
    specialitate: str
    camera: Optional[str]
    telefon: Optional[str]
    email: Optional[str]
    activ: bool

class PacientCreate(BaseModel):
    nume: str
    email: Optional[str] = ""
    telefon: Optional[str] = ""
    cnp: Optional[str] = ""
    data_nasterii: Optional[str] = ""
    adresa: Optional[str] = ""

class PacientResponse(BaseModel):
    id: int
    nume: str
    email: Optional[str]
    telefon: Optional[str]
    cnp: Optional[str]
    data_nasterii: Optional[str]
    adresa: Optional[str]

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

class ProgramareResponse(BaseModel):
    id: int
    pacient_id: int
    pacient_nume: str
    doctor_id: int
    doctor_nume: str
    data: str
    ora: str
    status: str
    data_propusa: Optional[str]
    ora_propusa: Optional[str]
    created_at: Optional[str]

class IstoricMedicalCreate(BaseModel):
    pacient_id: int
    doctor_id: int
    data_consultatiei: str
    diagnostic: str
    tratament: str
    observatii: Optional[str] = ""

class IstoricMedicalResponse(BaseModel):
    id: int
    pacient_id: int
    doctor_nume: str
    data_consultatiei: str
    diagnostic: str
    tratament: str
    observatii: Optional[str]

# ─── Helper Functions ──────────────────────────────────────

def format_doctor(row):
    return {
        "id": row["id"],
        "nume": row["nume"],
        "specialitate": row["specialitate"],
        "camera": row.get("camera", ""),
        "telefon": row.get("telefon", ""),
        "email": row.get("email", ""),
        "activ": row.get("activ", True)
    }

def format_pacient(row):
    return {
        "id": row["id"],
        "nume": row["nume"],
        "email": row.get("email", ""),
        "telefon": row.get("telefon", ""),
        "cnp": row.get("cnp", ""),
        "data_nasterii": row.get("data_nasterii", ""),
        "adresa": row.get("adresa", "")
    }

def format_programare(row):
    return {
        "id": row["id"],
        "pacient_id": row["pacient_id"],
        "pacient_nume": row.get("pacient_nume", ""),
        "doctor_id": row["doctor_id"],
        "doctor_nume": row.get("doctor_nume", ""),
        "data": row["data"],
        "ora": row["ora"],
        "status": row.get("status", "confirmed"),
        "data_propusa": row.get("data_propusa"),
        "ora_propusa": row.get("ora_propusa"),
        "created_at": str(row.get("created_at", ""))
    }

# ─── DOCTORI ENDPOINTS ─────────────────────────────────────

@app.get("/api/policlinica/doctors")
def get_doctors():
    """Get all doctors"""
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT * FROM doctori WHERE activ = TRUE ORDER BY nume")
        doctors = [format_doctor(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": doctors}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/doctors")
def create_doctor(doctor: DoctorCreate):
    """Create new doctor (admin only)"""
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO doctori (nume, specialitate, camera, telefon, email) VALUES (%s, %s, %s, %s, %s) RETURNING *",
            (doctor.nume, doctor.specialitate, doctor.camera, doctor.telefon, doctor.email)
        )
        new_doctor = format_doctor(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_doctor}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/api/policlinica/doctors/{doctor_id}")
def delete_doctor(doctor_id: int):
    """Delete doctor (admin only) - soft delete"""
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

# ─── PACIENTI ENDPOINTS ────────────────────────────────────

@app.get("/api/policlinica/patients")
def get_patients():
    """Get all patients"""
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT * FROM pacienti ORDER BY nume")
        patients = [format_pacient(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": patients}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/patients")
def create_patient(patient: PacientCreate):
    """Create new patient"""
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO pacienti (nume, email, telefon, cnp, data_nasterii, adresa) VALUES (%s, %s, %s, %s, %s, %s) RETURNING *",
            (patient.nume, patient.email, patient.telefon, patient.cnp, patient.data_nasterii, patient.adresa)
        )
        new_patient = format_pacient(cur.fetchone())
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": new_patient}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── PROGRAMĂRI ENDPOINTS ──────────────────────────────────

@app.get("/api/policlinica/appointments")
def get_appointments(pacient_id: Optional[int] = None, doctor_id: Optional[int] = None):
    """Get appointments - optionally filtered by patient or doctor"""
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
        
        appointments = [format_programare(row) for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": appointments}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/appointments")
def create_appointment(appt: ProgramareCreate):
    """Create new appointment"""
    try:
        # Verifică dacă ora e disponibilă
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
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
        
        # Get full data
        cur.execute("""
            SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
            FROM programari p
            JOIN pacienti pac ON p.pacient_id = pac.id
            JOIN doctori d ON p.doctor_id = d.id
            WHERE p.id = %s
        """, (cur.fetchone()["id"],))
        
        new_appt = format_programare(cur.fetchone())
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
    """Update appointment status or propose new time"""
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
            raise HTTPException(status_code=400, detail="Nicio actualizare specificată")
        
        values.append(appt_id)
        query = f"UPDATE programari SET {', '.join(updates)} WHERE id = %s RETURNING *"
        
        cur.execute(query, values)
        updated_appt = cur.fetchone()
        
        # Get full data
        cur.execute("""
            SELECT p.*, pac.nume as pacient_nume, d.nume as doctor_nume 
            FROM programari p
            JOIN pacienti pac ON p.pacient_id = pac.id
            JOIN doctori d ON p.doctor_id = d.id
            WHERE p.id = %s
        """, (appt_id,))
        
        result = format_programare(cur.fetchone())
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
    """Delete/cancel appointment"""
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

# ─── ISTORIC MEDICAL ENDPOINTS ─────────────────────────────

@app.get("/api/policlinica/medical-history/{pacient_id}")
def get_medical_history(pacient_id: int):
    """Get patient medical history"""
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
        history = [{
            "id": row["id"],
            "pacient_id": row["pacient_id"],
            "doctor_nume": row["doctor_nume"],
            "data_consultatiei": row["data_consultatiei"],
            "diagnostic": row["diagnostic"],
            "tratamente": row["tratamente"],
            "observatii": row.get("observatii", "")
        } for row in cur.fetchall()]
        cur.close()
        conn.close()
        return {"success": True, "data": history}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/policlinica/medical-history")
def create_medical_history(history: IstoricMedicalCreate):
    """Add medical record (doctor/admin only)"""
    try:
        conn = get_db_conn()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(
            "INSERT INTO istoric_medical (pacient_id, doctor_id, data_consultatiei, diagnostic, tratament, observatii) VALUES (%s, %s, %s, %s, %s, %s) RETURNING *",
            (history.pacient_id, history.doctor_id, history.data_consultatiei, history.diagnostic, history.tratament, history.observatii)
        )
        new_record = cur.fetchone()
        result = {
            "id": new_record["id"],
            "pacient_id": new_record["pacient_id"],
            "doctor_nume": "",
            "data_consultatiei": new_record["data_consultatiei"],
            "diagnostic": new_record["diagnostic"],
            "tratamente": new_record["tratamente"],
            "observatii": new_record.get("observatii", "")
        }
        conn.commit()
        cur.close()
        conn.close()
        return {"success": True, "data": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─── STATISTICI ENDPOINTS ──────────────────────────────────

@app.get("/api/policlinica/stats")
def get_stats():
    """Get clinic statistics"""
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
        upcoming_appointments = cur.fetchone()["total"]
        
        cur.close()
        conn.close()
        
        return {
            "success": True,
            "data": {
                "total_doctors": total_doctors,
                "total_appointments": total_appointments,
                "total_patients": total_patients,
                "upcoming_appointments": upcoming_appointments
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### Pasul 3: Repornește FastAPI
```bash
sudo systemctl restart fastapi
sudo systemctl status fastapi
```

### Pasul 4: Testează endpoint-urile
```bash
# Testează lista de doctori
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/doctors

# Testează lista de programări
curl https://registrational-jessenia-sleevelike.ngrok-free.dev/api/policlinica/appointments
```

---

## 📱 Cum Folosești în Android

După ce adaugi endpoint-urile pe server, voi actualiza `ApiService.kt` să pointeze la:
- `/api/policlinica/doctors`
- `/api/policlinica/appointments`
- `/api/policlinica/patients`
- etc.

---

## ⚠️ Important

1. **Nu șterge** codul existent din `main.py` - adaugă codul nou la final
2. **Backup** la `main.py` înainte de modificări: `cp /home/caj/main.py /home/caj/main.py.backup`
3. **Testează** fiecare endpoint după adăugare
