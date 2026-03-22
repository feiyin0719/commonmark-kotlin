package org.commonmark.text

internal actual fun Int.charCategory(): CharCategory {
    if (this in 0..0xFFFF) {
        return this.toChar().category
    }
    // For supplementary code points, default to UNASSIGNED on WasmJS
    return CharCategory.UNASSIGNED
}
