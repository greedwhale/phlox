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

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.MarginSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.phlox.api.AbstractRenderContext;

/**
 * PDF-specific render context. Adds page size, margins and font settings on top of the shared
 * {@link AbstractRenderContext} knobs.
 *
 * @author Philip Helger
 */
public class PdfRenderContext extends AbstractRenderContext <PdfRenderContext>
{
  /** Default page size (A4). */
  public static final PDRectangle DEFAULT_PAGE_SIZE = PDRectangle.A4;
  private static final PLColor ACCENT = new PLColor (0x24, 0x5c, 0x64);
  private static final PLColor ACCENT_WASH = new PLColor (0xe8, 0xf1, 0xf2);
  private static final PLColor MUTED = new PLColor (0x66, 0x70, 0x85);
  private static final PLColor WASH = new PLColor (0xf6, 0xf7, 0xf9);

  /** Default page margins (20 mm). */
  public static final MarginSpec DEFAULT_MARGINS = new MarginSpec (20 * 72 / 25.4f); // 20mm in points
  /** Default font of the body text. */
  public static final FontSpec DEFAULT_BODY_FONT = new FontSpec (PreloadFont.REGULAR, 10f);
  /** Default font of section headers. */
  public static final FontSpec DEFAULT_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 8f, MUTED);
  /** Default font of table headers. */
  public static final FontSpec DEFAULT_TABLE_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 7.5f, MUTED);

  private PDRectangle m_aPageSize = DEFAULT_PAGE_SIZE;
  private MarginSpec m_aMargins = DEFAULT_MARGINS;
  private FontSpec m_aBodyFont = DEFAULT_BODY_FONT;
  private FontSpec m_aHeaderFont = DEFAULT_HEADER_FONT;
  private FontSpec m_aTableHeaderFont = DEFAULT_TABLE_HEADER_FONT;
  private final ICommonsMap <String, PdfBlockStyle> m_aBlockStyles = new CommonsHashMap <> ();

  /**
   * Create a new context pre-populated with the shared settings of another context.
   *
   * @param aSource
   *        The context to copy the shared settings from. Never {@code null}.
   * @return A new context. Never {@code null}.
   */
  @NonNull
  public static PdfRenderContext copyFrom (@NonNull final AbstractRenderContext <?> aSource)
  {
    return new PdfRenderContext ().copyBaseFieldsFrom (aSource);
  }

  /**
   * Get the page size.
   *
   * @return The page size.
   */
  @NonNull
  public PDRectangle getPageSize ()
  {
    return m_aPageSize;
  }

  /**
   * Set the page size.
   *
   * @param aPageSize
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setPageSize (@NonNull final PDRectangle aPageSize)
  {
    m_aPageSize = aPageSize;
    return this;
  }

  /**
   * Get the page margins.
   *
   * @return The page margins.
   */
  @NonNull
  public MarginSpec getMargins ()
  {
    return m_aMargins;
  }

  /**
   * Set the page margins.
   *
   * @param aMargins
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setMargins (@NonNull final MarginSpec aMargins)
  {
    m_aMargins = aMargins;
    return this;
  }

  /**
   * Get the body font.
   *
   * @return The body font.
   */
  @NonNull
  public FontSpec getBodyFont ()
  {
    return m_aBodyFont;
  }

  /**
   * Set the body font.
   *
   * @param aBodyFont
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setBodyFont (@NonNull final FontSpec aBodyFont)
  {
    m_aBodyFont = aBodyFont;
    return this;
  }

  /**
   * Get the header font.
   *
   * @return The header font.
   */
  @NonNull
  public FontSpec getHeaderFont ()
  {
    return m_aHeaderFont;
  }

  /**
   * Set the header font.
   *
   * @param aHeaderFont
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setHeaderFont (@NonNull final FontSpec aHeaderFont)
  {
    m_aHeaderFont = aHeaderFont;
    return this;
  }

  /**
   * Get the table header font.
   *
   * @return The table header font.
   */
  @NonNull
  public FontSpec getTableHeaderFont ()
  {
    return m_aTableHeaderFont;
  }

  /**
   * Set the table header font.
   *
   * @param aTableHeaderFont
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setTableHeaderFont (@NonNull final FontSpec aTableHeaderFont)
  {
    m_aTableHeaderFont = aTableHeaderFont;
    return this;
  }

  /**
   * Set the look of the blocks and table rows that a template marked with {@code {{class:name}}}.
   * A block class without a style is rendered unstyled.
   *
   * @param sName
   *        The class name as used in the template, without prefix. Never {@code null}.
   * @param aStyle
   *        The style. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext putBlockStyle (@NonNull final String sName, @NonNull final PdfBlockStyle aStyle)
  {
    m_aBlockStyles.put (sName, aStyle);
    return this;
  }

  /** Create a new context with the default PDF settings. */
  public PdfRenderContext ()
  {
    // The row with the amount due of the template of an earlier version
    putBlockStyle ("totals-due", PdfBlockStyle.fill (PLColor.gray (0xe8)));

    // The look of the default template
    putBlockStyle ("kind", PdfBlockStyle.builder ().textColor (ACCENT).fontSize (Float.valueOf (8.5f)).bold (true).build ());
    putBlockStyle ("label", PdfBlockStyle.builder ().textColor (MUTED).fontSize (Float.valueOf (7.5f)).bold (true).build ());
    putBlockStyle ("muted", PdfBlockStyle.builder ().textColor (MUTED).fontSize (Float.valueOf (8.5f)).build ());
    putBlockStyle ("party-name", PdfBlockStyle.builder ().fontSize (Float.valueOf (11f)).bold (true).build ());
    putBlockStyle ("rule", PdfBlockStyle.builder ().borderTopColor (ACCENT).borderWidth (1.5f).build ());
    putBlockStyle ("head-logo", PdfBlockStyle.builder ().widthPercent (Float.valueOf (25f)).build ());
    putBlockStyle ("totals-col", PdfBlockStyle.builder ().widthPercent (Float.valueOf (42f)).build ());
    putBlockStyle ("totals", PdfBlockStyle.builder ().fillColor (WASH).padding (8f).build ());
    putBlockStyle ("due",
                   PdfBlockStyle.builder ().fillColor (ACCENT).textColor (PLColor.WHITE).padding (8f).bold (true).build ());
    putBlockStyle ("pay", PdfBlockStyle.builder ().fillColor (ACCENT_WASH).padding (10f).build ());
    putBlockStyle ("foot", PdfBlockStyle.builder ().textColor (MUTED).fontSize (Float.valueOf (7.5f)).build ());
  }

  /**
   * Get the look of a block class.
   *
   * @param sName
   *        The class name as used in the template, without prefix. Never {@code null}.
   * @return The style or {@code null} if no style is registered for that name.
   */
  @Nullable
  public PdfBlockStyle getBlockStyle (@NonNull final String sName)
  {
    return m_aBlockStyles.get (sName);
  }
}
