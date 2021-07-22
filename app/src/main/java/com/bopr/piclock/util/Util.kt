package com.bopr.piclock.util

import java.security.MessageDigest

/**
 * Miscellaneous constants and utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

const val SECOND_DURATION = 1000L
const val MINUTE_DURATION = 60 * SECOND_DURATION
const val HOUR_DURATION = 60 * MINUTE_DURATION

val Int.isOdd get() = !isEven

val Int.isEven get() = this % 2 == 0

val Long.isOdd get() = !isEven

val Long.isEven get() = this % 2 == 0L

fun toPercents(decimal: Float) = (decimal * 100).toInt()

fun toDecimal(percents: Int) = percents / 100f

fun sha512(s: String): String {
    val digest = MessageDigest.getInstance("SHA-512").digest(s.toByteArray())
    return StringBuilder().apply {
        for (i in digest.indices) {
            append(((digest[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
    }.toString()
}
