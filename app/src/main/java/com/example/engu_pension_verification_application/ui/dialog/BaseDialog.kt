package com.example.engu_pension_verification_application.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.adapter.PopUpAdapter
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel


open class BaseDialog : DialogFragment() {
    private val loaderViewModel by activityViewModels<LoaderViewModel>()
    fun showLoader() {
        loaderViewModel.show()
    }

    fun dismissLoader() {
        loaderViewModel.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnShowListener {
            val window = dialog.window ?: return@setOnShowListener
            val background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.dimen_16)).build()
            ).apply {
                fillColor = context?.getColorStateList(R.color.white)
            }
            window.setBackgroundDrawable(background)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    fun showListPopUp(anchor: View, items: List<String>, onItemClick: (Int, String) -> Unit) {
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
        val adapter = PopUpAdapter(requireContext(), items, { position, text ->
            popupWindow.dismiss()
            onItemClick(position, text)
        })
        listView.adapter = adapter
        PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, -40, Gravity.BOTTOM)
    }
}