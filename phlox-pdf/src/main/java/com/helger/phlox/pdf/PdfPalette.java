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

import java.awt.Color;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.concurrent.Immutable;
import com.helger.pdflayout.base.PLColor;
import com.helger.phlox.theme.PhloxTheme;

/**
 * The colours of the default PDF layout. They have the same meaning as the CSS custom properties of
 * the default HTML stylesheet (<code>--phlox-ink</code> and so on, see {@link PhloxTheme}).
 *
 * @author Philip Helger
 */
@Immutable
public final class PdfPalette
{
  /** The colours of the default layout. */
  public static final PdfPalette DEFAULT = new PdfPalette (new PLColor (0x1d, 0x24, 0x33),
                                                           new PLColor (0x66, 0x70, 0x85),
                                                           new PLColor (0x98, 0xa2, 0xb3),
                                                           new PLColor (0xe4, 0xe7, 0xec),
                                                           new PLColor (0xf6, 0xf7, 0xf9),
                                                           new PLColor (0x24, 0x5c, 0x64),
                                                           new PLColor (0xe8, 0xf1, 0xf2));

  private final PLColor m_aInk;
  private final PLColor m_aMuted;
  private final PLColor m_aFaint;
  private final PLColor m_aRule;
  private final PLColor m_aWash;
  private final PLColor m_aAccent;
  private final PLColor m_aAccentWash;

  /**
   * Constructor.
   *
   * @param aInk
   *        The colour of the text and of the line under the table headers. May not be
   *        <code>null</code>.
   * @param aMuted
   *        The colour of secondary text. May not be <code>null</code>.
   * @param aFaint
   *        The colour of the faintest text. May not be <code>null</code>.
   * @param aRule
   *        The colour of thin lines. May not be <code>null</code>.
   * @param aWash
   *        The light background colour. May not be <code>null</code>.
   * @param aAccent
   *        The accent colour. May not be <code>null</code>.
   * @param aAccentWash
   *        The light background in the accent colour. May not be <code>null</code>.
   */
  public PdfPalette (@NonNull final PLColor aInk,
                     @NonNull final PLColor aMuted,
                     @NonNull final PLColor aFaint,
                     @NonNull final PLColor aRule,
                     @NonNull final PLColor aWash,
                     @NonNull final PLColor aAccent,
                     @NonNull final PLColor aAccentWash)
  {
    m_aInk = aInk;
    m_aMuted = aMuted;
    m_aFaint = aFaint;
    m_aRule = aRule;
    m_aWash = aWash;
    m_aAccent = aAccent;
    m_aAccentWash = aAccentWash;
  }

  @NonNull
  private static PLColor _color (@NonNull final PhloxTheme aTheme,
                                 @NonNull final String sName,
                                 @NonNull final PLColor aDefault)
  {
    final Color aColor = aTheme.getColorVariable (sName, null);
    return aColor == null ? aDefault : new PLColor (aColor.getRed (), aColor.getGreen (), aColor.getBlue ());
  }

  /**
   * Create the palette from the variables of a theme. A colour that the theme does not define is
   * the one of {@link #DEFAULT}.
   *
   * @param aTheme
   *        The theme. May not be <code>null</code>.
   * @return The palette. Never <code>null</code>.
   */
  @NonNull
  public static PdfPalette fromTheme (@NonNull final PhloxTheme aTheme)
  {
    return new PdfPalette (_color (aTheme, PhloxTheme.VAR_INK, DEFAULT.m_aInk),
                           _color (aTheme, PhloxTheme.VAR_MUTED, DEFAULT.m_aMuted),
                           _color (aTheme, PhloxTheme.VAR_FAINT, DEFAULT.m_aFaint),
                           _color (aTheme, PhloxTheme.VAR_RULE, DEFAULT.m_aRule),
                           _color (aTheme, PhloxTheme.VAR_WASH, DEFAULT.m_aWash),
                           _color (aTheme, PhloxTheme.VAR_ACCENT, DEFAULT.m_aAccent),
                           _color (aTheme, PhloxTheme.VAR_ACCENT_WASH, DEFAULT.m_aAccentWash));
  }

  /**
   * @return The colour of the text and of the line under the table headers.
   */
  @NonNull
  public PLColor getInk ()
  {
    return m_aInk;
  }

  /**
   * @return The colour of secondary text.
   */
  @NonNull
  public PLColor getMuted ()
  {
    return m_aMuted;
  }

  /**
   * @return The colour of the faintest text.
   */
  @NonNull
  public PLColor getFaint ()
  {
    return m_aFaint;
  }

  /**
   * @return The colour of thin lines.
   */
  @NonNull
  public PLColor getRule ()
  {
    return m_aRule;
  }

  /**
   * @return The light background colour.
   */
  @NonNull
  public PLColor getWash ()
  {
    return m_aWash;
  }

  /**
   * @return The accent colour.
   */
  @NonNull
  public PLColor getAccent ()
  {
    return m_aAccent;
  }

  /**
   * @return The light background in the accent colour.
   */
  @NonNull
  public PLColor getAccentWash ()
  {
    return m_aAccentWash;
  }
}
