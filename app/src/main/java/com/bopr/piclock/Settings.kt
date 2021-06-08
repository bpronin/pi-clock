package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.SharedPreferencesWrapper

class Settings(context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
) {
    companion object {

        var sharedPreferencesName = "com.bopr.android.smailer_preferences"

        private const val SETTINGS_VERSION = 1

        const val PREF_HOURS_COLOR = "hours-color"
        const val PREF_BACKGROUND_COLOR = "background-color"

        const val VAL_PREF_DEFAULT = "default"
    }

}