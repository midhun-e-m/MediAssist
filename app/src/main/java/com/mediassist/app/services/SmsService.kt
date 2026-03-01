package com.mediassist.app.services

import android.telephony.SmsManager
import android.util.Log

object SmsService {

    fun sendSms(phoneRaw: String, message: String) {
        try {
            val phone = normalizePhone(phoneRaw)

            // 1. Get the manager (Use getDefault() as a fallback for broader compatibility)
            val smsManager = try {
                SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId())
            } catch (e: Exception) {
                SmsManager.getDefault()
            }

            // 2. Split the message if it's too long
            val parts = smsManager.divideMessage(message)

            // 3. Send as multipart (handles both short and long messages)
            smsManager.sendMultipartTextMessage(
                phone,
                null,
                parts,
                null,
                null
            )

            Log.d("SMS", "SMS sent to $phone with ${parts.size} parts")

        } catch (e: Exception) {
            Log.e("SMS", "SMS failed", e)
        }
    }

    private fun normalizePhone(phone: String): String {
        val clean = phone.replace("\\s".toRegex(), "")
        return if (clean.startsWith("+")) clean else "+91$clean"
    }
}