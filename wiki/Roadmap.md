# Roadmap

This page tracks the planned evolution of `phlox`. Items are grouped by milestone; nothing here is committed-to until it lands.

A box is checked when the headline deliverable is in the repo (code shipped, or design ratified for design-only bullets). When a bullet covers both code and tests, partial progress is noted in an indented sub-bullet rather than ticking the box prematurely.

## 0.1.0 — Foundations (EN 16931:2017)

- [x] `phlox-parent` BOM with pinned `ph-commons`, `ph-ubl21`, `ph-cii-d16b`, `ph-oton-html`, `ph-pdf-layout`.
- [x] `phlox-api` with the full intermediate model (every BG/BT from [[Domain Model]]), the base `RenderContext`, and the `WatermarkSpec` / `PhloxProgramWatermark` types.
- [x] `phlox-api` resource bundles for English and German.
- [x] `phlox-testfiles` module seeded with the curated EN 16931:2017 examples for UBL and CII; folder layout per [[Maven Module Structure]].
- [ ] `phlox-ubl` reader for UBL 2.1 Invoice + CreditNote with golden-file tests sourced from `phlox-testfiles`.
  - reader code shipped (`UblInvoiceReader`); golden-file regression suite pending.
- [ ] `phlox-cii` reader for CII D16B with golden-file tests sourced from `phlox-testfiles`.
  - reader code shipped (`CiiInvoiceReader`); shape-coverage suite shipped (`CiiInvoiceReaderTest` — one test per corpus tier, asserts every file parses and carries the EN 16931 mandatory fields BT-1/2/5/27/44, BG-22, and at least one BG-25 line; 104 files green); JSON-snapshot golden tests still pending.
- [ ] `phlox-html` writer + `HtmlRenderContext` with golden-file tests in both languages. Watermarking implemented via embedded CSS (`position: fixed`, `@media print`) — see [[Watermarking]].
  - writer + CSS-based watermarking shipped (`HtmlInvoiceWriter`); golden-file tests in `en` / `de` pending.
- [ ] `phlox-pdf` writer + `PdfRenderContext` with visual-regression baseline in both languages. Watermarking implemented via a `ph-pdf-layout` page decorator drawing rotated `PLText`.
  - writer + PLText-decorator watermarking shipped (`PdfInvoiceWriter`); visual-regression baseline pending.
- [ ] `phlox-cli` shaded fat JAR with `--watermark` / `--watermark-opacity` / `--watermark-rotation` flags.
  - CLI scaffold shipped (`PhloxCli`); watermark flag wiring pending verification.
- [ ] CI: `mvn clean package` green on Java 17.
  - no `.github/workflows/` yet.

## 0.2.0 — Polish & coverage

- [x] Test corpus expansion: pull example files from `en16931-cii2ubl/src/test/resources/external/cii/` and equivalent UBL sources.
  - CII side: 89 files imported into `phlox-testfiles/2017/cii/{xrechnung,issues}/`, exposed via `MockPhloxTestFiles.getCiiD16BXRechnung*Files()` / `getCiiD16BTechnicalFiles()` / `getCiiD16BIssueFiles()`; total CII corpus now 104 files. Provenance documented in `2017/cii/README.md`.
  - UBL side: 9 files imported from `en16931-ubl2cii/src/test/resources/external/ubl21/{inv,cn}/peppol/` under the `peppol-` prefix; exposed via the existing `getUbl21InvoiceFiles()` / `getUbl21CreditNoteFiles()` accessors. Provenance documented in `2017/ubl/README.md`.

## 0.3.0 — EN 16931:2026 support

- [x] `phlox-ubl` `fromUbl25()` factory — adds the UBL 2.5 binding alongside the UBL 2.1 binding (same module, one reader per edition — see [[Source Readers]]).
- [x] `UblInvoiceReader.autoDetect()` — determines the edition from BT-24 (`EEN16931Edition` of `en16931-basics`) and fails with a clear error when BT-24 is missing.
- [x] `phlox-cii` D25A reader (`CiiInvoiceReader.fromD25A()`, `autoDetect()`) — EN 16931:2026 uses CII D25A.
- [x] Superset intermediate model: BG-33 to BG-39 and BT-166 to BT-220 (see [[Domain Model]]).
- [x] 2026 test corpus in `phlox-testfiles` (`2026/ubl`: 22 files, `2026/cii`: 16 files), imported from `en16931-ubl2cii` / `en16931-cii2ubl`.
- [x] Labels (EN, DE) and code lists for the 2026 terms.
- [x] HTML and PDF writers and the default template render the 2026 terms; `phlox-cli --en-version`.
- [ ] Replace the imported corpus by the official EN 16931:2026 example files once they are published.
- [ ] German wording for BG-39, BT-215 to BT-220, BT-167-1/-2, BT-177-1 and BT-193-1: the German draft of EN 16931-1 does not contain them yet (English text is used meanwhile).

## 0.4.0 — Customizable templates

- [x] **New Maven module `phlox-template`** (see [[Maven Module Structure]]) — registered in the parent pom; child `pom.xml` declares JMustache + commonmark-java + GFM extensions. Compile-scope dependency on `phlox-html` and `phlox-pdf` is documented in the wiki; concrete `<dependency>` entries in those modules' poms still pending.
- [x] [[Template Language]] design ratified. A single `.ptpl` template renders both HTML and PDF and is composed of three off-the-shelf grammars: **JMustache** for placeholders/loops/conditionals, **commonmark-java** (+ GFM tables and strikethrough extensions) for block structure, and **ph-pdf-layout's CommonMark-compatible inline parser** so the PDF writer renders the same paragraph markup natively.
- [x] [[Template Processing]] design ratified — pipeline stages, threading, error handling, integration points all written up.
- [ ] `IPhloxTemplate` marker interface in `phlox-api`; `AbstractRenderContext.setTemplate(IPhloxTemplate)` falls through to the existing hard-coded section grammar when unset, so 0.1.0 call sites keep working.
  - design documented; Java code pending.
- [ ] phlox's Mustache adapter (`PhloxInvoiceMustacheContext` in `phlox-template`): BT/BG path resolution, format modifiers (`amount`, `date:long`, `code:doctype`, `percent`, …), `{{label:...}}` lookup routed through `IPhloxLabelResolver`, and the synthesized booleans (`logo`, `references_present`, `line_extras_present`).
  - design documented in [[Template Processing]]; Java code pending.
- [x] A single language-independent default template `default.ptpl` packaged as a classpath resource in `phlox-template`. It reproduces the layout currently produced by the built-in writers; every literal string routes through `{{label:...}}`, so new languages need only a new `phlox-labels_<lang>.properties` file in `phlox-api`. Every BG section is annotated with a `{{! }}` comment block enumerating the BTs it covers.
- [ ] CommonMark post-pass for `{{class:…}}` / `{{pagebreak}}` sentinels and image-URL resolution (`data:`, `attachment:logo`, `attachment:BG-24[i]`, `classpath:`) — see [[Template Processing]] § Stage 2.
  - design documented; Java code pending.
- [ ] `HTMLTemplateEmitter` and `PDFTemplateEmitter` visitors over the commonmark-java AST in their respective writer modules; golden HTML + visual-regression PDF coverage for the default template under both `en` and `de` locales.
  - design documented (node-to-output mapping tables in [[Template Processing]] § Stage 3); Java code + golden corpus pending.
- [ ] `phlox-cli` accepts `--template <path>` and resolves classpath URIs (`classpath:templates/foo.ptpl`) in addition to filesystem paths.
- [ ] Round-trip fuzz tests: read example → render HTML → render PDF through the default template → assert no exceptions for every file in the corpus. Lands here (rather than 0.2.0) because the canonical render path is template-driven; running it against the hard-coded writers would test code that is on its way out.

## 0.5.0 — Lightweight text-based output formats

- [ ] `phlox-text` — plain-text renderer (handy for accessibility and email previews). Consumes the same template AST as HTML/PDF; inline styling markers are stripped.
- [ ] `phlox-markdown` — GitHub-flavored Markdown renderer. Uses Markdown tables for the line and VAT-breakdown sections. Useful for diff-friendly invoices and for embedding into PR descriptions, wikis, and chat systems.
- [ ] `phlox-asciidoc` — AsciiDoc renderer. Uses AsciiDoc table syntax (`|===`) so the output can be processed by Antora/Asciidoctor pipelines, included in publication toolchains, and converted to other formats downstream.

## Future / unscheduled

- [ ] Validation hook: optional integration with `phive-rules-en16931` so renderers can surface BR-* failures inline (off by default; opt-in via `RenderContext`).
- [ ] Side-by-side bilingual rendering (one column EN, one DE) for cross-border audits.
- [ ] EN 16931 extension support: render Peppol BIS Billing 3.0 extension fields when present in the source.
- [ ] Optional embedded XSLT-FO comparison renderer for sanity-checking the PDF layout against the EU "Common View" stylesheet.
- [ ] Additional language packs beyond `en` / `de` (e.g. `fr`, `it`, `es`, `nl`) — each new language is one `phlox-labels_<lang>.properties` + one `phlox-codelists_<lang>.properties` file in `phlox-api`.
- [ ] `phlox-docx` (stretch goal) — Word-document renderer for users who need to edit the output. Consumes the same template AST.

## What is intentionally **not** on the roadmap

- Writing/generating invoices. `phlox` only reads.
- Validation of business rules — that's `phive-rules-en16931`.
- Conversion between syntaxes — that's `en16931-cii2ubl`.
- Web UI / hosted service.
