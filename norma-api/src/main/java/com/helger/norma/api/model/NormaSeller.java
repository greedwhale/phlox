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
 * BG-4 SELLER plus nested BG-5 (postal address) and BG-6 (contact). Carries BT-27..BT-34.
 *
 * @author Philip Helger
 */
public class NormaSeller
{
  private String m_sName;
  private String m_sTradingName;
  private final ICommonsList <NormaIdentifier> m_aIdentifiers = new CommonsArrayList <> ();
  private NormaIdentifier m_aLegalRegistration;
  private String m_sVATIdentifier;
  private String m_sTaxRegistration;
  private String m_sAdditionalLegalInfo;
  private NormaIdentifier m_aElectronicAddress;
  private NormaPostalAddress m_aPostalAddress;
  private NormaContact m_aContact;

  public NormaSeller ()
  {}

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaSeller setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  @Nullable
  public String getTradingName ()
  {
    return m_sTradingName;
  }

  @NonNull
  public NormaSeller setTradingName (@Nullable final String sTradingName)
  {
    m_sTradingName = sTradingName;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaIdentifier> identifiers ()
  {
    return m_aIdentifiers;
  }

  @Nullable
  public NormaIdentifier getLegalRegistration ()
  {
    return m_aLegalRegistration;
  }

  @NonNull
  public NormaSeller setLegalRegistration (@Nullable final NormaIdentifier aLegalRegistration)
  {
    m_aLegalRegistration = aLegalRegistration;
    return this;
  }

  @Nullable
  public String getVATIdentifier ()
  {
    return m_sVATIdentifier;
  }

  @NonNull
  public NormaSeller setVATIdentifier (@Nullable final String sVATIdentifier)
  {
    m_sVATIdentifier = sVATIdentifier;
    return this;
  }

  @Nullable
  public String getTaxRegistration ()
  {
    return m_sTaxRegistration;
  }

  @NonNull
  public NormaSeller setTaxRegistration (@Nullable final String sTaxRegistration)
  {
    m_sTaxRegistration = sTaxRegistration;
    return this;
  }

  @Nullable
  public String getAdditionalLegalInfo ()
  {
    return m_sAdditionalLegalInfo;
  }

  @NonNull
  public NormaSeller setAdditionalLegalInfo (@Nullable final String sAdditionalLegalInfo)
  {
    m_sAdditionalLegalInfo = sAdditionalLegalInfo;
    return this;
  }

  @Nullable
  public NormaIdentifier getElectronicAddress ()
  {
    return m_aElectronicAddress;
  }

  @NonNull
  public NormaSeller setElectronicAddress (@Nullable final NormaIdentifier aElectronicAddress)
  {
    m_aElectronicAddress = aElectronicAddress;
    return this;
  }

  @Nullable
  public NormaPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  @NonNull
  public NormaSeller setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }

  @Nullable
  public NormaContact getContact ()
  {
    return m_aContact;
  }

  @NonNull
  public NormaSeller setContact (@Nullable final NormaContact aContact)
  {
    m_aContact = aContact;
    return this;
  }
}
