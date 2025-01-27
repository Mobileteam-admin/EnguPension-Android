package com.enugu.pension.ui.fragment.signup.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enugu.pension.R
import com.enugu.pension.ui.fragment.base.BaseFragment


class ForgotOtpVerifyFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_otp_verify, container, false)
    }


}