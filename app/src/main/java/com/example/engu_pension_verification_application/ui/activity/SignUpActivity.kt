package com.example.engu_pension_verification_application.ui.activity


import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.ExitAppDialog
import java.security.AccessController.getContext


class SignUpActivity : AppCompatActivity(), ExitAppDialog.ExitClick {
    private lateinit var signUp_navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUp_navController = Navigation.findNavController(this, R.id.nav_host_fragment_signup)


    }


    /* override fun onBackPressed() {
         Toast.makeText(this,"back button pressed",Toast.LENGTH_LONG).show()
        *//* if (findNavController(R.id.nav_host_fragment_signup).currentDestination?.id == R.id.navigation_login) {
           // ExitAppDialog.showDialog(this, this)
           // super.onBackPressed()
        } else {
           // onBackPressedDispatcher.onBackPressed()
           // super.onBackPressed()
            //super.getOnBackPressedDispatcher().onBackPressed()
//supportFragmentManager.popBackStack()
        }*//*
    }*/


    /*  override fun onSupportNavigateUp(): Boolean {
          //getSupportFragmentManager().popBackStack()
          // onBackPressed()
        //  onBackPressedDispatcher.onBackPressed()
        //  super.onSupportNavigateUp()
          return true
      }*/

    override fun proceedExit() {
        // finish()
    }

    private fun OnBackPressedMethod() {
        if (findNavController(R.id.nav_host_fragment_signup).currentDestination?.id == R.id.navigation_login) {
            ExitAppDialog.showDialog(this, this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}