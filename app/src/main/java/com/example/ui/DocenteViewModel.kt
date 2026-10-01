package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DocenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DocenteDatabase.getDatabase(application)
    private val repository = DocenteRepository(db.docenteDao())
    private val interruptionMutex = Mutex()

    val teacher = repository.teacher.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allSubjects = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStudents = repository.allStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allInterruptions = repository.allInterruptions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedSubjectId = MutableStateFlow("sub_matematica")
    val selectedSubjectId: StateFlow<String> = _selectedSubjectId.asStateFlow()

    // Dynamically retrieve the students belonging to the active subject selection
    val filteredStudents: StateFlow<List<StudentEntity>> = _selectedSubjectId
        .combine(repository.allStudents) { id, list ->
            list.filter { it.subjectId == id }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _themeColor = MutableStateFlow("LIGHT") // "LIGHT" (default), "DARK", "BLUE SKY"
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    private val _fontSizeScale = MutableStateFlow("Mediano") // "Pequeño", "Mediano", "Grande"
    val fontSizeScale: StateFlow<String> = _fontSizeScale.asStateFlow()

    // Dynamically retrieve the active plannings based on selectedSubjectId
    val activePlannings: StateFlow<List<PlanningEntity>> = _selectedSubjectId
        .flatMapLatest { id -> repository.getPlanningsForSubject(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            // Seed sample database values on first launch so that the app starts beautifully
            repository.seedMockDataIfEmpty()
        }
    }

    fun selectSubject(subjectId: String) {
        _selectedSubjectId.value = subjectId
    }

    fun setThemeColor(color: String) {
        _themeColor.value = color
    }

    fun setFontSizeScale(scale: String) {
        _fontSizeScale.value = scale
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.insertTeacher(teacher)
        }
    }

    fun insertDailyPlanning(subjectId: String, unit: String, topic: String, activities: String) {
        viewModelScope.launch {
            val plannings = repository.getPlanningsForSubject(subjectId).firstOrNull() ?: emptyList()
            val filteredPlannings = plannings.filter { it.classOrder > 0 }
            val nextOrder = (filteredPlannings.maxOfOrNull { it.classOrder } ?: 0) + 1
            val newPlan = PlanningEntity(
                id = "plan_${subjectId}_${nextOrder}_${java.util.UUID.randomUUID().toString().take(4)}",
                subjectId = subjectId,
                classOrder = nextOrder,
                unit = unit,
                topic = topic,
                activities = activities,
                assignedDate = null,
                state = "PENDIENTE"
            )
            repository.insertPlannings(listOf(newPlan))
            repository.runCascadingReschedule(subjectId, LocalDate.of(2026, 6, 1))
        }
    }

    fun insertGeneralPlanning(subjectId: String, unit: String, topic: String, activities: String) {
        viewModelScope.launch {
            val plannings = repository.getPlanningsForSubject(subjectId).firstOrNull() ?: emptyList()
            val generalPlannings = plannings.filter { it.classOrder <= 0 }
            val nextOrder = (generalPlannings.minOfOrNull { it.classOrder } ?: 0) - 1
            val newPlan = PlanningEntity(
                id = "plan_general_${subjectId}_${nextOrder}_${java.util.UUID.randomUUID().toString().take(4)}",
                subjectId = subjectId,
                classOrder = nextOrder,
                unit = unit,
                topic = topic,
                activities = activities,
                assignedDate = null,
                state = "GENERAL"
            )
            repository.insertPlannings(listOf(newPlan))
        }
    }

    /**
     * Mark a class state ("PENDIENTE", "DICTADA", "NO_DICTADA").
     * This automatically triggers a cascade rescheduling starting from June 1st, 2026.
     */
    fun updatePlanningState(planning: PlanningEntity, newState: String) {
        viewModelScope.launch {
            val updated = planning.copy(state = newState)
            repository.updatePlanningState(updated)
            // Recalculate the entire cascading plan for this subject
            repository.runCascadingReschedule(planning.subjectId, LocalDate.of(2026, 6, 1))
        }
    }

    /**
     * Update planning details (unit, topic, activities) manually.
     */
    fun updatePlanningDetails(planning: PlanningEntity, newUnit: String, newTopic: String, newActivities: String) {
        viewModelScope.launch {
            val updated = planning.copy(unit = newUnit, topic = newTopic, activities = newActivities)
            repository.updatePlanningState(updated)
            // Recalculate the entire cascading plan for this subject
            repository.runCascadingReschedule(planning.subjectId, LocalDate.of(2026, 6, 1))
        }
    }

    /**
     * Toggle a specific date as an interruption.
     */
    fun toggleInterruption(date: LocalDate, description: String, type: String = "FERIADO") {
        viewModelScope.launch {
            interruptionMutex.withLock {
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val currentList = repository.allInterruptions.firstOrNull() ?: emptyList()
                val existing = currentList.find { it.date == dateStr }
                
                if (existing != null) {
                    repository.removeInterruption(dateStr)
                } else {
                    repository.addInterruption(InterruptionEntity(dateStr, description, type))
                }
                // Trigger cascading recalculation for the active subjects
                repository.runCascadingReschedule("sub_matematica", LocalDate.of(2026, 6, 1))
                repository.runCascadingReschedule("sub_sistemas", LocalDate.of(2026, 6, 1))
                repository.runCascadingReschedule("sub_programacion", LocalDate.of(2026, 6, 1))
            }
        }
    }

    /**
     * Import multiple interruptions atomically in one batch and reschedule at the end.
     */
    fun importMultipleInterruptions(list: List<Pair<LocalDate, String>>) {
        viewModelScope.launch {
            interruptionMutex.withLock {
                val currentList = repository.allInterruptions.firstOrNull() ?: emptyList()
                var anyAdded = false
                for ((date, description) in list) {
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val alreadyExists = currentList.any { it.date == dateStr }
                    if (!alreadyExists) {
                        repository.addInterruption(InterruptionEntity(dateStr, description, "FERIADO"))
                        anyAdded = true
                    }
                }
                if (anyAdded) {
                    // Trigger cascading recalculation for the active subjects once
                    repository.runCascadingReschedule("sub_matematica", LocalDate.of(2026, 6, 1))
                    repository.runCascadingReschedule("sub_sistemas", LocalDate.of(2026, 6, 1))
                    repository.runCascadingReschedule("sub_programacion", LocalDate.of(2026, 6, 1))
                }
            }
        }
    }

    /**
     * Registers a student attendance register via agile gestures.
     * Appends today's attendance records Offline-first.
     */
    fun toggleStudentAttendance(studentId: String, attended: Boolean) {
        viewModelScope.launch {
            val studentsStr = repository.allStudents.firstOrNull() ?: emptyList()
            val student = studentsStr.find { it.id == studentId } ?: return@launch
            
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val recordsMap = student.getAttendanceMap().toMutableMap()
            recordsMap[todayStr] = attended
            
            val serializedRecords = recordsMap.entries.joinToString(",") { "${it.key}:${if (it.value) "T" else "F"}" }
            val updated = student.copy(attendanceRecords = serializedRecords)
            repository.updateStudent(updated)
        }
    }

    /**
     * MASSIVE ATTENDANCE: Marks all students present for today to absolute save time.
     */
    fun applyMassiveAttendance(attended: Boolean = true) {
        viewModelScope.launch {
            val currentSelected = _selectedSubjectId.value
            val studentsStr = repository.allStudents.firstOrNull() ?: emptyList()
            val filtered = studentsStr.filter { it.subjectId == currentSelected }
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            val updatedStudents = filtered.map { student ->
                val recordsMap = student.getAttendanceMap().toMutableMap()
                recordsMap[todayStr] = attended
                val serializedRecords = recordsMap.entries.joinToString(",") { "${it.key}:${if (it.value) "T" else "F"}" }
                student.copy(attendanceRecords = serializedRecords)
            }
            repository.insertStudents(updatedStudents)
        }
    }

    /**
     * Import multiple student names (from CSV/XLSX) atomically into the selected course.
     */
    fun importClassNomina(names: List<String>) {
        viewModelScope.launch {
            val currentSelected = _selectedSubjectId.value
            val newStudents = names.map { name ->
                StudentEntity(
                    id = "stud_" + java.util.UUID.randomUUID().toString().take(6),
                    name = name,
                    attendanceRecords = "",
                    gradesRecord = "",
                    subjectId = currentSelected,
                    evaluationNote = "",
                    observation = ""
                )
            }
            repository.insertStudents(newStudents)
        }
    }

    /**
     * Instantly register a new student manually via a direct dialog.
     */
    fun addManualStudent(name: String) {
        viewModelScope.launch {
            val currentSelected = _selectedSubjectId.value
            val newStudent = StudentEntity(
                id = "stud_" + java.util.UUID.randomUUID().toString().take(6),
                name = name,
                attendanceRecords = "",
                gradesRecord = "",
                subjectId = currentSelected,
                evaluationNote = "",
                observation = ""
            )
            repository.insertStudents(listOf(newStudent))
        }
    }

    /**
     * Persist qualitative observations and quantitative evaluation score on the student index.
     */
    fun updateStudentDetails(studentId: String, evalNote: String, observationString: String) {
        viewModelScope.launch {
            val studentsList = repository.allStudents.firstOrNull() ?: emptyList()
            val student = studentsList.find { it.id == studentId } ?: return@launch
            val updated = student.copy(
                evaluationNote = evalNote,
                observation = observationString
            )
            repository.updateStudent(updated)
        }
    }
}
