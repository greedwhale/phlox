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
 * BG-29 PRICE DETAILS. BT-146..BT-150.
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

  public NormaPriceDetails ()
  {}

  @Nullable
  public BigDecimal getNetPrice ()
  {
    return m_aNetPrice;
  }

  @NonNull
  public NormaPriceDetails setNetPrice (@Nullable final BigDecimal aNetPrice)
  {
    m_aNetPrice = aNetPrice;
    return this;
  }

  @Nullable
  public BigDecimal getDiscount ()
  {
    return m_aDiscount;
  }

  @NonNull
  public NormaPriceDetails setDiscount (@Nullable final BigDecimal aDiscount)
  {
    m_aDiscount = aDiscount;
    return this;
  }

  @Nullable
  public BigDecimal getGrossPrice ()
  {
    return m_aGrossPrice;
  }

  @NonNull
  public NormaPriceDetails setGrossPrice (@Nullable final BigDecimal aGrossPrice)
  {
    m_aGrossPrice = aGrossPrice;
    return this;
  }

  @Nullable
  public BigDecimal getBaseQuantity ()
  {
    return m_aBaseQuantity;
  }

  @NonNull
  public NormaPriceDetails setBaseQuantity (@Nullable final BigDecimal aBaseQuantity)
  {
    m_aBaseQuantity = aBaseQuantity;
    return this;
  }

  @Nullable
  public String getBaseQuantityUnit ()
  {
    return m_sBaseQuantityUnit;
  }

  @NonNull
  public NormaPriceDetails setBaseQuantityUnit (@Nullable final String sBaseQuantityUnit)
  {
    m_sBaseQuantityUnit = sBaseQuantityUnit;
    return this;
  }
}
