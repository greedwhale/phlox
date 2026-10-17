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
 * BG-19 DIRECT DEBIT (0..1). Carries BT-89 (mandate reference ID), BT-90 (bank-assigned
 * creditor identifier) and BT-91 (debited account identifier).
 *
 * @author Philip Helger
 */
public class PhloxDirectDebit
{
  private String m_sMandateReferenceID;
  private String m_sBankAssignedCreditorID;
  private String m_sDebitedAccountID;

  /** Create an empty direct-debit record. */
  public PhloxDirectDebit ()
  {}

  /** @return Mandate reference ID (BT-89). May be {@code null}. */
  @Nullable
  public String getMandateReferenceID ()
  {
    return m_sMandateReferenceID;
  }

  /**
   * Set the mandate reference ID (BT-89).
   *
   * @param sMandateReferenceID
   *        Mandate reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDirectDebit setMandateReferenceID (@Nullable final String sMandateReferenceID)
  {
    m_sMandateReferenceID = sMandateReferenceID;
    return this;
  }

  /** @return Bank assigned creditor identifier (BT-90). May be {@code null}. */
  @Nullable
  public String getBankAssignedCreditorID ()
  {
    return m_sBankAssignedCreditorID;
  }

  /**
   * Set the bank assigned creditor ID (BT-90).
   *
   * @param sBankAssignedCreditorID
   *        Creditor identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDirectDebit setBankAssignedCreditorID (@Nullable final String sBankAssignedCreditorID)
  {
    m_sBankAssignedCreditorID = sBankAssignedCreditorID;
    return this;
  }

  /** @return Debited account identifier (BT-91) — typically the debtor's IBAN. May be {@code null}. */
  @Nullable
  public String getDebitedAccountID ()
  {
    return m_sDebitedAccountID;
  }

  /**
   * Set the debited account identifier (BT-91).
   *
   * @param sDebitedAccountID
   *        Account ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDirectDebit setDebitedAccountID (@Nullable final String sDebitedAccountID)
  {
    m_sDebitedAccountID = sDebitedAccountID;
    return this;
  }
}
