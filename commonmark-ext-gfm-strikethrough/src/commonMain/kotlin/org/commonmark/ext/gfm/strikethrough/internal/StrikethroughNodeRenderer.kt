package org.commonmark.ext.gfm.strikethrough.internal

import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import kotlin.reflect.KClass

internal abstract class StrikethroughNodeRenderer : NodeRenderer {
    override fun getNodeTypes(): Set<KClass<out Node>> = setOf(Strikethrough::class)
}
