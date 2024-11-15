package com.example.engu_pension_verification_application.data

import android.util.Log
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputActiveBasicDetails
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.input.InputEinNumber
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.input.InputRefreshToken
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload
import com.example.engu_pension_verification_application.model.response.ResponseBankInfo
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseBankVerify
import com.example.engu_pension_verification_application.model.response.ResponseCombinationDetails
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.model.response.ResponseSwiftBankCode
import com.example.engu_pension_verification_application.network.ApiInterface
import com.example.engu_pension_verification_application.utils.ApiUtil
import okhttp3.RequestBody

class NetworkRepo(private val apiInterface: ApiInterface) {
    suspend fun fetchBankList(): ResponseBankList {
//        return ApiUtil.handleResponse(apiInterface.fetchBankList(ApiUtil.getAccessToken()))
        return apiInterface.getAddedBanks(ApiUtil.getAccessToken())
    }
    suspend fun fetchCombinedDetails(inputLGAList: InputLGAList): ResponseCombinationDetails {
        return apiInterface.getCombinationDetails(inputLGAList)
    }
    suspend fun fetchActiveBasicDetails(): ResponseActiveBasicRetrive {
        return apiInterface.getActiveBasicRetrive(ApiUtil.getAccessToken())
    }
    suspend fun uploadAccountDetails(inputActiveBasicDetails: InputActiveBasicDetails): ResponseActiveBasicDetails {
        return apiInterface.getActiveDetails(ApiUtil.getAccessToken(), inputActiveBasicDetails)
    }
    suspend fun fetchActiveDocuments(): ResponseActiveDocRetrive {
        return apiInterface.getActiveDocRetrive(ApiUtil.getAccessToken())
    }
    suspend fun fetchBankDetails(inputSwiftBankCode: InputSwiftBankCode): ResponseSwiftBankCode {
        return apiInterface.getSwiftBankCode(ApiUtil.getAccessToken(),inputSwiftBankCode)
    }
    suspend fun submitBankInfo(inputActiveBankInfo: InputActiveBankInfo): ResponseBankInfo {
        return apiInterface.getActiveBankInfo(ApiUtil.getAccessToken(),inputActiveBankInfo)
    }
    suspend fun verifyBankAccount(inputBankVerification: InputBankVerification): ResponseBankVerify {
        return apiInterface.getBankVerify(ApiUtil.getAccessToken(),inputBankVerification)
    }
    suspend fun submitEin(ein: String): ResponseEinNumber {
        return apiInterface.getEinNumber(ApiUtil.getAccessToken(), InputEinNumber(ein))
    }
    suspend fun fetchRefreshToken(): ResponseRefreshToken {
        return apiInterface.getRefreshToken(InputRefreshToken(AppConstants.REFRESH_TOKEN))
    }

    suspend fun uploadActiveDocuments(requestBody: RequestBody): ResponseActiveDocUpload {
        return apiInterface.upLoadActiveUserDocuments(
            ApiUtil.getAccessToken(),
            requestBody
        )
    }
}