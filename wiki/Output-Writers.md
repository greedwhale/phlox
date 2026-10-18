# Output Writers

A **writer** consumes a `PhloxInvoice` plus an output-typed `RenderContext` subclass and produces an output artifact. Writers contain all the presentation logic: layout, label lookup, locale-aware formatting.

## SPI

```java
package com.helger.phlox.api.write;

public interface IInvoiceWriter <O, CTX extends RenderContext>
{
  @Nonnull
  O write (@Nonnull PhloxInvoice aInvoice, @Nonnull CTX aCtx);
}
```

Each writer binds its own `CTX` type so the compiler rejects accidental mixups (e.g. passing a `PdfRenderContext` to the HTML writer).

The base `RenderContext` lives in `phlox-api` and carries only the syntax-neutral knobs (locale, label resolver, logo, format options, annotate-IDs, and an optional `IPhloxTemplate` for layout customization — see [[Template Language]]). Output-specific extras live on subclasses defined alongside the writer: `HtmlRenderContext` in `phlox-html`, `PdfRenderContext` in `phlox-pdf`. See [[Configuration Properties]] for the full setter inventory.

## Shared layout grammar

Both writers organize the output into the same logical sections so that an HTML and a PDF rendering of the same invoice look like cousins:

```
┌─────────────────────────────────────────────────┐
│  HEADER                                         │
│   ┌─ Logo (if RenderContext.aLogo)              │
│   ├─ Document title + BT-3 type description     │
│   ├─ BT-1 Invoice number, BT-2 issue date       │
│   └─ BT-23/BT-24 process control                │
├─────────────────────────────────────────────────┤
│  PARTIES (two columns)                          │
│   ┌─ Seller (BG-4 + BG-5 + BG-6)                │
│   └─ Buyer (BG-7 + BG-8 + BG-9)                 │
├─────────────────────────────────────────────────┤
│  OPTIONAL PARTIES (one row each, only if set)   │
│   ┌─ Payee (BG-10)                              │
│   ├─ Seller tax representative (BG-11 + BG-12)  │
│   └─ Delivery info (BG-13 + BG-15)              │
├─────────────────────────────────────────────────┤
│  REFERENCES (only those populated)              │
│   BT-10..BT-19, BG-3 preceding invoices         │
├─────────────────────────────────────────────────┤
│  INVOICE NOTES (BG-1, 0..n)                     │
├─────────────────────────────────────────────────┤
│  LINES (BG-25)                                  │
│   Table: BT-126 │ BT-153 │ BT-129 BT-130 │      │
│          BT-146 │ BT-131 │ VAT (BG-30)          │
│   With expandable details: line notes,          │
│   allowances, charges, item attrs, period       │
├─────────────────────────────────────────────────┤
│  ALLOWANCES & CHARGES (BG-20, BG-21)            │
├─────────────────────────────────────────────────┤
│  VAT BREAKDOWN (BG-23)                          │
│   Table: BT-118 │ BT-116 │ BT-119 │ BT-117      │
├─────────────────────────────────────────────────┤
│  DOCUMENT TOTALS (BG-22)                        │
│   Right-aligned ladder ending at BT-115         │
├─────────────────────────────────────────────────┤
│  PAYMENT INSTRUCTIONS (BG-16 incl. BG-17..19)   │
│  PAYMENT TERMS (BT-20)                          │
├─────────────────────────────────────────────────┤
│  SUPPORTING DOCUMENTS (BG-24)                   │
│   List with optional links / attachments        │
└─────────────────────────────────────────────────┘
```

Each section is implemented in a `*SectionRenderer` class so the HTML and PDF writers can share the structural decisions ("skip section if empty", "two-column when both populated") and diverge only on the actual markup/layout calls.

This grammar is the **default** layout, used whenever `RenderContext.getTemplate() == null`. A caller who needs a different structure supplies a `.ptpl` via `setTemplate(...)` and the writers walk that instead — see [[Template Language]].

## Concrete writers

### `HtmlInvoiceWriter` (module `phlox-html`)

`implements IInvoiceWriter <String, HtmlRenderContext>`. Produces a complete HTML5 document.

```java
final HtmlRenderContext aCtx = new HtmlRenderContext ().setLocale (Locale.GERMAN);
final HCHtml aHtml = new HtmlInvoiceWriter ().writeAsHcHtml (aInvoice, aCtx);
final String sHTML = HCRenderer.getAsHTMLString (aHtml, HCSettings.getMutableConversionSettings ());
// or
new HtmlInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
```

Built on `ph-oton-html`:

- Root element: `com.helger.html.hc.html.root.HCHtml` with `HCHead` (CSS) + `HCBody` (layout).
- Sections built from `HCDiv`, `HCTable`, `HCSpan` (from `ph-oton-html` subpackages).
- CSS: a single embedded stylesheet (`<style>` in `<head>`) with class names prefixed by `HtmlRenderContext.m_sCssClassPrefix` (default `phlox-`). Inline styles only where strictly needed. Driven by `setEmbeddedStylesheet(...)` / `setCustomStylesheet(...)` on the context.
- Rendering: `HCRenderer.getAsHTMLString(...)` for a `String`, `HCRenderer.writeHtmlTo(...)` for streaming output.

**Why HC and not raw strings:** safety (auto-escapes), reuse, no template language to learn. The same approach the Helger stack already uses.

### `PdfInvoiceWriter` (module `phlox-pdf`)

`implements IInvoiceWriter <Void, PdfRenderContext>`. Produces a PDF.

```java
final PdfRenderContext aCtx = new PdfRenderContext ().setLocale (Locale.GERMAN);
new PdfInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
// or
new PdfInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputFile);
```

Built on `ph-pdf-layout`:

- Top-level: `com.helger.pdflayout.PageLayoutPDF` containing one `com.helger.pdflayout.base.PLPageSet`. Page size and margins come from `PdfRenderContext.setPageSize(...)` / `.setMargins(...)` (default A4 portrait, 20 mm margins).
- Layout primitives: `PLVBox`, `PLHBox` for the section grid, `PLTable` for tabular data (invoice lines, VAT breakdown), `PLText` for everything else.
- Fonts: `com.helger.pdflayout.spec.FontSpec` instances on `PdfRenderContext` (`setBodyFont`, `setHeaderFont`, `setTableHeaderFont`). Defaults: Helvetica 10pt body, Helvetica-Bold 12pt section headings.
- Rendering: `PageLayoutPDF.renderTo(outputStream)` or `.renderTo(file)`.

**Page break policy:** `ph-pdf-layout` handles line-wrapping inside `PLTable` automatically; the writer adds explicit `PLPageBreak` only before the invoice-line table when the header would otherwise crowd it.

## Locale-aware formatting

Both writers route every formatted value through `PhloxFormat` helpers in `phlox-api`:

| Value type | Helper | Behaviour |
|------------|--------|-----------|
| `BigDecimal` amount | `PhloxFormat.amount (val, currency, locale)` | Group separators, fraction digits from currency; currency symbol position from locale (e.g. `1.234,56 €` for `de-DE`, `€1,234.56` for `en-US`). |
| `BigDecimal` percentage | `PhloxFormat.percent (val, locale)` | Two decimals, `%` suffix. |
| `LocalDate` | `PhloxFormat.date (val, locale)` | `DateTimeFormatter.ofLocalizedDate (FormatStyle.MEDIUM)` per locale. |
| Code-list value | `PhloxFormat.codeListLabel (listName, code, locale)` | Looks up a human label from a code-list resource bundle; falls back to the code itself. |

This is the only place where `Locale` enters the call graph. Models contain raw values.

## Label lookup

Both writers fetch human-readable text from a `IPhloxLabelResolver` whose default implementation reads a Java `ResourceBundle`. Keys are EN 16931 IDs (`BT-1`, `BG-4`, etc.) plus a small set of compound keys for sub-section titles. See [[Localization]] for the full list and the resource-bundle layout.

`RenderContext.m_bAnnotateBtIDs` (default `false`) wraps every label with its BT/BG identifier for debugging:

```
Invoice number  →  Invoice number [BT-1]
Rechnungsnummer →  Rechnungsnummer [BT-1]
```

## Watermarks

Both writers honour `RenderContext.setUserWatermark(...)` and the static `PhloxProgramWatermark`. In HTML this is implemented via embedded CSS (`position: fixed`, `@media print`); in PDF via a `ph-pdf-layout` page decorator that draws rotated `PLText` over every page. Full design in [[Watermarking]].

## What writers do **not** do

- They don't fetch external resources (logos, attachments). The caller passes binary content in `RenderContext` or in BG-24 attachments embedded in the model.
- They don't recompute totals. They render BT-106 through BT-115 verbatim. Discrepancies between displayed numbers reflect discrepancies in the source.
- They don't honor `xml:lang` from the source. The output language is dictated by `RenderContext.m_aLocale`, not by the invoice payload.

## Template mode

Both writers have a second, template-driven path. When `RenderContext.getTemplate ()` is set, `HtmlInvoiceWriter` hands the rendered template (`PhloxTemplate.renderAst`) to `HtmlTemplateEmitter`, and `PdfInvoiceWriter` hands it to `PdfTemplateEmitter`; the section methods above are not used. The head, stylesheet and watermarks of the HTML page and the page size, margins, fonts and watermark of the PDF are the same in both modes. See [[Template Language]] and [[Template Processing]]. The CLI switches to this mode with `--template`.

## Testing strategy

What exists today: for both writers a corpus smoke test (parse every example file, render it in EN and DE, assert a non-empty result with the right invoice number, and write the output to `generated/`), content assertions for the 2026 terms, and the same for the template path. The PDF template tests also read the PDF back with PDFBox (text, page count, fonts, stroke operators).

What is still planned:

1. Golden-file tests for HTML — diff against a checked-in fixture.
2. Visual-regression tests for PDF — render to PNG (via PDFBox), compare pixel-perfect to a checked-in PNG, and only re-baseline on intentional layout changes.
