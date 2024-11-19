package com.example.engu_pension_verification_application.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.ApiResult
import retrofit2.Response

object NetworkUtils {
    fun getAccessToken(): String {
        if (AppConstants.ACCESS_TOKEN.isEmpty())
            AppConstants.ACCESS_TOKEN = SharedPref.access_token ?: ""
        return "${AppConstants.BEARER} ${AppConstants.ACCESS_TOKEN}"
    }

    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
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