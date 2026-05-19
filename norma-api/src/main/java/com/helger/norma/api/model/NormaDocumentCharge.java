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
 * BG-21 DOCUMENT LEVEL CHARGE (0..n). Carries BT-99 (charge amount), BT-100 (base amount),
 * BT-101 (percentage), BT-102 (VAT category code), BT-103 (VAT rate), BT-104 (reason text)
 * and BT-105 (reason code). Mirror of {@link NormaDocumentAllowance}.
 *
 * @author Philip Helger
 */
public class NormaDocumentCharge
{
  private BigDecimal m_aAmount;
  private BigDecimal m_aBaseAmount;
  private BigDecimal m_aPercentage;
  private String m_sVATCategoryCode;
  private BigDecimal m_aVATRate;
  private String m_sReason;
  private String m_sReasonCode;

  /** Create an empty document-level charge. */
  public NormaDocumentCharge ()
  {}

  /** @return Charge amount (BT-99). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the charge amount (BT-99).
   *
   * @param aAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  /** @return Charge base amount (BT-100). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  /**
   * Set the charge base amount (BT-100).
   *
   * @param aBaseAmount
   *        Base amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  /** @return Charge percentage (BT-101). May be {@code null}. */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the charge percentage (BT-101).
   *
   * @param aPercentage
   *        Percentage as a {@link BigDecimal} (e.g. {@code 5} for 5 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /** @return VAT category code (BT-102) — UNTDID 5305. May be {@code null}. */
  @Nullable
  public String getVATCategoryCode ()
  {
    return m_sVATCategoryCode;
  }

  /**
   * Set the VAT category code (BT-102).
   *
   * @param sVATCategoryCode
   *        UNTDID 5305 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setVATCategoryCode (@Nullable final String sVATCategoryCode)
  {
    m_sVATCategoryCode = sVATCategoryCode;
    return this;
  }

  /** @return VAT rate (BT-103). May be {@code null}. */
  @Nullable
  public BigDecimal getVATRate ()
  {
    return m_aVATRate;
  }

  /**
   * Set the VAT rate (BT-103).
   *
   * @param aVATRate
   *        Rate as a {@link BigDecimal} (e.g. {@code 19} for 19 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setVATRate (@Nullable final BigDecimal aVATRate)
  {
    m_aVATRate = aVATRate;
    return this;
  }

  /** @return Charge reason text (BT-104). May be {@code null}. */
  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  /**
   * Set the charge reason text (BT-104).
   *
   * @param sReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  /** @return Charge reason code (BT-105) — UNTDID 7161. May be {@code null}. */
  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  /**
   * Set the charge reason code (BT-105).
   *
   * @param sReasonCode
   *        UNTDID 7161 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentCharge setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }
}
