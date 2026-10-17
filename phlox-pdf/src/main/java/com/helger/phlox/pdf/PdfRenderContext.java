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

import com.helger.phlox.api.AbstractRenderContext;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.MarginSpec;
import com.helger.pdflayout.spec.PreloadFont;

/**
 * PDF-specific render context. Adds page size, margins and font settings on top of the shared
 * {@link AbstractRenderContext} knobs.
 *
 * @author Philip Helger
 */
public class PdfRenderContext extends AbstractRenderContext <PdfRenderContext>
{
  public static final PDRectangle DEFAULT_PAGE_SIZE = PDRectangle.A4;
  public static final MarginSpec DEFAULT_MARGINS = new MarginSpec (20 * 72 / 25.4f); // 20mm in points
  public static final FontSpec DEFAULT_BODY_FONT = new FontSpec (PreloadFont.REGULAR, 10f);
  public static final FontSpec DEFAULT_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 12f);
  public static final FontSpec DEFAULT_TABLE_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 10f);

  private PDRectangle m_aPageSize = DEFAULT_PAGE_SIZE;
  private MarginSpec m_aMargins = DEFAULT_MARGINS;
  private FontSpec m_aBodyFont = DEFAULT_BODY_FONT;
  private FontSpec m_aHeaderFont = DEFAULT_HEADER_FONT;
  private FontSpec m_aTableHeaderFont = DEFAULT_TABLE_HEADER_FONT;

  public PdfRenderContext ()
  {}

  /** Pre-populate from another context's shared settings. */
  @NonNull
  public static PdfRenderContext copyFrom (@NonNull final AbstractRenderContext <?> aSource)
  {
    return new PdfRenderContext ().copyBaseFieldsFrom (aSource);
  }

  @NonNull
  public PDRectangle getPageSize ()
  {
    return m_aPageSize;
  }

  @NonNull
  public PdfRenderContext setPageSize (@NonNull final PDRectangle aPageSize)
  {
    m_aPageSize = aPageSize;
    return this;
  }

  @NonNull
  public MarginSpec getMargins ()
  {
    return m_aMargins;
  }

  @NonNull
  public PdfRenderContext setMargins (@NonNull final MarginSpec aMargins)
  {
    m_aMargins = aMargins;
    return this;
  }

  @NonNull
  public FontSpec getBodyFont ()
  {
    return m_aBodyFont;
  }

  @NonNull
  public PdfRenderContext setBodyFont (@NonNull final FontSpec aBodyFont)
  {
    m_aBodyFont = aBodyFont;
    return this;
  }

  @NonNull
  public FontSpec getHeaderFont ()
  {
    return m_aHeaderFont;
  }

  @NonNull
  public PdfRenderContext setHeaderFont (@NonNull final FontSpec aHeaderFont)
  {
    m_aHeaderFont = aHeaderFont;
    return this;
  }

  @NonNull
  public FontSpec getTableHeaderFont ()
  {
    return m_aTableHeaderFont;
  }

  @NonNull
  public PdfRenderContext setTableHeaderFont (@NonNull final FontSpec aTableHeaderFont)
  {
    m_aTableHeaderFont = aTableHeaderFont;
    return this;
  }
}
