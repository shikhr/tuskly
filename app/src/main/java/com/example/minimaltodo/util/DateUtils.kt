package com.example.minimaltodo.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

fun todayDateString(): String = LocalDate.now().format(DATE_FORMATTER)

fun LocalDate.toDateString(): String = format(DATE_FORMATTER)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, DATE_FORMATTER)

/**
 * Returns the "logical" date string accounting for a custom reset hour.
 *
 * If the current hour is before [resetHour], the logical day is still yesterday
 * (goals haven't reset yet). At or after [resetHour], it's today.
 */
fun logicalDateString(resetHour: Int): String {
    val now = LocalDateTime.now()
    val date = if (now.hour < resetHour) {
        now.toLocalDate().minusDays(1)
    } else {
        now.toLocalDate()
    }
    return date.format(DATE_FORMATTER)
}

/**
 * Emits the current logical date and re-emits when either:
 * - The [resetHourFlow] value changes (user updates settings).
 * - The clock crosses the next reset boundary.
 *
 * The flow sleeps until the next boundary instead of polling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun logicalDateFlow(resetHourFlow: Flow<Int>): Flow<String> =
    resetHourFlow.flatMapLatest { resetHour ->
        flow {
            while (true) {
                val now = LocalDateTime.now()
                emit(logicalDateString(resetHour))

                // Sleep until the next reset boundary
                val nextReset = if (now.hour >= resetHour) {
                    now.toLocalDate().plusDays(1).atTime(resetHour, 0)
                } else {
                    now.toLocalDate().atTime(resetHour, 0)
                }
                val delayMs = Duration.between(now, nextReset).toMillis() + 1000L
                delay(delayMs)
            }
        }
    }.distinctUntilChanged()

/**
 * Formats an hour (0–23) for display, e.g. "12:00 AM", "3:00 AM", "12:00 PM".
 */
fun formatResetHour(hour: Int): String {
    val time = LocalTime.of(hour, 0)
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}
