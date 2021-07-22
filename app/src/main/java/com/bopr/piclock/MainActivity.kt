package com.bopr.piclock

import android.content.Intent
import android.os.Bundle
import com.bopr.piclock.util.sha512
import com.bopr.piclock.util.ui.BaseActivity


/**
 * Main application activity.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity<MainFragment>(::MainFragment) {
    //todo: если "схватить" движущиеся часы пальцем они начинают "вырываться"
    //todo: joke: settings "show nanoseconds"
    //todo: joke: release notes: seconds view moved 1px right and 2px up
    //todo: option to save different layouts for landscape and portrait orientation
    //todo: option custom time separator symbol
    //todo: randomly animate date, hours and minutes (with same values)
    //todo: когда подходит ремя будильника звонить меняеся цветт текста или фона

    private val settings by lazy { Settings(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDebugIntent()
        settings.validate()
    }

    override fun onBackPressed() {
        if (!fragment.onBackPressed()) super.onBackPressed()
    }

    private fun handleDebugIntent() {
        intent.getStringExtra("pwd")?.also {
            if (sha512(it) != getString(R.string.developer_sha)) return
        } ?: return

        intent.getStringExtra("target")?.also {
            when (it) {
                "timer" -> {
                    fragment.setTimeParams(
                        intent.getFloatExtra("multiplier", -1f),
                        intent.getLongExtra("increment", -1)
                    )
                }
                "clear-settings" -> {
                    settings.update { clear() }
                }
                "browse-sound" -> {
                    startActivity(Intent(this, BrowseSoundActivity::class.java))
                }
                "debug" -> {
                    startActivity(Intent(this, DebugActivity::class.java))
                }
                else ->
                    throw IllegalArgumentException("Invalid target: $it")
            }
        }
    }
}