# EN 16931:2026 UBL 2.5 test corpus

This directory holds the UBL 2.5 `Invoice` and `CreditNote` instances for EN 16931:2026 (BT-24 `urn:cen.eu:en16931:2026`) used by `phlox-ubl` reader tests and by HTML/PDF writer regression tests.

## Layout

```
2026/ubl/
├── README.md
├── invoice/      ← 17 invoices (13 external + 4 edge cases)
└── creditnote/   ← 5 credit notes
```

Total: 22 files.

## Provenance

All files were imported unchanged from `en16931-ubl2cii` at `../en16931-ubl2cii/en16931-ubl2cii/src/test/resources/{external/ubl25/{inv,cn},edge/ubl25}/`. The `d25a-new-*` files exercise exactly the business terms and groups that are new in EN 16931:2026 (BT-166 to BT-220, BG-33 to BG-39). The license of that project is Apache 2.0 (matching this project).

## Java lookups

| Method | Files |
|---|---|
| `getUbl25InvoiceFiles()` | 17 |
| `getUbl25CreditNoteFiles()` | 5 |
| `getAllUbl25Files()` | 22 |
