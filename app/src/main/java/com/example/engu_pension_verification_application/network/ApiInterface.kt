package com.example.engu_pension_verification_application.network


import com.example.engu_pension_verification_application.model.input.*
import com.example.engu_pension_verification_application.model.response.*
import okhttp3.RequestBody
import retrofit2.http.*


interface ApiInterface {

    @POST("/api/v1/register")
    suspend fun getSignUp(@Body inputSignup: InputSignup): SignupResponse


    /*@POST("/api/v1/verify/")
    suspend fun getVerifyOTP(@Body inputVerify: InputVerify): VerifyResponse*/

    @POST("/api/v1/registration_verify")
    suspend fun getVerifyRegistrationOTP(@Body inputSignupVerify: InputSignupVerify): VerifyResponse

    @POST("/api/v1/forgot_password_verify")
    suspend fun getVerifyForgotOTP(@Body inputForgotVerify: InputForgotVerify): VerifyResponse

    @POST("/api/v1/reverify")
    suspend fun getResendOTP(@Body inputResendotp: InputResendotp): ResendotpResponse

    @POST("/api/v1/token")
    suspend fun getLogin(@Body inputLogin: InputLogin): ResponseLogin

    @POST("/api/v1/token/refresh")
    suspend fun getRefreshToken(@Body inputRefreshToken: InputRefreshToken): ResponseRefreshToken

    @POST("/api/v1/token/logout")
    suspend fun getLogout(@Header("Authorization") token: String): ResponseLogout

    @POST("/api/v1/forgot-password")
    suspend fun getForgotPassword(@Body inputForgotPassword: InputForgotPassword): ResponseForgotPassword

    @POST("/api/v1/reset-password")
    suspend fun getResetPassword(@Body inputResetPassword: InputResetPassword): ResponseResetPassword

    @POST("/api/v1/account_completion/details")
    suspend fun getCombinationDetails(
        @Body inputBankList: InputLGAList
    ): ResponseCombinationDetails
    // @Header("Authorization") token: String,


    //get banklist,accountypelist based on country
    @GET("/api/v1/banks")
    suspend fun getAddedBanks(@Header("Authorization") token: String): ResponseBankList






    @POST("/api/v1/get_bank_details")
    suspend fun getSwiftBankCode(
        @Header("Authorization") token: String, @Body inputSwiftBankCode: InputSwiftBankCode
    ): ResponseSwiftBankCode






    @POST("/api/v1/account_completion/active")
    suspend fun getActiveDetails(
        @Header("Authorization") token: String,
        @Body inputActiveBasicDetails: InputActiveBasicDetails
    ): ResponseActiveBasicDetails

    @POST("/api/v1/account_completion/retiree")
    suspend fun getRetireeDetails(
        @Header("Authorization") token: String,
        @Body inputRetireeBasicDetail: InputRetireeBasicDetails
    ): ResponseRetireeBasicDetails

    @POST("/api/v1/create-or-update-bank-details")
    suspend fun getActiveBankInfo(
        @Header("Authorization") token: String,
        @Body inputActiveBankinfo: InputActiveBankInfo
    ): ResponseBankInfo

    @POST("/api/v1/save_ein")
    suspend fun getEinNumber(
        @Header("Authorization") token: String,
        @Body inputEinNumber: InputEinNumber
    ): ResponseEinNumber
    /* @Multipart
     @POST("/api/v1/account_completion/doc_upload_active")
     suspend fun upLoadActiveUserDetails(
         @Header("Authorization") token: String,
         @Part application_form_file : MultipartBody.Part,
         @Part promotion_letter_transfer_letter_file : MultipartBody.Part,
         @Part id_card_file : MultipartBody.Part,
         @Part passport_photo_file : MultipartBody.Part,
         @Part clearance_form_file : MultipartBody.Part
     ): ResponseActiveDocUpload*/

    @POST("/api/v1/account_completion/doc_upload_active")
    suspend fun upLoadActiveUserDocuments(
        @Header("Authorization") token: String,
        @Body file: RequestBody
    ): ResponseActiveDocUpload

    @POST("/api/v1/account_completion/doc_upload_retired")
    suspend fun upLoadRetireeeUserDocuments(
        @Header("Authorization") token: String,
        @Body file: RequestBody
    ): ResponseRetireeDocUpload

    @GET("/api/v1/account_completion/doc_retrieve_active")
    suspend fun getActiveDocRetrive(@Header("Authorization") token: String): ResponseActiveDocRetrive

    //Retiree Retrive
    @GET("/api/v1/account_completion/doc_retrieve_retired")
    suspend fun getRetireeDocRetrive(@Header("Authorization") token: String): ResponseRetireeDocRetrive

// doc upload -eg
    /* suspend fun setDocumentUpload(
         @Header("Authorization") authorization: String, @Body file: RequestBody
     ): ResponseDocumentUpload*/

    //active retrive
    @GET("/api/v1/account_completion/basic_details_active")
    suspend fun getActiveBasicRetrive(@Header("Authorization") token: String): ResponseActiveBasicRetrive

    //Retiree retrive
    @GET("/api/v1/account_completion/basic_details_retired")
    suspend fun getRetireeBasicRetrive(@Header("Authorization") token: String): ResponseRetireeBasicRetrive

    @GET("/api/v1/users_govt_verfication_status")
    suspend fun getActiveProcessingVerify(@Header("Authorization") token: String): ResponseActiveProcessingVerify

    @GET("/api/v1/profile/dashboard_details")
    suspend fun getDashBoardDetails(@Header("Authorization") token: String): ResponseDashboardDetails

    @POST("/api/v1/verify_bank_account")
    suspend fun getBankVerify(
        @Header("Authorization") token: String,
        @Query("account_number") accountNumber: String,
        @Query("bank_code") bankCode: String,
    ): ResponseBankVerify







    @POST("/api/v1/verify_bank_account")
    suspend fun getBankVerify(
        @Header("Authorization") token: String,
        @Body inputBankVerification: InputBankVerification
    ): ResponseBankVerify


}