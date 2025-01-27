package com.enugu.pension.network

class BankAccountItem(
    val bankName: String,
    val isPrimary: Boolean,
    val accountNumber: String,
    val accountType: String,
    val logoUrl: String? = null,
)