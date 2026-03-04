package com.mediassist.app.services

import android.content.Context
import android.content.Intent
import com.mediassist.app.ui.user.RequestAmbulanceActivity

class EmergencyTriggerService {

    companion object {

        fun triggerEmergency(context: Context) {

            val intent = Intent(context, RequestAmbulanceActivity::class.java)

            // This flag tells the activity to auto-send ambulance request
            intent.putExtra("AUTO_TRIGGER", true)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)

        }

    }
}