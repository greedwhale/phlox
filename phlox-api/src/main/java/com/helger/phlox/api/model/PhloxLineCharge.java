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
 * BG-28 INVOICE LINE CHARGE (0..n on a line). Carries BT-141 (charge amount), BT-142 (base
 * amount), BT-143 (percentage), BT-144 (reason text) and BT-145 (reason code). Same shape as
 * {@link PhloxLineAllowance}.
 *
 * @author Philip Helger
 */
public class PhloxLineCharge
{
  private BigDecimal m_aAmount;
  private BigDecimal m_aBaseAmount;
  private BigDecimal m_aPercentage;
  private String m_sReason;
  private String m_sReasonCode;

  /** Create an empty line charge. */
  public PhloxLineCharge ()
  {}

  /** @return Line charge amount (BT-141). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the line charge amount (BT-141).
   *
   * @param aAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineCharge setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  /** @return Line charge base amount (BT-142). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  /**
   * Set the line charge base amount (BT-142).
   *
   * @param aBaseAmount
   *        Base amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineCharge setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  /** @return Line charge percentage (BT-143). May be {@code null}. */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the line charge percentage (BT-143).
   *
   * @param aPercentage
   *        Percentage as a {@link BigDecimal}; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineCharge setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /** @return Line charge reason text (BT-144). May be {@code null}. */
  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  /**
   * Set the line charge reason text (BT-144).
   *
   * @param sReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineCharge setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  /** @return Line charge reason code (BT-145) — UNTDID 7161. May be {@code null}. */
  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  /**
   * Set the line charge reason code (BT-145).
   *
   * @param sReasonCode
   *        UNTDID 7161 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineCharge setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }
}
