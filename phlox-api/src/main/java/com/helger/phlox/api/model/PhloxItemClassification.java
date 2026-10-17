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
 * Item classification (0..n inside BG-31 ITEM INFORMATION). Carries BT-158 (item classification
 * identifier value), BT-158-1 (classification scheme list ID) and BT-158-2 (classification
 * scheme version ID).
 *
 * @author Philip Helger
 */
public class PhloxItemClassification
{
  private String m_sCode;
  private String m_sListID;
  private String m_sListVersionID;

  /** Create an empty classification. */
  public PhloxItemClassification ()
  {}

  /**
   * Get the classification code value (BT-158).
   *
   * @return Classification code value (BT-158). May be {@code null}.
   */
  @Nullable
  public String getCode ()
  {
    return m_sCode;
  }

  /**
   * Set the classification code value (BT-158).
   *
   * @param sCode
   *        Code value; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxItemClassification setCode (@Nullable final String sCode)
  {
    m_sCode = sCode;
    return this;
  }

  /**
   * Get the classification scheme list ID (BT-158-1).
   *
   * @return Classification scheme list ID (BT-158-1). May be {@code null}.
   */
  @Nullable
  public String getListID ()
  {
    return m_sListID;
  }

  /**
   * Set the classification scheme list ID (BT-158-1).
   *
   * @param sListID
   *        List ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxItemClassification setListID (@Nullable final String sListID)
  {
    m_sListID = sListID;
    return this;
  }

  /**
   * Get the classification scheme version ID (BT-158-2).
   *
   * @return Classification scheme version ID (BT-158-2). May be {@code null}.
   */
  @Nullable
  public String getListVersionID ()
  {
    return m_sListVersionID;
  }

  /**
   * Set the classification scheme version ID (BT-158-2).
   *
   * @param sListVersionID
   *        Version ID; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxItemClassification setListVersionID (@Nullable final String sListVersionID)
  {
    m_sListVersionID = sListVersionID;
    return this;
  }
}
