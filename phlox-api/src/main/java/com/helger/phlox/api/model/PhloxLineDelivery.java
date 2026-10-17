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
 * BG-37 INVOICE LINE DELIVERY INFORMATION (0..1 on a line) plus nested BG-38 (line deliver-to
 * address). Line-level equivalent of BG-13: deliver-to party name (BT-185), deliver-to location
 * identifier with scheme (BT-186 / BT-186-1), actual delivery date (BT-187) and the deliver-to
 * address (BT-203 to BT-209, see {@link PhloxPostalAddress}). Introduced by EN 16931:2026. The
 * line period (BG-26) is nested under BG-37 in the 2026 data model but stays on
 * {@link PhloxInvoiceLine#getPeriod()} here.
 *
 * @author Philip Helger
 */
public class PhloxLineDelivery
{
  private String m_sPartyName;
  private PhloxIdentifier m_aLocationIdentifier;
  private LocalDate m_aActualDeliveryDate;
  private PhloxPostalAddress m_aPostalAddress;

  /** Create an empty instance. */
  public PhloxLineDelivery ()
  {}

  /**
   * Get the invoice line deliver to party name (BT-185).
   *
   * @return Invoice line deliver to party name (BT-185). May be {@code null}.
   */
  @Nullable
  public String getPartyName ()
  {
    return m_sPartyName;
  }

  /**
   * Set the invoice line deliver to party name (BT-185).
   *
   * @param sPartyName
   *        Invoice line deliver to party name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineDelivery setPartyName (@Nullable final String sPartyName)
  {
    m_sPartyName = sPartyName;
    return this;
  }

  /**
   * Get the invoice line deliver to location identifier (BT-186 / BT-186-1).
   *
   * @return Invoice line deliver to location identifier (BT-186 / BT-186-1). May be {@code null}.
   */
  @Nullable
  public PhloxIdentifier getLocationIdentifier ()
  {
    return m_aLocationIdentifier;
  }

  /**
   * Set the invoice line deliver to location identifier (BT-186 / BT-186-1).
   *
   * @param aLocationIdentifier
   *        Invoice line deliver to location identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineDelivery setLocationIdentifier (@Nullable final PhloxIdentifier aLocationIdentifier)
  {
    m_aLocationIdentifier = aLocationIdentifier;
    return this;
  }

  /**
   * Get the invoice line actual delivery date (BT-187).
   *
   * @return Invoice line actual delivery date (BT-187). May be {@code null}.
   */
  @Nullable
  public LocalDate getActualDeliveryDate ()
  {
    return m_aActualDeliveryDate;
  }

  /**
   * Set the invoice line actual delivery date (BT-187).
   *
   * @param aActualDeliveryDate
   *        Invoice line actual delivery date; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineDelivery setActualDeliveryDate (@Nullable final LocalDate aActualDeliveryDate)
  {
    m_aActualDeliveryDate = aActualDeliveryDate;
    return this;
  }

  /**
   * Get the invoice line deliver to address (BG-38).
   *
   * @return Invoice line deliver to address (BG-38). May be {@code null}.
   */
  @Nullable
  public PhloxPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  /**
   * Set the invoice line deliver to address (BG-38).
   *
   * @param aPostalAddress
   *        Invoice line deliver to address; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxLineDelivery setPostalAddress (@Nullable final PhloxPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }
}
