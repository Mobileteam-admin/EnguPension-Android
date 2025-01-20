package com.example.engu_pension_verification_application.network

class BankAccountItem(
    val bankName: String,
    val isPrimary: Boolean,
    val accountNumber: String,
    val accountType: String,
    val logoUrl: String? = null,
)