package com.example.kharcha


import java.text.SimpleDateFormat
import java.util.Date

fun getTodaysDate(): String {
    val dateFormat = SimpleDateFormat("ddMMyyyy")
    return dateFormat.format(Date())
}