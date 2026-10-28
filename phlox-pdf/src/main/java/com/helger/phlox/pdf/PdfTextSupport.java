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

import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;

/**
 * Text adjustments for the template driven PDF output ({@link PdfTemplateEmitter} and the footer of
 * {@link PdfInvoiceWriter}).
 *
 * @author Philip Helger
 */
final class PdfTextSupport
{
  /**
   * A currency symbol that is directly followed by a digit, as in {@code €10.00}. The glyph of the
   * symbol touches the first digit in the PDF fonts, so a gap is inserted.
   */
  private static final Pattern CURRENCY_SYMBOL_BEFORE_DIGIT = Pattern.compile ("(\\p{Sc}+)(?=\\p{Nd})");

  private PdfTextSupport ()
  {}

  /**
   * Insert a non-breaking space between a currency symbol and the number that follows it, so that
   * the symbol and the number are never split over two lines.
   *
   * @param sText
   *        The text to adjust. Never {@code null}.
   * @return The adjusted text. Never {@code null}.
   */
  @NonNull
  static String separateCurrencySymbol (@NonNull final String sText)
  {
    return CURRENCY_SYMBOL_BEFORE_DIGIT.matcher (sText).replaceAll ("$1\u00A0");
  }

  /**
   * Escape the characters that the markup of ph-pdf-layout-richtext treats as a style or link
   * directive. A curly brace cannot be escaped, so it is replaced by a round one.
   *
   * @param sText
   *        The text to escape. Never {@code null}.
   * @return The escaped text. Never {@code null}.
   */
  @NonNull
  static String escapeMarkup (@NonNull final String sText)
  {
    final StringBuilder aSb = new StringBuilder (sText.length () + 8);
    final char [] aChars = sText.toCharArray ();
    for (int i = 0; i < aChars.length; ++i)
    {
      final char c = aChars[i];
      // Two tildes in a row are the strike-through marker
      // The markup parser does not honour a backslash in front of "{", so invoice data could
      // inject {link}, {color} or {bg} directives. Curly braces are shown as round ones instead.
      if (c == '{')
        aSb.append ('(');
      else
        if (c == '}')
          aSb.append (')');
        else
        {
          if (c == '\\' ||
              c == '*' ||
              c == '_' ||
              c == '`' ||
              (c == '~' && i + 1 < aChars.length && aChars[i + 1] == '~'))
            aSb.append ('\\');
          aSb.append (c);
        }
    }
    return aSb.toString ();
  }
}
