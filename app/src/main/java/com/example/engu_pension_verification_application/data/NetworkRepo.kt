package com.example.engu_pension_verification_application.data

import com.example.engu_pension_verification_application.model.input.BookAppointmentRequest
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
import com.example.engu_pension_verification_application.model.input.TopUpRequest
import com.example.engu_pension_verification_application.model.input.TransferRequest
import com.example.engu_pension_verification_application.model.input.VideoCallRequest
import com.example.engu_pension_verification_application.network.ApiInterface
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.SharedPref
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

    fun submitActiveBasicDetails(inputActiveBasicDetails: InputActiveBasicDetails) =
        apiInterface.submitActiveDetails(NetworkUtils.getAccessToken(), inputActiveBasicDetails)

    suspend fun fetchActiveDocuments() =
        apiInterface.getActiveDocRetrive(NetworkUtils.getAccessToken())

    suspend fun fetchRetireeDocuments() =
        apiInterface.getRetireeDocRetrive(NetworkUtils.getAccessToken())

    suspend fun fetchBankDetails(inputSwiftBankCode: InputSwiftBankCode) =
        apiInterface.getSwiftBankCode(NetworkUtils.getAccessToken(), inputSwiftBankCode)

    suspend fun submitBankInfo(inputActiveBankInfo: InputActiveBankInfo) =
        apiInterface.submitBankInfo(NetworkUtils.getAccessToken(), inputActiveBankInfo)

    fun verifyBankAccount(inputBankVerification: InputBankVerification) =
        apiInterface.getBankVerify(NetworkUtils.getAccessToken(), inputBankVerification)

    suspend fun submitEin(ein: String) =
        apiInterface.getEinNumber(NetworkUtils.getAccessToken(), InputEinNumber(ein))

    suspend fun fetchRefreshToken() =
        apiInterface.getRefreshToken(InputRefreshToken(SharedPref.refresh_token))

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

    fun submitRetireeBasicDetails(inputRetireeBasicDetail: InputRetireeBasicDetails) =
        apiInterface.submitRetireeDetails(NetworkUtils.getAccessToken(), inputRetireeBasicDetail)

    suspend fun logout() = apiInterface.getLogout(NetworkUtils.getAccessToken())
    suspend fun fetchDashboardDetails() =
        apiInterface.getDashBoardDetails(NetworkUtils.getAccessToken())

    suspend fun getAccountCompletionStatus() =
        apiInterface.getAccountCompletionStatus(NetworkUtils.getAccessToken())

    suspend fun topUp(topUpRequest: TopUpRequest) =
        apiInterface.topUp(NetworkUtils.getAccessToken(), topUpRequest)

    fun topUpCall(topUpRequest: TopUpRequest) =
        apiInterface.topUpCall(NetworkUtils.getAccessToken(), topUpRequest)

    suspend fun getPaymentStatus(sessionId: String) =
        apiInterface.getPaymentStatus(NetworkUtils.getAccessToken(), sessionId)

    suspend fun fetchBookingSlots(selectedDay: String) =
        apiInterface.fetchBookingSlots(NetworkUtils.getAccessToken(), selectedDay)

    fun fetchBookingDateRange() = apiInterface.fetchBookingDateRange()

    suspend fun bookAppointment(request: BookAppointmentRequest) =
        apiInterface.bookAppointment(NetworkUtils.getAccessToken(), request)

    fun bookAppointmentCall(request: BookAppointmentRequest) =
        apiInterface.bookAppointmentCall(NetworkUtils.getAccessToken(), request)

    fun transferToFinalAccount(request: TransferRequest) =
        apiInterface.transferToFinalAccount(NetworkUtils.getAccessToken(), request)

    suspend fun fetchVideoCallLink(request: VideoCallRequest) =
        apiInterface.fetchVideoCallLink(NetworkUtils.getAccessToken(), request)

    suspend fun fetchTransactionHistory(page: Int, limit: Int) =
        apiInterface.fetchTransactionHistory(NetworkUtils.getAccessToken(), page, limit)
}