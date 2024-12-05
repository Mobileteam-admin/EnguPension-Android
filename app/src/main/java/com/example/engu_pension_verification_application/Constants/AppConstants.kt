package com.example.engu_pension_verification_application.Constants

object AppConstants {
    const val BEARER = "Bearer"
    const val SUCCESS = "success"
    const val FAIL = "fail"
    const val EXPIRED = "expired"
    const val EMAIL_REGEX = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    const val PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$"
    const val DEFAULT_CURRENCY_CODE = "ngn"
    const val SESSION_ID = "session_id"

}