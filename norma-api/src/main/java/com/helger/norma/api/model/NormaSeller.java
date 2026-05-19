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
 * BG-4 SELLER plus nested BG-5 (postal address) and BG-6 (contact). Carries BT-27 (name),
 * BT-28 (trading name), BT-29/BT-29-1 (identifiers), BT-30/BT-30-1 (legal registration),
 * BT-31 (VAT identifier), BT-32 (tax registration), BT-33 (additional legal info) and
 * BT-34/BT-34-1 (electronic address).
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

  /** Create an empty seller. */
  public NormaSeller ()
  {}

  /** @return Seller name (BT-27). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the seller name (BT-27).
   *
   * @param sName
   *        Name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Seller trading name (BT-28). May be {@code null}. */
  @Nullable
  public String getTradingName ()
  {
    return m_sTradingName;
  }

  /**
   * Set the seller trading name (BT-28).
   *
   * @param sTradingName
   *        Trading name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setTradingName (@Nullable final String sTradingName)
  {
    m_sTradingName = sTradingName;
    return this;
  }

  /**
   * @return The mutable list of seller identifiers (BT-29 with BT-29-1 scheme IDs). Never
   *         {@code null}; empty when none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaIdentifier> identifiers ()
  {
    return m_aIdentifiers;
  }

  /** @return The legal registration identifier (BT-30 with BT-30-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getLegalRegistration ()
  {
    return m_aLegalRegistration;
  }

  /**
   * Set the legal registration identifier (BT-30 / BT-30-1).
   *
   * @param aLegalRegistration
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setLegalRegistration (@Nullable final NormaIdentifier aLegalRegistration)
  {
    m_aLegalRegistration = aLegalRegistration;
    return this;
  }

  /** @return Seller VAT identifier (BT-31). May be {@code null}. */
  @Nullable
  public String getVATIdentifier ()
  {
    return m_sVATIdentifier;
  }

  /**
   * Set the seller VAT identifier (BT-31).
   *
   * @param sVATIdentifier
   *        VAT ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setVATIdentifier (@Nullable final String sVATIdentifier)
  {
    m_sVATIdentifier = sVATIdentifier;
    return this;
  }

  /** @return Seller tax registration ID (BT-32). May be {@code null}. */
  @Nullable
  public String getTaxRegistration ()
  {
    return m_sTaxRegistration;
  }

  /**
   * Set the seller tax registration ID (BT-32).
   *
   * @param sTaxRegistration
   *        Tax registration ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setTaxRegistration (@Nullable final String sTaxRegistration)
  {
    m_sTaxRegistration = sTaxRegistration;
    return this;
  }

  /** @return Additional seller legal information (BT-33). May be {@code null}. */
  @Nullable
  public String getAdditionalLegalInfo ()
  {
    return m_sAdditionalLegalInfo;
  }

  /**
   * Set the additional seller legal information (BT-33).
   *
   * @param sAdditionalLegalInfo
   *        Free-form text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setAdditionalLegalInfo (@Nullable final String sAdditionalLegalInfo)
  {
    m_sAdditionalLegalInfo = sAdditionalLegalInfo;
    return this;
  }

  /** @return Seller electronic address (BT-34 with BT-34-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getElectronicAddress ()
  {
    return m_aElectronicAddress;
  }

  /**
   * Set the seller electronic address (BT-34 / BT-34-1).
   *
   * @param aElectronicAddress
   *        Electronic address identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setElectronicAddress (@Nullable final NormaIdentifier aElectronicAddress)
  {
    m_aElectronicAddress = aElectronicAddress;
    return this;
  }

  /** @return Seller postal address (BG-5). May be {@code null}. */
  @Nullable
  public NormaPostalAddress getPostalAddress ()
  {
    return m_aPostalAddress;
  }

  /**
   * Set the seller postal address (BG-5).
   *
   * @param aPostalAddress
   *        Address; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setPostalAddress (@Nullable final NormaPostalAddress aPostalAddress)
  {
    m_aPostalAddress = aPostalAddress;
    return this;
  }

  /** @return Seller contact (BG-6). May be {@code null}. */
  @Nullable
  public NormaContact getContact ()
  {
    return m_aContact;
  }

  /**
   * Set the seller contact (BG-6).
   *
   * @param aContact
   *        Contact; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSeller setContact (@Nullable final NormaContact aContact)
  {
    m_aContact = aContact;
    return this;
  }
}
