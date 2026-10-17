# Source Readers

A **reader** parses an electronic invoice in one of the supported syntaxes and populates a [[Domain Model|Domain-Model]] `PhloxInvoice`. Readers do not validate business rules; they only translate the source structure into the syntax-neutral intermediate model.

## SPI

```java
package com.helger.phlox.api.read;

public interface IInvoiceReader <SRC>
{
  @Nullable
  PhloxInvoice read (@Nonnull SRC aSource, @Nonnull ErrorList aErrors);
}
```

- Returns `null` when parsing failed catastrophically (malformed XML, wrong root element).
- Adds entries to `aErrors` for recoverable problems (unknown enum values, unrecognized formats) but still returns a model if anything could be salvaged.
- Caller treats the result as **successful** only if both: result is non-null AND `aErrors.containsAtLeastOneError()` is `false`. Same rule as `en16931-cii2ubl`.

## Concrete readers

### `UblInvoiceReader` (module `phlox-ubl`)

Reads UBL `Invoice` and `CreditNote` documents. The module supports **the two UBL versions that EN 16931 has a syntax binding for**:

| UBL version | EN 16931 edition | ph-ubl artifact |
|-------------|------------------|-----------------|
| **UBL 2.1** | EN 16931:2017 (current) | `com.helger.ubl:ph-ubl21` |
| **UBL 2.5** | EN 16931:2026 (upcoming) | `com.helger.ubl:ph-ubl25` |

No other UBL versions are in scope — EN 16931 only specifies bindings for these two, so anything else (2.2 / 2.3 / 2.4) is irrelevant for EN 16931 visualization. Both bindings live in **the same `phlox-ubl` module**: the reader logic is nearly identical, the BG/BT field set is identical (the EN 16931 semantic model itself does not change between editions), and the only differences are which JAXB classes are imported and a handful of element-name updates that EN 16931:2026 introduces.

```java
// EN 16931:2017 binding
final PhloxInvoice aModel = UblInvoiceReader.fromUbl21 ().read (aXMLStream, aErrors);

// EN 16931:2026 binding
final PhloxInvoice aModel = UblInvoiceReader.fromUbl25 ().read (aXMLStream, aErrors);
```

Internally:

1. Sniff the root element (`Invoice` vs `CreditNote`) to pick the right marshaller variant for the chosen UBL version.
2. Use `com.helger.ubl21.UBL21Marshaller.invoice().read(...)` / `.creditNote().read(...)` — or the equivalent `UBL25Marshaller` calls — to obtain the typed JAXB object (`oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType` / `creditnote_21.CreditNoteType`, or the `_25` variants).
3. Walk the JAXB tree and populate the model. Element paths come from the **UBL Invoice** and **UBL Credit Note** columns of the EN 16931 three-way mapping (`../en16931-cii2ubl/docs/en16931-3way-mapping.md`).

**Credit-Note-specific paths.** A handful of BTs sit at different element paths in `CreditNote` than in `Invoice` — see the "Key Differences" section of the mapping doc. The reader handles them with a small dispatch helper that takes the appropriate JAXB type. Notable cases:

- BT-3 — `InvoiceTypeCode` vs `CreditNoteTypeCode`.
- BT-9 — header `DueDate` (Invoice) vs `PaymentMeans/PaymentDueDate` (Credit Note).
- BG-25 — `InvoiceLine` vs `CreditNoteLine`.
- BT-129 — `InvoicedQuantity` vs `CreditedQuantity`.

**Auto-detection.** `UblInvoiceReader.autoDetect()` reads `cbc:UBLVersionID` from the document and routes to the matching binding; it falls back to UBL 2.1 when the element is absent (which is common in EN 16931:2017 instances).

### `CiiInvoiceReader` (module `phlox-cii`)

Reads CII D16B `CrossIndustryInvoice` documents.

```java
final PhloxInvoice aModel = new CiiInvoiceReader ().read (aXMLStream, aErrors);
```

Internally:

1. Use `new com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller().read(...)`.
2. Determine document kind (`INVOICE` vs `CREDIT_NOTE`) from `ExchangedDocument/TypeCode`. Reuse the code-list constants from `en16931-cii2ubl`'s `INVOICE_TYPE_CODES` / `CREDIT_NOTE_TYPE_CODES`.
3. Walk the JAXB tree and populate the model from the **CII D16B** column of the mapping doc.

**CII gotchas the reader has to deal with:**

- **Discriminators.** Multiple BTs share the same parent element and are told apart by `TypeCode` (BT-17/18/BG-24), `ChargeIndicator` (BG-20/BG-21), `schemeID` (BT-31 VAT vs BT-32 tax-reg via `"VA"`/`"FC"`), or `@currencyID` (BT-110 vs BT-111). The reader filters by these on the way in.
- **Date formats.** Most CII dates are `udt:DateTimeString` with `@format="102"` (`yyyyMMdd`). One slot (BT-26) uses `qdt:DateTimeString`. The reader normalizes all to `LocalDate`.
- **Sign of amounts.** CII credit notes carry positive amounts; the EN 16931 model also treats them as positive. No sign manipulation in the reader.

## What readers **do** validate

- **XSD.** The underlying `ph-ubl` and `ph-cii` marshallers perform XSD validation by default, and `phlox` keeps that on. Malformed-against-schema documents are rejected — the reader returns `null` and adds the JAXB validation events to the `ErrorList`.
- That's it on the validation side.

## What readers do **not** do

- They don't apply **Schematron** rules. EN 16931 business rules (BR-CO-*, BR-S-*, BR-CL-*, BR-AE-* …) live in Schematron and are out of scope here. Run `phive-rules-en16931` upstream if you need them.
- They don't reconcile arithmetic (e.g. confirming BT-115 = BT-112 − BT-113 + BT-114). The writer renders whatever the source contains; reconciliation is out of scope.
- They don't enrich with code-list values. The reader stores `"S"` for "Standard rate" verbatim; if the writer wants to print "Standardsatz" it looks that up itself (see [[Localization]]).

## Testing strategy

Each reader has a parallel JUnit test class that:

1. Loads every example file from the shared `phlox-testfiles` module (see [[Maven Module Structure]]). The module organises files as `external/en16931/<edition>/<syntax>/...` for `<edition>` ∈ {`2017`, `2026`} and `<syntax>` ∈ {`ubl`, `cii`}.
2. Parses it.
3. Snapshots the resulting `PhloxInvoice` to JSON.
4. Diffs against the checked-in snapshot.

This catches regressions in the field-by-field mapping without needing a full PDF/HTML render cycle.
