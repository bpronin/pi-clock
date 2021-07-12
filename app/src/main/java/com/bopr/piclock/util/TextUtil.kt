package com.bopr.piclock.util

import java.security.MessageDigest

/**
 * Miscellaneous text constants and utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

private val COMMA_ESCAPED = Regex("(?<!/),")  /* matches commas not preceded by slash symbol */

fun Collection<*>.commaJoin(): String {
    return joinToString(",") {
        it.toString().replace(",", "/,")
    }
}

fun commaSplit(s: String): List<String> {
    return if (s.isNotEmpty()) {
        s.split(COMMA_ESCAPED).map {
            it.trim().replace("/,", ",")
        }
    } else {
        emptyList() /* important. to match commaJoin("") */
    }
}

fun sha512(s: String): String {
    val digest = MessageDigest.getInstance("SHA-512").digest(s.toByteArray())
    return StringBuilder().apply {
        for (i in digest.indices) {
            append(((digest[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
    }.toString()
}
