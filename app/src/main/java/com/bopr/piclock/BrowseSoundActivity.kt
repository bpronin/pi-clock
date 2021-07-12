package com.bopr.piclock

import android.os.Bundle
import com.bopr.piclock.util.ui.BaseActivity

/**
 * [BrowseSoundFragment]'s activity.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BrowseSoundActivity : BaseActivity<BrowseSoundFragment>(
    BrowseSoundFragment::class.java,
    R.layout.activity_default_toolbar
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}