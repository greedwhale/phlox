# Configuration Properties

`phlox` itself is **stateless** — it reads no configuration files at startup. All tuning is done by passing values to a `RenderContext` (or one of its output-specific subclasses) at render time.

This page lists every knob a caller can turn.

## Context-class hierarchy

```
AbstractRenderContext       (phlox-api — syntax-neutral knobs only; abstract)
   ├─ HtmlRenderContext     (phlox-html — adds CSS/stylesheet options)
   └─ PdfRenderContext      (phlox-pdf  — adds page-size/margin/font options)
```

Each writer's `write(...)` accepts its own context subtype, so the compiler rejects accidental mixups like passing a `PdfRenderContext` to the HTML writer. The base class lives in `phlox-api` so it never pulls `ph-pdf-layout` or `ph-oton-html` types onto the API classpath.

## `AbstractRenderContext` (phlox-api)

Shared by every writer.

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setLocale(Locale)` | `Locale` | `Locale.ENGLISH` | Selects the resource bundle for labels and the formatter for dates/amounts/percents. See [[Localization]]. |
| `setFormatOptions(PhloxFormatOptions)` | `PhloxFormatOptions` | `PhloxFormatOptions.defaults()` | Per-call overrides for date pattern, number grouping, currency-symbol position. |
| `setLogo(PhloxAttachment)` | `PhloxAttachment` | `null` | Image to draw in the header. Each writer adapts it to its medium (HTML data-URI, PDF `PLImage`). |
| `setLabelResolver(IPhloxLabelResolver)` | `IPhloxLabelResolver` | `DefaultPhloxLabelResolver` | Replace the resource-bundle lookup with a custom one (useful for branding). |
| `setAnnotateBtIDs(boolean)` | `boolean` | `false` | When true, every label is suffixed with its EN 16931 ID (`Invoice number [BT-1]`). Debug aid. |
| `setIncludeEmptySections(boolean)` | `boolean` | `false` | When true, render section headings even if their content is empty. Off by default — clean output. |
| `setUserWatermark(WatermarkSpec)` | `WatermarkSpec` | `null` | A caller-defined watermark drawn on every page ("DRAFT", "PRELIMINARY", ...). Separate from the library's program watermark. See [[Watermarking]]. |
| `setTemplate(IPhloxTemplate)` | `IPhloxTemplate` | `null` | Optional custom `.ptpl` template. When set, `HtmlInvoiceWriter` and `PdfInvoiceWriter` render through the template (`HtmlTemplateEmitter` / `PdfTemplateEmitter`) instead of their hard-coded section grammar; when `null`, they use the built-in layout. The `IPhloxTemplate` marker interface lives in `phlox-api`; the parser implementation lives in the `phlox-template` module. See [[Template Language]]. |

All setters return the **concrete** context type (Helger `IMPLTYPE` pattern), so fluent chains stay typed across the hierarchy:

```java
final HtmlRenderContext aCtx = new HtmlRenderContext ()
    .setLocale (Locale.GERMAN)        // returns HtmlRenderContext, not RenderContext
    .setEmbeddedStylesheet (false);   // HTML-only setter still in chain
```

## `HtmlRenderContext` (phlox-html)

Inherits all `RenderContext` knobs and adds:

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setCssClassPrefix(String)` | `String` | `"phlox-"` | Prefix applied to every CSS class emitted by `HtmlInvoiceWriter`. |
| `setEmbeddedStylesheet(boolean)` | `boolean` | `true` | Embed CSS in a `<style>` block; when false, only class names are emitted (caller supplies external CSS). |
| `setCustomStylesheet(String)` | `String` | `null` | Additional CSS text appended after the default stylesheet (when `EmbeddedStylesheet` is on). |
| `setEmitDoctype(boolean)` | `boolean` | `true` | When false, returns just the `<html>...</html>` subtree without `<!DOCTYPE html>` — useful for embedding. |

## `PdfRenderContext` (phlox-pdf)

Inherits all `RenderContext` knobs and adds:

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setPageSize(PDRectangle)` | `org.apache.pdfbox.pdmodel.common.PDRectangle` | `PDRectangle.A4` | Page size. |
| `setMargins(MarginSpec)` | `com.helger.pdflayout.spec.MarginSpec` | 20 mm on all sides | Page margins. |
| `setBodyFont(FontSpec)` | `com.helger.pdflayout.spec.FontSpec` | Helvetica 10pt black | Default font for body text. |
| `setHeaderFont(FontSpec)` | `FontSpec` | Helvetica-Bold 12pt black | Default font for section headings. |
| `setTableHeaderFont(FontSpec)` | `FontSpec` | Helvetica-Bold 10pt black | Font for table column headers (line table, VAT breakdown). |
| `putBlockStyle(String, PdfBlockStyle)` | `PdfBlockStyle` (fill and text `PLColor`, both optional) | `totals-due` → light grey fill | Look of the template blocks and table rows marked with `{{class:name}}`. A class without a style is logged at WARN and rendered unstyled. Only used in template mode. |

These setters expose `ph-pdf-layout` types directly — fine because `PdfRenderContext` lives in `phlox-pdf`, which already depends on that library.

## `PhloxFormatOptions`

Optional fine-tuning of locale-driven formatting. Held by `RenderContext.setFormatOptions(...)`; shared across writers.

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setDateStyle(FormatStyle)` | `FormatStyle` | `FormatStyle.MEDIUM` | Date format (e.g. `MEDIUM` = `Mar 14, 2025` / `14.03.2025`). |
| `setAmountFractionDigits(Integer)` | `Integer` | `null` (use currency default) | Force a fixed number of decimal places for amounts. |
| `setPercentFractionDigits(int)` | `int` | `2` | Decimal places for VAT rates and discount percentages. |
| `setNegativeAmountStyle(ENegativeStyle)` | `ENegativeStyle` | `MINUS_SIGN` | `MINUS_SIGN` (`-1.234,56`) or `PARENTHESES` (`(1.234,56)`). |

## Reusing a configuration across formats

A small helper on each subclass copies the base settings into the other subclass:

```java
final HtmlRenderContext aHtmlCtx = new HtmlRenderContext ()
    .setLocale (Locale.GERMAN)
    .setAnnotateBtIDs (true);

// Same shared settings, different output-specific extras:
final PdfRenderContext aPdfCtx = PdfRenderContext.copyFrom (aHtmlCtx)
    .setHeaderFont (new FontSpec (PreloadFont.REGULAR_BOLD, 14f));
```

`copyFrom(RenderContext)` is a static factory on each subclass; it copies only the base fields and leaves the format-specific knobs at their defaults.

## Worked example

```java
final PdfRenderContext aCtx = new PdfRenderContext ()
    .setLocale (Locale.GERMAN)
    .setAnnotateBtIDs (false)
    .setFormatOptions (new PhloxFormatOptions ()
        .setDateStyle (FormatStyle.LONG)
        .setNegativeAmountStyle (ENegativeStyle.PARENTHESES))
    .setHeaderFont (new FontSpec (PreloadFont.REGULAR_BOLD, 14f));

new PdfInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
```

## Program watermark — set once, not per-render

`PhloxProgramWatermark.setInstance(...)` (in `phlox-api`) installs a library-wide attribution watermark that all writers draw on every output. It is **not** part of `RenderContext` because it is a distribution-time choice, not a render-time one:

```java
// Typically called once at JVM startup — e.g. from a commercial distribution's bootstrap:
PhloxProgramWatermark.setInstance (null);          // suppress entirely
// or
PhloxProgramWatermark.setInstance (WatermarkSpec.text ("ACME Invoicing v4.2"));
```

The open-source build ships with a small low-opacity attribution in the corner; the commercial build typically replaces or suppresses it. See [[Watermarking]] for the full design and the rendered appearance in HTML vs PDF.

## What `phlox` does **not** configure

- **No system properties.** Nothing reads `System.getProperty(...)`.
- **No environment variables.** Same.
- **No `application.properties`.** `phlox` does not depend on `ph-commons` config or Spring.
- **No service-loader knobs.** Readers and writers are instantiated by the caller directly.

The intent: `phlox` should be safe to embed inside any host application without inheriting global state.
