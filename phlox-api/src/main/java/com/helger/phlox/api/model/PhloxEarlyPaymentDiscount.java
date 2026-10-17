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
 * BG-35 EARLY PAYMENT DISCOUNT (0..n within BG-33). Structured early-payment discount: the
 * last date on which it can be claimed (BT-170) and the discount as percentage (BT-171) and/or
 * amount (BT-172). Introduced by EN 16931:2026.
 *
 * @author Philip Helger
 */
public class PhloxEarlyPaymentDiscount
{
  private LocalDate m_aEndDate;
  private BigDecimal m_aPercentage;
  private BigDecimal m_aAmount;

  /** Create an empty instance. */
  public PhloxEarlyPaymentDiscount ()
  {}

  /**
   * Get the discount end date (BT-170).
   *
   * @return Discount end date (BT-170). May be {@code null}.
   */
  @Nullable
  public LocalDate getEndDate ()
  {
    return m_aEndDate;
  }

  /**
   * Set the discount end date (BT-170).
   *
   * @param aEndDate
   *        Discount end date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxEarlyPaymentDiscount setEndDate (@Nullable final LocalDate aEndDate)
  {
    m_aEndDate = aEndDate;
    return this;
  }

  /**
   * Get the discount percentage (BT-171).
   *
   * @return Discount percentage (BT-171). May be {@code null}.
   */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the discount percentage (BT-171).
   *
   * @param aPercentage
   *        Discount percentage; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxEarlyPaymentDiscount setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /**
   * Get the discount amount (BT-172).
   *
   * @return Discount amount (BT-172). May be {@code null}.
   */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the discount amount (BT-172).
   *
   * @param aAmount
   *        Discount amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxEarlyPaymentDiscount setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }
}
