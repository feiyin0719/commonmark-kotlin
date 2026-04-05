# commonmark-kotlin

A Kotlin Multiplatform library for parsing and rendering [Markdown](https://commonmark.org/) text according
to the [CommonMark](https://spec.commonmark.org/) specification.

This project is a Kotlin Multiplatform port of [commonmark-java](https://github.com/commonmark/commonmark-java).
Thanks to the commonmark-java team for creating such an excellent library — commonmark-kotlin would not
exist without their work.

## Features

- **Multiplatform**: Supports Android, JVM, JS (browser & Node.js), Wasm, iOS, macOS, Linux, and Windows
- **Small**: The core module has zero dependencies; extensions are provided as separate artifacts
- **Flexible**: Access the AST (Abstract Syntax Tree) after parsing for inspection or manipulation
- **Extensible**: Built-in extensions for tables, strikethrough, autolink, and more
- **Multiple renderers**: Render to HTML, Markdown, or plain text

## Supported Platforms

| Platform | Targets |
|---|---|
| Android | `androidTarget` (min SDK 24) |
| JVM | `jvm` (Java 11+) |
| JS | `js` (browser, Node.js) |
| Wasm | `wasmJs` (browser) |
| iOS | `iosX64`, `iosArm64`, `iosSimulatorArm64` |
| macOS | `macosX64`, `macosArm64` |
| Linux | `linuxX64` |
| Windows | `mingwX64` |

## Setup

Add the dependency to your project. The core module:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.feiyin0719:commonmark:<version>")
}
```

For extensions, add the corresponding artifact:

```kotlin
dependencies {
    implementation("io.github.feiyin0719:commonmark-ext-gfm-tables:<version>")
}
```

## Usage

### Parse and render to HTML

```kotlin
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

val parser = Parser.builder().build()
val document = parser.parse("This is *Markdown*")
val renderer = HtmlRenderer.builder().build()
val html = renderer.render(document)
// "<p>This is <em>Markdown</em></p>\n"
```

### Render back to Markdown

```kotlin
import org.commonmark.renderer.markdown.MarkdownRenderer

val renderer = MarkdownRenderer.builder().build()
val markdown = renderer.render(document)
```

### Render to plain text

```kotlin
import org.commonmark.renderer.text.TextContentRenderer

val renderer = TextContentRenderer.builder().build()
val text = renderer.render(document)
```

### Use extensions

```kotlin
import org.commonmark.ext.gfm.tables.TablesExtension

val extensions = listOf(TablesExtension.create())
val parser = Parser.builder().extensions(extensions).build()
val renderer = HtmlRenderer.builder().extensions(extensions).build()
```

### Visit nodes with the Visitor pattern

```kotlin
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Text

val visitor = object : AbstractVisitor() {
    override fun visit(text: Text) {
        println(text.literal)
    }
}
document.accept(visitor)
```

## Extensions

| Extension | Artifact | Description |
|---|---|---|
| Autolink | `commonmark-ext-autolink` | Automatically turns URLs into links |
| Strikethrough | `commonmark-ext-gfm-strikethrough` | GFM strikethrough (`~~text~~`) |
| Tables | `commonmark-ext-gfm-tables` | GFM tables |
| Footnotes | `commonmark-ext-footnotes` | Footnote references and definitions |
| Heading Anchor | `commonmark-ext-heading-anchor` | Generates `id` attributes for headings |
| Ins | `commonmark-ext-ins` | Inserted/underlined text (`++text++`) |
| Image Attributes | `commonmark-ext-image-attributes` | Custom attributes on images |
| Task List Items | `commonmark-ext-task-list-items` | Task lists (`- [x] done`) |
| YAML Front Matter | `commonmark-ext-yaml-front-matter` | YAML metadata at the top of documents |

## Acknowledgements

This project is a Kotlin Multiplatform port of [commonmark-java](https://github.com/commonmark/commonmark-java).
Huge thanks to the commonmark-java contributors for building a well-designed, spec-compliant Markdown
parsing library that made this port possible.

## License

MIT License - see [LICENSE](LICENSE) for details.
