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
package com.helger.phlox.theme;

import java.awt.Color;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;

/**
 * Parser for the typed values of a theme: colours, lengths and numbers. All methods throw an
 * {@link IllegalArgumentException} with a short message for an invalid value; the caller adds the
 * place in the theme.
 *
 * @author Philip Helger
 */
final class PhloxThemeValues
{
  private static final Pattern RGB = Pattern.compile ("rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)");
  private static final Pattern LENGTH = Pattern.compile ("(-?\\d+(?:\\.\\d+)?)\\s*(mm|cm|in|pt|px)?");
  private static final float POINTS_PER_INCH = 72f;

  private PhloxThemeValues ()
  {}

  /**
   * Parse a colour. Supported are <code>#rgb</code>, <code>#rrggbb</code>,
   * <code>#rrggbbaa</code>, <code>rgb(r, g, b)</code>, <code>white</code> and <code>black</code>.
   *
   * @param sValue
   *        The text. May not be <code>null</code>.
   * @return The colour. Never <code>null</code>.
   * @throws IllegalArgumentException
   *         If the text is no colour.
   */
  @NonNull
  static Color parseColor (@NonNull final String sValue)
  {
    final String sText = sValue.trim ().toLowerCase (Locale.ROOT);
    if ("white".equals (sText))
      return Color.WHITE;
    if ("black".equals (sText))
      return Color.BLACK;
    if (sText.startsWith ("#"))
    {
      final String sHex = sText.substring (1);
      try
      {
        switch (sHex.length ())
        {
          case 3:
          {
            final int nR = Integer.parseInt (sHex.substring (0, 1), 16);
            final int nG = Integer.parseInt (sHex.substring (1, 2), 16);
            final int nB = Integer.parseInt (sHex.substring (2, 3), 16);
            return new Color (nR * 17, nG * 17, nB * 17);
          }
          case 6:
            return new Color (Integer.parseInt (sHex, 16));
          case 8:
            return new Color (Integer.parseInt (sHex.substring (0, 6), 16));
          default:
            break;
        }
      }
      catch (final NumberFormatException ex)
      {
        // fall through
      }
    }
    final Matcher aMatcher = RGB.matcher (sText);
    if (aMatcher.matches ())
    {
      final int nR = Integer.parseInt (aMatcher.group (1));
      final int nG = Integer.parseInt (aMatcher.group (2));
      final int nB = Integer.parseInt (aMatcher.group (3));
      if (nR <= 255 && nG <= 255 && nB <= 255)
        return new Color (nR, nG, nB);
    }
    throw new IllegalArgumentException ("'" + sValue + "' is no colour - use #rgb, #rrggbb, rgb(r, g, b), white or black");
  }

  /**
   * Parse a length. A number without unit is in points; the units <code>pt</code>,
   * <code>mm</code>, <code>cm</code>, <code>in</code> and <code>px</code> (0.75 pt) are known.
   *
   * @param sValue
   *        The text. May not be <code>null</code>.
   * @return The length in points.
   * @throws IllegalArgumentException
   *         If the text is no length.
   */
  static float parseLength (@NonNull final String sValue)
  {
    final Matcher aMatcher = LENGTH.matcher (sValue.trim ().toLowerCase (Locale.ROOT));
    if (!aMatcher.matches ())
      throw new IllegalArgumentException ("'" + sValue + "' is no length - use a number of points or a number with mm, cm, in, pt or px");
    final float fNumber = Float.parseFloat (aMatcher.group (1));
    final String sUnit = aMatcher.group (2);
    if (sUnit == null || "pt".equals (sUnit))
      return fNumber;
    switch (sUnit)
    {
      case "mm":
        return fNumber * POINTS_PER_INCH / 25.4f;
      case "cm":
        return fNumber * POINTS_PER_INCH / 2.54f;
      case "in":
        return fNumber * POINTS_PER_INCH;
      default:
        return fNumber * 0.75f;
    }
  }

  /**
   * Parse a decimal number.
   *
   * @param sValue
   *        The text. May not be <code>null</code>.
   * @return The number.
   * @throws IllegalArgumentException
   *         If the text is no number.
   */
  static float parseFloat (@NonNull final String sValue)
  {
    try
    {
      final float ret = Float.parseFloat (sValue.trim ());
      if (!Float.isNaN (ret) && !Float.isInfinite (ret))
        return ret;
    }
    catch (final NumberFormatException ex)
    {
      // fall through
    }
    throw new IllegalArgumentException ("'" + sValue + "' is no number");
  }
}
