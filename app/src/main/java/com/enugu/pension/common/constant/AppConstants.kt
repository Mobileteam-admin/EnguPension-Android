package com.enugu.pension.common.constant

import android.text.InputFilter

object AppConstants {
    const val BASE_URL: String = "https://pension-distributor.demoserver.work"
    const val BEARER = "Bearer"
    const val SUCCESS = "success"
    const val FAIL = "fail"
    const val EXPIRED = "expired"
    const val TOKEN_EXPIRED = "token_expired"
    const val EMAIL_REGEX = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    const val PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$"
    const val FULL_NAME_REGEX ="^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$"

    const val DEFAULT_CURRENCY_CODE = "ngn"
    const val SESSION_ID = "session_id"

    val SwiftCodeFilter = InputFilter { source, start, end, dest, dstart, dend ->
        for (index in start until end) {
            if (!Character.isDigit(source[index]) && !Character.isUpperCase(source[index])) {
                return@InputFilter ""
            }
        }
        null
    }

}