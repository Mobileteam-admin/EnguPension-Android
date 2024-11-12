package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import com.example.engu_pension_verification_application.model.response.ResponseDashboardDetails
import com.example.engu_pension_verification_application.model.response.ResponseLogout

interface DashboardCallBack {

    fun ondashboardLogoutSuccess(response: ResponseLogout)
    fun ondashboardLogoutFailure(response: ResponseLogout)

    fun ondashboardDetailsSuccess(response: ResponseDashboardDetails)
    fun ondashboardDetailsFailure(response: ResponseDashboardDetails)
}