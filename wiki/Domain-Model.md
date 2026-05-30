# Domain Model

This page defines the **intermediate model** — the central Java data structure that every reader populates and every writer consumes. It is intentionally a 1:1 mapping of the EN 16931 semantic model: one Java type per Business Group (BG), one field per Business Term (BT).

The shape below is derived from the EN 16931 three-way mapping at `../en16931-cii2ubl/docs/en16931-3way-mapping.md`. Every cardinality below matches that document.

## Conventions

- All model types live in `com.helger.norma.api.model`.
- Class names use the `Norma` prefix to avoid collision with JAXB types from `ph-ubl` and `ph-cii` already on the classpath (`InvoiceType`, `PartyType`, etc.).
- Field naming follows Hungarian notation (see project root coding rules): `m_sFoo` for `String`, `m_aFoo` for objects/collections, `m_nFoo` for ints, etc.
- Collections use `ICommonsList` (`ph-commons`) and are never `null` — empty when absent.
- Single-valued optional fields are `null` when absent.
- Monetary amounts are `BigDecimal` with explicit currency held alongside (in `BT-5` at the model root, not duplicated per amount).
- Dates are `java.time.LocalDate`.

## Semantic data types

Every BT carries a **semantic data type** from EN 16931's fixed vocabulary. The `Type` column in the tables below records it; the table here pins down how each type lands in Java and which default template formatter applies (see [[Template Language]] for the format-modifier grammar).

| EN 16931 type | Java rendering on the model | Default template formatter | Notes |
|---|---|---|---|
| Identifier | `String`, or `NormaIdentifier{value, schemeID}` when a scheme is associated | as-is | Opaque or scheme-qualified. No localization. |
| Document Reference | `String` | as-is | Reference to another document. Mechanically an Identifier; semantically called out by EN 16931 because templates may want link styling. |
| Date | `java.time.LocalDate` | `\| date:long` (locale-aware) | Renderer uses `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)` by default. |
| Code | `String` (always referencing a code list) | `\| code:<listName>` (locale-aware) | Resolved through `norma-codelists_<lang>.properties`. |
| Amount | `BigDecimal` (currency from BT-5) | `\| amount` (locale-aware) | Group separators and fraction digits derived from the active currency. |
| Unit Price Amount | `BigDecimal` (subtype of Amount; may carry more fraction digits) | `\| amount` | Distinguished by EN 16931 so unit prices can show 4 decimals while totals show 2. |
| Percentage | `BigDecimal` (e.g. `19` for 19 %) | `\| percent` (locale-aware) | Two decimals + `%` suffix. |
| Quantity | `BigDecimal` (paired with a unit code BT, e.g. BT-130) | as-is | The accompanying unit code is the BT immediately after. |
| Text | `String` | as-is | Locale-blind free text — content comes from the invoice payload, not the renderer. |
| Binary Object | `byte[]` (with mime code + filename siblings) | rendered as `attachment:BG-24[i]` URL in templates | The only non-`String`, non-numeric leaf type. |

Where a row spans a value plus its scheme (e.g. `BT-29 / BT-29-1`), the `Type` column records the value's type (Identifier). The accompanying scheme BT is a paired Identifier per EN 16931 and shows up in the same row to keep the table compact.

## Top-level shape

```
NormaInvoice                              (root; corresponds to "the document")
  ├─ ENormaDocumentKind eKind             INVOICE | CREDIT_NOTE
  ├─ String sNumber                       BT-1
  ├─ LocalDate aIssueDate                 BT-2
  ├─ String sTypeCode                     BT-3
  ├─ String sCurrency                     BT-5
  ├─ String sVATAccountingCurrency        BT-6
  ├─ LocalDate aVATPointDate              BT-7
  ├─ String sVATPointDateCode             BT-8
  ├─ LocalDate aPaymentDueDate            BT-9
  ├─ String sBuyerReference               BT-10
  ├─ String sProjectReference             BT-11
  ├─ String sContractReference            BT-12
  ├─ String sPurchaseOrderReference       BT-13
  ├─ String sSalesOrderReference          BT-14
  ├─ String sReceivingAdviceReference     BT-15
  ├─ String sDespatchAdviceReference      BT-16
  ├─ String sTenderOrLotReference         BT-17
  ├─ NormaInvoicedObjectIdentifier        BT-18 / BT-18-1
  ├─ String sBuyerAccountingReference     BT-19
  ├─ String sPaymentTerms                 BT-20
  │
  ├─ ICommonsList <NormaInvoiceNote>           BG-1   (0..n)
  ├─ NormaProcessControl                       BG-2   (1..1)
  ├─ ICommonsList <NormaPrecedingInvoiceRef>   BG-3   (0..n)
  ├─ NormaSeller                               BG-4   (1..1)  → BG-5, BG-6 nested
  ├─ NormaBuyer                                BG-7   (1..1)  → BG-8, BG-9 nested
  ├─ NormaPayee                                BG-10  (0..1)
  ├─ NormaSellerTaxRepresentative              BG-11  (0..1)  → BG-12 nested
  ├─ NormaDeliveryInformation                  BG-13  (0..1)  → BG-15 nested
  ├─ NormaInvoicingPeriod                      BG-14  (0..1)
  ├─ NormaPaymentInstructions                  BG-16  (0..1)  → BG-17/BG-18/BG-19 nested
  ├─ ICommonsList <NormaDocumentAllowance>     BG-20  (0..n)
  ├─ ICommonsList <NormaDocumentCharge>        BG-21  (0..n)
  ├─ NormaDocumentTotals                       BG-22  (1..1)
  ├─ ICommonsList <NormaVATBreakdown>          BG-23  (1..n)
  ├─ ICommonsList <NormaSupportingDocument>    BG-24  (0..n)
  └─ ICommonsList <NormaInvoiceLine>           BG-25  (1..n)
```

## Header-level BTs (no BG)

These live directly on `NormaInvoice` and correspond to fields the EN 16931 model places at the root of the document.

| BT | Name | Type | Card. | Java field |
|----|------|------|-------|------------|
| BT-1 | Invoice number | Identifier | 1..1 | `m_sNumber` |
| BT-2 | Invoice issue date | Date | 1..1 | `m_aIssueDate` |
| BT-3 | Invoice type code | Code | 1..1 | `m_sTypeCode` |
| BT-5 | Invoice currency code | Code | 1..1 | `m_sCurrency` |
| BT-6 | VAT accounting currency code | Code | 0..1 | `m_sVATAccountingCurrency` |
| BT-7 | VAT point date | Date | 0..1 | `m_aVATPointDate` |
| BT-8 | VAT point date code | Code | 0..1 | `m_sVATPointDateCode` |
| BT-9 | Payment due date | Date | 0..1 | `m_aPaymentDueDate` |
| BT-10 | Buyer reference | Text | 0..1 | `m_sBuyerReference` |
| BT-11 | Project reference | Document Reference | 0..1 | `m_sProjectReference` |
| BT-12 | Contract reference | Document Reference | 0..1 | `m_sContractReference` |
| BT-13 | Purchase order reference | Document Reference | 0..1 | `m_sPurchaseOrderReference` |
| BT-14 | Sales order reference | Document Reference | 0..1 | `m_sSalesOrderReference` |
| BT-15 | Receiving advice reference | Document Reference | 0..1 | `m_sReceivingAdviceReference` |
| BT-16 | Despatch advice reference | Document Reference | 0..1 | `m_sDespatchAdviceReference` |
| BT-17 | Tender or lot reference | Document Reference | 0..1 | `m_sTenderOrLotReference` |
| BT-18 / BT-18-1 | Invoiced object identifier + scheme | Identifier | 0..1 | `m_aInvoicedObjectIdentifier` |
| BT-19 | Buyer accounting reference | Text | 0..1 | `m_sBuyerAccountingReference` |
| BT-20 | Payment terms | Text | 0..1 | `m_sPaymentTerms` |

## BG-1 Invoice Note (0..n) — `NormaInvoiceNote`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-21 | Note subject code | Code | 0..1 | `m_sSubjectCode` |
| BT-22 | Note | Text | 1..1 | `m_sContent` |

## BG-2 Process Control (1..1) — `NormaProcessControl`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-23 | Business process type | Text | 0..1 | `m_sBusinessProcessType` |
| BT-24 | Specification identifier | Identifier | 1..1 | `m_sSpecificationIdentifier` |

## BG-3 Preceding Invoice Reference (0..n) — `NormaPrecedingInvoiceRef`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-25 | Preceding Invoice number | Document Reference | 1..1 | `m_sNumber` |
| BT-26 | Preceding Invoice issue date | Date | 0..1 | `m_aIssueDate` |

## BG-4 Seller (1..1) — `NormaSeller`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-27 | Seller name | Text | 1..1 | `m_sName` |
| BT-28 | Seller trading name | Text | 0..1 | `m_sTradingName` |
| BT-29 / BT-29-1 | Seller identifiers (id + schemeID) | Identifier | 0..n | `m_aIdentifiers` |
| BT-30 / BT-30-1 | Legal registration ID + schemeID | Identifier | 0..1 | `m_aLegalRegistration` |
| BT-31 | Seller VAT identifier | Identifier | 0..1 | `m_sVATIdentifier` |
| BT-32 | Seller tax registration ID | Identifier | 0..1 | `m_sTaxRegistration` |
| BT-33 | Seller additional legal info | Text | 0..1 | `m_sAdditionalLegalInfo` |
| BT-34 / BT-34-1 | Electronic address + schemeID | Identifier | 1..1 | `m_aElectronicAddress` |
| — | Postal address (BG-5) | — | 1..1 | `m_aPostalAddress` |
| — | Contact (BG-6) | — | 0..1 | `m_aContact` |

## BG-5 Seller Postal Address (1..1) — `NormaPostalAddress`

Shared shape with BG-8, BG-12, BG-15.

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-35 (BT-50 / BT-64 / BT-75) | Address line 1 | Text | 0..1 | `m_sLine1` |
| BT-36 (BT-51 / BT-65 / BT-76) | Address line 2 | Text | 0..1 | `m_sLine2` |
| BT-162 (BT-163 / BT-164 / BT-165) | Address line 3 | Text | 0..1 | `m_sLine3` |
| BT-37 (BT-52 / BT-66 / BT-77) | City | Text | 0..1 | `m_sCity` |
| BT-38 (BT-53 / BT-67 / BT-78) | Post code | Text | 0..1 | `m_sPostCode` |
| BT-39 (BT-54 / BT-68 / BT-79) | Country subdivision | Text | 0..1 | `m_sCountrySubdivision` |
| BT-40 (BT-55 / BT-69 / BT-80) | Country code (ISO 3166-1) | Code | 1..1 | `m_sCountryCode` |

## BG-6 Seller Contact (0..1) — `NormaContact`

Shared shape with BG-9.

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-41 (BT-56) | Contact point | Text | 0..1 | `m_sName` |
| BT-42 (BT-57) | Telephone | Text | 0..1 | `m_sTelephone` |
| BT-43 (BT-58) | Email | Text | 0..1 | `m_sEmail` |

## BG-7 Buyer (1..1) — `NormaBuyer`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-44 | Buyer name | Text | 1..1 | `m_sName` |
| BT-45 | Buyer trading name | Text | 0..1 | `m_sTradingName` |
| BT-46 / BT-46-1 | Buyer identifiers | Identifier | 0..n | `m_aIdentifiers` |
| BT-47 / BT-47-1 | Legal registration ID + schemeID | Identifier | 0..1 | `m_aLegalRegistration` |
| BT-48 | Buyer VAT identifier | Identifier | 0..1 | `m_sVATIdentifier` |
| BT-49 / BT-49-1 | Electronic address + schemeID | Identifier | 1..1 | `m_aElectronicAddress` |
| — | Postal address (BG-8) | — | 1..1 | `m_aPostalAddress` |
| — | Contact (BG-9) | — | 0..1 | `m_aContact` |

## BG-10 Payee (0..1) — `NormaPayee`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-59 | Payee name | Text | 1..1 | `m_sName` |
| BT-60 / BT-60-1 | Payee identifier + schemeID | Identifier | 0..1 | `m_aIdentifier` |
| BT-61 / BT-61-1 | Legal registration ID + schemeID | Identifier | 0..1 | `m_aLegalRegistration` |

## BG-11 Seller Tax Representative (0..1) — `NormaSellerTaxRepresentative`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-62 | Tax rep name | Text | 1..1 | `m_sName` |
| BT-63 | Tax rep VAT ID | Identifier | 1..1 | `m_sVATIdentifier` |
| — | Postal address (BG-12) | — | 1..1 | `m_aPostalAddress` |

## BG-13 Delivery Information (0..1) — `NormaDeliveryInformation`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-70 | Deliver-to party name | Text | 0..1 | `m_sPartyName` |
| BT-71 / BT-71-1 | Deliver-to location id + schemeID | Identifier | 0..1 | `m_aLocationIdentifier` |
| BT-72 | Actual delivery date | Date | 0..1 | `m_aActualDeliveryDate` |
| — | Deliver-to address (BG-15) | — | 0..1 | `m_aPostalAddress` |

## BG-14 Invoicing Period (0..1) — `NormaInvoicingPeriod`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-73 | Period start date | Date | 0..1 | `m_aStartDate` |
| BT-74 | Period end date | Date | 0..1 | `m_aEndDate` |

## BG-16 Payment Instructions (0..1) — `NormaPaymentInstructions`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-81 | Payment means type code | Code | 1..1 | `m_sPaymentMeansCode` |
| BT-82 | Payment means text | Text | 0..1 | `m_sPaymentMeansText` |
| BT-83 | Remittance information | Text | 0..1 | `m_sRemittanceInformation` |
| — | Credit transfers (BG-17) | — | 0..n | `m_aCreditTransfers` |
| — | Payment card (BG-18) | — | 0..1 | `m_aPaymentCard` |
| — | Direct debit (BG-19) | — | 0..1 | `m_aDirectDebit` |

### BG-17 Credit Transfer (0..n) — `NormaCreditTransfer`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-84 | Payment account identifier | Identifier | 1..1 | `m_sAccountIdentifier` |
| BT-85 | Payment account name | Text | 0..1 | `m_sAccountName` |
| BT-86 | Payment service provider ID | Identifier | 0..1 | `m_sServiceProviderID` |

### BG-18 Payment Card (0..1) — `NormaPaymentCard`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-87 | Payment card PAN | Identifier | 1..1 | `m_sPAN` |
| BT-88 | Payment card holder name | Text | 0..1 | `m_sHolderName` |

### BG-19 Direct Debit (0..1) — `NormaDirectDebit`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-89 | Mandate reference ID | Identifier | 0..1 | `m_sMandateReferenceID` |
| BT-90 | Bank assigned creditor ID | Identifier | 0..1 | `m_sBankAssignedCreditorID` |
| BT-91 | Debited account ID | Identifier | 0..1 | `m_sDebitedAccountID` |

## BG-20 Document-Level Allowance (0..n) — `NormaDocumentAllowance`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-92 | Allowance amount | Amount | 1..1 | `m_aAmount` |
| BT-93 | Allowance base amount | Amount | 0..1 | `m_aBaseAmount` |
| BT-94 | Allowance percentage | Percentage | 0..1 | `m_aPercentage` |
| BT-95 | VAT category code | Code | 1..1 | `m_sVATCategoryCode` |
| BT-96 | VAT rate | Percentage | 0..1 | `m_aVATRate` |
| BT-97 | Allowance reason | Text | 0..1 | `m_sReason` |
| BT-98 | Allowance reason code | Code | 0..1 | `m_sReasonCode` |

## BG-21 Document-Level Charge (0..n) — `NormaDocumentCharge`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-99 | Charge amount | Amount | 1..1 | `m_aAmount` |
| BT-100 | Charge base amount | Amount | 0..1 | `m_aBaseAmount` |
| BT-101 | Charge percentage | Percentage | 0..1 | `m_aPercentage` |
| BT-102 | VAT category code | Code | 1..1 | `m_sVATCategoryCode` |
| BT-103 | VAT rate | Percentage | 0..1 | `m_aVATRate` |
| BT-104 | Charge reason | Text | 0..1 | `m_sReason` |
| BT-105 | Charge reason code | Code | 0..1 | `m_sReasonCode` |

## BG-22 Document Totals (1..1) — `NormaDocumentTotals`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-106 | Sum of line net amounts | Amount | 1..1 | `m_aLineNetTotal` |
| BT-107 | Sum of document-level allowances | Amount | 0..1 | `m_aAllowanceTotal` |
| BT-108 | Sum of document-level charges | Amount | 0..1 | `m_aChargeTotal` |
| BT-109 | Invoice total without VAT | Amount | 1..1 | `m_aTaxExclusiveTotal` |
| BT-110 | Invoice total VAT amount | Amount | 0..1 | `m_aTaxTotal` |
| BT-111 | VAT total in accounting currency | Amount | 0..1 | `m_aTaxTotalAccountingCurrency` |
| BT-112 | Invoice total with VAT | Amount | 1..1 | `m_aTaxInclusiveTotal` |
| BT-113 | Paid amount | Amount | 0..1 | `m_aPrepaidAmount` |
| BT-114 | Rounding amount | Amount | 0..1 | `m_aRoundingAmount` |
| BT-115 | Amount due for payment | Amount | 1..1 | `m_aPayableAmount` |

> BT-111 is the only amount on the model whose currency may differ from BT-5. The Mustache adapter routes `{{BT-111 | amount}}` through BT-6 when present (see [[Template Language]] format modifiers).

## BG-23 VAT Breakdown (1..n) — `NormaVATBreakdown`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-116 | Taxable amount | Amount | 1..1 | `m_aTaxableAmount` |
| BT-117 | Tax amount | Amount | 1..1 | `m_aTaxAmount` |
| BT-118 | VAT category code | Code | 1..1 | `m_sCategoryCode` |
| BT-119 | VAT rate | Percentage | 0..1 | `m_aRate` |
| BT-120 | Exemption reason text | Text | 0..1 | `m_sExemptionReason` |
| BT-121 | Exemption reason code | Code | 0..1 | `m_sExemptionReasonCode` |

## BG-24 Additional Supporting Document (0..n) — `NormaSupportingDocument`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-122 | Document reference | Document Reference | 1..1 | `m_sReference` |
| BT-123 | Document description | Text | 0..1 | `m_sDescription` |
| BT-124 | External document location | Identifier | 0..1 | `m_sExternalLocation` |
| BT-125 | Attached document (binary) | Binary Object | 0..1 | `m_aAttachment` |
| BT-125-1 | Attachment mime code | Code | 1..1 | `m_aAttachment.m_sMimeCode` |
| BT-125-2 | Attachment filename | Text | 0..1 | `m_aAttachment.m_sFilename` |

## BG-25 Invoice Line (1..n) — `NormaInvoiceLine`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-126 | Line identifier | Identifier | 1..1 | `m_sID` |
| BT-127 | Line note | Text | 0..1 | `m_sNote` |
| BT-128 / BT-128-1 | Line object identifier + schemeID | Identifier | 0..1 | `m_aObjectIdentifier` |
| BT-129 | Invoiced quantity | Quantity | 1..1 | `m_aQuantity` |
| BT-130 | Quantity unit of measure | Code | 1..1 | `m_sQuantityUnit` |
| BT-131 | Line net amount | Amount | 1..1 | `m_aNetAmount` |
| BT-132 | Referenced purchase-order line | Document Reference | 0..1 | `m_sPurchaseOrderLineRef` |
| BT-133 | Line buyer accounting reference | Text | 0..1 | `m_sAccountingCost` |
| — | Line period (BG-26) | — | 0..1 | `m_aPeriod` |
| — | Line allowances (BG-27) | — | 0..n | `m_aAllowances` |
| — | Line charges (BG-28) | — | 0..n | `m_aCharges` |
| — | Price details (BG-29) | — | 1..1 | `m_aPrice` |
| — | Line VAT info (BG-30) | — | 1..1 | `m_aLineVAT` |
| — | Item information (BG-31) | — | 1..1 | `m_aItem` |

### BG-26 Invoice Line Period (0..1) — `NormaInvoicingPeriod` (reused)

Fields BT-134 / BT-135 — same shape as BG-14; both `Date`.

### BG-27 Line Allowance (0..n) — `NormaLineAllowance`

Fields BT-136 through BT-140 — analogous to BG-20 minus the VAT category fields. Types: BT-136 `Amount`, BT-137 `Amount`, BT-138 `Percentage`, BT-139 `Text`, BT-140 `Code`.

### BG-28 Line Charge (0..n) — `NormaLineCharge`

Fields BT-141 through BT-145 — analogous to BG-21 minus the VAT category fields. Types: BT-141 `Amount`, BT-142 `Amount`, BT-143 `Percentage`, BT-144 `Text`, BT-145 `Code`.

### BG-29 Price Details (1..1) — `NormaPriceDetails`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-146 | Item net price | Unit Price Amount | 1..1 | `m_aNetPrice` |
| BT-147 | Item price discount | Unit Price Amount | 0..1 | `m_aDiscount` |
| BT-148 | Item gross price | Unit Price Amount | 0..1 | `m_aGrossPrice` |
| BT-149 | Item price base quantity | Quantity | 0..1 | `m_aBaseQuantity` |
| BT-150 | Base quantity unit | Code | 0..1 | `m_sBaseQuantityUnit` |

### BG-30 Line VAT Information (1..1) — `NormaLineVAT`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-151 | Item VAT category code | Code | 1..1 | `m_sCategoryCode` |
| BT-152 | Item VAT rate | Percentage | 0..1 | `m_aRate` |

### BG-31 Item Information (1..1) — `NormaItem`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-153 | Item name | Text | 1..1 | `m_sName` |
| BT-154 | Item description | Text | 0..1 | `m_sDescription` |
| BT-155 | Seller's item identifier | Identifier | 0..1 | `m_sSellerItemID` |
| BT-156 | Buyer's item identifier | Identifier | 0..1 | `m_sBuyerItemID` |
| BT-157 / BT-157-1 | Standard item identifier + schemeID | Identifier | 0..1 | `m_aStandardItemID` |
| BT-158 / BT-158-1 / BT-158-2 | Classifications | Identifier | 0..n | `m_aClassifications` |
| BT-159 | Country of origin | Code | 0..1 | `m_sCountryOfOrigin` |
| — | Item attributes (BG-32) | — | 0..n | `m_aAttributes` |

### BG-32 Item Attributes (0..n) — `NormaItemAttribute`

| BT | Name | Type | Card. | Field |
|----|------|------|-------|-------|
| BT-160 | Attribute name | Text | 1..1 | `m_sName` |
| BT-161 | Attribute value | Text | 1..1 | `m_sValue` |

## Reusable value-object types

These small classes carry an identifier and its scheme together (used wherever the EN 16931 model has BT-X plus BT-X-1):

```
NormaIdentifier
  ├─ String m_sValue
  └─ String m_sSchemeID                  (nullable)

NormaAttachment                          (BT-125)
  ├─ byte[] m_aBinary
  ├─ String m_sMimeCode                  (BT-125-1, required)
  └─ String m_sFilename                  (BT-125-2)
```

## Open questions deferred to implementation

- **Multi-currency amounts (BT-111).** Held as a separate `NormaMonetaryAmount{BigDecimal, String currency}` only for the one slot that may differ; everywhere else the model carries plain `BigDecimal` and the writer formats with the root currency from BT-5.
- **Builder vs. setters.** Defer until first reader is written; mutable beans with `set*` methods are the default Helger pattern.
- **JSON serialization.** Will follow once readers and writers stabilize; intended for diagnostics, not for round-tripping.
