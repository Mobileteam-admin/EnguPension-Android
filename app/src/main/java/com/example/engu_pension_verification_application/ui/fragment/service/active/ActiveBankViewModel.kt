package com.example.engu_pension_verification_application.ui.fragment.service.active


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActiveBankViewModel(var activeBankCallBack: ActiveBankCallBack){

    /*(application: Application) : AndroidViewModel(application)*/
    private val prefs = SharedPref

    /*private val _banklistStatus = MutableLiveData<ResponseBankList>()
    val BanklistStatus: LiveData<ResponseBankList>
        get() = _banklistStatus

    private val _SwiftBankCodeStatus = MutableLiveData<ResponseSwiftBankCode>()
    val SwiftBankCodeStatus: LiveData<ResponseSwiftBankCode>
        get() = _SwiftBankCodeStatus

    private val _ActiveBankInfoStatus = MutableLiveData<ResponseBankInfo>()
    val ActiveBankInfoStatus : LiveData<ResponseBankInfo>
    get() = _ActiveBankInfoStatus*/


    /*init {
        application.let { prefs.with(it) }
    }*/

    fun ActiveBankinformation(inputActiveBankInfo: InputActiveBankInfo){
        GlobalScope.launch(Dispatchers.Main){
            try {

                Log.d("Inputs", "FinishFnCall: "+inputActiveBankInfo)
                Log.d("token", "FinishFnCall: "+ prefs.access_token.toString())
                val response = ApiClient.getRetrofit().getActiveBankInfo("Bearer " + prefs.access_token.toString(),inputActiveBankInfo)
                //_ActiveBankInfoStatus.value = response


                if (response!!.detail?.status.equals("success")){

                    activeBankCallBack.onActiveBankInfoSubmitSuccess(response)
                }
                else {
                    activeBankCallBack.onActiveBankInfoSubmitFailure(response)
                }

            }
            catch (e : java.lang.Exception){
                Loader.hideLoader()
                Log.d("TAG", "ActiveBankinformation: "+e.localizedMessage)


                activeBankCallBack.onActiveBankInfoSubmitFailure(
                    ResponseBankInfo(
                        BankDetail(message = "Something went wrong in Bank Submit Api")
                    )
                )




//                _ActiveBankInfoStatus.value = ResponseActiveBankInfo(ResBankDetail( "Something Went Wrong "))
            }
        }
    }

    /*fun getBankList() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getAddedBanks(
                    "Bearer " + prefs.access_token.toString()) //
               // Log.d("Model", "getBankList: "+response.banks)
                if (response.detail?.status.equals("success")) {

                   // Log.d("Model", "getBankList: "+response.banks)
                    //_banklistStatus.value = response
                    activeBankCallBack.onBankListSuccess(response)
                } else {
                    //_banklistStatus.value = response
                    activeBankCallBack.onBankListFailure(response)
                }

            } catch (e: java.lang.Exception) {
                Log.d("Model", "getBankList: "+e.localizedMessage)
//                Log.d("bank_error", "${ResponseBankList(BankDetail())}")
//
//                _banklistStatus.value = ResponseBankList(BankDetail(message = "Something went wrong"))
            }
        }
    }*/

    fun getswiftBankCode(inputswiftcode: InputSwiftBankCode) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getSwiftBankCode(
                    "Bearer " + prefs.access_token.toString(),inputswiftcode )

                if (response.swiftbankdetail?.status.equals("success")) {
                    // Log.d("Model", "getBankList: "+response.banks)
                    //_SwiftBankCodeStatus.value = response
                    activeBankCallBack.onSwiftBankCodeSuccess(response)
                } else {
                    activeBankCallBack.onSwiftBankCodeFailure(response)
                    //_SwiftBankCodeStatus.value = response
                }

            } catch (e: java.lang.Exception) {
                Log.d("bank_swift_error", "${ResponseSwiftBankCode(SwiftBankDetail())}")
//                _SwiftBankCodeStatus.value = ResponseSwiftBankCode(SwiftBankDetail(message = "Something went wrong"))

                activeBankCallBack.onSwiftBankCodeFailure(
                    ResponseSwiftBankCode(
                        SwiftBankDetail(message = "Something went wrong in swift Api")
                    )
                )
            }
        }
    }

}