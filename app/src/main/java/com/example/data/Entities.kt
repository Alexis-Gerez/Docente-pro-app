package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String,
    val name: String,
    val institution: String,
    val specialty: String,
    val avatarUrl: String
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val classroom: String,
    val teachingDaysOfWeek: String // Comma separated days. 1 = Monday, 7 = Sunday. e.g., "1,3" for Mon/Wed
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val attendanceRecords: String, // Comma separated T/F or date records. e.g., "2026-05-20:T,2026-05-22:F"
    val gradesRecord: String, // Comma separated grades. e.g., "7.5,8.0"
    val subjectId: String = "sub_matematica",
    val evaluationNote: String = "",
    val observation: String = ""
) {
    fun getAttendanceMap(): Map<String, Boolean> {
        if (attendanceRecords.isBlank()) return emptyMap()
        return attendanceRecords.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                parts[0] to (parts[1] == "T")
            } else null
        }.toMap()
    }

    fun getGradesMap(): Map<String, Double> {
        if (gradesRecord.isBlank()) return emptyMap()
        return gradesRecord.split(",").mapNotNull {
            if (it.contains(":")) {
                val pair = it.split(":")
                pair[0] to (pair[1].toDoubleOrNull() ?: 0.0)
            } else {
                // Treat unstructured records as generic grades
                "Nota" to (it.toDoubleOrNull() ?: 0.0)
            }
        }.toMap()
    }

    fun getAbsencesPercentage(): Double {
        val map = getAttendanceMap()
        if (map.isEmpty()) return 0.0
        val absences = map.values.count { !it }
        return (absences.toDouble() / map.size) * 100.0
    }

    fun getAttendancePercentage(): Double {
        val map = getAttendanceMap()
        if (map.isEmpty()) return 100.0
        val presents = map.values.count { it }
        return (presents.toDouble() / map.size) * 100.0
    }

    fun getGradesAverage(): Double {
        val map = getGradesMap()
        if (map.isEmpty()) return 0.0
        return map.values.average()
    }

    fun getAlertMessage(): String? {
        val attendance = getAttendancePercentage()
        val average = getGradesAverage()
        
        if (attendance < 75.0) return "Alerta: ${attendance.toInt()}% Asistencia"
        if (average > 0 && average < 6.0) return "Alerta: Promedio ${"%.1f".format(average)}"
        return null
    }

    fun hasEarlyWarningFlag(): Boolean {
        return getAlertMessage() != null
    }
}

@Entity(tableName = "plannings")
data class PlanningEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val classOrder: Int, // e.g. 1, 2, 3...
    val unit: String,
    val topic: String,
    val activities: String,
    val assignedDate: String?, // Format: yyyy-MM-dd
    val state: String // "PENDIENTE", "DICTADA", "NO_DICTADA"
)

@Entity(tableName = "interruptions")
data class InterruptionEntity(
    @PrimaryKey val date: String, // Format: yyyy-MM-dd
    val description: String,
    val type: String // "FERIADO", "PARO", "LICENCIA"
)
