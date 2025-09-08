package com.enugu.pension.ui.model

class BankAccountItem(
    val bankName: String,
    val isPrimary: Boolean,
    val accountNumber: String,
    val accountType: String,
    val logoUrl: String? = null,
)