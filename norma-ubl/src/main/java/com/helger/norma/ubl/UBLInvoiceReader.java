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
package com.helger.norma.ubl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.datetime.xml.XMLOffsetDate;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.norma.api.model.ENormaDocumentKind;
import com.helger.norma.api.model.NormaAttachment;
import com.helger.norma.api.model.NormaBuyer;
import com.helger.norma.api.model.NormaContact;
import com.helger.norma.api.model.NormaCreditTransfer;
import com.helger.norma.api.model.NormaDeliveryInformation;
import com.helger.norma.api.model.NormaDirectDebit;
import com.helger.norma.api.model.NormaDocumentAllowance;
import com.helger.norma.api.model.NormaDocumentCharge;
import com.helger.norma.api.model.NormaIdentifier;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.api.model.NormaInvoiceLine;
import com.helger.norma.api.model.NormaInvoiceNote;
import com.helger.norma.api.model.NormaInvoicingPeriod;
import com.helger.norma.api.model.NormaItem;
import com.helger.norma.api.model.NormaItemAttribute;
import com.helger.norma.api.model.NormaItemClassification;
import com.helger.norma.api.model.NormaLineAllowance;
import com.helger.norma.api.model.NormaLineCharge;
import com.helger.norma.api.model.NormaLineVAT;
import com.helger.norma.api.model.NormaPayee;
import com.helger.norma.api.model.NormaPaymentCard;
import com.helger.norma.api.model.NormaPaymentInstructions;
import com.helger.norma.api.model.NormaPostalAddress;
import com.helger.norma.api.model.NormaPrecedingInvoiceRef;
import com.helger.norma.api.model.NormaPriceDetails;
import com.helger.norma.api.model.NormaSeller;
import com.helger.norma.api.model.NormaSellerTaxRepresentative;
import com.helger.norma.api.model.NormaSupportingDocument;
import com.helger.norma.api.model.NormaVATBreakdown;
import com.helger.norma.api.read.IInvoiceReader;
import com.helger.ubl21.UBL21Marshaller;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AttachmentType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BranchType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CommodityClassificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CreditNoteLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ExternalReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialAccountType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderLineReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyLegalEntityType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyTaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMandateType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PriceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxTotalType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Reads UBL 2.1 Invoice and CreditNote documents into a {@link NormaInvoice}. Default factory:
 * {@link #fromUBL21()}. Auto-detect on root element: {@link #autoDetect()}.
 *
 * @author Philip Helger
 */
public class UBLInvoiceReader implements IInvoiceReader <InputStream>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UBLInvoiceReader.class);

  protected UBLInvoiceReader ()
  {}

  /** @return A reader for UBL 2.1 (EN 16931:2017 binding). */
  @NonNull
  public static UBLInvoiceReader fromUBL21 ()
  {
    return new UBLInvoiceReader ();
  }

  /**
   * @return A reader that auto-detects the root element ({@code Invoice} vs {@code CreditNote})
   *         per call. Defaults to UBL 2.1 binding.
   */
  @NonNull
  public static UBLInvoiceReader autoDetect ()
  {
    return new UBLInvoiceReader ();
  }

  @Override
  @Nullable
  public NormaInvoice read (@NonNull final InputStream aSource, @NonNull final ErrorList aErrors)
  {
    // Try Invoice first; if the marshaller rejects, try CreditNote.
    // The InputStream can only be read once, so we buffer it.
    final byte [] aBytes;
    try
    {
      aBytes = aSource.readAllBytes ();
    }
    catch (final java.io.IOException ex)
    {
      aErrors.add (SingleError.builderError ().errorText ("Failed to read source: " + ex.getMessage ()).build ());
      return null;
    }

    final InvoiceType aInvoice = UBL21Marshaller.invoice ().read (new com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream (aBytes));
    if (aInvoice != null)
      return readInvoice (aInvoice);

    final CreditNoteType aCreditNote = UBL21Marshaller.creditNote ().read (new com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream (aBytes));
    if (aCreditNote != null)
      return readCreditNote (aCreditNote);

    aErrors.add (SingleError.builderError ()
                            .errorText ("Source could not be parsed as UBL 2.1 Invoice or CreditNote")
                            .build ());
    return null;
  }

  /**
   * Read an already-parsed UBL Invoice into a {@link NormaInvoice}.
   *
   * @param aInvoice
   *        The JAXB invoice. Never {@code null}.
   * @return The populated model.
   */
  @NonNull
  public NormaInvoice readInvoice (@NonNull final InvoiceType aInvoice)
  {
    final NormaInvoice aModel = new NormaInvoice ();
    aModel.setKind (ENormaDocumentKind.INVOICE);

    // BT-1
    aModel.setNumber (_str (aInvoice.getIDValue ()));
    // BT-2
    aModel.setIssueDate (_d (aInvoice.getIssueDateValue ()));
    // BT-3
    aModel.setTypeCode (_strFromCode (aInvoice.getInvoiceTypeCodeValue ()));
    // BG-1 — notes
    for (final NoteType aNote : aInvoice.getNote ())
      if (aNote.getValue () != null)
        aModel.invoiceNotes ().add (_parseInvoiceNote (aNote.getValue ()));
    // BT-5 / BT-6
    aModel.setCurrency (aInvoice.getDocumentCurrencyCodeValue ());
    aModel.setVATAccountingCurrency (aInvoice.getTaxCurrencyCodeValue ());
    // BT-7
    aModel.setVATPointDate (_d (aInvoice.getTaxPointDateValue ()));
    // BT-9 (Invoice: header DueDate)
    aModel.setPaymentDueDate (_d (aInvoice.getDueDateValue ()));
    // BT-10
    aModel.setBuyerReference (_str (aInvoice.getBuyerReferenceValue ()));
    // BT-19
    aModel.setBuyerAccountingReference (_str (aInvoice.getAccountingCostValue ()));
    // BG-2
    aModel.getProcessControl ().setBusinessProcessType (_str (aInvoice.getProfileIDValue ()));
    aModel.getProcessControl ().setSpecificationIdentifier (_str (aInvoice.getCustomizationIDValue ()));
    // BT-13
    if (aInvoice.getOrderReference () != null)
    {
      final OrderReferenceType aOrder = aInvoice.getOrderReference ();
      aModel.setPurchaseOrderReference (_str (aOrder.getIDValue ()));
      aModel.setSalesOrderReference (_str (aOrder.getSalesOrderIDValue ()));
    }
    // BT-12 — contract reference
    if (!aInvoice.getContractDocumentReference ().isEmpty ())
      aModel.setContractReference (_str (aInvoice.getContractDocumentReference ().get (0).getIDValue ()));
    // BT-15 — receiving advice
    if (!aInvoice.getReceiptDocumentReference ().isEmpty ())
      aModel.setReceivingAdviceReference (_str (aInvoice.getReceiptDocumentReference ().get (0).getIDValue ()));
    // BT-16 — despatch advice
    if (!aInvoice.getDespatchDocumentReference ().isEmpty ())
      aModel.setDespatchAdviceReference (_str (aInvoice.getDespatchDocumentReference ().get (0).getIDValue ()));
    // BT-17 — tender or lot
    if (!aInvoice.getOriginatorDocumentReference ().isEmpty ())
      aModel.setTenderOrLotReference (_str (aInvoice.getOriginatorDocumentReference ().get (0).getIDValue ()));
    // BT-11 — project reference
    if (!aInvoice.getProjectReference ().isEmpty () && aInvoice.getProjectReference ().get (0).getID () != null)
      aModel.setProjectReference (aInvoice.getProjectReference ().get (0).getID ().getValue ());

    // BT-18 + BG-24 — additional document references
    for (final DocumentReferenceType aRef : aInvoice.getAdditionalDocumentReference ())
    {
      final String sTypeCode = aRef.getDocumentTypeCodeValue ();
      if ("130".equals (sTypeCode))
      {
        // BT-18: Invoiced object identifier
        if (aRef.getID () != null)
          aModel.setInvoicedObjectIdentifier (new NormaIdentifier (aRef.getIDValue (), aRef.getID ().getSchemeID ()));
      }
      else
      {
        // BG-24: supporting document
        aModel.supportingDocuments ().add (_readSupportingDocument (aRef));
      }
    }

    // BG-3 — preceding invoice references
    for (final BillingReferenceType aBR : aInvoice.getBillingReference ())
      if (aBR.getInvoiceDocumentReference () != null)
      {
        final DocumentReferenceType aDR = aBR.getInvoiceDocumentReference ();
        aModel.precedingInvoiceRefs ().add (new NormaPrecedingInvoiceRef ().setNumber (_str (aDR.getIDValue ()))
                                                                          .setIssueDate (_d (aDR.getIssueDateValue ())));
      }

    // BG-4 + BG-5 + BG-6 — Seller
    if (aInvoice.getAccountingSupplierParty () != null)
      _readSeller (aInvoice.getAccountingSupplierParty (), aModel.getSeller ());
    // BG-7 + BG-8 + BG-9 — Buyer
    if (aInvoice.getAccountingCustomerParty () != null)
      _readBuyer (aInvoice.getAccountingCustomerParty (), aModel.getBuyer ());
    // BG-10 — Payee
    if (aInvoice.getPayeeParty () != null)
      aModel.setPayee (_readPayee (aInvoice.getPayeeParty ()));
    // BG-11 + BG-12 — Seller tax representative
    if (aInvoice.getTaxRepresentativeParty () != null)
      aModel.setSellerTaxRepresentative (_readSellerTaxRepresentative (aInvoice.getTaxRepresentativeParty ()));
    // BG-13 + BG-15 — Delivery
    if (!aInvoice.getDelivery ().isEmpty ())
      aModel.setDeliveryInformation (_readDelivery (aInvoice.getDelivery ().get (0)));
    // BG-14 — Invoicing period
    if (!aInvoice.getInvoicePeriod ().isEmpty ())
    {
      final PeriodType aPeriod = aInvoice.getInvoicePeriod ().get (0);
      final NormaInvoicingPeriod aIP = new NormaInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      // BT-8 — VAT point date code (lives in InvoicePeriod for UBL)
      aModel.setVATPointDateCode (_str ((aPeriod.getDescriptionCode ().isEmpty () ? null : aPeriod.getDescriptionCode ().get (0).getValue ())));
      if (!aIP.isEmpty ())
        aModel.setInvoicingPeriod (aIP);
    }

    // BG-16/17/18/19 — Payment instructions
    if (!aInvoice.getPaymentMeans ().isEmpty ())
      aModel.setPaymentInstructions (_readPaymentInstructions (aInvoice.getPaymentMeans (),
                                                               aInvoice.getPaymentTerms (),
                                                               aInvoice.getAccountingSupplierParty (),
                                                               aInvoice.getPayeeParty ()));
    // BT-20 — payment terms (text)
    if (!aInvoice.getPaymentTerms ().isEmpty ())
    {
      final PaymentTermsType aPT = aInvoice.getPaymentTerms ().get (0);
      if (!aPT.getNote ().isEmpty ())
        aModel.setPaymentTerms (_joinNotes (aPT.getNote ()));
    }

    // BG-20 / BG-21 — document-level allowances and charges
    for (final AllowanceChargeType aAC : aInvoice.getAllowanceCharge ())
      if (aAC.getChargeIndicator () != null && Boolean.TRUE.equals (aAC.getChargeIndicator ().isValue ()))
        aModel.documentCharges ().add (_readDocumentCharge (aAC));
      else
        aModel.documentAllowances ().add (_readDocumentAllowance (aAC));

    // BG-22 — totals
    if (aInvoice.getLegalMonetaryTotal () != null)
      _readTotals (aInvoice.getLegalMonetaryTotal (), aModel);

    // BG-23 — VAT breakdowns
    _readTaxTotals (aInvoice.getTaxTotal (), aModel);

    // BG-25 — lines
    for (final InvoiceLineType aLine : aInvoice.getInvoiceLine ())
      aModel.lines ().add (_readInvoiceLine (aLine));

    return aModel;
  }

  /**
   * Read an already-parsed UBL CreditNote into a {@link NormaInvoice}.
   *
   * @param aCreditNote
   *        The JAXB credit note.
   * @return The populated model.
   */
  @NonNull
  public NormaInvoice readCreditNote (@NonNull final CreditNoteType aCreditNote)
  {
    final NormaInvoice aModel = new NormaInvoice ();
    aModel.setKind (ENormaDocumentKind.CREDIT_NOTE);

    aModel.setNumber (_str (aCreditNote.getIDValue ()));
    aModel.setIssueDate (_d (aCreditNote.getIssueDateValue ()));
    aModel.setTypeCode (_strFromCode (aCreditNote.getCreditNoteTypeCodeValue ()));
    for (final NoteType aNote : aCreditNote.getNote ())
      if (aNote.getValue () != null)
        aModel.invoiceNotes ().add (_parseInvoiceNote (aNote.getValue ()));
    aModel.setCurrency (aCreditNote.getDocumentCurrencyCodeValue ());
    aModel.setVATAccountingCurrency (aCreditNote.getTaxCurrencyCodeValue ());
    aModel.setVATPointDate (_d (aCreditNote.getTaxPointDateValue ()));
    aModel.setBuyerReference (_str (aCreditNote.getBuyerReferenceValue ()));
    aModel.setBuyerAccountingReference (_str (aCreditNote.getAccountingCostValue ()));
    aModel.getProcessControl ().setBusinessProcessType (_str (aCreditNote.getProfileIDValue ()));
    aModel.getProcessControl ().setSpecificationIdentifier (_str (aCreditNote.getCustomizationIDValue ()));
    if (aCreditNote.getOrderReference () != null)
    {
      final OrderReferenceType aOrder = aCreditNote.getOrderReference ();
      aModel.setPurchaseOrderReference (_str (aOrder.getIDValue ()));
      aModel.setSalesOrderReference (_str (aOrder.getSalesOrderIDValue ()));
    }
    if (!aCreditNote.getContractDocumentReference ().isEmpty ())
      aModel.setContractReference (_str (aCreditNote.getContractDocumentReference ().get (0).getIDValue ()));
    if (!aCreditNote.getReceiptDocumentReference ().isEmpty ())
      aModel.setReceivingAdviceReference (_str (aCreditNote.getReceiptDocumentReference ().get (0).getIDValue ()));
    if (!aCreditNote.getDespatchDocumentReference ().isEmpty ())
      aModel.setDespatchAdviceReference (_str (aCreditNote.getDespatchDocumentReference ().get (0).getIDValue ()));
    if (!aCreditNote.getOriginatorDocumentReference ().isEmpty ())
      aModel.setTenderOrLotReference (_str (aCreditNote.getOriginatorDocumentReference ().get (0).getIDValue ()));

    // BT-18 + BG-24 from additional document references
    for (final DocumentReferenceType aRef : aCreditNote.getAdditionalDocumentReference ())
    {
      final String sTypeCode = aRef.getDocumentTypeCodeValue ();
      if ("130".equals (sTypeCode) || "ATS".equalsIgnoreCase (sTypeCode))
      {
        if (aRef.getID () != null)
          aModel.setInvoicedObjectIdentifier (new NormaIdentifier (aRef.getIDValue (), aRef.getID ().getSchemeID ()));
      }
      else
      {
        aModel.supportingDocuments ().add (_readSupportingDocument (aRef));
      }
    }

    for (final BillingReferenceType aBR : aCreditNote.getBillingReference ())
      if (aBR.getInvoiceDocumentReference () != null)
      {
        final DocumentReferenceType aDR = aBR.getInvoiceDocumentReference ();
        aModel.precedingInvoiceRefs ().add (new NormaPrecedingInvoiceRef ().setNumber (_str (aDR.getIDValue ()))
                                                                          .setIssueDate (_d (aDR.getIssueDateValue ())));
      }

    if (aCreditNote.getAccountingSupplierParty () != null)
      _readSeller (aCreditNote.getAccountingSupplierParty (), aModel.getSeller ());
    if (aCreditNote.getAccountingCustomerParty () != null)
      _readBuyer (aCreditNote.getAccountingCustomerParty (), aModel.getBuyer ());
    if (aCreditNote.getPayeeParty () != null)
      aModel.setPayee (_readPayee (aCreditNote.getPayeeParty ()));
    if (aCreditNote.getTaxRepresentativeParty () != null)
      aModel.setSellerTaxRepresentative (_readSellerTaxRepresentative (aCreditNote.getTaxRepresentativeParty ()));
    if (!aCreditNote.getDelivery ().isEmpty ())
      aModel.setDeliveryInformation (_readDelivery (aCreditNote.getDelivery ().get (0)));
    if (!aCreditNote.getInvoicePeriod ().isEmpty ())
    {
      final PeriodType aPeriod = aCreditNote.getInvoicePeriod ().get (0);
      final NormaInvoicingPeriod aIP = new NormaInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      aModel.setVATPointDateCode (_str ((aPeriod.getDescriptionCode ().isEmpty () ? null : aPeriod.getDescriptionCode ().get (0).getValue ())));
      if (!aIP.isEmpty ())
        aModel.setInvoicingPeriod (aIP);
    }

    if (!aCreditNote.getPaymentMeans ().isEmpty ())
    {
      aModel.setPaymentInstructions (_readPaymentInstructions (aCreditNote.getPaymentMeans (),
                                                               aCreditNote.getPaymentTerms (),
                                                               aCreditNote.getAccountingSupplierParty (),
                                                               aCreditNote.getPayeeParty ()));
      // BT-9 in CN: PaymentMeans/PaymentDueDate
      for (final PaymentMeansType aPM : aCreditNote.getPaymentMeans ())
        if (_d (aPM.getPaymentDueDateValue ()) != null)
        {
          aModel.setPaymentDueDate (_d (aPM.getPaymentDueDateValue ()));
          break;
        }
    }
    if (!aCreditNote.getPaymentTerms ().isEmpty ())
    {
      final PaymentTermsType aPT = aCreditNote.getPaymentTerms ().get (0);
      if (!aPT.getNote ().isEmpty ())
        aModel.setPaymentTerms (_joinNotes (aPT.getNote ()));
    }

    for (final AllowanceChargeType aAC : aCreditNote.getAllowanceCharge ())
      if (aAC.getChargeIndicator () != null && Boolean.TRUE.equals (aAC.getChargeIndicator ().isValue ()))
        aModel.documentCharges ().add (_readDocumentCharge (aAC));
      else
        aModel.documentAllowances ().add (_readDocumentAllowance (aAC));

    if (aCreditNote.getLegalMonetaryTotal () != null)
      _readTotals (aCreditNote.getLegalMonetaryTotal (), aModel);
    _readTaxTotals (aCreditNote.getTaxTotal (), aModel);

    for (final CreditNoteLineType aLine : aCreditNote.getCreditNoteLine ())
      aModel.lines ().add (_readCreditNoteLine (aLine));

    return aModel;
  }

  // ===== Internal walkers =====

  private static void _readSeller (@NonNull final SupplierPartyType aSP, @NonNull final NormaSeller aSeller)
  {
    if (aSP.getParty () == null)
      return;
    final PartyType aParty = aSP.getParty ();
    _readPartyCommon (aParty, aSeller);
  }

  private static void _readBuyer (@NonNull final CustomerPartyType aCP, @NonNull final NormaBuyer aBuyer)
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
        aBuyer.setLegalRegistration (new NormaIdentifier (aLE.getCompanyIDValue (),
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
        aBuyer.identifiers ().add (new NormaIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    // BT-48 — VAT
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null && "VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
        aBuyer.setVATIdentifier (_str (aPTS.getCompanyIDValue ()));
    // BT-49
    if (aParty.getEndpointID () != null)
      aBuyer.setElectronicAddress (new NormaIdentifier (aParty.getEndpointIDValue (),
                                                       aParty.getEndpointID ().getSchemeID ()));
    // BG-8
    if (aParty.getPostalAddress () != null)
      aBuyer.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    // BG-9
    if (aParty.getContact () != null)
      aBuyer.setContact (_readContact (aParty.getContact ()));
  }

  private static void _readPartyCommon (@NonNull final PartyType aParty, @NonNull final NormaSeller aSeller)
  {
    // BT-27
    if (!aParty.getPartyLegalEntity ().isEmpty ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);
      aSeller.setName (_str (aLE.getRegistrationNameValue ()));
      if (aLE.getCompanyID () != null)
        aSeller.setLegalRegistration (new NormaIdentifier (aLE.getCompanyIDValue (),
                                                          aLE.getCompanyID ().getSchemeID ()));
      // BT-33
      aSeller.setAdditionalLegalInfo (_str (aLE.getCompanyLegalFormValue ()));
    }
    // BT-28
    if (!aParty.getPartyName ().isEmpty ())
      aSeller.setTradingName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    // BT-29
    for (final PartyIdentificationType aPI : aParty.getPartyIdentification ())
      if (aPI.getID () != null)
        aSeller.identifiers ().add (new NormaIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    // BT-31 / BT-32
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null)
      {
        if ("VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
          aSeller.setVATIdentifier (_str (aPTS.getCompanyIDValue ()));
        else
          aSeller.setTaxRegistration (_str (aPTS.getCompanyIDValue ()));
      }
    // BT-34
    if (aParty.getEndpointID () != null)
      aSeller.setElectronicAddress (new NormaIdentifier (aParty.getEndpointIDValue (),
                                                        aParty.getEndpointID ().getSchemeID ()));
    // BG-5
    if (aParty.getPostalAddress () != null)
      aSeller.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    // BG-6
    if (aParty.getContact () != null)
      aSeller.setContact (_readContact (aParty.getContact ()));
  }

  @NonNull
  private static NormaPostalAddress _readAddress (@NonNull final AddressType aAddr)
  {
    final NormaPostalAddress aOut = new NormaPostalAddress ();
    aOut.setLine1 (_str (aAddr.getStreetNameValue ()));
    aOut.setLine2 (_str (aAddr.getAdditionalStreetNameValue ()));
    if (!aAddr.getAddressLine ().isEmpty ())
    {
      final AddressLineType aAL = aAddr.getAddressLine ().get (0);
      aOut.setLine3 (_str (aAL.getLineValue ()));
    }
    aOut.setCity (_str (aAddr.getCityNameValue ()));
    aOut.setPostCode (_str (aAddr.getPostalZoneValue ()));
    aOut.setCountrySubdivision (_str (aAddr.getCountrySubentityValue ()));
    final CountryType aCountry = aAddr.getCountry ();
    if (aCountry != null)
      aOut.setCountryCode (_str (aCountry.getIdentificationCodeValue ()));
    return aOut;
  }

  @NonNull
  private static NormaContact _readContact (@NonNull final ContactType aContact)
  {
    final NormaContact aOut = new NormaContact ();
    aOut.setName (_str (aContact.getNameValue ()));
    aOut.setTelephone (_str (aContact.getTelephoneValue ()));
    aOut.setEmail (_str (aContact.getElectronicMailValue ()));
    return aOut;
  }

  @Nullable
  private static NormaPayee _readPayee (@NonNull final PartyType aParty)
  {
    final NormaPayee aOut = new NormaPayee ();
    if (!aParty.getPartyName ().isEmpty ())
      aOut.setName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    if (!aParty.getPartyIdentification ().isEmpty () && aParty.getPartyIdentification ().get (0).getID () != null)
    {
      final PartyIdentificationType aPI = aParty.getPartyIdentification ().get (0);
      aOut.setIdentifier (new NormaIdentifier (aPI.getIDValue (), aPI.getID ().getSchemeID ()));
    }
    if (!aParty.getPartyLegalEntity ().isEmpty ())
    {
      final PartyLegalEntityType aLE = aParty.getPartyLegalEntity ().get (0);
      if (aLE.getCompanyID () != null)
        aOut.setLegalRegistration (new NormaIdentifier (aLE.getCompanyIDValue (),
                                                       aLE.getCompanyID ().getSchemeID ()));
    }
    return aOut;
  }

  @NonNull
  private static NormaSellerTaxRepresentative _readSellerTaxRepresentative (@NonNull final PartyType aParty)
  {
    final NormaSellerTaxRepresentative aOut = new NormaSellerTaxRepresentative ();
    if (!aParty.getPartyName ().isEmpty ())
      aOut.setName (_str (aParty.getPartyName ().get (0).getNameValue ()));
    for (final PartyTaxSchemeType aPTS : aParty.getPartyTaxScheme ())
      if (aPTS.getTaxScheme () != null && "VAT".equals (aPTS.getTaxScheme ().getIDValue ()))
        aOut.setVATIdentifier (_str (aPTS.getCompanyIDValue ()));
    if (aParty.getPostalAddress () != null)
      aOut.setPostalAddress (_readAddress (aParty.getPostalAddress ()));
    return aOut;
  }

  @NonNull
  private static NormaDeliveryInformation _readDelivery (@NonNull final DeliveryType aDelivery)
  {
    final NormaDeliveryInformation aOut = new NormaDeliveryInformation ();
    if (aDelivery.getDeliveryParty () != null && !aDelivery.getDeliveryParty ().getPartyName ().isEmpty ())
      aOut.setPartyName (_str (aDelivery.getDeliveryParty ().getPartyName ().get (0).getNameValue ()));
    if (aDelivery.getDeliveryLocation () != null)
    {
      if (aDelivery.getDeliveryLocation ().getID () != null)
        aOut.setLocationIdentifier (new NormaIdentifier (aDelivery.getDeliveryLocation ().getIDValue (),
                                                        aDelivery.getDeliveryLocation ().getID ().getSchemeID ()));
      if (aDelivery.getDeliveryLocation ().getAddress () != null)
        aOut.setPostalAddress (_readAddress (aDelivery.getDeliveryLocation ().getAddress ()));
    }
    aOut.setActualDeliveryDate (_d (aDelivery.getActualDeliveryDateValue ()));
    return aOut;
  }

  @NonNull
  private static NormaPaymentInstructions _readPaymentInstructions (@NonNull final List <PaymentMeansType> aPaymentMeans,
                                                                    @NonNull final List <PaymentTermsType> aPaymentTerms,
                                                                    @Nullable final SupplierPartyType aSupplier,
                                                                    @Nullable final PartyType aPayee)
  {
    final NormaPaymentInstructions aOut = new NormaPaymentInstructions ();
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
    if (aPM.getCardAccount () != null)
    {
      final NormaPaymentCard aCard = new NormaPaymentCard ();
      aCard.setPAN (_str (aPM.getCardAccount ().getPrimaryAccountNumberIDValue ()));
      aCard.setHolderName (_str (aPM.getCardAccount ().getHolderNameValue ()));
      aOut.setPaymentCard (aCard);
    }

    // BG-19 — direct debit
    if (aPM.getPaymentMandate () != null)
    {
      final PaymentMandateType aMandate = aPM.getPaymentMandate ();
      final NormaDirectDebit aDD = new NormaDirectDebit ();
      aDD.setMandateReferenceID (_str (aMandate.getIDValue ()));
      if (aMandate.getPayerFinancialAccount () != null)
        aDD.setDebitedAccountID (_str (aMandate.getPayerFinancialAccount ().getIDValue ()));
      // BT-90 — bank assigned creditor id (UBL: from supplier/payee PartyIdentification with SEPA scheme)
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
  private static NormaCreditTransfer _readCreditTransfer (@NonNull final FinancialAccountType aAcc)
  {
    final NormaCreditTransfer aOut = new NormaCreditTransfer ();
    aOut.setAccountIdentifier (_str (aAcc.getIDValue ()));
    aOut.setAccountName (_str (aAcc.getNameValue ()));
    final BranchType aBranch = aAcc.getFinancialInstitutionBranch ();
    if (aBranch != null)
      aOut.setServiceProviderID (_str (aBranch.getIDValue ()));
    return aOut;
  }

  @NonNull
  private static NormaDocumentAllowance _readDocumentAllowance (@NonNull final AllowanceChargeType aAC)
  {
    final NormaDocumentAllowance aOut = new NormaDocumentAllowance ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ().get (0).getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    if (aAC.getTaxCategory () != null && !aAC.getTaxCategory ().isEmpty ())
    {
      final TaxCategoryType aTC = aAC.getTaxCategory ().get (0);
      aOut.setVATCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setVATRate (aTC.getPercent ().getValue ());
    }
    return aOut;
  }

  @NonNull
  private static NormaDocumentCharge _readDocumentCharge (@NonNull final AllowanceChargeType aAC)
  {
    final NormaDocumentCharge aOut = new NormaDocumentCharge ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ().get (0).getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    if (aAC.getTaxCategory () != null && !aAC.getTaxCategory ().isEmpty ())
    {
      final TaxCategoryType aTC = aAC.getTaxCategory ().get (0);
      aOut.setVATCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setVATRate (aTC.getPercent ().getValue ());
    }
    return aOut;
  }

  private static void _readTotals (@NonNull final MonetaryTotalType aLMT, @NonNull final NormaInvoice aModel)
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

  private static void _readTaxTotals (@NonNull final List <TaxTotalType> aTaxTotals,
                                      @NonNull final NormaInvoice aModel)
  {
    final String sCurrency = aModel.getCurrency ();
    final String sAccountingCurrency = aModel.getVATAccountingCurrency ();
    boolean bMainTaxTotalSet = false;
    for (final TaxTotalType aTT : aTaxTotals)
    {
      if (aTT.getTaxAmount () != null)
      {
        final BigDecimal aAmount = aTT.getTaxAmount ().getValue ();
        final String sCurrencyID = aTT.getTaxAmount ().getCurrencyID ();
        if (sAccountingCurrency != null && sAccountingCurrency.equals (sCurrencyID))
          aModel.getDocumentTotals ().setTaxTotalAccountingCurrency (aAmount);
        else if (!bMainTaxTotalSet)
        {
          aModel.getDocumentTotals ().setTaxTotal (aAmount);
          bMainTaxTotalSet = true;
        }
      }
      for (final TaxSubtotalType aSub : aTT.getTaxSubtotal ())
        aModel.vatBreakdowns ().add (_readVATBreakdown (aSub));
    }
  }

  @NonNull
  private static NormaVATBreakdown _readVATBreakdown (@NonNull final TaxSubtotalType aSub)
  {
    final NormaVATBreakdown aOut = new NormaVATBreakdown ();
    if (aSub.getTaxableAmount () != null)
      aOut.setTaxableAmount (aSub.getTaxableAmount ().getValue ());
    if (aSub.getTaxAmount () != null)
      aOut.setTaxAmount (aSub.getTaxAmount ().getValue ());
    if (aSub.getTaxCategory () != null)
    {
      final TaxCategoryType aTC = aSub.getTaxCategory ();
      aOut.setCategoryCode (_str (aTC.getIDValue ()));
      if (aTC.getPercent () != null)
        aOut.setRate (aTC.getPercent ().getValue ());
      aOut.setExemptionReason (aTC.getTaxExemptionReason ().isEmpty () ? null : _str (aTC.getTaxExemptionReason ().get (0).getValue ()));
      aOut.setExemptionReasonCode (_str (aTC.getTaxExemptionReasonCodeValue ()));
    }
    return aOut;
  }

  @NonNull
  private static NormaSupportingDocument _readSupportingDocument (@NonNull final DocumentReferenceType aRef)
  {
    final NormaSupportingDocument aOut = new NormaSupportingDocument ();
    aOut.setReference (_str (aRef.getIDValue ()));
    aOut.setDescription (_str (aRef.getDocumentDescriptionAtIndex (0) != null
                                                                             ? aRef.getDocumentDescriptionAtIndex (0)
                                                                                   .getValue ()
                                                                             : null));
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
        final NormaAttachment aBin = new NormaAttachment ();
        aBin.setBinary (aAtt.getEmbeddedDocumentBinaryObject ().getValue ());
        aBin.setMimeCode (aAtt.getEmbeddedDocumentBinaryObject ().getMimeCode ());
        aBin.setFilename (aAtt.getEmbeddedDocumentBinaryObject ().getFilename ());
        aOut.setAttachment (aBin);
      }
    }
    return aOut;
  }

  @NonNull
  private static NormaInvoiceLine _readInvoiceLine (@NonNull final InvoiceLineType aLine)
  {
    final NormaInvoiceLine aOut = new NormaInvoiceLine ();
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
    if (!aLine.getOrderLineReference ().isEmpty ())
    {
      final OrderLineReferenceType aOLR = aLine.getOrderLineReference ().get (0);
      aOut.setPurchaseOrderLineRef (_str (aOLR.getLineIDValue ()));
    }
    if (!aLine.getDocumentReference ().isEmpty () && aLine.getDocumentReference ().get (0).getID () != null)
    {
      final DocumentReferenceType aDR = aLine.getDocumentReference ().get (0);
      aOut.setObjectIdentifier (new NormaIdentifier (aDR.getIDValue (), aDR.getID ().getSchemeID ()));
    }
    if (!aLine.getInvoicePeriod ().isEmpty ())
    {
      final PeriodType aPeriod = aLine.getInvoicePeriod ().get (0);
      final NormaInvoicingPeriod aIP = new NormaInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      if (!aIP.isEmpty ())
        aOut.setPeriod (aIP);
    }
    for (final AllowanceChargeType aAC : aLine.getAllowanceCharge ())
      if (aAC.getChargeIndicator () != null && Boolean.TRUE.equals (aAC.getChargeIndicator ().isValue ()))
        aOut.charges ().add (_readLineCharge (aAC));
      else
        aOut.allowances ().add (_readLineAllowance (aAC));
    if (aLine.getPrice () != null)
      aOut.setPrice (_readPriceDetails (aLine.getPrice ()));
    if (aLine.getItem () != null)
    {
      aOut.setItem (_readItem (aLine.getItem ()));
      // BG-30 from ClassifiedTaxCategory of the item
      if (!aLine.getItem ().getClassifiedTaxCategory ().isEmpty ())
      {
        final TaxCategoryType aTC = aLine.getItem ().getClassifiedTaxCategory ().get (0);
        final NormaLineVAT aVAT = new NormaLineVAT ();
        aVAT.setCategoryCode (_str (aTC.getIDValue ()));
        if (aTC.getPercent () != null)
          aVAT.setRate (aTC.getPercent ().getValue ());
        aOut.setLineVAT (aVAT);
      }
    }
    return aOut;
  }

  @NonNull
  private static NormaInvoiceLine _readCreditNoteLine (@NonNull final CreditNoteLineType aLine)
  {
    final NormaInvoiceLine aOut = new NormaInvoiceLine ();
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
    if (!aLine.getOrderLineReference ().isEmpty ())
      aOut.setPurchaseOrderLineRef (_str (aLine.getOrderLineReference ().get (0).getLineIDValue ()));
    if (!aLine.getDocumentReference ().isEmpty () && aLine.getDocumentReference ().get (0).getID () != null)
    {
      final DocumentReferenceType aDR = aLine.getDocumentReference ().get (0);
      aOut.setObjectIdentifier (new NormaIdentifier (aDR.getIDValue (), aDR.getID ().getSchemeID ()));
    }
    if (!aLine.getInvoicePeriod ().isEmpty ())
    {
      final PeriodType aPeriod = aLine.getInvoicePeriod ().get (0);
      final NormaInvoicingPeriod aIP = new NormaInvoicingPeriod ();
      aIP.setStartDate (_d (aPeriod.getStartDateValue ()));
      aIP.setEndDate (_d (aPeriod.getEndDateValue ()));
      if (!aIP.isEmpty ())
        aOut.setPeriod (aIP);
    }
    for (final AllowanceChargeType aAC : aLine.getAllowanceCharge ())
      if (aAC.getChargeIndicator () != null && Boolean.TRUE.equals (aAC.getChargeIndicator ().isValue ()))
        aOut.charges ().add (_readLineCharge (aAC));
      else
        aOut.allowances ().add (_readLineAllowance (aAC));
    if (aLine.getPrice () != null)
      aOut.setPrice (_readPriceDetails (aLine.getPrice ()));
    if (aLine.getItem () != null)
    {
      aOut.setItem (_readItem (aLine.getItem ()));
      if (!aLine.getItem ().getClassifiedTaxCategory ().isEmpty ())
      {
        final TaxCategoryType aTC = aLine.getItem ().getClassifiedTaxCategory ().get (0);
        final NormaLineVAT aVAT = new NormaLineVAT ();
        aVAT.setCategoryCode (_str (aTC.getIDValue ()));
        if (aTC.getPercent () != null)
          aVAT.setRate (aTC.getPercent ().getValue ());
        aOut.setLineVAT (aVAT);
      }
    }
    return aOut;
  }

  @NonNull
  private static NormaLineAllowance _readLineAllowance (@NonNull final AllowanceChargeType aAC)
  {
    final NormaLineAllowance aOut = new NormaLineAllowance ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ().get (0).getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    return aOut;
  }

  @NonNull
  private static NormaLineCharge _readLineCharge (@NonNull final AllowanceChargeType aAC)
  {
    final NormaLineCharge aOut = new NormaLineCharge ();
    if (aAC.getAmount () != null)
      aOut.setAmount (aAC.getAmount ().getValue ());
    if (aAC.getBaseAmount () != null)
      aOut.setBaseAmount (aAC.getBaseAmount ().getValue ());
    if (aAC.getMultiplierFactorNumeric () != null)
      aOut.setPercentage (aAC.getMultiplierFactorNumeric ().getValue ());
    aOut.setReason (aAC.getAllowanceChargeReason ().isEmpty () ? null : _str (aAC.getAllowanceChargeReason ().get (0).getValue ()));
    aOut.setReasonCode (_str (aAC.getAllowanceChargeReasonCodeValue ()));
    return aOut;
  }

  @NonNull
  private static NormaPriceDetails _readPriceDetails (@NonNull final PriceType aPrice)
  {
    final NormaPriceDetails aOut = new NormaPriceDetails ();
    if (aPrice.getPriceAmount () != null)
      aOut.setNetPrice (aPrice.getPriceAmount ().getValue ());
    if (aPrice.getBaseQuantity () != null)
    {
      aOut.setBaseQuantity (aPrice.getBaseQuantity ().getValue ());
      aOut.setBaseQuantityUnit (aPrice.getBaseQuantity ().getUnitCode ());
    }
    for (final AllowanceChargeType aAC : aPrice.getAllowanceCharge ())
      if (aAC.getChargeIndicator () == null || !Boolean.TRUE.equals (aAC.getChargeIndicator ().isValue ()))
      {
        if (aAC.getAmount () != null)
          aOut.setDiscount (aAC.getAmount ().getValue ());
        if (aAC.getBaseAmount () != null)
          aOut.setGrossPrice (aAC.getBaseAmount ().getValue ());
      }
    return aOut;
  }

  @NonNull
  private static NormaItem _readItem (@NonNull final ItemType aItem)
  {
    final NormaItem aOut = new NormaItem ();
    aOut.setName (_str (aItem.getNameValue ()));
    aOut.setDescription (!aItem.getDescription ().isEmpty () ? aItem.getDescription ().get (0).getValue () : null);
    if (aItem.getSellersItemIdentification () != null)
      aOut.setSellerItemID (_str (aItem.getSellersItemIdentification ().getIDValue ()));
    if (aItem.getBuyersItemIdentification () != null)
      aOut.setBuyerItemID (_str (aItem.getBuyersItemIdentification ().getIDValue ()));
    if (aItem.getStandardItemIdentification () != null)
    {
      final ItemIdentificationType aSII = aItem.getStandardItemIdentification ();
      if (aSII.getID () != null)
        aOut.setStandardItemID (new NormaIdentifier (aSII.getIDValue (), aSII.getID ().getSchemeID ()));
    }
    if (aItem.getOriginCountry () != null)
      aOut.setCountryOfOrigin (_str (aItem.getOriginCountry ().getIdentificationCodeValue ()));
    for (final CommodityClassificationType aCC : aItem.getCommodityClassification ())
      if (aCC.getItemClassificationCode () != null)
      {
        final NormaItemClassification aIC = new NormaItemClassification ();
        aIC.setCode (aCC.getItemClassificationCode ().getValue ());
        aIC.setListID (aCC.getItemClassificationCode ().getListID ());
        aIC.setListVersionID (aCC.getItemClassificationCode ().getListVersionID ());
        aOut.classifications ().add (aIC);
      }
    for (final ItemPropertyType aIP : aItem.getAdditionalItemProperty ())
      aOut.attributes ().add (new NormaItemAttribute (_str (aIP.getNameValue ()), _str (aIP.getValueValue ())));
    return aOut;
  }

  // ===== Helpers =====

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

  @Nullable
  private static String _strFromCode (@Nullable final String s)
  {
    return _str (s);
  }

  @NonNull
  private static NormaInvoiceNote _parseInvoiceNote (@NonNull final String sRaw)
  {
    // UBL convention: subject code embedded as "#code#" prefix; e.g. "#AAB#Some note"
    if (sRaw.startsWith ("#"))
    {
      final int n = sRaw.indexOf ('#', 1);
      if (n > 1)
        return new NormaInvoiceNote (sRaw.substring (1, n), sRaw.substring (n + 1));
    }
    return new NormaInvoiceNote (null, sRaw);
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
