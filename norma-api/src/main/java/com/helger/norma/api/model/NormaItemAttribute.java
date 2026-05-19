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
 * BG-32 ITEM ATTRIBUTES. BT-160 name, BT-161 value.
 *
 * @author Philip Helger
 */
public class NormaItemAttribute
{
  private String m_sName;
  private String m_sValue;

  public NormaItemAttribute ()
  {}

  public NormaItemAttribute (@Nullable final String sName, @Nullable final String sValue)
  {
    m_sName = sName;
    m_sValue = sValue;
  }

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @NonNull
  public NormaItemAttribute setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  @NonNull
  public NormaItemAttribute setValue (@Nullable final String sValue)
  {
    m_sValue = sValue;
    return this;
  }
}
