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
 * Item classification: BT-158 code, BT-158-1 scheme list ID, BT-158-2 scheme version ID.
 *
 * @author Philip Helger
 */
public class NormaItemClassification
{
  private String m_sCode;
  private String m_sListID;
  private String m_sListVersionID;

  public NormaItemClassification ()
  {}

  @Nullable
  public String getCode ()
  {
    return m_sCode;
  }

  @NonNull
  public NormaItemClassification setCode (@Nullable final String sCode)
  {
    m_sCode = sCode;
    return this;
  }

  @Nullable
  public String getListID ()
  {
    return m_sListID;
  }

  @NonNull
  public NormaItemClassification setListID (@Nullable final String sListID)
  {
    m_sListID = sListID;
    return this;
  }

  @Nullable
  public String getListVersionID ()
  {
    return m_sListVersionID;
  }

  @NonNull
  public NormaItemClassification setListVersionID (@Nullable final String sListVersionID)
  {
    m_sListVersionID = sListVersionID;
    return this;
  }
}
