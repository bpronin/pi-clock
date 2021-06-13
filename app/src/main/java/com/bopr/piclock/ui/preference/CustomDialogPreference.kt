package com.bopr.piclock.ui.preference

import androidx.fragment.app.DialogFragment

internal interface CustomDialogPreference {

    fun createDialogFragment(): DialogFragment
}