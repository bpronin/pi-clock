package com.bopr.piclock

import android.content.Intent
import android.os.Bundle
import com.bopr.piclock.util.sha512
import com.bopr.piclock.util.ui.BaseActivity

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity(MainFragment::class.java) {

    //todo: separate date view into 'date' and 'day name'
    //todo: option to set floating speed
    //todo: option to select floating trajectory
    //todo: option to make custom floating trajectory
    //todo: float animation duration should depend on distance
    //todo: если потрясти часы начинают болтаться внутри отскакивая от стенок и тикать
    //todo: brightness controlled by external light
    //todo: start on power plug in
    //todo: fast changing battery indicator
    //todo: sound and vibration feedback when scaling and changing brightness
    //todo: buttons for fast timers (5, 10 , 15.. min) on main screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings(this).validate()

        intent.getStringExtra("target")?.also { target ->
            when (target) {
                "settings" ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                "debug" -> {
                    intent.getStringExtra("pwd")?.also { password ->
                        if (getString(R.string.debug_sha) == sha512(password)) {
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