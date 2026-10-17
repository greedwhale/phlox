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
 * BG-13 DELIVERY INFORMATION plus nested BG-15 (Deliver-to address). Carries BT-70 (deliver-to
 * party name), BT-71/BT-71-1 (deliver-to location identifier), BT-72 (actual delivery date) and
 * the BG-15 address fields (BT-75..BT-80, BT-165).
 *
 * @author Philip Helger
 */
public class PhloxDeliveryInformation
{
  private String m_sPartyName;
  private PhloxIdentifier m_aLocationIdentifier;
  private LocalDate m_aActualDeliveryDate;
  private PhloxPostalAddress m_aPostalAddress;

  /** Create an empty delivery information. */
  public PhloxDeliveryInformation ()
  {}

  /** @return Deliver-to party name (BT-70). May be {@code null}. */
  @Nullable
  public String getPartyName ()
  {
    return m_sPartyName;
  }

  /**
   * Set the deliver-to party name (BT-70).
   *
   * @param sPartyName
   *        Party name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDeliveryInformation setPartyName (@Nullable final String sPartyName)
  {
    m_sPartyName = sPartyName;
    return this;
  }

  /** @return Deliver-to location identifier (BT-71 with BT-71-1 scheme ID). May be {@code null}. */
  @Nullable
  public PhloxIdentifier getLocationIdentifier ()
  {
    return m_aLocationIdentifier;
  }

  /**
   * Set the deliver-to location identifier (BT-71 / BT-71-1).
   *
   * @param aLocationIdentifier
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDeliveryInformation setLocationIdentifier (@Nullable final PhloxIdentifier aLocationIdentifier)
  {
    m_aLocationIdentifier = aLocationIdentifier;
    return this;
  }

  /** @return Actual delivery date (BT-72). May be {@code null}. */
  @Nullable
  public LocalDate getActualDeliveryDate ()
  {
    return m_aActualDeliveryDate;
  }

  /**
   * Set the actual delivery date (BT-72).
   *
   * @param aActualDeliveryDate
   *        Delivery date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDeliveryInformation setActualDeliveryDate (@Nullable final LocalDate aActualDeliveryDate)
  {
    m_aActualDeliveryDate = aActualDeliveryDate;
    return this;
  }

  /** @return Deliver-to address (BG-15). May be {@code null}. */
  @Nullable
  public PhloxPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  /**
   * Set the deliver-to address (BG-15).
   *
   * @param aPostalAddress
   *        Address; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDeliveryInformation setPostalAddress (@Nullable final PhloxPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }
}
