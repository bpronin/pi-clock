package com.bopr.piclock.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import kotlin.reflect.KClass

/* Must be subclass of SharedPreferences! Otherwise it leads to unpredictable results */
@Suppress("unused")
open class SharedPreferencesWrapper(private val wrappedPreferences: SharedPreferences) :
    SharedPreferences {

    override fun getAll(): MutableMap<String, *> {
        return wrappedPreferences.all
    }

    override fun getString(key: String, defValue: String?): String? {
        return wrappedPreferences.getString(key, defValue)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return wrappedPreferences.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return wrappedPreferences.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return wrappedPreferences.getFloat(key, defValue)
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        /* should be a copy of values set. see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        return wrappedPreferences.getStringSet(key, null)?.toMutableSet() ?: defValues
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return wrappedPreferences.getBoolean(key, defValue)
    }

    fun getString(key: String): String {
        checkKeyExists(key)
        return getString(key, "")!!
    }

    fun getInt(key: String): Int {
        checkKeyExists(key)
        return getInt(key, 0)
    }

    fun getLong(key: String): Long {
        checkKeyExists(key)
        return getLong(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        checkKeyExists(key)
        return getBoolean(key, false)
    }

    fun getStringSet(key: String): MutableSet<String> {
        checkKeyExists(key)
        return getStringSet(key, mutableSetOf())!!
    }

    fun getStringList(key: String): MutableList<String> {
        checkKeyExists(key)
        return commaSplit(getString(key)).toMutableList()
    }

    override fun contains(key: String): Boolean {
        return wrappedPreferences.contains(key)
    }

    fun contains(key: String, valueClass: KClass<*>): Boolean {
        val value = wrappedPreferences.all.get(key)
        return value != null && valueClass.isInstance(value)
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(wrappedPreferences.edit())
    }

    fun update(action: EditorWrapper.() -> Unit) {
        val editor = edit()
        action(editor)
        editor.apply()
    }

    private fun checkKeyExists(key: String) {
        if (!contains(key)) {
            throw IllegalArgumentException("Setting does not exists: $key")
        }
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    open inner class EditorWrapper(private val wrappedEditor: SharedPreferences.Editor) :
        SharedPreferences.Editor {

        override fun putString(key: String, value: String?): EditorWrapper {
            wrappedEditor.putString(key, value)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): EditorWrapper {
            wrappedEditor.putStringSet(key, values)
            return this
        }

        fun putStringList(key: String, value: Collection<String>?): EditorWrapper {
            putString(key, value?.let { commaJoin(value) })
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

        private fun putOptional(
            key: String,
            valueClass: KClass<*>,
            isExistentValid: () -> Boolean,
            put: () -> Unit
        ): EditorWrapper {
            if (!contains(key, valueClass) || !isExistentValid()) {
                put()
            }
            return this
        }

        fun putStringOptional(
            key: String, value: String?,
            isExistentValid: () -> Boolean = { true }
        ): EditorWrapper {
            return putOptional(key, String::class, isExistentValid) { putString(key, value) }
        }

        fun putStringSetOptional(
            key: String,
            values: Set<String>?,
            isExistentValid: () -> Boolean = { true }
        ): EditorWrapper {
            return putOptional(key, Set::class, isExistentValid) { putStringSet(key, values) }
        }

        fun putBooleanOptional(
            key: String, value: Boolean,
            isExistentValid: () -> Boolean = { true }
        ): EditorWrapper {
            return putOptional(key, Boolean::class, isExistentValid) { putBoolean(key, value) }
        }

        fun putIntOptional(
            key: String, value: Int,
            isExistentValid: () -> Boolean = { true }
        ): EditorWrapper {
            return putOptional(key, Int::class, isExistentValid) { putInt(key, value) }
        }

        fun putLongOptional(
            key: String, value: Long,
            isExistentValid: () -> Boolean = { true }
        ): EditorWrapper {
            return putOptional(key, Long::class, isExistentValid) { putLong(key, value) }
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

    }

}