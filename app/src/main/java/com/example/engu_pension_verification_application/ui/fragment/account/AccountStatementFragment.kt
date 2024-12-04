package com.example.engu_pension_verification_application.ui.fragment.account

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentAccountStatementBinding
import com.example.engu_pension_verification_application.ui.adapter.AccountStatementAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment

class AccountStatementFragment : BaseFragment() {
    private lateinit var binding:FragmentAccountStatementBinding
    private lateinit var accountStatement_lm: LinearLayoutManager
    lateinit var accountStatementAdapter: AccountStatementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
        val resolution = getScreenResolution(requireContext())
        Log.d("ScreenResolution", "onViewCreated: "+resolution )
        onAdapterset()


    }

    private fun onClicked() {
        binding.llDownload.setOnClickListener {
            val download= context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val PdfUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/online-learning-ea8c0.appspot.com/o/Uploads%2FSystem%20Design%20Basics%20Handbook%20-8.pdf?alt=media&token=084ec8ce-598a-4a14-ad7c-61003e509af6")
            val getPdf = DownloadManager.Request(PdfUri)
            getPdf.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            download.enqueue(getPdf)
            Toast.makeText(context,"Sample Download Started", Toast.LENGTH_LONG).show()
        }

        binding.imgAccountstatementBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onAdapterset() {
        accountStatementAdapter = AccountStatementAdapter() {}
        accountStatement_lm = LinearLayoutManager(requireContext())
        binding.rvAccountstatement.layoutManager = accountStatement_lm
        binding.rvAccountstatement.adapter = accountStatementAdapter
    }
    private fun getScreenResolution(context: Context): String? {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return "{$width,$height}"
    }
}