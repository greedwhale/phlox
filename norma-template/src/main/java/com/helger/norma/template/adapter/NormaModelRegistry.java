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
package com.helger.norma.template.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.norma.api.model.NormaAttachment;
import com.helger.norma.api.model.NormaBuyer;
import com.helger.norma.api.model.NormaContact;
import com.helger.norma.api.model.NormaCreditTransfer;
import com.helger.norma.api.model.NormaDeliveryInformation;
import com.helger.norma.api.model.NormaDirectDebit;
import com.helger.norma.api.model.NormaDocumentAllowance;
import com.helger.norma.api.model.NormaDocumentCharge;
import com.helger.norma.api.model.NormaDocumentTotals;
import com.helger.norma.api.model.NormaIdentifier;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.api.model.NormaInvoiceLine;
import com.helger.norma.api.model.NormaInvoiceNote;
import com.helger.norma.api.model.NormaInvoicingPeriod;
import com.helger.norma.api.model.NormaItem;
import com.helger.norma.api.model.NormaLineAllowance;
import com.helger.norma.api.model.NormaLineCharge;
import com.helger.norma.api.model.NormaLineVAT;
import com.helger.norma.api.model.NormaPayee;
import com.helger.norma.api.model.NormaPaymentCard;
import com.helger.norma.api.model.NormaPaymentInstructions;
import com.helger.norma.api.model.NormaPostalAddress;
import com.helger.norma.api.model.NormaPrecedingInvoiceRef;
import com.helger.norma.api.model.NormaPriceDetails;
import com.helger.norma.api.model.NormaProcessControl;
import com.helger.norma.api.model.NormaSeller;
import com.helger.norma.api.model.NormaSellerTaxRepresentative;
import com.helger.norma.api.model.NormaSupportingDocument;
import com.helger.norma.api.model.NormaVATBreakdown;

/**
 * Static (Class, BT/BG name) → getter registry. Drives the Mustache adapter's name resolution.
 * Every BT/BG referenced by {@code default.ntpl} (and by extension, every BT/BG documented in
 * {@code wiki/Domain-Model.md}) is registered here.
 * <p>
 * Multiple BG positions sharing a Java type — e.g. {@code NormaPostalAddress} is reached via
 * BG-5 (BT-35..40), BG-8 (BT-50..55), BG-12 (BT-64..69) and BG-15 (BT-75..80) — register all
 * BT aliases against the single Java class so any usage site resolves correctly.
 *
 * @author Philip Helger
 */
public final class NormaModelRegistry
{
  private static final Map <Class <?>, Map <String, Function <Object, Object>>> REG = new HashMap <> ();

  static
  {
    // ─── Top-level invoice header — BT-1..BT-20 ──────────────────────────
    _add (NormaInvoice.class, "BT-1", NormaInvoice::getNumber);
    _add (NormaInvoice.class, "BT-2", NormaInvoice::getIssueDate);
    _add (NormaInvoice.class, "BT-3", NormaInvoice::getTypeCode);
    _add (NormaInvoice.class, "BT-5", NormaInvoice::getCurrency);
    _add (NormaInvoice.class, "BT-6", NormaInvoice::getVATAccountingCurrency);
    _add (NormaInvoice.class, "BT-7", NormaInvoice::getVATPointDate);
    _add (NormaInvoice.class, "BT-8", NormaInvoice::getVATPointDateCode);
    _add (NormaInvoice.class, "BT-9", NormaInvoice::getPaymentDueDate);
    _add (NormaInvoice.class, "BT-10", NormaInvoice::getBuyerReference);
    _add (NormaInvoice.class, "BT-11", NormaInvoice::getProjectReference);
    _add (NormaInvoice.class, "BT-12", NormaInvoice::getContractReference);
    _add (NormaInvoice.class, "BT-13", NormaInvoice::getPurchaseOrderReference);
    _add (NormaInvoice.class, "BT-14", NormaInvoice::getSalesOrderReference);
    _add (NormaInvoice.class, "BT-15", NormaInvoice::getReceivingAdviceReference);
    _add (NormaInvoice.class, "BT-16", NormaInvoice::getDespatchAdviceReference);
    _add (NormaInvoice.class, "BT-17", NormaInvoice::getTenderOrLotReference);
    _add (NormaInvoice.class, "BT-18", NormaInvoice::getInvoicedObjectIdentifier);
    _add (NormaInvoice.class, "BT-19", NormaInvoice::getBuyerAccountingReference);
    _add (NormaInvoice.class, "BT-20", NormaInvoice::getPaymentTerms);

    // ─── Top-level BG containers and lists ───────────────────────────────
    _add (NormaInvoice.class, "BG-1", NormaInvoice::invoiceNotes);
    _add (NormaInvoice.class, "BG-2", NormaInvoice::getProcessControl);
    _add (NormaInvoice.class, "BG-3", NormaInvoice::precedingInvoiceRefs);
    _add (NormaInvoice.class, "BG-4", NormaInvoice::getSeller);
    _add (NormaInvoice.class, "BG-7", NormaInvoice::getBuyer);
    _add (NormaInvoice.class, "BG-10", NormaInvoice::getPayee);
    _add (NormaInvoice.class, "BG-11", NormaInvoice::getSellerTaxRepresentative);
    _add (NormaInvoice.class, "BG-13", NormaInvoice::getDeliveryInformation);
    _add (NormaInvoice.class, "BG-14", NormaInvoice::getInvoicingPeriod);
    _add (NormaInvoice.class, "BG-16", NormaInvoice::getPaymentInstructions);
    _add (NormaInvoice.class, "BG-20", NormaInvoice::documentAllowances);
    _add (NormaInvoice.class, "BG-21", NormaInvoice::documentCharges);
    _add (NormaInvoice.class, "BG-22", NormaInvoice::getDocumentTotals);
    _add (NormaInvoice.class, "BG-23", NormaInvoice::vatBreakdowns);
    _add (NormaInvoice.class, "BG-24", NormaInvoice::supportingDocuments);
    _add (NormaInvoice.class, "BG-25", NormaInvoice::lines);

    // ─── BG-2 — Process control ──────────────────────────────────────────
    _add (NormaProcessControl.class, "BT-23", NormaProcessControl::getBusinessProcessType);
    _add (NormaProcessControl.class, "BT-24", NormaProcessControl::getSpecificationIdentifier);

    // ─── BG-4 — Seller ───────────────────────────────────────────────────
    _add (NormaSeller.class, "BT-27", NormaSeller::getName);
    _add (NormaSeller.class, "BT-28", NormaSeller::getTradingName);
    _add (NormaSeller.class, "BT-30", NormaSeller::getLegalRegistration);
    _add (NormaSeller.class, "BT-31", NormaSeller::getVATIdentifier);
    _add (NormaSeller.class, "BT-32", NormaSeller::getTaxRegistration);
    _add (NormaSeller.class, "BT-33", NormaSeller::getAdditionalLegalInfo);
    _add (NormaSeller.class, "BT-34", NormaSeller::getElectronicAddress);
    _add (NormaSeller.class, "BG-5", NormaSeller::getPostalAddress);
    _add (NormaSeller.class, "BG-6", NormaSeller::getContact);

    // ─── BG-7 — Buyer ────────────────────────────────────────────────────
    _add (NormaBuyer.class, "BT-44", NormaBuyer::getName);
    _add (NormaBuyer.class, "BT-45", NormaBuyer::getTradingName);
    _add (NormaBuyer.class, "BT-47", NormaBuyer::getLegalRegistration);
    _add (NormaBuyer.class, "BT-48", NormaBuyer::getVATIdentifier);
    _add (NormaBuyer.class, "BT-49", NormaBuyer::getElectronicAddress);
    _add (NormaBuyer.class, "BG-8", NormaBuyer::getPostalAddress);
    _add (NormaBuyer.class, "BG-9", NormaBuyer::getContact);

    // ─── Postal address — shared by BG-5 / BG-8 / BG-12 / BG-15 ──────────
    // BG-5 (seller):       BT-35..40
    // BG-8 (buyer):        BT-50..55
    // BG-12 (tax rep):     BT-64, BT-66, BT-67, BT-69
    // BG-15 (deliver-to):  BT-75, BT-77, BT-78, BT-80
    _add (NormaPostalAddress.class, "BT-35", NormaPostalAddress::getLine1);
    _add (NormaPostalAddress.class, "BT-36", NormaPostalAddress::getLine2);
    _add (NormaPostalAddress.class, "BT-37", NormaPostalAddress::getCity);
    _add (NormaPostalAddress.class, "BT-38", NormaPostalAddress::getPostCode);
    _add (NormaPostalAddress.class, "BT-39", NormaPostalAddress::getCountrySubdivision);
    _add (NormaPostalAddress.class, "BT-40", NormaPostalAddress::getCountryCode);
    _add (NormaPostalAddress.class, "BT-50", NormaPostalAddress::getLine1);
    _add (NormaPostalAddress.class, "BT-51", NormaPostalAddress::getLine2);
    _add (NormaPostalAddress.class, "BT-52", NormaPostalAddress::getCity);
    _add (NormaPostalAddress.class, "BT-53", NormaPostalAddress::getPostCode);
    _add (NormaPostalAddress.class, "BT-54", NormaPostalAddress::getCountrySubdivision);
    _add (NormaPostalAddress.class, "BT-55", NormaPostalAddress::getCountryCode);
    _add (NormaPostalAddress.class, "BT-64", NormaPostalAddress::getLine1);
    _add (NormaPostalAddress.class, "BT-66", NormaPostalAddress::getCity);
    _add (NormaPostalAddress.class, "BT-67", NormaPostalAddress::getPostCode);
    _add (NormaPostalAddress.class, "BT-69", NormaPostalAddress::getCountryCode);
    _add (NormaPostalAddress.class, "BT-75", NormaPostalAddress::getLine1);
    _add (NormaPostalAddress.class, "BT-77", NormaPostalAddress::getCity);
    _add (NormaPostalAddress.class, "BT-78", NormaPostalAddress::getPostCode);
    _add (NormaPostalAddress.class, "BT-80", NormaPostalAddress::getCountryCode);

    // ─── Contact — BG-6 (seller) / BG-9 (buyer) ──────────────────────────
    _add (NormaContact.class, "BT-41", NormaContact::getName);
    _add (NormaContact.class, "BT-42", NormaContact::getTelephone);
    _add (NormaContact.class, "BT-43", NormaContact::getEmail);
    _add (NormaContact.class, "BT-56", NormaContact::getName);
    _add (NormaContact.class, "BT-57", NormaContact::getTelephone);
    _add (NormaContact.class, "BT-58", NormaContact::getEmail);

    // ─── BG-10 — Payee ───────────────────────────────────────────────────
    _add (NormaPayee.class, "BT-59", NormaPayee::getName);
    _add (NormaPayee.class, "BT-60", NormaPayee::getIdentifier);
    _add (NormaPayee.class, "BT-61", NormaPayee::getLegalRegistration);

    // ─── BG-11 — Seller tax representative ───────────────────────────────
    _add (NormaSellerTaxRepresentative.class, "BT-62", NormaSellerTaxRepresentative::getName);
    _add (NormaSellerTaxRepresentative.class, "BT-63", NormaSellerTaxRepresentative::getVATIdentifier);
    _add (NormaSellerTaxRepresentative.class, "BG-12", NormaSellerTaxRepresentative::getPostalAddress);

    // ─── BG-13 — Delivery information ────────────────────────────────────
    _add (NormaDeliveryInformation.class, "BT-70", NormaDeliveryInformation::getPartyName);
    _add (NormaDeliveryInformation.class, "BT-71", NormaDeliveryInformation::getLocationIdentifier);
    _add (NormaDeliveryInformation.class, "BT-72", NormaDeliveryInformation::getActualDeliveryDate);
    _add (NormaDeliveryInformation.class, "BG-15", NormaDeliveryInformation::getPostalAddress);

    // ─── BG-14 / BG-26 — Invoicing period (shared class) ─────────────────
    _add (NormaInvoicingPeriod.class, "BT-73", NormaInvoicingPeriod::getStartDate);
    _add (NormaInvoicingPeriod.class, "BT-74", NormaInvoicingPeriod::getEndDate);
    _add (NormaInvoicingPeriod.class, "BT-134", NormaInvoicingPeriod::getStartDate);
    _add (NormaInvoicingPeriod.class, "BT-135", NormaInvoicingPeriod::getEndDate);

    // ─── BG-16 — Payment instructions ────────────────────────────────────
    _add (NormaPaymentInstructions.class, "BT-81", NormaPaymentInstructions::getPaymentMeansCode);
    _add (NormaPaymentInstructions.class, "BT-82", NormaPaymentInstructions::getPaymentMeansText);
    _add (NormaPaymentInstructions.class, "BT-83", NormaPaymentInstructions::getRemittanceInformation);
    _add (NormaPaymentInstructions.class, "BG-17", NormaPaymentInstructions::creditTransfers);
    _add (NormaPaymentInstructions.class, "BG-18", NormaPaymentInstructions::getPaymentCard);
    _add (NormaPaymentInstructions.class, "BG-19", NormaPaymentInstructions::getDirectDebit);

    // ─── BG-17 — Credit transfer ─────────────────────────────────────────
    _add (NormaCreditTransfer.class, "BT-84", NormaCreditTransfer::getAccountIdentifier);
    _add (NormaCreditTransfer.class, "BT-85", NormaCreditTransfer::getAccountName);
    _add (NormaCreditTransfer.class, "BT-86", NormaCreditTransfer::getServiceProviderID);

    // ─── BG-18 — Payment card ────────────────────────────────────────────
    _add (NormaPaymentCard.class, "BT-87", NormaPaymentCard::getPAN);
    _add (NormaPaymentCard.class, "BT-88", NormaPaymentCard::getHolderName);

    // ─── BG-19 — Direct debit ────────────────────────────────────────────
    _add (NormaDirectDebit.class, "BT-89", NormaDirectDebit::getMandateReferenceID);
    _add (NormaDirectDebit.class, "BT-90", NormaDirectDebit::getBankAssignedCreditorID);
    _add (NormaDirectDebit.class, "BT-91", NormaDirectDebit::getDebitedAccountID);

    // ─── BG-20 — Document-level allowance ────────────────────────────────
    _add (NormaDocumentAllowance.class, "BT-92", NormaDocumentAllowance::getAmount);
    _add (NormaDocumentAllowance.class, "BT-95", NormaDocumentAllowance::getVATCategoryCode);
    _add (NormaDocumentAllowance.class, "BT-96", NormaDocumentAllowance::getVATRate);
    _add (NormaDocumentAllowance.class, "BT-97", NormaDocumentAllowance::getReason);
    _add (NormaDocumentAllowance.class, "BT-98", NormaDocumentAllowance::getReasonCode);

    // ─── BG-21 — Document-level charge ───────────────────────────────────
    _add (NormaDocumentCharge.class, "BT-99", NormaDocumentCharge::getAmount);
    _add (NormaDocumentCharge.class, "BT-102", NormaDocumentCharge::getVATCategoryCode);
    _add (NormaDocumentCharge.class, "BT-103", NormaDocumentCharge::getVATRate);
    _add (NormaDocumentCharge.class, "BT-104", NormaDocumentCharge::getReason);
    _add (NormaDocumentCharge.class, "BT-105", NormaDocumentCharge::getReasonCode);

    // ─── BG-22 — Document totals ─────────────────────────────────────────
    _add (NormaDocumentTotals.class, "BT-106", NormaDocumentTotals::getLineNetTotal);
    _add (NormaDocumentTotals.class, "BT-107", NormaDocumentTotals::getAllowanceTotal);
    _add (NormaDocumentTotals.class, "BT-108", NormaDocumentTotals::getChargeTotal);
    _add (NormaDocumentTotals.class, "BT-109", NormaDocumentTotals::getTaxExclusiveTotal);
    _add (NormaDocumentTotals.class, "BT-110", NormaDocumentTotals::getTaxTotal);
    _add (NormaDocumentTotals.class, "BT-111", NormaDocumentTotals::getTaxTotalAccountingCurrency);
    _add (NormaDocumentTotals.class, "BT-112", NormaDocumentTotals::getTaxInclusiveTotal);
    _add (NormaDocumentTotals.class, "BT-113", NormaDocumentTotals::getPrepaidAmount);
    _add (NormaDocumentTotals.class, "BT-114", NormaDocumentTotals::getRoundingAmount);
    _add (NormaDocumentTotals.class, "BT-115", NormaDocumentTotals::getPayableAmount);

    // ─── BG-23 — VAT breakdown ───────────────────────────────────────────
    _add (NormaVATBreakdown.class, "BT-116", NormaVATBreakdown::getTaxableAmount);
    _add (NormaVATBreakdown.class, "BT-117", NormaVATBreakdown::getTaxAmount);
    _add (NormaVATBreakdown.class, "BT-118", NormaVATBreakdown::getCategoryCode);
    _add (NormaVATBreakdown.class, "BT-119", NormaVATBreakdown::getRate);
    _add (NormaVATBreakdown.class, "BT-120", NormaVATBreakdown::getExemptionReason);
    _add (NormaVATBreakdown.class, "BT-121", NormaVATBreakdown::getExemptionReasonCode);

    // ─── BG-24 — Supporting document ─────────────────────────────────────
    _add (NormaSupportingDocument.class, "BT-122", NormaSupportingDocument::getReference);
    _add (NormaSupportingDocument.class, "BT-123", NormaSupportingDocument::getDescription);
    _add (NormaSupportingDocument.class, "BT-124", NormaSupportingDocument::getExternalLocation);
    _add (NormaSupportingDocument.class, "BT-125", NormaSupportingDocument::getAttachment);

    // ─── BG-25 — Invoice line ────────────────────────────────────────────
    _add (NormaInvoiceLine.class, "BT-126", NormaInvoiceLine::getID);
    _add (NormaInvoiceLine.class, "BT-127", NormaInvoiceLine::getNote);
    _add (NormaInvoiceLine.class, "BT-128", NormaInvoiceLine::getObjectIdentifier);
    _add (NormaInvoiceLine.class, "BT-129", NormaInvoiceLine::getQuantity);
    _add (NormaInvoiceLine.class, "BT-130", NormaInvoiceLine::getQuantityUnit);
    _add (NormaInvoiceLine.class, "BT-131", NormaInvoiceLine::getNetAmount);
    _add (NormaInvoiceLine.class, "BT-132", NormaInvoiceLine::getPurchaseOrderLineRef);
    _add (NormaInvoiceLine.class, "BT-133", NormaInvoiceLine::getAccountingCost);
    _add (NormaInvoiceLine.class, "BG-26", NormaInvoiceLine::getPeriod);
    _add (NormaInvoiceLine.class, "BG-27", NormaInvoiceLine::allowances);
    _add (NormaInvoiceLine.class, "BG-28", NormaInvoiceLine::charges);
    _add (NormaInvoiceLine.class, "BG-29", NormaInvoiceLine::getPrice);
    _add (NormaInvoiceLine.class, "BG-30", NormaInvoiceLine::getLineVAT);
    _add (NormaInvoiceLine.class, "BG-31", NormaInvoiceLine::getItem);

    // ─── BG-1 — Invoice note ─────────────────────────────────────────────
    _add (NormaInvoiceNote.class, "BT-21", NormaInvoiceNote::getSubjectCode);
    _add (NormaInvoiceNote.class, "BT-22", NormaInvoiceNote::getContent);

    // ─── BG-3 — Preceding invoice ref ────────────────────────────────────
    _add (NormaPrecedingInvoiceRef.class, "BT-25", NormaPrecedingInvoiceRef::getNumber);
    _add (NormaPrecedingInvoiceRef.class, "BT-26", NormaPrecedingInvoiceRef::getIssueDate);

    // ─── BG-27 — Line allowance ──────────────────────────────────────────
    _add (NormaLineAllowance.class, "BT-136", NormaLineAllowance::getAmount);
    _add (NormaLineAllowance.class, "BT-139", NormaLineAllowance::getReason);

    // ─── BG-28 — Line charge ─────────────────────────────────────────────
    _add (NormaLineCharge.class, "BT-141", NormaLineCharge::getAmount);
    _add (NormaLineCharge.class, "BT-144", NormaLineCharge::getReason);

    // ─── BG-29 — Price details ───────────────────────────────────────────
    _add (NormaPriceDetails.class, "BT-146", NormaPriceDetails::getNetPrice);

    // ─── BG-30 — Line VAT ────────────────────────────────────────────────
    _add (NormaLineVAT.class, "BT-151", NormaLineVAT::getCategoryCode);
    _add (NormaLineVAT.class, "BT-152", NormaLineVAT::getRate);

    // ─── BG-31 — Item ────────────────────────────────────────────────────
    _add (NormaItem.class, "BT-153", NormaItem::getName);
    _add (NormaItem.class, "BT-154", NormaItem::getDescription);
    _add (NormaItem.class, "BT-155", NormaItem::getSellerItemID);

    // ─── NormaIdentifier — flatten to scheme-prefixed value when present ─
    _add (NormaIdentifier.class, "value", NormaIdentifier::getValue);
    _add (NormaIdentifier.class, "schemeID", NormaIdentifier::getSchemeID);

    // ─── NormaAttachment — used by attachment:logo / attachment:BG-24[i] ─
    _add (NormaAttachment.class, "filename", NormaAttachment::getFilename);
    _add (NormaAttachment.class, "mimeCode", NormaAttachment::getMimeCode);
  }

  private NormaModelRegistry ()
  {}

  private static <T> void _add (@NonNull final Class <T> aClass,
                                @NonNull final String sName,
                                @NonNull final Function <? super T, ?> aGetter)
  {
    final Map <String, Function <Object, Object>> aMap = REG.computeIfAbsent (aClass, k -> new HashMap <> ());
    @SuppressWarnings ("unchecked")
    final Function <Object, Object> aBoxed = o -> aGetter.apply ((T) o);
    aMap.put (sName, aBoxed);
  }

  /**
   * @param aCtx
   *        The current context value (model object).
   * @param sName
   *        The bare BT/BG name (no modifier, no leading {@code ?}).
   * @return The resolved value, or {@code null} when the name is not known for the context's
   *         type or the getter returned {@code null}.
   */
  @Nullable
  public static Object resolve (@NonNull final Object aCtx, @NonNull final String sName)
  {
    final Map <String, Function <Object, Object>> aMap = REG.get (aCtx.getClass ());
    if (aMap == null)
      return null;
    final Function <Object, Object> aGetter = aMap.get (sName);
    if (aGetter == null)
      return null;
    return aGetter.apply (aCtx);
  }

  /** @return {@code true} when {@code aClass} has any registered name. */
  public static boolean knows (@NonNull final Class <?> aClass)
  {
    return REG.containsKey (aClass);
  }
}
