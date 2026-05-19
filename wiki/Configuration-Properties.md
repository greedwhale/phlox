# Configuration Properties

`norma` itself is **stateless** — it reads no configuration files at startup. All tuning is done by passing values to a `RenderContext` (or one of its output-specific subclasses) at render time.

This page lists every knob a caller can turn.

## Context-class hierarchy

```
RenderContext               (norma-api — syntax-neutral knobs only)
   ├─ HTMLRenderContext     (norma-html — adds CSS/stylesheet options)
   └─ PDFRenderContext      (norma-pdf  — adds page-size/margin/font options)
```

Each writer's `write(...)` accepts its own context subtype, so the compiler rejects accidental mixups like passing a `PDFRenderContext` to the HTML writer. The base class lives in `norma-api` so it never pulls `ph-pdf-layout` or `ph-oton-html` types onto the API classpath.

## `RenderContext` (norma-api)

Shared by every writer.

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setLocale(Locale)` | `Locale` | `Locale.ENGLISH` | Selects the resource bundle for labels and the formatter for dates/amounts/percents. See [[Localization]]. |
| `setFormatOptions(NormaFormatOptions)` | `NormaFormatOptions` | `NormaFormatOptions.defaults()` | Per-call overrides for date pattern, number grouping, currency-symbol position. |
| `setLogo(NormaAttachment)` | `NormaAttachment` | `null` | Image to draw in the header. Each writer adapts it to its medium (HTML data-URI, PDF `PLImage`). |
| `setLabelResolver(INormaLabelResolver)` | `INormaLabelResolver` | `DefaultNormaLabelResolver` | Replace the resource-bundle lookup with a custom one (useful for branding). |
| `setAnnotateBTIDs(boolean)` | `boolean` | `false` | When true, every label is suffixed with its EN 16931 ID (`Invoice number [BT-1]`). Debug aid. |
| `setIncludeEmptySections(boolean)` | `boolean` | `false` | When true, render section headings even if their content is empty. Off by default — clean output. |

All setters return the **concrete** context type (Helger `IMPLTYPE` pattern), so fluent chains stay typed across the hierarchy:

```java
final HTMLRenderContext aCtx = new HTMLRenderContext ()
    .setLocale (Locale.GERMAN)        // returns HTMLRenderContext, not RenderContext
    .setEmbeddedStylesheet (false);   // HTML-only setter still in chain
```

## `HTMLRenderContext` (norma-html)

Inherits all `RenderContext` knobs and adds:

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setCSSClassPrefix(String)` | `String` | `"norma-"` | Prefix applied to every CSS class emitted by `HTMLInvoiceWriter`. |
| `setEmbeddedStylesheet(boolean)` | `boolean` | `true` | Embed CSS in a `<style>` block; when false, only class names are emitted (caller supplies external CSS). |
| `setCustomStylesheet(String)` | `String` | `null` | Additional CSS text appended after the default stylesheet (when `EmbeddedStylesheet` is on). |
| `setEmitDoctype(boolean)` | `boolean` | `true` | When false, returns just the `<html>...</html>` subtree without `<!DOCTYPE html>` — useful for embedding. |

## `PDFRenderContext` (norma-pdf)

Inherits all `RenderContext` knobs and adds:

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setPageSize(PDRectangle)` | `org.apache.pdfbox.pdmodel.common.PDRectangle` | `PDRectangle.A4` | Page size. |
| `setMargins(MarginSpec)` | `com.helger.pdflayout.spec.MarginSpec` | 20 mm on all sides | Page margins. |
| `setBodyFont(FontSpec)` | `com.helger.pdflayout.spec.FontSpec` | Helvetica 10pt black | Default font for body text. |
| `setHeaderFont(FontSpec)` | `FontSpec` | Helvetica-Bold 12pt black | Default font for section headings. |
| `setTableHeaderFont(FontSpec)` | `FontSpec` | Helvetica-Bold 10pt black | Font for table column headers (line table, VAT breakdown). |

These setters expose `ph-pdf-layout` types directly — fine because `PDFRenderContext` lives in `norma-pdf`, which already depends on that library.

## `NormaFormatOptions`

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
final HTMLRenderContext aHtmlCtx = new HTMLRenderContext ()
    .setLocale (Locale.GERMAN)
    .setAnnotateBTIDs (true);

// Same shared settings, different output-specific extras:
final PDFRenderContext aPdfCtx = PDFRenderContext.copyFrom (aHtmlCtx)
    .setHeaderFont (new FontSpec (PreloadFont.REGULAR_BOLD, 14f));
```

`copyFrom(RenderContext)` is a static factory on each subclass; it copies only the base fields and leaves the format-specific knobs at their defaults.

## Worked example

```java
final PDFRenderContext aCtx = new PDFRenderContext ()
    .setLocale (Locale.GERMAN)
    .setAnnotateBTIDs (false)
    .setFormatOptions (new NormaFormatOptions ()
        .setDateStyle (FormatStyle.LONG)
        .setNegativeAmountStyle (ENegativeStyle.PARENTHESES))
    .setHeaderFont (new FontSpec (PreloadFont.REGULAR_BOLD, 14f));

new PDFInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
```

## What `norma` does **not** configure

- **No system properties.** Nothing reads `System.getProperty(...)`.
- **No environment variables.** Same.
- **No `application.properties`.** `norma` does not depend on `ph-commons` config or Spring.
- **No service-loader knobs.** Readers and writers are instantiated by the caller directly.

The intent: `norma` should be safe to embed inside any host application without inheriting global state.
