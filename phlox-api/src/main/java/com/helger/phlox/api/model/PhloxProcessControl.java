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
package com.helger.phlox.api.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-2 PROCESS CONTROL (1..1). Carries the business process type (BT-23) and the EN 16931
 * specification identifier (BT-24).
 *
 * @author Philip Helger
 */
public class PhloxProcessControl
{
  private String m_sBusinessProcessType;
  private String m_sSpecificationIdentifier;

  /** Create an empty process control. */
  public PhloxProcessControl ()
  {}

  /**
   * Get the business process type (BT-23).
   *
   * @return Business process type (BT-23). May be {@code null}.
   */
  @Nullable
  public String getBusinessProcessType ()
  {
    return m_sBusinessProcessType;
  }

  /**
   * Set the business process type (BT-23).
   *
   * @param sBusinessProcessType
   *        Profile / process identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxProcessControl setBusinessProcessType (@Nullable final String sBusinessProcessType)
  {
    m_sBusinessProcessType = sBusinessProcessType;
    return this;
  }

  /**
   * Get the specification identifier (BT-24) — the EN 16931 customisation URN. May be {@code null}.
   *
   * @return Specification identifier (BT-24) — the EN 16931 customisation URN. May be {@code null}.
   */
  @Nullable
  public String getSpecificationIdentifier ()
  {
    return m_sSpecificationIdentifier;
  }

  /**
   * Set the specification identifier (BT-24) — the EN 16931 customisation URN.
   *
   * @param sSpecificationIdentifier
   *        URN; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxProcessControl setSpecificationIdentifier (@Nullable final String sSpecificationIdentifier)
  {
    m_sSpecificationIdentifier = sSpecificationIdentifier;
    return this;
  }
}
