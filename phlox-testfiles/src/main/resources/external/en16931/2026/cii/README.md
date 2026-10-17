# EN 16931:2026 CII D25A test corpus

This directory holds the UN/CEFACT CII D25A instances for EN 16931:2026 (BT-24 `urn:cen.eu:en16931:2026`) used by `phlox-cii` reader tests and by HTML/PDF writer regression tests.

All 16 files (invoices and credit notes) sit flat in this directory.

## Provenance

All files were imported unchanged from `en16931-cii2ubl` at `../en16931-cii2ubl/en16931-cii2ubl/src/test/resources/external/cii-d25a/` (identical to the copy in `en16931-ubl2cii`). The `d25a-new-*` files exercise exactly the business terms and groups that are new in EN 16931:2026. The license of that project is Apache 2.0 (matching this project).

## Java lookups

| Method | Files |
|---|---|
| `getCiiD25AFiles()` | 16 |
