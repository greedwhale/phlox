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

| BT | Name | Card. | Java field |
|----|------|-------|------------|
| BT-1 | Invoice number | 1..1 | `m_sNumber` |
| BT-2 | Invoice issue date | 1..1 | `m_aIssueDate` |
| BT-3 | Invoice type code | 1..1 | `m_sTypeCode` |
| BT-5 | Invoice currency code | 1..1 | `m_sCurrency` |
| BT-6 | VAT accounting currency code | 0..1 | `m_sVATAccountingCurrency` |
| BT-7 | VAT point date | 0..1 | `m_aVATPointDate` |
| BT-8 | VAT point date code | 0..1 | `m_sVATPointDateCode` |
| BT-9 | Payment due date | 0..1 | `m_aPaymentDueDate` |
| BT-10 | Buyer reference | 0..1 | `m_sBuyerReference` |
| BT-11 | Project reference | 0..1 | `m_sProjectReference` |
| BT-12 | Contract reference | 0..1 | `m_sContractReference` |
| BT-13 | Purchase order reference | 0..1 | `m_sPurchaseOrderReference` |
| BT-14 | Sales order reference | 0..1 | `m_sSalesOrderReference` |
| BT-15 | Receiving advice reference | 0..1 | `m_sReceivingAdviceReference` |
| BT-16 | Despatch advice reference | 0..1 | `m_sDespatchAdviceReference` |
| BT-17 | Tender or lot reference | 0..1 | `m_sTenderOrLotReference` |
| BT-18 / BT-18-1 | Invoiced object identifier + scheme | 0..1 | `m_aInvoicedObjectIdentifier` |
| BT-19 | Buyer accounting reference | 0..1 | `m_sBuyerAccountingReference` |
| BT-20 | Payment terms | 0..1 | `m_sPaymentTerms` |

## BG-1 Invoice Note (0..n) — `NormaInvoiceNote`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-21 | Note subject code | 0..1 | `m_sSubjectCode` |
| BT-22 | Note | 1..1 | `m_sContent` |

## BG-2 Process Control (1..1) — `NormaProcessControl`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-23 | Business process type | 0..1 | `m_sBusinessProcessType` |
| BT-24 | Specification identifier | 1..1 | `m_sSpecificationIdentifier` |

## BG-3 Preceding Invoice Reference (0..n) — `NormaPrecedingInvoiceRef`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-25 | Preceding Invoice number | 1..1 | `m_sNumber` |
| BT-26 | Preceding Invoice issue date | 0..1 | `m_aIssueDate` |

## BG-4 Seller (1..1) — `NormaSeller`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-27 | Seller name | 1..1 | `m_sName` |
| BT-28 | Seller trading name | 0..1 | `m_sTradingName` |
| BT-29 / BT-29-1 | Seller identifiers (id + schemeID) | 0..n | `m_aIdentifiers` |
| BT-30 / BT-30-1 | Legal registration ID + schemeID | 0..1 | `m_aLegalRegistration` |
| BT-31 | Seller VAT identifier | 0..1 | `m_sVATIdentifier` |
| BT-32 | Seller tax registration ID | 0..1 | `m_sTaxRegistration` |
| BT-33 | Seller additional legal info | 0..1 | `m_sAdditionalLegalInfo` |
| BT-34 / BT-34-1 | Electronic address + schemeID | 1..1 | `m_aElectronicAddress` |
| — | Postal address (BG-5) | 1..1 | `m_aPostalAddress` |
| — | Contact (BG-6) | 0..1 | `m_aContact` |

## BG-5 Seller Postal Address (1..1) — `NormaPostalAddress`

Shared shape with BG-8, BG-12, BG-15.

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-35 (BT-50 / BT-64 / BT-75) | Address line 1 | 0..1 | `m_sLine1` |
| BT-36 (BT-51 / BT-65 / BT-76) | Address line 2 | 0..1 | `m_sLine2` |
| BT-162 (BT-163 / BT-164 / BT-165) | Address line 3 | 0..1 | `m_sLine3` |
| BT-37 (BT-52 / BT-66 / BT-77) | City | 0..1 | `m_sCity` |
| BT-38 (BT-53 / BT-67 / BT-78) | Post code | 0..1 | `m_sPostCode` |
| BT-39 (BT-54 / BT-68 / BT-79) | Country subdivision | 0..1 | `m_sCountrySubdivision` |
| BT-40 (BT-55 / BT-69 / BT-80) | Country code (ISO 3166-1) | 1..1 | `m_sCountryCode` |

## BG-6 Seller Contact (0..1) — `NormaContact`

Shared shape with BG-9.

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-41 (BT-56) | Contact point | 0..1 | `m_sName` |
| BT-42 (BT-57) | Telephone | 0..1 | `m_sTelephone` |
| BT-43 (BT-58) | Email | 0..1 | `m_sEmail` |

## BG-7 Buyer (1..1) — `NormaBuyer`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-44 | Buyer name | 1..1 | `m_sName` |
| BT-45 | Buyer trading name | 0..1 | `m_sTradingName` |
| BT-46 / BT-46-1 | Buyer identifiers | 0..n | `m_aIdentifiers` |
| BT-47 / BT-47-1 | Legal registration ID + schemeID | 0..1 | `m_aLegalRegistration` |
| BT-48 | Buyer VAT identifier | 0..1 | `m_sVATIdentifier` |
| BT-49 / BT-49-1 | Electronic address + schemeID | 1..1 | `m_aElectronicAddress` |
| — | Postal address (BG-8) | 1..1 | `m_aPostalAddress` |
| — | Contact (BG-9) | 0..1 | `m_aContact` |

## BG-10 Payee (0..1) — `NormaPayee`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-59 | Payee name | 1..1 | `m_sName` |
| BT-60 / BT-60-1 | Payee identifier + schemeID | 0..1 | `m_aIdentifier` |
| BT-61 / BT-61-1 | Legal registration ID + schemeID | 0..1 | `m_aLegalRegistration` |

## BG-11 Seller Tax Representative (0..1) — `NormaSellerTaxRepresentative`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-62 | Tax rep name | 1..1 | `m_sName` |
| BT-63 | Tax rep VAT ID | 1..1 | `m_sVATIdentifier` |
| — | Postal address (BG-12) | 1..1 | `m_aPostalAddress` |

## BG-13 Delivery Information (0..1) — `NormaDeliveryInformation`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-70 | Deliver-to party name | 0..1 | `m_sPartyName` |
| BT-71 / BT-71-1 | Deliver-to location id + schemeID | 0..1 | `m_aLocationIdentifier` |
| BT-72 | Actual delivery date | 0..1 | `m_aActualDeliveryDate` |
| — | Deliver-to address (BG-15) | 0..1 | `m_aPostalAddress` |

## BG-14 Invoicing Period (0..1) — `NormaInvoicingPeriod`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-73 | Period start date | 0..1 | `m_aStartDate` |
| BT-74 | Period end date | 0..1 | `m_aEndDate` |

## BG-16 Payment Instructions (0..1) — `NormaPaymentInstructions`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-81 | Payment means type code | 1..1 | `m_sPaymentMeansCode` |
| BT-82 | Payment means text | 0..1 | `m_sPaymentMeansText` |
| BT-83 | Remittance information | 0..1 | `m_sRemittanceInformation` |
| — | Credit transfers (BG-17) | 0..n | `m_aCreditTransfers` |
| — | Payment card (BG-18) | 0..1 | `m_aPaymentCard` |
| — | Direct debit (BG-19) | 0..1 | `m_aDirectDebit` |

### BG-17 Credit Transfer (0..n) — `NormaCreditTransfer`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-84 | Payment account identifier | 1..1 | `m_sAccountIdentifier` |
| BT-85 | Payment account name | 0..1 | `m_sAccountName` |
| BT-86 | Payment service provider ID | 0..1 | `m_sServiceProviderID` |

### BG-18 Payment Card (0..1) — `NormaPaymentCard`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-87 | Payment card PAN | 1..1 | `m_sPAN` |
| BT-88 | Payment card holder name | 0..1 | `m_sHolderName` |

### BG-19 Direct Debit (0..1) — `NormaDirectDebit`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-89 | Mandate reference ID | 0..1 | `m_sMandateReferenceID` |
| BT-90 | Bank assigned creditor ID | 0..1 | `m_sBankAssignedCreditorID` |
| BT-91 | Debited account ID | 0..1 | `m_sDebitedAccountID` |

## BG-20 Document-Level Allowance (0..n) — `NormaDocumentAllowance`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-92 | Allowance amount | 1..1 | `m_aAmount` |
| BT-93 | Allowance base amount | 0..1 | `m_aBaseAmount` |
| BT-94 | Allowance percentage | 0..1 | `m_aPercentage` |
| BT-95 | VAT category code | 1..1 | `m_sVATCategoryCode` |
| BT-96 | VAT rate | 0..1 | `m_aVATRate` |
| BT-97 | Allowance reason | 0..1 | `m_sReason` |
| BT-98 | Allowance reason code | 0..1 | `m_sReasonCode` |

## BG-21 Document-Level Charge (0..n) — `NormaDocumentCharge`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-99 | Charge amount | 1..1 | `m_aAmount` |
| BT-100 | Charge base amount | 0..1 | `m_aBaseAmount` |
| BT-101 | Charge percentage | 0..1 | `m_aPercentage` |
| BT-102 | VAT category code | 1..1 | `m_sVATCategoryCode` |
| BT-103 | VAT rate | 0..1 | `m_aVATRate` |
| BT-104 | Charge reason | 0..1 | `m_sReason` |
| BT-105 | Charge reason code | 0..1 | `m_sReasonCode` |

## BG-22 Document Totals (1..1) — `NormaDocumentTotals`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-106 | Sum of line net amounts | 1..1 | `m_aLineNetTotal` |
| BT-107 | Sum of document-level allowances | 0..1 | `m_aAllowanceTotal` |
| BT-108 | Sum of document-level charges | 0..1 | `m_aChargeTotal` |
| BT-109 | Invoice total without VAT | 1..1 | `m_aTaxExclusiveTotal` |
| BT-110 | Invoice total VAT amount | 0..1 | `m_aTaxTotal` |
| BT-111 | VAT total in accounting currency | 0..1 | `m_aTaxTotalAccountingCurrency` |
| BT-112 | Invoice total with VAT | 1..1 | `m_aTaxInclusiveTotal` |
| BT-113 | Paid amount | 0..1 | `m_aPrepaidAmount` |
| BT-114 | Rounding amount | 0..1 | `m_aRoundingAmount` |
| BT-115 | Amount due for payment | 1..1 | `m_aPayableAmount` |

## BG-23 VAT Breakdown (1..n) — `NormaVATBreakdown`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-116 | Taxable amount | 1..1 | `m_aTaxableAmount` |
| BT-117 | Tax amount | 1..1 | `m_aTaxAmount` |
| BT-118 | VAT category code | 1..1 | `m_sCategoryCode` |
| BT-119 | VAT rate | 0..1 | `m_aRate` |
| BT-120 | Exemption reason text | 0..1 | `m_sExemptionReason` |
| BT-121 | Exemption reason code | 0..1 | `m_sExemptionReasonCode` |

## BG-24 Additional Supporting Document (0..n) — `NormaSupportingDocument`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-122 | Document reference | 1..1 | `m_sReference` |
| BT-123 | Document description | 0..1 | `m_sDescription` |
| BT-124 | External document location | 0..1 | `m_sExternalLocation` |
| BT-125 | Attached document (binary) | 0..1 | `m_aAttachment` |
| BT-125-1 | Attachment mime code | 1..1 | `m_aAttachment.m_sMimeCode` |
| BT-125-2 | Attachment filename | 0..1 | `m_aAttachment.m_sFilename` |

## BG-25 Invoice Line (1..n) — `NormaInvoiceLine`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-126 | Line identifier | 1..1 | `m_sID` |
| BT-127 | Line note | 0..1 | `m_sNote` |
| BT-128 / BT-128-1 | Line object identifier + schemeID | 0..1 | `m_aObjectIdentifier` |
| BT-129 | Invoiced quantity | 1..1 | `m_aQuantity` |
| BT-130 | Quantity unit of measure | 1..1 | `m_sQuantityUnit` |
| BT-131 | Line net amount | 1..1 | `m_aNetAmount` |
| BT-132 | Referenced purchase-order line | 0..1 | `m_sPurchaseOrderLineRef` |
| BT-133 | Line buyer accounting reference | 0..1 | `m_sAccountingCost` |
| — | Line period (BG-26) | 0..1 | `m_aPeriod` |
| — | Line allowances (BG-27) | 0..n | `m_aAllowances` |
| — | Line charges (BG-28) | 0..n | `m_aCharges` |
| — | Price details (BG-29) | 1..1 | `m_aPrice` |
| — | Line VAT info (BG-30) | 1..1 | `m_aLineVAT` |
| — | Item information (BG-31) | 1..1 | `m_aItem` |

### BG-26 Invoice Line Period (0..1) — `NormaInvoicingPeriod` (reused)

Fields BT-134 / BT-135 — same shape as BG-14.

### BG-27 Line Allowance (0..n) — `NormaLineAllowance`

Fields BT-136 through BT-140 — analogous to BG-20 minus the VAT category fields.

### BG-28 Line Charge (0..n) — `NormaLineCharge`

Fields BT-141 through BT-145 — analogous to BG-21 minus the VAT category fields.

### BG-29 Price Details (1..1) — `NormaPriceDetails`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-146 | Item net price | 1..1 | `m_aNetPrice` |
| BT-147 | Item price discount | 0..1 | `m_aDiscount` |
| BT-148 | Item gross price | 0..1 | `m_aGrossPrice` |
| BT-149 | Item price base quantity | 0..1 | `m_aBaseQuantity` |
| BT-150 | Base quantity unit | 0..1 | `m_sBaseQuantityUnit` |

### BG-30 Line VAT Information (1..1) — `NormaLineVAT`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-151 | Item VAT category code | 1..1 | `m_sCategoryCode` |
| BT-152 | Item VAT rate | 0..1 | `m_aRate` |

### BG-31 Item Information (1..1) — `NormaItem`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-153 | Item name | 1..1 | `m_sName` |
| BT-154 | Item description | 0..1 | `m_sDescription` |
| BT-155 | Seller's item identifier | 0..1 | `m_sSellerItemID` |
| BT-156 | Buyer's item identifier | 0..1 | `m_sBuyerItemID` |
| BT-157 / BT-157-1 | Standard item identifier + schemeID | 0..1 | `m_aStandardItemID` |
| BT-158 / BT-158-1 / BT-158-2 | Classifications | 0..n | `m_aClassifications` |
| BT-159 | Country of origin | 0..1 | `m_sCountryOfOrigin` |
| — | Item attributes (BG-32) | 0..n | `m_aAttributes` |

### BG-32 Item Attributes (0..n) — `NormaItemAttribute`

| BT | Name | Card. | Field |
|----|------|-------|-------|
| BT-160 | Attribute name | 1..1 | `m_sName` |
| BT-161 | Attribute value | 1..1 | `m_sValue` |

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
