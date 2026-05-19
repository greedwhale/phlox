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
 * BG-17 CREDIT TRANSFER (0..n). Carries BT-84 (account identifier), BT-85 (account name) and
 * BT-86 (payment service provider ID, e.g. BIC).
 *
 * @author Philip Helger
 */
public class NormaCreditTransfer
{
  private String m_sAccountIdentifier;
  private String m_sAccountName;
  private String m_sServiceProviderID;

  /** Create an empty credit transfer. */
  public NormaCreditTransfer ()
  {}

  /** @return Payment account identifier (BT-84) — IBAN or proprietary ID. May be {@code null}. */
  @Nullable
  public String getAccountIdentifier ()
  {
    return m_sAccountIdentifier;
  }

  /**
   * Set the payment account identifier (BT-84).
   *
   * @param sAccountIdentifier
   *        Account ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaCreditTransfer setAccountIdentifier (@Nullable final String sAccountIdentifier)
  {
    m_sAccountIdentifier = sAccountIdentifier;
    return this;
  }

  /** @return Payment account name (BT-85). May be {@code null}. */
  @Nullable
  public String getAccountName ()
  {
    return m_sAccountName;
  }

  /**
   * Set the payment account name (BT-85).
   *
   * @param sAccountName
   *        Account name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaCreditTransfer setAccountName (@Nullable final String sAccountName)
  {
    m_sAccountName = sAccountName;
    return this;
  }

  /** @return Payment service provider ID (BT-86) — typically the BIC. May be {@code null}. */
  @Nullable
  public String getServiceProviderID ()
  {
    return m_sServiceProviderID;
  }

  /**
   * Set the payment service provider ID (BT-86).
   *
   * @param sServiceProviderID
   *        Service provider ID (BIC); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaCreditTransfer setServiceProviderID (@Nullable final String sServiceProviderID)
  {
    m_sServiceProviderID = sServiceProviderID;
    return this;
  }
}
