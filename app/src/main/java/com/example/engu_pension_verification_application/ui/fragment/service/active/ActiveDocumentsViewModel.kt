package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.util.Log
import com.example.engu_pension_verification_application.model.response.ActiveDocDetail
import com.example.engu_pension_verification_application.model.response.ActiveDocRetriveDetail
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ActiveDocumentsViewModel(var activeDocCallBack: ActiveDocCallBack) {

    private val prefs = SharedPref

    fun docUpload(requestbody: RequestBody) {
        GlobalScope.launch(Dispatchers.Main) {
            try {

                val response = ApiClient.getRetrofit().upLoadActiveUserDocuments(
                    "Bearer " + prefs.access_token.toString(),requestbody)

                if (response.detail?.status.equals("success")){

                    activeDocCallBack.onDocUploadSuccess(response)

                }
                else{
                    activeDocCallBack.onDocUploadFailure(response)

                }
            }
            catch (e : java.lang.Exception){
                Log.e("Exception", "docUpload: "+e.localizedMessage )
                activeDocCallBack.onDocUploadFailure( ResponseActiveDocUpload(ActiveDocDetail( message = "Something went wrong in Doc submit")))

            }
        }
    }

    fun docRetrive(){
        GlobalScope.launch(Dispatchers.Main){
            try {
                val response = ApiClient.getRetrofit().getActiveDocRetrive(
                    "Bearer " + prefs.access_token.toString()
                )

                Log.d("DocRetrive", "DocRetrive : $response")

                if (response.detail?.status.equals("success")){
                    activeDocCallBack.onDocRetriveSuccess(response)
                }
                else{
                    activeDocCallBack.onDocRetriveFailure(response)
                }

            }
            catch (e: java.lang.Exception){

                activeDocCallBack.onDocRetriveFailure(ResponseActiveDocRetrive(
                    ActiveDocRetriveDetail(message = "Something went wrong in doc Retrive api")
                ))

            }
        }

    }



}