package com.example.engu_pension_verification_application.commons

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.engu_pension_verification_application.R

object ExitAppDialog {

    private lateinit var builder: AlertDialog.Builder
    private lateinit var customLayout: View
    private lateinit var dialog: AlertDialog

    fun showDialog(context: Context, exitClick: ExitClick){
        try {
            builder = AlertDialog.Builder( context )
            customLayout = LayoutInflater.from( context ).inflate(R.layout.exit_app_dialog,null)
            builder.setView(customLayout)
            dialog = builder.create()
            dialog.setCancelable(false)

            val confirm = customLayout.findViewById<TextView>(R.id.confirm)
            val cancel = customLayout.findViewById<TextView>(R.id.cancel)

            confirm.setOnClickListener {
                exitClick.proceedExit()
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



    interface ExitClick{
        fun proceedExit()
    }

}