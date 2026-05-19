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

import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * BG-16 PAYMENT INSTRUCTIONS (0..1) plus the nested BG-17 (credit transfers), BG-18 (payment
 * card) and BG-19 (direct debit) groups. Carries BT-81 (payment means type code), BT-82
 * (payment means text) and BT-83 (remittance information).
 *
 * @author Philip Helger
 */
public class NormaPaymentInstructions
{
  private String m_sPaymentMeansCode;
  private String m_sPaymentMeansText;
  private String m_sRemittanceInformation;
  private final ICommonsList <NormaCreditTransfer> m_aCreditTransfers = new CommonsArrayList <> ();
  private NormaPaymentCard m_aPaymentCard;
  private NormaDirectDebit m_aDirectDebit;

  /** Create an empty payment instructions record. */
  public NormaPaymentInstructions ()
  {}

  /** @return Payment means type code (BT-81) — UNTDID 4461. May be {@code null}. */
  @Nullable
  public String getPaymentMeansCode ()
  {
    return m_sPaymentMeansCode;
  }

  /**
   * Set the payment means type code (BT-81).
   *
   * @param sPaymentMeansCode
   *        UNTDID 4461 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentInstructions setPaymentMeansCode (@Nullable final String sPaymentMeansCode)
  {
    m_sPaymentMeansCode = sPaymentMeansCode;
    return this;
  }

  /** @return Payment means text (BT-82). May be {@code null}. */
  @Nullable
  public String getPaymentMeansText ()
  {
    return m_sPaymentMeansText;
  }

  /**
   * Set the payment means text (BT-82).
   *
   * @param sPaymentMeansText
   *        Free-form text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentInstructions setPaymentMeansText (@Nullable final String sPaymentMeansText)
  {
    m_sPaymentMeansText = sPaymentMeansText;
    return this;
  }

  /** @return Remittance information (BT-83). May be {@code null}. */
  @Nullable
  public String getRemittanceInformation ()
  {
    return m_sRemittanceInformation;
  }

  /**
   * Set the remittance information (BT-83).
   *
   * @param sRemittanceInformation
   *        Remittance text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentInstructions setRemittanceInformation (@Nullable final String sRemittanceInformation)
  {
    m_sRemittanceInformation = sRemittanceInformation;
    return this;
  }

  /**
   * @return Mutable list of credit transfers (BG-17). Never {@code null}; empty when none are
   *         present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaCreditTransfer> creditTransfers ()
  {
    return m_aCreditTransfers;
  }

  /** @return Payment card information (BG-18). May be {@code null}. */
  @Nullable
  public NormaPaymentCard getPaymentCard ()
  {
    return m_aPaymentCard;
  }

  /**
   * Set the payment card information (BG-18).
   *
   * @param aPaymentCard
   *        Card details; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentInstructions setPaymentCard (@Nullable final NormaPaymentCard aPaymentCard)
  {
    m_aPaymentCard = aPaymentCard;
    return this;
  }

  /** @return Direct debit information (BG-19). May be {@code null}. */
  @Nullable
  public NormaDirectDebit getDirectDebit ()
  {
    return m_aDirectDebit;
  }

  /**
   * Set the direct debit information (BG-19).
   *
   * @param aDirectDebit
   *        Direct debit details; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaPaymentInstructions setDirectDebit (@Nullable final NormaDirectDebit aDirectDebit)
  {
    m_aDirectDebit = aDirectDebit;
    return this;
  }
}
