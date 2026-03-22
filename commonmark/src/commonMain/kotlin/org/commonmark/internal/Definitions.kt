package org.commonmark.internal

import org.commonmark.node.DefinitionMap
import kotlin.reflect.KClass

internal class Definitions {
    private val definitionsByType = mutableMapOf<KClass<*>, DefinitionMap<*>>()

    fun <D : Any> addDefinitions(definitionMap: DefinitionMap<D>) {
        val existingMap = getMap(definitionMap.type)
        if (existingMap == null) {
            definitionsByType[definitionMap.type] = definitionMap
        } else {
            existingMap.addAll(definitionMap)
        }
    }

    fun <V : Any> getDefinition(
        type: KClass<V>,
        label: String,
    ): V? {
        val definitionMap = getMap(type) ?: return null
        return definitionMap[label]
    }

    @Suppress("UNCHECKED_CAST")
    private fun <V : Any> getMap(type: KClass<V>): DefinitionMap<V>? = definitionsByType[type] as? DefinitionMap<V>
}
