package com.example.engu_pension_verification_application.commons

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.engu_pension_verification_application.R


@SuppressLint("StaticFieldLeak")
object Loader {

    private lateinit var builder: AlertDialog.Builder
    private lateinit var customLayout: View
    private lateinit var dialog: AlertDialog

    fun showLoader(context: Context){
        try {
            builder = AlertDialog.Builder( context )
            customLayout = LayoutInflater.from( context ).inflate(R.layout.loader,null)
            builder.setView(customLayout)
            dialog = builder.create()
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            dialog.show()
        } catch (e: Exception) {
        }
    }

    fun hideLoader(){
        try {
            dialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}