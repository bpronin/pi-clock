package com.bopr.piclock

import android.content.Intent
import android.os.Bundle
import com.bopr.piclock.util.sha512
import com.bopr.piclock.util.ui.BaseActivity

/**
 * Main activity.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity<MainFragment>(MainFragment::class.java) {

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
    //todo: option to automatically switch to vertical layout on rotation
    //todo: менять цвет фона в зависимости о времени
    //todo: значки солнца и луны вместо am/pm
    //todo: анимация вниз-вправо из кнопки настроек. пол экрана под пример
    //todo: при наклоне экрана двигать в сторону наклона ("падать")
    //todo: option to tell time aloud
    //todo: option to animate digits only in active mode
    //todo: option to auto disable animations when unplugged
    //todo: option to show milliseconds (why not?)
    //todo: звук кукушки каждый час
    //todo: звук курантов, бой каждый час
    //todo: ability to hide settings view by swiping down the settings button
    //todo: ability to open settings view by swiping up the screen edge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings(this).validate()

        intent.getStringExtra("target")?.also { target ->
            when (target) {
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

    override fun onBackPressed() {
        if (!fragment.onBackPressed()) super.onBackPressed()
    }
}