package org.commonmark.ext.task.list.items

import org.commonmark.node.CustomNode

/**
 * A marker node indicating that a list item contains a task.
 */
public class TaskListItemMarker(public val isChecked: Boolean) : CustomNode()
