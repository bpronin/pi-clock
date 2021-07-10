package com.bopr.piclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.ACTION_POWER_DISCONNECTED
import com.bopr.piclock.Settings.Companion.PREF_START_ON_POWER_PLUG

class PowerConnectorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.run {
            when (action) {
                ACTION_POWER_CONNECTED -> {
                    if (Settings(context).getBoolean(PREF_START_ON_POWER_PLUG)) {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                }
                ACTION_POWER_DISCONNECTED -> {
                    // Do something when power disconnected
                }
            }
        }
    }
}