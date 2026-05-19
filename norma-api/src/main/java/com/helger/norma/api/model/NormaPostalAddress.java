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
 * Postal address. Shape shared by BG-5 (Seller), BG-8 (Buyer), BG-12 (Tax representative) and
 * BG-15 (Deliver to). Field BT numbers vary by occurrence; the EN 16931 mapping table covers it.
 *
 * @author Philip Helger
 */
public class NormaPostalAddress
{
  private String m_sLine1;
  private String m_sLine2;
  private String m_sLine3;
  private String m_sCity;
  private String m_sPostCode;
  private String m_sCountrySubdivision;
  private String m_sCountryCode;

  public NormaPostalAddress ()
  {}

  @Nullable
  public String getLine1 ()
  {
    return m_sLine1;
  }

  @NonNull
  public NormaPostalAddress setLine1 (@Nullable final String sLine1)
  {
    m_sLine1 = sLine1;
    return this;
  }

  @Nullable
  public String getLine2 ()
  {
    return m_sLine2;
  }

  @NonNull
  public NormaPostalAddress setLine2 (@Nullable final String sLine2)
  {
    m_sLine2 = sLine2;
    return this;
  }

  @Nullable
  public String getLine3 ()
  {
    return m_sLine3;
  }

  @NonNull
  public NormaPostalAddress setLine3 (@Nullable final String sLine3)
  {
    m_sLine3 = sLine3;
    return this;
  }

  @Nullable
  public String getCity ()
  {
    return m_sCity;
  }

  @NonNull
  public NormaPostalAddress setCity (@Nullable final String sCity)
  {
    m_sCity = sCity;
    return this;
  }

  @Nullable
  public String getPostCode ()
  {
    return m_sPostCode;
  }

  @NonNull
  public NormaPostalAddress setPostCode (@Nullable final String sPostCode)
  {
    m_sPostCode = sPostCode;
    return this;
  }

  @Nullable
  public String getCountrySubdivision ()
  {
    return m_sCountrySubdivision;
  }

  @NonNull
  public NormaPostalAddress setCountrySubdivision (@Nullable final String sCountrySubdivision)
  {
    m_sCountrySubdivision = sCountrySubdivision;
    return this;
  }

  @Nullable
  public String getCountryCode ()
  {
    return m_sCountryCode;
  }

  @NonNull
  public NormaPostalAddress setCountryCode (@Nullable final String sCountryCode)
  {
    m_sCountryCode = sCountryCode;
    return this;
  }

  public boolean isEmpty ()
  {
    return m_sLine1 == null &&
           m_sLine2 == null &&
           m_sLine3 == null &&
           m_sCity == null &&
           m_sPostCode == null &&
           m_sCountrySubdivision == null &&
           m_sCountryCode == null;
  }
}
