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
 * BG-27 INVOICE LINE ALLOWANCE (0..n on a line). Carries BT-136 (allowance amount), BT-137
 * (base amount), BT-138 (percentage), BT-139 (reason text) and BT-140 (reason code). VAT
 * details are not repeated here because line-level VAT applies to the whole line (see BG-30).
 *
 * @author Philip Helger
 */
public class PhloxLineAllowance
{
  private BigDecimal m_aAmount;
  private BigDecimal m_aBaseAmount;
  private BigDecimal m_aPercentage;
  private String m_sReason;
  private String m_sReasonCode;

  /** Create an empty line allowance. */
  public PhloxLineAllowance ()
  {}

  /** @return Line allowance amount (BT-136). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the line allowance amount (BT-136).
   *
   * @param aAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineAllowance setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  /** @return Line allowance base amount (BT-137). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  /**
   * Set the line allowance base amount (BT-137).
   *
   * @param aBaseAmount
   *        Base amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineAllowance setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  /** @return Line allowance percentage (BT-138). May be {@code null}. */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the line allowance percentage (BT-138).
   *
   * @param aPercentage
   *        Percentage as a {@link BigDecimal}; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineAllowance setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /** @return Line allowance reason text (BT-139). May be {@code null}. */
  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  /**
   * Set the line allowance reason text (BT-139).
   *
   * @param sReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineAllowance setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  /** @return Line allowance reason code (BT-140) — UNTDID 5189. May be {@code null}. */
  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  /**
   * Set the line allowance reason code (BT-140).
   *
   * @param sReasonCode
   *        UNTDID 5189 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineAllowance setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }
}
