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
 * BG-31 ITEM INFORMATION plus nested BG-32 attributes. BT-153..BT-159 plus the attributes list.
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

  public NormaItem ()
  {}

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaItem setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  @Nullable
  public String getDescription ()
  {
    return m_sDescription;
  }

  @NonNull
  public NormaItem setDescription (@Nullable final String sDescription)
  {
    m_sDescription = sDescription;
    return this;
  }

  @Nullable
  public String getSellerItemID ()
  {
    return m_sSellerItemID;
  }

  @NonNull
  public NormaItem setSellerItemID (@Nullable final String sSellerItemID)
  {
    m_sSellerItemID = sSellerItemID;
    return this;
  }

  @Nullable
  public String getBuyerItemID ()
  {
    return m_sBuyerItemID;
  }

  @NonNull
  public NormaItem setBuyerItemID (@Nullable final String sBuyerItemID)
  {
    m_sBuyerItemID = sBuyerItemID;
    return this;
  }

  @Nullable
  public NormaIdentifier getStandardItemID ()
  {
    return m_aStandardItemID;
  }

  @NonNull
  public NormaItem setStandardItemID (@Nullable final NormaIdentifier aStandardItemID)
  {
    m_aStandardItemID = aStandardItemID;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaItemClassification> classifications ()
  {
    return m_aClassifications;
  }

  @Nullable
  public String getCountryOfOrigin ()
  {
    return m_sCountryOfOrigin;
  }

  @NonNull
  public NormaItem setCountryOfOrigin (@Nullable final String sCountryOfOrigin)
  {
    m_sCountryOfOrigin = sCountryOfOrigin;
    return this;
  }

  @NonNull
  @ReturnsMutableObject
  public ICommonsList <NormaItemAttribute> attributes ()
  {
    return m_aAttributes;
  }
}
