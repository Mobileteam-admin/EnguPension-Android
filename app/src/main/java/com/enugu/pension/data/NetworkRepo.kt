package com.enugu.pension.data

import android.app.DownloadManager
import android.content.ContentResolver
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
import com.enugu.pension.model.request.NextOfKinRequest
import com.enugu.pension.model.request.StatementRequest
import com.enugu.pension.model.request.TopUpRequest
import com.enugu.pension.model.request.TransferRequest
import com.enugu.pension.model.request.UpdateProfileForm
import com.enugu.pension.model.response.BookingSlotResponse
import com.enugu.pension.model.response.ProfileResponse
import com.enugu.pension.model.response.ProfileUpdateResponse
import com.enugu.pension.model.response.SwiftCodeVerificationResponse
import com.enugu.pension.network.ApiInterface
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.TimeZone

class NetworkRepo(private val apiInterface: ApiInterface) {
    private fun createStringPart(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun createImagePart(file: File?, paramName: String): MultipartBody.Part? {
        return file?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, it.name, requestFile)
        }
    }

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

    suspend fun fetchBankDetails(inputSwiftBankCode: InputSwiftBankCode): SwiftCodeVerificationResponse {
        /* Temporary solution to overcome the external swift code API error
        return SwiftCodeVerificationResponse(
            detail = SwiftCodeVerificationResponse.Detail(
                status = AppConstants.SUCCESS,
                tokenStatus = AppConstants.SUCCESS,
                message = "",
                swiftCodeResponse = SwiftCodeResponse(
                    branchName = "Fort Branch",
                    cityName = "Mumbai, India",
                    bankName = "Fort Branch",
                ),)
        )*/
        return apiInterface.fetchBankDetails(NetworkUtils.getAccessToken(), inputSwiftBankCode)
    }

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

    suspend fun fetchBookingSlots(selectedDay: String): BookingSlotResponse {
        val country = Locale.getDefault().displayCountry

        val timezone = TimeZone.getDefault().id
        return apiInterface.fetchBookingSlots(
            token = NetworkUtils.getAccessToken(),
            selectedDay = selectedDay,
            country = country,
            timezone = timezone
        )
    }

    fun fetchBookingDateRange() = apiInterface.fetchBookingDateRange()

    suspend fun bookAppointment(request: BookAppointmentRequest) =
        apiInterface.bookAppointment(NetworkUtils.getAccessToken(), request)

    fun bookAppointmentCall(request: BookAppointmentRequest) =
        apiInterface.bookAppointmentCall(NetworkUtils.getAccessToken(), request)

    fun transferToFinalAccount(request: TransferRequest) =
        apiInterface.transferToFinalAccount(NetworkUtils.getAccessToken(), request)

    suspend fun createVideoCallRoom() =
        apiInterface.createVideoCallRoom(NetworkUtils.getAccessToken())

    suspend fun fetchTransactionHistory(page: Int, limit: Int) =
        apiInterface.fetchTransactionHistory(NetworkUtils.getAccessToken(), page, limit)

    suspend fun fetchBankAccountList() =
        apiInterface.fetchBankAccountList(NetworkUtils.getAccessToken())

    suspend fun fetchStatementPdfLink() =
        apiInterface.fetchStatementPdfLink(
            NetworkUtils.getAccessToken(),
        )

    suspend fun fetchProfileDetails() =
        apiInterface.fetchProfileDetails(NetworkUtils.getAccessToken())

    suspend fun updateProfileDetails(updateProfileForm: UpdateProfileForm): ProfileUpdateResponse {
        val stringParts: Map<String, RequestBody> =
            updateProfileForm.items.associate { it.key to createStringPart(it.value) }
        val imagePart = createImagePart(updateProfileForm.profilePicFile, "profile_picture")
        return apiInterface.updateProfileDetails(
            NetworkUtils.getAccessToken(),
            stringParts,
            imagePart
        )
    }

    fun downloadFile(
        downloadManager: DownloadManager,
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

    suspend fun downloadFile(fileUrl: String, file: File): File? {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody: ResponseBody = apiInterface.downloadFile(fileUrl)
                val inputStream: InputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(file)
                inputStream.use { it.copyTo(outputStream) }
                outputStream.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun downloadFile(fileUrl: String, uri: Uri, contentResolver: ContentResolver): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody: ResponseBody = apiInterface.downloadFile(fileUrl)
                val inputStream = responseBody.byteStream()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream.use { it.copyTo(outputStream) }
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun fetchReservationDetails() =
        apiInterface.fetchReservationDetails(NetworkUtils.getAccessToken())

    suspend fun fetchVerificationHistory() =
        apiInterface.fetchVerificationHistory(NetworkUtils.getAccessToken())

    suspend fun fetchNextOfKinDetails() =
        apiInterface.fetchNextOfKinDetails(NetworkUtils.getAccessToken())

    suspend fun fetchAccountDetails() =
        apiInterface.fetchAccountDetails(NetworkUtils.getAccessToken())

    suspend fun fetchStatement(startDate: String, endDate: String) =
        apiInterface.fetchStatement(
            NetworkUtils.getAccessToken(),
            StatementRequest(startDate, endDate)
        )

    suspend fun submitNextOfKinDetails(nextOfKinRequest: NextOfKinRequest) =
        apiInterface.submitNextOfKinDetails(NetworkUtils.getAccessToken(), nextOfKinRequest)
}