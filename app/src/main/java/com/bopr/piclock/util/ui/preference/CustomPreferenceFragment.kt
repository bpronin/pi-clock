package com.bopr.piclock.util.ui.preference

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/**
 * [PreferenceFragmentCompat] with custom preferences support.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class CustomPreferenceFragment : PreferenceFragmentCompat() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CustomDialogPreference) {
            val dialogFragment = (preference as CustomDialogPreference).createDialogFragment()
//            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    protected fun requirePreference(key: CharSequence): Preference {
        return findPreference(key) ?: throw IllegalStateException("Preference $key does not exist")
    }

}