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
 * Binary attachment. Used for BT-125 attached document with BT-125-1 mime code and BT-125-2
 * filename. Also reused by {@code RenderContext.setLogo(...)}.
 *
 * @author Philip Helger
 */
public class NormaAttachment
{
  private byte [] m_aBinary;
  private String m_sMimeCode;
  private String m_sFilename;

  public NormaAttachment ()
  {}

  @Nullable
  public byte [] getBinary ()
  {
    return m_aBinary;
  }

  @NonNull
  public NormaAttachment setBinary (@Nullable final byte [] aBinary)
  {
    m_aBinary = aBinary;
    return this;
  }

  @Nullable
  public String getMimeCode ()
  {
    return m_sMimeCode;
  }

  @NonNull
  public NormaAttachment setMimeCode (@Nullable final String sMimeCode)
  {
    m_sMimeCode = sMimeCode;
    return this;
  }

  @Nullable
  public String getFilename ()
  {
    return m_sFilename;
  }

  @NonNull
  public NormaAttachment setFilename (@Nullable final String sFilename)
  {
    m_sFilename = sFilename;
    return this;
  }
}
