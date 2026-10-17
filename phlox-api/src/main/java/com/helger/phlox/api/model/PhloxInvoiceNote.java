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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-1 INVOICE NOTE (0..n on the invoice). Carries the subject-code prefix (BT-21) and the
 * free-text content (BT-22).
 *
 * @author Philip Helger
 */
public class PhloxInvoiceNote
{
  private String m_sSubjectCode;
  private String m_sContent;

  /** Create an empty note. */
  public PhloxInvoiceNote ()
  {}

  /**
   * Create a note with both subject code and content set.
   *
   * @param sSubjectCode
   *        Note subject code (BT-21); may be {@code null}.
   * @param sContent
   *        Note content (BT-22); may be {@code null}.
   */
  public PhloxInvoiceNote (@Nullable final String sSubjectCode, @Nullable final String sContent)
  {
    m_sSubjectCode = sSubjectCode;
    m_sContent = sContent;
  }

  /**
   * Get the note subject code (BT-21).
   *
   * @return Note subject code (BT-21). May be {@code null}.
   */
  @Nullable
  public String getSubjectCode ()
  {
    return m_sSubjectCode;
  }

  /**
   * Set the note subject code (BT-21).
   *
   * @param sSubjectCode
   *        Subject code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxInvoiceNote setSubjectCode (@Nullable final String sSubjectCode)
  {
    m_sSubjectCode = sSubjectCode;
    return this;
  }

  /**
   * Get the note content (BT-22).
   *
   * @return Note content (BT-22). May be {@code null}.
   */
  @Nullable
  public String getContent ()
  {
    return m_sContent;
  }

  /**
   * Set the note content (BT-22).
   *
   * @param sContent
   *        Content text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxInvoiceNote setContent (@Nullable final String sContent)
  {
    m_sContent = sContent;
    return this;
  }
}
