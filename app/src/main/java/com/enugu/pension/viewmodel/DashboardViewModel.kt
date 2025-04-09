package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.ApiResult
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.VideoCallRequest
import com.enugu.pension.model.response.*
import com.enugu.pension.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DashboardViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    var banks: ArrayList<ListBanksItem?>? = null
    var bankAccounts: ArrayList<BankAccountListResponse.Detail.BankAccount>? = null
    var bankAccountTypes: ArrayList<AccountTypeItem?>? = null
    private val _logoutResult =
        MutableLiveData<ResponseLogout>()
    val logoutResult: LiveData<ResponseLogout>
        get() = _logoutResult
    val profilePictureUrl = MutableLiveData<String?>(null) // TODO: remove after dashboard-details API update

    private val _dashboardDetailsResult =
        MutableLiveData<ResponseDashboardDetails>(null)
    val dashboardDetailsResult: LiveData<ResponseDashboardDetails>
        get() = _dashboardDetailsResult

    private val _bankAccountListApiResult = MutableLiveData<BankAccountListResponse>()
    val bankAccountListApiResult: LiveData<BankAccountListResponse>
        get() = _bankAccountListApiResult

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _logoutResult.postValue(networkRepo.logout())
            } catch (e: Exception) {
                _logoutResult.postValue(
                    ResponseLogout(
                        LogoutDetail(message = "Something went wrong")
                    )
                )
            }
        }
    }

    fun fetchDashboardDetails(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                _dashboardDetailsResult.postValue(networkRepo.fetchDashboardDetails())
            } catch (e: Exception) {
                e.printStackTrace()
                _dashboardDetailsResult.postValue(
                    ResponseDashboardDetails(
                        DashboardDetails(message = "Something went wrong with fetching dashboard details")
                    )
                )
            }
        }
    }

    fun fetchBankAccountList(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                _bankAccountListApiResult.postValue(networkRepo.fetchBankAccountList())
            } catch (e: Exception) {
                _bankAccountListApiResult.postValue(
                    BankAccountListResponse(
                        BankAccountListResponse.Detail(message = "Something went wrong with fetching bank list")
                    )
                )
            }
        }
    }



    fun fetchProfilePicture() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = networkRepo.fetchProfileDetails()
                result.detail?.userProfileDetails?.imageUrl?.let { profilePictureUrl.postValue(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}