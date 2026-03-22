package org.commonmark.ext.autolink

/**
 * The types of strings that can be automatically turned into links.
 */
public enum class AutolinkType {
    /**
     * URL such as `http://example.com`.
     */
    URL,

    /**
     * Email address such as `foo@example.com`.
     */
    EMAIL,

    /**
     * URL such as `www.example.com`.
     */
    WWW
}
