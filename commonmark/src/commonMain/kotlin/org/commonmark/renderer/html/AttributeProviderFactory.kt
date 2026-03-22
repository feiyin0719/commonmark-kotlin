package org.commonmark.renderer.html

/**
 * Factory for instantiating new attribute providers when rendering is done.
 */
public fun interface AttributeProviderFactory {
    /**
     * Create a new attribute provider.
     *
     * @param context for this attribute provider
     * @return an AttributeProvider
     */
    public fun create(context: AttributeProviderContext): AttributeProvider
}
