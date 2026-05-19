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

/**
 * BG-22 DOCUMENT TOTALS. BT-106..BT-115.
 *
 * @author Philip Helger
 */
public class NormaDocumentTotals
{
  private BigDecimal m_aLineNetTotal;
  private BigDecimal m_aAllowanceTotal;
  private BigDecimal m_aChargeTotal;
  private BigDecimal m_aTaxExclusiveTotal;
  private BigDecimal m_aTaxTotal;
  private BigDecimal m_aTaxTotalAccountingCurrency;
  private BigDecimal m_aTaxInclusiveTotal;
  private BigDecimal m_aPrepaidAmount;
  private BigDecimal m_aRoundingAmount;
  private BigDecimal m_aPayableAmount;

  public NormaDocumentTotals ()
  {}

  @Nullable
  public BigDecimal getLineNetTotal ()
  {
    return m_aLineNetTotal;
  }

  @NonNull
  public NormaDocumentTotals setLineNetTotal (@Nullable final BigDecimal aLineNetTotal)
  {
    m_aLineNetTotal = aLineNetTotal;
    return this;
  }

  @Nullable
  public BigDecimal getAllowanceTotal ()
  {
    return m_aAllowanceTotal;
  }

  @NonNull
  public NormaDocumentTotals setAllowanceTotal (@Nullable final BigDecimal aAllowanceTotal)
  {
    m_aAllowanceTotal = aAllowanceTotal;
    return this;
  }

  @Nullable
  public BigDecimal getChargeTotal ()
  {
    return m_aChargeTotal;
  }

  @NonNull
  public NormaDocumentTotals setChargeTotal (@Nullable final BigDecimal aChargeTotal)
  {
    m_aChargeTotal = aChargeTotal;
    return this;
  }

  @Nullable
  public BigDecimal getTaxExclusiveTotal ()
  {
    return m_aTaxExclusiveTotal;
  }

  @NonNull
  public NormaDocumentTotals setTaxExclusiveTotal (@Nullable final BigDecimal aTaxExclusiveTotal)
  {
    m_aTaxExclusiveTotal = aTaxExclusiveTotal;
    return this;
  }

  @Nullable
  public BigDecimal getTaxTotal ()
  {
    return m_aTaxTotal;
  }

  @NonNull
  public NormaDocumentTotals setTaxTotal (@Nullable final BigDecimal aTaxTotal)
  {
    m_aTaxTotal = aTaxTotal;
    return this;
  }

  @Nullable
  public BigDecimal getTaxTotalAccountingCurrency ()
  {
    return m_aTaxTotalAccountingCurrency;
  }

  @NonNull
  public NormaDocumentTotals setTaxTotalAccountingCurrency (@Nullable final BigDecimal aTaxTotalAccountingCurrency)
  {
    m_aTaxTotalAccountingCurrency = aTaxTotalAccountingCurrency;
    return this;
  }

  @Nullable
  public BigDecimal getTaxInclusiveTotal ()
  {
    return m_aTaxInclusiveTotal;
  }

  @NonNull
  public NormaDocumentTotals setTaxInclusiveTotal (@Nullable final BigDecimal aTaxInclusiveTotal)
  {
    m_aTaxInclusiveTotal = aTaxInclusiveTotal;
    return this;
  }

  @Nullable
  public BigDecimal getPrepaidAmount ()
  {
    return m_aPrepaidAmount;
  }

  @NonNull
  public NormaDocumentTotals setPrepaidAmount (@Nullable final BigDecimal aPrepaidAmount)
  {
    m_aPrepaidAmount = aPrepaidAmount;
    return this;
  }

  @Nullable
  public BigDecimal getRoundingAmount ()
  {
    return m_aRoundingAmount;
  }

  @NonNull
  public NormaDocumentTotals setRoundingAmount (@Nullable final BigDecimal aRoundingAmount)
  {
    m_aRoundingAmount = aRoundingAmount;
    return this;
  }

  @Nullable
  public BigDecimal getPayableAmount ()
  {
    return m_aPayableAmount;
  }

  @NonNull
  public NormaDocumentTotals setPayableAmount (@Nullable final BigDecimal aPayableAmount)
  {
    m_aPayableAmount = aPayableAmount;
    return this;
  }
}
