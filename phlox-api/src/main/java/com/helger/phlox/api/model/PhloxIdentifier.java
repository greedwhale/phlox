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
 * An identifier with an optional scheme. Used for BT-X / BT-X-1 pairs throughout the EN 16931
 * model (e.g. BT-29 seller identifier with BT-29-1 scheme ID, BT-46 buyer identifier with
 * BT-46-1, BT-60 / BT-60-1 payee identifier, BT-71 / BT-71-1 deliver-to location, etc.).
 *
 * @author Philip Helger
 */
public class PhloxIdentifier
{
  private String m_sValue;
  private String m_sSchemeID;

  /** Create an empty identifier. */
  public PhloxIdentifier ()
  {}

  /**
   * Create an identifier with the given value and scheme ID.
   *
   * @param sValue
   *        The identifier value (the BT-X value). May be {@code null}.
   * @param sSchemeID
   *        The scheme ID (the BT-X-1 value). May be {@code null}.
   */
  public PhloxIdentifier (@Nullable final String sValue, @Nullable final String sSchemeID)
  {
    m_sValue = sValue;
    m_sSchemeID = sSchemeID;
  }

  /** @return The identifier value (BT-X). May be {@code null}. */
  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  /**
   * Set the identifier value (BT-X).
   *
   * @param sValue
   *        The new value; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxIdentifier setValue (@Nullable final String sValue)
  {
    m_sValue = sValue;
    return this;
  }

  /** @return The scheme ID (BT-X-1) qualifying the identifier value. May be {@code null}. */
  @Nullable
  public String getSchemeID ()
  {
    return m_sSchemeID;
  }

  /**
   * Set the scheme ID (BT-X-1) qualifying the identifier value.
   *
   * @param sSchemeID
   *        The new scheme ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxIdentifier setSchemeID (@Nullable final String sSchemeID)
  {
    m_sSchemeID = sSchemeID;
    return this;
  }
}
