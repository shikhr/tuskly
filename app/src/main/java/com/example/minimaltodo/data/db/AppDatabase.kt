package com.example.minimaltodo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.minimaltodo.data.dao.CompletionLogDao
import com.example.minimaltodo.data.dao.GoalDao
import com.example.minimaltodo.data.dao.TaskDao
import com.example.minimaltodo.data.entity.CompletionLog
import com.example.minimaltodo.data.entity.Goal
import com.example.minimaltodo.data.entity.Task

@Database(
    entities = [Goal::class, Task::class, CompletionLog::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun completionLogDao(): CompletionLogDao

    companion object {
        private const val DB_NAME = "minimaltodo.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** Single shared instance used by both Hilt and widgets. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME,
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE goals ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN deletedAt INTEGER")
            }
        }
    }
}
