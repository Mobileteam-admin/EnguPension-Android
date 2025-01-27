package com.enugu.pension.ui.fragment.base

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.enugu.pension.R
import com.enugu.pension.util.BaseUtils
import com.enugu.pension.viewmodel.LoaderViewModel

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