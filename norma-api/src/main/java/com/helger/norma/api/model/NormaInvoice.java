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
 * Root of the EN 16931 intermediate model. One instance represents one Invoice or one Credit
 * Note, populated by a source reader and consumed by an output writer. All optional fields
 * are {@code null} when not present in the source. All collections are non-{@code null} and
 * empty when not populated.
 *
 * @author Philip Helger
 */
public class NormaInvoice
{
  // Document kind
  private ENormaDocumentKind m_eKind = ENormaDocumentKind.INVOICE;

  // Header-level BTs (no BG)
  private String m_sNumber;                     // BT-1
  private LocalDate m_aIssueDate;               // BT-2
  private String m_sTypeCode;                   // BT-3
  private String m_sCurrency;                   // BT-5
  private String m_sVATAccountingCurrency;      // BT-6
  private LocalDate m_aVATPointDate;            // BT-7
  private String m_sVATPointDateCode;           // BT-8
  private LocalDate m_aPaymentDueDate;          // BT-9
  private String m_sBuyerReference;             // BT-10
  private String m_sProjectReference;           // BT-11
  private String m_sContractReference;          // BT-12
  private String m_sPurchaseOrderReference;     // BT-13
  private String m_sSalesOrderReference;        // BT-14
  private String m_sReceivingAdviceReference;   // BT-15
  private String m_sDespatchAdviceReference;    // BT-16
  private String m_sTenderOrLotReference;       // BT-17
  private NormaIdentifier m_aInvoicedObjectIdentifier; // BT-18 / BT-18-1
  private String m_sBuyerAccountingReference;   // BT-19
  private String m_sPaymentTerms;               // BT-20

  // BGs
  private final ICommonsList <NormaInvoiceNote> m_aInvoiceNotes = new CommonsArrayList <> ();        // BG-1
  private NormaProcessControl m_aProcessControl = new NormaProcessControl ();                       // BG-2
  private final ICommonsList <NormaPrecedingInvoiceRef> m_aPrecedingInvoiceRefs = new CommonsArrayList <> (); // BG-3
  private NormaSeller m_aSeller = new NormaSeller ();                                               // BG-4 (+5/+6)
  private NormaBuyer m_aBuyer = new NormaBuyer ();                                                  // BG-7 (+8/+9)
  private NormaPayee m_aPayee;                                                                      // BG-10
  private NormaSellerTaxRepresentative m_aSellerTaxRepresentative;                                  // BG-11 (+12)
  private NormaDeliveryInformation m_aDeliveryInformation;                                          // BG-13 (+15)
  private NormaInvoicingPeriod m_aInvoicingPeriod;                                                  // BG-14
  private NormaPaymentInstructions m_aPaymentInstructions;                                          // BG-16 (+17/+18/+19)
  private final ICommonsList <NormaDocumentAllowance> m_aDocumentAllowances = new CommonsArrayList <> (); // BG-20
  private final ICommonsList <NormaDocumentCharge> m_aDocumentCharges = new CommonsArrayList <> ();       // BG-21
  private NormaDocumentTotals m_aDocumentTotals = new NormaDocumentTotals ();                       // BG-22
  private final ICommonsList <NormaVATBreakdown> m_aVATBreakdowns = new CommonsArrayList <> ();           // BG-23
  private final ICommonsList <NormaSupportingDocument> m_aSupportingDocuments = new CommonsArrayList <> (); // BG-24
  private final ICommonsList <NormaInvoiceLine> m_aLines = new CommonsArrayList <> ();              // BG-25

  public NormaInvoice ()
  {}

  // === Header-level ===

  @NonNull
  public ENormaDocumentKind getKind ()
  {
    return m_eKind;
  }

  @NonNull
  public NormaInvoice setKind (@NonNull final ENormaDocumentKind eKind)
  {
    m_eKind = eKind;
    return this;
  }

  public boolean isInvoice ()
  {
    return m_eKind == ENormaDocumentKind.INVOICE;
  }

  public boolean isCreditNote ()
  {
    return m_eKind == ENormaDocumentKind.CREDIT_NOTE;
  }

  @Nullable
  public String getNumber ()
  {
    return m_sNumber;
  }

  @NonNull
  public NormaInvoice setNumber (@Nullable final String sNumber)
  {
    m_sNumber = sNumber;
    return this;
  }

  @Nullable
  public LocalDate getIssueDate ()
  {
    return m_aIssueDate;
  }

  @NonNull
  public NormaInvoice setIssueDate (@Nullable final LocalDate aIssueDate)
  {
    m_aIssueDate = aIssueDate;
    return this;
  }

  @Nullable
  public String getTypeCode ()
  {
    return m_sTypeCode;
  }

  @NonNull
  public NormaInvoice setTypeCode (@Nullable final String sTypeCode)
  {
    m_sTypeCode = sTypeCode;
    return this;
  }

  @Nullable
  public String getCurrency ()
  {
    return m_sCurrency;
  }

  @NonNull
  public NormaInvoice setCurrency (@Nullable final String sCurrency)
  {
    m_sCurrency = sCurrency;
    return this;
  }

  @Nullable
  public String getVATAccountingCurrency ()
  {
    return m_sVATAccountingCurrency;
  }

  @NonNull
  public NormaInvoice setVATAccountingCurrency (@Nullable final String sVATAccountingCurrency)
  {
    m_sVATAccountingCurrency = sVATAccountingCurrency;
    return this;
  }

  @Nullable
  public LocalDate getVATPointDate ()
  {
    return m_aVATPointDate;
  }

  @NonNull
  public NormaInvoice setVATPointDate (@Nullable final LocalDate aVATPointDate)
  {
    m_aVATPointDate = aVATPointDate;
    return this;
  }

  @Nullable
  public String getVATPointDateCode ()
  {
    return m_sVATPointDateCode;
  }

  @NonNull
  public NormaInvoice setVATPointDateCode (@Nullable final String sVATPointDateCode)
  {
    m_sVATPointDateCode = sVATPointDateCode;
    return this;
  }

  @Nullable
  public LocalDate getPaymentDueDate ()
  {
    return m_aPaymentDueDate;
  }

  @NonNull
  public NormaInvoice setPaymentDueDate (@Nullable final LocalDate aPaymentDueDate)
  {
    m_aPaymentDueDate = aPaymentDueDate;
    return this;
  }

  @Nullable
  public String getBuyerReference ()
  {
    return m_sBuyerReference;
  }

  @NonNull
  public NormaInvoice setBuyerReference (@Nullable final String sBuyerReference)
  {
    m_sBuyerReference = sBuyerReference;
    return this;
  }

  @Nullable
  public String getProjectReference ()
  {
    return m_sProjectReference;
  }

  @NonNull
  public NormaInvoice setProjectReference (@Nullable final String sProjectReference)
  {
    m_sProjectReference = sProjectReference;
    return this;
  }

  @Nullable
  public String getContractReference ()
  {
    return m_sContractReference;
  }

  @NonNull
  public NormaInvoice setContractReference (@Nullable final String sContractReference)
  {
    m_sContractReference = sContractReference;
    return this;
  }

  @Nullable
  public String getPurchaseOrderReference ()
  {
    return m_sPurchaseOrderReference;
  }

  @NonNull
  public NormaInvoice setPurchaseOrderReference (@Nullable final String sPurchaseOrderReference)
  {
    m_sPurchaseOrderReference = sPurchaseOrderReference;
    return this;
  }

  @Nullable
  public String getSalesOrderReference ()
  {
    return m_sSalesOrderReference;
  }

  @NonNull
  public NormaInvoice setSalesOrderReference (@Nullable final String sSalesOrderReference)
  {
    m_sSalesOrderReference = sSalesOrderReference;
    return this;
  }

  @Nullable
  public String getReceivingAdviceReference ()
  {
    return m_sReceivingAdviceReference;
  }

  @NonNull
  public NormaInvoice setReceivingAdviceReference (@Nullable final String sReceivingAdviceReference)
  {
    m_sReceivingAdviceReference = sReceivingAdviceReference;
    return this;
  }

  @Nullable
  public String getDespatchAdviceReference ()
  {
    return m_sDespatchAdviceReference;
  }

  @NonNull
  public NormaInvoice setDespatchAdviceReference (@Nullable final String sDespatchAdviceReference)
  {
    m_sDespatchAdviceReference = sDespatchAdviceReference;
    return this;
  }

  @Nullable
  public String getTenderOrLotReference ()
  {
    return m_sTenderOrLotReference;
  }

  @NonNull
  public NormaInvoice setTenderOrLotReference (@Nullable final String sTenderOrLotReference)
  {
    m_sTenderOrLotReference = sTenderOrLotReference;
    return this;
  }

  @Nullable
  public NormaIdentifier getInvoicedObjectIdentifier ()
  {
    return m_aInvoicedObjectIdentifier;
  }

  @NonNull
  public NormaInvoice setInvoicedObjectIdentifier (@Nullable final NormaIdentifier aInvoicedObjectIdentifier)
  {
    m_aInvoicedObjectIdentifier = aInvoicedObjectIdentifier;
    return this;
  }

  @Nullable
  public String getBuyerAccountingReference ()
  {
    return m_sBuyerAccountingReference;
  }

  @NonNull
  public NormaInvoice setBuyerAccountingReference (@Nullable final String sBuyerAccountingReference)
  {
    m_sBuyerAccountingReference = sBuyerAccountingReference;
    return this;
  }

  @Nullable
  public String getPaymentTerms ()
  {
    return m_sPaymentTerms;
  }

  @NonNull
  public NormaInvoice setPaymentTerms (@Nullable final String sPaymentTerms)
  {
    m_sPaymentTerms = sPaymentTerms;
    return this;
  }

  // === BGs ===

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaInvoiceNote> invoiceNotes ()
  {
    return m_aInvoiceNotes;
  }

  @NonNull
  public NormaProcessControl getProcessControl ()
  {
    return m_aProcessControl;
  }

  @NonNull
  public NormaInvoice setProcessControl (@NonNull final NormaProcessControl aProcessControl)
  {
    m_aProcessControl = aProcessControl;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaPrecedingInvoiceRef> precedingInvoiceRefs ()
  {
    return m_aPrecedingInvoiceRefs;
  }

  @NonNull
  public NormaSeller getSeller ()
  {
    return m_aSeller;
  }

  @NonNull
  public NormaInvoice setSeller (@NonNull final NormaSeller aSeller)
  {
    m_aSeller = aSeller;
    return this;
  }

  @NonNull
  public NormaBuyer getBuyer ()
  {
    return m_aBuyer;
  }

  @NonNull
  public NormaInvoice setBuyer (@NonNull final NormaBuyer aBuyer)
  {
    m_aBuyer = aBuyer;
    return this;
  }

  @Nullable
  public NormaPayee getPayee ()
  {
    return m_aPayee;
  }

  @NonNull
  public NormaInvoice setPayee (@Nullable final NormaPayee aPayee)
  {
    m_aPayee = aPayee;
    return this;
  }

  @Nullable
  public NormaSellerTaxRepresentative getSellerTaxRepresentative ()
  {
    return m_aSellerTaxRepresentative;
  }

  @NonNull
  public NormaInvoice setSellerTaxRepresentative (@Nullable final NormaSellerTaxRepresentative aSellerTaxRepresentative)
  {
    m_aSellerTaxRepresentative = aSellerTaxRepresentative;
    return this;
  }

  @Nullable
  public NormaDeliveryInformation getDeliveryInformation ()
  {
    return m_aDeliveryInformation;
  }

  @NonNull
  public NormaInvoice setDeliveryInformation (@Nullable final NormaDeliveryInformation aDeliveryInformation)
  {
    m_aDeliveryInformation = aDeliveryInformation;
    return this;
  }

  @Nullable
  public NormaInvoicingPeriod getInvoicingPeriod ()
  {
    return m_aInvoicingPeriod;
  }

  @NonNull
  public NormaInvoice setInvoicingPeriod (@Nullable final NormaInvoicingPeriod aInvoicingPeriod)
  {
    m_aInvoicingPeriod = aInvoicingPeriod;
    return this;
  }

  @Nullable
  public NormaPaymentInstructions getPaymentInstructions ()
  {
    return m_aPaymentInstructions;
  }

  @NonNull
  public NormaInvoice setPaymentInstructions (@Nullable final NormaPaymentInstructions aPaymentInstructions)
  {
    m_aPaymentInstructions = aPaymentInstructions;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaDocumentAllowance> documentAllowances ()
  {
    return m_aDocumentAllowances;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaDocumentCharge> documentCharges ()
  {
    return m_aDocumentCharges;
  }

  @NonNull
  public NormaDocumentTotals getDocumentTotals ()
  {
    return m_aDocumentTotals;
  }

  @NonNull
  public NormaInvoice setDocumentTotals (@NonNull final NormaDocumentTotals aDocumentTotals)
  {
    m_aDocumentTotals = aDocumentTotals;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaVATBreakdown> vatBreakdowns ()
  {
    return m_aVATBreakdowns;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaSupportingDocument> supportingDocuments ()
  {
    return m_aSupportingDocuments;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaInvoiceLine> lines ()
  {
    return m_aLines;
  }
}
