package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.util.Log
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocUpload
import com.example.engu_pension_verification_application.model.response.RetireeDocDetail
import com.example.engu_pension_verification_application.model.response.RetireeDocRetriveResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class RetireeDocumentsViewModel(var retireeDocCallBack: RetireeDocCallBack) {
    private val prefs = SharedPref

    fun docUpload(requestbody: RequestBody){

        GlobalScope.launch(Dispatchers.Main){

            try {


                val response = ApiClient.getApiInterface().upLoadRetireeeUserDocuments(
                    "Bearer " + prefs.access_token.toString(),requestbody)

                Log.d("DocSubmitRes", "DocSubmitRes : $response")

                if (response.detail?.status.equals("success")){

                    retireeDocCallBack.onDocUploadSuccess(response)

                }
                else{
                    retireeDocCallBack.onDocUploadFailure(response)

                }
            }
            catch (e : java.lang.Exception){
                Log.e("Exception", "docUpload: "+e.localizedMessage )
                retireeDocCallBack.onDocUploadFailure( ResponseRetireeDocUpload(RetireeDocDetail( message = "Something went wrong in Retiree Doc submit")))

            }
        }

    }
    //RetriveFun in MV
    fun retireedocRetrive(){
        GlobalScope.launch(Dispatchers.Main){
            try {
                val response = ApiClient.getApiInterface().getRetireeDocRetrive(
                    "Bearer " + prefs.access_token.toString()
                )

                Log.d("DocRetrive", "DocRetrive : $response")

                if (response.detail?.status.equals("success")){
                    retireeDocCallBack.onRetireeDocRetriveSuccess(response)
                }
                else{
                    retireeDocCallBack.onRetireeDocRetriveFailure(response)
                }

            }
            catch (e: java.lang.Exception){

                retireeDocCallBack.onRetireeDocRetriveFailure(
                    ResponseRetireeDocRetrive(
                        RetireeDocRetriveResponse(message = "Something went wrong in doc Retrive api")
                    )
                )

            }
        }

    }
}