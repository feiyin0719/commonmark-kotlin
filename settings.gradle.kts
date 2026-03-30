pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "commonmark-kotlin"
include(":commonmark")
include(":commonmark-ext-ins")
include(":commonmark-ext-gfm-strikethrough")
include(":commonmark-ext-gfm-tables")
include(":commonmark-ext-yaml-front-matter")
include(":commonmark-ext-task-list-items")
include(":commonmark-ext-heading-anchor")
include(":commonmark-ext-image-attributes")
include(":commonmark-ext-autolink")
include(":commonmark-ext-footnotes")
include(":commonmark-ext-html-converter")
