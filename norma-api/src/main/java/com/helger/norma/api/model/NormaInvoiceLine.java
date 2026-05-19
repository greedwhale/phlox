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
 * BG-25 INVOICE LINE plus nested BG-26..BG-32. BT-126..BT-133 plus all the line-level groups.
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

  public NormaInvoiceLine ()
  {}

  @Nullable
  public String getID ()
  {
    return m_sID;
  }

  @NonNull
  public NormaInvoiceLine setID (@Nullable final String sID)
  {
    m_sID = sID;
    return this;
  }

  @Nullable
  public String getNote ()
  {
    return m_sNote;
  }

  @NonNull
  public NormaInvoiceLine setNote (@Nullable final String sNote)
  {
    m_sNote = sNote;
    return this;
  }

  @Nullable
  public NormaIdentifier getObjectIdentifier ()
  {
    return m_aObjectIdentifier;
  }

  @NonNull
  public NormaInvoiceLine setObjectIdentifier (@Nullable final NormaIdentifier aObjectIdentifier)
  {
    m_aObjectIdentifier = aObjectIdentifier;
    return this;
  }

  @Nullable
  public BigDecimal getQuantity ()
  {
    return m_aQuantity;
  }

  @NonNull
  public NormaInvoiceLine setQuantity (@Nullable final BigDecimal aQuantity)
  {
    m_aQuantity = aQuantity;
    return this;
  }

  @Nullable
  public String getQuantityUnit ()
  {
    return m_sQuantityUnit;
  }

  @NonNull
  public NormaInvoiceLine setQuantityUnit (@Nullable final String sQuantityUnit)
  {
    m_sQuantityUnit = sQuantityUnit;
    return this;
  }

  @Nullable
  public BigDecimal getNetAmount ()
  {
    return m_aNetAmount;
  }

  @NonNull
  public NormaInvoiceLine setNetAmount (@Nullable final BigDecimal aNetAmount)
  {
    m_aNetAmount = aNetAmount;
    return this;
  }

  @Nullable
  public String getPurchaseOrderLineRef ()
  {
    return m_sPurchaseOrderLineRef;
  }

  @NonNull
  public NormaInvoiceLine setPurchaseOrderLineRef (@Nullable final String sPurchaseOrderLineRef)
  {
    m_sPurchaseOrderLineRef = sPurchaseOrderLineRef;
    return this;
  }

  @Nullable
  public String getAccountingCost ()
  {
    return m_sAccountingCost;
  }

  @NonNull
  public NormaInvoiceLine setAccountingCost (@Nullable final String sAccountingCost)
  {
    m_sAccountingCost = sAccountingCost;
    return this;
  }

  @Nullable
  public NormaInvoicingPeriod getPeriod ()
  {
    return m_aPeriod;
  }

  @NonNull
  public NormaInvoiceLine setPeriod (@Nullable final NormaInvoicingPeriod aPeriod)
  {
    m_aPeriod = aPeriod;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaLineAllowance> allowances ()
  {
    return m_aAllowances;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaLineCharge> charges ()
  {
    return m_aCharges;
  }

  @Nullable
  public NormaPriceDetails getPrice ()
  {
    return m_aPrice;
  }

  @NonNull
  public NormaInvoiceLine setPrice (@Nullable final NormaPriceDetails aPrice)
  {
    m_aPrice = aPrice;
    return this;
  }

  @Nullable
  public NormaLineVAT getLineVAT ()
  {
    return m_aLineVAT;
  }

  @NonNull
  public NormaInvoiceLine setLineVAT (@Nullable final NormaLineVAT aLineVAT)
  {
    m_aLineVAT = aLineVAT;
    return this;
  }

  @Nullable
  public NormaItem getItem ()
  {
    return m_aItem;
  }

  @NonNull
  public NormaInvoiceLine setItem (@Nullable final NormaItem aItem)
  {
    m_aItem = aItem;
    return this;
  }
}
