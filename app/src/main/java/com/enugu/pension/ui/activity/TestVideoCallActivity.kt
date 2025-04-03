package com.enugu.pension.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enugu.pension.R

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import co.daily.CallClient
import co.daily.CallClientListener
import co.daily.model.OutboundMediaType
import co.daily.model.Participant
import co.daily.model.ParticipantId
import co.daily.settings.InputSettings
import co.daily.view.VideoView

class TestVideoCallActivity : AppCompatActivity() { //todo-remove after completing video call implementation
        private val TAG: String = "TestVideoCallActivity"
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { !it }) {
                checkPermissions()
            } else {
                // Permission is granted, we can initialize the call
                initializeCallClient()
            }
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_test_video_call)

            checkPermissions()
        }

        private fun initializeCallClient() {
            // Create call client
            val call = CallClient(applicationContext)

            // Create map of video views
            val videoViews = mutableMapOf<ParticipantId, VideoView>()
            val layout = findViewById<LinearLayout>(R.id.videoLinearLayout)
            call.addListener(object : CallClientListener {

                // Handle a remote participant joining
                override fun onParticipantJoined(participant: Participant) {
                    val participantView = layoutInflater.inflate(R.layout.participant_view, layout, false)

                    val videoView = participantView.findViewById<VideoView>(R.id.participant_video)
                    videoView.track = participant.media?.camera?.track
                    videoViews[participant.id] = videoView

                    layout.addView(participantView)
                }

                // Handle a participant updating (e.g. their tracks changing)
                override fun onParticipantUpdated(participant: Participant) {
                    val videoView = videoViews[participant.id]
                    videoView?.track = participant.media?.camera?.track
                }
            })

            call.join(url = "https://videokycpension.daily.co/kyc-video-room") {
                it.error?.apply {
                    Log.e(TAG, "Got error while joining call: $msg")
                }
                it.success?.apply {
                    Log.i(TAG, "Successfully joined call.")
                }
            }
        }

    private fun checkPermissions() {

//        // Check whether permissions have been granted.
//        // If not, ask for permissions
//        val appContext: Context = applicationContext
//
//        val permissionList: Array<String> = appContext.packageManager.getPackageInfo(
//            appContext.packageName,
//            PackageManager.GET_PERMISSIONS
//        ).requestedPermissions
//
//        val notGrantedPermissions:MutableList<String> = ArrayList()
//        for(permission in permissionList) {
//            if (ContextCompat.checkSelfPermission(appContext, permission)
//                != PackageManager.PERMISSION_GRANTED) {
//                notGrantedPermissions.add(permission)
//            }
//        }

//        if (notGrantedPermissions.isNotEmpty()) {
//            requestPermissionLauncher.launch(notGrantedPermissions.toTypedArray())
//        } else {
            // Permission is granted, we can initialize the call
            initializeCallClient()
//        }

    }

}
