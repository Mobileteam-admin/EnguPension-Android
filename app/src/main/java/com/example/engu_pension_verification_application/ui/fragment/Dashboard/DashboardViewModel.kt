package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import android.util.Log
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardViewModel(var dashboardCallBack: DashboardCallBack) {

    private val prefs = SharedPref

    fun getLogout() {
        GlobalScope.launch(Dispatchers.Main) {

          /*  try {
                val response = ApiClient.getRetrofit().getLogout ("Bearer " + prefs.access_token)
Loader.hideLoader()
                if (response.logout_detail?.status.equals("success")) {
                    Toast.makeText(
                        getApplication(),
                        response.logout_detail?.tokenStatus,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        getApplication(),
                        "fail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: java.lang.Exception) {
                Loader.hideLoader()
                Toast.makeText(
                    getApplication(),
                    "wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }*/
            try {
                val response = ApiClient.getApiInterface().getLogout("Bearer " +prefs.access_token.toString()) //"Bearer Token " +

                if (response.logout_detail?.status.equals("success")) {
                    dashboardCallBack.ondashboardLogoutSuccess(response)

                } else {
                    dashboardCallBack.ondashboardLogoutFailure(response)

                }

            } catch (e: java.lang.Exception) {
                dashboardCallBack.ondashboardLogoutFailure(
                    ResponseLogout(
                        LogoutDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }

    fun getDashboardDetails() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getDashBoardDetails("Bearer " +prefs.access_token.toString()) //"Bearer Token " +

                if (response.detail?.status.equals("success")) {
                    dashboardCallBack.ondashboardDetailsSuccess(response)

                } else {
                    dashboardCallBack.ondashboardDetailsFailure(response)

                }

            } catch (e: java.lang.Exception) {
                Log.d("Model", "getDashboard details: " + e.localizedMessage)
                dashboardCallBack.ondashboardDetailsFailure(
                    /*   ResponseDashboardDetails(
                           DashboardDetails(message = "Something went wrong Dash Api")
                       )*/
                    ResponseDashboardDetails(DashboardDetails(message = "Something went wrong Dash Api"))
                )
            }
        }
    }


}