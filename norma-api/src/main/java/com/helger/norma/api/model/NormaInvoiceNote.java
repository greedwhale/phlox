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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-1 INVOICE NOTE. BT-21 subject code, BT-22 content.
 *
 * @author Philip Helger
 */
public class NormaInvoiceNote
{
  private String m_sSubjectCode;
  private String m_sContent;

  public NormaInvoiceNote ()
  {}

  public NormaInvoiceNote (@Nullable final String sSubjectCode, @Nullable final String sContent)
  {
    m_sSubjectCode = sSubjectCode;
    m_sContent = sContent;
  }

  @Nullable
  public String getSubjectCode ()
  {
    return m_sSubjectCode;
  }

  @NonNull
  public NormaInvoiceNote setSubjectCode (@Nullable final String sSubjectCode)
  {
    m_sSubjectCode = sSubjectCode;
    return this;
  }

  @Nullable
  public String getContent ()
  {
    return m_sContent;
  }

  @NonNull
  public NormaInvoiceNote setContent (@Nullable final String sContent)
  {
    m_sContent = sContent;
    return this;
  }
}
