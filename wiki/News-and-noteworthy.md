# News and noteworthy

## 0.1.0 — Unreleased

Initial specification.

- Project bootstrapped in specification phase — no code yet. See [[Architecture Overview]] for the agreed shape and [[Roadmap]] for the planned 0.1.0 deliverables.
- Three-stage pipeline: source readers (UBL, CII) → intermediate model (`PhloxInvoice`) → output writers (HTML, PDF).
- Initial language support: English and German.
- EN 16931 coverage: every BG (BG-1..BG-32) and every BT (BT-1..BT-165), as enumerated in [[Domain Model]].
- UBL bindings: 0.1.0 ships UBL 2.1 (EN 16931:2017); UBL 2.5 (EN 16931:2026) lands in 0.3.0, sharing the same `phlox-ubl` module since EN 16931's semantic model does not change between editions.
- Module split: `phlox-parent`, `phlox-api`, `phlox-ubl`, `phlox-cii`, `phlox-template`, `phlox-html`, `phlox-pdf`, `phlox-cli`, `phlox-testfiles`. See [[Maven Module Structure]].
- Configuration: split `RenderContext` (in `phlox-api`) → `HtmlRenderContext` (in `phlox-html`) / `PdfRenderContext` (in `phlox-pdf`) so each writer has type-safe options and `ph-pdf-layout`/`ph-oton-html` types never leak into `phlox-api`. See [[Configuration Properties]].
- Watermarking is part of 0.1.0 — `RenderContext.setUserWatermark(...)` for caller-defined labels (e.g. "DRAFT") and `PhloxProgramWatermark` for the distribution-edition attribution. Implemented in both HTML (via CSS) and PDF (via page decorator). See [[Watermarking]].
- Source readers perform **XSD validation** by default (carried over from `ph-ubl` / `ph-cii` marshallers) but **not Schematron** — that remains the job of `phive-rules-en16931`. Clarified in [[Source Readers]].
- New `phlox-testfiles` module hosts a curated EN 16931 example corpus, organised as `external/en16931/{2017,2026}/{ubl,cii}/...`. Pulled in with test scope by every reader/writer module.
- Roadmap additions: 0.4.0 introduces a customizable [[Template Language]] (JMustache + CommonMark) for both HTML and PDF, with the parser machinery split into a new `phlox-template` module. 0.5.0 brings lightweight text-based outputs (`phlox-text`, `phlox-markdown`, `phlox-asciidoc`); 0.6.0 brings `phlox-docx`.
