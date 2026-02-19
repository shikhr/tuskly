package com.example.minimaltodo.widget

import android.content.Context

/**
 * Convenience facade for refreshing homescreen widgets.
 * Each method reads the latest data from Room, pushes it into Glance
 * DataStore state via [updateAppWidgetState], and triggers a recomposition.
 */
object WidgetRefresh {

    suspend fun refreshAll(context: Context) {
        GoalsWidget.refreshAll(context)
        TasksWidget.refreshAll(context)
    }

    suspend fun refreshGoals(context: Context) {
        GoalsWidget.refreshAll(context)
    }

    suspend fun refreshTasks(context: Context) {
        TasksWidget.refreshAll(context)
    }
}
