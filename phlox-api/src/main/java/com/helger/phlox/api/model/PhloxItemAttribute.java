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
 * BG-32 ITEM ATTRIBUTES (0..n on an item). Carries BT-160 (item attribute name) and BT-161
 * (item attribute value).
 *
 * @author Philip Helger
 */
public class PhloxItemAttribute
{
  private String m_sName;
  private String m_sValue;

  /** Create an empty item attribute. */
  public PhloxItemAttribute ()
  {}

  /**
   * Create an item attribute with name and value set.
   *
   * @param sName
   *        Attribute name (BT-160); may be {@code null}.
   * @param sValue
   *        Attribute value (BT-161); may be {@code null}.
   */
  public PhloxItemAttribute (@Nullable final String sName, @Nullable final String sValue)
  {
    m_sName = sName;
    m_sValue = sValue;
  }

  /** @return Item attribute name (BT-160). May be {@code null}. */
  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  /**
   * Set the item attribute name (BT-160).
   *
   * @param sName
   *        Attribute name; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxItemAttribute setName (@Nullable final String sName)
  {
    m_sName = sName;
    return this;
  }

  /** @return Item attribute value (BT-161). May be {@code null}. */
  @Nullable
  public String getValue ()
  {
    return m_sValue;
  }

  /**
   * Set the item attribute value (BT-161).
   *
   * @param sValue
   *        Attribute value; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxItemAttribute setValue (@Nullable final String sValue)
  {
    m_sValue = sValue;
    return this;
  }
}
