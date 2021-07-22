package com.bopr.piclock

import android.os.Handler
import android.util.Log
import com.bopr.piclock.util.HandlerTimer
import com.bopr.piclock.util.SECOND_DURATION
import java.util.*

internal class TimeControl(handler: Handler, onTimer: (time: Date, tick: Int) -> Unit) {

    private val timer = HandlerTimer(handler, SECOND_DURATION / TICKS_PER_SECOND) {
        onTimer(getTime(), getNextTick())
    }

    private var tick = 1
    private var fakeTime = -1L

    var fakeTimeMultiplier = 1f
        set(value) {
            field = value
            timer.interval = (SECOND_DURATION / field).toLong() / TICKS_PER_SECOND
            fakeTime = 0L
        }

    var fakeTimeIncrement = SECOND_DURATION
        set(value) {
            field = value
            fakeTime = 0L
        }

    private fun getTime(): Date {
        return if (fakeTime == -1L) {
            Date()
        } else {
            if (tick == 1) fakeTime += fakeTimeIncrement
            Date(fakeTime)
        }
    }

    private fun getNextTick(): Int {
        if (tick < TICKS_PER_SECOND) tick++ else tick = 1
        return tick
    }

    fun pause() {
        timer.enabled = false

        Log.d(TAG, "Paused")
    }

    fun resume() {
        tick = 1
        timer.enabled = true

        Log.d(TAG, "Resumed")
    }

    companion object {

        private const val TAG = "TimeControl"
        private const val TICKS_PER_SECOND = 4
    }
}
