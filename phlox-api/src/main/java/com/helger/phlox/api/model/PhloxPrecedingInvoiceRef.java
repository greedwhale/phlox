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

import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-3 PRECEDING INVOICE REFERENCE (0..n). Carries the preceding invoice number (BT-25) and
 * its issue date (BT-26).
 *
 * @author Philip Helger
 */
public class PhloxPrecedingInvoiceRef
{
  private String m_sNumber;
  private LocalDate m_aIssueDate;
  private String m_sTypeCode;

  /** Create an empty preceding-invoice reference. */
  public PhloxPrecedingInvoiceRef ()
  {}

  /** @return Preceding invoice number (BT-25). May be {@code null}. */
  @Nullable
  public String getNumber ()
  {
    return m_sNumber;
  }

  /**
   * Set the preceding invoice number (BT-25).
   *
   * @param sNumber
   *        Preceding invoice number; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPrecedingInvoiceRef setNumber (@Nullable final String sNumber)
  {
    m_sNumber = sNumber;
    return this;
  }

  /** @return Preceding invoice issue date (BT-26). May be {@code null}. */
  @Nullable
  public LocalDate getIssueDate ()
  {
    return m_aIssueDate;
  }

  /**
   * Set the preceding invoice issue date (BT-26).
   *
   * @param aIssueDate
   *        Issue date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPrecedingInvoiceRef setIssueDate (@Nullable final LocalDate aIssueDate)
  {
    m_aIssueDate = aIssueDate;
    return this;
  }

  /** @return Preceding invoice type code (BT-202). May be {@code null}. */
  @Nullable
  public String getTypeCode ()
  {
    return m_sTypeCode;
  }

  /**
   * Set the preceding invoice type code (BT-202).
   *
   * @param sTypeCode
   *        Preceding invoice type code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPrecedingInvoiceRef setTypeCode (@Nullable final String sTypeCode)
  {
    m_sTypeCode = sTypeCode;
    return this;
  }
}
