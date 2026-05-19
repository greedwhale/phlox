# Source Readers

A **reader** parses an electronic invoice in one of the supported syntaxes and populates a [[Domain Model|Domain-Model]] `NormaInvoice`. Readers do not validate business rules; they only translate the source structure into the syntax-neutral intermediate model.

## SPI

```java
package com.helger.norma.api.read;

public interface IInvoiceReader <SRC>
{
  @Nullable
  NormaInvoice read (@Nonnull SRC aSource, @Nonnull ErrorList aErrors);
}
```

- Returns `null` when parsing failed catastrophically (malformed XML, wrong root element).
- Adds entries to `aErrors` for recoverable problems (unknown enum values, unrecognized formats) but still returns a model if anything could be salvaged.
- Caller treats the result as **successful** only if both: result is non-null AND `aErrors.containsAtLeastOneError()` is `false`. Same rule as `en16931-cii2ubl`.

## Concrete readers

### `UBLInvoiceReader` (module `norma-ubl`)

Reads UBL 2.1 `Invoice` and `CreditNote` documents.

```java
final InvoiceModel aModel = UBLInvoiceReader.fromUBL21 ().read (aXMLStream, aErrors);
```

Internally:

1. Sniff the root element (`Invoice` vs `CreditNote`) to pick the right marshaller.
2. Use `com.helger.ubl21.UBL21Marshaller.invoice().read(...)` or `.creditNote().read(...)` to obtain `oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType` / `creditnote_21.CreditNoteType`.
3. Walk the JAXB tree and populate the model. Element paths come from the **UBL Invoice** and **UBL Credit Note** columns of the EN 16931 three-way mapping (`../en16931-cii2ubl/docs/en16931-3way-mapping.md`).

**Credit-Note-specific paths.** A handful of BTs sit at different element paths in `CreditNote` than in `Invoice` — see the "Key Differences" section of the mapping doc. The reader handles them with a small dispatch helper that takes the appropriate JAXB type. Notable cases:

- BT-3 — `InvoiceTypeCode` vs `CreditNoteTypeCode`.
- BT-9 — header `DueDate` (Invoice) vs `PaymentMeans/PaymentDueDate` (Credit Note).
- BG-25 — `InvoiceLine` vs `CreditNoteLine`.
- BT-129 — `InvoicedQuantity` vs `CreditedQuantity`.

**UBL versions.** Only UBL 2.1 is in the initial release because that is the EN 16931 baseline. Adding 2.2/2.3/2.4 later is a copy-and-tweak of `UBLInvoiceReader`, mirroring how `en16931-cii2ubl` maintains four parallel converter classes.

### `CIIInvoiceReader` (module `norma-cii`)

Reads CII D16B `CrossIndustryInvoice` documents.

```java
final InvoiceModel aModel = new CIIInvoiceReader ().read (aXMLStream, aErrors);
```

Internally:

1. Use `new com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller().read(...)`.
2. Determine document kind (`INVOICE` vs `CREDIT_NOTE`) from `ExchangedDocument/TypeCode`. Reuse the code-list constants from `en16931-cii2ubl`'s `INVOICE_TYPE_CODES` / `CREDIT_NOTE_TYPE_CODES`.
3. Walk the JAXB tree and populate the model from the **CII D16B** column of the mapping doc.

**CII gotchas the reader has to deal with:**

- **Discriminators.** Multiple BTs share the same parent element and are told apart by `TypeCode` (BT-17/18/BG-24), `ChargeIndicator` (BG-20/BG-21), `schemeID` (BT-31 VAT vs BT-32 tax-reg via `"VA"`/`"FC"`), or `@currencyID` (BT-110 vs BT-111). The reader filters by these on the way in.
- **Date formats.** Most CII dates are `udt:DateTimeString` with `@format="102"` (`yyyyMMdd`). One slot (BT-26) uses `qdt:DateTimeString`. The reader normalizes all to `LocalDate`.
- **Sign of amounts.** CII credit notes carry positive amounts; the EN 16931 model also treats them as positive. No sign manipulation in the reader.

## What readers do **not** do

- They don't validate against XSDs other than what the marshaller enforces by default.
- They don't apply EN 16931 business rules (BR-CO-*, BR-S-* etc.). That is the job of `phive-rules-en16931`, run by callers if they want it.
- They don't reconcile arithmetic (e.g. confirming BT-115 = BT-112 − BT-113 + BT-114). The writer renders whatever the source contains; reconciliation is out of scope.
- They don't enrich with code-list values. The reader stores `"S"` for "Standard rate" verbatim; if the writer wants to print "Standardsatz" it looks that up itself (see [[Localization]]).

## Testing strategy

Each reader has a parallel JUnit test class that:

1. Loads every example file from a shared corpus (mirroring how `en16931-cii2ubl` consumes `src/test/resources/external/cii/`).
2. Parses it.
3. Snapshots the resulting `NormaInvoice` to JSON.
4. Diffs against the checked-in snapshot.

This catches regressions in the field-by-field mapping without needing a full PDF/HTML render cycle.
