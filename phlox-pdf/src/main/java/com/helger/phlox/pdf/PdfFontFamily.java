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

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.font.api.IFontResource;
import com.helger.font.api.IHasFontResource;
import com.helger.pdflayout.richtext.run.PLFontFamily;
import com.helger.pdflayout.spec.PreloadFont;

/**
 * The fonts of the PDF output: one font for each of the four styles regular, bold, italic and
 * bold italic. The fonts of the default layout are the standard fonts of PDF ({@link #STANDARD}),
 * which only contain the characters of Western European languages. A text in another script, like
 * Cyrillic or Greek, needs a font that contains these characters. Use {@link #of(IHasFontResource,
 * IHasFontResource, IHasFontResource, IHasFontResource)} for the fonts of the ph-fonts project, or
 * the key <code>pdf.fonts</code> of a theme for font files. The fonts are embedded into the PDF.
 * <p>
 * Monospace text (code) always uses the standard monospace font.
 *
 * @author Philip Helger
 */
@Immutable
public final class PdfFontFamily
{
  /** The standard fonts of PDF (Helvetica), the fonts of the default layout. */
  public static final PdfFontFamily STANDARD = new PdfFontFamily (PreloadFont.REGULAR,
                                                                  PreloadFont.REGULAR_BOLD,
                                                                  PreloadFont.REGULAR_ITALIC,
                                                                  PreloadFont.REGULAR_BOLD_ITALIC);

  private final PreloadFont m_aRegular;
  private final PreloadFont m_aBold;
  private final PreloadFont m_aItalic;
  private final PreloadFont m_aBoldItalic;

  /**
   * Constructor.
   *
   * @param aRegular
   *        The regular font. May not be <code>null</code>.
   * @param aBold
   *        The bold font. May not be <code>null</code>.
   * @param aItalic
   *        The italic font. May not be <code>null</code>.
   * @param aBoldItalic
   *        The bold italic font. May not be <code>null</code>.
   */
  public PdfFontFamily (@NonNull final PreloadFont aRegular,
                        @NonNull final PreloadFont aBold,
                        @NonNull final PreloadFont aItalic,
                        @NonNull final PreloadFont aBoldItalic)
  {
    m_aRegular = ValueEnforcer.notNull (aRegular, "Regular");
    m_aBold = ValueEnforcer.notNull (aBold, "Bold");
    m_aItalic = ValueEnforcer.notNull (aItalic, "Italic");
    m_aBoldItalic = ValueEnforcer.notNull (aBoldItalic, "BoldItalic");
  }

  /**
   * @return The regular font. Never <code>null</code>.
   */
  @NonNull
  public PreloadFont getRegular ()
  {
    return m_aRegular;
  }

  /**
   * @return The bold font. Never <code>null</code>.
   */
  @NonNull
  public PreloadFont getBold ()
  {
    return m_aBold;
  }

  /**
   * @return The italic font. Never <code>null</code>.
   */
  @NonNull
  public PreloadFont getItalic ()
  {
    return m_aItalic;
  }

  /**
   * @return The bold italic font. Never <code>null</code>.
   */
  @NonNull
  public PreloadFont getBoldItalic ()
  {
    return m_aBoldItalic;
  }

  /**
   * @return This family in the form that the rich text of the PDF layout uses. Never
   *         <code>null</code>.
   */
  @NonNull
  public PLFontFamily getAsPLFontFamily ()
  {
    return new PLFontFamily (m_aRegular, m_aBold, m_aItalic, m_aBoldItalic);
  }

  /**
   * Get the font of the same style in another family, for example the bold font of the target if
   * the passed font is the bold font of this family.
   *
   * @param aFont
   *        A font of this family. Any other font is returned as it is. May not be
   *        <code>null</code>.
   * @param aTarget
   *        The family to get the font from. May not be <code>null</code>.
   * @return The font of the target. Never <code>null</code>.
   */
  @NonNull
  public PreloadFont getSameStyleIn (@NonNull final PreloadFont aFont, @NonNull final PdfFontFamily aTarget)
  {
    ValueEnforcer.notNull (aFont, "Font");
    ValueEnforcer.notNull (aTarget, "Target");
    if (aFont.equals (m_aBoldItalic))
      return aTarget.m_aBoldItalic;
    if (aFont.equals (m_aBold))
      return aTarget.m_aBold;
    if (aFont.equals (m_aItalic))
      return aTarget.m_aItalic;
    if (aFont.equals (m_aRegular))
      return aTarget.m_aRegular;
    return aFont;
  }

  /**
   * Create a family from font resources. The fonts are embedded into the PDF.
   *
   * @param aRegular
   *        The regular font. May not be <code>null</code>.
   * @param aBold
   *        The bold font. May not be <code>null</code>.
   * @param aItalic
   *        The italic font. May not be <code>null</code>.
   * @param aBoldItalic
   *        The bold italic font. May not be <code>null</code>.
   * @return The new family. Never <code>null</code>.
   * @throws IllegalArgumentException
   *         If a font cannot be loaded.
   */
  @NonNull
  public static PdfFontFamily of (@NonNull final IFontResource aRegular,
                                  @NonNull final IFontResource aBold,
                                  @NonNull final IFontResource aItalic,
                                  @NonNull final IFontResource aBoldItalic)
  {
    return new PdfFontFamily (PreloadFont.createEmbedding (aRegular),
                              PreloadFont.createEmbedding (aBold),
                              PreloadFont.createEmbedding (aItalic),
                              PreloadFont.createEmbedding (aBoldItalic));
  }

  /**
   * Create a family from the fonts of the ph-fonts project, for example
   * <code>EFontResourceRoboto.ROBOTO_NORMAL</code>. The fonts are embedded into the PDF.
   *
   * @param aRegular
   *        The regular font. May not be <code>null</code>.
   * @param aBold
   *        The bold font. May not be <code>null</code>.
   * @param aItalic
   *        The italic font. May not be <code>null</code>.
   * @param aBoldItalic
   *        The bold italic font. May not be <code>null</code>.
   * @return The new family. Never <code>null</code>.
   * @throws IllegalArgumentException
   *         If a font cannot be loaded.
   */
  @NonNull
  public static PdfFontFamily of (@NonNull final IHasFontResource aRegular,
                                  @NonNull final IHasFontResource aBold,
                                  @NonNull final IHasFontResource aItalic,
                                  @NonNull final IHasFontResource aBoldItalic)
  {
    return of (aRegular.getFontResource (),
               aBold.getFontResource (),
               aItalic.getFontResource (),
               aBoldItalic.getFontResource ());
  }
}
