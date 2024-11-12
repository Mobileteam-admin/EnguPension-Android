package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.util.Log
import com.example.engu_pension_verification_application.model.input.InputActiveBasicDetails
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActiveBasicDetailViewModel(var activeBasicDetailViewCallBack: ActiveBasicDetailViewCallBack) {
    //(application: Application) : AndroidViewModel(application)
    private val prefs = SharedPref


    /* private val _combinedDetailstatus = MutableLiveData<ResponseCombinationDetails>()
     val combinedDetailstatusStatus: LiveData<ResponseCombinationDetails>
         get() = _combinedDetailstatus


     private val _activeBasicDetailstatus = MutableLiveData<ResponseActiveBasicDetails>()
     val activeBasicDetailsStatus: LiveData<ResponseActiveBasicDetails>
         get() = _activeBasicDetailstatus
 */

    /*init {
        application.let { prefs.with(it) }
    }*/


    fun getCombinedDetails(inputLGAList: InputLGAList) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getCombinationDetails(
                    inputLGAList
                ) //"Token " +prefs.access_token.toString()) //"Bearer Token "
                // "Bearer " + prefs.access_token.toString(),
                Log.d("combineDetail", "$response")

                if (response.combinedetails?.status.equals("success")) {
                    // _combinedDetailstatus.value = response
                    activeBasicDetailViewCallBack.onAcombinedDetailSuccess(response)
                } else {
                    //_combinedDetailstatus.value = response
                    activeBasicDetailViewCallBack.onAcombinedDetailFail(response)
                }

            } catch (e: java.lang.Exception) {
                activeBasicDetailViewCallBack.onAcombinedDetailFail(
                    ResponseCombinationDetails(
                        CombinationDetail(message = "Something went wrong Combination Api")
                    )
                )/* _combinedDetailstatus.value =
                     ResponseCombinationDetails(
                         CombinationDetail(
                             message = "Something went wrong"
                         )
                     )*/
            }
        }

        /* GlobalScope.launch(Dispatchers.Main) {
             try {
                 val response = ApiClient.getRetrofit().getCombinationDetails(inputLGAList)
                 if (response.combinedetails?.status!!.equals("success")) {
                     activeBasicDetailViewCallBack.onActiveBasicCombinedDetailSuccess(response)

                 } else {
                     activeBasicDetailViewCallBack.onActiveBasicCombinedDetailFailure(response)
                 }
             } catch (e: java.lang.Exception) {
                 Log.d("Exception", "getCombinedDetails: " + e.localizedMessage)
                 activeBasicDetailViewCallBack.onActiveBasicCombinedDetailFailure(
                     ResponseCombinationDetails(CombinationDetail(message = "Something went wrong"))
                 )
             }
         }*/
    }

    //submission api
    fun getAccountDetails(inputActiveBasicDetails: InputActiveBasicDetails) {
        GlobalScope.launch(Dispatchers.Main) {
            try {

                //"Bearer " + prefs.access_token.toString()
                val response = ApiClient.getRetrofit().getActiveDetails(
                    "Bearer " + prefs.access_token.toString(), inputActiveBasicDetails
                )
                if (response.detail?.status.equals("success")) {
                    //_activeBasicDetailstatus.value = response
                    activeBasicDetailViewCallBack.onActiveBasicDetailSuccess(response)



                    Log.d("active_basic_response", "${response.detail?.userProfileDetails}")
                    //Log.d("inputActive", "$inputActiveBasicDetails")
                } else {
                    //_activeBasicDetailstatus.value = response
                    activeBasicDetailViewCallBack.onActiveBasicDetailFailure(response)
                }
            } catch (e: java.lang.Exception) {

                Log.e("active_basic_response", "activeBasicFailure", e)
                activeBasicDetailViewCallBack.onActiveBasicDetailFailure(
                    ResponseActiveBasicDetails(
                        ActiveBasicDetail(message = "Something went wrong Active basic submit Api")
                    )
                )/*  _activeBasicDetailstatus.value =
                      ResponseActiveBasicDetails(
                          ActiveBasicDetail(
                              message = "Something went wrong"
                          )

                      )*/

            }
        }
    }

    fun getRetriveDetails() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getActiveBasicRetrive(
                    "Bearer " + prefs.access_token.toString()
                )

                Log.d("retrive", "Activeretriv" + response)

                /*var s = response.detail?.status

                when(s){
                    s.equals("success").toString() -> activeBasicDetailViewCallBack.onActiveRetriveSuccess(response)
                    s.equals("fail").toString() -> activeBasicDetailViewCallBack.onActiveRetriveFailure(response)
                    else -> {

                        activeBasicDetailViewCallBack.onActiveRetriveFailure(
                            ResponseActiveRetrive(
                                ActiveRetriveDetail(message = "Something went wrong in Retrive api")
                            )
                        )
                    }
                }*/

                if (response.detail?.status.equals("success")) {
                    activeBasicDetailViewCallBack.onActiveRetriveSuccess(response)

                } else {
                    activeBasicDetailViewCallBack.onActiveRetriveFailure(response)
                }

            } catch (e: java.lang.Exception) {


                activeBasicDetailViewCallBack.onActiveRetriveFailure(
                    ResponseActiveBasicRetrive(
                        ActiveRetriveDetail(message = "Something went wrong in Active Basic Retrive api")
                    )
                )
            }
        }
    }

}