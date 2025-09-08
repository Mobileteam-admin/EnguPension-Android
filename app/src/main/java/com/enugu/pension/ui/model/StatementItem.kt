package com.enugu.pension.ui.model

data class StatementItem(
    val id: Int,
    val isPension: Boolean,
    val amount: Double,
    val date: String,
    val description: String? = "",
)
