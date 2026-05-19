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
 * BG-3 PRECEDING INVOICE REFERENCE. BT-25 number, BT-26 issue date.
 *
 * @author Philip Helger
 */
public class NormaPrecedingInvoiceRef
{
  private String m_sNumber;
  private LocalDate m_aIssueDate;

  public NormaPrecedingInvoiceRef ()
  {}

  @Nullable
  public String getNumber ()
  {
    return m_sNumber;
  }

  @NonNull
  public NormaPrecedingInvoiceRef setNumber (@Nullable final String sNumber)
  {
    m_sNumber = sNumber;
    return this;
  }

  @Nullable
  public LocalDate getIssueDate ()
  {
    return m_aIssueDate;
  }

  @NonNull
  public NormaPrecedingInvoiceRef setIssueDate (@Nullable final LocalDate aIssueDate)
  {
    m_aIssueDate = aIssueDate;
    return this;
  }
}
