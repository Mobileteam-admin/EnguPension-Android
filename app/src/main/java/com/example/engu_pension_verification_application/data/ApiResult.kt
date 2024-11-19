package com.example.engu_pension_verification_application.data

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
//    object Loading : ApiResult<Nothing>()
}