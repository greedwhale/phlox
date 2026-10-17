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
 * BG-11 SELLER TAX REPRESENTATIVE PARTY plus nested BG-12 postal address. Carries BT-62
 * (tax representative name), BT-63 (tax representative VAT ID) and the BG-12 address fields
 * (BT-64..BT-69).
 *
 * @author Philip Helger
 */
public class PhloxSellerTaxRepresentative
{
  private String m_sName;
  private String m_sVatIdentifier;
  private PhloxPostalAddress m_aPostalAddress;

  /** Create an empty tax representative. */
  public PhloxSellerTaxRepresentative ()
  {}

  /** @return Tax representative name (BT-62). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the tax representative name (BT-62).
   *
   * @param sName
   *        Name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxSellerTaxRepresentative setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Tax representative VAT identifier (BT-63). May be {@code null}. */
  @Nullable
  public String getVatIdentifier ()
  {
    return m_sVatIdentifier;
  }

  /**
   * Set the tax representative VAT identifier (BT-63).
   *
   * @param sVatIdentifier
   *        VAT ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxSellerTaxRepresentative setVatIdentifier (@Nullable final String sVatIdentifier)
  {
    m_sVatIdentifier = sVatIdentifier;
    return this;
  }

  /** @return Tax representative postal address (BG-12). May be {@code null}. */
  @Nullable
  public PhloxPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  /**
   * Set the tax representative postal address (BG-12).
   *
   * @param aPostalAddress
   *        Address; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxSellerTaxRepresentative setPostalAddress (@Nullable final PhloxPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }
}
