package com.example.engu_pension_verification_application.utils

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
    }
}