package com.bopr.piclock.util.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.piclock.R
import kotlin.reflect.KClass

/**
 * Base application activity with default behaviour and action bar.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseToolbarActivity<F : Fragment>(
    fragmentClass: KClass<out F>
) : BaseActivity<F>(
    fragmentClass,
    R.layout.activity_default_toolbar
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}