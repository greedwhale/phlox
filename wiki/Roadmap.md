# Roadmap

This page tracks the planned evolution of `norma`. Items are grouped by milestone; nothing here is committed-to until it lands.

A box is checked when the headline deliverable is in the repo (code shipped, or design ratified for design-only bullets). When a bullet covers both code and tests, partial progress is noted in an indented sub-bullet rather than ticking the box prematurely.

## 0.1.0 — Foundations (EN 16931:2017)

- [x] `norma-parent` BOM with pinned `ph-commons`, `ph-ubl21`, `ph-cii-d16b`, `ph-oton-html`, `ph-pdf-layout`.
- [x] `norma-api` with the full intermediate model (every BG/BT from [[Domain Model]]), the base `RenderContext`, and the `WatermarkSpec` / `NormaProgramWatermark` types.
- [x] `norma-api` resource bundles for English and German.
- [x] `norma-testfiles` module seeded with the curated EN 16931:2017 examples for UBL and CII; folder layout per [[Maven Module Structure]].
- [ ] `norma-ubl` reader for UBL 2.1 Invoice + CreditNote with golden-file tests sourced from `norma-testfiles`.
  - reader code shipped (`UBLInvoiceReader`); golden-file regression suite pending.
- [ ] `norma-cii` reader for CII D16B with golden-file tests sourced from `norma-testfiles`.
  - reader code shipped (`CIIInvoiceReader`); shape-coverage suite shipped (`CIIInvoiceReaderTest` — one test per corpus tier, asserts every file parses and carries the EN 16931 mandatory fields BT-1/2/5/27/44, BG-22, and at least one BG-25 line; 104 files green); JSON-snapshot golden tests still pending.
- [ ] `norma-html` writer + `HTMLRenderContext` with golden-file tests in both languages. Watermarking implemented via embedded CSS (`position: fixed`, `@media print`) — see [[Watermarking]].
  - writer + CSS-based watermarking shipped (`HTMLInvoiceWriter`); golden-file tests in `en` / `de` pending.
- [ ] `norma-pdf` writer + `PDFRenderContext` with visual-regression baseline in both languages. Watermarking implemented via a `ph-pdf-layout` page decorator drawing rotated `PLText`.
  - writer + PLText-decorator watermarking shipped (`PDFInvoiceWriter`); visual-regression baseline pending.
- [ ] `norma-cli` shaded fat JAR with `--watermark` / `--watermark-opacity` / `--watermark-rotation` flags.
  - CLI scaffold shipped (`NormaCLI`); watermark flag wiring pending verification.
- [ ] CI: `mvn clean package` green on Java 17.
  - no `.github/workflows/` yet.

## 0.2.0 — Polish & coverage

- [x] Test corpus expansion: pull example files from `en16931-cii2ubl/src/test/resources/external/cii/` and equivalent UBL sources.
  - CII side: 89 files imported into `norma-testfiles/2017/cii/{xrechnung,issues}/`, exposed via `MockNormaTestFiles.getCIID16BXRechnung*Files()` / `getCIID16BTechnicalFiles()` / `getCIID16BIssueFiles()`; total CII corpus now 104 files. Provenance documented in `2017/cii/README.md`.
  - UBL side: 9 files imported from `en16931-ubl2cii/src/test/resources/external/ubl21/{inv,cn}/peppol/` under the `peppol-` prefix; exposed via the existing `getUBL21InvoiceFiles()` / `getUBL21CreditNoteFiles()` accessors. Provenance documented in `2017/ubl/README.md`.

## 0.3.0 — EN 16931:2026 support

- [x] `norma-ubl` `fromUBL25()` factory — adds the UBL 2.5 binding alongside the existing 2.1 binding (same module — see [[Source Readers]]). The semantic model (BGs/BTs) is unchanged; only the JAXB types and a handful of element-name renames differ.
- [x] `UBLInvoiceReader.autoDetect()` — sniffs `cbc:UBLVersionID` and routes to the right binding.
- [ ] Golden-file test corpus for UBL 2.5 once EN 16931:2026 example files are published.
- [ ] Reassess whether the CII binding needs to track a newer revision for EN 16931:2026 (TBD — depends on what the EN 16931:2026 CII syntax binding specifies).

## 0.4.0 — Customizable templates

- [x] **New Maven module `norma-template`** (see [[Maven Module Structure]]) — registered in the parent pom; child `pom.xml` declares JMustache + commonmark-java + GFM extensions. Compile-scope dependency on `norma-html` and `norma-pdf` is documented in the wiki; concrete `<dependency>` entries in those modules' poms still pending.
- [x] [[Template Language]] design ratified. A single `.ntpl` template renders both HTML and PDF and is composed of three off-the-shelf grammars: **JMustache** for placeholders/loops/conditionals, **commonmark-java** (+ GFM tables and strikethrough extensions) for block structure, and **ph-pdf-layout's CommonMark-compatible inline parser** so the PDF writer renders the same paragraph markup natively.
- [x] [[Template Processing]] design ratified — pipeline stages, threading, error handling, integration points all written up.
- [ ] `INormaTemplate` marker interface in `norma-api`; `AbstractRenderContext.setTemplate(INormaTemplate)` falls through to the existing hard-coded section grammar when unset, so 0.1.0 call sites keep working.
  - design documented; Java code pending.
- [ ] norma's Mustache adapter (`NormaInvoiceMustacheContext` in `norma-template`): BT/BG path resolution, format modifiers (`amount`, `date:long`, `code:doctype`, `percent`, …), `{{label:...}}` lookup routed through `INormaLabelResolver`, and the synthesized booleans (`logo`, `references_present`, `line_extras_present`).
  - design documented in [[Template Processing]]; Java code pending.
- [x] A single language-independent default template `default.ntpl` packaged as a classpath resource in `norma-template`. It reproduces the layout currently produced by the built-in writers; every literal string routes through `{{label:...}}`, so new languages need only a new `norma-labels_<lang>.properties` file in `norma-api`. Every BG section is annotated with a `{{! }}` comment block enumerating the BTs it covers.
- [ ] CommonMark post-pass for `{{class:…}}` / `{{pagebreak}}` sentinels and image-URL resolution (`data:`, `attachment:logo`, `attachment:BG-24[i]`, `classpath:`) — see [[Template Processing]] § Stage 2.
  - design documented; Java code pending.
- [ ] `HTMLTemplateEmitter` and `PDFTemplateEmitter` visitors over the commonmark-java AST in their respective writer modules; golden HTML + visual-regression PDF coverage for the default template under both `en` and `de` locales.
  - design documented (node-to-output mapping tables in [[Template Processing]] § Stage 3); Java code + golden corpus pending.
- [ ] `norma-cli` accepts `--template <path>` and resolves classpath URIs (`classpath:templates/foo.ntpl`) in addition to filesystem paths.
- [ ] Round-trip fuzz tests: read example → render HTML → render PDF through the default template → assert no exceptions for every file in the corpus. Lands here (rather than 0.2.0) because the canonical render path is template-driven; running it against the hard-coded writers would test code that is on its way out.

## 0.5.0 — Lightweight text-based output formats

- [ ] `norma-text` — plain-text renderer (handy for accessibility and email previews). Consumes the same template AST as HTML/PDF; inline styling markers are stripped.
- [ ] `norma-markdown` — GitHub-flavored Markdown renderer. Uses Markdown tables for the line and VAT-breakdown sections. Useful for diff-friendly invoices and for embedding into PR descriptions, wikis, and chat systems.
- [ ] `norma-asciidoc` — AsciiDoc renderer. Uses AsciiDoc table syntax (`|===`) so the output can be processed by Antora/Asciidoctor pipelines, included in publication toolchains, and converted to other formats downstream.

## 0.6.0 — Editable output

- [ ] `norma-docx` (stretch goal) — Word-document renderer for users who need to edit the output. Consumes the same template AST.

## Future / unscheduled

- [ ] Validation hook: optional integration with `phive-rules-en16931` so renderers can surface BR-* failures inline (off by default; opt-in via `RenderContext`).
- [ ] Side-by-side bilingual rendering (one column EN, one DE) for cross-border audits.
- [ ] EN 16931 extension support: render Peppol BIS Billing 3.0 extension fields when present in the source.
- [ ] Optional embedded XSLT-FO comparison renderer for sanity-checking the PDF layout against the EU "Common View" stylesheet.
- [ ] Additional language packs beyond `en` / `de` (e.g. `fr`, `it`, `es`, `nl`) — each new language is one `norma-labels_<lang>.properties` + one `norma-codelists_<lang>.properties` file in `norma-api`.

## What is intentionally **not** on the roadmap

- Writing/generating invoices. `norma` only reads.
- Validation of business rules — that's `phive-rules-en16931`.
- Conversion between syntaxes — that's `en16931-cii2ubl`.
- Web UI / hosted service.
