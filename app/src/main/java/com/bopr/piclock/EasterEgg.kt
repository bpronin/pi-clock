package com.bopr.piclock

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import com.bopr.piclock.util.Contextual
import com.bopr.piclock.util.JustForFun

@JustForFun
internal class EasterEgg(private val context: Context) : Contextual {

    private var clicksCount: Int = 0
    private var messageIndex: Int = 0

    private val messages = arrayListOf(
        "Wrong rhythm! You are definitely not me.",
        "Oops! Try again.",
        "Well I'll tell you... The right rhythm is ta ta-ta ta.",
        "Almost. But not.",
        "OK. I'm just kidding. There is nothing here.",
        "Resetting messages counter...",
        "Wrong rhythm! You are definitely not me.",
        "And you are persistent!",
        "I give up. The secret code is 42. Use it... somewhere."
    )

    override fun requireContext(): Context {
        return context
    }

    fun onClick() {
        if (++clicksCount == 4) {
            clicksCount = 0
            Toast.makeText(requireContext(), messages[messageIndex++], Toast.LENGTH_LONG).show()

            if (messageIndex >= messages.size) {
                messageIndex = 0

                MediaPlayer.create(requireContext(), R.raw.tada).run {
                    setOnCompletionListener { release() }
                    start()
                }
            }
        }
    }

}