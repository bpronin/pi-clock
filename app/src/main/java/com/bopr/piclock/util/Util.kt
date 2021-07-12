package com.bopr.piclock.util

/**
 * Miscellaneous constants and utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun toPercents(decimal: Float) = (decimal * 100).toInt()

fun toDecimal(percents: Int) = percents / 100f
