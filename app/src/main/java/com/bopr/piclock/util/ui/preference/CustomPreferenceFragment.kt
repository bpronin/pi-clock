package com.bopr.piclock.util.ui.preference

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bopr.piclock.util.Contextual

/**
 * [PreferenceFragmentCompat] with custom preferences support.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class CustomPreferenceFragment : PreferenceFragmentCompat(), Contextual {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CustomDialogPreference) {
            val dialogFragment = (preference as CustomDialogPreference).onCreateDialogFragment()

            @Suppress("DEPRECATION")
            dialogFragment.setTargetFragment(this, 0)

            dialogFragment.show(parentFragmentManager, null)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    protected fun <T : Preference> requirePreference(key: CharSequence): T {
        return findPreference(key) ?: throw IllegalStateException("Preference $key does not exist")
    }

}