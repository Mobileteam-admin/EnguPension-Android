package com.example.engu_pension_verification_application.data

import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputActiveBasicDetails
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.input.InputEinNumber
import com.example.engu_pension_verification_application.model.input.InputForgotPassword
import com.example.engu_pension_verification_application.model.input.InputForgotVerify
import com.example.engu_pension_verification_application.model.input.InputLGAList
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.input.InputRefreshToken
import com.example.engu_pension_verification_application.model.input.InputResendotp
import com.example.engu_pension_verification_application.model.input.InputResetPassword
import com.example.engu_pension_verification_application.model.input.InputSignup
import com.example.engu_pension_verification_application.model.input.InputSignupVerify
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.ResendotpResponse
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload
import com.example.engu_pension_verification_application.model.response.ResponseActiveProcessingVerify
import com.example.engu_pension_verification_application.model.response.ResponseBankInfo
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseBankVerify
import com.example.engu_pension_verification_application.model.response.ResponseCombinationDetails
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.model.response.ResponseResetPassword
import com.example.engu_pension_verification_application.model.response.ResponseSwiftBankCode
import com.example.engu_pension_verification_application.model.response.SignupResponse
import com.example.engu_pension_verification_application.model.response.VerifyResponse
import com.example.engu_pension_verification_application.network.ApiInterface
import com.example.engu_pension_verification_application.utils.NetworkUtils
import okhttp3.RequestBody

class NetworkRepo(private val apiInterface: ApiInterface) {
    suspend fun login(inputLogin: InputLogin): ResponseLogin {
        return apiInterface.getLogin(inputLogin)
    }
    suspend fun signUp(inputSignup: InputSignup): SignupResponse {
        return apiInterface.getSignUp(inputSignup)
    }
    suspend fun verifyOTP(inputSignupVerify: InputSignupVerify): VerifyResponse {
        return apiInterface.getVerifyRegistrationOTP(inputSignupVerify)
    }
    suspend fun verifyForgotPassword(inputForgotVerify: InputForgotVerify): VerifyResponse {
        return apiInterface.getVerifyForgotOTP(inputForgotVerify)
    }
    suspend fun resendOTP(inputResendOtp: InputResendotp): ResendotpResponse {
        return apiInterface.getResendOTP(inputResendOtp)
    }
    suspend fun forgotPassword(inputForgotPassword: InputForgotPassword): ResponseForgotPassword {
        return apiInterface.getForgotPassword(inputForgotPassword)
    }
    suspend fun resetPassword(inputResetPassword: InputResetPassword): ResponseResetPassword {
        return apiInterface.getResetPassword(inputResetPassword)
    }
    suspend fun fetchBankList(): ResponseBankList {
//        return ApiUtil.handleResponse(apiInterface.fetchBankList(ApiUtil.getAccessToken()))
        return apiInterface.getAddedBanks(NetworkUtils.getAccessToken())
    }
    suspend fun fetchCombinedDetails(inputLGAList: InputLGAList): ResponseCombinationDetails {
        return apiInterface.getCombinationDetails(inputLGAList)
    }
    suspend fun fetchActiveBasicDetails(): ResponseActiveBasicRetrive {
        return apiInterface.getActiveBasicRetrive(NetworkUtils.getAccessToken())
    }
    suspend fun uploadAccountDetails(inputActiveBasicDetails: InputActiveBasicDetails): ResponseActiveBasicDetails {
        return apiInterface.getActiveDetails(NetworkUtils.getAccessToken(), inputActiveBasicDetails)
    }
    suspend fun fetchActiveDocuments(): ResponseActiveDocRetrive {
        return apiInterface.getActiveDocRetrive(NetworkUtils.getAccessToken())
    }
    suspend fun fetchBankDetails(inputSwiftBankCode: InputSwiftBankCode): ResponseSwiftBankCode {
        return apiInterface.getSwiftBankCode(NetworkUtils.getAccessToken(),inputSwiftBankCode)
    }
    suspend fun submitBankInfo(inputActiveBankInfo: InputActiveBankInfo): ResponseBankInfo {
        return apiInterface.getActiveBankInfo(NetworkUtils.getAccessToken(),inputActiveBankInfo)
    }
    suspend fun verifyBankAccount(inputBankVerification: InputBankVerification): ResponseBankVerify {
        return apiInterface.getBankVerify(NetworkUtils.getAccessToken(),inputBankVerification)
    }
    suspend fun submitEin(ein: String): ResponseEinNumber {
        return apiInterface.getEinNumber(NetworkUtils.getAccessToken(), InputEinNumber(ein))
    }
    suspend fun fetchRefreshToken(): ResponseRefreshToken {
        return apiInterface.getRefreshToken(InputRefreshToken(AppConstants.REFRESH_TOKEN))
    }

    suspend fun uploadActiveDocuments(requestBody: RequestBody): ResponseActiveDocUpload {
        return apiInterface.upLoadActiveUserDocuments(
            NetworkUtils.getAccessToken(),
            requestBody
        )
    }
    suspend fun getGovtVerificationStatus(): ResponseActiveProcessingVerify {
        return apiInterface.getActiveProcessingVerify(NetworkUtils.getAccessToken())
    }
}