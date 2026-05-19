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
 * Trade contact details. Shape shared by BG-6 (Seller contact) and BG-9 (Buyer contact).
 *
 * @author Philip Helger
 */
public class NormaContact
{
  private String m_sName;
  private String m_sTelephone;
  private String m_sEmail;

  public NormaContact ()
  {}

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaContact setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  @Nullable
  public String getTelephone ()
  {
    return m_sTelephone;
  }

  @NonNull
  public NormaContact setTelephone (@Nullable final String sTelephone)
  {
    m_sTelephone = sTelephone;
    return this;
  }

  @Nullable
  public String getEmail ()
  {
    return m_sEmail;
  }

  @NonNull
  public NormaContact setEmail (@Nullable final String sEmail)
  {
    m_sEmail = sEmail;
    return this;
  }

  public boolean isEmpty ()
  {
    return m_sName == null && m_sTelephone == null && m_sEmail == null;
  }
}
