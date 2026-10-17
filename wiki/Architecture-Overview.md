# Architecture Overview

## Goal

`phlox` turns a structured electronic invoice (UBL or CII XML) into a human-readable rendering (HTML or PDF) in the reader's preferred language. The library is **read-only** with respect to the source — it never modifies the input.

## Three-stage pipeline

```
   Source XML                Intermediate model              Output
  ┌──────────┐  Reader     ┌────────────────────┐  Writer  ┌──────────┐
  │  UBL /   │ ─────────▶  │   PhloxInvoice     │ ───────▶ │ HTML /   │
  │  CII XML │             │   (phlox-api)      │          │ PDF      │
  └──────────┘             └────────────────────┘          └──────────┘
                                    │
                                    │ uses
                                    ▼
                            ┌────────────────┐
                            │ Localization   │
                            │ (EN/DE labels) │
                            └────────────────┘
```

The boundary in the middle is **the intermediate model** (`PhloxInvoice`). It is a syntax-neutral, in-memory representation of an EN 16931 invoice with every BG/BT slot present, regardless of whether it was populated from the source.

### Stage 1 — Read

A `IInvoiceReader<T>` implementation parses a source XML stream into the corresponding JAXB type (via `ph-ubl` or `ph-cii`), then walks that JAXB tree and populates an `PhloxInvoice`. Each reader is **lossy** in one direction: it discards everything outside the EN 16931 model (custom extensions, syntax-specific noise). It is **complete** in the other direction: every EN 16931 field present in the source ends up in the model.

### Stage 2 — Hold

The `PhloxInvoice` is a plain Java bean tree. It has no XML, no formatting, no localization, no rendering hooks. It can be inspected, serialized to JSON for diagnostics, snapshotted in tests, or fed to multiple writers in turn.

### Stage 3 — Write

A `IInvoiceWriter<O>` implementation consumes the `PhloxInvoice` plus a `RenderContext` (locale, options, optional template) and produces the output. The writer is the only place that knows about:

- **Layout** (sections, tables, grids). Either the hard-coded section grammar (default) or a user-supplied `.ptpl` template applied via `RenderContext.setTemplate(...)` — see [[Template Language]].
- **Labels** (the human text for each BG/BT, looked up in resource bundles).
- **Formatting** (dates, numbers, currencies — all locale-driven).

## Why this shape

| Decision | Rationale |
|----------|-----------|
| Intermediate model in `phlox-api` | Adding a new source (e.g. UBL 2.4) means one new module; adding a new format (e.g. text/plain) means one new module. No cross-product. |
| Model carries **all** BGs/BTs | Writers don't need fallback logic for missing slots — they always see a uniform shape; they only check for `null` / empty collections. |
| Locale held in `RenderContext`, not the model | A single parsed invoice can be rendered in EN and DE without re-reading. |
| `RenderContext` split into `HtmlRenderContext` / `PdfRenderContext` subclasses | Format-specific knobs (PDF fonts, HTML stylesheets) stay in their own modules and out of `phlox-api`. Writer signatures are type-safe — the compiler rejects passing a PDF context to the HTML writer. |
| No business-rule validation in `phlox` | Separation of concerns. `phive-rules-en16931` already does this and is heavy; visualization should not pay that cost. |
| Programmatic HTML/PDF (not XSLT) | Type safety, reuse of layout primitives, easier to test. Matches the rest of the Helger stack (`ph-oton-html`, `ph-pdf-layout`). |

## Dependency-direction rules

```
                       ┌─────────────┐
                       │ phlox-api   │  ← intermediate model + reader/writer/template SPIs + I18N
                       └──────┬──────┘
                              │
       ┌──────────────────────┼─────────────────────┬──────────────────┐
       │                      │                     │                  │
  ┌────▼─────┐         ┌──────▼─────┐       ┌───────▼──────┐    ┌──────┴──────┐
  │phlox-ubl │         │ phlox-cii  │       │phlox-template│    │ phlox-html /│
  └────┬─────┘         └──────┬─────┘       └──────┬───────┘    │  phlox-pdf  │
       │                      │                    │            └──────┬──────┘
       │                      │                    └─── compile ──────▶│
       │                      │                                        │
       └──────────────────────┴───────────┬────────────────────────────┘
                                          │
                                     ┌────▼────┐
                                     │phlox-cli│  ← wires readers + writers
                                     └─────────┘
```

- Readers depend only on `phlox-api` and their source library (`ph-ubl21` or `ph-cii-d16b`).
- Writers depend on `phlox-api`, `phlox-template`, and their output library (`ph-oton-html` or `ph-pdf-layout`).
- `phlox-template` depends only on `phlox-api` and the parser jars (JMustache + commonmark-java + GFM extensions).
- Readers and writers **never** depend on each other.
- The CLI is the only module that depends on multiple readers and writers at once.

See [[Maven Module Structure]] for module-by-module details.

## What a typical caller does

```java
// 1. Read
final IInvoiceReader <InputStream> aReader = UblInvoiceReader.fromUbl21 ();
final PhloxInvoice aModel = aReader.read (aXMLStream, aErrors);

// 2. Configure rendering — typed per output format
final HtmlRenderContext aHtmlCtx = new HtmlRenderContext ().setLocale (Locale.GERMAN);
final PdfRenderContext  aPdfCtx  = PdfRenderContext.copyFrom (aHtmlCtx);

// 3. Write (one or more outputs from the same model)
final String sHTML = new HtmlInvoiceWriter ().writeAsString (aModel, aHtmlCtx);
new PdfInvoiceWriter ().writeTo (aModel, aPdfCtx, aOutputStream);
```

Each step is independent. A caller that only needs HTML never pulls `ph-pdf-layout` (or `PdfRenderContext`) onto its classpath, because `phlox-pdf` is in a separate module.

Both writers honour `RenderContext.setTemplate(IPhloxTemplate)` for custom layouts. When unset, each writer uses its built-in section grammar from [[Output Writers]]; when set, both walk the same parsed template AST. See [[Template Language]] for the grammar and [[Template Processing]] for the pipeline.
