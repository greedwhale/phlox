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
import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-36 LATE PAYMENT PENALTY (0..n within BG-33). Structured late-payment penalty: the first
 * date on which it applies (BT-181) and the penalty as yearly interest percentage (BT-182)
 * and/or fixed amount (BT-183). Introduced by EN 16931:2026.
 *
 * @author Philip Helger
 */
public class PhloxLatePaymentPenalty
{
  private LocalDate m_aStartDate;
  private BigDecimal m_aYearlyInterestPercentage;
  private BigDecimal m_aAmount;

  /** Create an empty instance. */
  public PhloxLatePaymentPenalty ()
  {}

  /** @return Penalty start date (BT-181). May be {@code null}. */
  @Nullable
  public LocalDate getStartDate ()
  {
    return m_aStartDate;
  }

  /**
   * Set the penalty start date (BT-181).
   *
   * @param aStartDate
   *        Penalty start date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLatePaymentPenalty setStartDate (@Nullable final LocalDate aStartDate)
  {
    m_aStartDate = aStartDate;
    return this;
  }

  /** @return Penalty yearly interest percentage (BT-182). May be {@code null}. */
  @Nullable
  public BigDecimal getYearlyInterestPercentage ()
  {
    return m_aYearlyInterestPercentage;
  }

  /**
   * Set the penalty yearly interest percentage (BT-182).
   *
   * @param aYearlyInterestPercentage
   *        Penalty yearly interest percentage; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLatePaymentPenalty setYearlyInterestPercentage (@Nullable final BigDecimal aYearlyInterestPercentage)
  {
    m_aYearlyInterestPercentage = aYearlyInterestPercentage;
    return this;
  }

  /** @return Penalty amount (BT-183). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the penalty amount (BT-183).
   *
   * @param aAmount
   *        Penalty amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLatePaymentPenalty setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }
}
