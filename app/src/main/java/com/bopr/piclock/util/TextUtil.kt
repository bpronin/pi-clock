package com.bopr.piclock.util

private val COMMA_ESCAPED = Regex("(?<!/),")  /* matches commas not preceded by slash symbol */

fun commaJoin(values: Collection<*>): String {
    return values.joinToString(",") {
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