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
package com.helger.norma.api.model;

import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * Root of the EN 16931 intermediate model. One instance represents one Invoice or one Credit Note,
 * populated by a source reader and consumed by an output writer. All optional fields are
 * {@code null} when not present in the source; all collections are non-{@code null} and empty when
 * not populated.
 * <p>
 * Header-level BTs sit directly on this class (BT-1 through BT-20). Each business group (BG-1
 * through BG-25) is exposed as either a single nested object or a mutable list of nested objects.
 *
 * @author Philip Helger
 */
public class NormaInvoice
{
  private ENormaDocumentKind m_eKind = ENormaDocumentKind.INVOICE;

  private String m_sNumber;
  private LocalDate m_aIssueDate;
  private String m_sTypeCode;
  private String m_sCurrency;
  private String m_sVATAccountingCurrency;
  private LocalDate m_aVATPointDate;
  private String m_sVATPointDateCode;
  private LocalDate m_aPaymentDueDate;
  private String m_sBuyerReference;
  private String m_sProjectReference;
  private String m_sContractReference;
  private String m_sPurchaseOrderReference;
  private String m_sSalesOrderReference;
  private String m_sReceivingAdviceReference;
  private String m_sDespatchAdviceReference;
  private String m_sTenderOrLotReference;
  private NormaIdentifier m_aInvoicedObjectIdentifier;
  private String m_sBuyerAccountingReference;
  private String m_sPaymentTerms;

  private final ICommonsList <NormaInvoiceNote> m_aInvoiceNotes = new CommonsArrayList <> ();
  private NormaProcessControl m_aProcessControl = new NormaProcessControl ();
  private final ICommonsList <NormaPrecedingInvoiceRef> m_aPrecedingInvoiceRefs = new CommonsArrayList <> ();
  private NormaSeller m_aSeller = new NormaSeller ();
  private NormaBuyer m_aBuyer = new NormaBuyer ();
  private NormaPayee m_aPayee;
  private NormaSellerTaxRepresentative m_aSellerTaxRepresentative;
  private NormaDeliveryInformation m_aDeliveryInformation;
  private NormaInvoicingPeriod m_aInvoicingPeriod;
  private NormaPaymentInstructions m_aPaymentInstructions;
  private final ICommonsList <NormaDocumentAllowance> m_aDocumentAllowances = new CommonsArrayList <> ();
  private final ICommonsList <NormaDocumentCharge> m_aDocumentCharges = new CommonsArrayList <> ();
  private NormaDocumentTotals m_aDocumentTotals = new NormaDocumentTotals ();
  private final ICommonsList <NormaVATBreakdown> m_aVATBreakdowns = new CommonsArrayList <> ();
  private final ICommonsList <NormaSupportingDocument> m_aSupportingDocuments = new CommonsArrayList <> ();
  private final ICommonsList <NormaInvoiceLine> m_aLines = new CommonsArrayList <> ();

  /** Create an empty invoice (defaults to {@link ENormaDocumentKind#INVOICE}). */
  public NormaInvoice ()
  {}

  /** @return The document kind (Invoice or Credit Note), derived from BT-3. Never {@code null}. */
  @NonNull
  public ENormaDocumentKind getKind ()
  {
    return m_eKind;
  }

  /**
   * Set the document kind.
   *
   * @param eKind
   *        Kind; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setKind (@NonNull final ENormaDocumentKind eKind)
  {
    m_eKind = eKind;
    return this;
  }

  /** @return {@code true} when {@link #getKind()} is {@link ENormaDocumentKind#INVOICE}. */
  public boolean isInvoice ()
  {
    return m_eKind.isInvoice ();
  }

  /** @return {@code true} when {@link #getKind()} is {@link ENormaDocumentKind#CREDIT_NOTE}. */
  public boolean isCreditNote ()
  {
    return m_eKind.isCreditNote ();
  }

  /** @return Invoice number (BT-1). May be {@code null}. */
  @Nullable
  public String getNumber ()
  {
    return m_sNumber;
  }

  /**
   * Set the invoice number (BT-1).
   *
   * @param sNumber
   *        Invoice number; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setNumber (@Nullable final String sNumber)
  {
    m_sNumber = sNumber;
    return this;
  }

  /** @return Invoice issue date (BT-2). May be {@code null}. */
  @Nullable
  public LocalDate getIssueDate ()
  {
    return m_aIssueDate;
  }

  /**
   * Set the invoice issue date (BT-2).
   *
   * @param aIssueDate
   *        Issue date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setIssueDate (@Nullable final LocalDate aIssueDate)
  {
    m_aIssueDate = aIssueDate;
    return this;
  }

  /** @return Invoice type code (BT-3) — UNTDID 1001. May be {@code null}. */
  @Nullable
  public String getTypeCode ()
  {
    return m_sTypeCode;
  }

  /**
   * Set the invoice type code (BT-3).
   *
   * @param sTypeCode
   *        UNTDID 1001 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setTypeCode (@Nullable final String sTypeCode)
  {
    m_sTypeCode = sTypeCode;
    return this;
  }

  /** @return Invoice currency code (BT-5) — ISO 4217. May be {@code null}. */
  @Nullable
  public String getCurrency ()
  {
    return m_sCurrency;
  }

  /**
   * Set the invoice currency code (BT-5).
   *
   * @param sCurrency
   *        ISO 4217 currency code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setCurrency (@Nullable final String sCurrency)
  {
    m_sCurrency = sCurrency;
    return this;
  }

  /** @return VAT accounting currency code (BT-6) — ISO 4217. May be {@code null}. */
  @Nullable
  public String getVATAccountingCurrency ()
  {
    return m_sVATAccountingCurrency;
  }

  /**
   * Set the VAT accounting currency code (BT-6).
   *
   * @param sVATAccountingCurrency
   *        ISO 4217 currency code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setVATAccountingCurrency (@Nullable final String sVATAccountingCurrency)
  {
    m_sVATAccountingCurrency = sVATAccountingCurrency;
    return this;
  }

  /** @return Value added tax point date (BT-7). May be {@code null}. */
  @Nullable
  public LocalDate getVATPointDate ()
  {
    return m_aVATPointDate;
  }

  /**
   * Set the value added tax point date (BT-7).
   *
   * @param aVATPointDate
   *        VAT point date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setVATPointDate (@Nullable final LocalDate aVATPointDate)
  {
    m_aVATPointDate = aVATPointDate;
    return this;
  }

  /** @return Value added tax point date code (BT-8). May be {@code null}. */
  @Nullable
  public String getVATPointDateCode ()
  {
    return m_sVATPointDateCode;
  }

  /**
   * Set the value added tax point date code (BT-8).
   *
   * @param sVATPointDateCode
   *        Code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setVATPointDateCode (@Nullable final String sVATPointDateCode)
  {
    m_sVATPointDateCode = sVATPointDateCode;
    return this;
  }

  /** @return Payment due date (BT-9). May be {@code null}. */
  @Nullable
  public LocalDate getPaymentDueDate ()
  {
    return m_aPaymentDueDate;
  }

  /**
   * Set the payment due date (BT-9).
   *
   * @param aPaymentDueDate
   *        Due date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setPaymentDueDate (@Nullable final LocalDate aPaymentDueDate)
  {
    m_aPaymentDueDate = aPaymentDueDate;
    return this;
  }

  /** @return Buyer reference (BT-10). May be {@code null}. */
  @Nullable
  public String getBuyerReference ()
  {
    return m_sBuyerReference;
  }

  /**
   * Set the buyer reference (BT-10).
   *
   * @param sBuyerReference
   *        Buyer reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setBuyerReference (@Nullable final String sBuyerReference)
  {
    m_sBuyerReference = sBuyerReference;
    return this;
  }

  /** @return Project reference (BT-11). May be {@code null}. */
  @Nullable
  public String getProjectReference ()
  {
    return m_sProjectReference;
  }

  /**
   * Set the project reference (BT-11).
   *
   * @param sProjectReference
   *        Project reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setProjectReference (@Nullable final String sProjectReference)
  {
    m_sProjectReference = sProjectReference;
    return this;
  }

  /** @return Contract reference (BT-12). May be {@code null}. */
  @Nullable
  public String getContractReference ()
  {
    return m_sContractReference;
  }

  /**
   * Set the contract reference (BT-12).
   *
   * @param sContractReference
   *        Contract reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setContractReference (@Nullable final String sContractReference)
  {
    m_sContractReference = sContractReference;
    return this;
  }

  /** @return Purchase order reference (BT-13). May be {@code null}. */
  @Nullable
  public String getPurchaseOrderReference ()
  {
    return m_sPurchaseOrderReference;
  }

  /**
   * Set the purchase order reference (BT-13).
   *
   * @param sPurchaseOrderReference
   *        PO reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setPurchaseOrderReference (@Nullable final String sPurchaseOrderReference)
  {
    m_sPurchaseOrderReference = sPurchaseOrderReference;
    return this;
  }

  /** @return Sales order reference (BT-14). May be {@code null}. */
  @Nullable
  public String getSalesOrderReference ()
  {
    return m_sSalesOrderReference;
  }

  /**
   * Set the sales order reference (BT-14).
   *
   * @param sSalesOrderReference
   *        Sales order reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setSalesOrderReference (@Nullable final String sSalesOrderReference)
  {
    m_sSalesOrderReference = sSalesOrderReference;
    return this;
  }

  /** @return Receiving advice reference (BT-15). May be {@code null}. */
  @Nullable
  public String getReceivingAdviceReference ()
  {
    return m_sReceivingAdviceReference;
  }

  /**
   * Set the receiving advice reference (BT-15).
   *
   * @param sReceivingAdviceReference
   *        Receiving advice reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setReceivingAdviceReference (@Nullable final String sReceivingAdviceReference)
  {
    m_sReceivingAdviceReference = sReceivingAdviceReference;
    return this;
  }

  /** @return Despatch advice reference (BT-16). May be {@code null}. */
  @Nullable
  public String getDespatchAdviceReference ()
  {
    return m_sDespatchAdviceReference;
  }

  /**
   * Set the despatch advice reference (BT-16).
   *
   * @param sDespatchAdviceReference
   *        Despatch advice reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setDespatchAdviceReference (@Nullable final String sDespatchAdviceReference)
  {
    m_sDespatchAdviceReference = sDespatchAdviceReference;
    return this;
  }

  /** @return Tender or lot reference (BT-17). May be {@code null}. */
  @Nullable
  public String getTenderOrLotReference ()
  {
    return m_sTenderOrLotReference;
  }

  /**
   * Set the tender or lot reference (BT-17).
   *
   * @param sTenderOrLotReference
   *        Tender / lot reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setTenderOrLotReference (@Nullable final String sTenderOrLotReference)
  {
    m_sTenderOrLotReference = sTenderOrLotReference;
    return this;
  }

  /** @return Invoiced object identifier (BT-18 with BT-18-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getInvoicedObjectIdentifier ()
  {
    return m_aInvoicedObjectIdentifier;
  }

  /**
   * Set the invoiced object identifier (BT-18 / BT-18-1).
   *
   * @param aInvoicedObjectIdentifier
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setInvoicedObjectIdentifier (@Nullable final NormaIdentifier aInvoicedObjectIdentifier)
  {
    m_aInvoicedObjectIdentifier = aInvoicedObjectIdentifier;
    return this;
  }

  /** @return Buyer accounting reference (BT-19). May be {@code null}. */
  @Nullable
  public String getBuyerAccountingReference ()
  {
    return m_sBuyerAccountingReference;
  }

  /**
   * Set the buyer accounting reference (BT-19).
   *
   * @param sBuyerAccountingReference
   *        Accounting cost reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setBuyerAccountingReference (@Nullable final String sBuyerAccountingReference)
  {
    m_sBuyerAccountingReference = sBuyerAccountingReference;
    return this;
  }

  /** @return Payment terms description (BT-20). May be {@code null}. */
  @Nullable
  public String getPaymentTerms ()
  {
    return m_sPaymentTerms;
  }

  /**
   * Set the payment terms description (BT-20).
   *
   * @param sPaymentTerms
   *        Payment terms text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setPaymentTerms (@Nullable final String sPaymentTerms)
  {
    m_sPaymentTerms = sPaymentTerms;
    return this;
  }

  /**
   * @return Mutable list of invoice notes (BG-1). Never {@code null}; empty when none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaInvoiceNote> invoiceNotes ()
  {
    return m_aInvoiceNotes;
  }

  /** @return Process control (BG-2). Never {@code null} (initialised to an empty record). */
  @NonNull
  public NormaProcessControl getProcessControl ()
  {
    return m_aProcessControl;
  }

  /**
   * Set the process control record (BG-2).
   *
   * @param aProcessControl
   *        Process control; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setProcessControl (@NonNull final NormaProcessControl aProcessControl)
  {
    m_aProcessControl = aProcessControl;
    return this;
  }

  /**
   * @return Mutable list of preceding invoice references (BG-3). Never {@code null}; empty when
   *         none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaPrecedingInvoiceRef> precedingInvoiceRefs ()
  {
    return m_aPrecedingInvoiceRefs;
  }

  /** @return Seller (BG-4 with nested BG-5 and BG-6). Never {@code null} (initialised). */
  @NonNull
  public NormaSeller getSeller ()
  {
    return m_aSeller;
  }

  /**
   * Set the seller (BG-4).
   *
   * @param aSeller
   *        Seller; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setSeller (@NonNull final NormaSeller aSeller)
  {
    m_aSeller = aSeller;
    return this;
  }

  /** @return Buyer (BG-7 with nested BG-8 and BG-9). Never {@code null} (initialised). */
  @NonNull
  public NormaBuyer getBuyer ()
  {
    return m_aBuyer;
  }

  /**
   * Set the buyer (BG-7).
   *
   * @param aBuyer
   *        Buyer; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setBuyer (@NonNull final NormaBuyer aBuyer)
  {
    m_aBuyer = aBuyer;
    return this;
  }

  /** @return Payee (BG-10). May be {@code null}. */
  @Nullable
  public NormaPayee getPayee ()
  {
    return m_aPayee;
  }

  /**
   * Set the payee (BG-10).
   *
   * @param aPayee
   *        Payee; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setPayee (@Nullable final NormaPayee aPayee)
  {
    m_aPayee = aPayee;
    return this;
  }

  /** @return Seller tax representative (BG-11 with nested BG-12). May be {@code null}. */
  @Nullable
  public NormaSellerTaxRepresentative getSellerTaxRepresentative ()
  {
    return m_aSellerTaxRepresentative;
  }

  /**
   * Set the seller tax representative (BG-11).
   *
   * @param aSellerTaxRepresentative
   *        Tax representative; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setSellerTaxRepresentative (@Nullable final NormaSellerTaxRepresentative aSellerTaxRepresentative)
  {
    m_aSellerTaxRepresentative = aSellerTaxRepresentative;
    return this;
  }

  /** @return Delivery information (BG-13 with nested BG-15). May be {@code null}. */
  @Nullable
  public NormaDeliveryInformation getDeliveryInformation ()
  {
    return m_aDeliveryInformation;
  }

  /**
   * Set the delivery information (BG-13).
   *
   * @param aDeliveryInformation
   *        Delivery information; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setDeliveryInformation (@Nullable final NormaDeliveryInformation aDeliveryInformation)
  {
    m_aDeliveryInformation = aDeliveryInformation;
    return this;
  }

  /** @return Invoicing period (BG-14). May be {@code null}. */
  @Nullable
  public NormaInvoicingPeriod getInvoicingPeriod ()
  {
    return m_aInvoicingPeriod;
  }

  /**
   * Set the invoicing period (BG-14).
   *
   * @param aInvoicingPeriod
   *        Invoicing period; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setInvoicingPeriod (@Nullable final NormaInvoicingPeriod aInvoicingPeriod)
  {
    m_aInvoicingPeriod = aInvoicingPeriod;
    return this;
  }

  /** @return Payment instructions (BG-16 with nested BG-17/BG-18/BG-19). May be {@code null}. */
  @Nullable
  public NormaPaymentInstructions getPaymentInstructions ()
  {
    return m_aPaymentInstructions;
  }

  /**
   * Set the payment instructions (BG-16).
   *
   * @param aPaymentInstructions
   *        Payment instructions; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setPaymentInstructions (@Nullable final NormaPaymentInstructions aPaymentInstructions)
  {
    m_aPaymentInstructions = aPaymentInstructions;
    return this;
  }

  /**
   * @return Mutable list of document-level allowances (BG-20). Never {@code null}; empty when none
   *         are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaDocumentAllowance> documentAllowances ()
  {
    return m_aDocumentAllowances;
  }

  /**
   * @return Mutable list of document-level charges (BG-21). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaDocumentCharge> documentCharges ()
  {
    return m_aDocumentCharges;
  }

  /** @return Document totals (BG-22). Never {@code null} (initialised to an empty record). */
  @NonNull
  public NormaDocumentTotals getDocumentTotals ()
  {
    return m_aDocumentTotals;
  }

  /**
   * Set the document totals (BG-22).
   *
   * @param aDocumentTotals
   *        Totals; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoice setDocumentTotals (@NonNull final NormaDocumentTotals aDocumentTotals)
  {
    m_aDocumentTotals = aDocumentTotals;
    return this;
  }

  /**
   * @return Mutable list of VAT breakdowns (BG-23). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaVATBreakdown> vatBreakdowns ()
  {
    return m_aVATBreakdowns;
  }

  /**
   * @return Mutable list of supporting documents (BG-24). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaSupportingDocument> supportingDocuments ()
  {
    return m_aSupportingDocuments;
  }

  /**
   * @return Mutable list of invoice lines (BG-25). Never {@code null}; the EN 16931 model requires
   *         at least one entry for a valid invoice.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaInvoiceLine> lines ()
  {
    return m_aLines;
  }
}
