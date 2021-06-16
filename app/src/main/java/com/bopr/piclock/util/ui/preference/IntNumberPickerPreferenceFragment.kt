package com.bopr.piclock.util.ui.preference

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat

class IntNumberPickerPreferenceFragment : PreferenceDialogFragmentCompat() {

    private val thePreference get() = preference as IntNumberPickerPreference
//    private lateinit var editText: EditText
    private var value = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        value = savedInstanceState?.getInt(SAVE_STATE_VALUE) ?: thePreference.getValue()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_VALUE, value)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
//        editText = view.findViewById(android.R.id.edit)
//        editText.inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL
//        editText.setText(value.toString())
    }

    override fun needInputMethod(): Boolean {
        // We want the input method to show, if possible, when dialog is displayed
        return true
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
//            val value = try {
//                editText.text.toString().toInt()
//            } catch (e: NumberFormatException) {
//                /* invalid input. do nothing */
//                return
//            }
//
//            if (thePreference.callChangeListener(value)) {
//                thePreference.setValue(value)
//            }
        }
    }

    companion object {

        private const val SAVE_STATE_VALUE = "IntNumberPickerPreferenceFragment.value"

        fun newInstance(key: String?): IntNumberPickerPreferenceFragment {
            return IntNumberPickerPreferenceFragment().apply {
                arguments = Bundle(1).apply {
                    putString(ARG_KEY, key)
                }
            }
        }
    }
}