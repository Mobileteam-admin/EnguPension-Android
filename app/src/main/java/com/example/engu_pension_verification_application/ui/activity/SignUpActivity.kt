package com.example.engu_pension_verification_application.ui.activity


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.ExitAppDialog
import java.security.AccessController.getContext


class SignUpActivity : BaseActivity(), ExitAppDialog.ExitClick {
    private lateinit var signUp_navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUp_navController = Navigation.findNavController(this, R.id.nav_host_fragment_signup)


    }

    override fun proceedExit() {
        // finish()
    }
}