package com.bopr.piclock

import android.content.Context
import java.io.IOException
import java.util.*

/**
 * Application release info.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ReleaseInfo private constructor(
    val buildNumber: String,
    val buildTime: String,
    val versionName: String
) {

    companion object {

        fun get(context: Context): ReleaseInfo {
            Properties().apply {
                context.assets.open("release.properties").use { stream ->
                    try {
                        load(stream)
                    } catch (x: IOException) {
                        throw Error("Cannot read release properties file", x)
                    }
                }
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

                return ReleaseInfo(
                    getProperty("build_number"),
                    getProperty("build_time"),
                    packageInfo.versionName,
                )
            }
        }
    }
}

