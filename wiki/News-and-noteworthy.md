# News and noteworthy

## 0.1.0 — Unreleased

Initial specification.

- Project bootstrapped in specification phase — no code yet. See [[Architecture Overview]] for the agreed shape and [[Roadmap]] for the planned 0.1.0 deliverables.
- Three-stage pipeline: source readers (UBL, CII) → intermediate model (`NormaInvoice`) → output writers (HTML, PDF).
- Initial language support: English and German.
- EN 16931 coverage: every BG (BG-1..BG-32) and every BT (BT-1..BT-165), as enumerated in [[Domain Model]].
- Module split: `norma-parent`, `norma-api`, `norma-ubl`, `norma-cii`, `norma-html`, `norma-pdf`, `norma-cli`. See [[Maven Module Structure]].
