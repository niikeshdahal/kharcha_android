package com.example.kharcha

data class TransactionState(
    val transactions: List<TransactionsItem> = emptyList()
){
    fun deleteTransaction(transaction: TransactionsItem): TransactionState {
        return copy(transactions = transactions.filterNot { it == transaction })
    }
}
