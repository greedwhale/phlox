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
 * Trade contact details. Shape shared by BG-6 (Seller contact, BT-41/42/43) and BG-9 (Buyer
 * contact, BT-56/57/58).
 *
 * @author Philip Helger
 */
public class PhloxContact
{
  private String m_sName;
  private String m_sTelephone;
  private String m_sEmail;

  /** Create an empty contact. */
  public PhloxContact ()
  {}

  /**
   * Get the contact point name (BT-41 / BT-56).
   *
   * @return Contact point name (BT-41 / BT-56). May be {@code null}.
   */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the contact point name (BT-41 / BT-56).
   *
   * @param sName
   *        Contact name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxContact setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /**
   * Get the telephone number (BT-42 / BT-57).
   *
   * @return Telephone number (BT-42 / BT-57). May be {@code null}.
   */
  @Nullable
  public String getTelephone ()
  {
    return m_sTelephone;
  }

  /**
   * Set the telephone number (BT-42 / BT-57).
   *
   * @param sTelephone
   *        Telephone string; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxContact setTelephone (@Nullable final String sTelephone)
  {
    m_sTelephone = sTelephone;
    return this;
  }

  /**
   * Get the email address (BT-43 / BT-58).
   *
   * @return Email address (BT-43 / BT-58). May be {@code null}.
   */
  @Nullable
  public String getEmail ()
  {
    return m_sEmail;
  }

  /**
   * Set the email address (BT-43 / BT-58).
   *
   * @param sEmail
   *        Email; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxContact setEmail (@Nullable final String sEmail)
  {
    m_sEmail = sEmail;
    return this;
  }

  /**
   * Get the {@code true} when every field is {@code null}.
   *
   * @return {@code true} when every field is {@code null}.
   */
  public boolean isEmpty ()
  {
    return m_sName == null && m_sTelephone == null && m_sEmail == null;
  }
}
