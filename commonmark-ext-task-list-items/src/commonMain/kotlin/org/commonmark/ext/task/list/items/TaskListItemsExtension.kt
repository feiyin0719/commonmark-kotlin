package org.commonmark.ext.task.list.items

import org.commonmark.Extension
import org.commonmark.ext.task.list.items.internal.TaskListItemHtmlNodeRenderer
import org.commonmark.ext.task.list.items.internal.TaskListItemPostProcessor
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * Extension for adding task list items.
 *
 * Create it with [create] and then configure it on the builders
 * ([Parser.Builder.extensions], [HtmlRenderer.Builder.extensions]).
 *
 * @since 0.15.0
 */
public class TaskListItemsExtension private constructor() :
    Parser.ParserExtension,
    HtmlRenderer.HtmlRendererExtension {
        public companion object {
            public fun create(): Extension = TaskListItemsExtension()
        }

        override fun extend(parserBuilder: Parser.Builder) {
            parserBuilder.postProcessor(TaskListItemPostProcessor())
        }

        override fun extend(rendererBuilder: HtmlRenderer.Builder) {
            rendererBuilder.nodeRendererFactory { context -> TaskListItemHtmlNodeRenderer(context) }
        }
    }
