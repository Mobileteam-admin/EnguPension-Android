package com.enugu.pension.data

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import com.enugu.pension.model.request.BookAppointmentRequest
import com.enugu.pension.model.request.ExtraBankAccountRequest
import com.enugu.pension.model.request.InputActiveBankInfo
import com.enugu.pension.model.request.InputActiveBasicDetails
import com.enugu.pension.model.request.InputBankVerification
import com.enugu.pension.model.request.InputEinNumber
import com.enugu.pension.model.request.InputForgotPassword
import com.enugu.pension.model.request.InputForgotVerify
import com.enugu.pension.model.request.InputLGAList
import com.enugu.pension.model.request.InputLogin
import com.enugu.pension.model.request.InputRefreshToken
import com.enugu.pension.model.request.InputResendotp
import com.enugu.pension.model.request.InputResetPassword
import com.enugu.pension.model.request.InputRetireeBasicDetails
import com.enugu.pension.model.request.InputSignup
import com.enugu.pension.model.request.InputSignupVerify
import com.enugu.pension.model.request.InputSwiftBankCode
import com.enugu.pension.model.request.TopUpRequest
import com.enugu.pension.model.request.TransferRequest
import com.enugu.pension.model.request.VideoCallRequest
import com.enugu.pension.network.ApiInterface
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

    suspend fun createExtraBankAccount(request: ExtraBankAccountRequest) =
        apiInterface.createExtraBankAccount(NetworkUtils.getAccessToken(), request)

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

    suspend fun fetchBankAccountList() =
        apiInterface.fetchBankAccountList(NetworkUtils.getAccessToken())

    suspend fun fetchStatementPdfLink() =
        apiInterface.fetchStatementPdfLink(NetworkUtils.getAccessToken(), SharedPref.user_id!!.toInt())

    fun downloadFile(
        downloadManager:DownloadManager,
        fileUrl: String, fileName: String,
        downloadDescription: String = "Downloading file..."
    ): Long {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription(downloadDescription)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
        return downloadManager.enqueue(request)
    }

    suspend fun downloadCacheFile(fileUrl: String, cacheFile: File): File? {
            return withContext(Dispatchers.IO) {
                try {
                    val responseBody: ResponseBody = apiInterface.downloadFile(fileUrl)
                    val inputStream: InputStream = responseBody.byteStream()
                    val outputStream = FileOutputStream(cacheFile)
                    inputStream.use { it.copyTo(outputStream) } // Safe copy
                    outputStream.close()
                    cacheFile
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
}