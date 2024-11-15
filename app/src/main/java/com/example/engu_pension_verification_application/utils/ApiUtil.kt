package com.example.engu_pension_verification_application.utils

import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.ApiResult
import retrofit2.Response

object ApiUtil {
    fun getAccessToken(): String {
        if (AppConstants.ACCESS_TOKEN.isEmpty())
            AppConstants.ACCESS_TOKEN = SharedPref.access_token ?: ""
        return "${AppConstants.BEARER} ${AppConstants.ACCESS_TOKEN}"
    }

    fun <T> handleResponse(response: Response<T>): ApiResult<T> {
        return try {
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("An error occurred")
            }
        } catch (e: Exception) {
            ApiResult.Error("An error occurred")
        }
    }
}