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
 * BG-19 DIRECT DEBIT. BT-89 mandate reference, BT-90 creditor ID, BT-91 debited account.
 *
 * @author Philip Helger
 */
public class NormaDirectDebit
{
  private String m_sMandateReferenceID;
  private String m_sBankAssignedCreditorID;
  private String m_sDebitedAccountID;

  public NormaDirectDebit ()
  {}

  @Nullable
  public String getMandateReferenceID ()
  {
    return m_sMandateReferenceID;
  }

  @NonNull
  public NormaDirectDebit setMandateReferenceID (@Nullable final String sMandateReferenceID)
  {
    m_sMandateReferenceID = sMandateReferenceID;
    return this;
  }

  @Nullable
  public String getBankAssignedCreditorID ()
  {
    return m_sBankAssignedCreditorID;
  }

  @NonNull
  public NormaDirectDebit setBankAssignedCreditorID (@Nullable final String sBankAssignedCreditorID)
  {
    m_sBankAssignedCreditorID = sBankAssignedCreditorID;
    return this;
  }

  @Nullable
  public String getDebitedAccountID ()
  {
    return m_sDebitedAccountID;
  }

  @NonNull
  public NormaDirectDebit setDebitedAccountID (@Nullable final String sDebitedAccountID)
  {
    m_sDebitedAccountID = sDebitedAccountID;
    return this;
  }
}
