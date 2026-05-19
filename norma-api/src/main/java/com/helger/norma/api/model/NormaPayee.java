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
 * BG-10 PAYEE (0..1). Carries BT-59 (payee name), BT-60/BT-60-1 (payee identifier with scheme)
 * and BT-61/BT-61-1 (payee legal registration with scheme).
 *
 * @author Philip Helger
 */
public class NormaPayee
{
  private String m_sName;
  private NormaIdentifier m_aIdentifier;
  private NormaIdentifier m_aLegalRegistration;

  /** Create an empty payee. */
  public NormaPayee ()
  {}

  /** @return Payee name (BT-59). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the payee name (BT-59).
   *
   * @param sName
   *        Name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPayee setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Payee identifier (BT-60 with BT-60-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getIdentifier ()
  {
    return m_aIdentifier;
  }

  /**
   * Set the payee identifier (BT-60 / BT-60-1).
   *
   * @param aIdentifier
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPayee setIdentifier (@Nullable final NormaIdentifier aIdentifier)
  {
    m_aIdentifier = aIdentifier;
    return this;
  }

  /** @return Payee legal registration (BT-61 with BT-61-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getLegalRegistration ()
  {
    return m_aLegalRegistration;
  }

  /**
   * Set the payee legal registration (BT-61 / BT-61-1).
   *
   * @param aLegalRegistration
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPayee setLegalRegistration (@Nullable final NormaIdentifier aLegalRegistration)
  {
    m_aLegalRegistration = aLegalRegistration;
    return this;
  }
}
