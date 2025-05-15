package com.enugu.pension.model.ui

data class StatementItem(
    val id: Int,
    val isPension: Boolean,
    val amount: Double,
    val date: String,
    val description: String? = "",
)
