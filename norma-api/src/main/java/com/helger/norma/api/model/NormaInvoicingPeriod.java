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

import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-14 INVOICING PERIOD (also reused for BG-26 invoice line period). BT-73/BT-74 (or
 * BT-134/BT-135).
 *
 * @author Philip Helger
 */
public class NormaInvoicingPeriod
{
  private LocalDate m_aStartDate;
  private LocalDate m_aEndDate;

  public NormaInvoicingPeriod ()
  {}

  @Nullable
  public LocalDate getStartDate ()
  {
    return m_aStartDate;
  }

  @NonNull
  public NormaInvoicingPeriod setStartDate (@Nullable final LocalDate aStartDate)
  {
    m_aStartDate = aStartDate;
    return this;
  }

  @Nullable
  public LocalDate getEndDate ()
  {
    return m_aEndDate;
  }

  @NonNull
  public NormaInvoicingPeriod setEndDate (@Nullable final LocalDate aEndDate)
  {
    m_aEndDate = aEndDate;
    return this;
  }

  public boolean isEmpty ()
  {
    return m_aStartDate == null && m_aEndDate == null;
  }
}
