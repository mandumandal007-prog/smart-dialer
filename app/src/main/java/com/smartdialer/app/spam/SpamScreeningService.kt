package com.smartdialer.app.spam

import android.telecom.Call
import android.telecom.CallScreeningService
import com.smartdialer.app.data.SpamRepository

class SpamScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        val blocked = SpamRepository.isBlocked(this, number)
        val response = CallResponse.Builder()
            .setDisallowCall(blocked)
            .setRejectCall(blocked)
            .setSkipCallLog(false)
            .setSkipNotification(blocked)
            .build()
        respondToCall(callDetails, response)
    }
}
