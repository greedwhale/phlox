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
 * BG-7 BUYER plus nested BG-8 (postal address) and BG-9 (contact). Carries BT-44..BT-49.
 *
 * @author Philip Helger
 */
public class NormaBuyer
{
  private String m_sName;
  private String m_sTradingName;
  private final ICommonsList <NormaIdentifier> m_aIdentifiers = new CommonsArrayList <> ();
  private NormaIdentifier m_aLegalRegistration;
  private String m_sVATIdentifier;
  private NormaIdentifier m_aElectronicAddress;
  private NormaPostalAddress m_aPostalAddress;
  private NormaContact m_aContact;

  public NormaBuyer ()
  {}

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaBuyer setName (@Nullable final String sName)
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
  public NormaBuyer setTradingName (@Nullable final String sTradingName)
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
  public NormaBuyer setLegalRegistration (@Nullable final NormaIdentifier aLegalRegistration)
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
  public NormaBuyer setVATIdentifier (@Nullable final String sVATIdentifier)
  {
    m_sVATIdentifier = sVATIdentifier;
    return this;
  }

  @Nullable
  public NormaIdentifier getElectronicAddress ()
  {
    return m_aElectronicAddress;
  }

  @NonNull
  public NormaBuyer setElectronicAddress (@Nullable final NormaIdentifier aElectronicAddress)
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
  public NormaBuyer setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
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
  public NormaBuyer setContact (@Nullable final NormaContact aContact)
  {
    m_aContact = aContact;
    return this;
  }
}
