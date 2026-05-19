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

import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-13 DELIVERY INFORMATION plus nested BG-15 deliver-to address.
 *
 * @author Philip Helger
 */
public class NormaDeliveryInformation
{
  private String m_sPartyName;
  private NormaIdentifier m_aLocationIdentifier;
  private LocalDate m_aActualDeliveryDate;
  private NormaPostalAddress m_aPostalAddress;

  public NormaDeliveryInformation ()
  {}

  @Nullable
  public String getPartyName ()
  {
    return m_sPartyName;
  }

  @NonNull
  public NormaDeliveryInformation setPartyName (@Nullable final String sPartyName)
  {
    m_sPartyName = sPartyName;
    return this;
  }

  @Nullable
  public NormaIdentifier getLocationIdentifier ()
  {
    return m_aLocationIdentifier;
  }

  @NonNull
  public NormaDeliveryInformation setLocationIdentifier (@Nullable final NormaIdentifier aLocationIdentifier)
  {
    m_aLocationIdentifier = aLocationIdentifier;
    return this;
  }

  @Nullable
  public LocalDate getActualDeliveryDate ()
  {
    return m_aActualDeliveryDate;
  }

  @NonNull
  public NormaDeliveryInformation setActualDeliveryDate (@Nullable final LocalDate aActualDeliveryDate)
  {
    m_aActualDeliveryDate = aActualDeliveryDate;
    return this;
  }

  @Nullable
  public NormaPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  @NonNull
  public NormaDeliveryInformation setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }
}
