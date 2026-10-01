package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TeacherEntity::class,
        SubjectEntity::class,
        StudentEntity::class,
        PlanningEntity::class,
        InterruptionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DocenteDatabase : RoomDatabase() {
    abstract fun docenteDao(): DocenteDao

    companion object {
        @Volatile
        private var INSTANCE: DocenteDatabase? = null

        fun getDatabase(context: Context): DocenteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocenteDatabase::class.java,
                    "docentepro_database"
                )
                .fallbackToDestructiveMigration() // Facilitates local rapid iterative schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
