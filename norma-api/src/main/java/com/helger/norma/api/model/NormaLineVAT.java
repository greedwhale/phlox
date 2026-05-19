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
 * BG-30 LINE VAT INFORMATION. BT-151 category code, BT-152 rate.
 *
 * @author Philip Helger
 */
public class NormaLineVAT
{
  private String m_sCategoryCode;
  private BigDecimal m_aRate;

  public NormaLineVAT ()
  {}

  @Nullable
  public String getCategoryCode ()
  {
    return m_sCategoryCode;
  }

  @NonNull
  public NormaLineVAT setCategoryCode (@Nullable final String sCategoryCode)
  {
    m_sCategoryCode = sCategoryCode;
    return this;
  }

  @Nullable
  public BigDecimal getRate ()
  {
    return m_aRate;
  }

  @NonNull
  public NormaLineVAT setRate (@Nullable final BigDecimal aRate)
  {
    m_aRate = aRate;
    return this;
  }
}
