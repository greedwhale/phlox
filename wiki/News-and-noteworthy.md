# News and noteworthy

## 0.1.0 — Unreleased

Initial specification.

- Project bootstrapped in specification phase — no code yet. See [[Architecture Overview]] for the agreed shape and [[Roadmap]] for the planned 0.1.0 deliverables.
- Three-stage pipeline: source readers (UBL, CII) → intermediate model (`NormaInvoice`) → output writers (HTML, PDF).
- Initial language support: English and German.
- EN 16931 coverage: every BG (BG-1..BG-32) and every BT (BT-1..BT-165), as enumerated in [[Domain Model]].
- UBL bindings: 0.1.0 ships UBL 2.1 (EN 16931:2017); UBL 2.5 (EN 16931:2026) lands in 0.3.0, sharing the same `norma-ubl` module since EN 16931's semantic model does not change between editions.
- Module split: `norma-parent`, `norma-api`, `norma-ubl`, `norma-cii`, `norma-html`, `norma-pdf`, `norma-cli`. See [[Maven Module Structure]].
- Configuration: split `RenderContext` (in `norma-api`) → `HTMLRenderContext` (in `norma-html`) / `PDFRenderContext` (in `norma-pdf`) so each writer has type-safe options and `ph-pdf-layout`/`ph-oton-html` types never leak into `norma-api`. See [[Configuration Properties]].
