package com.example.engu_pension_verification_application.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.adapter.PopUpAdapter
import com.example.engu_pension_verification_application.ui.dialog.EnguDialog
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel

interface BaseUtils {
    fun provideContext(): Context
    fun provideActivity(): Activity
    fun provideFragmentManager(): FragmentManager
    fun provideLoaderViewModel(): LoaderViewModel

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_LONG) {
        showToast(provideContext().getString(messageResId), duration)
    }

    fun showMessage(message: String, title: String? = null) {
        showDialog(EnguDialog.newInstance(message, title))
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        if (message.length > 60) showMessage(message)
        else Toast.makeText(provideContext(), message, duration).show()
    }
    fun showDialog(dialog: DialogFragment) {
        if (!dialog.isAdded) {
            dialog.show(provideFragmentManager(), null)
        }
    }
    fun showLoader() {
        provideLoaderViewModel().show()
    }

    fun dismissLoader() {
        provideLoaderViewModel().dismiss()
    }

    fun showFetchErrorDialog(
        retry: (() -> Unit),
        @StringRes messageResId: Int,
    ) {
        showFetchErrorDialog(retry, provideContext().getString(messageResId))
    }

    fun showFetchErrorDialog(
        retry: (() -> Unit),
        message: String,
    ) {
        showAlertDialog(
            message = message,
            positiveTextId = R.string.retry,
            onPositiveClick = retry,
            negativeTextId = R.string.close,
            onNegativeClick = {
                provideActivity().finish()
            },
            neutralTextId = R.string.logout,
            onNeutralClick = {
                SharedPref.logout()
                provideActivity().finish()
                val intent = Intent(provideContext(), SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                provideActivity().startActivity(intent)
            },
        )
    }

    fun showAlertDialog(
        message: String,
        title: String? = null,
        positiveTextId: Int,
        negativeTextId: Int? = null,
        neutralTextId: Int? = null,
        onPositiveClick: (() -> Unit),
        onNegativeClick: (() -> Unit)? = null,
        onNeutralClick: (() -> Unit)? = null,
        isCancellable: Boolean = false,

        ) {
        val builder = AlertDialog.Builder(provideContext()).setMessage(message)
        title?.let { builder.setTitle(it) }
        builder.setPositiveButton(positiveTextId) { dialogInterface, _ ->
            dialogInterface.dismiss()
            onPositiveClick()
        }
        if (neutralTextId != null && onNeutralClick != null) {
            builder.setNeutralButton(neutralTextId) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onNeutralClick()
            }
        }
        if (negativeTextId != null && onNegativeClick != null) {
            builder.setNegativeButton(negativeTextId) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onNegativeClick()
            }
        }
        builder.setCancelable(isCancellable)
        builder.show()
    }

    fun showListPopUp(anchor: View, items: List<String>, onItemClick: (Int, String) -> Unit) {
        val popupView = LayoutInflater.from(provideContext()).inflate(
            R.layout.popup_list_layout, null
        )
        val listView: ListView = popupView.findViewById(R.id.listView_1)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        val adapter = PopUpAdapter(provideContext(), items, { position, text ->
            popupWindow.dismiss()
            onItemClick(position, text)
        })
        listView.adapter = adapter
        PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, 0, Gravity.BOTTOM)
    }
}