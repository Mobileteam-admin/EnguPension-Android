package com.example.engu_pension_verification_application.util

import com.example.engu_pension_verification_application.Constants.AppConstants
import java.util.regex.Pattern

class AppUtils {
    companion object {
        fun isValidEmailAddress(emailAddress: String): Boolean {
            val pattern = Pattern.compile(AppConstants.EMAIL_REGEX)
            return (pattern.matcher(emailAddress).matches())
        }
        fun isValidPassword(password: String): Boolean {
            val pattern = Pattern.compile(AppConstants.PASSWORD_REGEX)
            return (pattern.matcher(password).matches())
        }
        fun getFullName(firstName:String?, middleName:String?, lastName:String?): String {
            return listOfNotNull(firstName, middleName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }
        fun String.isValidNumber() = this.toFloatOrNull() != null
        fun String.capitalizeFirstLetter(): String {
            return if (this.isEmpty()) {
                this
            } else {
                this.substring(0, 1).uppercase() + this.substring(1).lowercase()
            }
        }
        fun isValidBankAccountNumber(accountNumber: String): Boolean {
            val pattern = Pattern.compile(AppConstants.BANK_ACCOUNT_NUMBER_REGEX)
            return (pattern.matcher(accountNumber).matches())
        }
        fun isValidFullName(fullName: String): Boolean {
            val pattern = Pattern.compile(AppConstants.FULL_NAME_REGEX)
            return (pattern.matcher(fullName).matches())
        }
    }
}