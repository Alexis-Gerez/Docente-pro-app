package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocenteDao {

    // Teacher queries
    @Query("SELECT * FROM teachers LIMIT 1")
    fun getTeacher(): Flow<TeacherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    // Subject queries
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    // Student queries
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    // Planning queries
    @Query("SELECT * FROM plannings WHERE subjectId = :subjectId ORDER BY classOrder ASC")
    fun getPlanningsForSubject(subjectId: String): Flow<List<PlanningEntity>>

    @Query("SELECT * FROM plannings WHERE subjectId = :subjectId ORDER BY classOrder ASC")
    suspend fun getPlanningsForSubjectList(subjectId: String): List<PlanningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannings(plannings: List<PlanningEntity>)

    @Update
    suspend fun updatePlanning(planning: PlanningEntity)

    @Query("DELETE FROM plannings WHERE subjectId = :subjectId")
    suspend fun deletePlanningsForSubject(subjectId: String)

    // Interruption queries
    @Query("SELECT * FROM interruptions ORDER BY date ASC")
    fun getAllInterruptions(): Flow<List<InterruptionEntity>>

    @Query("SELECT * FROM interruptions ORDER BY date ASC")
    suspend fun getAllInterruptionsList(): List<InterruptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterruption(interruption: InterruptionEntity)

    @Query("DELETE FROM interruptions WHERE date = :date")
    suspend fun deleteInterruption(date: String)
}
