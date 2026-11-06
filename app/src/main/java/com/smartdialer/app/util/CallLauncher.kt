package com.smartdialer.app.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object CallLauncher {

    fun simAccounts(context: Context): List<PhoneAccountHandle> {
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        if (telecom == null) return emptyList()
        return try {
            telecom.callCapablePhoneAccounts ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun dial(activity: AppCompatActivity, number: String) {
        if (number.isBlank()) return
        val accounts = simAccounts(activity)
        if (accounts.size > 1) {
            val labels = ArrayList<String>()
            for (index in accounts.indices) {
                labels.add("SIM " + (index + 1))
            }
            AlertDialog.Builder(activity)
                .setTitle("Call with")
                .setItems(labels.toTypedArray()) { _, which ->
                    place(activity, number, accounts[which])
                }
                .show()
        } else {
            place(activity, number, null)
        }
    }

    fun place(context: Context, number: String, handle: PhoneAccountHandle?) {
        val uri = Uri.fromParts("tel", number, null)
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            try {
                if (handle != null) {
                    val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    val extras = Bundle()
                    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                    telecom.placeCall(uri, extras)
                } else {
                    val intent = Intent(Intent.ACTION_CALL, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                return
            } catch (e: Exception) {
                // fall back to the system dialer below
            }
        }
        val dialIntent = Intent(Intent.ACTION_DIAL, uri)
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(dialIntent)
    }

    fun whatsapp(context: Context, number: String) {
        val digits = number.filter { it.isDigit() }
        if (digits.isEmpty()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + digits))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // whatsapp not installed
        }
    }

    fun sms(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // no messaging app
        }
    }
}
