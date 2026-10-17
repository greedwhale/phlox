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
 * BG-39 LINE-LEVEL PRECEDING INVOICE REFERENCE (0..n on a line). Line-level equivalent of BG-3:
 * the preceding invoice reference (BT-217), its issue date (BT-218), its type code (BT-219) and
 * the referenced line (BT-220). Introduced by EN 16931:2026.
 *
 * @author Philip Helger
 */
public class PhloxLinePrecedingInvoiceRef
{
  private String m_sNumber;
  private LocalDate m_aIssueDate;
  private String m_sTypeCode;
  private String m_sLineReference;

  /** Create an empty instance. */
  public PhloxLinePrecedingInvoiceRef ()
  {}

  /** @return Line-level preceding invoice reference (BT-217). May be {@code null}. */
  @Nullable
  public String getNumber ()
  {
    return m_sNumber;
  }

  /**
   * Set the line-level preceding invoice reference (BT-217).
   *
   * @param sNumber
   *        Line-level preceding invoice reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLinePrecedingInvoiceRef setNumber (@Nullable final String sNumber)
  {
    m_sNumber = sNumber;
    return this;
  }

  /** @return Line-level preceding invoice issue date (BT-218). May be {@code null}. */
  @Nullable
  public LocalDate getIssueDate ()
  {
    return m_aIssueDate;
  }

  /**
   * Set the line-level preceding invoice issue date (BT-218).
   *
   * @param aIssueDate
   *        Line-level preceding invoice issue date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLinePrecedingInvoiceRef setIssueDate (@Nullable final LocalDate aIssueDate)
  {
    m_aIssueDate = aIssueDate;
    return this;
  }

  /** @return Line-level preceding invoice type code (BT-219). May be {@code null}. */
  @Nullable
  public String getTypeCode ()
  {
    return m_sTypeCode;
  }

  /**
   * Set the line-level preceding invoice type code (BT-219).
   *
   * @param sTypeCode
   *        Line-level preceding invoice type code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLinePrecedingInvoiceRef setTypeCode (@Nullable final String sTypeCode)
  {
    m_sTypeCode = sTypeCode;
    return this;
  }

  /** @return Line-level preceding invoice line reference (BT-220). May be {@code null}. */
  @Nullable
  public String getLineReference ()
  {
    return m_sLineReference;
  }

  /**
   * Set the line-level preceding invoice line reference (BT-220).
   *
   * @param sLineReference
   *        Line-level preceding invoice line reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLinePrecedingInvoiceRef setLineReference (@Nullable final String sLineReference)
  {
    m_sLineReference = sLineReference;
    return this;
  }
}
