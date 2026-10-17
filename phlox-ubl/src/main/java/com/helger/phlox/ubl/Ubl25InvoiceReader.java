/*
 * Copyright (C) 2026 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.phlox.ubl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.datetime.xml.XMLOffsetDate;
import com.helger.datetime.xml.XMLOffsetTime;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.basics.EEN16931Edition;
import com.helger.phlox.api.model.*;
import com.helger.ubl25.UBL25Marshaller;
import com.helger.xsds.ccts.cct.schemamodule.AmountType;
import com.helger.xsds.ccts.cct.schemamodule.TextType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.BuyerReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.NoteType;
import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType;

/**
 * Reads UBL 2.5 Invoice and CreditNote documents (EN 16931:2026 binding) into a
 * {@link PhloxInvoice}. Used by {@link UblInvoiceReader}.
 *
 * @author Philip Helger
 */
final class Ubl25InvoiceReader
{
  /** UNTDID 5153 list identifier used to tell BT-177/BT-193 from the reason codes BT-105/BT-145. */
  private static final String NON_VAT_TAX_CODE_LIST_ID = "5153";

  Ubl25InvoiceReader ()
  {}

  /**
   * Read a buffered UBL 2.5 document. Tries Invoice first, then CreditNote.
   *
   * @param aBytes
   *        The document bytes. Never {@code null}.
   * @param aErrors
   *        The error list to fill. Never {@code null}.
   * @return The populated model or {@code null} if the document is neither a UBL 2.5 Invoice nor
   *         CreditNote.
   */
  @Nullable
  PhloxInvoice read (final byte @NonNull [] aBytes, @NonNull final ErrorList aErrors)
  {
    final InvoiceType aInvoice = UBL25Marshaller.invoice ().read (new NonBlockingByteArrayInputStream (aBytes));
    if (aInvoice != null)
      return readInvoice (aInvoice, aErrors);

    final CreditNoteType aCreditNote = UBL25Marshaller.creditNote ()
                                                      .read (new NonBlockingByteArrayInputStream (aBytes));
    if (aCreditNote != null)
      return readCreditNote (aCreditNote, aErrors);

    aErrors.add (SingleError.builderError ()
                            .errorText ("Source could not be parsed as UBL 2.5 Invoice or CreditNote")
                            .build ());
    return null;
  }

  // ===== Document level helpers =====

  private static void _readIssueTime (@Nullable final XMLOffsetTime aTime,
                                      @NonNull final PhloxInvoice aModel,
                                      @NonNull final ErrorList aErrors)
  {
    // BT-166 — without a time offset the time cannot be represented as OffsetTime
    if (aTime != null)
      if (aTime.hasOffset ())
        aModel.setIssueTime (OffsetTime.of (aTime.toLocalTime (), aTime.getOffset ()));
      else
        aErrors.add (SingleError.builderWarn ()
                                .errorText ("The invoice issue time (BT-166) '" +
                                            aTime +
                                            "' has no time offset and is therefore ignored")
                                .build ());
  }

  private static void _readAnnotations (@NonNull final List <AnnotationType> aAnnotations,
                                        @NonNull final PhloxInvoice aModel)
  {
    // BG-1 — BT-21 subject code and BT-22 note
    for (final AnnotationType aAnnotation : aAnnotations)
    {
      final String sSubjectCode = _str (aAnnotation.getSubjectCodeValue ());
      final String sContent = aAnnotation.getAnnotationContent ().isEmpty () ? null
                                                                              : _str (aAnnotation.getAnnotationContent ()
                                                                                                 .get (0)
                                                                                                 .getValue ());
      if (sSubjectCode != null || sContent != null)
        aModel.invoiceNotes ().add (new PhloxInvoiceNote (sSubjectCode, sContent));
    }
  }

  private static void _readBuyerReferences (@NonNull final List <BuyerAssignedReferenceType> aRefs,
                                            @NonNull final PhloxInvoice aModel)
  {
    // BT-10 with BT-10-1 (UNTDID 1153 scheme identifier)
    for (final BuyerAssignedReferenceType aRef : aRefs)
    {
      final String sScheme = _str (aRef.getBuyerReferenceCodeValue ());
      for (final BuyerReferenceType aBuyerReference : aRef.getBuyerReference ())
      {
        final String sValue = _str (aBuyerReference.getValue ());
        if (sValue != null)
          aModel.buyerReferences ().add (new PhloxIdentifier (sValue, sScheme));
      }
    }
  }

  private static void _readExchangeRate (@Nullable final ExchangeRateType aRate, @NonNull final PhloxInvoice aModel)
  {
    // BT-167 with BT-167-1 (target currency) and BT-167-2 (source currency)
    if (aRate != null)
    {
      aModel.setVatExchangeRate (aRate.getCalculationRateValue ());
      aModel.setVatExchangeRateTargetCurrency (_str (aRate.getTargetCurrencyCodeValue ()));
      aModel.setVatExchangeRateSourceCurrency (_str (aRate.getSourceCurrencyCodeValue ()));
    }
  }

  private static void _readPaymentTerms (@NonNull final List <PaymentTermsType> aPaymentTerms,
                                         @NonNull final PhloxInvoice aModel)
  {
    // BG-33 PAYMENT TERMS with BG-35 EARLY PAYMENT DISCOUNT and BG-36 LATE PAYMENT PENALTY
    for (final PaymentTermsType aPT : aPaymentTerms)
    {
      final PhloxPaymentTerms aOut = new PhloxPaymentTerms ();
      // BT-20
      if (!aPT.getNote ().isEmpty ())
        aOut.setText (_joinNotes (aPT.getNote ()));

      // BG-35
      final PhloxEarlyPaymentDiscount aDiscount = new PhloxEarlyPaymentDiscount ();
      if (aPT.getSettlementPeriod () != null)
        aDiscount.setEndDate (_d (aPT.getSettlementPeriod ().getEndDateValue ()));
      aDiscount.setPercentage (aPT.getSettlementDiscountPercentValue ());
      if (aPT.getSettlementDiscountAmount () != null)
        aDiscount.setAmount (aPT.getSettlementDiscountAmount ().getValue ());
      if (aDiscount.getEndDate () != null || aDiscount.getPercentage () != null || aDiscount.getAmount () != null)
        aOut.earlyDiscounts ().add (aDiscount);

      // BG-36
      final PhloxLatePaymentPenalty aPenalty = new PhloxLatePaymentPenalty ();
      if (aPT.getPenaltyPeriod () != null)
        aPenalty.setStartDate (_d (aPT.getPenaltyPeriod ().getStartDateValue ()));
      if (aPT.getPenaltyInterestRate () != null)
        aPenalty.setYearlyInterestPercentage (aPT.getPenaltyInterestRate ().getInterestRatePercentValue ());
      if (aPT.getPenaltyAmount () != null)
        aPenalty.setAmount (aPT.getPenaltyAmount ().getValue ());
      if (aPenalty.getStartDate () != null ||
          aPenalty.getYearlyInterestPercentage () != null ||
          aPenalty.getAmount () != null)
        aOut.latePenalties ().add (aPenalty);

      if (aOut.getText () != null || !aOut.earlyDiscounts ().isEmpty () || !aOut.latePenalties ().isEmpty ())
        aModel.paymentTerms ().add (aOut);
    }
  }

  private static void _readThirdPartyCharge (@Nullable final AmountType aAmount,
                                             @Nullable final ItemType aItem,
                                             @NonNull final PhloxInvoice aModel)
  {
    // BG-34 — BT-179 charge amount and BT-180 charges specification
    final PhloxThirdPartyCharge aOut = new PhloxThirdPartyCharge ();
    if (aAmount != null)
      aOut.setAmount (aAmount.getValue ());
    if (aItem != null && !aItem.getDescription ().isEmpty ())
      aOut.setSpecification (_str (aItem.getDescription ().get (0).getValue ()));
    if (aOut.getAmount () != null || aOut.getSpecification () != null)
      aModel.getDocumentTotals ().thirdPartyCharges ().add (aOut);
  }

  private static void _readPrecedingInvoiceRefs (@NonNull final List <BillingReferenceType> aRefs,
                                                 @NonNull final PhloxInvoice aModel)
  {
    // BG-3 — BT-25, BT-26 and BT-202
    for (final BillingReferenceType aBR : aRefs)
      if (aBR.getInvoiceDocumentReference () != null)
      {
        final DocumentReferenceType aDR = aBR.getInvoiceDocumentReference ();
        aModel.precedingInvoiceRefs ()
              .add (new PhloxPrecedingInvoiceRef ().setNumber (_str (aDR.getIDValue ()))
                                                   .setIssueDate (_d (aDR.getIssueDateValue ()))
                                                   .setTypeCode (_str (aDR.getDocumentTypeCodeValue ())));
      }
  }

  private static void _readAdditionalDocumentReferences (@NonNull final List <DocumentReferenceType> aRefs,
                                                         @NonNull final PhloxInvoice aModel)
  {
    // BT-18 (type code 130) and BG-24 (type code 916)
    for (final DocumentReferenceType aRef : aRefs)
      if ("130".equals (aRef.getDocumentTypeCodeValue ()))
      {
        // BT-18: Invoiced object identifier
        if (aRef.getID () != null)
          aModel.setInvoicedObjectIdentifier (new PhloxIdentifier (aRef.getIDValue (), aRef.getID ().getSchemeID ()));
      }
      else
        aModel.supportingDocuments ().add (_readSupportingDocument (aRef));
  }

  private static void _readInvoicePeriod (@NonNull final List <PeriodType> aPeriods,
                                          @NonNull final PhloxInvoice aModel)
  {
    // BG-14
    if (!aPeriods.isEmpty ())
    {
      final PeriodType aPeriod = aPeriods.get (0);
      final PhloxInvoicingPeriod aIP = new PhloxInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      // BT-8 — VAT point date code (lives in InvoicePeriod for UBL)
      aModel.setVatPointDateCode (_str (aPeriod.getDescriptionCode ().isEmpty () ? null
                                                                                 : aPeriod.getDescriptionCode ()
                                                                                          .get (0)
                                                                                          .getValue ()));
      if (!aIP.isEmpty ())
        aModel.setInvoicingPeriod (aIP);
    }
  }

  private static void _readDocumentAllowanceCharges (@NonNull final List <AllowanceChargeType> aACs,
                                                     @NonNull final PhloxInvoice aModel)
  {
    // BG-20 / BG-21
    for (final AllowanceChargeType aAC : aACs)
      if (aAC.getChargeIndicator () != null && aAC.getChargeIndicator ().isValue ())
        aModel.documentCharges ().add (_readDocumentCharge (aAC));
      else
        aModel.documentAllowances ().add (_readDocumentAllowance (aAC));
  }

  private static void _readHeaderDelivery (@NonNull final List <DeliveryType> aDeliveries,
                                           @NonNull final PhloxInvoice aModel)
  {
    // BG-13 + BG-15
    if (!aDeliveries.isEmpty ())
      aModel.setDeliveryInformation (_readDelivery (aDeliveries.get (0)));
  }

  private static void _readCommonReferences (@Nullable final OrderReferenceType aOrder,
                                             @NonNull final List <DocumentReferenceType> aContract,
                                             @NonNull final List <DocumentReferenceType> aReceipt,
                                             @NonNull final List <DocumentReferenceType> aDespatch,
                                             @NonNull final List <DocumentReferenceType> aDeliveryNote,
                                             @NonNull final List <DocumentReferenceType> aOriginator,
                                             @NonNull final List <ProjectReferenceType> aProject,
                                             @NonNull final PhloxInvoice aModel)
  {
    // BT-13 / BT-14
    if (aOrder != null)
    {
      aModel.setPurchaseOrderReference (_str (aOrder.getIDValue ()));
      aModel.setSalesOrderReference (_str (aOrder.getSalesOrderIDValue ()));
    }
    // BT-12 — contract reference
    if (!aContract.isEmpty ())
      aModel.setContractReference (_str (aContract.get (0).getIDValue ()));
    // BT-15 — receiving advice
    if (!aReceipt.isEmpty ())
      aModel.setReceivingAdviceReference (_str (aReceipt.get (0).getIDValue ()));
    // BT-16 — despatch advice
    if (!aDespatch.isEmpty ())
      aModel.setDespatchAdviceReference (_str (aDespatch.get (0).getIDValue ()));
    // BT-197 — delivery note reference
    if (!aDeliveryNote.isEmpty ())
      aModel.setDeliveryNoteReference (_str (aDeliveryNote.get (0).getIDValue ()));
    // BT-17 — tender or lot
    if (!aOriginator.isEmpty ())
      aModel.setTenderOrLotReference (_str (aOriginator.get (0).getIDValue ()));
    // BT-11 — project reference
    if (!aProject.isEmpty ())
      aModel.setProjectReference (_str (aProject.get (0).getIDValue ()));
  }

  private static void _readParties (@Nullable final SupplierPartyType aSupplier,
                                    @Nullable final CustomerPartyType aCustomer,
                                    @Nullable final PartyType aPayee,
                                    @Nullable final PartyType aTaxRepresentative,
                                    @NonNull final PhloxInvoice aModel)
  {
    // BG-4 + BG-5 + BG-6 — Seller
    if (aSupplier != null)
      _readSeller (aSupplier, aModel.getSeller ());
    // BG-7 + BG-8 + BG-9 — Buyer
    if (aCustomer != null)
      _readBuyer (aCustomer, aModel.getBuyer ());
    // BG-10 — Payee
    if (aPayee != null)
      aModel.setPayee (_readPayee (aPayee));
    // BG-11 + BG-12 — Seller tax representative
    if (aTaxRepresentative != null)
      aModel.setSellerTaxRepresentative (_readSellerTaxRepresentative (aTaxRepresentative));
  }

  /**
   * Read an already-parsed UBL 2.5 Invoice into a {@link PhloxInvoice}.
   *
   * @param aInvoice
   *        The JAXB invoice. Never {@code null}.
   * @param aErrors
   *        The error list to fill with warnings. Never {@code null}.
   * @return The populated model.
   */
  @NonNull
  public PhloxInvoice readInvoice (@NonNull final InvoiceType aInvoice, @NonNull final ErrorList aErrors)
  {
    final PhloxInvoice aModel = new PhloxInvoice ();
    aModel.setKind (EPhloxDocumentKind.INVOICE);
    aModel.setEdition (EEN16931Edition.EN2026);

    // BT-1
    aModel.setNumber (_str (aInvoice.getIDValue ()));
    // BT-2 + BT-166
    aModel.setIssueDate (_d (aInvoice.getIssueDateValue ()));
    _readIssueTime (aInvoice.getIssueTimeValue (), aModel, aErrors);
    // BT-3
    aModel.setTypeCode (_str (aInvoice.getInvoiceTypeCodeValue ()));
    // BG-1 — notes
    _readAnnotations (aInvoice.getAnnotation (), aModel);
    // BT-5 / BT-6 / BT-167
    aModel.setCurrency (aInvoice.getDocumentCurrencyCodeValue ());
    aModel.setVatAccountingCurrency (aInvoice.getTaxCurrencyCodeValue ());
    _readExchangeRate (aInvoice.getTaxExchangeRate (), aModel);
    // BT-7
    aModel.setVatPointDate (_d (aInvoice.getTaxPointDateValue ()));
    // BT-9 (Invoice: header DueDate)
    aModel.setPaymentDueDate (_d (aInvoice.getDueDateValue ()));
    // BT-10
    _readBuyerReferences (aInvoice.getBuyerAssignedReference (), aModel);
    // BT-19
    aModel.setBuyerAccountingReference (_str (aInvoice.getAccountingCostValue ()));
    // BG-2
    aModel.getProcessControl ().setBusinessProcessType (_str (aInvoice.getProfileIDValue ()));
    aModel.getProcessControl ().setSpecificationIdentifier (_str (aInvoice.getCustomizationIDValue ()));
    // BT-11 to BT-17, BT-197
    _readCommonReferences (aInvoice.getOrderReference (),
                           aInvoice.getContractDocumentReference (),
                           aInvoice.getReceiptDocumentReference (),
                           aInvoice.getDespatchDocumentReference (),
                           aInvoice.getDeliveryNoteDocumentReference (),
                           aInvoice.getOriginatorDocumentReference (),
                           aInvoice.getProjectReference (),
                           aModel);
    // BT-18 + BG-24
    _readAdditionalDocumentReferences (aInvoice.getAdditionalDocumentReference (), aModel);
    // BG-3
    _readPrecedingInvoiceRefs (aInvoice.getBillingReference (), aModel);
    // BG-4 to BG-12
    _readParties (aInvoice.getAccountingSupplierParty (),
                  aInvoice.getAccountingCustomerParty (),
                  aInvoice.getPayeeParty (),
                  aInvoice.getTaxRepresentativeParty (),
                  aModel);
    // BG-13 + BG-15 — Delivery
    _readHeaderDelivery (aInvoice.getDelivery (), aModel);
    // BG-14 — Invoicing period
    _readInvoicePeriod (aInvoice.getInvoicePeriod (), aModel);

    // BG-16/17/18/19 — Payment instructions
    if (!aInvoice.getPaymentMeans ().isEmpty ())
      aModel.setPaymentInstructions (_readPaymentInstructions (aInvoice.getPaymentMeans (),
                                                               aInvoice.getAccountingSupplierParty (),
                                                               aInvoice.getPayeeParty ()));
    // BG-33 / BG-35 / BG-36 — payment terms
    _readPaymentTerms (aInvoice.getPaymentTerms (), aModel);

    // BG-20 / BG-21 — document-level allowances and charges
    _readDocumentAllowanceCharges (aInvoice.getAllowanceCharge (), aModel);

    // BG-22 — totals
    if (aInvoice.getLegalMonetaryTotal () != null)
      _readTotals (aInvoice.getLegalMonetaryTotal (), aModel);
    // BG-34 — charges on behalf of a third party
    for (final InvoiceLineType aCollectionLine : aInvoice.getCollectionInvoiceLine ())
      _readThirdPartyCharge (aCollectionLine.getTaxInclusiveLineExtensionAmount (), aCollectionLine.getItem (), aModel);

    // BG-23 — VAT breakdowns
    _readTaxTotals (aInvoice.getTaxTotal (), aModel);

    // BG-25 — lines
    for (final InvoiceLineType aLine : aInvoice.getInvoiceLine ())
      aModel.lines ().add (_readInvoiceLine (aLine));

    return aModel;
  }

  /**
   * Read an already-parsed UBL 2.5 CreditNote into a {@link PhloxInvoice}.
   *
   * @param aCreditNote
   *        The JAXB credit note. Never {@code null}.
   * @param aErrors
   *        The error list to fill with warnings. Never {@code null}.
   * @return The populated model.
   */
  @NonNull
  public PhloxInvoice readCreditNote (@NonNull final CreditNoteType aCreditNote, @NonNull final ErrorList aErrors)
  {
    final PhloxInvoice aModel = new PhloxInvoice ();
    aModel.setKind (EPhloxDocumentKind.CREDIT_NOTE);
    aModel.setEdition (EEN16931Edition.EN2026);

    aModel.setNumber (_str (aCreditNote.getIDValue ()));
    aModel.setIssueDate (_d (aCreditNote.getIssueDateValue ()));
    _readIssueTime (aCreditNote.getIssueTimeValue (), aModel, aErrors);
    aModel.setTypeCode (_str (aCreditNote.getCreditNoteTypeCodeValue ()));
    _readAnnotations (aCreditNote.getAnnotation (), aModel);
    aModel.setCurrency (aCreditNote.getDocumentCurrencyCodeValue ());
    aModel.setVatAccountingCurrency (aCreditNote.getTaxCurrencyCodeValue ());
    _readExchangeRate (aCreditNote.getTaxExchangeRate (), aModel);
    aModel.setVatPointDate (_d (aCreditNote.getTaxPointDateValue ()));
    // BT-9 in CN: cbc:DueDate since UBL 2.2, same as the invoice
    aModel.setPaymentDueDate (_d (aCreditNote.getDueDateValue ()));
    _readBuyerReferences (aCreditNote.getBuyerAssignedReference (), aModel);
    aModel.setBuyerAccountingReference (_str (aCreditNote.getAccountingCostValue ()));
    aModel.getProcessControl ().setBusinessProcessType (_str (aCreditNote.getProfileIDValue ()));
    aModel.getProcessControl ().setSpecificationIdentifier (_str (aCreditNote.getCustomizationIDValue ()));
    _readCommonReferences (aCreditNote.getOrderReference (),
                           aCreditNote.getContractDocumentReference (),
                           aCreditNote.getReceiptDocumentReference (),
                           aCreditNote.getDespatchDocumentReference (),
                           aCreditNote.getDeliveryNoteDocumentReference (),
                           aCreditNote.getOriginatorDocumentReference (),
                           aCreditNote.getProjectReference (),
                           aModel);
    _readAdditionalDocumentReferences (aCreditNote.getAdditionalDocumentReference (), aModel);
    _readPrecedingInvoiceRefs (aCreditNote.getBillingReference (), aModel);
    _readParties (aCreditNote.getAccountingSupplierParty (),
                  aCreditNote.getAccountingCustomerParty (),
                  aCreditNote.getPayeeParty (),
                  aCreditNote.getTaxRepresentativeParty (),
                  aModel);
    _readHeaderDelivery (aCreditNote.getDelivery (), aModel);
    _readInvoicePeriod (aCreditNote.getInvoicePeriod (), aModel);

    if (!aCreditNote.getPaymentMeans ().isEmpty ())
      aModel.setPaymentInstructions (_readPaymentInstructions (aCreditNote.getPaymentMeans (),
                                                               aCreditNote.getAccountingSupplierParty (),
                                                               aCreditNote.getPayeeParty ()));
    _readPaymentTerms (aCreditNote.getPaymentTerms (), aModel);

    _readDocumentAllowanceCharges (aCreditNote.getAllowanceCharge (), aModel);

    if (aCreditNote.getLegalMonetaryTotal () != null)
      _readTotals (aCreditNote.getLegalMonetaryTotal (), aModel);
    for (final CreditNoteLineType aCollectionLine : aCreditNote.getCollectionCreditNoteLine ())
      _readThirdPartyCharge (aCollectionLine.getTaxInclusiveLineExtensionAmount (), aCollectionLine.getItem (), aModel);
    _readTaxTotals (aCreditNote.getTaxTotal (), aModel);

    for (final CreditNoteLineType aLine : aCreditNote.getCreditNoteLine ())
      aModel.lines ().add (_readCreditNoteLine (aLine));

    return aModel;
  }

  // ===== Internal walkers =====

  private static void _readSeller (@NonNull final SupplierPartyType aSP, @NonNull final PhloxSeller aSeller)
  {
    if (aSP.getParty () == null)
      return;
    final PartyType aParty = aSP.getParty ();
    _readPartyCommon (aParty, aSeller);
  }

  private static void _readBuyer (@NonNull final CustomerPartyType aCP, @NonNull final PhloxBuyer aBuyer)
  {
    if (aCP.getParty () == null)
      return;
    final PartyType aParty = aCP.getParty ();
    // BT-44
    if (!aParty.getPartyLegalEntity ().isEmpty ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);
      aBuyer.setName (_str (aLE.getRegistrationNameValue ()));
      if (aLE.getCompanyID () != null)
        aBuyer.setLegalRegistration (new PhloxIdentifier (aLE.getCompanyIDValue (),
                                                          aLE.getCompanyID ().getSchemeID ()));
    }
    // BT-45 — trading name
    if (!aParty.getPartyName ().isEmpty ())
    {
      final PartyNameType aPN = aParty.getPartyName ().get (0);
      aBuyer.setTradingName (_str (aPN.getNameValue ()));
    }
    // BT-46 — identifiers
    for (final PartyIdentificationType aPI : aParty.getPartyIdentification ())
      if (aPI.getID () != null)
        aBuyer.identifiers ().add (new PhloxIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    // BT-48 — VAT
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null && "VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
        aBuyer.setVatIdentifier (_str (aPTS.getCompanyIDValue ()));
    // BT-49
    if (aParty.getEndpointID () != null)
      aBuyer.setElectronicAddress (new PhloxIdentifier (aParty.getEndpointIDValue (),
                                                        aParty.getEndpointID ().getSchemeID ()));
    // BG-8
    if (aParty.getPostalAddress () != null)
      aBuyer.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    // BG-9
    if (aParty.getContact () != null)
      aBuyer.setContact (_readContact (aParty.getContact ()));
  }

  private static void _readPartyCommon (@NonNull final PartyType aParty, @NonNull final PhloxSeller aSeller)
  {
    // BT-27
    if (!aParty.getPartyLegalEntity ().isEmpty ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);
      aSeller.setName (_str (aLE.getRegistrationNameValue ()));
      if (aLE.getCompanyID () != null)
        aSeller.setLegalRegistration (new PhloxIdentifier (aLE.getCompanyIDValue (),
                                                           aLE.getCompanyID ().getSchemeID ()));
      // BT-33
      aSeller.setAdditionalLegalInfo (_firstText (aLE.getCompanyLegalForm ()));
    }
    // BT-28
    if (!aParty.getPartyName ().isEmpty ())
      aSeller.setTradingName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    // BT-29
    for (final PartyIdentificationType aPI : aParty.getPartyIdentification ())
      if (aPI.getID () != null)
        aSeller.identifiers ().add (new PhloxIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    // BT-31 (tax scheme "VAT") / BT-32 + BT-32-1 (tax scheme "LOC")
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null)
      {
        if ("VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
          aSeller.setVatIdentifier (_str (aPTS.getCompanyIDValue ()));
        else
        {
          aSeller.setTaxRegistration (_str (aPTS.getCompanyIDValue ()));
          if (aPTS.getCompanyID () != null)
            aSeller.setTaxRegistrationSchemeID (_str (aPTS.getCompanyID ().getSchemeID ()));
        }
      }
    // BT-34
    if (aParty.getEndpointID () != null)
      aSeller.setElectronicAddress (new PhloxIdentifier (aParty.getEndpointIDValue (),
                                                         aParty.getEndpointID ().getSchemeID ()));
    // BG-5
    if (aParty.getPostalAddress () != null)
      aSeller.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    // BG-6
    if (aParty.getContact () != null)
      aSeller.setContact (_readContact (aParty.getContact ()));
  }

  @NonNull
  private static PhloxPostalAddress _readAddress (@NonNull final AddressType aAddr)
  {
    final PhloxPostalAddress aOut = new PhloxPostalAddress ();
    aOut.setLine1 (_firstText (aAddr.getStreetName ()));
    aOut.setLine2 (_firstText (aAddr.getAdditionalStreetName ()));
    if (!aAddr.getAddressLine ().isEmpty ())
    {
      final AddressLineType aAL = aAddr.getAddressLine ().get (0);
      aOut.setLine3 (_firstText (aAL.getLine ()));
    }
    aOut.setCity (_firstText (aAddr.getCityName ()));
    aOut.setPostCode (_firstText (aAddr.getPostalZone ()));
    aOut.setCountrySubdivision (_firstText (aAddr.getCountrySubentity ()));
    final CountryType aCountry = aAddr.getCountry ();
    if (aCountry != null)
      aOut.setCountryCode (_str (aCountry.getIdentificationCodeValue ()));
    return aOut;
  }

  @NonNull
  private static PhloxContact _readContact (@NonNull final ContactType aContact)
  {
    final PhloxContact aOut = new PhloxContact ();
    aOut.setName (_str (aContact.getNameValue ()));
    aOut.setTelephone (_str (aContact.getTelephoneValue ()));
    aOut.setEmail (_str (aContact.getElectronicMailValue ()));
    return aOut;
  }

  @Nullable
  private static PhloxPayee _readPayee (@NonNull final PartyType aParty)
  {
    final PhloxPayee aOut = new PhloxPayee ();
    if (!aParty.getPartyName ().isEmpty ())
      aOut.setName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    if (!aParty.getPartyIdentification ().isEmpty () && aParty.getPartyIdentification ().get (0).getID () != null)
    {
      final PartyIdentificationType aPI = aParty.getPartyIdentification ().get (0);
      aOut.setIdentifier (new PhloxIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    }
    if (!aParty.getPartyLegalEntity ().isEmpty ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);
      if (aLE.getCompanyID () != null)
        aOut.setLegalRegistration (new PhloxIdentifier (aLE.getCompanyIDValue (), aLE.getCompanyID ().getSchemeID ()));
    }
    return aOut;
  }

  @NonNull
  private static PhloxSellerTaxRepresentative _readSellerTaxRepresentative (@NonNull final PartyType aParty)
  {
    final PhloxSellerTaxRepresentative aOut = new PhloxSellerTaxRepresentative ();
    if (!aParty.getPartyName ().isEmpty ())
      aOut.setName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null && "VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
        aOut.setVatIdentifier (_str (aPTS.getCompanyIDValue ()));
    if (aParty.getPostalAddress () != null)
      aOut.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    return aOut;
  }

  @NonNull
  private static PhloxDeliveryInformation _readDelivery (@NonNull final DeliveryType aDelivery)
  {
    final PhloxDeliveryInformation aOut = new PhloxDeliveryInformation ();
    if (aDelivery.getDeliveryParty () != null && !aDelivery.getDeliveryParty ().getPartyName ().isEmpty ())
      aOut.setPartyName (_str (aDelivery.getDeliveryParty ().getPartyName ().get (0).getNameValue ()));
    if (aDelivery.getDeliveryLocation () != null)
    {
      if (aDelivery.getDeliveryLocation ().getID () != null)
        aOut.setLocationIdentifier (new PhloxIdentifier (aDelivery.getDeliveryLocation ().getIDValue (),
                                                         aDelivery.getDeliveryLocation ().getID ().getSchemeID ()));
      if (aDelivery.getDeliveryLocation ().getAddress () != null)
        aOut.setPostalAddress (_readAddress (aDelivery.getDeliveryLocation ().getAddress ()));
    }
    aOut.setActualDeliveryDate (_d (aDelivery.getActualDeliveryDateValue ()));
    return aOut;
  }

  @NonNull
  private static PhloxPaymentInstructions _readPaymentInstructions (@NonNull final List <PaymentMeansType> aPaymentMeans,
                                                                    @Nullable final SupplierPartyType aSupplier,
                                                                    @Nullable final PartyType aPayee)
  {
    final PhloxPaymentInstructions aOut = new PhloxPaymentInstructions ();
    final PaymentMeansType aPM = aPaymentMeans.get (0);
    aOut.setPaymentMeansCode (_str (aPM.getPaymentMeansCodeValue ()));
    if (aPM.getPaymentMeansCode () != null)
      aOut.setPaymentMeansText (aPM.getPaymentMeansCode ().getName ());
    if (!aPM.getPaymentID ().isEmpty ())
      aOut.setRemittanceInformation (_str (aPM.getPaymentID ().get (0).getValue ()));

    // BG-17 — credit transfers across ALL payment means
    for (final PaymentMeansType aOnePM : aPaymentMeans)
      if (aOnePM.getPayeeFinancialAccount () != null)
        aOut.creditTransfers ().add (_readCreditTransfer (aOnePM.getPayeeFinancialAccount ()));

    // BG-18 — payment card
    if (!aPM.getCardAccount ().isEmpty ())
    {
      final CardAccountType aCardAccount = aPM.getCardAccount ().get (0);
      final PhloxPaymentCard aCard = new PhloxPaymentCard ();
      aCard.setPan (_str (aCardAccount.getPrimaryAccountNumberIDValue ()));
      aCard.setHolderName (_str (aCardAccount.getHolderNameValue ()));
      aOut.setPaymentCard (aCard);
    }

    // BG-19 — direct debit
    if (aPM.getPaymentMandate () != null)
    {
      final PaymentMandateType aMandate = aPM.getPaymentMandate ();
      final PhloxDirectDebit aDD = new PhloxDirectDebit ();
      aDD.setMandateReferenceID (_str (aMandate.getIDValue ()));
      if (aMandate.getPayerFinancialAccount () != null)
      {
        final FinancialAccountType aPayerAccount = aMandate.getPayerFinancialAccount ();
        aDD.setDebitedAccountID (_str (aPayerAccount.getIDValue ()));
        // BT-216 — debited account name
        aDD.setDebitedAccountName (_str (aPayerAccount.getNameValue ()));
        // BT-215 — debited account payment service provider identifier
        if (aPayerAccount.getFinancialInstitutionBranch () != null)
          aDD.setDebitedAccountProviderID (_str (aPayerAccount.getFinancialInstitutionBranch ().getIDValue ()));
      }
      // BT-90 — bank assigned creditor id (UBL: from supplier/payee PartyIdentification with SEPA
      // scheme)
      aDD.setBankAssignedCreditorID (_findSepaCreditorID (aSupplier, aPayee));
      aOut.setDirectDebit (aDD);
    }
    return aOut;
  }

  @Nullable
  private static String _findSepaCreditorID (@Nullable final SupplierPartyType aSupplier,
                                             @Nullable final PartyType aPayee)
  {
    if (aPayee != null)
      for (final PartyIdentificationType aPI : aPayee.getPartyIdentification ())
        if (aPI.getID () != null && "SEPA".equals (aPI.getID ().getSchemeID ()))
          return aPI.getIDValue ();
    if (aSupplier != null && aSupplier.getParty () != null)
      for (final PartyIdentificationType aPI : aSupplier.getParty ().getPartyIdentification ())
        if (aPI.getID () != null && "SEPA".equals (aPI.getID ().getSchemeID ()))
          return aPI.getIDValue ();
    return null;
  }

  @NonNull
  private static PhloxCreditTransfer _readCreditTransfer (@NonNull final FinancialAccountType aAcc)
  {
    final PhloxCreditTransfer aOut = new PhloxCreditTransfer ();
    aOut.setAccountIdentifier (_str (aAcc.getIDValue ()));
    aOut.setAccountName (_str (aAcc.getNameValue ()));
    final BranchType aBranch = aAcc.getFinancialInstitutionBranch ();
    if (aBranch != null)
      aOut.setServiceProviderID (_str (aBranch.getIDValue ()));
    return aOut;
  }

  /** @return The first exemption reason text of the tax category or {@code null}. */
  @Nullable
  private static String _firstExemptionReason (@NonNull final TaxCategoryType aTC)
  {
    return aTC.getTaxExemptionReason ().isEmpty () ? null : _str (aTC.getTaxExemptionReason ().get (0).getValue ());
  }

  /**
   * @return {@code true} if the reason code of the allowance/charge is a non-VAT tax code (UNTDID
   *         5153: BT-177 respectively BT-193) instead of an allowance/charge reason code.
   */
  private static boolean _isNonVatTaxCode (@NonNull final AllowanceChargeType aAC)
  {
    return aAC.getAllowanceChargeReasonCode () != null &&
           NON_VAT_TAX_CODE_LIST_ID.equals (aAC.getAllowanceChargeReasonCode ().getListID ());
  }

  @NonNull
  private static PhloxDocumentAllowance _readDocumentAllowance (@NonNull final AllowanceChargeType aAC)
  {
    final PhloxDocumentAllowance aOut = new PhloxDocumentAllowance ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ()
                                                                                 .get (0)
                                                                                 .getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    if (aAC.getTaxCategory () != null && !aAC.getTaxCategory ().isEmpty ())
    {
      final TaxCategoryType aTC = aAC.getTaxCategory ().get (0);
      aOut.setVatCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setVatRate (aTC.getPercent ().getValue ());
      // BT-173
      aOut.setExemptionReasonText (_firstExemptionReason (aTC));
      // BT-174
      aOut.setExemptionReasonCode (_str (aTC.getTaxExemptionReasonCodeValue ()));
      // BT-213
      aOut.setGoodsServicesCode (_str (aTC.getSupplyTypeCodeValue ()));
    }
    return aOut;
  }

  @NonNull
  private static PhloxDocumentCharge _readDocumentCharge (@NonNull final AllowanceChargeType aAC)
  {
    final PhloxDocumentCharge aOut = new PhloxDocumentCharge ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ()
                                                                                 .get (0)
                                                                                 .getValue ()));
    // BT-105 (UNTDID 7161) or BT-177 + BT-177-1 (UNTDID 5153, told apart by @listID)
    if (_isNonVatTaxCode (aAC))
    {
      aOut.setNonVatTaxCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
      aOut.setNonVatTaxCodeListID (_str (aAC.getAllowanceChargeReasonCode ().getListID ()));
    }
    else
      aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    if (aAC.getTaxCategory () != null && !aAC.getTaxCategory ().isEmpty ())
    {
      final TaxCategoryType aTC = aAC.getTaxCategory ().get (0);
      aOut.setVatCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setVatRate (aTC.getPercent ().getValue ());
      // BT-175
      aOut.setExemptionReasonText (_firstExemptionReason (aTC));
      // BT-176
      aOut.setExemptionReasonCode (_str (aTC.getTaxExemptionReasonCodeValue ()));
      // BT-214
      aOut.setGoodsServicesCode (_str (aTC.getSupplyTypeCodeValue ()));
    }
    return aOut;
  }

  private static void _readTotals (@NonNull final MonetaryTotalType aLMT, @NonNull final PhloxInvoice aModel)
  {
    if (aLMT.getLineExtensionAmount () != null)
      aModel.getDocumentTotals ().setLineNetTotal (aLMT.getLineExtensionAmount ().getValue ());
    if (aLMT.getAllowanceTotalAmount () != null)
      aModel.getDocumentTotals ().setAllowanceTotal (aLMT.getAllowanceTotalAmount ().getValue ());
    if (aLMT.getChargeTotalAmount () != null)
      aModel.getDocumentTotals ().setChargeTotal (aLMT.getChargeTotalAmount ().getValue ());
    if (aLMT.getTaxExclusiveAmount () != null)
      aModel.getDocumentTotals ().setTaxExclusiveTotal (aLMT.getTaxExclusiveAmount ().getValue ());
    if (aLMT.getTaxInclusiveAmount () != null)
      aModel.getDocumentTotals ().setTaxInclusiveTotal (aLMT.getTaxInclusiveAmount ().getValue ());
    if (aLMT.getPrepaidAmount () != null)
      aModel.getDocumentTotals ().setPrepaidAmount (aLMT.getPrepaidAmount ().getValue ());
    if (aLMT.getPayableRoundingAmount () != null)
      aModel.getDocumentTotals ().setRoundingAmount (aLMT.getPayableRoundingAmount ().getValue ());
    if (aLMT.getPayableAmount () != null)
      aModel.getDocumentTotals ().setPayableAmount (aLMT.getPayableAmount ().getValue ());
  }

  private static void _readTaxTotals (@NonNull final List <TaxTotalType> aTaxTotals, @NonNull final PhloxInvoice aModel)
  {
    final String sCurrency = aModel.getCurrency ();
    final String sAccountingCurrency = aModel.getVatAccountingCurrency ();
    boolean bMainTaxTotalSet = false;
    for (final TaxTotalType aTT : aTaxTotals)
    {
      if (aTT.getTaxAmount () != null)
      {
        final BigDecimal aAmount = aTT.getTaxAmount ().getValue ();
        final String sCurrencyID = aTT.getTaxAmount ().getCurrencyID ();
        if (sAccountingCurrency != null && sAccountingCurrency.equals (sCurrencyID))
          aModel.getDocumentTotals ().setTaxTotalAccountingCurrency (aAmount);
        else
          if (!bMainTaxTotalSet)
          {
            aModel.getDocumentTotals ().setTaxTotal (aAmount);
            bMainTaxTotalSet = true;
          }
      }
      for (final TaxSubtotalType aSub : aTT.getTaxSubtotal ())
        aModel.vatBreakdowns ().add (_readVatBreakdown (aSub, sCurrency));
    }
  }

  @NonNull
  private static PhloxVatBreakdown _readVatBreakdown (@NonNull final TaxSubtotalType aSub,
                                                         @Nullable final String sInvoiceCurrency)
  {
    final PhloxVatBreakdown aOut = new PhloxVatBreakdown ();
    if (aSub.getTaxableAmount () != null)
      aOut.setTaxableAmount (aSub.getTaxableAmount ().getValue ());
    if (aSub.getTaxAmount () != null)
      aOut.setTaxAmount (aSub.getTaxAmount ().getValue ());
    // BT-184 — only meaningful if it differs from the invoice currency BT-5
    if (aSub.getTaxAmount () != null)
    {
      final String sCurrencyID = _str (aSub.getTaxAmount ().getCurrencyID ());
      if (sCurrencyID != null && !sCurrencyID.equals (sInvoiceCurrency))
        aOut.setCurrency (sCurrencyID);
    }
    if (aSub.getTaxCategory () != null)
    {
      final TaxCategoryType aTC = aSub.getTaxCategory ();
      aOut.setCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setRate (aTC.getPercent ().getValue ());
      aOut.setExemptionReason (aTC.getTaxExemptionReason ().isEmpty () ? null : _str (aTC.getTaxExemptionReason ()
                                                                                         .get (0)
                                                                                         .getValue ()));
      aOut.setExemptionReasonCode (_str (aTC.getTaxExemptionReasonCodeValue ()));
      // BT-210
      aOut.setGoodsServicesCode (_str (aTC.getSupplyTypeCodeValue ()));
    }
    return aOut;
  }

  @NonNull
  private static PhloxSupportingDocument _readSupportingDocument (@NonNull final DocumentReferenceType aRef)
  {
    final PhloxSupportingDocument aOut = new PhloxSupportingDocument ();
    aOut.setReference (_str (aRef.getIDValue ()));
    aOut.setDescription (_str (aRef.getDocumentDescriptionAtIndex (0) != null ? aRef.getDocumentDescriptionAtIndex (0)
                                                                                    .getValue () : null));
    if (aRef.getAttachment () != null)
    {
      final AttachmentType aAtt = aRef.getAttachment ();
      if (aAtt.getExternalReference () != null)
      {
        final ExternalReferenceType aER = aAtt.getExternalReference ();
        aOut.setExternalLocation (_str (aER.getURIValue ()));
      }
      if (aAtt.getEmbeddedDocumentBinaryObject () != null)
      {
        final PhloxAttachment aBin = new PhloxAttachment ();
        aBin.setBinary (aAtt.getEmbeddedDocumentBinaryObject ().getValue ());
        aBin.setMimeCode (aAtt.getEmbeddedDocumentBinaryObject ().getMimeCode ());
        aBin.setFilename (aAtt.getEmbeddedDocumentBinaryObject ().getFilename ());
        aOut.setAttachment (aBin);
      }
    }
    return aOut;
  }

  private static void _readLineReferences (@NonNull final List <OrderLineReferenceType> aOrderLineRefs,
                                            @NonNull final List <LineReferenceType> aDespatchLineRefs,
                                            @NonNull final List <LineReferenceType> aReceiptLineRefs,
                                            @NonNull final List <DeliveryType> aDeliveries,
                                            @NonNull final PhloxInvoiceLine aOut)
  {
    // BT-132 + BT-188 + BT-200 + BT-201 — order line reference
    if (!aOrderLineRefs.isEmpty ())
    {
      final OrderLineReferenceType aOLR = aOrderLineRefs.get (0);
      aOut.setPurchaseOrderLineRef (_str (aOLR.getLineIDValue ()));
      aOut.setSalesOrderLineReference (_str (aOLR.getSalesOrderLineIDValue ()));
      if (aOLR.getOrderReference () != null)
      {
        aOut.setPurchaseOrderReference (_str (aOLR.getOrderReference ().getIDValue ()));
        aOut.setSalesOrderReference (_str (aOLR.getOrderReference ().getSalesOrderIDValue ()));
      }
    }
    // BT-189 + BT-190 — despatch advice reference
    if (!aDespatchLineRefs.isEmpty ())
    {
      final LineReferenceType aLR = aDespatchLineRefs.get (0);
      aOut.setDespatchAdviceLineReference (_str (aLR.getLineIDValue ()));
      if (aLR.getDocumentReference () != null)
        aOut.setDespatchAdviceReference (_str (aLR.getDocumentReference ().getIDValue ()));
    }
    // BT-191 + BT-192 — receiving advice reference
    if (!aReceiptLineRefs.isEmpty ())
    {
      final LineReferenceType aLR = aReceiptLineRefs.get (0);
      aOut.setReceivingAdviceLineReference (_str (aLR.getLineIDValue ()));
      if (aLR.getDocumentReference () != null)
        aOut.setReceivingAdviceReference (_str (aLR.getDocumentReference ().getIDValue ()));
    }
    if (!aDeliveries.isEmpty ())
    {
      final DeliveryType aDelivery = aDeliveries.get (0);
      // BT-198 + BT-199 — delivery note reference
      if (!aDelivery.getDeliveryNoteDocumentReference ().isEmpty ())
        aOut.setDeliveryNoteReference (_str (aDelivery.getDeliveryNoteDocumentReference ().get (0).getIDValue ()));
      if (!aDelivery.getDeliveryNoteLineReference ().isEmpty ())
        aOut.setDeliveryNoteLineReference (_str (aDelivery.getDeliveryNoteLineReference ().get (0).getLineIDValue ()));
      // BG-37 + BG-38 — line delivery information
      final PhloxLineDelivery aLineDelivery = _readLineDelivery (aDelivery);
      if (aLineDelivery != null)
        aOut.setDelivery (aLineDelivery);
    }
  }

  /** @return The line delivery or {@code null} if the source carries none of its terms. */
  @Nullable
  private static PhloxLineDelivery _readLineDelivery (@NonNull final DeliveryType aDelivery)
  {
    final PhloxLineDelivery aOut = new PhloxLineDelivery ();
    boolean bAny = false;
    // BT-185
    if (aDelivery.getDeliveryParty () != null && !aDelivery.getDeliveryParty ().getPartyName ().isEmpty ())
    {
      aOut.setPartyName (_str (aDelivery.getDeliveryParty ().getPartyName ().get (0).getNameValue ()));
      bAny |= aOut.getPartyName () != null;
    }
    if (aDelivery.getDeliveryLocation () != null)
    {
      // BT-186 + BT-186-1
      if (aDelivery.getDeliveryLocation ().getID () != null)
      {
        aOut.setLocationIdentifier (new PhloxIdentifier (aDelivery.getDeliveryLocation ().getIDValue (),
                                                         aDelivery.getDeliveryLocation ().getID ().getSchemeID ()));
        bAny = true;
      }
      // BG-38 — BT-203 to BT-209
      if (aDelivery.getDeliveryLocation ().getAddress () != null)
      {
        final PhloxPostalAddress aAddress = _readAddress (aDelivery.getDeliveryLocation ().getAddress ());
        if (!aAddress.isEmpty ())
        {
          aOut.setPostalAddress (aAddress);
          bAny = true;
        }
      }
    }
    // BT-187
    aOut.setActualDeliveryDate (_d (aDelivery.getActualDeliveryDateValue ()));
    bAny |= aOut.getActualDeliveryDate () != null;
    return bAny ? aOut : null;
  }

  private static void _readLinePrecedingInvoiceRefs (@NonNull final List <BillingReferenceType> aRefs,
                                                     @NonNull final PhloxInvoiceLine aOut)
  {
    // BG-39 — BT-217 to BT-220
    for (final BillingReferenceType aBR : aRefs)
      if (aBR.getInvoiceDocumentReference () != null)
      {
        final DocumentReferenceType aDR = aBR.getInvoiceDocumentReference ();
        final PhloxLinePrecedingInvoiceRef aRef = new PhloxLinePrecedingInvoiceRef ();
        aRef.setNumber (_str (aDR.getIDValue ()));
        aRef.setIssueDate (_d (aDR.getIssueDateValue ()));
        aRef.setTypeCode (_str (aDR.getDocumentTypeCodeValue ()));
        if (!aBR.getBillingReferenceLine ().isEmpty ())
          aRef.setLineReference (_str (aBR.getBillingReferenceLine ().get (0).getIDValue ()));
        aOut.precedingInvoiceRefs ().add (aRef);
      }
  }

  private static void _readLineCommon (@NonNull final List <DocumentReferenceType> aDocRefs,
                                       @NonNull final List <PeriodType> aPeriods,
                                       @NonNull final List <AllowanceChargeType> aACs,
                                       @Nullable final PriceType aPrice,
                                       @Nullable final ItemType aItem,
                                       @NonNull final PhloxInvoiceLine aOut)
  {
    // BT-128
    if (!aDocRefs.isEmpty () && aDocRefs.get (0).getID () != null)
    {
      final DocumentReferenceType aDR = aDocRefs.get (0);
      aOut.setObjectIdentifier (new PhloxIdentifier (aDR.getIDValue (), aDR.getID ().getSchemeID ()));
    }
    // BG-26
    if (!aPeriods.isEmpty ())
    {
      final PeriodType aPeriod = aPeriods.get (0);
      final PhloxInvoicingPeriod aIP = new PhloxInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      if (!aIP.isEmpty ())
        aOut.setPeriod (aIP);
    }
    // BG-27 / BG-28
    for (final AllowanceChargeType aAC : aACs)
      if (aAC.getChargeIndicator () != null && aAC.getChargeIndicator ().isValue ())
        aOut.charges ().add (_readLineCharge (aAC));
      else
        aOut.allowances ().add (_readLineAllowance (aAC));
    // BG-29
    if (aPrice != null)
      aOut.setPrice (_readPriceDetails (aPrice));
    if (aItem != null)
    {
      // BG-31 + BG-32
      aOut.setItem (_readItem (aItem));
      // BG-30 from ClassifiedTaxCategory of the item
      if (!aItem.getClassifiedTaxCategory ().isEmpty ())
      {
        final TaxCategoryType aTC = aItem.getClassifiedTaxCategory ().get (0);
        final PhloxLineVat aVat = new PhloxLineVat ();
        aVat.setCategoryCode (_str (aTC.getIDValue ()));
        if (aTC.getPercent () != null)
          aVat.setRate (aTC.getPercent ().getValue ());
        // BT-194
        aVat.setExemptionReasonText (_firstExemptionReason (aTC));
        // BT-195
        aVat.setExemptionReasonCode (_str (aTC.getTaxExemptionReasonCodeValue ()));
        aOut.setLineVat (aVat);
        // BT-196 — read from the line VAT category, stored with the item
        aOut.getItem ().setGoodsServicesCode (_str (aTC.getSupplyTypeCodeValue ()));
      }
    }
  }

  @NonNull
  private static PhloxInvoiceLine _readInvoiceLine (@NonNull final InvoiceLineType aLine)
  {
    final PhloxInvoiceLine aOut = new PhloxInvoiceLine ();
    aOut.setID (_str (aLine.getIDValue ()));
    if (!aLine.getNote ().isEmpty ())
      aOut.setNote (_str (aLine.getNote ().get (0).getValue ()));
    if (aLine.getInvoicedQuantity () != null)
    {
      aOut.setQuantity (aLine.getInvoicedQuantity ().getValue ());
      aOut.setQuantityUnit (aLine.getInvoicedQuantity ().getUnitCode ());
    }
    if (aLine.getLineExtensionAmount () != null)
      aOut.setNetAmount (aLine.getLineExtensionAmount ().getValue ());
    aOut.setAccountingCost (_str (aLine.getAccountingCostValue ()));
    _readLineReferences (aLine.getOrderLineReference (),
                         aLine.getDespatchLineReference (),
                         aLine.getReceiptLineReference (),
                         aLine.getDelivery (),
                         aOut);
    _readLinePrecedingInvoiceRefs (aLine.getBillingReference (), aOut);
    _readLineCommon (aLine.getDocumentReference (),
                     aLine.getInvoicePeriod (),
                     aLine.getAllowanceCharge (),
                     aLine.getPrice (),
                     aLine.getItem (),
                     aOut);
    return aOut;
  }

  @NonNull
  private static PhloxInvoiceLine _readCreditNoteLine (@NonNull final CreditNoteLineType aLine)
  {
    final PhloxInvoiceLine aOut = new PhloxInvoiceLine ();
    aOut.setID (_str (aLine.getIDValue ()));
    if (!aLine.getNote ().isEmpty ())
      aOut.setNote (_str (aLine.getNote ().get (0).getValue ()));
    if (aLine.getCreditedQuantity () != null)
    {
      aOut.setQuantity (aLine.getCreditedQuantity ().getValue ());
      aOut.setQuantityUnit (aLine.getCreditedQuantity ().getUnitCode ());
    }
    if (aLine.getLineExtensionAmount () != null)
      aOut.setNetAmount (aLine.getLineExtensionAmount ().getValue ());
    aOut.setAccountingCost (_str (aLine.getAccountingCostValue ()));
    _readLineReferences (aLine.getOrderLineReference (),
                         aLine.getDespatchLineReference (),
                         aLine.getReceiptLineReference (),
                         aLine.getDelivery (),
                         aOut);
    _readLinePrecedingInvoiceRefs (aLine.getBillingReference (), aOut);
    _readLineCommon (aLine.getDocumentReference (),
                     aLine.getInvoicePeriod (),
                     aLine.getAllowanceCharge (),
                     aLine.getPrice (),
                     aLine.getItem (),
                     aOut);
    return aOut;
  }

  @NonNull
  private static PhloxLineAllowance _readLineAllowance (@NonNull final AllowanceChargeType aAC)
  {
    final PhloxLineAllowance aOut = new PhloxLineAllowance ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ()
                                                                                 .get (0)
                                                                                 .getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    return aOut;
  }

  @NonNull
  private static PhloxLineCharge _readLineCharge (@NonNull final AllowanceChargeType aAC)
  {
    final PhloxLineCharge aOut = new PhloxLineCharge ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ()
                                                                                 .get (0)
                                                                                 .getValue ()));
    // BT-145 (UNTDID 7161) or BT-193 + BT-193-1 (UNTDID 5153, told apart by @listID)
    if (_isNonVatTaxCode (aAC))
    {
      aOut.setNonVatTaxTypeCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
      aOut.setNonVatTaxTypeCodeListID (_str (aAC.getAllowanceChargeReasonCode ().getListID ()));
    }
    else
      aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    return aOut;
  }

  @NonNull
  private static PhloxPriceDetails _readPriceDetails (@NonNull final PriceType aPrice)
  {
    final PhloxPriceDetails aOut = new PhloxPriceDetails ();
    if (aPrice.getPriceAmount () != null)
      aOut.setNetPrice (aPrice.getPriceAmount ().getValue ());
    if (aPrice.getBaseQuantity () != null)
    {
      aOut.setBaseQuantity (aPrice.getBaseQuantity ().getValue ());
      aOut.setBaseQuantityUnit (aPrice.getBaseQuantity ().getUnitCode ());
    }
    for (final AllowanceChargeType aAC : aPrice.getAllowanceCharge ())
      if (aAC.getChargeIndicator () == null || !aAC.getChargeIndicator ().isValue ())
      {
        if (aAC.getAmount () != null)
          aOut.setDiscount (aAC.getAmount ().getValue ());
        if (aAC.getBaseAmount () != null)
          aOut.setGrossPrice (aAC.getBaseAmount ().getValue ());
      }
    return aOut;
  }

  @NonNull
  private static PhloxItem _readItem (@NonNull final ItemType aItem)
  {
    final PhloxItem aOut = new PhloxItem ();
    aOut.setName (_firstText (aItem.getName ()));
    aOut.setDescription (!aItem.getDescription ().isEmpty () ? aItem.getDescription ().get (0).getValue () : null);
    if (aItem.getSellersItemIdentification () != null)
      aOut.setSellerItemID (_str (aItem.getSellersItemIdentification ().getIDValue ()));
    if (aItem.getBuyersItemIdentification () != null)
      aOut.setBuyerItemID (_str (aItem.getBuyersItemIdentification ().getIDValue ()));
    if (aItem.getStandardItemIdentification () != null)
    {
      final ItemIdentificationType aSII = aItem.getStandardItemIdentification ();
      if (aSII.getID () != null)
        aOut.setStandardItemID (new PhloxIdentifier (aSII.getIDValue (), aSII.getID ().getSchemeID ()));
    }
    if (aItem.getOriginCountry () != null)
      aOut.setCountryOfOrigin (_str (aItem.getOriginCountry ().getIdentificationCodeValue ()));
    for (final CommodityClassificationType aCC : aItem.getCommodityClassification ())
      if (aCC.getItemClassificationCode () != null)
      {
        final PhloxItemClassification aIC = new PhloxItemClassification ();
        aIC.setCode (aCC.getItemClassificationCode ().getValue ());
        aIC.setListID (aCC.getItemClassificationCode ().getListID ());
        aIC.setListVersionID (aCC.getItemClassificationCode ().getListVersionID ());
        aOut.classifications ().add (aIC);
      }
    for (final ItemPropertyType aIP : aItem.getAdditionalItemProperty ())
    {
      final PhloxItemAttribute aAttr = new PhloxItemAttribute ();
      // BT-160
      aAttr.setName (_str (aIP.getNameValue ()));
      // BT-211
      aAttr.setCode (_str (aIP.getNameCodeValue ()));
      if (aIP.getValueQuantity () != null)
      {
        // BT-161b numeric value with BT-212 unit of measure code
        if (aIP.getValueQuantityValue () != null)
          aAttr.setValue (aIP.getValueQuantityValue ().toPlainString ());
        aAttr.setValueUnit (_str (aIP.getValueQuantity ().getUnitCode ()));
      }
      else
        // BT-161a text value
        aAttr.setValue (_str (aIP.getValueValue ()));
      aOut.attributes ().add (aAttr);
    }
    return aOut;
  }

  // ===== Helpers =====

  @Nullable
  private static String _firstText (@NonNull final List <? extends TextType> aTexts)
  {
    return aTexts.isEmpty () ? null : _str (aTexts.get (0).getValue ());
  }

  @Nullable
  private static LocalDate _d (@Nullable final XMLOffsetDate aDate)
  {
    return aDate == null ? null : aDate.toLocalDate ();
  }

  @Nullable
  private static String _str (@Nullable final String s)
  {
    if (s == null)
      return null;
    final String sTrim = s.trim ();
    return sTrim.isEmpty () ? null : sTrim;
  }

  @NonNull
  private static String _joinNotes (@NonNull final List <NoteType> aNotes)
  {
    final StringBuilder aSb = new StringBuilder ();
    for (final NoteType aN : aNotes)
    {
      if (aN.getValue () == null)
        continue;
      if (aSb.length () > 0)
        aSb.append ('\n');
      aSb.append (aN.getValue ());
    }
    return aSb.toString ();
  }
}
