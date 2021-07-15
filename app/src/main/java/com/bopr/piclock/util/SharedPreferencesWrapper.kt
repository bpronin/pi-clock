@file:Suppress("MemberVisibilityCanBePrivate")

package com.bopr.piclock.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

/* Must be subclass of SharedPreferences! Otherwise it leads to unpredictable results */
@Suppress("unused")
open class SharedPreferencesWrapper(private val wrapped: SharedPreferences) :
    SharedPreferences {

    override fun getAll(): Map<String, *> {
        return wrapped.all
    }

    override fun getString(key: String, defValue: String?): String? {
        return wrapped.getString(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return wrapped.getBoolean(key, defValue)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return wrapped.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return wrapped.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return wrapped.getFloat(key, defValue)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        /* should be a copy of values set. see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        return wrapped.getStringSet(key, null)?.toSet() ?: defValue
    }

    override fun contains(key: String): Boolean {
        return wrapped.contains(key)
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(wrapped.edit())
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrapped.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrapped.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private inline fun <reified V> getNullable(key: String, defValue: V?): V? {
        return all.get(key)?.let {
            if (it is V) it
            else throw ClassCastException(
                "Invalid ${V::class.java} getter for setting: $key, expected: ${it::class.java}"
            )
        } ?: defValue
    }

    private inline fun <reified V> getSafe(key: String): V {
        return getNullable<V?>(key, null)
            ?: throw IllegalArgumentException("Setting does not exist: $key")
    }

    fun getStringArray(key: String, defValue: Array<String>?): Array<String>? {
        return wrapped.getString(key, null)?.let {
            commaSplit(it).toTypedArray()
        } ?: defValue
    }

    fun getBoolean(key: String, defValue: Boolean?): Boolean? = getNullable(key, defValue)

    fun getInt(key: String, defValue: Int?): Int? = getNullable(key, defValue)

    fun getLong(key: String, defValue: Long?): Long? = getNullable(key, defValue)

    fun getFloat(key: String, defValue: Float?): Float? = getNullable(key, defValue)

    fun getString(key: String): String = getSafe(key)

    fun getInt(key: String): Int = getSafe(key)

    fun getLong(key: String): Long = getSafe(key)

    fun getFloat(key: String): Float = getSafe(key)

    fun getBoolean(key: String): Boolean = getSafe(key)

    fun getStringSet(key: String): Set<String> = getSafe(key)

    fun getStringArray(key: String): Array<String> = getSafe(key)

    inline fun update(action: EditorWrapper.() -> Unit) {
        edit().apply {
            action(this)
        }.apply()
    }

    fun addListener(listener: OnSharedPreferenceChangeListener) =
        registerOnSharedPreferenceChangeListener(listener)

    fun removeListener(listener: OnSharedPreferenceChangeListener) =
        unregisterOnSharedPreferenceChangeListener(listener)

    inner class EditorWrapper(private val wrappedEditor: SharedPreferences.Editor) :
        SharedPreferences.Editor {

        override fun putString(key: String, value: String?): EditorWrapper {
            wrappedEditor.putString(key, value)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): EditorWrapper {
            wrappedEditor.putStringSet(key, values)
            return this
        }

        override fun putInt(key: String, value: Int): EditorWrapper {
            wrappedEditor.putInt(key, value)
            return this
        }

        override fun putLong(key: String, value: Long): EditorWrapper {
            wrappedEditor.putLong(key, value)
            return this
        }

        override fun putFloat(key: String, value: Float): EditorWrapper {
            wrappedEditor.putFloat(key, value)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): EditorWrapper {
            wrappedEditor.putBoolean(key, value)
            return this
        }

        override fun remove(key: String): EditorWrapper {
            wrappedEditor.remove(key)
            return this
        }

        override fun clear(): EditorWrapper {
            wrappedEditor.clear()
            return this
        }

        override fun commit(): Boolean {
            return wrappedEditor.commit()
        }

        override fun apply() {
            wrappedEditor.apply()
        }

        fun putStringAarray(key: String, value: Array<String>?): EditorWrapper {
            putString(key, value?.let { value.commaJoin() })
            return this
        }

        private inline fun <reified V> putNullable(
            key: String, value: V?, put: (String, V) -> Unit
        ): EditorWrapper {
            value?.apply { put(key, this) } ?: apply { remove(key) }
            return this
        }

        fun putBoolean(key: String, value: Boolean?): EditorWrapper =
            putNullable(key, value, ::putBoolean)

        fun putInt(key: String, value: Int?): EditorWrapper =
            putNullable(key, value, ::putInt)

        fun putLong(key: String, value: Long?): EditorWrapper =
            putNullable(key, value, ::putLong)

        fun putFloat(key: String, value: Float?): EditorWrapper =
            putNullable(key, value, ::putFloat)

        private inline fun <reified V : Any> putOptional(
            key: String, value: V?,
            isOldValueValid: (V) -> Boolean,
            put: (String, V?) -> Unit,
            onPut: (V?) -> Unit
        ): EditorWrapper {
            val oldValue = all.get(key)
            if (oldValue == null || oldValue !is V || !isOldValueValid(oldValue)) {
                put(key, value)
                onPut(value)
            }
            return this
        }

        fun putStringOptional(
            key: String, value: String?,
            isOldValueValid: (String) -> Boolean = { true },
            onPut: (String?) -> Unit = {}
        ): EditorWrapper = putOptional(key, value, isOldValueValid, ::putString, onPut)

        fun putStringSetOptional(
            key: String, values: Set<String>?,
            isOldValueValid: (Set<String>) -> Boolean = { true } ,
            onPut: (Set<String>?) -> Unit = {}
        ): EditorWrapper = putOptional(key, values, isOldValueValid, ::putStringSet, onPut)

        fun putStringArrayOptional(
            key: String, values: Array<String>?,
            isOldValueValid: (Array<String>) -> Boolean = { true }  ,
            onPut: (Array<String>?) -> Unit = {}
        ): EditorWrapper = putOptional(key, values, isOldValueValid, ::putStringAarray, onPut)

        fun putBooleanOptional(
            key: String, value: Boolean,
            isOldValueValid: (Boolean) -> Boolean = { true }  ,
            onPut: (Boolean?) -> Unit = {}
        ): EditorWrapper = putOptional(key, value, isOldValueValid, ::putBoolean, onPut)

        fun putIntOptional(
            key: String, value: Int,
            isOldValueValid: (Int) -> Boolean = { true } ,
            onPut: (Int?) -> Unit = {}
        ): EditorWrapper = putOptional(key, value, isOldValueValid, ::putInt, onPut)

        fun putLongOptional(
            key: String, value: Long,
            isOldValueValid: (Long) -> Boolean = { true } ,
            onPut: (Long?) -> Unit = {}
        ): EditorWrapper = putOptional(key, value, isOldValueValid, ::putLong, onPut)

        fun putFloatOptional(
            key: String, value: Float,
            isOldValueValid: (Float) -> Boolean = { true },
            onPut: (Float?) -> Unit = {}
        ): EditorWrapper = putOptional(key, value, isOldValueValid, ::putFloat, onPut)

    }

}