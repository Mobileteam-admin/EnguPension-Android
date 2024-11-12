package com.example.engu_pension_verification_application.ui.fragment.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.commons.TabAccessControl
import com.example.engu_pension_verification_application.model.response.AccountTypeItem
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBankFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeBasicDetailsFragment
import com.example.engu_pension_verification_application.ui.fragment.service.retiree.RetireeDocumentsFragment
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshCallBack
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshViewModel
import com.example.engu_pension_verification_application.utils.SharedPref
import com.example.engu_pension_verification_application.utils.ViewPageCallBack
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_active_service.tab_tablayout_activeservice
import kotlinx.android.synthetic.main.fragment_retiree.*


class RetireeServiceFragment() : Fragment(), ViewPageCallBack, RetireeServiceViewCallBack,
    TokenRefreshCallBack,TabAccessControl {


    val prefs = SharedPref
    var BankdetailsList = ArrayList<ListBanksItem?>()
    var AccountTypeList = ArrayList<AccountTypeItem?>()
    lateinit var retireeServicePresenter: RetireeServicePresenter
    private lateinit var tokenRefreshViewModel: TokenRefreshViewModel
    val tabIcons = intArrayOf(
        R.drawable.ic_basicdetails_white, R.drawable.ic_documents_active, R.drawable.ic_bank_white
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // toolbar_retiree.setTitle("Retiree")

        retireeServicePresenter = RetireeServicePresenter(this)
        tokenRefreshViewModel = TokenRefreshViewModel(this)

        setupViewPager(tab_retiree_viewpager)
        tab_tablayout_retiree!!.setupWithViewPager(tab_retiree_viewpager)


        initCall()
        onClicked()
    }

    private fun initCall() {
        if (context?.isConnectedToNetwork()!!) {
            //Loader.showLoader(requireContext())
            retireeServicePresenter.getBankList()
        } else {
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun onClicked() {
        img_back_retiree.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupViewPager(viewpager: ViewPager?) {
        val retiree_adapter = ViewPagerAdapter(childFragmentManager)

        retiree_adapter.addFragment(RetireeBasicDetailsFragment(this,this), "Basic Details")
        retiree_adapter.addFragment(RetireeDocumentsFragment(this,this), "Documents")
        retiree_adapter.addFragment(
            RetireeBankFragment(BankdetailsList, AccountTypeList), "Bank Information"
        )


        viewpager?.adapter = retiree_adapter
        retiree_adapter.notifyDataSetChanged()

        /*viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.d("VIEW PAGER POSITION", "onPageScrolled: " + position)
            }

            override fun onPageSelected(position: Int) {
                retiree_adapter.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })*/


        tab_tablayout_retiree.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }*/

                val position = tab!!.position

                /*if (prefs.isRBasicSubmit == true)
                {
                    enableDisableTabs(tab_tablayout_retiree, true, true, false)
                    if (prefs.isRDocSubmit == true){
                        enableDisableTabs(tab_tablayout_retiree, true, true, true)
                    }
                }else{
                    enableDisableTabs(tab_tablayout_retiree, true, false, false)

                }*/

               /* Log.d("pref", "pref isRBasicSubmit on tab selectd ${prefs.isRBasicSubmit} ")
                when(prefs.isRBasicSubmit){
                    true->{
                        Log.d("pref", "pref isRBasicSubmit in isRBasic pref ${prefs.isRBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_retiree, true, true, false)
                    }
                    false->{
                        enableDisableTabs(tab_tablayout_retiree, true, false, false)
                    }
                }
                when(prefs.isRDocSubmit){
                    true->{
                        Log.d("pref", "pref isRBasicSubmit in isRDoc pref ${prefs.isRBasicSubmit} ")
                        enableDisableTabs(tab_tablayout_retiree, true, true, true)

                    }
                    false->{

                        if (prefs.isRBasicSubmit == false){
                            enableDisableTabs(tab_tablayout_retiree, true, false, false)
                        }else {
                            enableDisableTabs(tab_tablayout_retiree, true, true, false)
                        }
                    }
                }*/


                Log.d("position", "onTabSelected: " + position)
                when (position) {
                    0 -> {
                        tab_tablayout_retiree.getTabAt(0)?.setIcon(R.drawable.ic_basicdetails_white)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    1 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_active)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_inactive)
                    }

                    2 -> {
                        tab_tablayout_retiree.getTabAt(0)
                            ?.setIcon(R.drawable.ic_basic_details_inactive)
                        tab_tablayout_retiree.getTabAt(1)?.setIcon(R.drawable.ic_documents_inactive)
                        tab_tablayout_retiree.getTabAt(2)?.setIcon(R.drawable.ic_bank_white)
                    }
                }
            }


            override fun onTabUnselected(tab: TabLayout.Tab?) {/*val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.typeface = mUnselectedTypeFace
                }*/
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }
    // Function to enable or disable tabs
    /*fun enableDisableTabs(tabLayout: TabLayout, enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
        tabLayout.getTabAt(0)?.view?.isEnabled = enableTab0
        tabLayout.getTabAt(1)?.view?.isEnabled = enableTab1
        tabLayout.getTabAt(2)?.view?.isEnabled = enableTab2
    }*/




    class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getCount(): Int {
            return mFragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

    }

    override fun onViewMoveNext() {
        tab_retiree_viewpager.setCurrentItem(getItem(+1), true)
    }

    private fun getItem(i: Int): Int {
        return tab_retiree_viewpager.currentItem + i
    }

    override fun onBankDetailsSuccess(response: ResponseBankList) {
        Loader.hideLoader()

        BankdetailsList.clear()
        AccountTypeList.clear()

        BankdetailsList.addAll(response.detail?.banks!!)
        AccountTypeList.addAll(response.detail.accountType!!)
    }

    override fun onBankDetailsFailure(response: ResponseBankList) {
        Loader.hideLoader()

        if (response.detail?.status.equals("expired")) {
            Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
            Loader.hideLoader()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()
        } else {
            Loader.hideLoader()
            Toast.makeText(
                context, response.detail?.message, Toast.LENGTH_LONG
            ).show()

        }
    }

    override fun onTokenRefreshSuccess(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
        initCall()
    }

    override fun onTokenRefreshFailure(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(
            context, response.token_detail?.message, Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }



    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun enableDisableTabs(enableTab0: Boolean, enableTab1: Boolean, enableTab2: Boolean) {
        tab_tablayout_retiree.getTabAt(0)?.view?.isEnabled = enableTab0
        tab_tablayout_retiree.getTabAt(1)?.view?.isEnabled = enableTab1
        tab_tablayout_retiree.getTabAt(2)?.view?.isEnabled = enableTab2
    }
}