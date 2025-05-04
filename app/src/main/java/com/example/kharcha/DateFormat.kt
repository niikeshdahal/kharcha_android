package com.example.kharcha

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTransactionDate(dateString: String): String {
    // Assuming input date is in ddMMyyy format
    val inputFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    return try {
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString // Return original string if parsing fails
    }
}