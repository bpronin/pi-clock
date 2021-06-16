package com.bopr.piclock.util.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference

class IntEditTextPreference : EditTextPreference, CustomDialogPreference {

    private var value = 0

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    @Suppress("unused")
    constructor(context: Context?) : super(context)

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setValue(getPersistedInt(value))
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        val myState = SavedState(superState)
        myState.value = getValue()
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        setValue(myState.value)
    }

    override fun onCreateDialogFragment(): DialogFragment {
        return IntEditTextPreferenceFragment.newInstance(key)
    }

    fun setValue(value: Int) {
        val wasBlocking = shouldDisableDependents()
        this.value = value
        persistInt(this.value)
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking)
        }
    }

    fun getValue(): Int {
        return value
    }

    private class SavedState : BaseSavedState {

        var value = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            value = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(value)
        }

        companion object {

            @JvmField
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {

                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}