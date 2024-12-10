package com.example.engu_pension_verification_application.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.ApiResult
import org.json.JSONObject
import retrofit2.Call

object NetworkUtils {
    fun getAccessToken() =
        "${AppConstants.BEARER} ${SharedPref.access_token ?: ""}"

    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }

    fun <T> handleResponse(
        call: Call<T>,
        unknownErrorMsg: String? = null,
        detailKey: String = "detail",
        messageKey: String = "message"
    ): ApiResult<T> {
        var errorMessage = unknownErrorMsg ?: "Something went wrong"
        return try {
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                response.errorBody()?.let {
                    try {
                        val jsonObject = JSONObject(it.string())
                        errorMessage = jsonObject.getJSONObject(detailKey).getString(messageKey)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Error(errorMessage)
        }
    }
}