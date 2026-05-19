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
 * BG-2 PROCESS CONTROL. BT-23 business process type, BT-24 specification identifier.
 *
 * @author Philip Helger
 */
public class NormaProcessControl
{
  private String m_sBusinessProcessType;
  private String m_sSpecificationIdentifier;

  public NormaProcessControl ()
  {}

  @Nullable
  public String getBusinessProcessType ()
  {
    return m_sBusinessProcessType;
  }

  @NonNull
  public NormaProcessControl setBusinessProcessType (@Nullable final String sBusinessProcessType)
  {
    m_sBusinessProcessType = sBusinessProcessType;
    return this;
  }

  @Nullable
  public String getSpecificationIdentifier ()
  {
    return m_sSpecificationIdentifier;
  }

  @NonNull
  public NormaProcessControl setSpecificationIdentifier (@Nullable final String sSpecificationIdentifier)
  {
    m_sSpecificationIdentifier = sSpecificationIdentifier;
    return this;
  }
}
