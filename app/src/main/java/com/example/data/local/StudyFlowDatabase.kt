package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.FocusSession
import com.example.data.model.Task

@Database(
    entities = [Task::class, FocusSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: StudyFlowDatabase? = null

        fun getInstance(context: Context): StudyFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyFlowDatabase::class.java,
                    "studyflow_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
