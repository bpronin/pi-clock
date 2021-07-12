package com.bopr.piclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_POWER_DISCONNECTED
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.bopr.piclock.Settings.Companion.PREF_STOP_ON_POWER_DISCONNECTED
import com.bopr.piclock.util.sha512
import com.bopr.piclock.util.ui.BaseActivity


/**
 * Main application activity.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity<MainFragment>(MainFragment::class.java) {

    private val _tag = "MainActivity"
    private val settings by lazy { Settings(this) }

    private val powerDisconnectReceiver by lazy {
        object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_POWER_DISCONNECTED
                    && settings.getBoolean(PREF_STOP_ON_POWER_DISCONNECTED)
                ) {
                    Log.i(_tag, "Stop app by power disconnect")

                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings.validate()

        registerReceiver(powerDisconnectReceiver, IntentFilter(ACTION_POWER_DISCONNECTED))
        startPowerConnectService()
        handleDebugIntent()
    }

    override fun onDestroy() {
        unregisterReceiver(powerDisconnectReceiver)

        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!fragment.onBackPressed()) super.onBackPressed()
    }

    private fun startPowerConnectService() {
        val intent = Intent(this, PowerConnectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun handleDebugIntent() {
        intent.getStringExtra("target")?.also { target ->
            when (target) {
                "browse-sound" -> {
                    startActivity(Intent(this, BrowseSoundActivity::class.java))
                }
                "debug" -> {
                    intent.getStringExtra("pwd")?.also { password ->
                        if (getString(R.string.developer_sha) == sha512(password)) {
                            startActivity(Intent(this, DebugActivity::class.java))
                        }
                    }
                }
                else ->
                    throw IllegalArgumentException("Invalid target: $target")
            }
        }
    }
}