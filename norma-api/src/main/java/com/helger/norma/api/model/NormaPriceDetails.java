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
 * BG-29 PRICE DETAILS (1..1 on a line). Carries BT-146 (item net price), BT-147 (item price
 * discount), BT-148 (item gross price), BT-149 (item price base quantity) and BT-150 (item
 * price base quantity unit of measure).
 *
 * @author Philip Helger
 */
public class NormaPriceDetails
{
  private BigDecimal m_aNetPrice;
  private BigDecimal m_aDiscount;
  private BigDecimal m_aGrossPrice;
  private BigDecimal m_aBaseQuantity;
  private String m_sBaseQuantityUnit;

  /** Create an empty price-details record. */
  public NormaPriceDetails ()
  {}

  /** @return Item net price (BT-146). May be {@code null}. */
  @Nullable
  public BigDecimal getNetPrice ()
  {
    return m_aNetPrice;
  }

  /**
   * Set the item net price (BT-146).
   *
   * @param aNetPrice
   *        Net price; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPriceDetails setNetPrice (@Nullable final BigDecimal aNetPrice)
  {
    m_aNetPrice = aNetPrice;
    return this;
  }

  /** @return Item price discount (BT-147). May be {@code null}. */
  @Nullable
  public BigDecimal getDiscount ()
  {
    return m_aDiscount;
  }

  /**
   * Set the item price discount (BT-147).
   *
   * @param aDiscount
   *        Discount amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPriceDetails setDiscount (@Nullable final BigDecimal aDiscount)
  {
    m_aDiscount = aDiscount;
    return this;
  }

  /** @return Item gross price (BT-148). May be {@code null}. */
  @Nullable
  public BigDecimal getGrossPrice ()
  {
    return m_aGrossPrice;
  }

  /**
   * Set the item gross price (BT-148).
   *
   * @param aGrossPrice
   *        Gross price; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPriceDetails setGrossPrice (@Nullable final BigDecimal aGrossPrice)
  {
    m_aGrossPrice = aGrossPrice;
    return this;
  }

  /** @return Item price base quantity (BT-149). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseQuantity ()
  {
    return m_aBaseQuantity;
  }

  /**
   * Set the item price base quantity (BT-149).
   *
   * @param aBaseQuantity
   *        Base quantity; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPriceDetails setBaseQuantity (@Nullable final BigDecimal aBaseQuantity)
  {
    m_aBaseQuantity = aBaseQuantity;
    return this;
  }

  /** @return Item price base quantity unit of measure (BT-150) — UN/ECE Rec 20/21. May be {@code null}. */
  @Nullable
  public String getBaseQuantityUnit ()
  {
    return m_sBaseQuantityUnit;
  }

  /**
   * Set the item price base quantity unit of measure (BT-150).
   *
   * @param sBaseQuantityUnit
   *        Unit code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPriceDetails setBaseQuantityUnit (@Nullable final String sBaseQuantityUnit)
  {
    m_sBaseQuantityUnit = sBaseQuantityUnit;
    return this;
  }
}
