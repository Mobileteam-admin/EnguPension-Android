package com.example.engu_pension_verification_application.commons

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.engu_pension_verification_application.R

object LogoutConfirmDialog {

    private lateinit var builder: AlertDialog.Builder
    private lateinit var customLayout: View
    private lateinit var dialog: AlertDialog

    fun showDialog(context: Context, logoutClick: LogoutClick){
        try {
            builder = AlertDialog.Builder( context )
            customLayout = LayoutInflater.from( context ).inflate(R.layout.logout_dialog,null)
            builder.setView(customLayout)
            dialog = builder.create()
            dialog.setCancelable(false)

            val confirm = customLayout.findViewById<TextView>(R.id.tv_logout_confirm)
            val cancel = customLayout.findViewById<TextView>(R.id.tv_logout_cancel)

            confirm.setOnClickListener {
                logoutClick.proceedLogout()
                hideDialog() }

            cancel.setOnClickListener { hideDialog() }


            dialog.show()
        } catch (e: Exception) {
        }
    }

    fun hideDialog(){
        try {
            dialog.dismiss()
        } catch (e: Exception) {
        }
    }



    interface LogoutClick{
        fun proceedLogout()
    }
}