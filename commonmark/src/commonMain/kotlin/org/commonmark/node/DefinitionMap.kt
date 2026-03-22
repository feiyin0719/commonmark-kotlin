package org.commonmark.node

import kotlin.reflect.KClass

public class DefinitionMap<D : Any>(
    public val type: KClass<D>,
) {
    private val definitions = linkedMapOf<String, D>()

    public fun addAll(that: DefinitionMap<D>) {
        for ((key, value) in that.definitions) {
            definitions.putIfAbsent(key, value)
        }
    }

    public fun putIfAbsent(
        label: String,
        definition: D,
    ): D? {
        val normalizedLabel = normalizeLabel(label)
        return definitions.putIfAbsent(normalizedLabel, definition)
    }

    public operator fun get(label: String): D? {
        val normalizedLabel = normalizeLabel(label)
        return definitions[normalizedLabel]
    }

    public fun keySet(): Set<String> = definitions.keys

    public fun values(): Collection<D> = definitions.values

    private companion object {
        private val WHITESPACE = Regex("[ \\t\\r\\n]+")

        fun normalizeLabel(label: String): String {
            val trimmed = label.trim()
            val caseFolded = trimmed.lowercase().uppercase()
            return WHITESPACE.replace(caseFolded, " ")
        }
    }
}

private fun <K, V> MutableMap<K, V>.putIfAbsent(
    key: K,
    value: V,
): V? {
    val existing = this[key]
    if (existing != null) return existing
    this[key] = value
    return null
}
