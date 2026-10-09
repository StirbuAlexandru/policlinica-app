# ═══════════════════════════════════════════════════════════
# ADAUGĂ ACEST COD LA SFÂRȘITUL fișierului /home/caj/main.py
# ═══════════════════════════════════════════════════════════

from pydantic import BaseModel
from typing import Optional, List

# ─── Modele Pydantic pentru Policlinica ───────────────────

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

# ─── DOCTORI ENDPOINTS ────────────────────────────────────

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

# ─── PACIENTI ENDPOINTS ───────────────────────────────────

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

# ─── PROGRAMĂRI ENDPOINTS ─────────────────────────────────

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

# ─── ISTORIC MEDICAL ENDPOINTS ────────────────────────────

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

# ─── STATISTICI ENDPOINTS ─────────────────────────────────

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
