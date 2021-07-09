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