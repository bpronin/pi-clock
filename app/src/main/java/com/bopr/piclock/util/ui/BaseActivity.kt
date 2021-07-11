package com.bopr.piclock.util.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bopr.piclock.R

/**
 * Base application activity with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseActivity<F : Fragment>(
    fragmentClass: Class<out F>,
    @LayoutRes private val layout: Int = R.layout.activity_default
) :
    AppCompatActivity() {

/* NOTE: "To keep fragments self-contained, you SHOULD NOT have fragments communicate directly
   with other fragments or with its host activity." (i.e. do not use fragment listeners, references etc. ) */

    protected val fragment: F = fragmentClass.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        supportFragmentManager.apply {
            findFragmentById(R.id.fragment) ?: run {
                beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit()
            }
        }
    }

}