package com.example.minimaltodo.di

import android.content.Context
import com.example.minimaltodo.data.dao.CompletionLogDao
import com.example.minimaltodo.data.dao.GoalDao
import com.example.minimaltodo.data.dao.TaskDao
import com.example.minimaltodo.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideCompletionLogDao(db: AppDatabase): CompletionLogDao = db.completionLogDao()
}
