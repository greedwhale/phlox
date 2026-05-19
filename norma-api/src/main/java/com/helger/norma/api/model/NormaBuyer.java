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
 * BG-7 BUYER plus nested BG-8 (postal address) and BG-9 (contact). Carries BT-44 (name),
 * BT-45 (trading name), BT-46/BT-46-1 (identifiers), BT-47/BT-47-1 (legal registration),
 * BT-48 (VAT identifier) and BT-49/BT-49-1 (electronic address).
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

  /** Create an empty buyer. */
  public NormaBuyer ()
  {}

  /** @return Buyer name (BT-44). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the buyer name (BT-44).
   *
   * @param sName
   *        Name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Buyer trading name (BT-45). May be {@code null}. */
  @Nullable
  public String getTradingName ()
  {
    return m_sTradingName;
  }

  /**
   * Set the buyer trading name (BT-45).
   *
   * @param sTradingName
   *        Trading name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setTradingName (@Nullable final String sTradingName)
  {
    m_sTradingName = sTradingName;
    return this;
  }

  /**
   * @return The mutable list of buyer identifiers (BT-46 with BT-46-1 scheme IDs). Never
   *         {@code null}; empty when none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaIdentifier> identifiers ()
  {
    return m_aIdentifiers;
  }

  /** @return Buyer legal registration identifier (BT-47 with BT-47-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getLegalRegistration ()
  {
    return m_aLegalRegistration;
  }

  /**
   * Set the buyer legal registration identifier (BT-47 / BT-47-1).
   *
   * @param aLegalRegistration
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setLegalRegistration (@Nullable final NormaIdentifier aLegalRegistration)
  {
    m_aLegalRegistration = aLegalRegistration;
    return this;
  }

  /** @return Buyer VAT identifier (BT-48). May be {@code null}. */
  @Nullable
  public String getVATIdentifier ()
  {
    return m_sVATIdentifier;
  }

  /**
   * Set the buyer VAT identifier (BT-48).
   *
   * @param sVATIdentifier
   *        VAT ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setVATIdentifier (@Nullable final String sVATIdentifier)
  {
    m_sVATIdentifier = sVATIdentifier;
    return this;
  }

  /** @return Buyer electronic address (BT-49 with BT-49-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getElectronicAddress ()
  {
    return m_aElectronicAddress;
  }

  /**
   * Set the buyer electronic address (BT-49 / BT-49-1).
   *
   * @param aElectronicAddress
   *        Electronic address identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setElectronicAddress (@Nullable final NormaIdentifier aElectronicAddress)
  {
    m_aElectronicAddress = aElectronicAddress;
    return this;
  }

  /** @return Buyer postal address (BG-8). May be {@code null}. */
  @Nullable
  public NormaPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  /**
   * Set the buyer postal address (BG-8).
   *
   * @param aPostalAddress
   *        Address; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }

  /** @return Buyer contact (BG-9). May be {@code null}. */
  @Nullable
  public NormaContact getContact ()
  {
    return m_aContact;
  }

  /**
   * Set the buyer contact (BG-9).
   *
   * @param aContact
   *        Contact; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaBuyer setContact (@Nullable final NormaContact aContact)
  {
    m_aContact = aContact;
    return this;
  }
}
