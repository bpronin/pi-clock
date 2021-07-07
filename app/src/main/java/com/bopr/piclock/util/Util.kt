package com.bopr.piclock.util

fun toPercents(decimal: Float) = (decimal * 100).toInt()

fun toDecimal(percents: Int) = percents / 100f
