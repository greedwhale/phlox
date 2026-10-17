# News and noteworthy

## 0.1.0 — Unreleased

Initial specification.

- Project bootstrapped in specification phase — no code yet. See [[Architecture Overview]] for the agreed shape and [[Roadmap]] for the planned 0.1.0 deliverables.
- Three-stage pipeline: source readers (UBL, CII) → intermediate model (`PhloxInvoice`) → output writers (HTML, PDF).
- Initial language support: English and German.
- EN 16931 coverage: every BG (BG-1..BG-39) and every BT (BT-1..BT-220) of EN 16931:2017 and EN 16931:2026, as enumerated in [[Domain Model]]. One superset model serves both editions; `PhloxInvoice.getEdition ()` tells which one was read.
- Source syntaxes: UBL 2.1 and CII D16B (EN 16931:2017), UBL 2.5 and CII D25A (EN 16931:2026). Readers: `UblInvoiceReader.fromUbl21 ()` / `fromUbl25 ()` / `autoDetect ()` and `CiiInvoiceReader.fromD16B ()` / `fromD25A ()` / `autoDetect ()`; `autoDetect ()` takes the edition from BT-24. See [[Source Readers]].
- BT-10 (buyer reference) is a list of identifiers and BT-20 (payment terms) moved into BG-33 `PhloxPaymentTerms`, following EN 16931:2026.
- `phlox-cli`: new option `--en-version auto|2017|2026`.
- Label and code-list bundles cover the 2026 terms; German wording follows the German draft of EN 16931-1:2026 where available.
- Module split: `phlox-parent`, `phlox-api`, `phlox-ubl`, `phlox-cii`, `phlox-template`, `phlox-html`, `phlox-pdf`, `phlox-cli`, `phlox-testfiles`. See [[Maven Module Structure]].
- Configuration: split `RenderContext` (in `phlox-api`) → `HtmlRenderContext` (in `phlox-html`) / `PdfRenderContext` (in `phlox-pdf`) so each writer has type-safe options and `ph-pdf-layout`/`ph-oton-html` types never leak into `phlox-api`. See [[Configuration Properties]].
- Watermarking is part of 0.1.0 — `RenderContext.setUserWatermark(...)` for caller-defined labels (e.g. "DRAFT") and `PhloxProgramWatermark` for the distribution-edition attribution. Implemented in both HTML (via CSS) and PDF (via page decorator). See [[Watermarking]].
- Source readers perform **XSD validation** by default (carried over from `ph-ubl` / `ph-cii` marshallers) but **not Schematron** — that remains the job of `phive-rules-en16931`. Clarified in [[Source Readers]].
- New `phlox-testfiles` module hosts a curated EN 16931 example corpus, organised as `external/en16931/{2017,2026}/{ubl,cii}/...`. Pulled in with test scope by every reader/writer module.
- Roadmap additions: 0.4.0 introduces a customizable [[Template Language]] (JMustache + CommonMark) for both HTML and PDF, with the parser machinery split into a new `phlox-template` module. 0.5.0 brings lightweight text-based outputs (`phlox-text`, `phlox-markdown`, `phlox-asciidoc`); 0.6.0 brings `phlox-docx`.
