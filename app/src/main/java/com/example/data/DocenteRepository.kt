package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DocenteRepository(private val docenteDao: DocenteDao) {

    val teacher: Flow<TeacherEntity?> = docenteDao.getTeacher()
    val allSubjects: Flow<List<SubjectEntity>> = docenteDao.getAllSubjects()
    val allStudents: Flow<List<StudentEntity>> = docenteDao.getAllStudents()
    val allInterruptions: Flow<List<InterruptionEntity>> = docenteDao.getAllInterruptions()

    fun getPlanningsForSubject(subjectId: String): Flow<List<PlanningEntity>> {
        return docenteDao.getPlanningsForSubject(subjectId)
    }

    suspend fun insertTeacher(teacher: TeacherEntity) = docenteDao.insertTeacher(teacher)
    suspend fun insertSubject(subject: SubjectEntity) = docenteDao.insertSubject(subject)
    suspend fun insertStudents(students: List<StudentEntity>) = docenteDao.insertStudents(students)
    suspend fun updateStudent(student: StudentEntity) = docenteDao.insertStudent(student)
    
    suspend fun addInterruption(interruption: InterruptionEntity) {
        docenteDao.insertInterruption(interruption)
    }

    suspend fun removeInterruption(dateString: String) {
        docenteDao.deleteInterruption(dateString)
    }

    suspend fun updatePlanningState(planning: PlanningEntity) {
        docenteDao.updatePlanning(planning)
    }

    suspend fun insertPlannings(plannings: List<PlanningEntity>) {
        docenteDao.insertPlannings(plannings)
    }

    /**
     * ALGORITMO DE REPROGRAMACIÓN EN CASCADA (KOTLIN)
     * 
     * Implementa la lógica descrita para reescribir las fechas asignadas de las clases pendientes
     * omitiendo:
     * 1. Fines de semana no programados para la materia.
     * 2. Fechas de interrupciones (Feriados, Paros, etc.).
     * 3. Fechas en las cuales ya existen clases grabadas e inmutables del historial ("DICTADA").
     * 
     * Si el docente actualiza el estado o añade una interrupción, todo el itinerario se corre en cascada.
     */
    suspend fun runCascadingReschedule(subjectId: String, startLocalDate: LocalDate) = withContext(Dispatchers.IO) {
        Log.d("DocenteRepository", "Starting cascading reschedule for subject: $subjectId from $startLocalDate")
        val subject = docenteDao.getSubjectById(subjectId) ?: return@withContext
        val plannings = docenteDao.getPlanningsForSubjectList(subjectId).filter { it.classOrder > 0 }
        val interruptions = docenteDao.getAllInterruptionsList()
        
        // Parse teaching days. Monday=1, Sunday=7
        val teachingDays = subject.teachingDaysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { DayOfWeek.of(it) }
            .toSet()

        if (teachingDays.isEmpty()) return@withContext

        val interruptionDates = interruptions.map { LocalDate.parse(it.date) }.toSet()
        
        // Identify lock dates (where classes have already been lectured successfully)
        val lockedDates = plannings
            .filter { it.state == "DICTADA" && it.assignedDate != null }
            .map { LocalDate.parse(it.assignedDate) }
            .toSet()

        val updatedPlannings = mutableListOf<PlanningEntity>()
        var dateCursor = startLocalDate

        for (planning in plannings) {
            // If class is DICTADA, its historic date is sealed
            if (planning.state == "DICTADA") {
                updatedPlannings.add(planning)
                planning.assignedDate?.let {
                    val assignedLoc = LocalDate.parse(it)
                    if (assignedLoc.isAfter(dateCursor) || assignedLoc.isEqual(dateCursor)) {
                        dateCursor = assignedLoc.plusDays(1)
                    }
                }
                continue
            }

            // Find next valid day for this pending class card
            var assignedDate: LocalDate? = null
            var searchCursor = dateCursor
            var loopCount = 0

            while (assignedDate == null && loopCount < 500) {
                loopCount++
                val dayOfWeek = searchCursor.dayOfWeek
                val isTeachingDay = teachingDays.contains(dayOfWeek)
                val isInterrupted = interruptionDates.contains(searchCursor)
                val isOccupiedByHistory = lockedDates.contains(searchCursor)

                if (isTeachingDay && !isInterrupted && !isOccupiedByHistory) {
                    // Check if this specific planning was marked as "NO_DICTADA" on its expected day.
                    // If so, the day is considered discarded for learning, and we push the content search cursor forward.
                    if (planning.state == "NO_DICTADA") {
                        // Mark this class as having occurred (historically recorded as not dictated in this slot),
                        // and shift actual subsequent topics forward.
                        assignedDate = searchCursor
                        break
                    }
                    
                    assignedDate = searchCursor
                } else {
                    searchCursor = searchCursor.plusDays(1)
                }
            }

            if (assignedDate == null) {
                // Safeguard default: if we hit the limit, just assign current searchCursor
                assignedDate = searchCursor
            }

            updatedPlannings.add(
                planning.copy(
                    assignedDate = assignedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    state = if (planning.state == "NO_DICTADA") "NO_DICTADA" else "PENDIENTE"
                )
            )

            // Next class will start searching from at least the next day
            dateCursor = assignedDate.plusDays(1)
        }

        // Save back all updated dates in cascade
        docenteDao.insertPlannings(updatedPlannings)
        Log.d("DocenteRepository", "Completed cascading reschedule for subject: $subjectId")
    }

    /**
     * Seeds friendly demo data so the app starts fully populated, demonstrating all features.
     */
    suspend fun seedMockDataIfEmpty() {
        val currentTeacher = docenteDao.getTeacher().firstOrNull()
        if (currentTeacher == null) {
            Log.d("DocenteRepository", "Seeding initial mock data to Database...")
            
            // 1. Seed Teacher
            val teacher = TeacherEntity(
                id = "prof_alejandro",
                name = "Prof. Alejandro Gómez",
                institution = "E.T. N° 12 Libertador Gral. San Martín",
                specialty = "Análisis Matemático & Sistemas Inteligentes de Control",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"
            )
            docenteDao.insertTeacher(teacher)

            // 2. Seed Subjects
            val specSubject1 = SubjectEntity(
                id = "sub_sistemas",
                name = "4to Año - Sistemas de Control",
                classroom = "Aula 302 - Laboratorio de Robótica",
                teachingDaysOfWeek = "2,4" // Martes y Jueves
            )
            val specSubject2 = SubjectEntity(
                id = "sub_matematica",
                name = "5to Año - Matemática Aplicada",
                classroom = "Aula 405 (Tercer Piso)",
                teachingDaysOfWeek = "1,3" // Lunes y Miércoles
            )
            val specSubject3 = SubjectEntity(
                id = "sub_programacion",
                name = "6to Año - Taller de Robótica",
                classroom = "Laboratorio III",
                teachingDaysOfWeek = "5" // Viernes
            )
            docenteDao.insertSubject(specSubject1)
            docenteDao.insertSubject(specSubject2)
            docenteDao.insertSubject(specSubject3)

            // 3. Seed Students
            // Record formats: Date string: T (Present) or F (Absent)
            val students = listOf(
                StudentEntity(
                    id = "stud_1",
                    name = "Sofía Rodríguez",
                    attendanceRecords = "2026-05-18:T,2026-05-20:F,2026-05-25:F,2026-05-27:F", // 75% absences (3/4)
                    gradesRecord = "7.5,8.0,6.5",
                    subjectId = "sub_matematica"
                ),
                StudentEntity(
                    id = "stud_2",
                    name = "Lucas Benítez",
                    attendanceRecords = "2026-05-18:T,2026-05-20:T,2026-05-25:T,2026-05-27:T",
                    gradesRecord = "4.2,5.0,4.8", // Average: 4.67 (< 6)
                    subjectId = "sub_matematica"
                ),
                StudentEntity(
                    id = "stud_3",
                    name = "Mateo Álvarez",
                    attendanceRecords = "2026-05-18:F,2026-05-20:F,2026-05-25:T", // 66% absences (2/3)
                    gradesRecord = "5.5,5.0", // Average: 5.25 (< 6)
                    subjectId = "sub_matematica"
                ),
                StudentEntity(
                    id = "stud_4",
                    name = "Camila Sosa",
                    attendanceRecords = "2026-05-18:T,2026-05-20:T,2026-05-25:T,2026-05-27:T",
                    gradesRecord = "8.5,9.0,9.5",
                    subjectId = "sub_sistemas"
                ),
                StudentEntity(
                    id = "stud_5",
                    name = "Juan Pérez",
                    attendanceRecords = "2026-05-18:T,2026-05-20:T,2026-05-25:T,2026-05-27:T",
                    gradesRecord = "6.5,7.0,6.8",
                    subjectId = "sub_sistemas"
                ),
                StudentEntity(
                    id = "stud_6",
                    name = "María Delgado",
                    attendanceRecords = "2026-05-18:F,2026-05-20:F,2026-05-25:T,2026-05-27:F", // High Absences
                    gradesRecord = "4.0,5.0,4.5", // Low Grades average 4.5
                    subjectId = "sub_sistemas"
                ),
                StudentEntity(
                    id = "stud_7",
                    name = "Esteban Quito",
                    attendanceRecords = "2026-05-18:T,2026-05-20:T,2026-05-25:F,2026-05-27:T",
                    gradesRecord = "6.0,6.0,6.5",
                    subjectId = "sub_programacion"
                ),
                StudentEntity(
                    id = "stud_8",
                    name = "Alejandro Flores",
                    attendanceRecords = "2026-05-19:T,2026-05-22:F,2026-05-26:F", // Warnings
                    gradesRecord = "4.5,5.2,5.5",
                    subjectId = "sub_programacion"
                ),
                StudentEntity(
                    id = "stud_9",
                    name = "Lucía Fernández",
                    attendanceRecords = "2026-05-19:T,2026-05-22:T,2026-05-26:T",
                    gradesRecord = "9.0,8.5,10.0",
                    subjectId = "sub_programacion"
                )
            )
            docenteDao.insertStudents(students)

            // 4. Seed Interruptions
            val interruptions = listOf(
                InterruptionEntity("2026-06-08", "Día del Colegio - Feriado Institucional", "FERIADO"),
                InterruptionEntity("2026-06-15", "Paro Nacional de Docentes", "PARO"),
                InterruptionEntity("2026-06-25", "Licencia Médica por Congreso", "LICENCIA")
            )
            interruptions.forEach { docenteDao.insertInterruption(it) }

            // 5. Seed Plannings for Matemática Aplicada
            val mainPlanningsMat = listOf(
                PlanningEntity("mat_clase_1", "sub_matematica", 1, "Unidad I: EEDD", "Ecuaciones Diferenciales Ordinarias", "Modelado de llenado de tanques", "2026-06-01", "DICTADA"),
                PlanningEntity("mat_clase_2", "sub_matematica", 2, "Unidad I: EEDD", "Separación de Variables", "Resolución de problemas de frontera", "2026-06-03", "DICTADA"),
                PlanningEntity("mat_clase_3", "sub_matematica", 3, "Unidad I: EEDD", "Sistemas Lineales de Primer Orden", "Análisis capacitivo eléctrico", null, "PENDIENTE"),
                PlanningEntity("mat_clase_4", "sub_matematica", 4, "Unidad II: Laplace", "Transformada de Laplace", "Conversión algebraica de EEDD", null, "PENDIENTE"),
                PlanningEntity("mat_clase_5", "sub_matematica", 5, "Unidad II: Laplace", "Teoremas de Traslación", "Resolución de funciones de pulso", null, "PENDIENTE"),
                PlanningEntity("mat_clase_6", "sub_matematica", 6, "Unidad II: Laplace", "Convolución e Integración", "Uso del teorema de convolución", null, "PENDIENTE"),
                PlanningEntity("mat_clase_7", "sub_matematica", 7, "Unidad III: Fourier", "Series Trigonométricas de Fourier", "Aproximaciones harmónicas en simulación", null, "PENDIENTE"),
                PlanningEntity("mat_clase_8", "sub_matematica", 8, "Unidad III: Fourier", "Evaluación Integradora Escrita 1", "Examen individual presencial", null, "PENDIENTE")
            )
            docenteDao.insertPlannings(mainPlanningsMat)

            // 6. Seed Plannings for Sistemas de Control
            val mainPlanningsSis = listOf(
                PlanningEntity("sis_clase_1", "sub_sistemas", 1, "Unidad I: Modelado", "Conceptos de Lazo Abierto y Cerrado", "Discusión grupal de ejemplos cotidianos", "2026-06-02", "DICTADA"),
                PlanningEntity("sis_clase_2", "sub_sistemas", 2, "Unidad I: Modelado", "Función de Transferencia", "Análisis algebraico en pizarrón", null, "PENDIENTE"),
                PlanningEntity("sis_clase_3", "sub_sistemas", 3, "Unidad I: Modelado", "Diagramas de Bloques y Reducción", "Reducción de sistemas complejos", null, "PENDIENTE"),
                PlanningEntity("sis_clase_4", "sub_sistemas", 4, "Unidad II: Estabilidad", "Criterio de Routh-Hurwitz", "Cálculo de ganancias estables para motores", null, "PENDIENTE"),
                PlanningEntity("sis_clase_5", "sub_sistemas", 5, "Unidad II: Estabilidad", "Lugar Geométrico de las Raíces", "Trazado a mano alzada y verificación", null, "PENDIENTE"),
                PlanningEntity("sis_clase_6", "sub_sistemas", 6, "Unidad III: Controladores", "Control PID Analógico", "Sintonización analgésica proporcional", null, "PENDIENTE"),
                PlanningEntity("sis_clase_7", "sub_sistemas", 7, "Unidad III: Controladores", "Sintonización Heurística", "Método de Ziegler-Nichols en osciloscopio", null, "PENDIENTE"),
                PlanningEntity("sis_clase_8", "sub_sistemas", 8, "Unidad III: Controladores", "Sistemas Digitales de Control", "Programación de un lazo PID en Arduino", null, "PENDIENTE")
            )
            docenteDao.insertPlannings(mainPlanningsSis)

            // 7. Seed Plannings for Taller de Robótica / Programación
            val mainPlanningsProg = listOf(
                PlanningEntity("prog_clase_1", "sub_programacion", 1, "Unid I: Arduino", "Introducción a Microcontroladores", "Hola mundo de parpadeo LED", "2026-06-05", "DICTADA"),
                PlanningEntity("prog_clase_2", "sub_programacion", 2, "Unid I: Arduino", "Lectura de Sensores e Inputs", "Lectura de potenciómetro serial", null, "PENDIENTE"),
                PlanningEntity("prog_clase_3", "sub_programacion", 3, "Unid II: Motores", "Servomotores y Puentes H", "Control de velocidad de un motor DC", null, "PENDIENTE"),
                PlanningEntity("prog_clase_4", "sub_programacion", 4, "Unid II: Motores", "Programación Chasis Seguidor", "Ajuste de sensores infrarrojos de línea", null, "PENDIENTE")
            )
            docenteDao.insertPlannings(mainPlanningsProg)

            // Execute cascading reschedule for all subjects to set their initial automatic calendar slots
            runCascadingReschedule("sub_matematica", LocalDate.of(2026, 6, 1))
            runCascadingReschedule("sub_sistemas", LocalDate.of(2026, 6, 1))
            runCascadingReschedule("sub_programacion", LocalDate.of(2026, 6, 1))
        }
    }
}
