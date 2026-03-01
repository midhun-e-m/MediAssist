package com.mediassist.app.services
import android.telephony.SmsManager

object NotificationService {

    fun notifyContacts(numbers: List<String>, message: String) {
        val smsManager = SmsManager.getDefault()
        numbers.forEach {
            smsManager.sendTextMessage(it, null, message, null, null)
        }
    }
}
