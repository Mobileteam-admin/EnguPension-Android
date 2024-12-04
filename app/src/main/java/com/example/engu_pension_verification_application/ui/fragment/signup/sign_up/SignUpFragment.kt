package com.example.engu_pension_verification_application.ui.fragment.signup.sign_up

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
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
import com.example.engu_pension_verification_application.databinding.FragmentSignUpBinding
import com.example.engu_pension_verification_application.model.input.InputSignup
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.SignUpViewModel


class SignUpFragment : BaseFragment() {
    private lateinit var binding:FragmentSignUpBinding
    var Ph_no: String = ""
    private lateinit var signUpViewModel: SignUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        initViewModel()
        observeData()
        binding.ccp.registerPhoneNumberTextView(binding.etSignupPhone)

       /* binding.etSignupUsername.text =  Editable.Factory.getInstance().newEditable("Reema")
        binding.etSignupEmail.text =  Editable.Factory.getInstance().newEditable("r@gmail.com")
        binding.etSignupPhone.text =  Editable.Factory.getInstance().newEditable("9495745781")
        binding.etSignupPassword.text =  Editable.Factory.getInstance().newEditable("Reema@123")
        binding.etSignupConfirmPassword.text =  Editable.Factory.getInstance().newEditable("Reema@123")*/

        ontextWatcher()

        onClicked()
        //observeSignUp()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        signUpViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(SignUpViewModel::class.java)
    }
    private fun observeData() {
        signUpViewModel.signupStatus.observe(viewLifecycleOwner) { response ->
            if (response!=null) {
                dismissLoader()
                Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                val otpExistsMessage = "OTP is still valid. Please use the previously sent OTP."
                if (response.detail?.status == AppConstants.SUCCESS ||
                    response.detail?.message == otpExistsMessage // TODO:
                ) {
                    onSignUpSuccess()
                }
                signUpViewModel.resetSignupStatus()
            }
        }
    }
    private fun ontextWatcher() {
        binding.etSignupPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!AppUtils.isValidPassword(newText.toString())) {
                    binding.txtPasswordpatternError.visibility = View.VISIBLE
                } else {
                    binding.txtPasswordpatternError.visibility = View.GONE
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (!AppUtils.isValidPassword(binding.etSignupPassword.text.toString())) {
                        binding.txtPasswordpatternError.visibility = View.VISIBLE
                        //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
                    } else {
                        binding.txtPasswordpatternError.visibility = View.GONE
                    }


                }
            }
        })


        binding.etSignupConfirmPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(confirm: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!binding.etSignupPassword.text.toString().equals(confirm.toString())) {
                    Log.d("TAG", "onTextChanged: "+binding.etSignupPassword.text.toString()+" , "+confirm)
                    binding.txtSignupConfirmPasswordError.visibility = View.VISIBLE
                    binding.txtSignupConfirmPasswordError.text =
                        getResources().getString(R.string.password_mismatch)
                } else {
                    binding.txtSignupConfirmPasswordError.visibility = View.GONE
                }
            }

            override fun afterTextChanged(confirmm: Editable?) {
                if (binding.etSignupPassword.text.toString().equals(confirmm.toString())) {
                    Log.d("After", "onTextChanged: "+binding.etSignupPassword.text.toString()+" , "+confirmm)
                    binding.txtSignupConfirmPasswordError.visibility = View.GONE

                } else {
                    binding.txtSignupConfirmPasswordError.visibility = View.VISIBLE
                }
            }
        } )

    }

    private fun onClicked() {

        binding.llSignupSignup.setOnClickListener {
            if (isValidSignup()) {
                Ph_no = "+" + binding.ccp.fullNumber

                //final call
                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    signUpViewModel.doSignup(
                        InputSignup(
                            username = binding.etSignupUsername.text.toString(),
                            email = binding.etSignupEmail.text.toString(),
                            phoneNumber = Ph_no,
                            password = binding.etSignupPassword.text.toString(),
                            confirmPassword = binding.etSignupConfirmPassword.text.toString()
                        )
                    )

                } else {
                    dismissLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.llSignupLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun isValidSignup(): Boolean {
        if (TextUtils.isEmpty(binding.etSignupUsername.text)) {
            binding.txtSignupUnameError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupUnameError.visibility = View.GONE
        }

        if (binding.etSignupUsername.text.toString().contains(" ")){

        /*Toast.makeText(
                context,
                "Enter valid username",
                Toast.LENGTH_LONG
            ).show()*/
        }

        if (TextUtils.isEmpty(binding.etSignupEmail.text)) {
            binding.txtSignupEmailError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupEmailError.visibility = View.GONE
        }

        if (!AppUtils.isValidEmailAddress(binding.etSignupEmail.text.toString())) {
            binding.txtSignupEmailError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupEmailError.visibility = View.GONE
        }


        if (TextUtils.isEmpty(binding.etSignupPhone.text)) {
            binding.txtSignupPhoneError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupPhoneError.visibility = View.GONE
        }

        if ((!binding.ccp.isValid)) {
            Log.d("binding.ccp.isValid", "isValidLogin: " + binding.ccp.isValid)
            binding.txtSignupPhoneError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupPhoneError.visibility = View.GONE
        }

        if (TextUtils.isEmpty(binding.etSignupPassword.text)) {
            binding.txtSignupPasswordError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtSignupPasswordError.visibility = View.GONE
        }
        if (!AppUtils.isValidPassword(binding.etSignupPassword.text.toString())) {
            binding.txtPasswordpatternError.visibility = View.VISIBLE
                 //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
            return false
        } else {
            binding.txtPasswordpatternError.visibility = View.GONE
        }



        if (TextUtils.isEmpty(binding.etSignupConfirmPassword.text)) {
            binding.txtSignupConfirmPasswordError.visibility = View.VISIBLE
            binding.txtSignupConfirmPasswordError.text =
                getResources().getString(R.string.enter_valid_password)
            return false
        } else {
            binding.txtSignupConfirmPasswordError.visibility = View.GONE
        }

        if (!binding.etSignupPassword.text.toString().equals(binding.etSignupConfirmPassword.text.toString())) {
            binding.txtSignupConfirmPasswordError.visibility = View.VISIBLE
            binding.txtSignupConfirmPasswordError.text =
                getResources().getString(R.string.password_mismatch)
            return false
        } else {
            binding.txtSignupConfirmPasswordError.visibility = View.GONE
        }



        return true
    }

    private fun onSignUpSuccess() {
        val bundle = Bundle()
        bundle.putSerializable("screen", "Signup")
        bundle.putSerializable("Email", binding.etSignupEmail.text.toString())
        bundle.putSerializable("Phone", Ph_no)
        navigate(R.id.action_signup_to_otp, bundle)
    }


}