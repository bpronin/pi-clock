package com.bopr.piclock.util.property

import android.media.MediaPlayer
import android.util.Property
import android.view.View

/**
 * Custom properties used in animators.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

val PROP_SCALE by lazy {

    object : Property<View, Float>(Float::class.java, "scale") {

        override fun get(view: View?): Float? {
            return view?.scaleX
        }

        override fun set(view: View?, value: Float) {
            view?.apply {
                scaleX = value
                scaleY = scaleX
            }
        }
    }
}

val PROP_RELATIVE_TRANSITION_X by lazy {

    object : Property<View, Float>(Float::class.java, "relativeTranslationX") {

        override fun get(view: View?): Float? {
            return view?.run { translationX / width } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationX = (value ?: 0f) * width }
        }
    }
}

val PROP_RELATIVE_TRANSITION_Y by lazy {

    object : Property<View, Float>(Float::class.java, "relativeTranslationY") {

        override fun get(view: View?): Float? {
            return view?.run { translationY / height } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationY = (value ?: 0f) * height }
        }
    }
}

val PROP_X_CURRENT_TO_END by lazy {

    object : Property<View, Float>(Float::class.java, "xCurrentToEnd") {

        override fun get(view: View): Float {
            return view.x
        }

        override fun set(view: View, value: Float) {
            view.x = value
        }
    }
}

val PROP_Y_CURRENT_TO_END by lazy {

    object : Property<View, Float>(Float::class.java, "yCurrentToEnd") {

        override fun get(view: View): Float {
            return view.y
        }

        override fun set(view: View, value: Float) {
            view.y = value
        }
    }
}

val PROP_ALPHA_CURRENT_TO_ZERO by lazy {

    object : Property<View, Float>(Float::class.java, "alphaCurrentToZero") {

        override fun get(view: View): Float {
            return view.alpha
        }

        override fun set(view: View, value: Float) {
            view.alpha = value
        }
    }
}

val PROP_ALPHA_ZERO_TO_CURRENT by lazy {

    object : Property<View, Float>(Float::class.java, "alphaZeroToCurrent") {

        override fun get(view: View): Float {
            return view.alpha
        }

        override fun set(view: View, value: Float) {
            view.alpha = value
        }
    }
}

val PROP_VOLUME by lazy {

    object : Property<MediaPlayer, Float>(Float::class.java, "volume") {

        override fun get(player: MediaPlayer): Float {
            throw UnsupportedOperationException()
        }

        override fun set(player: MediaPlayer, value: Float) {
            player.setVolume(value, value)
        }
    }
}