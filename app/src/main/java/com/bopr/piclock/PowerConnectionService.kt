package com.bopr.piclock

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bopr.piclock.Notifications.Companion.SERVICE_NOTIFICATION_ID
import com.bopr.piclock.Settings.Companion.PREF_START_ON_POWER_CONNECTED


class PowerConnectionService : Service() {

    private val _tag = "PowerConnectionService"
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_POWER_CONNECTED) onPowerConnected()
        }
    }

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(SERVICE_NOTIFICATION_ID, Notifications(this).serviceNotification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerReceiver(receiver, IntentFilter(ACTION_POWER_CONNECTED))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun onPowerConnected() {
        if (Settings(this).getBoolean(PREF_START_ON_POWER_CONNECTED)) {
            Log.i(_tag, "Start app by power connect")

            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

}