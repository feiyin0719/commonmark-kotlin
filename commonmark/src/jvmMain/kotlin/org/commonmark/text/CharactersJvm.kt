package org.commonmark.text

internal actual fun Int.charCategory(): CharCategory {
    val type = Character.getType(this)
    return CharCategory.entries.firstOrNull { it.value == type } ?: CharCategory.UNASSIGNED
}
