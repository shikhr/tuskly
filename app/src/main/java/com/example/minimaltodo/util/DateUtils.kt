package com.example.minimaltodo.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

fun todayDateString(): String = LocalDate.now().format(DATE_FORMATTER)

fun LocalDate.toDateString(): String = format(DATE_FORMATTER)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, DATE_FORMATTER)
