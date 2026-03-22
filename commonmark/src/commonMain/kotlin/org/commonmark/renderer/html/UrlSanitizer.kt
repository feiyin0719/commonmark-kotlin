package org.commonmark.renderer.html

/**
 * Sanitizes urls for img and a elements by whitelisting protocols.
 * This is intended to prevent XSS payloads like `[Click this totally safe url](javascript:document.xss=true;)`
 *
 * Implementation based on https://github.com/OWASP/java-html-sanitizer
 */
public interface UrlSanitizer {
    /**
     * Sanitize a url for use in the href attribute of a link.
     *
     * @param url Link to sanitize
     * @return Sanitized link
     */
    public fun sanitizeLinkUrl(url: String): String

    /**
     * Sanitize a url for use in the src attribute of an image.
     *
     * @param url Link to sanitize
     * @return Sanitized link
     */
    public fun sanitizeImageUrl(url: String): String
}
