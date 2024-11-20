package com.example.engu_pension_verification_application.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.viewmodel.AddBankViewModel
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.card_add_bank.ll_addbank_close
import kotlinx.android.synthetic.main.card_add_bank.ll_addbank_submit
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_cancel
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_confirm


class AddBankDialog : BaseDialog() {
    private lateinit var viewModel: AddBankViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.card_add_bank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(AddBankViewModel::class.java)
    }
    private fun initViews() {
        ll_addbank_close.setOnClickListener { dismiss() }
        ll_addbank_submit.setOnClickListener {
        }
    }
}