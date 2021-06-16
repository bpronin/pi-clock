package com.bopr.piclock.util.ui.preference

import androidx.fragment.app.DialogFragment

internal interface CustomDialogPreference {

    fun createDialogFragment(): DialogFragment
}