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
 * BG-24 ADDITIONAL SUPPORTING DOCUMENT. BT-122..BT-125 (with BT-125-1, BT-125-2 carried by
 * the {@link NormaAttachment}).
 *
 * @author Philip Helger
 */
public class NormaSupportingDocument
{
  private String m_sReference;
  private String m_sDescription;
  private String m_sExternalLocation;
  private NormaAttachment m_aAttachment;

  public NormaSupportingDocument ()
  {}

  @Nullable
  public String getReference ()
  {
    return m_sReference;
  }

  @NonNull
  public NormaSupportingDocument setReference (@Nullable final String sReference)
  {
    m_sReference = sReference;
    return this;
  }

  @Nullable
  public String getDescription ()
  {
    return m_sDescription;
  }

  @NonNull
  public NormaSupportingDocument setDescription (@Nullable final String sDescription)
  {
    m_sDescription = sDescription;
    return this;
  }

  @Nullable
  public String getExternalLocation ()
  {
    return m_sExternalLocation;
  }

  @NonNull
  public NormaSupportingDocument setExternalLocation (@Nullable final String sExternalLocation)
  {
    m_sExternalLocation = sExternalLocation;
    return this;
  }

  @Nullable
  public NormaAttachment getAttachment ()
  {
    return m_aAttachment;
  }

  @NonNull
  public NormaSupportingDocument setAttachment (@Nullable final NormaAttachment aAttachment)
  {
    m_aAttachment = aAttachment;
    return this;
  }
}
