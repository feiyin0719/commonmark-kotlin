package org.commonmark.ext.htmlconverter.internal

import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import kotlin.reflect.KClass

internal abstract class HtmlConverterNodeRenderer : NodeRenderer {
    override fun getNodeTypes(): Set<KClass<out Node>> =
        setOf(
            HtmlBlock::class,
            HtmlInline::class,
        )
}
