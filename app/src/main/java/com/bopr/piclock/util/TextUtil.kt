package com.bopr.piclock.util

/**
 * Miscellaneous text constants and utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

private val COMMA_ESCAPED = Regex("(?<!/),")  /* matches commas not preceded by slash symbol */

fun Iterable<*>.commaJoin(): String {
    return joinToString(",") {
        it.toString().replace(",", "/,")
    }
}

fun Array<*>.commaJoin(): String {
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