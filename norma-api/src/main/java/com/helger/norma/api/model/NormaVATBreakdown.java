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
 * BG-23 VAT BREAKDOWN (1..n). Carries BT-116 (taxable amount), BT-117 (tax amount), BT-118
 * (VAT category code), BT-119 (VAT rate), BT-120 (exemption reason text) and BT-121
 * (exemption reason code).
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

  /** Create an empty VAT breakdown. */
  public NormaVATBreakdown ()
  {}

  /** @return VAT category taxable amount (BT-116). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxableAmount ()
  {
    return m_aTaxableAmount;
  }

  /**
   * Set the VAT category taxable amount (BT-116).
   *
   * @param aTaxableAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setTaxableAmount (@Nullable final BigDecimal aTaxableAmount)
  {
    m_aTaxableAmount = aTaxableAmount;
    return this;
  }

  /** @return VAT category tax amount (BT-117). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxAmount ()
  {
    return m_aTaxAmount;
  }

  /**
   * Set the VAT category tax amount (BT-117).
   *
   * @param aTaxAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setTaxAmount (@Nullable final BigDecimal aTaxAmount)
  {
    m_aTaxAmount = aTaxAmount;
    return this;
  }

  /** @return VAT category code (BT-118) — UNTDID 5305. May be {@code null}. */
  @Nullable
  public String getCategoryCode ()
  {
    return m_sCategoryCode;
  }

  /**
   * Set the VAT category code (BT-118).
   *
   * @param sCategoryCode
   *        UNTDID 5305 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setCategoryCode (@Nullable final String sCategoryCode)
  {
    m_sCategoryCode = sCategoryCode;
    return this;
  }

  /** @return VAT category rate (BT-119). May be {@code null}. */
  @Nullable
  public BigDecimal getRate ()
  {
    return m_aRate;
  }

  /**
   * Set the VAT category rate (BT-119).
   *
   * @param aRate
   *        Rate as a {@link BigDecimal} (e.g. {@code 19} for 19 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setRate (@Nullable final BigDecimal aRate)
  {
    m_aRate = aRate;
    return this;
  }

  /** @return VAT exemption reason text (BT-120). May be {@code null}. */
  @Nullable
  public String getExemptionReason ()
  {
    return m_sExemptionReason;
  }

  /**
   * Set the VAT exemption reason text (BT-120).
   *
   * @param sExemptionReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setExemptionReason (@Nullable final String sExemptionReason)
  {
    m_sExemptionReason = sExemptionReason;
    return this;
  }

  /** @return VAT exemption reason code (BT-121). May be {@code null}. */
  @Nullable
  public String getExemptionReasonCode ()
  {
    return m_sExemptionReasonCode;
  }

  /**
   * Set the VAT exemption reason code (BT-121).
   *
   * @param sExemptionReasonCode
   *        Reason code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaVATBreakdown setExemptionReasonCode (@Nullable final String sExemptionReasonCode)
  {
    m_sExemptionReasonCode = sExemptionReasonCode;
    return this;
  }
}
