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
 * BG-16 PAYMENT INSTRUCTIONS plus nested BG-17 credit transfers, BG-18 card, BG-19 direct
 * debit. BT-81..BT-83 plus the nested groups.
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

  public NormaPaymentInstructions ()
  {}

  @Nullable
  public String getPaymentMeansCode ()
  {
    return m_sPaymentMeansCode;
  }

  @NonNull
  public NormaPaymentInstructions setPaymentMeansCode (@Nullable final String sPaymentMeansCode)
  {
    m_sPaymentMeansCode = sPaymentMeansCode;
    return this;
  }

  @Nullable
  public String getPaymentMeansText ()
  {
    return m_sPaymentMeansText;
  }

  @NonNull
  public NormaPaymentInstructions setPaymentMeansText (@Nullable final String sPaymentMeansText)
  {
    m_sPaymentMeansText = sPaymentMeansText;
    return this;
  }

  @Nullable
  public String getRemittanceInformation ()
  {
    return m_sRemittanceInformation;
  }

  @NonNull
  public NormaPaymentInstructions setRemittanceInformation (@Nullable final String sRemittanceInformation)
  {
    m_sRemittanceInformation = sRemittanceInformation;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaCreditTransfer> creditTransfers ()
  {
    return m_aCreditTransfers;
  }

  @Nullable
  public NormaPaymentCard getPaymentCard ()
  {
    return m_aPaymentCard;
  }

  @NonNull
  public NormaPaymentInstructions setPaymentCard (@Nullable final NormaPaymentCard aPaymentCard)
  {
    m_aPaymentCard = aPaymentCard;
    return this;
  }

  @Nullable
  public NormaDirectDebit getDirectDebit ()
  {
    return m_aDirectDebit;
  }

  @NonNull
  public NormaPaymentInstructions setDirectDebit (@Nullable final NormaDirectDebit aDirectDebit)
  {
    m_aDirectDebit = aDirectDebit;
    return this;
  }
}
