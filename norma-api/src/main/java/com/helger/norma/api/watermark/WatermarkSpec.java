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
package com.helger.norma.api.watermark;

import java.awt.Color;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Syntax-neutral watermark description. Consumed identically by HTML and PDF writers; each
 * writer interprets it for its medium.
 *
 * @author Philip Helger
 */
public class WatermarkSpec
{
  public static final float DEFAULT_OPACITY = 0.10f;
  public static final float DEFAULT_ROTATION = -45f;
  public static final float DEFAULT_FONT_SIZE_PT = 72f;
  public static final boolean DEFAULT_REPEAT_PER_PAGE = true;

  private final String m_sText;
  private float m_fOpacity = DEFAULT_OPACITY;
  private float m_fRotation = DEFAULT_ROTATION;
  private Color m_aColor = Color.GRAY;
  private EWatermarkPosition m_ePosition = EWatermarkPosition.CENTER;
  private float m_fFontSizePt = DEFAULT_FONT_SIZE_PT;
  private boolean m_bRepeatPerPage = DEFAULT_REPEAT_PER_PAGE;

  protected WatermarkSpec (@NonNull final String sText)
  {
    if (sText == null)
      throw new IllegalArgumentException ("Watermark text must not be null");
    m_sText = sText;
  }

  /** Creates a new spec with the given text and all other settings at their defaults. */
  @NonNull
  public static WatermarkSpec text (@NonNull final String sText)
  {
    return new WatermarkSpec (sText);
  }

  @NonNull
  public String getText ()
  {
    return m_sText;
  }

  public float getOpacity ()
  {
    return m_fOpacity;
  }

  @NonNull
  public WatermarkSpec setOpacity (final float fOpacity)
  {
    m_fOpacity = fOpacity;
    return this;
  }

  public float getRotation ()
  {
    return m_fRotation;
  }

  @NonNull
  public WatermarkSpec setRotation (final float fRotation)
  {
    m_fRotation = fRotation;
    return this;
  }

  @NonNull
  public Color getColor ()
  {
    return m_aColor;
  }

  @NonNull
  public WatermarkSpec setColor (@NonNull final Color aColor)
  {
    m_aColor = aColor;
    return this;
  }

  @NonNull
  public EWatermarkPosition getPosition ()
  {
    return m_ePosition;
  }

  @NonNull
  public WatermarkSpec setPosition (@NonNull final EWatermarkPosition ePosition)
  {
    m_ePosition = ePosition;
    return this;
  }

  public float getFontSizePt ()
  {
    return m_fFontSizePt;
  }

  @NonNull
  public WatermarkSpec setFontSizePt (final float fFontSizePt)
  {
    m_fFontSizePt = fFontSizePt;
    return this;
  }

  public boolean isRepeatPerPage ()
  {
    return m_bRepeatPerPage;
  }

  @NonNull
  public WatermarkSpec setRepeatPerPage (final boolean bRepeatPerPage)
  {
    m_bRepeatPerPage = bRepeatPerPage;
    return this;
  }

  /** Convenience: an opaque RGBA string for HTML (e.g. {@code "rgba(128,128,128,0.10)"}). */
  @NonNull
  public String getCssRgba ()
  {
    return "rgba(" +
           m_aColor.getRed () + "," +
           m_aColor.getGreen () + "," +
           m_aColor.getBlue () + "," +
           m_fOpacity + ")";
  }

  /**
   * Helper that returns the given spec, or {@code null} if the spec is {@code null}. Used in
   * inheritance setters to keep things compact.
   *
   * @param aSpec
   *        The spec, may be {@code null}.
   * @return The same spec, or {@code null}.
   */
  @Nullable
  public static WatermarkSpec orNull (@Nullable final WatermarkSpec aSpec)
  {
    return aSpec;
  }
}
