package com.example.engu_pension_verification_application.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.example.engu_pension_verification_application.databinding.DialogEnguBinding

class EnguDialog private constructor() : BaseDialog() {
    private var title: String? = null
    private var message: String = ""
    private lateinit var binding: DialogEnguBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = arguments?.getString(ARG_MESSAGE) ?: ""
        title = arguments?.getString(ARG_TITLE)
    }

    companion object {
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_TITLE = "arg_title"
        fun newInstance(message: String, title: String? = null): EnguDialog {
            val fragment = EnguDialog()
            fragment.arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
                putString(ARG_TITLE, title)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogEnguBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.tvTitle.isGone = title == null
        title?.let { binding.tvTitle.text = it }
        binding.tvMessage.text = message
        binding.tvOk.setOnClickListener {
            dismiss()
        }
    }
}