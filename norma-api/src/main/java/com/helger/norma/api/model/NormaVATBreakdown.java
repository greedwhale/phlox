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
 * BG-23 VAT BREAKDOWN. BT-116..BT-121.
 *
 * @author Philip Helger
 */
public class NormaVATBreakdown
{
  private BigDecimal m_aTaxableAmount;
  private BigDecimal m_aTaxAmount;
  private String m_sCategoryCode;
  private BigDecimal m_aRate;
  private String m_sExemptionReason;
  private String m_sExemptionReasonCode;

  public NormaVATBreakdown ()
  {}

  @Nullable
  public BigDecimal getTaxableAmount ()
  {
    return m_aTaxableAmount;
  }

  @NonNull
  public NormaVATBreakdown setTaxableAmount (@Nullable final BigDecimal aTaxableAmount)
  {
    m_aTaxableAmount = aTaxableAmount;
    return this;
  }

  @Nullable
  public BigDecimal getTaxAmount ()
  {
    return m_aTaxAmount;
  }

  @NonNull
  public NormaVATBreakdown setTaxAmount (@Nullable final BigDecimal aTaxAmount)
  {
    m_aTaxAmount = aTaxAmount;
    return this;
  }

  @Nullable
  public String getCategoryCode ()
  {
    return m_sCategoryCode;
  }

  @NonNull
  public NormaVATBreakdown setCategoryCode (@Nullable final String sCategoryCode)
  {
    m_sCategoryCode = sCategoryCode;
    return this;
  }

  @Nullable
  public BigDecimal getRate ()
  {
    return m_aRate;
  }

  @NonNull
  public NormaVATBreakdown setRate (@Nullable final BigDecimal aRate)
  {
    m_aRate = aRate;
    return this;
  }

  @Nullable
  public String getExemptionReason ()
  {
    return m_sExemptionReason;
  }

  @NonNull
  public NormaVATBreakdown setExemptionReason (@Nullable final String sExemptionReason)
  {
    m_sExemptionReason = sExemptionReason;
    return this;
  }

  @Nullable
  public String getExemptionReasonCode ()
  {
    return m_sExemptionReasonCode;
  }

  @NonNull
  public NormaVATBreakdown setExemptionReasonCode (@Nullable final String sExemptionReasonCode)
  {
    m_sExemptionReasonCode = sExemptionReasonCode;
    return this;
  }
}
