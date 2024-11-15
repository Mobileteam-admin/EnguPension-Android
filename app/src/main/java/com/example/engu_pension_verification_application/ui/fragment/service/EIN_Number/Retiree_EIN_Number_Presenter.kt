package com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number

import android.util.Log
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputEinNumber
import com.example.engu_pension_verification_application.model.response.EinNumberDetail
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Retiree_EIN_Number_Presenter(var retireeinNumberCallback:Retiree_EIN_Number_CallBack) {

    private val prefs = SharedPref

    fun RetireeEin_Number_Submit(inputEinNumber: InputEinNumber){

        GlobalScope.launch(Dispatchers.Main){

            try {

                Log.d("Inputs", "RetireeEinSubmitCall: $inputEinNumber")
                Log.d("token", "RetireeEinSubmitCall: "+ prefs.access_token.toString())
                val response = ApiClient.getApiInterface().getEinNumber("Bearer " + prefs.access_token.toString(),inputEinNumber)
                //_ActiveBankInfoStatus.value = response


                if (response!!.detail?.status.equals("success")){

                    retireeinNumberCallback.onRetireeEinNumberSubmitSuccess(response)
                }
                else {
                    retireeinNumberCallback.onRetireeEinNumberSubmitFailure(response)
                }

            }

            catch (e : java.lang.Exception){
                Loader.hideLoader()
                Log.d("TAG", "Ein_Number_Verification: "+e.localizedMessage)


                retireeinNumberCallback.onRetireeEinNumberSubmitFailure(
                    ResponseEinNumber(
                        EinNumberDetail(message = "Something went wrong in Retiree Ein Submit Api")
                    )
                )




//                _ActiveBankInfoStatus.value = ResponseActiveBankInfo(ResBankDetail( "Something Went Wrong "))
            }


        }
    }
}