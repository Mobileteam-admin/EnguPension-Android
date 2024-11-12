package com.example.engu_pension_verification_application.ui.fragment.signup.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.response.LoginDetail
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.ui.activity.DashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ServiceActivity
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_splash2.*
import java.util.regex.Pattern
import kotlin.math.log


var loginGovResponse : String? = null

@Suppress("UNREACHABLE_CODE")
class LoginFragment : Fragment(),LoginViewCallBack {


    private lateinit var loginviewModel: LoginViewModel

    val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )


    var Ph_no: String = ""
    var email_Phn: String = ""

    //var loginGovResponse : String? = null

    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //loginviewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginviewModel = LoginViewModel(this)

        login_ccp.registerPhoneNumberTextView(et_login_phone)
        onClicked()
        //observeLoggedIn()
    }

    private fun observeLoggedIn() {
        loginviewModel.loginStatus.observe(viewLifecycleOwner, Observer { responselogin ->

            Loader.hideLoader()

            if (responselogin.login_detail?.status.equals("success")) {
                Toast.makeText(context, responselogin.login_detail!!.message, Toast.LENGTH_LONG)
                    .show()

                //check account completion true or not with local storage
                //true - dashboar
              /*  val intent = Intent(context, DashboardActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/

                //false - service
                val intent = Intent(context, ServiceActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(context, responselogin.login_detail!!.message, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun onClicked() {
        ll_log_signup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }
        ll_log_login.setOnClickListener {
            if (isValidLogin()) {
                if (isValidate_password()) {
                    Loader.showLoader(requireContext())
                    if (context?.isConnectedToNetwork()!!) {
                        Log.d(
                            "Login",
                            "onClicked: " + com.example.engu_pension_verification_application.model.input.InputLogin(
                                ed_password.text.toString(),
                                email_Phn
                            )
                        )
                        loginviewModel.doLogin(
                            com.example.engu_pension_verification_application.model.input.InputLogin(
                                ed_password.text.toString(),
                                email_Phn
                            )
                        )
                    } else {
                        Loader.hideLoader()
                        Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            /*val intent = Intent(context, DashboardActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)*/
        }
        text_forgotPass.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotpassword)
        }
    }

    private fun isValidate_password(): Boolean {
        if (TextUtils.isEmpty(ed_password.text)) {
            txt_password_error.visibility = View.VISIBLE
            return false
        } else {
            txt_password_error.visibility = View.GONE
            return true
        }
        return true
    }

    private fun isValidLogin(): Boolean {


        if (TextUtils.isEmpty(ed_email_phn.text)) {

            if (TextUtils.isEmpty(et_login_phone.text)) {
                Toast.makeText(context, "Please enter email or phone number", Toast.LENGTH_LONG)
                    .show()
                return false
            } else if ((!login_ccp.isValid)) {
                txt_login_phone_error.visibility = View.VISIBLE
                return false
            } else {
                txt_login_phone_error.visibility = View.GONE
                email_Phn = "+" + login_ccp.fullNumber
                return true
            }
            return false

        } else {

            if (!EMAIL_ADDRESS_PATTERN.matcher(
                    ed_email_phn.text.toString()
                ).matches()
            ) {
                txt_loginemail_error.visibility = View.VISIBLE
                return false
            } else {
                txt_loginemail_error.visibility = View.GONE
                email_Phn = ed_email_phn.text.toString()
                return true
            }

        }



        return true
    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun onLoginSuccess(response: ResponseLogin) {
        Loader.hideLoader()
        Toast.makeText(context, response.login_detail!!.message, Toast.LENGTH_LONG)
            .show()

        loginGovResponse = response.login_detail.userGovtVerified.toString()

        Log.d("gov status login", "variable set$loginGovResponse")

        if (loginGovResponse == "False" || loginGovResponse == "false"){

            prefs.isGovVerify = false
            Log.d("gov status login", "false set $loginGovResponse")

        }else{
            prefs.isGovVerify = true
            Log.d("gov status login", " true set $loginGovResponse")
        }


        if (prefs.isGovVerify == true || prefs.lastActivityDashboard == true){
            val dashIntent = Intent(context, DashboardActivity::class.java)
            dashIntent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d("pref status", "dash intent gov verify : ${prefs.isGovVerify} last dash:${prefs.lastActivityDashboard}")

            startActivity(dashIntent)
        }
        else{
            Log.d("pref status", "service intent gov verify : ${prefs.isGovVerify} last dash:${prefs.lastActivityDashboard}")
            val serviceIntent = Intent(context, ServiceActivity::class.java)
            serviceIntent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(serviceIntent)
        }

    }

    override fun onLoginFail(response: ResponseLogin) {
        Loader.hideLoader()
        Toast.makeText(context, response.login_detail!!.message, Toast.LENGTH_LONG)
            .show()
    }

}