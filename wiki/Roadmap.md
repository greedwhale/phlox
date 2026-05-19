# Roadmap

This page tracks the planned evolution of `norma`. Items are grouped by milestone; nothing here is committed-to until it lands.

## 0.1.0 — Foundations (EN 16931:2017)

- [ ] `norma-parent` BOM with pinned `ph-commons`, `ph-ubl21`, `ph-cii-d16b`, `ph-oton-html`, `ph-pdf-layout`.
- [ ] `norma-api` with the full intermediate model (every BG/BT from [[Domain Model]]), the base `RenderContext`, and the `WatermarkSpec` / `NormaProgramWatermark` types.
- [ ] `norma-api` resource bundles for English and German.
- [ ] `norma-testfiles` module seeded with the curated EN 16931:2017 examples for UBL and CII; folder layout per [[Maven Module Structure]].
- [ ] `norma-ubl` reader for UBL 2.1 Invoice + CreditNote with golden-file tests sourced from `norma-testfiles`.
- [ ] `norma-cii` reader for CII D16B with golden-file tests sourced from `norma-testfiles`.
- [ ] `norma-html` writer + `HTMLRenderContext` with golden-file tests in both languages. Watermarking implemented via embedded CSS (`position: fixed`, `@media print`) — see [[Watermarking]].
- [ ] `norma-pdf` writer + `PDFRenderContext` with visual-regression baseline in both languages. Watermarking implemented via a `ph-pdf-layout` page decorator drawing rotated `PLText`.
- [ ] `norma-cli` shaded fat JAR with `--watermark` / `--watermark-opacity` / `--watermark-rotation` flags.
- [ ] CI: `mvn clean package` green on Java 17.

## 0.2.0 — Polish & coverage

- [ ] Test corpus expansion: pull example files from `en16931-cii2ubl/src/test/resources/external/cii/` and equivalent UBL sources.
- [ ] Round-trip fuzz tests: read example → render HTML → render PDF → assert no exceptions for every file in the corpus.
- [ ] Optional embedded XSLT-FO comparison renderer for sanity-checking the PDF layout against the EU "Common View" stylesheet.
- [ ] Logo embedding in PDF via `PLImage` + HTML via data-URI (already in `RenderContext`).
- [ ] French (`fr`) and Italian (`it`) translations.

## 0.3.0 — EN 16931:2026 support

- [ ] `norma-ubl` `fromUBL25()` factory — adds the UBL 2.5 binding alongside the existing 2.1 binding (same module — see [[Source Readers]]). The semantic model (BGs/BTs) is unchanged; only the JAXB types and a handful of element-name renames differ.
- [ ] `UBLInvoiceReader.autoDetect()` — sniffs `cbc:UBLVersionID` and routes to the right binding.
- [ ] Golden-file test corpus for UBL 2.5 once EN 16931:2026 example files are published.
- [ ] Reassess whether the CII binding needs to track a newer revision for EN 16931:2026 (TBD — depends on what the EN 16931:2026 CII syntax binding specifies).

## 0.4.0 — Lightweight text-based output formats

- [ ] `norma-text` — plain-text renderer (handy for accessibility and email previews).
- [ ] `norma-markdown` — GitHub-flavored Markdown renderer. Shares the same section grammar as HTML/PDF (header → parties → references → lines → totals → payment), using Markdown tables for the line and VAT-breakdown sections. Useful for diff-friendly invoices and for embedding into PR descriptions, wikis, and chat systems.
- [ ] `norma-asciidoc` — AsciiDoc renderer. Same section grammar; uses AsciiDoc table syntax (`|===`) so the output can be processed by Antora/Asciidoctor pipelines, included in publication toolchains, and converted to other formats downstream.

## 0.5.0 — Editable output

- [ ] `norma-docx` (stretch goal) — Word-document renderer for users who need to edit the output.

## Future / unscheduled

- [ ] Validation hook: optional integration with `phive-rules-en16931` so renderers can surface BR-* failures inline (off by default; opt-in via `RenderContext`).
- [ ] Side-by-side bilingual rendering (one column EN, one DE) for cross-border audits.
- [ ] EN 16931 extension support: render Peppol BIS Billing 3.0 extension fields when present in the source.

## What is intentionally **not** on the roadmap

- Writing/generating invoices. `norma` only reads.
- Validation of business rules — that's `phive-rules-en16931`.
- Conversion between syntaxes — that's `en16931-cii2ubl`.
- Web UI / hosted service.
