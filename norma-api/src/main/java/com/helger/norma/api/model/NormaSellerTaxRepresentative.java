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
 * BG-11 SELLER TAX REPRESENTATIVE PARTY plus nested BG-12 postal address. Carries
 * BT-62, BT-63 and the address fields.
 *
 * @author Philip Helger
 */
public class NormaSellerTaxRepresentative
{
  private String m_sName;
  private String m_sVATIdentifier;
  private NormaPostalAddress m_aPostalAddress;

  public NormaSellerTaxRepresentative ()
  {}

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaSellerTaxRepresentative setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  @Nullable
  public String getVATIdentifier ()
  {
    return m_sVATIdentifier;
  }

  @NonNull
  public NormaSellerTaxRepresentative setVATIdentifier (@Nullable final String sVATIdentifier)
  {
    m_sVATIdentifier = sVATIdentifier;
    return this;
  }

  @Nullable
  public NormaPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  @NonNull
  public NormaSellerTaxRepresentative setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }
}
