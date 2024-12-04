package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentResetPasswordBinding
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.ResetPasswordViewModel


class ResetPasswordFragment : BaseFragment() {
    private lateinit var binding:FragmentResetPasswordBinding
    var token: String = ""
    var OTP: String = ""

    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        //resetPasswordViewModel = ViewModelProvider(this).get(ResetPasswordViewModel::class.java)
        val bundle = this.arguments
        if (bundle != null) {
            token = bundle.getString("Token").toString()
            OTP = bundle.getString("OTP").toString()
        }
        ontextWatcher()

        onClicked()
        //observe_resetpass()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        resetPasswordViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ResetPasswordViewModel::class.java)
    }
    private fun observeData() {
        resetPasswordViewModel.resetPassResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.reset_detail?.message, Toast.LENGTH_LONG).show()
            if (response.reset_detail?.status == AppConstants.SUCCESS) {
                findNavController().popBackStack(R.id.navigation_login, false)
            }
        }
    }

    private fun ontextWatcher() {
        binding.etNewResetpass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!AppUtils.isValidPassword(binding.etNewResetpass.text.toString())) {
                    binding.txtResetNewpassPatternError.visibility = View.VISIBLE
                } else {
                    binding.txtResetNewpassPatternError.visibility = View.GONE
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != null) {
                    if (!p0.toString().isEmpty()) {
                        binding.txtResetnewPasswordError.visibility = View.GONE
                    }

                }
            }
        })

        binding.etConfirmResetpass.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (!binding.etNewResetpass.text.toString().equals(p0.toString())) {
                    binding.txtResetConfirmPasswordError.visibility = View.VISIBLE
                } else {
                    binding.txtResetConfirmPasswordError.visibility = View.GONE
                }
            }
        } )

    }

    private fun onClicked() {
        binding.llResetpassSubmit.setOnClickListener {
            if (isValidReset()) {

                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    resetPasswordViewModel.doReset(
                        com.example.engu_pension_verification_application.model.input.InputResetPassword(
                            password = binding.etNewResetpass.text.toString(),
                            token = token,
                            otp = OTP
                        )
                    )
                }


            }
        }

        binding.llResetpassBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun isValidReset(): Boolean {


        if (TextUtils.isEmpty(binding.etNewResetpass.text)) {
            binding.txtResetnewPasswordError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtResetnewPasswordError.visibility = View.GONE
        }

        if (!AppUtils.isValidPassword(binding.etNewResetpass.text.toString())) {
            binding.txtResetNewpassPatternError.visibility = View.VISIBLE
            //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
            return false
        } else {
            binding.txtResetNewpassPatternError.visibility = View.GONE
        }



        if (TextUtils.isEmpty(binding.etConfirmResetpass.text)) {
            binding.txtResetConfirmPasswordError.visibility = View.VISIBLE
            binding.txtResetConfirmPasswordError.text =
                getResources().getString(R.string.enter_valid_password)
            return false
        } else {
            binding.txtResetConfirmPasswordError.visibility = View.GONE
        }

        if (binding.etNewResetpass.text.toString() != binding.etConfirmResetpass.text.toString()) {
            binding.txtResetConfirmPasswordError.visibility = View.VISIBLE
            binding.txtResetConfirmPasswordError.text =
                getResources().getString(R.string.password_mismatch)
            return false
        } else {
            binding.txtResetConfirmPasswordError.visibility = View.GONE
        }
        return true
    }
}