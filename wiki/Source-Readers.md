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

| UBL version | EN 16931 edition | ph-ubl artifact | Package-private reader |
|-------------|------------------|-----------------|------------------------|
| **UBL 2.1** | EN 16931:2017 | `com.helger.ubl:ph-ubl21` | `Ubl21InvoiceReader` |
| **UBL 2.5** | EN 16931:2026 | `com.helger.ubl:ph-ubl25` | `Ubl25InvoiceReader` |

No other UBL versions are in scope — EN 16931 only specifies bindings for these two. Both bindings live in **the same `phlox-ubl` module**, but the 2026 mapping is **not** a renamed copy of the 2017 one: the semantic model gained 52 BTs and 7 BGs and several XPaths changed (BT-10, BT-21/22, BT-32, BT-90, BT-161, credit note BT-9/BT-11/BT-18). `UblInvoiceReader` is therefore a thin public facade that delegates to one dedicated reader per edition.

```java
// EN 16931:2017 binding (UBL 2.1)
final PhloxInvoice aModel = UblInvoiceReader.fromUbl21 ().read (aXMLStream, aErrors);

// EN 16931:2026 binding (UBL 2.5)
final PhloxInvoice aModel = UblInvoiceReader.fromUbl25 ().read (aXMLStream, aErrors);

// Edition taken from BT-24 (Specification identifier)
final PhloxInvoice aModel = UblInvoiceReader.autoDetect ().read (aXMLStream, aErrors);
```

Internally:

1. Sniff the root element (`Invoice` vs `CreditNote`) to pick the right marshaller variant for the chosen UBL version.
2. Use `com.helger.ubl21.UBL21Marshaller` or `com.helger.ubl25.UBL25Marshaller` to obtain the typed JAXB object (`..._21` or `..._25` packages).
3. Walk the JAXB tree and populate the model. Element paths come from the **UBL Invoice** and **UBL Credit Note** columns of the EN 16931 three-way mappings (`../en16931-cii2ubl/docs/en16931-2017-syntax.md`, `en16931-2026-syntax.md`).

**Credit-Note-specific paths.** A handful of BTs sit at different element paths in `CreditNote` than in `Invoice`. The readers handle them with small dispatch helpers that take the appropriate JAXB type. Notable cases:

- BT-3 — `InvoiceTypeCode` vs `CreditNoteTypeCode`.
- BT-9 — header `DueDate` (Invoice) vs `PaymentMeans/PaymentDueDate` (Credit Note, 2017 only; 2026 uses `DueDate` for both).
- BG-25 — `InvoiceLine` vs `CreditNoteLine`.
- BT-129 — `InvoicedQuantity` vs `CreditedQuantity`.

**Auto-detection.** `UblInvoiceReader.autoDetect()` parses the document once and asks `EEN16931Edition.detect (Node)` (from [`en16931-basics`](https://github.com/phax/en16931-basics)) for the edition behind BT-24. The XML namespaces cannot be used for this, because UBL 2.1 and 2.5 share them. When BT-24 is missing or is not an EN 16931 identifier, the reader returns `null` and adds an error that names BT-24 and the explicit factories; use `fromUbl21()` / `fromUbl25()` for such documents. The explicit factories never inspect BT-24.

### `CiiInvoiceReader` (module `phlox-cii`)

Reads CII `CrossIndustryInvoice` documents:

| CII release | EN 16931 edition | ph-cii artifact | Package-private reader |
|-------------|------------------|-----------------|------------------------|
| **D16B** | EN 16931:2017 | `com.helger.cii:ph-cii-d16b` | `CiiD16BInvoiceReader` |
| **D25A** | EN 16931:2026 | `com.helger.cii:ph-cii-d25a` | `CiiD25AInvoiceReader` |

```java
final PhloxInvoice aModel2017 = CiiInvoiceReader.fromD16B ().read (aXMLStream, aErrors);
final PhloxInvoice aModel2026 = CiiInvoiceReader.fromD25A ().read (aXMLStream, aErrors);
// Edition taken from BT-24; the public no-arg constructor is equivalent
final PhloxInvoice aModelAuto = CiiInvoiceReader.autoDetect ().read (aXMLStream, aErrors);
```

CII D16B and D25A use identical namespaces, and a D16B instance also validates against the D25A schema, so BT-24 is the only reliable discriminator — see the auto-detection rules above, which apply unchanged.

Internally:

1. Use `CIID16BCrossIndustryInvoiceTypeMarshaller` or `CIID25ACrossIndustryInvoiceTypeMarshaller`.
2. Determine document kind (`INVOICE` vs `CREDIT_NOTE`) from `ExchangedDocument/TypeCode` (`EEN16931InvoiceTypeCode` from `en16931-basics`).
3. Walk the JAXB tree and populate the model from the **CII D16B** or **CII D25A** column of the mapping doc.

**CII gotchas the readers have to deal with:**

- **Discriminators.** Multiple BTs share the same parent element and are told apart by `TypeCode` (BT-17/18/BG-24), `ChargeIndicator` (BG-20/BG-21), `schemeID` (BT-31 VAT vs BT-32 tax-reg via `"VA"`/`"FC"`), or `@currencyID` (BT-110 vs BT-111). The readers filter by these on the way in.
- **Date formats.** CII dates are `udt:DateTimeString` with `@format` `102` (`yyyyMMdd`); since 2026 BT-166 uses `208` (date and time with offset). All dates are normalized to `LocalDate`, BT-166 to `OffsetTime`.
- **D25A changes.** `BuyerReferenceID` instead of `BuyerReference`, `GlobalID` only for the party identifiers BT-29/46/60/71, `CategoryCode` instead of `TypeCode` for the VAT category, `PersonName` or `DepartmentName` for contacts, `IBANID` or `ProprietaryID` for accounts, `Value` or `ValueMeasure` for item attributes.
- **Sign of amounts.** CII credit notes carry positive amounts; the EN 16931 model also treats them as positive. No sign manipulation in the readers.

## Editions and the model

Both readers fill the same superset model. EN 16931:2026 terms stay empty for 2017 documents; `PhloxInvoice.getEdition ()` records the edition that was used. Two 2017 terms changed shape in the model for both editions: BT-10 is a list of identifiers (0..n since 2026) and BT-20 moved into BG-33 `PhloxPaymentTerms`. See [[Domain Model]].

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
