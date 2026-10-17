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
package com.helger.phlox.template.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.string.StringHelper;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxBuyer;
import com.helger.phlox.api.model.PhloxContact;
import com.helger.phlox.api.model.PhloxCreditTransfer;
import com.helger.phlox.api.model.PhloxDeliveryInformation;
import com.helger.phlox.api.model.PhloxDirectDebit;
import com.helger.phlox.api.model.PhloxDocumentAllowance;
import com.helger.phlox.api.model.PhloxDocumentCharge;
import com.helger.phlox.api.model.PhloxDocumentTotals;
import com.helger.phlox.api.model.PhloxEarlyPaymentDiscount;
import com.helger.phlox.api.model.PhloxIdentifier;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.model.PhloxInvoiceLine;
import com.helger.phlox.api.model.PhloxInvoiceNote;
import com.helger.phlox.api.model.PhloxInvoicingPeriod;
import com.helger.phlox.api.model.PhloxItem;
import com.helger.phlox.api.model.PhloxItemAttribute;
import com.helger.phlox.api.model.PhloxLatePaymentPenalty;
import com.helger.phlox.api.model.PhloxLineAllowance;
import com.helger.phlox.api.model.PhloxLineCharge;
import com.helger.phlox.api.model.PhloxLineDelivery;
import com.helger.phlox.api.model.PhloxLinePrecedingInvoiceRef;
import com.helger.phlox.api.model.PhloxLineVat;
import com.helger.phlox.api.model.PhloxPayee;
import com.helger.phlox.api.model.PhloxPaymentCard;
import com.helger.phlox.api.model.PhloxPaymentInstructions;
import com.helger.phlox.api.model.PhloxPaymentTerms;
import com.helger.phlox.api.model.PhloxPostalAddress;
import com.helger.phlox.api.model.PhloxPrecedingInvoiceRef;
import com.helger.phlox.api.model.PhloxPriceDetails;
import com.helger.phlox.api.model.PhloxProcessControl;
import com.helger.phlox.api.model.PhloxSeller;
import com.helger.phlox.api.model.PhloxSellerTaxRepresentative;
import com.helger.phlox.api.model.PhloxSupportingDocument;
import com.helger.phlox.api.model.PhloxThirdPartyCharge;
import com.helger.phlox.api.model.PhloxVatBreakdown;

/**
 * Static (Class, BT/BG name) → getter registry. Drives the Mustache adapter's name resolution.
 * Every BT/BG referenced by {@code default.ptpl} (and by extension, every BT/BG documented in
 * {@code wiki/Domain-Model.md}) is registered here.
 * <p>
 * Multiple BG positions sharing a Java type — e.g. {@code PhloxPostalAddress} is reached via
 * BG-5 (BT-35..40), BG-8 (BT-50..55), BG-12 (BT-64..69) and BG-15 (BT-75..80) — register all
 * BT aliases against the single Java class so any usage site resolves correctly.
 *
 * @author Philip Helger
 */
public final class PhloxModelRegistry
{
  private static final Map <Class <?>, Map <String, Function <Object, Object>>> REG = new HashMap <> ();

  static
  {
    // ─── Top-level invoice header — BT-1..BT-20 ──────────────────────────
    _add (PhloxInvoice.class, "BT-1", PhloxInvoice::getNumber);
    _add (PhloxInvoice.class, "BT-2", PhloxInvoice::getIssueDate);
    _add (PhloxInvoice.class, "BT-3", PhloxInvoice::getTypeCode);
    _add (PhloxInvoice.class, "BT-5", PhloxInvoice::getCurrency);
    _add (PhloxInvoice.class, "BT-6", PhloxInvoice::getVatAccountingCurrency);
    _add (PhloxInvoice.class, "BT-7", PhloxInvoice::getVatPointDate);
    _add (PhloxInvoice.class, "BT-8", PhloxInvoice::getVatPointDateCode);
    _add (PhloxInvoice.class, "BT-9", PhloxInvoice::getPaymentDueDate);
    _add (PhloxInvoice.class, "BT-10", PhloxModelRegistry::_buyerReferences);
    _add (PhloxInvoice.class, "BT-11", PhloxInvoice::getProjectReference);
    _add (PhloxInvoice.class, "BT-12", PhloxInvoice::getContractReference);
    _add (PhloxInvoice.class, "BT-13", PhloxInvoice::getPurchaseOrderReference);
    _add (PhloxInvoice.class, "BT-14", PhloxInvoice::getSalesOrderReference);
    _add (PhloxInvoice.class, "BT-15", PhloxInvoice::getReceivingAdviceReference);
    _add (PhloxInvoice.class, "BT-16", PhloxInvoice::getDespatchAdviceReference);
    _add (PhloxInvoice.class, "BT-17", PhloxInvoice::getTenderOrLotReference);
    _add (PhloxInvoice.class, "BT-18", PhloxInvoice::getInvoicedObjectIdentifier);
    _add (PhloxInvoice.class, "BT-19", PhloxInvoice::getBuyerAccountingReference);
    _add (PhloxInvoice.class, "BT-166", PhloxInvoice::getIssueTime);
    _add (PhloxInvoice.class, "BT-167", PhloxInvoice::getVatExchangeRate);
    _add (PhloxInvoice.class, "BT-167-1", PhloxInvoice::getVatExchangeRateTargetCurrency);
    _add (PhloxInvoice.class, "BT-167-2", PhloxInvoice::getVatExchangeRateSourceCurrency);
    _add (PhloxInvoice.class, "BT-197", PhloxInvoice::getDeliveryNoteReference);

    // ─── Top-level BG containers and lists ───────────────────────────────
    _add (PhloxInvoice.class, "BG-1", PhloxInvoice::invoiceNotes);
    _add (PhloxInvoice.class, "BG-2", PhloxInvoice::getProcessControl);
    _add (PhloxInvoice.class, "BG-3", PhloxInvoice::precedingInvoiceRefs);
    _add (PhloxInvoice.class, "BG-33", PhloxInvoice::paymentTerms);
    _add (PhloxInvoice.class, "BG-4", PhloxInvoice::getSeller);
    _add (PhloxInvoice.class, "BG-7", PhloxInvoice::getBuyer);
    _add (PhloxInvoice.class, "BG-10", PhloxInvoice::getPayee);
    _add (PhloxInvoice.class, "BG-11", PhloxInvoice::getSellerTaxRepresentative);
    _add (PhloxInvoice.class, "BG-13", PhloxInvoice::getDeliveryInformation);
    _add (PhloxInvoice.class, "BG-14", PhloxInvoice::getInvoicingPeriod);
    _add (PhloxInvoice.class, "BG-16", PhloxInvoice::getPaymentInstructions);
    _add (PhloxInvoice.class, "BG-20", PhloxInvoice::documentAllowances);
    _add (PhloxInvoice.class, "BG-21", PhloxInvoice::documentCharges);
    _add (PhloxInvoice.class, "BG-22", PhloxInvoice::getDocumentTotals);
    _add (PhloxInvoice.class, "BG-23", PhloxInvoice::vatBreakdowns);
    _add (PhloxInvoice.class, "BG-24", PhloxInvoice::supportingDocuments);
    _add (PhloxInvoice.class, "BG-25", PhloxInvoice::lines);

    // ─── BG-2 — Process control ──────────────────────────────────────────
    _add (PhloxProcessControl.class, "BT-23", PhloxProcessControl::getBusinessProcessType);
    _add (PhloxProcessControl.class, "BT-24", PhloxProcessControl::getSpecificationIdentifier);

    // ─── BG-33 / BG-35 / BG-36 — Payment terms, early discount, late penalty ─
    _add (PhloxPaymentTerms.class, "BT-20", PhloxPaymentTerms::getText);
    _add (PhloxPaymentTerms.class, "BG-35", PhloxPaymentTerms::earlyDiscounts);
    _add (PhloxPaymentTerms.class, "BG-36", PhloxPaymentTerms::latePenalties);
    _add (PhloxEarlyPaymentDiscount.class, "BT-170", PhloxEarlyPaymentDiscount::getEndDate);
    _add (PhloxEarlyPaymentDiscount.class, "BT-171", PhloxEarlyPaymentDiscount::getPercentage);
    _add (PhloxEarlyPaymentDiscount.class, "BT-172", PhloxEarlyPaymentDiscount::getAmount);
    _add (PhloxLatePaymentPenalty.class, "BT-181", PhloxLatePaymentPenalty::getStartDate);
    _add (PhloxLatePaymentPenalty.class, "BT-182", PhloxLatePaymentPenalty::getYearlyInterestPercentage);
    _add (PhloxLatePaymentPenalty.class, "BT-183", PhloxLatePaymentPenalty::getAmount);

    // ─── BG-4 — Seller ───────────────────────────────────────────────────
    _add (PhloxSeller.class, "BT-27", PhloxSeller::getName);
    _add (PhloxSeller.class, "BT-28", PhloxSeller::getTradingName);
    _add (PhloxSeller.class, "BT-30", PhloxSeller::getLegalRegistration);
    _add (PhloxSeller.class, "BT-31", PhloxSeller::getVatIdentifier);
    _add (PhloxSeller.class, "BT-32", PhloxSeller::getTaxRegistration);
    _add (PhloxSeller.class, "BT-32-1", PhloxSeller::getTaxRegistrationSchemeID);
    _add (PhloxSeller.class, "BT-33", PhloxSeller::getAdditionalLegalInfo);
    _add (PhloxSeller.class, "BT-34", PhloxSeller::getElectronicAddress);
    _add (PhloxSeller.class, "BG-5", PhloxSeller::getPostalAddress);
    _add (PhloxSeller.class, "BG-6", PhloxSeller::getContact);

    // ─── BG-7 — Buyer ────────────────────────────────────────────────────
    _add (PhloxBuyer.class, "BT-44", PhloxBuyer::getName);
    _add (PhloxBuyer.class, "BT-45", PhloxBuyer::getTradingName);
    _add (PhloxBuyer.class, "BT-47", PhloxBuyer::getLegalRegistration);
    _add (PhloxBuyer.class, "BT-48", PhloxBuyer::getVatIdentifier);
    _add (PhloxBuyer.class, "BT-49", PhloxBuyer::getElectronicAddress);
    _add (PhloxBuyer.class, "BG-8", PhloxBuyer::getPostalAddress);
    _add (PhloxBuyer.class, "BG-9", PhloxBuyer::getContact);

    // ─── Postal address — shared by BG-5 / BG-8 / BG-12 / BG-15 ──────────
    // BG-5 (seller):       BT-35..40
    // BG-8 (buyer):        BT-50..55
    // BG-12 (tax rep):     BT-64, BT-66, BT-67, BT-69
    // BG-15 (deliver-to):  BT-75, BT-77, BT-78, BT-80
    _add (PhloxPostalAddress.class, "BT-35", PhloxPostalAddress::getLine1);
    _add (PhloxPostalAddress.class, "BT-36", PhloxPostalAddress::getLine2);
    _add (PhloxPostalAddress.class, "BT-37", PhloxPostalAddress::getCity);
    _add (PhloxPostalAddress.class, "BT-38", PhloxPostalAddress::getPostCode);
    _add (PhloxPostalAddress.class, "BT-39", PhloxPostalAddress::getCountrySubdivision);
    _add (PhloxPostalAddress.class, "BT-40", PhloxPostalAddress::getCountryCode);
    _add (PhloxPostalAddress.class, "BT-50", PhloxPostalAddress::getLine1);
    _add (PhloxPostalAddress.class, "BT-51", PhloxPostalAddress::getLine2);
    _add (PhloxPostalAddress.class, "BT-52", PhloxPostalAddress::getCity);
    _add (PhloxPostalAddress.class, "BT-53", PhloxPostalAddress::getPostCode);
    _add (PhloxPostalAddress.class, "BT-54", PhloxPostalAddress::getCountrySubdivision);
    _add (PhloxPostalAddress.class, "BT-55", PhloxPostalAddress::getCountryCode);
    _add (PhloxPostalAddress.class, "BT-64", PhloxPostalAddress::getLine1);
    _add (PhloxPostalAddress.class, "BT-66", PhloxPostalAddress::getCity);
    _add (PhloxPostalAddress.class, "BT-67", PhloxPostalAddress::getPostCode);
    _add (PhloxPostalAddress.class, "BT-69", PhloxPostalAddress::getCountryCode);
    _add (PhloxPostalAddress.class, "BT-75", PhloxPostalAddress::getLine1);
    _add (PhloxPostalAddress.class, "BT-77", PhloxPostalAddress::getCity);
    _add (PhloxPostalAddress.class, "BT-78", PhloxPostalAddress::getPostCode);
    _add (PhloxPostalAddress.class, "BT-80", PhloxPostalAddress::getCountryCode);
    // BG-38 (line deliver-to address): BT-203..209
    _add (PhloxPostalAddress.class, "BT-203", PhloxPostalAddress::getLine1);
    _add (PhloxPostalAddress.class, "BT-204", PhloxPostalAddress::getLine2);
    _add (PhloxPostalAddress.class, "BT-205", PhloxPostalAddress::getLine3);
    _add (PhloxPostalAddress.class, "BT-206", PhloxPostalAddress::getCity);
    _add (PhloxPostalAddress.class, "BT-207", PhloxPostalAddress::getPostCode);
    _add (PhloxPostalAddress.class, "BT-208", PhloxPostalAddress::getCountrySubdivision);
    _add (PhloxPostalAddress.class, "BT-209", PhloxPostalAddress::getCountryCode);

    // ─── Contact — BG-6 (seller) / BG-9 (buyer) ──────────────────────────
    _add (PhloxContact.class, "BT-41", PhloxContact::getName);
    _add (PhloxContact.class, "BT-42", PhloxContact::getTelephone);
    _add (PhloxContact.class, "BT-43", PhloxContact::getEmail);
    _add (PhloxContact.class, "BT-56", PhloxContact::getName);
    _add (PhloxContact.class, "BT-57", PhloxContact::getTelephone);
    _add (PhloxContact.class, "BT-58", PhloxContact::getEmail);

    // ─── BG-10 — Payee ───────────────────────────────────────────────────
    _add (PhloxPayee.class, "BT-59", PhloxPayee::getName);
    _add (PhloxPayee.class, "BT-60", PhloxPayee::getIdentifier);
    _add (PhloxPayee.class, "BT-61", PhloxPayee::getLegalRegistration);

    // ─── BG-11 — Seller tax representative ───────────────────────────────
    _add (PhloxSellerTaxRepresentative.class, "BT-62", PhloxSellerTaxRepresentative::getName);
    _add (PhloxSellerTaxRepresentative.class, "BT-63", PhloxSellerTaxRepresentative::getVatIdentifier);
    _add (PhloxSellerTaxRepresentative.class, "BG-12", PhloxSellerTaxRepresentative::getPostalAddress);

    // ─── BG-13 — Delivery information ────────────────────────────────────
    _add (PhloxDeliveryInformation.class, "BT-70", PhloxDeliveryInformation::getPartyName);
    _add (PhloxDeliveryInformation.class, "BT-71", PhloxDeliveryInformation::getLocationIdentifier);
    _add (PhloxDeliveryInformation.class, "BT-72", PhloxDeliveryInformation::getActualDeliveryDate);
    _add (PhloxDeliveryInformation.class, "BG-15", PhloxDeliveryInformation::getPostalAddress);

    // ─── BG-14 / BG-26 — Invoicing period (shared class) ─────────────────
    _add (PhloxInvoicingPeriod.class, "BT-73", PhloxInvoicingPeriod::getStartDate);
    _add (PhloxInvoicingPeriod.class, "BT-74", PhloxInvoicingPeriod::getEndDate);
    _add (PhloxInvoicingPeriod.class, "BT-134", PhloxInvoicingPeriod::getStartDate);
    _add (PhloxInvoicingPeriod.class, "BT-135", PhloxInvoicingPeriod::getEndDate);

    // ─── BG-16 — Payment instructions ────────────────────────────────────
    _add (PhloxPaymentInstructions.class, "BT-81", PhloxPaymentInstructions::getPaymentMeansCode);
    _add (PhloxPaymentInstructions.class, "BT-82", PhloxPaymentInstructions::getPaymentMeansText);
    _add (PhloxPaymentInstructions.class, "BT-83", PhloxPaymentInstructions::getRemittanceInformation);
    _add (PhloxPaymentInstructions.class, "BG-17", PhloxPaymentInstructions::creditTransfers);
    _add (PhloxPaymentInstructions.class, "BG-18", PhloxPaymentInstructions::getPaymentCard);
    _add (PhloxPaymentInstructions.class, "BG-19", PhloxPaymentInstructions::getDirectDebit);

    // ─── BG-17 — Credit transfer ─────────────────────────────────────────
    _add (PhloxCreditTransfer.class, "BT-84", PhloxCreditTransfer::getAccountIdentifier);
    _add (PhloxCreditTransfer.class, "BT-85", PhloxCreditTransfer::getAccountName);
    _add (PhloxCreditTransfer.class, "BT-86", PhloxCreditTransfer::getServiceProviderID);

    // ─── BG-18 — Payment card ────────────────────────────────────────────
    _add (PhloxPaymentCard.class, "BT-87", PhloxPaymentCard::getPan);
    _add (PhloxPaymentCard.class, "BT-88", PhloxPaymentCard::getHolderName);

    // ─── BG-19 — Direct debit ────────────────────────────────────────────
    _add (PhloxDirectDebit.class, "BT-89", PhloxDirectDebit::getMandateReferenceID);
    _add (PhloxDirectDebit.class, "BT-90", PhloxDirectDebit::getBankAssignedCreditorID);
    _add (PhloxDirectDebit.class, "BT-91", PhloxDirectDebit::getDebitedAccountID);
    _add (PhloxDirectDebit.class, "BT-215", PhloxDirectDebit::getDebitedAccountProviderID);
    _add (PhloxDirectDebit.class, "BT-216", PhloxDirectDebit::getDebitedAccountName);

    // ─── BG-20 — Document-level allowance ────────────────────────────────
    _add (PhloxDocumentAllowance.class, "BT-92", PhloxDocumentAllowance::getAmount);
    _add (PhloxDocumentAllowance.class, "BT-95", PhloxDocumentAllowance::getVatCategoryCode);
    _add (PhloxDocumentAllowance.class, "BT-96", PhloxDocumentAllowance::getVatRate);
    _add (PhloxDocumentAllowance.class, "BT-97", PhloxDocumentAllowance::getReason);
    _add (PhloxDocumentAllowance.class, "BT-98", PhloxDocumentAllowance::getReasonCode);
    _add (PhloxDocumentAllowance.class, "BT-173", PhloxDocumentAllowance::getExemptionReasonText);
    _add (PhloxDocumentAllowance.class, "BT-174", PhloxDocumentAllowance::getExemptionReasonCode);
    _add (PhloxDocumentAllowance.class, "BT-213", PhloxDocumentAllowance::getGoodsServicesCode);

    // ─── BG-21 — Document-level charge ───────────────────────────────────
    _add (PhloxDocumentCharge.class, "BT-99", PhloxDocumentCharge::getAmount);
    _add (PhloxDocumentCharge.class, "BT-102", PhloxDocumentCharge::getVatCategoryCode);
    _add (PhloxDocumentCharge.class, "BT-103", PhloxDocumentCharge::getVatRate);
    _add (PhloxDocumentCharge.class, "BT-104", PhloxDocumentCharge::getReason);
    _add (PhloxDocumentCharge.class, "BT-105", PhloxDocumentCharge::getReasonCode);
    _add (PhloxDocumentCharge.class, "BT-175", PhloxDocumentCharge::getExemptionReasonText);
    _add (PhloxDocumentCharge.class, "BT-176", PhloxDocumentCharge::getExemptionReasonCode);
    _add (PhloxDocumentCharge.class, "BT-177", PhloxDocumentCharge::getNonVatTaxCode);
    _add (PhloxDocumentCharge.class, "BT-177-1", PhloxDocumentCharge::getNonVatTaxCodeListID);
    _add (PhloxDocumentCharge.class, "BT-214", PhloxDocumentCharge::getGoodsServicesCode);

    // ─── BG-22 — Document totals ─────────────────────────────────────────
    _add (PhloxDocumentTotals.class, "BT-106", PhloxDocumentTotals::getLineNetTotal);
    _add (PhloxDocumentTotals.class, "BT-107", PhloxDocumentTotals::getAllowanceTotal);
    _add (PhloxDocumentTotals.class, "BT-108", PhloxDocumentTotals::getChargeTotal);
    _add (PhloxDocumentTotals.class, "BT-109", PhloxDocumentTotals::getTaxExclusiveTotal);
    _add (PhloxDocumentTotals.class, "BT-110", PhloxDocumentTotals::getTaxTotal);
    _add (PhloxDocumentTotals.class, "BT-111", PhloxDocumentTotals::getTaxTotalAccountingCurrency);
    _add (PhloxDocumentTotals.class, "BT-112", PhloxDocumentTotals::getTaxInclusiveTotal);
    _add (PhloxDocumentTotals.class, "BT-113", PhloxDocumentTotals::getPrepaidAmount);
    _add (PhloxDocumentTotals.class, "BT-114", PhloxDocumentTotals::getRoundingAmount);
    _add (PhloxDocumentTotals.class, "BT-115", PhloxDocumentTotals::getPayableAmount);
    _add (PhloxDocumentTotals.class, "BG-34", PhloxDocumentTotals::thirdPartyCharges);

    // ─── BG-34 — Charges on behalf of a third party ──────────────────────
    _add (PhloxThirdPartyCharge.class, "BT-179", PhloxThirdPartyCharge::getAmount);
    _add (PhloxThirdPartyCharge.class, "BT-180", PhloxThirdPartyCharge::getSpecification);

    // ─── BG-23 — VAT breakdown ───────────────────────────────────────────
    _add (PhloxVatBreakdown.class, "BT-116", PhloxVatBreakdown::getTaxableAmount);
    _add (PhloxVatBreakdown.class, "BT-117", PhloxVatBreakdown::getTaxAmount);
    _add (PhloxVatBreakdown.class, "BT-118", PhloxVatBreakdown::getCategoryCode);
    _add (PhloxVatBreakdown.class, "BT-119", PhloxVatBreakdown::getRate);
    _add (PhloxVatBreakdown.class, "BT-120", PhloxVatBreakdown::getExemptionReason);
    _add (PhloxVatBreakdown.class, "BT-121", PhloxVatBreakdown::getExemptionReasonCode);
    _add (PhloxVatBreakdown.class, "BT-184", PhloxVatBreakdown::getCurrency);
    _add (PhloxVatBreakdown.class, "BT-210", PhloxVatBreakdown::getGoodsServicesCode);

    // ─── BG-24 — Supporting document ─────────────────────────────────────
    _add (PhloxSupportingDocument.class, "BT-122", PhloxSupportingDocument::getReference);
    _add (PhloxSupportingDocument.class, "BT-123", PhloxSupportingDocument::getDescription);
    _add (PhloxSupportingDocument.class, "BT-124", PhloxSupportingDocument::getExternalLocation);
    _add (PhloxSupportingDocument.class, "BT-125", PhloxSupportingDocument::getAttachment);

    // ─── BG-25 — Invoice line ────────────────────────────────────────────
    _add (PhloxInvoiceLine.class, "BT-126", PhloxInvoiceLine::getID);
    _add (PhloxInvoiceLine.class, "BT-127", PhloxInvoiceLine::getNote);
    _add (PhloxInvoiceLine.class, "BT-128", PhloxInvoiceLine::getObjectIdentifier);
    _add (PhloxInvoiceLine.class, "BT-129", PhloxInvoiceLine::getQuantity);
    _add (PhloxInvoiceLine.class, "BT-130", PhloxInvoiceLine::getQuantityUnit);
    _add (PhloxInvoiceLine.class, "BT-131", PhloxInvoiceLine::getNetAmount);
    _add (PhloxInvoiceLine.class, "BT-132", PhloxInvoiceLine::getPurchaseOrderLineRef);
    _add (PhloxInvoiceLine.class, "BT-133", PhloxInvoiceLine::getAccountingCost);
    _add (PhloxInvoiceLine.class, "BT-188", PhloxInvoiceLine::getPurchaseOrderReference);
    _add (PhloxInvoiceLine.class, "BT-189", PhloxInvoiceLine::getDespatchAdviceReference);
    _add (PhloxInvoiceLine.class, "BT-190", PhloxInvoiceLine::getDespatchAdviceLineReference);
    _add (PhloxInvoiceLine.class, "BT-191", PhloxInvoiceLine::getReceivingAdviceReference);
    _add (PhloxInvoiceLine.class, "BT-192", PhloxInvoiceLine::getReceivingAdviceLineReference);
    _add (PhloxInvoiceLine.class, "BT-198", PhloxInvoiceLine::getDeliveryNoteReference);
    _add (PhloxInvoiceLine.class, "BT-199", PhloxInvoiceLine::getDeliveryNoteLineReference);
    _add (PhloxInvoiceLine.class, "BT-200", PhloxInvoiceLine::getSalesOrderReference);
    _add (PhloxInvoiceLine.class, "BT-201", PhloxInvoiceLine::getSalesOrderLineReference);
    _add (PhloxInvoiceLine.class, "BG-26", PhloxInvoiceLine::getPeriod);
    _add (PhloxInvoiceLine.class, "BG-37", PhloxInvoiceLine::getDelivery);
    _add (PhloxInvoiceLine.class, "BG-39", PhloxInvoiceLine::precedingInvoiceRefs);
    _add (PhloxInvoiceLine.class, "BG-27", PhloxInvoiceLine::allowances);
    _add (PhloxInvoiceLine.class, "BG-28", PhloxInvoiceLine::charges);
    _add (PhloxInvoiceLine.class, "BG-29", PhloxInvoiceLine::getPrice);
    _add (PhloxInvoiceLine.class, "BG-30", PhloxInvoiceLine::getLineVat);
    _add (PhloxInvoiceLine.class, "BG-31", PhloxInvoiceLine::getItem);

    // ─── BG-1 — Invoice note ─────────────────────────────────────────────
    _add (PhloxInvoiceNote.class, "BT-21", PhloxInvoiceNote::getSubjectCode);
    _add (PhloxInvoiceNote.class, "BT-22", PhloxInvoiceNote::getContent);

    // ─── BG-3 — Preceding invoice ref ────────────────────────────────────
    _add (PhloxPrecedingInvoiceRef.class, "BT-25", PhloxPrecedingInvoiceRef::getNumber);
    _add (PhloxPrecedingInvoiceRef.class, "BT-26", PhloxPrecedingInvoiceRef::getIssueDate);
    _add (PhloxPrecedingInvoiceRef.class, "BT-202", PhloxPrecedingInvoiceRef::getTypeCode);

    // ─── BG-39 — Line-level preceding invoice ref ────────────────────────
    _add (PhloxLinePrecedingInvoiceRef.class, "BT-217", PhloxLinePrecedingInvoiceRef::getNumber);
    _add (PhloxLinePrecedingInvoiceRef.class, "BT-218", PhloxLinePrecedingInvoiceRef::getIssueDate);
    _add (PhloxLinePrecedingInvoiceRef.class, "BT-219", PhloxLinePrecedingInvoiceRef::getTypeCode);
    _add (PhloxLinePrecedingInvoiceRef.class, "BT-220", PhloxLinePrecedingInvoiceRef::getLineReference);

    // ─── BG-37 — Invoice line delivery information (+ BG-38 address) ─────
    _add (PhloxLineDelivery.class, "BT-185", PhloxLineDelivery::getPartyName);
    _add (PhloxLineDelivery.class, "BT-186", PhloxLineDelivery::getLocationIdentifier);
    _add (PhloxLineDelivery.class, "BT-187", PhloxLineDelivery::getActualDeliveryDate);
    _add (PhloxLineDelivery.class, "BG-38", PhloxLineDelivery::getPostalAddress);

    // ─── BG-27 — Line allowance ──────────────────────────────────────────
    _add (PhloxLineAllowance.class, "BT-136", PhloxLineAllowance::getAmount);
    _add (PhloxLineAllowance.class, "BT-139", PhloxLineAllowance::getReason);

    // ─── BG-28 — Line charge ─────────────────────────────────────────────
    _add (PhloxLineCharge.class, "BT-141", PhloxLineCharge::getAmount);
    _add (PhloxLineCharge.class, "BT-144", PhloxLineCharge::getReason);
    _add (PhloxLineCharge.class, "BT-193", PhloxLineCharge::getNonVatTaxTypeCode);
    _add (PhloxLineCharge.class, "BT-193-1", PhloxLineCharge::getNonVatTaxTypeCodeListID);

    // ─── BG-29 — Price details ───────────────────────────────────────────
    _add (PhloxPriceDetails.class, "BT-146", PhloxPriceDetails::getNetPrice);

    // ─── BG-30 — Line VAT ────────────────────────────────────────────────
    _add (PhloxLineVat.class, "BT-151", PhloxLineVat::getCategoryCode);
    _add (PhloxLineVat.class, "BT-152", PhloxLineVat::getRate);
    _add (PhloxLineVat.class, "BT-194", PhloxLineVat::getExemptionReasonText);
    _add (PhloxLineVat.class, "BT-195", PhloxLineVat::getExemptionReasonCode);

    // ─── BG-31 — Item ────────────────────────────────────────────────────
    _add (PhloxItem.class, "BT-153", PhloxItem::getName);
    _add (PhloxItem.class, "BT-154", PhloxItem::getDescription);
    _add (PhloxItem.class, "BT-155", PhloxItem::getSellerItemID);
    _add (PhloxItem.class, "BT-196", PhloxItem::getGoodsServicesCode);
    _add (PhloxItem.class, "BG-32", PhloxItem::attributes);

    // ─── BG-32 — Item attribute ──────────────────────────────────────────
    _add (PhloxItemAttribute.class, "BT-160", PhloxItemAttribute::getName);
    _add (PhloxItemAttribute.class, "BT-161", PhloxItemAttribute::getValue);
    _add (PhloxItemAttribute.class, "BT-211", PhloxItemAttribute::getCode);
    _add (PhloxItemAttribute.class, "BT-212", PhloxItemAttribute::getValueUnit);

    // ─── PhloxIdentifier — flatten to scheme-prefixed value when present ─
    _add (PhloxIdentifier.class, "value", PhloxIdentifier::getValue);
    _add (PhloxIdentifier.class, "schemeID", PhloxIdentifier::getSchemeID);

    // ─── PhloxAttachment — used by attachment:logo / attachment:BG-24[i] ─
    _add (PhloxAttachment.class, "filename", PhloxAttachment::getFilename);
    _add (PhloxAttachment.class, "mimeCode", PhloxAttachment::getMimeCode);
  }

  private PhloxModelRegistry ()
  {}

  /**
   * Get the comma separated buyer references (BT-10), each followed by its scheme identifier.
   *
   * @return The comma separated buyer references (BT-10), each followed by its scheme identifier
   *         (BT-10-1) in parentheses if present, or {@code null} if there is none.
   */
  @Nullable
  private static String _buyerReferences (@NonNull final PhloxInvoice aInvoice)
  {
    final StringBuilder aSB = new StringBuilder ();
    for (final PhloxIdentifier aID : aInvoice.buyerReferences ())
      if (StringHelper.isNotEmpty (aID.getValue ()))
      {
        if (aSB.length () > 0)
          aSB.append (", ");
        aSB.append (aID.getValue ());
        if (StringHelper.isNotEmpty (aID.getSchemeID ()))
          aSB.append (" (").append (aID.getSchemeID ()).append (')');
      }
    return aSB.length () == 0 ? null : aSB.toString ();
  }

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
   * Resolve a registered BT/BG name against the given model object.
   *
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

  /**
   * Check if any BT/BG name is registered for a model class.
   *
   * @param aClass
   *        The model class to check. Never {@code null}.
   * @return {@code true} when {@code aClass} has any registered name.
   */
  public static boolean knows (@NonNull final Class <?> aClass)
  {
    return REG.containsKey (aClass);
  }
}
