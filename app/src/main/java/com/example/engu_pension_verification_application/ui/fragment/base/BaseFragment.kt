package com.example.engu_pension_verification_application.ui.fragment.base

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.adapter.PopUpAdapter
import com.example.engu_pension_verification_application.ui.dialog.LoaderDialog
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel

open class BaseFragment : Fragment() {
    private val loaderViewModel by activityViewModels<LoaderViewModel>()
    fun showLoader() {
        loaderViewModel.show()
    }

    fun dismissLoader() {
        loaderViewModel.dismiss()
    }

    fun navigate(
        @IdRes resId: Int,
        args: Bundle? = null,
        allowAnimation: Boolean = true,
        @IdRes popUpTo: Int? = null,
        isReverseAnim: Boolean = false,
        popUpToInclusive: Boolean = true,
    ) {
        val navOptionsBuilder = NavOptions.Builder()
        popUpTo?.let { navOptionsBuilder.setPopUpTo(popUpTo, popUpToInclusive) }
        if (allowAnimation) {
            if (isReverseAnim) navOptionsBuilder.setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right).setPopEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_left)
            else navOptionsBuilder.setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
        }
        findNavController().navigate(resId, args, navOptionsBuilder.build())
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

        ) {
        val builder = AlertDialog.Builder(requireContext()).setMessage(message)
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
        builder.show()
    }

    fun showDialog(dialog: DialogFragment) {
        if (!dialog.isAdded) {
            dialog.show(parentFragmentManager, null)
        }
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_LONG) {
        showToast(getString(messageResId), duration)
    }

    fun showListPopUp(anchor: View, items: List<String>, onItemClick: (Int,String) -> Unit) {
        val popupView = LayoutInflater.from(requireContext()).inflate(
            R.layout.popup_list_layout, null
        )
        val listView: ListView = popupView.findViewById(R.id.listView_1)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        val adapter = PopUpAdapter(requireContext(), items, { position,text ->
            popupWindow.dismiss()
            onItemClick(position,text)
        })
        listView.adapter = adapter
        PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, 0, Gravity.BOTTOM)
    }
}