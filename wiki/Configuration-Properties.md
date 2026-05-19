# Configuration Properties

`norma` itself is **stateless** — it reads no configuration files at startup. All tuning is done by passing values to `RenderContext` and `NormaFormatOptions` at render time.

This page lists every knob a caller can turn.

## `RenderContext`

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setLocale(Locale)` | `Locale` | `Locale.ENGLISH` | Selects the resource bundle for labels and the formatter for dates/amounts/percents. See [[Localization]]. |
| `setFormatOptions(NormaFormatOptions)` | `NormaFormatOptions` | `NormaFormatOptions.defaults()` | Per-call overrides for date pattern, number grouping, currency-symbol position. |
| `setLogo(NormaAttachment)` | `NormaAttachment` | `null` | Image to draw in the header. Used by both HTML (as `<img>` data-URI) and PDF (as `PLImage`). |
| `setLabelResolver(INormaLabelResolver)` | `INormaLabelResolver` | `DefaultNormaLabelResolver` | Replace the resource-bundle lookup with a custom one (useful for branding). |
| `setCSSClassPrefix(String)` | `String` | `"norma-"` | Prefix applied to every CSS class emitted by `HTMLInvoiceWriter`. |
| `setAnnotateBTIDs(boolean)` | `boolean` | `false` | When true, every label is suffixed with its EN 16931 ID (`Invoice number [BT-1]`). Debug aid. |
| `setIncludeEmptySections(boolean)` | `boolean` | `false` | When true, render section headings even if their content is empty. Off by default — clean output. |

## `NormaFormatOptions`

Optional fine-tuning of locale-driven formatting:

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setDateStyle(FormatStyle)` | `FormatStyle` | `FormatStyle.MEDIUM` | Date format (e.g. `MEDIUM` = `Mar 14, 2025` / `14.03.2025`). |
| `setAmountFractionDigits(Integer)` | `Integer` | `null` (use currency default) | Force a fixed number of decimal places for amounts. |
| `setPercentFractionDigits(int)` | `int` | `2` | Decimal places for VAT rates and discount percentages. |
| `setNegativeAmountStyle(ENegativeStyle)` | `ENegativeStyle` | `MINUS_SIGN` | `MINUS_SIGN` (`-1.234,56`) or `PARENTHESES` (`(1.234,56)`). |

## PDF-only options (passed to `PDFInvoiceWriter` constructor)

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setPageSize(PDRectangle)` | `org.apache.pdfbox.pdmodel.common.PDRectangle` | `PDRectangle.A4` | Page size. |
| `setMargins(MarginSpec)` | `com.helger.pdflayout.spec.MarginSpec` | 20 mm on all sides | Page margins. |
| `setBodyFont(FontSpec)` | `FontSpec` | Helvetica 10pt black | Default font for body text. |
| `setHeaderFont(FontSpec)` | `FontSpec` | Helvetica-Bold 12pt black | Default font for section headings. |

## HTML-only options (passed to `HTMLInvoiceWriter` constructor)

| Setter | Type | Default | Effect |
|--------|------|---------|--------|
| `setEmbeddedStylesheet(boolean)` | `boolean` | `true` | Embed CSS in a `<style>` block; when false, only class names are emitted (caller supplies external CSS). |
| `setCustomStylesheet(String)` | `String` | `null` | Additional CSS text to append after the default stylesheet. |
| `setEmitDoctype(boolean)` | `boolean` | `true` | When false, returns just the `<html>...</html>` subtree without `<!DOCTYPE html>` — useful for embedding. |

## Worked example

```java
final RenderContext aCtx = new RenderContext ()
    .setLocale (Locale.GERMAN)
    .setAnnotateBTIDs (false)
    .setFormatOptions (new NormaFormatOptions ()
        .setDateStyle (FormatStyle.LONG)
        .setNegativeAmountStyle (ENegativeStyle.PARENTHESES));

final PDFInvoiceWriter aPdfWriter = new PDFInvoiceWriter ()
    .setHeaderFont (new FontSpec (PreloadFont.REGULAR_BOLD, 14f));

aPdfWriter.writeTo (aInvoice, aCtx, aOutputStream);
```

## What `norma` does **not** configure

- **No system properties.** Nothing reads `System.getProperty(...)`.
- **No environment variables.** Same.
- **No `application.properties`.** `norma` does not depend on `ph-commons` config or Spring.
- **No service-loader knobs.** Readers and writers are instantiated by the caller directly.

The intent: `norma` should be safe to embed inside any host application without inheriting global state.
