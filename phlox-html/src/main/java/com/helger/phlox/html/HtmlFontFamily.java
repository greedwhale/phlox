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
package com.helger.phlox.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;

/**
 * The fonts of the HTML output: one font file for each of the four styles regular, bold, italic
 * and bold italic. The fonts are embedded into the document as <code>@font-face</code> rules with
 * <code>data:</code> URIs, so the HTML stays a single file, and the body uses them before the
 * default font stack. Supported are TTF, OTF, WOFF and WOFF2 files. Use the key
 * <code>html.fonts</code> of a theme for font files.
 *
 * @author Philip Helger
 */
@Immutable
public final class HtmlFontFamily
{
  /** The name of the font family in the CSS. */
  public static final String CSS_FAMILY_NAME = "Phlox Custom";

  private final String m_sCss;

  private static void _appendFace (@NonNull final StringBuilder aSB,
                                   @NonNull final String sReference,
                                   final byte @NonNull [] aBytes,
                                   final boolean bBold,
                                   final boolean bItalic)
  {
    final String sLower = sReference.toLowerCase (Locale.ROOT);
    final String sMime;
    final String sFormat;
    if (sLower.endsWith (".ttf"))
    {
      sMime = "font/ttf";
      sFormat = "truetype";
    }
    else
      if (sLower.endsWith (".otf"))
      {
        sMime = "font/otf";
        sFormat = "opentype";
      }
      else
        if (sLower.endsWith (".woff2"))
        {
          sMime = "font/woff2";
          sFormat = "woff2";
        }
        else
          if (sLower.endsWith (".woff"))
          {
            sMime = "font/woff";
            sFormat = "woff";
          }
          else
            throw new IllegalArgumentException ("'" + sReference + "' is neither a .ttf, .otf, .woff nor a .woff2 file");
    aSB.append ("@font-face{font-family:\"")
       .append (CSS_FAMILY_NAME)
       .append ("\";font-weight:")
       .append (bBold ? "700" : "400")
       .append (";font-style:")
       .append (bItalic ? "italic" : "normal")
       .append (";src:url(data:")
       .append (sMime)
       .append (";base64,")
       .append (Base64.getEncoder ().encodeToString (aBytes))
       .append (") format(\"")
       .append (sFormat)
       .append ("\")}\n");
  }

  /**
   * Constructor. A style without a file is synthesized by the browser from the regular font.
   *
   * @param sRegularName
   *        The file name or classpath reference of the regular font; its extension selects the
   *        font type. May neither be <code>null</code> nor empty.
   * @param aRegular
   *        The bytes of the regular font. May not be <code>null</code>.
   * @param sBoldName
   *        The name of the bold font. May be <code>null</code>.
   * @param aBold
   *        The bytes of the bold font. May be <code>null</code>.
   * @param sItalicName
   *        The name of the italic font. May be <code>null</code>.
   * @param aItalic
   *        The bytes of the italic font. May be <code>null</code>.
   * @param sBoldItalicName
   *        The name of the bold italic font. May be <code>null</code>.
   * @param aBoldItalic
   *        The bytes of the bold italic font. May be <code>null</code>.
   * @throws IllegalArgumentException
   *         If a file has an unsupported extension.
   */
  public HtmlFontFamily (@NonNull @Nonempty final String sRegularName,
                         final byte @NonNull [] aRegular,
                         @Nullable final String sBoldName,
                         final byte @Nullable [] aBold,
                         @Nullable final String sItalicName,
                         final byte @Nullable [] aItalic,
                         @Nullable final String sBoldItalicName,
                         final byte @Nullable [] aBoldItalic)
  {
    ValueEnforcer.notEmpty (sRegularName, "RegularName");
    ValueEnforcer.notNull (aRegular, "Regular");
    final StringBuilder aSB = new StringBuilder ();
    _appendFace (aSB, sRegularName, aRegular, false, false);
    // A style without a file is synthesized by the browser from the regular face
    if (aBold != null && sBoldName != null)
      _appendFace (aSB, sBoldName, aBold, true, false);
    if (aItalic != null && sItalicName != null)
      _appendFace (aSB, sItalicName, aItalic, false, true);
    if (aBoldItalic != null && sBoldItalicName != null)
      _appendFace (aSB, sBoldItalicName, aBoldItalic, true, true);
    aSB.append ("body{font-family:\"").append (CSS_FAMILY_NAME).append ("\",-apple-system,\"Segoe UI\",Helvetica,Arial,sans-serif}\n");
    m_sCss = aSB.toString ();
  }

  /**
   * @return The CSS to put into the stylesheet: the <code>@font-face</code> rules and the rule
   *         for the body. Never <code>null</code>.
   */
  @NonNull
  public String getCss ()
  {
    return m_sCss;
  }

  /**
   * Create a family from font files. A style without a file is synthesized by the browser from the regular font.
   *
   * @param aRegular
   *        The regular font file. May not be <code>null</code>.
   * @param aBold
   *        The bold font file. May be <code>null</code>.
   * @param aItalic
   *        The italic font file. May be <code>null</code>.
   * @param aBoldItalic
   *        The bold italic font file. May be <code>null</code>.
   * @return The new family. Never <code>null</code>.
   * @throws IOException
   *         If a file cannot be read.
   * @throws IllegalArgumentException
   *         If a file has an unsupported extension.
   */
  @NonNull
  public static HtmlFontFamily ofFiles (@NonNull final Path aRegular,
                                        @Nullable final Path aBold,
                                        @Nullable final Path aItalic,
                                        @Nullable final Path aBoldItalic) throws IOException
  {
    ValueEnforcer.notNull (aRegular, "Regular");
    return new HtmlFontFamily (aRegular.getFileName ().toString (),
                               Files.readAllBytes (aRegular),
                               aBold == null ? null : aBold.getFileName ().toString (),
                               aBold == null ? null : Files.readAllBytes (aBold),
                               aItalic == null ? null : aItalic.getFileName ().toString (),
                               aItalic == null ? null : Files.readAllBytes (aItalic),
                               aBoldItalic == null ? null : aBoldItalic.getFileName ().toString (),
                               aBoldItalic == null ? null : Files.readAllBytes (aBoldItalic));
  }
}
