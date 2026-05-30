# EN 16931:2017 UBL 2.1 test corpus

This directory holds the curated set of UBL 2.1 `Invoice` and `CreditNote` instances used by `norma-ubl` reader tests and by HTML/PDF writer regression tests.

## Layout

```
2017/ubl/
├── README.md                                ← this file
├── invoice/
│   ├── guide-example*.xml                   ← UBL Guide examples (3 files)
│   ├── peppol-*.xml                         ← Peppol BIS examples (8 files; imported from en16931-ubl2cii)
│   ├── sample-discount-price.xml            ← discount-on-unit-price stress case
│   └── ubl-tc434-example*.xml               ← CEN/TC434 reference examples (10 files)
└── creditnote/
    ├── peppol-base-creditnote-correction.xml ← imported from en16931-ubl2cii
    └── ubl-tc434-creditnote1.xml             ← CEN/TC434 reference credit note
```

Total: 23 files (21 invoices + 2 credit notes).

## Provenance

The `peppol-*.xml` files were imported from `en16931-ubl2cii` at `../en16931-ubl2cii/en16931-ubl2cii/src/test/resources/external/ubl21/{inv,cn}/peppol/`, preserving the upstream filenames with a `peppol-` prefix to keep them distinguishable from the CEN/TC434 reference set. The license of that project is Apache 2.0 (matching this project), and the files are EN 16931-conformant UBL 2.1 instances exercising the Peppol BIS Billing 3.0 binding profile — the underlying semantics are EN 16931 itself.

The `guide-example*.xml`, `sample-discount-price.xml`, `ubl-tc434-example*.xml`, and `ubl-tc434-creditnote1.xml` files were already in `norma-testfiles` from inception and originated from the EN 16931 / CEN/TC434 publicly available example sets.

## Java lookups

Test code retrieves typed lists via `MockNormaTestFiles`:

| Method | Files |
|---|---|
| `getUBL21InvoiceFiles()` | 21 (invoice corpus) |
| `getUBL21CreditNoteFiles()` | 2 (credit-note corpus) |
| `getAllUBL21Files()` | 23 (everything) |
