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
 * Postal address. Shape shared by BG-5 (Seller postal address), BG-8 (Buyer postal address),
 * BG-12 (Seller tax representative postal address) and BG-15 (Deliver-to address). The
 * underlying BT numbers vary by occurrence:
 * <ul>
 * <li>BG-5: BT-35..BT-40 (with BT-162 line 3)</li>
 * <li>BG-8: BT-50..BT-55 (with BT-163 line 3)</li>
 * <li>BG-12: BT-64..BT-69 (with BT-164 line 3)</li>
 * <li>BG-15: BT-75..BT-80 (with BT-165 line 3)</li>
 * </ul>
 *
 * @author Philip Helger
 */
public class PhloxPostalAddress
{
  private String m_sLine1;
  private String m_sLine2;
  private String m_sLine3;
  private String m_sCity;
  private String m_sPostCode;
  private String m_sCountrySubdivision;
  private String m_sCountryCode;

  /** Create an empty address. */
  public PhloxPostalAddress ()
  {}

  /** @return Address line 1 (BT-35 / BT-50 / BT-64 / BT-75). May be {@code null}. */
  @Nullable
  public String getLine1 ()
  {
    return m_sLine1;
  }

  /**
   * Set address line 1 (BT-35 / BT-50 / BT-64 / BT-75).
   *
   * @param sLine1
   *        Line 1 text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setLine1 (@Nullable final String sLine1)
  {
    m_sLine1 = sLine1;
    return this;
  }

  /** @return Address line 2 (BT-36 / BT-51 / BT-65 / BT-76). May be {@code null}. */
  @Nullable
  public String getLine2 ()
  {
    return m_sLine2;
  }

  /**
   * Set address line 2 (BT-36 / BT-51 / BT-65 / BT-76).
   *
   * @param sLine2
   *        Line 2 text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setLine2 (@Nullable final String sLine2)
  {
    m_sLine2 = sLine2;
    return this;
  }

  /** @return Address line 3 (BT-162 / BT-163 / BT-164 / BT-165). May be {@code null}. */
  @Nullable
  public String getLine3 ()
  {
    return m_sLine3;
  }

  /**
   * Set address line 3 (BT-162 / BT-163 / BT-164 / BT-165).
   *
   * @param sLine3
   *        Line 3 text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setLine3 (@Nullable final String sLine3)
  {
    m_sLine3 = sLine3;
    return this;
  }

  /** @return City name (BT-37 / BT-52 / BT-66 / BT-77). May be {@code null}. */
  @Nullable
  public String getCity ()
  {
    return m_sCity;
  }

  /**
   * Set city name (BT-37 / BT-52 / BT-66 / BT-77).
   *
   * @param sCity
   *        City name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setCity (@Nullable final String sCity)
  {
    m_sCity = sCity;
    return this;
  }

  /** @return Post code (BT-38 / BT-53 / BT-67 / BT-78). May be {@code null}. */
  @Nullable
  public String getPostCode ()
  {
    return m_sPostCode;
  }

  /**
   * Set post code (BT-38 / BT-53 / BT-67 / BT-78).
   *
   * @param sPostCode
   *        Post code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setPostCode (@Nullable final String sPostCode)
  {
    m_sPostCode = sPostCode;
    return this;
  }

  /** @return Country subdivision (BT-39 / BT-54 / BT-68 / BT-79). May be {@code null}. */
  @Nullable
  public String getCountrySubdivision ()
  {
    return m_sCountrySubdivision;
  }

  /**
   * Set country subdivision (BT-39 / BT-54 / BT-68 / BT-79).
   *
   * @param sCountrySubdivision
   *        Country subdivision; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setCountrySubdivision (@Nullable final String sCountrySubdivision)
  {
    m_sCountrySubdivision = sCountrySubdivision;
    return this;
  }

  /** @return ISO 3166-1 alpha-2 country code (BT-40 / BT-55 / BT-69 / BT-80). May be {@code null}. */
  @Nullable
  public String getCountryCode ()
  {
    return m_sCountryCode;
  }

  /**
   * Set ISO 3166-1 alpha-2 country code (BT-40 / BT-55 / BT-69 / BT-80).
   *
   * @param sCountryCode
   *        Two-letter country code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxPostalAddress setCountryCode (@Nullable final String sCountryCode)
  {
    m_sCountryCode = sCountryCode;
    return this;
  }

  /** @return {@code true} if every field is {@code null}. */
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
