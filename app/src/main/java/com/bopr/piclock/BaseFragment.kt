package com.bopr.piclock

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Base application fragment with default behaviour.
 */
open class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_about) {
//            AboutDialogFragment().show(this)
//        }
//        return super.onOptionsItemSelected(item)
//    }
}