package com.example.engu_pension_verification_application.ui.fragment.service.ProcessingVerify

import android.util.Log
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Processing_Verify_Presenter(var processingVerifyCallback: Processing_Verify_CallBack) {



    private val prefs = SharedPref

    fun getProcessing_Verify(){

        GlobalScope.launch(Dispatchers.Main){

/*            try {

                Log.d("Inputs", "EinSubmitCall: $inputEinNumber")
                Log.d("token", "EinSubmitCall: "+ prefs.access_token.toString())
                val response = ApiClient.getRetrofit().getEinNumber("Bearer " + prefs.access_token.toString(),inputEinNumber)
                //_ActiveBankInfoStatus.value = response


                if (response!!.detail?.status.equals("success")){

                    einNumberCallback.onEinNumberSubmitSuccess(response)
                }
                else {
                    einNumberCallback.onEinNumberSubmitFailure(response)
                }

            }

            catch (e : java.lang.Exception){
                Loader.hideLoader()
                Log.d("TAG", "Ein_Number_Verification: "+e.localizedMessage)


                einNumberCallback.onEinNumberSubmitFailure(
                    ResponseEinNumber(
                        EinNumberDetail(message = "Something went wrong in Ein Submit Api")
                    )
                )




//                _ActiveBankInfoStatus.value = ResponseActiveBankInfo(ResBankDetail( "Something Went Wrong "))
            }*/


            try {
                val response = ApiClient.getRetrofit().getActiveProcessingVerify(
                    "Bearer " + prefs.access_token.toString()
                ) //
                // Log.d("Model", "getBankList: "+response.banks)
                if (response.detail?.status.equals("success")) {

                    // Log.d("Model", "getBankList: "+response.banks)
                    //_banklistStatus.value = response
                    processingVerifyCallback.onProcessingVerifySuccess(response)
                } else {
                    //_banklistStatus.value = response
                    processingVerifyCallback.onProcessingVerifyFailure(response)
                }

            } catch (e: java.lang.Exception) {
                Log.d("Model", "getBankList: " + e.localizedMessage)
                processingVerifyCallback.onProcessingVerifyFailure(ResponseActiveProcessingVerify(ProcessVerifyDetail(message = "Something went wrong GetProcessVerify api")))
//                _banklistStatus.value = ResponseBankList(BankDetail(message = "Something went wrong"))
            }
        }


        }
    }
