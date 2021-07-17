package com.bopr.piclock.util

/**
 * An object that have to be destroyed along with its context.
 */
interface Destroyable {

    fun destroy()
}