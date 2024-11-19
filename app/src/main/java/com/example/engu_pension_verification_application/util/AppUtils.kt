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
    }
}