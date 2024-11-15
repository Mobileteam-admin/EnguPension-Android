package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.util.Log
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.input.InputRetireeBasicDetails
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RetireeBasicDetailsViewModel(var retireeBasicDetailsViewCallBack: RetireeBasicDetailsViewCallBack)
// (application: Application) : AndroidViewModel(application)
{

    private val prefs = SharedPref

    /*private val _combinedDetailstatus = MutableLiveData<ResponseCombinationDetails>()
    val combinedDetailstatusStatus: LiveData<ResponseCombinationDetails>
        get() = _combinedDetailstatus


    private val _retireeBasicDetailstatus = MutableLiveData<ResponseRetireeBasicDetails>()
    val retireeBasicDetailsStatus: LiveData<ResponseRetireeBasicDetails>
        get() = _retireeBasicDetailstatus*/

    /* init {
         application.let { prefs.with(it) }
     }
 */

    fun getCombinedDetails(inputLGAList: InputLGAList) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getCombinationDetails(
                    inputLGAList
                ) //"Token " +prefs.access_token.toString()) //"Bearer Token "
                // "Bearer " + prefs.access_token.toString(),

                if (response.combinedetails?.status.equals("success")) {
                    // _combinedDetailstatus.value = response
                    retireeBasicDetailsViewCallBack.onRcombinedDetailSuccess(response)
                } else {
                    //_combinedDetailstatus.value = response
                    retireeBasicDetailsViewCallBack.onRcombinedDetailFail(response)
                }

            } catch (e: java.lang.Exception) {
                retireeBasicDetailsViewCallBack.onRcombinedDetailFail(
                    ResponseCombinationDetails(
                        CombinationDetail(message = "Something went wrong Combined Api")
                    )
                )/* _combinedDetailstatus.value =
                    ResponseCombinationDetails(
                        CombinationDetail(
                            message = "Something went wrong"
                        )
                    )*/
            }
        }
    }

    //submission api
    fun getAccountDetails(inputRetireeBasicDetails: InputRetireeBasicDetails) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getRetireeDetails(
                    "Bearer " + prefs.access_token.toString(), inputRetireeBasicDetails
                )
                if (response.detail?.status.equals("success")) {
                    //_retireeBasicDetailstatus.value = response

                    retireeBasicDetailsViewCallBack.onRetireeBasicDetailSuccess(response)

                    Log.d("next_response_retire", "${response.detail?.userProfileDetails}")

                } else {
                    //_retireeBasicDetailstatus.value = response
                    retireeBasicDetailsViewCallBack.onRetireeBasicDetailFailure(response)
                }
            } catch (e: java.lang.Exception) {


                Log.e("next_response", "retiree submit", e)
                retireeBasicDetailsViewCallBack.onRetireeBasicDetailFailure(
                    ResponseRetireeBasicDetails(
                        RetireeBasicDetail(message = "Something went wrong Retire Basic Submit Api")
                    )
                )

                /*_retireeBasicDetailstatus.value =
                    ResponseRetireeBasicDetails(
                        RetireeBasicDetail(
                            message = "Something went wrong"
                        )

                    )*/

            }
        }
    }

    fun getRetriveDetails(){

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getRetireeBasicRetrive(
                    "Bearer " + prefs.access_token.toString()
                )

                Log.d("retrive", "Retireeretriv" + response)

                if (response.detail?.status.equals("success")) {

                    retireeBasicDetailsViewCallBack.onRetireeRetriveSuccess(response)


                } else {

                    retireeBasicDetailsViewCallBack.onRetireeRetriveFailure(response)

                }

            } catch (e: java.lang.Exception) {


                retireeBasicDetailsViewCallBack.onRetireeRetriveFailure(
                    ResponseRetireeBasicRetrive(
                        RetireeRetriveDetail(message = "Something went wrong in Retiree Basic Retrive api")
                    )
                )
            }
        }
    }


}