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
 * BG-24 ADDITIONAL SUPPORTING DOCUMENT (0..n). Carries BT-122 (supporting document reference),
 * BT-123 (document description), BT-124 (external document location URL) and BT-125 (attached
 * binary document — with BT-125-1 mime code and BT-125-2 filename carried by
 * {@link NormaAttachment}).
 *
 * @author Philip Helger
 */
public class NormaSupportingDocument
{
  private String m_sReference;
  private String m_sDescription;
  private String m_sExternalLocation;
  private NormaAttachment m_aAttachment;

  /** Create an empty supporting document. */
  public NormaSupportingDocument ()
  {}

  /** @return Supporting document reference (BT-122). May be {@code null}. */
  @Nullable
  public String getReference ()
  {
    return m_sReference;
  }

  /**
   * Set the supporting document reference (BT-122).
   *
   * @param sReference
   *        Reference; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSupportingDocument setReference (@Nullable final String sReference)
  {
    m_sReference = sReference;
    return this;
  }

  /** @return Supporting document description (BT-123). May be {@code null}. */
  @Nullable
  public String getDescription ()
  {
    return m_sDescription;
  }

  /**
   * Set the supporting document description (BT-123).
   *
   * @param sDescription
   *        Description; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSupportingDocument setDescription (@Nullable final String sDescription)
  {
    m_sDescription = sDescription;
    return this;
  }

  /** @return External document location URL (BT-124). May be {@code null}. */
  @Nullable
  public String getExternalLocation ()
  {
    return m_sExternalLocation;
  }

  /**
   * Set the external document location URL (BT-124).
   *
   * @param sExternalLocation
   *        URL; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSupportingDocument setExternalLocation (@Nullable final String sExternalLocation)
  {
    m_sExternalLocation = sExternalLocation;
    return this;
  }

  /**
   * @return The attached binary document (BT-125 with BT-125-1 mime and BT-125-2 filename).
   *         May be {@code null}.
   */
  @Nullable
  public NormaAttachment getAttachment ()
  {
    return m_aAttachment;
  }

  /**
   * Set the attached binary document (BT-125 / BT-125-1 / BT-125-2).
   *
   * @param aAttachment
   *        Attachment; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaSupportingDocument setAttachment (@Nullable final NormaAttachment aAttachment)
  {
    m_aAttachment = aAttachment;
    return this;
  }
}
