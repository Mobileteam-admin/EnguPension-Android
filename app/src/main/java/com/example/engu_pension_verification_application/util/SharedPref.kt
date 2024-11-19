package com.example.engu_pension_verification_application.util

import android.content.Context
import android.content.SharedPreferences
import com.example.engu_pension_verification_application.Constants.AppConstants

object SharedPref {


    private lateinit var sharedPreferences: SharedPreferences

    private const val PREF_NAME = "prefName"

    private const val USER_FIRST_NAME = "first_name"
    private const val USER_MIDDLE_NAME = "middle_name"
    private const val USER_LAST_NAME = "last_name"

    //RETIREE_VALS
    private const val RUSER_FIRST_NAME = "Rfirst_name"
    private const val RUSER_MIDDLE_NAME = "Rmiddle_name"
    private const val RUSER_LAST_NAME = "Rlast_name"

    private val IS_ACTIVE_BASIC_SUMBIT = "is_submit"
    private val IS_ACTIVE_DOC_SUMBIT = "is_submit"
    private val IS_ACTIVE_BANK_SUBMIT = "is_submit"

    private val IS_R_BASIC_SUMBIT = "is_submit"
    private val IS_R_DOC_SUMBIT = "is_submit"
    private val IS_R_BANK_SUBMIT = "is_submit"



  /*  //trial
    private const val USER_SEX = "sex"
    private const val USER_DOB = "dob"
    private const val USER_DOA = "doa"
    private const val USER_ADDRESS = "address"

    private const val A_LGA = "lga"
    //nasiya
    private const val A_SUB = "sub"
    private const val A_GRADE = "grade"
    private const val A_OCC = "Occupation"
    private const val OA_OCC = "a_Occupation"

    //nasiya RetireeeSpinners

    private const val R_LGA = "rlga"
    private const val R_LPGA = "rlpga"
    private const val R_SUB = "rsub"
    private const val R_GRADE = "rgrade"
    private const val R_LP = "rLastPosition"

    private const val KIN_NAME = "kin_name"
    private const val KIN_EMAIL = "kin_email"
    private const val KIN_PHONE = "kin_phone"
    private const val KIN_ADDRESS = "kin_address"*/

    /*private const val RUSER_ADDRESS = "Raddress"

    private const val RUSER_DOB = "Rdob"

    //trial
    private const val RUSER_SEX = "Rsex"

    private const val RKIN_NAME = "Rkin_name"
    private const val RKIN_EMAIL = "Rkin_email"
    private const val RKIN_PHONE = "Rkin_phone"
    private const val RKIN_ADDRESS = "Rkin_address"
    private const val RUSER_DOA = "Rdoa"
    private const val RUSER_LYP = "RLYP"
    private const val RUSER_DOR = "Rdor"
*/


    private const val USER_DETAILS = "userDetails"
    private val IS_LOGIN = "is_login"

    private val IS_DASH_LAST = "is_dash_last"
    private val IS_GOV_VERIFIED = "is_gov_verified"
    private const val USER_ID = "userid"
    private const val USER_NAME = "username"
    private const val USER_EMAIL = "useremail"
    private const val USER_ACCESS_TOKEN = "access_token"
    private const val USER_REFRESH_TOKEN = "refresh_token"

    /*private const val USER_BANK_VERIFY = "refresh_token"*/


    fun logout() {
        isLogin = false
        user_id = ""
        user_name = ""
        email = ""
        access_token = ""
        refresh_token = ""
        lastActivityDashboard = false
        isGovVerify = false
    }


    var lastActivityDashboard: Boolean
        get() = sharedPreferences.getBoolean(IS_DASH_LAST, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_DASH_LAST, value).apply()
    var isGovVerify : Boolean
        get() = sharedPreferences.getBoolean(IS_GOV_VERIFIED, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_GOV_VERIFIED, value).apply()

    /*var isBankVerify : Boolean
        get() = sharedPreferences.getBoolean(USER_BANK_VERIFY, false)
        set(value) = sharedPreferences.edit().putBoolean(USER_BANK_VERIFY, value).apply()*/



/*    //ACTIVE_BANK
    private const val ABANK = "A_BANK"     //SPINNER_LIST

    private const val AACC_NO = "A_ACC_NO"
    private const val AREACC_NO = "ARE_ACC_NO"
    private const val ASWIFT_CODE = "AS_CODE"
    private const val ABANK_CODE = "AB_CODE"

    private const val AACC_TYPE = "A_ACCTYPE"   //SPINNER_LIST


//END BANK

    //R_BANK
    private const val RBANK = "R_BANK"     //SPINNER_LIST

    private const val RACC_NO = "R_ACC_NO"
    private const val RREACC_NO = "RRE_ACC_NO"
    private const val RSWIFT_CODE = "RS_CODE"
    private const val RBANK_CODE = "RB_CODE"

    private const val RACC_TYPE = "RACCTYPE"   //SPINNER_LIST


//END BANK*/


    fun clearSession(context: Context) {
        val editor =
            context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }

    fun with(context: Context) {
        sharedPreferences =
            context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }


    var isActiveBasicSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_ACTIVE_BASIC_SUMBIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_ACTIVE_BASIC_SUMBIT, value).apply()

    var isActiveDocSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_ACTIVE_DOC_SUMBIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_ACTIVE_DOC_SUMBIT, value).apply()

    var isActiveBankSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_ACTIVE_BANK_SUBMIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_ACTIVE_BANK_SUBMIT, value).apply()




    var isRBasicSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_R_BASIC_SUMBIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_R_BASIC_SUMBIT, value).apply()

    var isRDocSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_R_DOC_SUMBIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_R_DOC_SUMBIT, value).apply()

    var isRBankSubmit : Boolean
        get() = sharedPreferences.getBoolean(IS_R_BANK_SUBMIT, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_R_BANK_SUBMIT, value).apply()



    var isLogin: Boolean
        get() = sharedPreferences.getBoolean(IS_LOGIN, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_LOGIN, value).apply()

    var user_id: String?
        get() = sharedPreferences.getString(USER_ID, "")
        set(id) = sharedPreferences.edit().putString(USER_ID, id).apply()

    var user_name: String?
        get() = sharedPreferences.getString(USER_NAME, "")
        set(value) = sharedPreferences.edit().putString(USER_NAME, value).apply()

    var email: String?
        get() = sharedPreferences.getString(USER_EMAIL, "")
        set(value) = sharedPreferences.edit().putString(USER_EMAIL, value).apply()

    var access_token: String?
        get() = sharedPreferences.getString(USER_ACCESS_TOKEN, "")
        set(value) {
            AppConstants.ACCESS_TOKEN = value ?: ""
            sharedPreferences.edit().putString(USER_ACCESS_TOKEN, value).apply()
        }

    var refresh_token: String?
        get() = sharedPreferences.getString(USER_REFRESH_TOKEN, "")
        set(value)  {
            AppConstants.REFRESH_TOKEN = value ?: ""
            sharedPreferences.edit().putString(USER_REFRESH_TOKEN, value).apply()
        }


    //active
    var first_name: String?
        get() = sharedPreferences.getString(USER_FIRST_NAME, "")
        set(value) = sharedPreferences.edit().putString(USER_FIRST_NAME, value).apply()

    var middle_name: String?
        get() = sharedPreferences.getString(USER_MIDDLE_NAME, "")
        set(value) = sharedPreferences.edit().putString(USER_MIDDLE_NAME, value).apply()

    var last_name: String?
        get() = sharedPreferences.getString(USER_LAST_NAME, "")
        set(value) = sharedPreferences.edit().putString(USER_LAST_NAME, value).apply()

    //retiree
    var Rfirst_name: String?
        get() = sharedPreferences.getString(RUSER_FIRST_NAME, "")
        set(value) = sharedPreferences.edit().putString(RUSER_FIRST_NAME, value).apply()

    var Rmiddle_name: String?
        get() = sharedPreferences.getString(RUSER_MIDDLE_NAME, "")
        set(value) = sharedPreferences.edit().putString(RUSER_MIDDLE_NAME, value).apply()

    var Rlast_name: String?
        get() = sharedPreferences.getString(RUSER_LAST_NAME, "")
        set(value) = sharedPreferences.edit().putString(RUSER_LAST_NAME, value).apply()





    //trial
    /*var sex: Int?
        get() = sharedPreferences.getInt(USER_SEX, 0)
        set(value) = sharedPreferences.edit().putInt(USER_SEX, 0).apply()*/

   /* var sex: String?
        get() = sharedPreferences.getString(USER_SEX, "")
        set(value) = sharedPreferences.edit().putString(USER_SEX, value).apply()

    var address: String?
        get() = sharedPreferences.getString(USER_ADDRESS, "")
        set(value) = sharedPreferences.edit().putString(USER_ADDRESS, value).apply()

    var doa:String?
        get() = sharedPreferences.getString(USER_DOA, "")
        set(value) = sharedPreferences.edit().putString(USER_DOA, value).apply()

    var dob:String?
        get() = sharedPreferences.getString(USER_DOB, "")
        set(value) = sharedPreferences.edit().putString(USER_DOB, value).apply()

    *//*var lga: Int?
        get() = sharedPreferences.getInt(A_LGA, 0)
        set(value) = sharedPreferences.edit().putInt(A_LGA, 0).apply()*//*


    var lga: String?
        get() = sharedPreferences.getString(A_LGA, "")
        set(value) = sharedPreferences.edit().putString(A_LGA, value).apply()

    var sub: String?
        get() = sharedPreferences.getString(A_SUB, "")
        set(value) = sharedPreferences.edit().putString(A_SUB, value).apply()

    var grade: String?
        get() = sharedPreferences.getString(A_GRADE, "")
        set(value) = sharedPreferences.edit().putString(A_GRADE, value).apply()

    var Occupation: String?
        get() = sharedPreferences.getString(A_OCC, "")
        set(value) = sharedPreferences.edit().putString(A_OCC, value).apply()
    var a_Occupation: String?
        get() = sharedPreferences.getString(OA_OCC, "")
        set(value) = sharedPreferences.edit().putString(OA_OCC, value).apply()

    var A_BANK: String?
        get() = sharedPreferences.getString(ABANK, "")
        set(value) = sharedPreferences.edit().putString(ABANK, value).apply()

    var A_ACCTYPE: String?
        get() = sharedPreferences.getString(AACC_TYPE, "")
        set(value) = sharedPreferences.edit().putString(AACC_TYPE, value).apply()

    var kin_name : String?
        get() = sharedPreferences.getString(KIN_NAME,"")
        set(value) = sharedPreferences.edit().putString(KIN_NAME,value).apply()

    var kin_email : String?
        get() = sharedPreferences.getString(KIN_EMAIL,"")
        set(value) = sharedPreferences.edit().putString(KIN_EMAIL,value).apply()

    var kin_phone : String?
        get() = sharedPreferences.getString(KIN_PHONE,"")
        set(value) = sharedPreferences.edit().putString(KIN_PHONE,value).apply()

    var kin_address : String?
        get() = sharedPreferences.getString(KIN_ADDRESS,"")
        set(value) = sharedPreferences.edit().putString(KIN_ADDRESS,value).apply()



    //A_BANK INFO
    var A_ACC_NO : String?

        get() = sharedPreferences.getString(AACC_NO,"")
        set(value) = sharedPreferences.edit().putString(AACC_NO,value).apply()

    var ARE_ACC_NO : String?

        get() = sharedPreferences.getString(AREACC_NO,"")
        set(value) = sharedPreferences.edit().putString(AREACC_NO,value).apply()*/

}