package com.bopr.piclock.util.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bopr.piclock.R

/**
 * Base application activity with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseActivity() : AppCompatActivity() {

    private var fragment: Fragment? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(true)
        setContentView(R.layout.activity_default)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        fragment = supportFragmentManager.findFragmentByTag("fragment")
        if (fragment == null) {
            fragment = onCreateFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment!!, "fragment")
                .commit()
        }
    }

    abstract fun onCreateFragment(): Fragment

    protected fun setHomeButtonEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}