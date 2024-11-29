package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.TopUpRequest
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.WebView.StripeWebViewActivity
import com.example.engu_pension_verification_application.ui.adapter.BankAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils.Companion.isValidNumber
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.example.engu_pension_verification_application.viewmodel.WalletFragmentViewModel
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalletFragment : BaseFragment() {
    companion object {
        const val BANK_ITEM_SELECT_ID = -1
    }

    private lateinit var viewModel: WalletFragmentViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var stripeActivityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVars()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
        showLoader()
        viewModel.fetchBankList()
    }

    private fun initVars() {
        stripeActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val sessionId = result.data?.getStringExtra(AppConstants.SESSION_ID)!!
                    viewModel.fetchPaymentStatus(sessionId)
                    showLoader()
                }
            }
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(WalletFragmentViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        img_wallet_back.setOnClickListener {
            findNavController().navigateUp()
        }
        ll_wallet_back.setOnClickListener {
            findNavController().navigateUp()
        }
        txt_wallethistory.setOnClickListener {
            navigate(R.id.action_wallet_to_wallet_history)
        }
        ll_wallet_topup.setOnClickListener {
            if (validateInputs()) {
                val topUpRequest = TopUpRequest(
                    userId = SharedPref.user_id?.toInt()!!,
                    bankId = sp_wallet_bank.selectedItemPosition,
                    amount = et_top_up_wallet_amount.text.toString().toFloat(),
                    currency = AppConstants.DEFAULT_CURRENCY_CODE,
                )
                viewModel.fetchTopUp(topUpRequest)
                showLoader()
            }
        }
        sp_wallet_bank.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                refreshBankImage(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun observeLiveData() {
        viewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                viewModel.bankItems.clear()
                viewModel.bankItems.add(
                    ListBanksItem(
                        name = " - Select Bank - ",
                        id = BANK_ITEM_SELECT_ID,
                    )
                )
                response.detail.banks?.let { viewModel.bankItems.addAll(it) }
                sp_wallet_bank.adapter = BankAdapter(context, viewModel.bankItems)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBankList()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.topUpApiResult.observe(viewLifecycleOwner) { pair ->
            dismissLoader()
            if (pair != null) {
                val request = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    val intent = Intent(requireActivity(), StripeWebViewActivity::class.java)
                    intent.putExtra(StripeWebViewActivity.EXTRA_URL, response.detail.checkoutUrl)
                    stripeActivityResultLauncher.launch(intent)
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            showLoader()
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchTopUp(request)
                            }
                        }
                    } else {
                        showToast(response.detail?.message ?: getString(R.string.common_error_msg_2))
                    }
                }
                viewModel.resetTopUpApiResult()
            }
        }
        viewModel.paymentResult.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            if (response != null) {
                showToast(response.detail?.message ?: getString(R.string.common_error_msg))
                et_top_up_wallet_amount.setText("")
                viewModel.resetPaymentResult()
            }
        }
    }

    private fun refreshBankImage(position: Int) {
        img_activebank_.setImageResource(R.drawable.ic_bank_green)
        viewModel.bankItems[position]?.let {
            Glide.with(requireContext())
                .load(it.logo)
                .placeholder(R.drawable.ic_bank_green)
                .into(img_activebank_)
        }
    }

    private fun validateInputs(): Boolean {
        var errorResId: Int? = null
        val amount = et_top_up_wallet_amount.text.toString()
        if (!amount.isValidNumber()) {
            errorResId = R.string.invalid_amount_msg
        } else if (sp_wallet_bank.selectedItemPosition !in viewModel.bankItems.indices
            || viewModel.bankItems[sp_wallet_bank.selectedItemPosition]?.id == BANK_ITEM_SELECT_ID
        ) {
            errorResId = R.string.no_bank_selected_msg
        }
        if (errorResId != null) {
            showToast(errorResId)
            return false
        }
        return true
    }

}