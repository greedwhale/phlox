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

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * BG-25 INVOICE LINE (1..n) plus all the nested line-level groups (BG-26 invoice line period,
 * BG-27 line allowances, BG-28 line charges, BG-29 price details, BG-30 line VAT, BG-31 item
 * information with BG-32 item attributes). Carries BT-126 (line identifier), BT-127 (line note),
 * BT-128/BT-128-1 (line object identifier), BT-129 (quantity), BT-130 (quantity unit),
 * BT-131 (line net amount), BT-132 (referenced purchase order line) and BT-133 (line buyer
 * accounting reference).
 *
 * @author Philip Helger
 */
public class NormaInvoiceLine
{
  private String m_sID;
  private String m_sNote;
  private NormaIdentifier m_aObjectIdentifier;
  private BigDecimal m_aQuantity;
  private String m_sQuantityUnit;
  private BigDecimal m_aNetAmount;
  private String m_sPurchaseOrderLineRef;
  private String m_sAccountingCost;
  private NormaInvoicingPeriod m_aPeriod;
  private final ICommonsList <NormaLineAllowance> m_aAllowances = new CommonsArrayList <> ();
  private final ICommonsList <NormaLineCharge> m_aCharges = new CommonsArrayList <> ();
  private NormaPriceDetails m_aPrice;
  private NormaLineVAT m_aLineVAT;
  private NormaItem m_aItem;

  /** Create an empty invoice line. */
  public NormaInvoiceLine ()
  {}

  /** @return Invoice line identifier (BT-126). May be {@code null}. */
  @Nullable
  public String getID ()
  {
    return m_sID;
  }

  /**
   * Set the invoice line identifier (BT-126).
   *
   * @param sID
   *        Line ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setID (@Nullable final String sID)
  {
    m_sID = sID;
    return this;
  }

  /** @return Invoice line note (BT-127). May be {@code null}. */
  @Nullable
  public String getNote ()
  {
    return m_sNote;
  }

  /**
   * Set the invoice line note (BT-127).
   *
   * @param sNote
   *        Note text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setNote (@Nullable final String sNote)
  {
    m_sNote = sNote;
    return this;
  }

  /** @return Invoice line object identifier (BT-128 with BT-128-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getObjectIdentifier ()
  {
    return m_aObjectIdentifier;
  }

  /**
   * Set the invoice line object identifier (BT-128 / BT-128-1).
   *
   * @param aObjectIdentifier
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setObjectIdentifier (@Nullable final NormaIdentifier aObjectIdentifier)
  {
    m_aObjectIdentifier = aObjectIdentifier;
    return this;
  }

  /** @return Invoiced quantity (BT-129) — credit-note credited quantity also stored here. May be {@code null}. */
  @Nullable
  public BigDecimal getQuantity ()
  {
    return m_aQuantity;
  }

  /**
   * Set the invoiced quantity (BT-129).
   *
   * @param aQuantity
   *        Quantity; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setQuantity (@Nullable final BigDecimal aQuantity)
  {
    m_aQuantity = aQuantity;
    return this;
  }

  /** @return Invoiced quantity unit of measure (BT-130) — UN/ECE Rec 20/21. May be {@code null}. */
  @Nullable
  public String getQuantityUnit ()
  {
    return m_sQuantityUnit;
  }

  /**
   * Set the invoiced quantity unit of measure (BT-130).
   *
   * @param sQuantityUnit
   *        Unit code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setQuantityUnit (@Nullable final String sQuantityUnit)
  {
    m_sQuantityUnit = sQuantityUnit;
    return this;
  }

  /** @return Invoice line net amount (BT-131). May be {@code null}. */
  @Nullable
  public BigDecimal getNetAmount ()
  {
    return m_aNetAmount;
  }

  /**
   * Set the invoice line net amount (BT-131).
   *
   * @param aNetAmount
   *        Net amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setNetAmount (@Nullable final BigDecimal aNetAmount)
  {
    m_aNetAmount = aNetAmount;
    return this;
  }

  /** @return Referenced purchase order line reference (BT-132). May be {@code null}. */
  @Nullable
  public String getPurchaseOrderLineRef ()
  {
    return m_sPurchaseOrderLineRef;
  }

  /**
   * Set the referenced purchase order line reference (BT-132).
   *
   * @param sPurchaseOrderLineRef
   *        PO line reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setPurchaseOrderLineRef (@Nullable final String sPurchaseOrderLineRef)
  {
    m_sPurchaseOrderLineRef = sPurchaseOrderLineRef;
    return this;
  }

  /** @return Invoice line buyer accounting reference (BT-133). May be {@code null}. */
  @Nullable
  public String getAccountingCost ()
  {
    return m_sAccountingCost;
  }

  /**
   * Set the invoice line buyer accounting reference (BT-133).
   *
   * @param sAccountingCost
   *        Accounting cost reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setAccountingCost (@Nullable final String sAccountingCost)
  {
    m_sAccountingCost = sAccountingCost;
    return this;
  }

  /** @return Invoice line period (BG-26 — BT-134 / BT-135). May be {@code null}. */
  @Nullable
  public NormaInvoicingPeriod getPeriod ()
  {
    return m_aPeriod;
  }

  /**
   * Set the invoice line period (BG-26).
   *
   * @param aPeriod
   *        Period; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setPeriod (@Nullable final NormaInvoicingPeriod aPeriod)
  {
    m_aPeriod = aPeriod;
    return this;
  }

  /**
   * @return Mutable list of line allowances (BG-27). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaLineAllowance> allowances ()
  {
    return m_aAllowances;
  }

  /**
   * @return Mutable list of line charges (BG-28). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaLineCharge> charges ()
  {
    return m_aCharges;
  }

  /** @return Price details (BG-29). May be {@code null}. */
  @Nullable
  public NormaPriceDetails getPrice ()
  {
    return m_aPrice;
  }

  /**
   * Set the price details (BG-29).
   *
   * @param aPrice
   *        Price details; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setPrice (@Nullable final NormaPriceDetails aPrice)
  {
    m_aPrice = aPrice;
    return this;
  }

  /** @return Line VAT information (BG-30). May be {@code null}. */
  @Nullable
  public NormaLineVAT getLineVAT ()
  {
    return m_aLineVAT;
  }

  /**
   * Set the line VAT information (BG-30).
   *
   * @param aLineVAT
   *        Line VAT; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setLineVAT (@Nullable final NormaLineVAT aLineVAT)
  {
    m_aLineVAT = aLineVAT;
    return this;
  }

  /** @return Item information (BG-31). May be {@code null}. */
  @Nullable
  public NormaItem getItem ()
  {
    return m_aItem;
  }

  /**
   * Set the item information (BG-31).
   *
   * @param aItem
   *        Item; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaInvoiceLine setItem (@Nullable final NormaItem aItem)
  {
    m_aItem = aItem;
    return this;
  }
}
