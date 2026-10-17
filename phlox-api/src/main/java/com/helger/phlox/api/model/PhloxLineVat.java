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
 * BG-30 LINE VAT INFORMATION (1..1 on a line). Carries BT-151 (invoiced item VAT category code)
 * and BT-152 (invoiced item VAT rate).
 *
 * @author Philip Helger
 */
public class PhloxLineVat
{
  private String m_sCategoryCode;
  private BigDecimal m_aRate;

  /** Create an empty line VAT information record. */
  public PhloxLineVat ()
  {}

  /** @return Invoiced item VAT category code (BT-151) — UNTDID 5305. May be {@code null}. */
  @Nullable
  public String getCategoryCode ()
  {
    return m_sCategoryCode;
  }

  /**
   * Set the invoiced item VAT category code (BT-151).
   *
   * @param sCategoryCode
   *        UNTDID 5305 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineVat setCategoryCode (@Nullable final String sCategoryCode)
  {
    m_sCategoryCode = sCategoryCode;
    return this;
  }

  /** @return Invoiced item VAT rate (BT-152). May be {@code null}. */
  @Nullable
  public BigDecimal getRate ()
  {
    return m_aRate;
  }

  /**
   * Set the invoiced item VAT rate (BT-152).
   *
   * @param aRate
   *        Rate as a {@link BigDecimal} (e.g. {@code 19} for 19 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineVat setRate (@Nullable final BigDecimal aRate)
  {
    m_aRate = aRate;
    return this;
  }
}
