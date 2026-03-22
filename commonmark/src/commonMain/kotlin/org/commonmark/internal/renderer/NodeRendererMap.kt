package org.commonmark.internal.renderer

import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import kotlin.reflect.KClass

internal class NodeRendererMap {

    private val nodeRenderers: MutableList<NodeRenderer> = mutableListOf()
    private val renderers: MutableMap<KClass<out Node>, NodeRenderer> = HashMap(32)

    /**
     * Set the renderer for each [NodeRenderer.getNodeTypes], unless there was already a renderer set (first wins).
     */
    fun add(nodeRenderer: NodeRenderer) {
        nodeRenderers.add(nodeRenderer)
        for (nodeType in nodeRenderer.getNodeTypes()) {
            // The first node renderer for a node type "wins".
            renderers.putIfAbsent(nodeType, nodeRenderer)
        }
    }

    fun render(node: Node) {
        val nodeRenderer = renderers[node::class]
        nodeRenderer?.render(node)
    }

    fun beforeRoot(node: Node) {
        nodeRenderers.forEach { it.beforeRoot(node) }
    }

    fun afterRoot(node: Node) {
        nodeRenderers.forEach { it.afterRoot(node) }
    }

    private fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V) {
        if (!this.containsKey(key)) {
            this[key] = value
        }
    }
}
