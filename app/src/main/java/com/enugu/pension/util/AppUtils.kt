package com.enugu.pension.util

import android.content.Context
import com.enugu.pension.R
import com.enugu.pension.constant.AppConstants
import java.util.regex.Pattern

object AppUtils {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isValidEmailAddress(emailAddress: String): Boolean {
        val pattern = Pattern.compile(AppConstants.EMAIL_REGEX)
        return (pattern.matcher(emailAddress).matches())
    }

    fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile(AppConstants.PASSWORD_REGEX)
        return (pattern.matcher(password).matches())
    }

    fun getFullName(firstName: String?, middleName: String?, lastName: String?): String {
        return listOfNotNull(firstName, middleName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    fun isValidBankAccountNumber(accountNumber: String): Boolean {
        return true // Remove after confirmation
        val minLength = appContext.resources.getInteger(R.integer.account_number_min_length)
        val maxLength = appContext.resources.getInteger(R.integer.account_number_max_length)
        val pattern = Pattern.compile("\\d{$minLength,$maxLength}")
        return (pattern.matcher(accountNumber).matches())
    }

    fun isValidFullName(fullName: String): Boolean {
        val pattern = Pattern.compile(AppConstants.FULL_NAME_REGEX)
        return (pattern.matcher(fullName).matches())
    }

    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun getSwiftCodeLength1() = appContext.resources.getInteger(R.integer.swift_code_length_2)
    fun getSwiftCodeLength2() = appContext.resources.getInteger(R.integer.swift_code_length_1)
    fun getSwiftCodeRange() = listOf(getSwiftCodeLength1(), getSwiftCodeLength2())
}