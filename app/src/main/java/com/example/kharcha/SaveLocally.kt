package com.example.kharcha

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private const val FILE_NAME = "transactions.json"

fun saveTransactionsToFile(context: Context, transactions: List<TransactionsItem>) {
    val file = File(context.filesDir, FILE_NAME)
    val json = Gson().toJson(transactions)
    file.writeText(json)
}

fun loadTransactionsFromFile(context: Context): List<TransactionsItem> {
    val file = File(context.filesDir, FILE_NAME)
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val type = object : TypeToken<List<TransactionsItem>>() {}.type
    return Gson().fromJson(json, type) ?: emptyList()
}
