package com.bopr.piclock.util.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.piclock.R

/**
 * Base application activity with default behaviour and action bar.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseToolbarActivity<F : Fragment>(
    onGetFragment: () -> F
) : BaseActivity<F>(
    onGetFragment,
    R.layout.activity_default_toolbar
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}