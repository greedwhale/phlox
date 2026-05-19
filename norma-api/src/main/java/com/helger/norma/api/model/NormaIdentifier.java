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
 * An identifier with an optional scheme. Used for BT-X / BT-X-1 pairs throughout the EN 16931
 * model (e.g. BT-29 seller identifier with BT-29-1 scheme ID).
 *
 * @author Philip Helger
 */
public class NormaIdentifier
{
  private String m_sValue;
  private String m_sSchemeID;

  public NormaIdentifier ()
  {}

  public NormaIdentifier (@Nullable final String sValue, @Nullable final String sSchemeID)
  {
    m_sValue = sValue;
    m_sSchemeID = sSchemeID;
  }

  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  @NonNull
  public NormaIdentifier setValue (@Nullable final String sValue)
  {
    m_sValue = sValue;
    return this;
  }

  @Nullable
  public String getSchemeID ()
  {
    return m_sSchemeID;
  }

  @NonNull
  public NormaIdentifier setSchemeID (@Nullable final String sSchemeID)
  {
    m_sSchemeID = sSchemeID;
    return this;
  }
}
