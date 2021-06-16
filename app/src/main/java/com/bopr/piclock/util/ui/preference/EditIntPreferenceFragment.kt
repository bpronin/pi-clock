package com.bopr.piclock.util.ui.preference

import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.view.View
import android.widget.EditText
import androidx.preference.PreferenceDialogFragmentCompat

class EditIntPreferenceFragment : PreferenceDialogFragmentCompat() {

    private val editIntPreference get() = preference as EditIntPreference
    private lateinit var editText: EditText
    private var value = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        value = savedInstanceState?.getInt(SAVE_STATE_VALUE) ?: editIntPreference.getValue()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_VALUE, value)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        editText = view.findViewById(android.R.id.edit)
        editText.inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL
        editText.setText(value.toString())
    }

    override fun needInputMethod(): Boolean {
        // We want the input method to show, if possible, when dialog is displayed
        return true
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value = try {
                editText.text.toString().toInt()
            } catch (e: NumberFormatException) {
                /* invalid input. do nothing */
                return
            }

            if (editIntPreference.callChangeListener(value)) {
                editIntPreference.setValue(value)
            }
        }
    }

    companion object {

        private const val SAVE_STATE_VALUE = "EditIntPreferenceFragment.value"

        fun newInstance(key: String?): EditIntPreferenceFragment {
            return EditIntPreferenceFragment().apply {
                arguments = Bundle(1).apply {
                    putString(ARG_KEY, key)
                }
            }
        }
    }
}