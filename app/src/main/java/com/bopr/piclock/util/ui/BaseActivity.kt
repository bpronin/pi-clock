package com.bopr.piclock.util.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bopr.piclock.R

/**
 * Base application activity with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseActivity(private val fragmentClass: Class<out Fragment>) : AppCompatActivity() {

/* NOTE: "To keep fragments self-contained, you SHOULD NOT have fragments communicate directly
   with other fragments or with its host activity." (i.e. do not use fragment listeners, references etc. ) */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)
        supportFragmentManager.apply {
            findFragmentByTag("fragment") ?: run {
                beginTransaction()
                    .replace(R.id.content, fragmentClass.newInstance(), "fragment")
                    .commit()
            }
        }
    }

}