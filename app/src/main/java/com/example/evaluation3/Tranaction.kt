package com.example.evaluation3

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val type: TransactionType,
    val date: String,
    val categoryOrReason: String, // Category for income, Reason for expense
    val amount: String,
    val description: String? // Optional for expenses
)