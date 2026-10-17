# Template Language

`phlox` ships with a small **template language** that lets a caller customize the rendered invoice without writing Java. One template produces both HTML and PDF — the template is the **single source of truth** for layout and styling, and each writer maps it to its medium.

> **Status:** Design proposal. Not yet implemented. Targeted at the 0.4.0 milestone (see [[Roadmap]]).

## Why

The 0.1.0 writers (`HtmlInvoiceWriter`, `PdfInvoiceWriter`) lay out invoices with a fixed section grammar described in [[Output Writers]].
That covers the "show me a readable invoice" case. It does **not** cover:

- "Use our brand fonts and brand colors."
- "Hide the project reference for non-public-sector invoices."
- "Render allowances and charges inline inside the line, not as a separate section."
- "Add a custom footer block under payment instructions."

Users currently solve this by post-processing the HTML (fragile) or forking the writer (heavy).
The template language replaces both: load a `.ptpl` (phlox template) file, hand it to either writer, get the customized output.

## Composition (no new syntax invented)

A `.ptpl` file is a stack of three well-known grammars. None of them is invented by phlox; we wire them together and add a small invoice-aware data binding.

```
┌─ JMustache                         ← {{BT-X}}, {{#BG-X}}, {{?BT-X}}, {{label:...}}
├─ CommonMark + GFM extension        ← # / ## headings, **bold**, *italic*, lists, |…| tables
└─ CommonMark inline parsed by ph-pdf-layout ← same paragraph text rendered to PDF
```

Stage 1 is **JMustache 1.16+** (Apache 2.0, ~70 KB, zero transitive deps). It substitutes data values and expands loops/conditionals, producing a plain-text document.

Stage 2 is **commonmark-java 0.22+** (`org.commonmark`, Apache 2.0, maintained by Atlassian) with the GFM `tables` and `strikethrough` extensions. It parses the substituted document into a block-level AST.

Stage 3 is the **two emitters** — `HCNode` (HTML, built on `ph-oton-html`) and `PLPageSet` content (PDF, built on `ph-pdf-layout`). Paragraph text is fed through ph-pdf-layout's CommonMark-compatible inline parser, so the same `**bold**` / `*italic*` / `[text](url)` markup yields the same visual result in both media.

The upshot: a developer who knows Mustache and Markdown already knows the template language. No new lexer, no new parser, no new spec to learn.

## At a glance

```ntpl
# {{label:BT-1}}: {{BT-1}}

**{{label:BT-2}}:** {{BT-2 | date:long}}

{{?BT-10}}Buyer reference: {{BT-10}}{{/BT-10}}

## Seller

{{BG-4.BT-27}}
{{BG-4.BG-5.BT-35}}
{{BG-4.BG-5.BT-37}} {{BG-4.BG-5.BT-38}}, {{BG-4.BG-5.BT-40 | code:country}}

## Lines

| # | Item | Qty | Unit price | Net |
|---|------|----:|-----------:|----:|
{{#BG-25}}| {{BT-126}} | {{BT-153}} | {{BT-129}} {{BT-130 | code:unit}} | {{BT-146 | amount}} | {{BT-131 | amount}} |
{{/BG-25}}

## Totals

| {{label:BT-109}} | {{BT-109 | amount}} |
| {{label:BT-110}} | {{BT-110 | amount}} |
| **{{label:BT-115}}** | **{{BT-115 | amount}}** |
```

The same file renders to HTML (semantic `<h1>`, `<h2>`, `<table>`, `<strong>`, `<em>`) and to PDF (`PLText` headings, `PLTable` for the line grid, bold runs inside `PLRichText` for the totals row).

## Design constraints

| Constraint | Consequence |
|---|---|
| Inline styling is **CommonMark + GFM**, parsed natively by ph-pdf-layout | `**bold**`, `*italic*`, `` `code` ``, `~~strike~~`, `[text](url)`, hard line break (`\`) — same byte sequence renders in both media. |
| `commonmark-java` parses block structure (headings, lists, blockquote, fenced code, tables) | Block-level AST is shared; emitters diverge only on the output API call. |
| CommonMark does **not** define inline color, underline, font size, or background | These are intentionally absent from the template grammar. Block-level color comes from a per-block attribute syntax (see "Block attributes"); document-wide colors come from `HtmlRenderContext.setCustomStylesheet(...)` / `PdfRenderContext` font setters. |
| Tables can't nest paragraphs of arbitrary block content | GFM tables are single-line cells with inline-only content — matches what `PLTable` supports natively. |
| Templates must not leak BT-empty placeholders | The Mustache adapter respects `IncludeEmptySections` / `AnnotateBtIDs` — same rules as the built-in writers. |

## Syntax overview

| Construct | Source grammar | Example |
|---|---|---|
| Value placeholder | JMustache | `{{BT-1}}` |
| Path placeholder | JMustache (custom resolver) | `{{BG-4.BT-27}}` |
| Label placeholder | JMustache (custom resolver) | `{{label:BT-1}}` |
| Format modifier | phlox extension | `{{BT-2 \| date:long}}` |
| Optional block | JMustache `{{#...}}` truthy test | `{{?BT-10}}…{{/BT-10}}` |
| Inverted block | JMustache `{{^...}}` | `{{^BG-10}}…{{/BG-10}}` |
| Loop block | JMustache `{{#...}}` over iterable | `{{#BG-25}}…{{/BG-25}}` |
| Comment | JMustache | `{{! anything }}` |
| Page heading | CommonMark ATX heading | `# title` |
| Section heading | CommonMark ATX heading | `## title` |
| Sub-section heading | CommonMark ATX heading | `### title` |
| Paragraph | CommonMark | (blank-line-separated text) |
| Unordered list | CommonMark | `- item` |
| Ordered list | CommonMark | `1. item` |
| Blockquote | CommonMark | `> note` |
| Fenced code | CommonMark | ` ``` …  ``` ` |
| Thematic break | CommonMark | `---` |
| Inline strong | CommonMark | `**bold**` |
| Inline emphasis | CommonMark | `*italic*` |
| Inline code | CommonMark | `` `code` `` |
| Inline strikethrough | GFM extension | `~~text~~` |
| Inline link | CommonMark | `[text](https://example.com)` |
| Auto-link | GFM autolink ext. | `<https://example.com>` |
| Hard line break | CommonMark | trailing `\` or two spaces |
| Image | CommonMark | `![alt](data:…)` (rendered in both HTML and PDF; see "Images" below) |
| Table | GFM tables extension | `\| col \| col \|` … |
| Backslash escape | CommonMark | `\*`, `\_`, `\\`, `\|` |
| Page break | phlox directive | `{{pagebreak}}` |
| Block attributes | phlox directive (optional) | `{{class:totals-row}}` before a block |

The grammar is a strict superset of CommonMark 0.30 with the two GFM extensions enabled. The phlox additions are all Mustache placeholders, so they're invisible to plain CommonMark tools and don't break syntax highlighting.

## Placeholders

Every EN 16931 BT and BG defined in [[Domain Model]] is addressable.

### Resolution

`{{BT-1}}` goes through the phlox Mustache resolver, which:

1. Parses the placeholder name as a BT or BG identifier (or a `label:` / format modifier).
2. Walks the `PhloxInvoice` to the matching getter.
3. Applies a default formatter chosen from the Java type.
4. Returns a HTML-escaped string in the HTML pipeline, a raw string in the PDF pipeline (PDF text escaping is handled by `PLRichText`).

| Java type on the model | Default formatter | Source |
|---|---|---|
| `String` | as-is | direct |
| `LocalDate` | `PhloxFormat.date(val, locale)` — medium style | [[Output Writers]] |
| `BigDecimal` amount BTs | `PhloxFormat.amount(val, BT-5 currency, locale)` | currency taken from BT-5 |
| `BigDecimal` percentage BTs | `PhloxFormat.percent(val, locale)` | two decimals + `%` |
| Code BTs (UNTDID 1001, 5305, etc.) | `PhloxFormat.codeListLabel(listName, code, locale)` | list inferred from BT |
| `PhloxIdentifier` | scheme-prefixed if BT-X-1 set, otherwise the value | as in built-in writers |
| `ICommonsList<…>` | only valid inside a loop block | error otherwise |

### Path syntax

JMustache resolves `{{a.b.c}}` left-to-right. The phlox resolver overloads dot to also walk into BGs.

- `{{BG-4.BT-27}}` — seller name.
- `{{BG-4.BG-5.BT-37}}` — seller postal-address city.
- `{{BG-22.BT-115}}` — payable amount.
- Inside `{{#BG-25}}…{{/BG-25}}`, unqualified `{{BT-126}}` refers to the **current line's** BT-126. JMustache's parent-scope lookup (`{{.../}}`) is reused as-is to climb back to the document.

### Format modifiers

A trailing `| modifier` selects an explicit formatter, exactly the same pipe convention as Mustache and most templating engines:

| Modifier | Effect | Example |
|---|---|---|
| `date` | Default date style (medium) | `{{BT-2 \| date}}` |
| `date:short` / `date:medium` / `date:long` | Explicit style | `{{BT-9 \| date:long}}` |
| `amount` | Amount with invoice currency (BT-5) | `{{BT-115 \| amount}}` |
| `amount:BT-6` | Amount in a different currency BT | `{{BT-111 \| amount:BT-6}}` |
| `percent` | Two-decimal percentage | `{{BT-119 \| percent}}` |
| `code:listName` | Code-list lookup | `{{BT-3 \| code:doctype}}` |
| `raw` | Skip formatting; print the underlying value | `{{BT-2 \| raw}}` |
| `upper` / `lower` / `title` | Letter case | `{{BG-4.BT-27 \| upper}}` |

Modifiers are pure functions over the resolved value and the active `RenderContext.Locale`. They do not see neighboring template state.

### EN 16931:2026 terms

All BGs and BTs of EN 16931:2026 (BG-33 to BG-39, BT-166 to BT-220) resolve like the 2017 ones; they are empty for EN 16931:2017 documents, so wrap them in `{{?…}}` / `{{#…}}` sections. Notes:

- `{{BT-10}}` renders the buyer references comma-joined, each followed by its scheme identifier in parentheses (BT-10 is a list since 2026).
- BT-20 is addressed inside BG-33: `{{#BG-33}}{{BT-20}}{{/BG-33}}`. BG-35 and BG-36 are lists inside BG-33; BG-34 is a list inside BG-22; BG-37 is a single object and BG-39 a list inside BG-25.
- List-valued paths (`{{#BG-x}}`) reach JMustache unchanged, so sections iterate over the elements. Modifiers are always separated by a plain `|`, also inside Markdown table rows: placeholders are substituted before the Markdown is parsed, so no `\|` escape is needed (the escaped pipes in the tables of this wiki page are only for GitHub's own table renderer).

### Labels

`{{label:BT-1}}` returns the localized label for the BT, resolved through `IPhloxLabelResolver` exactly like the built-in writers. `{{label:BG-4}}` works the same way for BGs. `{{label:section.parties.title}}` reads an arbitrary key from the resource bundle, so templates can use the same translation file as the rest of the library.

### Missing values

A BT may be `null`, an empty `String`, or — for lists — empty. By default the placeholder renders as the empty string and surrounding whitespace is preserved.

| `RenderContext` flag | Behavior |
|---|---|
| `IncludeEmptySections=false` (default) | Empty placeholders disappear; CommonMark then collapses empty paragraphs and rows during AST construction. |
| `IncludeEmptySections=true` | Empty values render as `—`; lines and rows are preserved. |
| `AnnotateBtIDs=true` | Every placeholder's output is suffixed with `[BT-1]` for debugging — same convention as the built-in writers. |

## Sections and loops

### Optional and inverted blocks

```ntpl
{{?BT-10}}Buyer reference: {{BT-10}}{{/BT-10}}
{{^BG-10}}No payee specified.{{/BG-10}}
```

`{{?...}}` is sugar for JMustache's `{{#...}}` truthy test (non-null `String` of length > 0, non-null scalar, non-empty list, non-null BG). `{{^...}}` is JMustache's native inverted section.

### Loops

```ntpl
{{#BG-25}}
- {{BT-126}}: {{BT-153}}
{{/BG-25}}
```

JMustache iterates every entry of a list-typed BG. Inside the loop, the current entry is the **default scope**, so unqualified BT references resolve against it. The full set of list-typed BGs:

| BG | Java field | Iterates over |
|---|---|---|
| BG-1 | `m_aInvoiceNotes` | `PhloxInvoiceNote` |
| BG-3 | `m_aPrecedingInvoiceRefs` | `PhloxPrecedingInvoiceRef` |
| BG-20 | `m_aDocumentAllowances` | `PhloxDocumentAllowance` |
| BG-21 | `m_aDocumentCharges` | `PhloxDocumentCharge` |
| BG-23 | `m_aVatBreakdowns` | `PhloxVatBreakdown` |
| BG-24 | `m_aSupportingDocuments` | `PhloxSupportingDocument` |
| BG-25 | `m_aLines` | `PhloxInvoiceLine` |
| BG-29 / BG-30 / BG-31 / BG-32 | nested in lines | per-line allowances, charges, item attributes |

Nested loops are allowed: a `BG-25` loop can contain a `BG-29` (line-level allowances) loop.

### Sections

CommonMark ATX headings (`#`, `##`, `###`) are the section primitive. The renderer maps the heading level to the natural output:

- HTML: `<h1>` / `<h2>` / `<h3>` with the configured CSS class prefix.
- PDF: `PLText` with descending font sizes derived from `PdfRenderContext.getHeaderFont()`.

There is no separate "section" directive; a heading marks a section. For an explicit page break in the PDF output, use the placeholder `{{pagebreak}}`. CommonMark `---` (thematic break) renders as `<hr>` in HTML and as a thin horizontal rule in PDF — it is **not** a page break.

## Tables

GFM-tables syntax — recognized by `commonmark-java` with the `org.commonmark:commonmark-ext-gfm-tables` extension enabled. Combined with a loop block:

```ntpl
| # | Item | Qty | VAT % | Net |
|---|------|----:|------:|----:|
{{#BG-25}}| {{BT-126}} | {{BT-153}} | {{BT-129}} | {{BT-152 | percent}} | {{BT-131 | amount}} |
{{/BG-25}}
```

- The header row defines column count.
- The alignment row chooses left/right/center per column (`:--`, `--:`, `:-:`). PDF maps these to `EHorzAlignment.LEFT / RIGHT / CENTER`; HTML maps them to a `text-align` style.
- A row inside `{{#BG-X}}…{{/BG-X}}` is repeated once per entry of the list.
- Inline CommonMark markup inside cells works as elsewhere — bold, italic, links, code.
- A blank line ends the table.

HTML: `<table><thead><tr><th>…</th></tr></thead><tbody><tr><td>…</td></tr></tbody></table>` with the CSS class prefix from `HtmlRenderContext.setCssClassPrefix(...)`.
PDF: a `PLTable` with the same column count, the header row styled with `PdfRenderContext.tableHeaderFont`, body rows styled with the body font, and column widths derived either from an explicit column-widths attribute or from a default even split.

## Block attributes (optional)

Some block-level customizations have no CommonMark equivalent — most obviously the per-row background fill on the totals ladder. The template engine recognizes an optional `{{class:name}}` placeholder at the start of a block; on the HTML side it becomes a class attribute, on the PDF side it looks up a `PLTableCell` fill color from a small style map exposed on `PdfRenderContext`. This is the only place where per-block style escapes the CommonMark grammar.

```ntpl
{{class:totals-due}}
| **{{label:BT-115}}** | **{{BT-115 | amount}}** |
```

No syntax is added for inline color: if a template truly needs it, the caller can either use a class on the surrounding block, or attach a `HtmlRenderContext.setCustomStylesheet(...)` for the HTML side (the PDF side won't see it).

## Images

CommonMark image syntax — `![alt](url)` — renders in both writers, but with one structural difference the template author must understand: **`PLImage` is a block element in ph-pdf-layout — it cannot sit inside a `PLRichText` run.** HTML has no such restriction; an `<img>` sits inside `<p>` happily.

### Asymmetric handling

| Source paragraph | HTML emitter | PDF emitter |
|---|---|---|
| Pure text, no image | one `<p>` | one `PLRichText` |
| Text + `![alt](url)` + text on the same line | one `<p>` containing two text runs around an inline `<img>` | three siblings inside the parent block: pre-text `PLRichText`, `PLImage`, post-text `PLRichText` — i.e. the paragraph is **split** around the image |
| `![alt](url)` alone on its own line (CommonMark block-level image) | one `<p><img></p>` | one `PLImage` block |

The PDF emitter never tries to flow text around the image; it lifts the image out and continues text on the next block. This visibly differs from HTML, where the image sits on the same baseline as the surrounding text.

**Recommendation:** put images on their own line (i.e. as a CommonMark block-level image) whenever the HTML and PDF outputs need to look the same. Inline-with-text only when the HTML/PDF divergence is acceptable — typically: small inline icons in HTML degrade to a vertical break in PDF.

### URL schemes

Per [[Output Writers]], **writers never fetch external resources**. Only these schemes are accepted; everything else fails fast at parse time.

| Scheme | Resolves to | HTML side | PDF side |
|---|---|---|---|
| `data:` | Inline base64-encoded bytes | passed through verbatim | decoded into a `PLImage` |
| `attachment:logo` | The `PhloxAttachment` set via `RenderContext.setLogo(...)` | rendered as a data-URI `<img>` | wrapped in a `PLImage` |
| `attachment:BG-24[i]` | The i-th entry of BG-24 supporting documents (if it carries inline binary) | rendered as a data-URI `<img>` | wrapped in a `PLImage` |
| `classpath:/path` | A resource on the writer's classpath | inlined as a data URI | wrapped in a `PLImage` |
| `http:`, `https:`, `file:` | (rejected at parse time) | — | — |

## Defaults

A built-in default template reproduces the layout described in [[Output Writers]] section-for-section. Callers don't have to write a template to get a usable invoice — they only set one when they want to override layout. The default template ships as a single language-independent resource under `phlox-template/src/main/resources/templates/default.ptpl` and is loaded if no other template is set on the context. All localized strings inside it route through `{{label:...}}` placeholders, so new languages are added by dropping in a new `phlox-labels_<lang>.properties` file in `phlox-api` — never a new template. See [[Localization]] for the resource-bundle layout.

## Integration

A new abstract setter on `AbstractRenderContext`:

```java
public IMPLTYPE setTemplate (@Nullable IPhloxTemplate aTemplate);
```

`IPhloxTemplate` is a small interface so the same template can be loaded from a classpath resource, a `String`, or a file. The engine parses the template (Mustache compile + CommonMark parse of the post-substitution document) into an AST per render; the Mustache compile is cached on the `IPhloxTemplate` instance, so reusing the same template across many invoices is cheap.

```java
final IPhloxTemplate aTpl = PhloxTemplate.fromClasspath ("/templates/my-brand.ptpl");
final HtmlRenderContext aCtxHtml = new HtmlRenderContext ()
    .setLocale (Locale.GERMAN)
    .setTemplate (aTpl);
final String sHtml = new HtmlInvoiceWriter ().writeAsString (aInvoice, aCtxHtml);

final PdfRenderContext aCtxPdf = new PdfRenderContext ()
    .setLocale (Locale.GERMAN)
    .setTemplate (aTpl);
new PdfInvoiceWriter ().writeTo (aInvoice, aCtxPdf, aOutputStream);
```

Both writers consult `RenderContext.getTemplate()`. When it is `null`, they fall back to their hard-coded section grammar — so existing call sites keep working unchanged.

## Output mapping

How each CommonMark / GFM / Mustache construct lands in the two output formats:

| Template construct | HTML | PDF (via `ph-pdf-layout`) |
|---|---|---|
| `# title` | `<h1 class="phlox-title">title</h1>` | `PLText` with `headerFont`, largest size |
| `## title` | `<h2 class="phlox-section-title">title</h2>` | `PLText` with `headerFont` |
| `### title` | `<h3 class="phlox-subsection-title">title</h3>` | `PLText` with `headerFont`, smaller size |
| Paragraph | `<p>…</p>` | `PLRichText` populated from ph-pdf-layout's CommonMark inline parser |
| `**bold**` | `<strong>` | bold run inside `PLRichText` |
| `*italic*` | `<em>` | italic run inside `PLRichText` |
| `~~strike~~` (GFM) | `<s>` | strikethrough run inside `PLRichText` |
| `` `code` `` | `<code>` | monospace run inside `PLRichText` |
| `[text](uri)` | `<a href="uri">text</a>` | hyperlink annotation inside `PLRichText` |
| `<https://…>` (autolink) | `<a href="…">…</a>` | hyperlink annotation |
| `- item` (unordered list) | `<ul><li>…</li></ul>` | `ph-pdf-layout` bullet list block |
| `1. item` (ordered list) | `<ol><li>…</li></ol>` | `ph-pdf-layout` numbered list block |
| `> quote` | `<blockquote>…</blockquote>` | indented block with left rule |
| ` ``` …  ``` ` | `<pre><code>…</code></pre>` | indented block in monospace font |
| `---` (thematic break) | `<hr>` | thin horizontal rule |
| GFM table | `<table>…</table>` | `PLTable` |
| Inline image `![alt](url)` | `<img src="…" alt="…">` (inline inside `<p>`) | `PLImage` as a standalone block; the surrounding paragraph is split into pre-image `PLRichText` + `PLImage` + post-image `PLRichText` (see "Images" below) |
| `{{pagebreak}}` | `<div style="page-break-after:always"></div>` | `PLPageBreak` |
| `{{class:name}}` before block (or before a table row) | class attribute on the next block / table row | `PLTableCell` fill / `PLBox` fill from `PdfRenderContext` style map |

## Limitations

A direct consequence of "CommonMark is the grammar":

- **No inline color**, **no inline underline**, **no inline font size**, **no inline background color** — CommonMark doesn't define them, and we explicitly choose not to add custom inline extensions. Use `{{class:...}}` on the enclosing block, a `setCustomStylesheet(...)` (HTML only), or the body/header font setters (PDF only).
- **No nested block content inside table cells** — GFM tables permit inline-only cell content. A multi-paragraph cell isn't expressible. This matches `PLTable`'s capabilities and the EU "Common View" stylesheet conventions for invoices.
- **No subscript / superscript** — CommonMark has neither. Use Unicode (`m²`, `H₂O`) when needed.
- **No raw HTML pass-through** — the HTML emitter rejects CommonMark's raw-HTML blocks at parse time, because the PDF emitter can't honor them. This keeps the two outputs symmetric and avoids HTML injection through templates.
- **No template includes / partials** — JMustache supports partials (`{{> name}}`); we leave them disabled in 0.4.0 to keep templates single-file. May relax in a later milestone.

When a template uses an unsupported construct the engine fails fast at parse time with a precise location, not at render time.

## Resolution algorithm

1. **Substitute** (JMustache) — walk the Mustache AST with a `PhloxInvoice`-aware `Mustache.CustomContext`, applying default formatters and `{{label:...}}` lookups. Output: a CommonMark document as a `String`. Loops have been expanded; conditionals have been resolved; placeholders have been replaced with locale-aware formatted values.
2. **Parse** (commonmark-java with `TablesExtension` + `StrikethroughExtension`) — turn the document into a `Node` AST. Standard CommonMark parser, no phlox-specific modifications.
3. **Emit** — visit the AST through one of two `AbstractVisitor` subclasses:
   - `HTMLEmitter` builds an `HCNode` tree (same `ph-oton-html` types the current `HtmlInvoiceWriter` uses).
   - `PDFEmitter` builds `PLPageSet` content (same `ph-pdf-layout` types the current `PdfInvoiceWriter` uses). Paragraphs feed their inline content through ph-pdf-layout's CommonMark inline parser.

Each stage is independently testable:

- Stage 1 by snapshot-testing the substituted CommonMark output for a fixture invoice.
- Stage 2 covered by `commonmark-java`'s own spec suite — we trust it.
- Stage 3 by the existing writer harness — golden HTML and visual-regression PDF (see [[Output Writers]]).

## Dependencies added

| Module | New dep | Version | Size | License |
|---|---|---|---|---|
| `phlox-api` | (none — only the marker interface `IPhloxTemplate` is added; zero new transitive deps) | — | — | — |
| `phlox-template` | `com.samskivert:jmustache` | 1.16 | ~70 KB | BSD-2-Clause (Apache 2.0-compatible) |
| `phlox-template` | `org.commonmark:commonmark` | 0.22.0 | ~120 KB | BSD-2-Clause |
| `phlox-template` | `org.commonmark:commonmark-ext-gfm-tables` | 0.22.0 | ~25 KB | BSD-2-Clause |
| `phlox-template` | `org.commonmark:commonmark-ext-gfm-strikethrough` | 0.22.0 | ~10 KB | BSD-2-Clause |
| `phlox-html` | `phlox-template` (compile) | (this project) | — | Apache 2.0 |
| `phlox-pdf` | `phlox-template` (compile) + (existing) `ph-pdf-layout` with its CommonMark inline support | (TBD) | (unchanged) | Apache 2.0 |

No JRuby, no servlet container, no template-engine framework. All four new third-party deps are pure Java and version-stable.

Because `phlox-template` is a compile-scope dependency of `phlox-html` and `phlox-pdf`, the four jars are transitively pulled onto the classpath of any consumer of either writer (~225 KB total, ~250 KB with `phlox-template` itself). Consumers who use the writers without ever calling `setTemplate(...)` will load those classes lazily — the runtime cost is effectively zero — but they're on the classpath. Consumers of `phlox-api`, `phlox-ubl`, or `phlox-cii` alone (e.g. an embedding that only parses invoices) get neither `phlox-template` nor its deps.

If the classpath cost ever becomes a problem, a future revision can make the dep optional (Maven `<optional>true</optional>`) and load the emitter via `ServiceLoader` — that's a non-breaking change.

## See also

- [[Template Processing]] — the implementation companion: pipeline stages, threading, error handling, integration with writers.
- [[Output Writers]] — the section grammar the default template reproduces.
- [[Domain Model]] — the full BG/BT catalog addressable from a template, plus the EN 16931 semantic-type vocabulary that picks default formatters.
- [[Localization]] — labels, code-lists, and locale-aware formatters.
- [[Configuration Properties]] — `setTemplate(...)` on `AbstractRenderContext`.
- [JMustache](https://github.com/samskivert/jmustache) — Mustache implementation.
- [commonmark-java](https://github.com/commonmark/commonmark-java) — CommonMark parser.
- [CommonMark spec](https://spec.commonmark.org/0.30/) — authoritative inline + block grammar.
- [GFM tables extension](https://github.github.com/gfm/#tables-extension-) — the table syntax we adopt.
