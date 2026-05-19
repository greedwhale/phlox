# Output Writers

A **writer** consumes a `NormaInvoice` plus a `RenderContext` and produces an output artifact. Writers contain all the presentation logic: layout, label lookup, locale-aware formatting.

## SPI

```java
package com.helger.norma.api.write;

public interface IInvoiceWriter <O>
{
  @Nonnull
  O write (@Nonnull NormaInvoice aInvoice, @Nonnull RenderContext aCtx);
}
```

```java
package com.helger.norma.api;

public final class RenderContext
{
  // Locale for labels and number/date formatting (default: Locale.ENGLISH)
  private Locale m_aLocale = Locale.ENGLISH;

  // Optional override of per-locale number/date formats
  private NormaFormatOptions m_aFormatOptions;

  // Optional logo to print in the header (binary + mime code)
  private NormaAttachment m_aLogo;

  // Optional CSS class prefix for HTML writer
  private String m_sCSSClassPrefix = "norma-";

  // Whether to include EN 16931 BG/BT identifiers as annotations (debug)
  private boolean m_bAnnotateBTIDs = false;
}
```

The same `RenderContext` is consumed by both the HTML writer and the PDF writer.

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

## Concrete writers

### `HTMLInvoiceWriter` (module `norma-html`)

Produces a complete HTML5 document.

```java
final HCHtml aHtml = new HTMLInvoiceWriter ().writeAsHCHtml (aInvoice, aCtx);
final String sHTML = HCRenderer.getAsHTMLString (aHtml, HCSettings.getMutableConversionSettings ());
// or
new HTMLInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
```

Built on `ph-oton-html`:

- Root element: `com.helger.html.hc.html.root.HCHtml` with `HCHead` (CSS) + `HCBody` (layout).
- Sections built from `HCDiv`, `HCTable`, `HCSpan` (from `ph-oton-html` subpackages).
- CSS: a single embedded stylesheet (`<style>` in `<head>`) with class names prefixed by `RenderContext.m_sCSSClassPrefix` (default `norma-`). Inline styles only where strictly needed.
- Rendering: `HCRenderer.getAsHTMLString(...)` for a `String`, `HCRenderer.writeHtmlTo(...)` for streaming output.

**Why HC and not raw strings:** safety (auto-escapes), reuse, no template language to learn. The same approach the Helger stack already uses.

### `PDFInvoiceWriter` (module `norma-pdf`)

Produces a PDF.

```java
new PDFInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputStream);
// or
new PDFInvoiceWriter ().writeTo (aInvoice, aCtx, aOutputFile);
```

Built on `ph-pdf-layout`:

- Top-level: `com.helger.pdflayout.PageLayoutPDF` containing one `com.helger.pdflayout.base.PLPageSet` (A4 portrait, configurable via `NormaFormatOptions`).
- Layout primitives: `PLVBox`, `PLHBox` for the section grid, `PLTable` for tabular data (invoice lines, VAT breakdown), `PLText` for everything else.
- Fonts: `com.helger.pdflayout.spec.FontSpec` with a default of Helvetica 10pt; configurable via `NormaFormatOptions`. Bold-Helvetica for headers, regular for body.
- Rendering: `PageLayoutPDF.renderTo(outputStream)` or `.renderTo(file)`.

**Page break policy:** `ph-pdf-layout` handles line-wrapping inside `PLTable` automatically; the writer adds explicit `PLPageBreak` only before the invoice-line table when the header would otherwise crowd it.

## Locale-aware formatting

Both writers route every formatted value through `NormaFormat` helpers in `norma-api`:

| Value type | Helper | Behaviour |
|------------|--------|-----------|
| `BigDecimal` amount | `NormaFormat.amount (val, currency, locale)` | Group separators, fraction digits from currency; currency symbol position from locale (e.g. `1.234,56 €` for `de-DE`, `€1,234.56` for `en-US`). |
| `BigDecimal` percentage | `NormaFormat.percent (val, locale)` | Two decimals, `%` suffix. |
| `LocalDate` | `NormaFormat.date (val, locale)` | `DateTimeFormatter.ofLocalizedDate (FormatStyle.MEDIUM)` per locale. |
| Code-list value | `NormaFormat.codeListLabel (listName, code, locale)` | Looks up a human label from a code-list resource bundle; falls back to the code itself. |

This is the only place where `Locale` enters the call graph. Models contain raw values.

## Label lookup

Both writers fetch human-readable text from a `INormaLabelResolver` whose default implementation reads a Java `ResourceBundle`. Keys are EN 16931 IDs (`BT-1`, `BG-4`, etc.) plus a small set of compound keys for sub-section titles. See [[Localization]] for the full list and the resource-bundle layout.

`RenderContext.m_bAnnotateBTIDs` (default `false`) wraps every label with its BT/BG identifier for debugging:

```
Invoice number  →  Invoice number [BT-1]
Rechnungsnummer →  Rechnungsnummer [BT-1]
```

## What writers do **not** do

- They don't fetch external resources (logos, attachments). The caller passes binary content in `RenderContext` or in BG-24 attachments embedded in the model.
- They don't recompute totals. They render BT-106 through BT-115 verbatim. Discrepancies between displayed numbers reflect discrepancies in the source.
- They don't honor `xml:lang` from the source. The output language is dictated by `RenderContext.m_aLocale`, not by the invoice payload.

## Testing strategy

For each writer:

1. Golden-file tests for HTML — diff against a checked-in fixture.
2. Visual-regression tests for PDF — render to PNG (via PDFBox), compare pixel-perfect to a checked-in PNG, and only re-baseline on intentional layout changes.
3. Round-trip smoke test — read an example file, render it in both EN and DE for both formats, assert no exceptions and non-empty output.
