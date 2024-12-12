package com.example.engu_pension_verification_application.ui.fragment.base

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.adapter.PopUpAdapter
import com.example.engu_pension_verification_application.ui.dialog.LoaderDialog
import com.example.engu_pension_verification_application.util.BaseUtils
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel

open class BaseFragment : Fragment(), BaseUtils {
    private val loaderViewModel by activityViewModels<LoaderViewModel>()
    override fun provideContext() = requireContext()
    override fun provideActivity() = requireActivity()
    override fun provideFragmentManager() = parentFragmentManager
    override fun provideLoaderViewModel() = loaderViewModel


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


}