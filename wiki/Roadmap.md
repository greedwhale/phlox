# Roadmap

This page tracks the planned evolution of `norma`. Items are grouped by milestone; nothing here is committed-to until it lands.

## 0.1.0 — Foundations

- [ ] `norma-parent` BOM with pinned `ph-commons`, `ph-ubl21`, `ph-cii-d16b`, `ph-oton-html`, `ph-pdf-layout`.
- [ ] `norma-api` with the full intermediate model (every BG/BT from [[Domain Model]]).
- [ ] `norma-api` resource bundles for English and German.
- [ ] `norma-ubl` reader for UBL 2.1 Invoice + CreditNote with golden-file tests.
- [ ] `norma-cii` reader for CII D16B with golden-file tests.
- [ ] `norma-html` writer with golden-file tests in both languages.
- [ ] `norma-pdf` writer with visual-regression baseline in both languages.
- [ ] `norma-cli` shaded fat JAR.
- [ ] CI: `mvn clean package` green on Java 17.

## 0.2.0 — Polish & coverage

- [ ] Test corpus expansion: pull example files from `en16931-cii2ubl/src/test/resources/external/cii/` and equivalent UBL sources.
- [ ] Round-trip fuzz tests: read example → render HTML → render PDF → assert no exceptions for every file in the corpus.
- [ ] Optional embedded XSLT-FO comparison renderer for sanity-checking the PDF layout against the EU "Common View" stylesheet.
- [ ] Logo embedding in PDF via `PLImage` + HTML via data-URI (already in `RenderContext`).
- [ ] French (`fr`) and Italian (`it`) translations.

## 0.3.0 — Additional UBL versions

- [ ] `norma-ubl22` — separate module (or merged into `norma-ubl` with an internal switch) for UBL 2.2 Invoice / CreditNote.
- [ ] `norma-ubl23`, `norma-ubl24` likewise.
- [ ] Decide whether to keep `norma-ubl` as a single multi-version module (like `UBL21Marshaller.invoice()` factory style) or split per version (like `en16931-cii2ubl`'s four converter classes).

## 0.4.0 — Optional output formats

- [ ] `norma-text` — plain-text renderer (handy for accessibility and email previews).
- [ ] `norma-docx` (stretch goal) — Word-document renderer for users who need to edit the output.

## Future / unscheduled

- [ ] Validation hook: optional integration with `phive-rules-en16931` so renderers can surface BR-* failures inline (off by default; opt-in via `RenderContext`).
- [ ] Watermarking (e.g. "PRELIMINARY") for PDFs.
- [ ] Side-by-side bilingual rendering (one column EN, one DE) for cross-border audits.
- [ ] EN 16931 extension support: render Peppol BIS Billing 3.0 extension fields when present in the source.

## What is intentionally **not** on the roadmap

- Writing/generating invoices. `norma` only reads.
- Validation of business rules — that's `phive-rules-en16931`.
- Conversion between syntaxes — that's `en16931-cii2ubl`.
- Web UI / hosted service.
