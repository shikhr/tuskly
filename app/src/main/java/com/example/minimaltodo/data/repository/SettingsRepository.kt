package com.example.minimaltodo.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Reactive stream of the current reset hour (0–23). */
    val resetHour: Flow<Int> = prefsFlow()
        .map { it.getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR) }
        .distinctUntilChanged()

    /** Synchronous read of the current reset hour. */
    fun getResetHour(): Int = prefs.getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR)

    fun setResetHour(hour: Int) {
        require(hour in 0..23) { "Reset hour must be 0–23, got $hour" }
        prefs.edit().putInt(KEY_RESET_HOUR, hour).apply()
    }

    private fun prefsFlow(): Flow<SharedPreferences> = callbackFlow {
        trySend(prefs)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, _ ->
            trySend(sp)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_RESET_HOUR = "reset_hour"
        private const val DEFAULT_RESET_HOUR = 0

        /** Static accessor for use outside Hilt (e.g. widgets). */
        fun getResetHour(context: Context): Int =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR)
    }
}
