package com.example.engu_pension_verification_application.ui.fragment.base

import android.app.AlertDialog
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.engu_pension_verification_application.R
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
}