package com.bopr.piclock.util

import android.content.Context

/**
 * Convenience class to represent objects owning [Context] reference.
 * Fragments, Activities and so on should not override it's methods
 * because they are shadowed by existing ones.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
interface Contextual {

    fun requireContext(): Context
}
