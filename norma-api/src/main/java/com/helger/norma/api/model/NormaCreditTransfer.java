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
 * BG-17 CREDIT TRANSFER. BT-84..BT-86.
 *
 * @author Philip Helger
 */
public class NormaCreditTransfer
{
  private String m_sAccountIdentifier;
  private String m_sAccountName;
  private String m_sServiceProviderID;

  public NormaCreditTransfer ()
  {}

  @Nullable
  public String getAccountIdentifier ()
  {
    return m_sAccountIdentifier;
  }

  @NonNull
  public NormaCreditTransfer setAccountIdentifier (@Nullable final String sAccountIdentifier)
  {
    m_sAccountIdentifier = sAccountIdentifier;
    return this;
  }

  @Nullable
  public String getAccountName ()
  {
    return m_sAccountName;
  }

  @NonNull
  public NormaCreditTransfer setAccountName (@Nullable final String sAccountName)
  {
    m_sAccountName = sAccountName;
    return this;
  }

  @Nullable
  public String getServiceProviderID ()
  {
    return m_sServiceProviderID;
  }

  @NonNull
  public NormaCreditTransfer setServiceProviderID (@Nullable final String sServiceProviderID)
  {
    m_sServiceProviderID = sServiceProviderID;
    return this;
  }
}
