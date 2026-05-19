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
 * BG-31 ITEM INFORMATION (1..1 on a line) plus nested BG-32 (item attributes). Carries
 * BT-153 (item name), BT-154 (item description), BT-155 (seller's item identifier), BT-156
 * (buyer's item identifier), BT-157/BT-157-1 (standard item identifier and scheme),
 * BT-158/BT-158-1/BT-158-2 (classifications) and BT-159 (country of origin).
 *
 * @author Philip Helger
 */
public class NormaItem
{
  private String m_sName;
  private String m_sDescription;
  private String m_sSellerItemID;
  private String m_sBuyerItemID;
  private NormaIdentifier m_aStandardItemID;
  private final ICommonsList <NormaItemClassification> m_aClassifications = new CommonsArrayList <> ();
  private String m_sCountryOfOrigin;
  private final ICommonsList <NormaItemAttribute> m_aAttributes = new CommonsArrayList <> ();

  /** Create an empty item. */
  public NormaItem ()
  {}

  /** @return Item name (BT-153). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the item name (BT-153).
   *
   * @param sName
   *        Name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Item description (BT-154). May be {@code null}. */
  @Nullable
  public String getDescription ()
  {
    return m_sDescription;
  }

  /**
   * Set the item description (BT-154).
   *
   * @param sDescription
   *        Description; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setDescription (@Nullable final String sDescription)
  {
    m_sDescription = sDescription;
    return this;
  }

  /** @return Item seller's identifier (BT-155). May be {@code null}. */
  @Nullable
  public String getSellerItemID ()
  {
    return m_sSellerItemID;
  }

  /**
   * Set the item seller's identifier (BT-155).
   *
   * @param sSellerItemID
   *        Seller's item identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setSellerItemID (@Nullable final String sSellerItemID)
  {
    m_sSellerItemID = sSellerItemID;
    return this;
  }

  /** @return Item buyer's identifier (BT-156). May be {@code null}. */
  @Nullable
  public String getBuyerItemID ()
  {
    return m_sBuyerItemID;
  }

  /**
   * Set the item buyer's identifier (BT-156).
   *
   * @param sBuyerItemID
   *        Buyer's item identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setBuyerItemID (@Nullable final String sBuyerItemID)
  {
    m_sBuyerItemID = sBuyerItemID;
    return this;
  }

  /** @return Item standard identifier (BT-157 with BT-157-1 scheme ID). May be {@code null}. */
  @Nullable
  public NormaIdentifier getStandardItemID ()
  {
    return m_aStandardItemID;
  }

  /**
   * Set the item standard identifier (BT-157 / BT-157-1).
   *
   * @param aStandardItemID
   *        Identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setStandardItemID (@Nullable final NormaIdentifier aStandardItemID)
  {
    m_aStandardItemID = aStandardItemID;
    return this;
  }

  /**
   * @return Mutable list of item classifications (BT-158 / BT-158-1 / BT-158-2). Never
   *         {@code null}; empty when none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaItemClassification> classifications ()
  {
    return m_aClassifications;
  }

  /** @return Item country of origin (BT-159) — ISO 3166-1 alpha-2. May be {@code null}. */
  @Nullable
  public String getCountryOfOrigin ()
  {
    return m_sCountryOfOrigin;
  }

  /**
   * Set the item country of origin (BT-159).
   *
   * @param sCountryOfOrigin
   *        Country code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaItem setCountryOfOrigin (@Nullable final String sCountryOfOrigin)
  {
    m_sCountryOfOrigin = sCountryOfOrigin;
    return this;
  }

  /**
   * @return Mutable list of item attributes (BG-32 — BT-160 / BT-161). Never {@code null};
   *         empty when none are present.
   */
  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaItemAttribute> attributes ()
  {
    return m_aAttributes;
  }
}
