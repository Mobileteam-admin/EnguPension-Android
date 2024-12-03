package com.example.engu_pension_verification_application.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.DialogAddBankBinding
import com.example.engu_pension_verification_application.model.response.AccountTypeItem
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.adapter.AccountTypeAdapter
import com.example.engu_pension_verification_application.ui.adapter.BankAdapter
import com.example.engu_pension_verification_application.viewmodel.AddBankViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddBankDialog : BaseDialog() {
    companion object {
        private const val BANK_ITEM_SELECT_ID = -1
        private const val ACC_TYPE_ITEM_SELECT_ID = -1
    }
    private lateinit var binding:DialogAddBankBinding
    private lateinit var viewModel: AddBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        observeLiveData()
        showLoader()
        viewModel.fetchBankList()
    }

    private fun observeLiveData() {
        viewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                setAdapter(response.detail.banks, response.detail.accountType)
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
                    dismiss()
                }
            }
        }
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(AddBankViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.llAddbankClose.setOnClickListener { dismiss() }
        binding.llAddbankSubmit.setOnClickListener {
        }
        binding.spBank.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshBankImage(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.etSwiftCode.setOnFocusChangeListener { view, hasFocus ->  }


    }

    private fun setAdapter(
        banks: ArrayList<ListBanksItem?>?,
        accountTypes: ArrayList<AccountTypeItem?>?
    ) {
        viewModel.bankItems.clear()
        viewModel.accountTypeItems.clear()
        if (!banks.isNullOrEmpty()) {
            viewModel.bankItems.add(
                ListBanksItem(
                    "",
                    " - Select Bank - ",
                    "0",
                    "",
                    BANK_ITEM_SELECT_ID,
                    ""
                )
            )
            banks.forEach {
                viewModel.bankItems.add(
                    ListBanksItem(
                        it?.code, it?.name, it?.ussd, it?.logo, it?.id, it?.slug
                    )
                )
            }
        }
        if (!accountTypes.isNullOrEmpty()) {
            viewModel.accountTypeItems.add(
                AccountTypeItem(
                    ACC_TYPE_ITEM_SELECT_ID,
                    " - Select Account Type - "
                )
            )
            accountTypes.forEach {
                viewModel.accountTypeItems.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        binding.spBank.adapter = BankAdapter(context, viewModel.bankItems)
        binding.spAccountType.adapter = AccountTypeAdapter(context, viewModel.accountTypeItems)
    }

    private fun refreshBankImage(position:Int) {
        binding.imgBank.setImageResource(R.drawable.ic_bank_green)
        viewModel.bankItems[position]?.let {
            if (it.id != BANK_ITEM_SELECT_ID) {
                Glide.with(requireContext())
                    .load(it.logo)
                    .placeholder(R.drawable.ic_bank_green)
                    .into(binding.imgBank)
            }
        }
    }
}