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
package com.helger.phlox.pdf;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.pdflayout.base.PLColor;

/**
 * The look of a template block or table row that was marked with {@code {{class:name}}}. Register
 * it under that name with {@link PdfRenderContext#putBlockStyle(String, PdfBlockStyle)}.
 *
 * @author Philip Helger
 */
public final class PdfBlockStyle
{
  private final PLColor m_aFillColor;
  private final PLColor m_aTextColor;

  /**
   * Create a new style.
   *
   * @param aFillColor
   *        The background colour. May be {@code null} for no background.
   * @param aTextColor
   *        The text colour. May be {@code null} to keep the colour of the font.
   */
  public PdfBlockStyle (@Nullable final PLColor aFillColor, @Nullable final PLColor aTextColor)
  {
    m_aFillColor = aFillColor;
    m_aTextColor = aTextColor;
  }

  /**
   * Get the background colour.
   *
   * @return The background colour or {@code null} if there is none.
   */
  @Nullable
  public PLColor getFillColor ()
  {
    return m_aFillColor;
  }

  /**
   * Get the text colour.
   *
   * @return The text colour or {@code null} if the colour of the font is kept.
   */
  @Nullable
  public PLColor getTextColor ()
  {
    return m_aTextColor;
  }

  /**
   * Create a style that only has a background colour.
   *
   * @param aFillColor
   *        The background colour. Never {@code null}.
   * @return A new style. Never {@code null}.
   */
  @NonNull
  public static PdfBlockStyle fill (@NonNull final PLColor aFillColor)
  {
    return new PdfBlockStyle (aFillColor, null);
  }

  /**
   * Create a style that only has a text colour.
   *
   * @param aTextColor
   *        The text colour. Never {@code null}.
   * @return A new style. Never {@code null}.
   */
  @NonNull
  public static PdfBlockStyle text (@NonNull final PLColor aTextColor)
  {
    return new PdfBlockStyle (null, aTextColor);
  }
}
