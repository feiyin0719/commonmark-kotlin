package org.commonmark.ext.front.matter

import org.commonmark.node.CustomNode

public class YamlFrontMatterNode(
    public var key: String,
    public var values: List<String>,
) : CustomNode()
