package com.bopr.piclock.ui.preference;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import static java.lang.Long.parseLong;

public class EditLongPreferenceFragment extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_VALUE = "EditLongPreferenceDialogFragment.value";

    private EditText editText;
    private long value;

    public static EditLongPreferenceFragment newInstance(String key) {
        final EditLongPreferenceFragment fragment = new EditLongPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            value = getEditLongPreference().getValue();
        } else {
            value = savedInstanceState.getLong(SAVE_STATE_VALUE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SAVE_STATE_VALUE, value);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(android.R.id.edit);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setText(String.valueOf(value));
    }

    private EditLongPreference getEditLongPreference() {
        return (EditLongPreference) getPreference();
    }

    @Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            long value;
            try {
                value = parseLong(editText.getText().toString());
            } catch (NumberFormatException e) {
                /* invalid input. do nothing */
                return;
            }

            if (getEditLongPreference().callChangeListener(value)) {
                getEditLongPreference().setValue(value);
            }
        }
    }

}
