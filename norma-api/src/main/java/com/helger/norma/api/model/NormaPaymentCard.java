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
 * BG-18 PAYMENT CARD INFORMATION (0..1). Carries BT-87 (PAN — masked primary account number)
 * and BT-88 (card holder name).
 *
 * @author Philip Helger
 */
public class NormaPaymentCard
{
  private String m_sPAN;
  private String m_sHolderName;

  /** Create an empty payment card. */
  public NormaPaymentCard ()
  {}

  /** @return Payment card primary account number / PAN (BT-87) — typically masked. May be {@code null}. */
  @Nullable
  public String getPAN ()
  {
    return m_sPAN;
  }

  /**
   * Set the payment card PAN (BT-87).
   *
   * @param sPAN
   *        Primary account number (typically masked); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentCard setPAN (@Nullable final String sPAN)
  {
    m_sPAN = sPAN;
    return this;
  }

  /** @return Payment card holder name (BT-88). May be {@code null}. */
  @Nullable
  public String getHolderName ()
  {
    return m_sHolderName;
  }

  /**
   * Set the payment card holder name (BT-88).
   *
   * @param sHolderName
   *        Holder name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentCard setHolderName (@Nullable final String sHolderName)
  {
    m_sHolderName = sHolderName;
    return this;
  }
}
