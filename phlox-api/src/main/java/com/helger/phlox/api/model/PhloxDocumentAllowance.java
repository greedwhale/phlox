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
package com.helger.phlox.api.model;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-20 DOCUMENT LEVEL ALLOWANCE (0..n). Carries BT-92 (allowance amount), BT-93 (base
 * amount), BT-94 (percentage), BT-95 (VAT category code), BT-96 (VAT rate), BT-97 (reason text)
 * and BT-98 (reason code).
 *
 * @author Philip Helger
 */
public class PhloxDocumentAllowance
{
  private BigDecimal m_aAmount;
  private BigDecimal m_aBaseAmount;
  private BigDecimal m_aPercentage;
  private String m_sVatCategoryCode;
  private BigDecimal m_aVatRate;
  private String m_sReason;
  private String m_sReasonCode;

  /** Create an empty document-level allowance. */
  public PhloxDocumentAllowance ()
  {}

  /** @return Allowance amount (BT-92). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the allowance amount (BT-92).
   *
   * @param aAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  /** @return Allowance base amount (BT-93). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  /**
   * Set the allowance base amount (BT-93).
   *
   * @param aBaseAmount
   *        Base amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  /** @return Allowance percentage (BT-94). May be {@code null}. */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the allowance percentage (BT-94).
   *
   * @param aPercentage
   *        Percentage as a {@link BigDecimal} (e.g. {@code 10} for 10 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /** @return VAT category code (BT-95) — UNTDID 5305. May be {@code null}. */
  @Nullable
  public String getVatCategoryCode ()
  {
    return m_sVatCategoryCode;
  }

  /**
   * Set the VAT category code (BT-95).
   *
   * @param sVatCategoryCode
   *        UNTDID 5305 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setVatCategoryCode (@Nullable final String sVatCategoryCode)
  {
    m_sVatCategoryCode = sVatCategoryCode;
    return this;
  }

  /** @return VAT rate (BT-96). May be {@code null}. */
  @Nullable
  public BigDecimal getVatRate ()
  {
    return m_aVatRate;
  }

  /**
   * Set the VAT rate (BT-96).
   *
   * @param aVatRate
   *        Rate as a {@link BigDecimal} (e.g. {@code 19} for 19 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setVatRate (@Nullable final BigDecimal aVatRate)
  {
    m_aVatRate = aVatRate;
    return this;
  }

  /** @return Allowance reason text (BT-97). May be {@code null}. */
  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  /**
   * Set the allowance reason text (BT-97).
   *
   * @param sReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  /** @return Allowance reason code (BT-98) — UNTDID 5189. May be {@code null}. */
  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  /**
   * Set the allowance reason code (BT-98).
   *
   * @param sReasonCode
   *        UNTDID 5189 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentAllowance setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }
}
