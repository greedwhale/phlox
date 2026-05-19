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
 * BG-21 DOCUMENT LEVEL CHARGE. BT-99..BT-105. Mirror of {@link NormaDocumentAllowance} with
 * the same field set.
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

  public NormaDocumentCharge ()
  {}

  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  @NonNull
  public NormaDocumentCharge setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  @NonNull
  public NormaDocumentCharge setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  @NonNull
  public NormaDocumentCharge setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  @Nullable
  public String getVATCategoryCode ()
  {
    return m_sVATCategoryCode;
  }

  @NonNull
  public NormaDocumentCharge setVATCategoryCode (@Nullable final String sVATCategoryCode)
  {
    m_sVATCategoryCode = sVATCategoryCode;
    return this;
  }

  @Nullable
  public BigDecimal getVATRate ()
  {
    return m_aVATRate;
  }

  @NonNull
  public NormaDocumentCharge setVATRate (@Nullable final BigDecimal aVATRate)
  {
    m_aVATRate = aVATRate;
    return this;
  }

  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  @NonNull
  public NormaDocumentCharge setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  @NonNull
  public NormaDocumentCharge setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }
}
