package com.example.engu_pension_verification_application.ui.fragment.service.bank_verification

import android.util.Log
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.input.InputEinNumber
import com.example.engu_pension_verification_application.model.response.BankVerifyDetail
import com.example.engu_pension_verification_application.model.response.Detail
import com.example.engu_pension_verification_application.model.response.EinNumberDetail
import com.example.engu_pension_verification_application.model.response.ResponseBankVerify
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number.EIN_Number_CallBack
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.http.Query

class Bank_Verify_Presenter(var bankVerifyCallback: Bank_Verify_Callback) {


    private val prefs = SharedPref
    fun BankVerify_Submit(inputBankVerification: InputBankVerification){

        GlobalScope.launch(Dispatchers.Main){
            try {

                Log.d("Inputs", "bankverifycall: $inputBankVerification")
                Log.d("token", "bankverifycall: "+ prefs.access_token.toString())
                val response = ApiClient.getRetrofit().getBankVerify("Bearer " + prefs.access_token.toString(), inputBankVerification)
                //_ActiveBankInfoStatus.value = response


                if (response.detail?.status.equals("success")){

                    bankVerifyCallback.onBankVerifySubmitSuccess(response)
                }
                else {
                    bankVerifyCallback.onBankVerifySubmitFailure(response)
                }

            }

            catch (e : java.lang.Exception){
                Loader.hideLoader()
                Log.d("TAG", "Bank_Verification: "+e.localizedMessage)


                bankVerifyCallback.onBankVerifySubmitFailure(
                    ResponseBankVerify(
                        BankVerifyDetail(message = "Bank Verification api call failed")
                        //Something went wrong in Bank Verify Submit Api
                    )
                )

            }


        }
    }

}

