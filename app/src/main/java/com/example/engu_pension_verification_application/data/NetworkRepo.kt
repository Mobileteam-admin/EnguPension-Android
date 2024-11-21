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
import com.example.engu_pension_verification_application.model.input.InputRetireeBasicDetails
import com.example.engu_pension_verification_application.model.input.InputSignup
import com.example.engu_pension_verification_application.model.input.InputSignupVerify
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.network.ApiInterface
import com.example.engu_pension_verification_application.util.NetworkUtils
import okhttp3.RequestBody

class NetworkRepo(private val apiInterface: ApiInterface) {
    suspend fun login(inputLogin: InputLogin) = apiInterface.getLogin(inputLogin)

    suspend fun signUp(inputSignup: InputSignup) = apiInterface.getSignUp(inputSignup)

    suspend fun verifyOTP(inputSignupVerify: InputSignupVerify) =
        apiInterface.getVerifyRegistrationOTP(inputSignupVerify)

    suspend fun verifyForgotPassword(inputForgotVerify: InputForgotVerify) =
        apiInterface.getVerifyForgotOTP(inputForgotVerify)

    suspend fun resendOTP(inputResendOtp: InputResendotp) =
        apiInterface.getResendOTP(inputResendOtp)

    suspend fun forgotPassword(inputForgotPassword: InputForgotPassword) =
        apiInterface.getForgotPassword(inputForgotPassword)

    suspend fun resetPassword(inputResetPassword: InputResetPassword) =
        apiInterface.getResetPassword(inputResetPassword)

    suspend fun fetchBankList() = apiInterface.getAddedBanks(NetworkUtils.getAccessToken())

    suspend fun fetchCombinedDetails(inputLGAList: InputLGAList) =
        apiInterface.getCombinationDetails(inputLGAList)

    suspend fun fetchActiveBasicDetails() =
        apiInterface.getActiveBasicRetrive(NetworkUtils.getAccessToken())

    suspend fun uploadAccountDetails(inputActiveBasicDetails: InputActiveBasicDetails) =
        apiInterface.getActiveDetails(NetworkUtils.getAccessToken(), inputActiveBasicDetails)

    suspend fun fetchActiveDocuments() =
        apiInterface.getActiveDocRetrive(NetworkUtils.getAccessToken())

    suspend fun fetchRetireeDocuments() =
        apiInterface.getRetireeDocRetrive(NetworkUtils.getAccessToken())

    suspend fun fetchBankDetails(inputSwiftBankCode: InputSwiftBankCode) =
        apiInterface.getSwiftBankCode(NetworkUtils.getAccessToken(), inputSwiftBankCode)

    suspend fun submitBankInfo(inputActiveBankInfo: InputActiveBankInfo) =
        apiInterface.getActiveBankInfo(NetworkUtils.getAccessToken(), inputActiveBankInfo)

    suspend fun verifyBankAccount(inputBankVerification: InputBankVerification) =
        apiInterface.getBankVerify(NetworkUtils.getAccessToken(), inputBankVerification)

    suspend fun submitEin(ein: String) =
        apiInterface.getEinNumber(NetworkUtils.getAccessToken(), InputEinNumber(ein))

    suspend fun fetchRefreshToken() =
        apiInterface.getRefreshToken(InputRefreshToken(AppConstants.REFRESH_TOKEN))

    suspend fun uploadActiveDocuments(requestBody: RequestBody) =
        apiInterface.upLoadActiveUserDocuments(
            NetworkUtils.getAccessToken(), requestBody
        )

    suspend fun uploadRetireeDocuments(requestBody: RequestBody) =
        apiInterface.upLoadRetireeeUserDocuments(
            NetworkUtils.getAccessToken(), requestBody
        )

    suspend fun getGovtVerificationStatus() =
        apiInterface.getActiveProcessingVerify(NetworkUtils.getAccessToken())

    suspend fun fetchRetireeBasicDetails() =
        apiInterface.getRetireeBasicRetrive(NetworkUtils.getAccessToken())

    suspend fun submitRetireeBasicDetails(inputRetireeBasicDetail: InputRetireeBasicDetails) =
        apiInterface.getRetireeDetails(NetworkUtils.getAccessToken(), inputRetireeBasicDetail)

    suspend fun logout() = apiInterface.getLogout(NetworkUtils.getAccessToken())
    suspend fun fetchDashboardDetails() =
        apiInterface.getDashBoardDetails(NetworkUtils.getAccessToken())

    suspend fun getAccountCompletionStatus() =
        apiInterface.getAccountCompletionStatus(NetworkUtils.getAccessToken())

}