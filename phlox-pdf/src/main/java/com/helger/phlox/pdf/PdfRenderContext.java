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
import java.util.Locale;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.MarginSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.phlox.api.AbstractRenderContext;
import com.helger.phlox.theme.PhloxTheme;
import com.helger.phlox.theme.PhloxThemeApplier;
import com.helger.phlox.theme.PhloxThemeException;
import com.helger.phlox.theme.PhloxThemeObject;

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
  /** Default page margins (20 mm). */
  public static final MarginSpec DEFAULT_MARGINS = new MarginSpec (20 * 72 / 25.4f); // 20mm in
                                                                                     // points
  /** Default font of the body text. */
  public static final FontSpec DEFAULT_BODY_FONT = new FontSpec (PreloadFont.REGULAR, 10f);
  /** Default font of section headers. */
  public static final FontSpec DEFAULT_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 8f, PdfPalette.DEFAULT.getMuted ());
  /** Default font of table headers. */
  public static final FontSpec DEFAULT_TABLE_HEADER_FONT = new FontSpec (PreloadFont.REGULAR_BOLD, 7.5f, PdfPalette.DEFAULT.getMuted ());

  private PDRectangle m_aPageSize = DEFAULT_PAGE_SIZE;
  private MarginSpec m_aMargins = DEFAULT_MARGINS;
  private FontSpec m_aBodyFont = DEFAULT_BODY_FONT;
  private FontSpec m_aHeaderFont = DEFAULT_HEADER_FONT;
  private FontSpec m_aTableHeaderFont = DEFAULT_TABLE_HEADER_FONT;
  private PdfPalette m_aPalette = PdfPalette.DEFAULT;
  private final ICommonsMap <String, PdfBlockStyle> m_aBlockStyles = new CommonsHashMap <> ();

  @NonNull
  private static PDRectangle _getPageSize (@NonNull final PhloxThemeObject aPdf, @NonNull final String sName)
  {
    switch (sName.trim ().toUpperCase (Locale.ROOT))
    {
      case "A0":
        return PDRectangle.A0;
      case "A1":
        return PDRectangle.A1;
      case "A2":
        return PDRectangle.A2;
      case "A3":
        return PDRectangle.A3;
      case "A4":
        return PDRectangle.A4;
      case "A5":
        return PDRectangle.A5;
      case "A6":
        return PDRectangle.A6;
      case "LETTER":
        return PDRectangle.LETTER;
      case "LEGAL":
        return PDRectangle.LEGAL;
      default:
        throw new PhloxThemeException ("Theme error at '" +
                                       aPdf.getPath () +
                                       ".pageSize': '" +
                                       sName +
                                       "' is unknown, the possible values are a0, a1, a2, a3, a4, a5, a6, letter, legal");
    }
  }

  @NonNull
  private static MarginSpec _getMargins (@NonNull final PhloxThemeObject aPdf, @NonNull final MarginSpec aCurrent)
  {
    final PhloxThemeObject aSides = aPdf.getObject ("margins");
    if (aSides == null)
    {
      final Float aAll = aPdf.getLength ("margins");
      return aAll == null ? aCurrent : new MarginSpec (aAll.floatValue ());
    }
    aSides.checkKeys ("top", "right", "bottom", "left");
    final Float aTop = aSides.getLength ("top");
    final Float aRight = aSides.getLength ("right");
    final Float aBottom = aSides.getLength ("bottom");
    final Float aLeft = aSides.getLength ("left");
    return new MarginSpec (aTop == null ? aCurrent.getTop () : aTop.floatValue (),
                           aRight == null ? aCurrent.getRight () : aRight.floatValue (),
                           aBottom == null ? aCurrent.getBottom () : aBottom.floatValue (),
                           aLeft == null ? aCurrent.getLeft () : aLeft.floatValue ());
  }

  @NonNull
  private static FontSpec _getFont (@NonNull final PhloxThemeObject aPdf,
                                    @NonNull final String sKey,
                                    @NonNull final FontSpec aCurrent)
  {
    final PhloxThemeObject aFont = aPdf.getObject (sKey);
    if (aFont == null)
      return aCurrent;
    aFont.checkKeys ("size", "bold", "color");
    final Boolean aBold = aFont.getBoolean ("bold");
    final Float aSize = aFont.getFloat ("size");
    final Color aColor = aFont.getColor ("color");
    return new FontSpec (aBold == null ? aCurrent.getPreloadFont ()
                                       : aBold.booleanValue () ? PreloadFont.REGULAR_BOLD : PreloadFont.REGULAR,
                         aSize == null ? aCurrent.getFontSize () : aSize.floatValue (),
                         aColor == null ? aCurrent.getColor () : new PLColor (aColor.getRed (), aColor.getGreen (), aColor.getBlue ()));
  }

  private static void _setBorder (final PdfBlockStyle.@NonNull Builder aBuilder,
                                  @NonNull final PhloxThemeObject aStyle,
                                  @NonNull final String sKey,
                                  final boolean bTop)
  {
    final PhloxThemeObject aBorder = aStyle.getObject (sKey);
    if (aBorder == null)
      return;
    aBorder.checkKeys ("color", "width");
    final Color aColor = aBorder.getColor ("color");
    if (aColor == null)
      throw new PhloxThemeException ("Theme error at '" + aBorder.getPath () + ".color': the colour of the line is required");
    final PLColor aPLColor = new PLColor (aColor.getRed (), aColor.getGreen (), aColor.getBlue ());
    if (bTop)
      aBuilder.borderTopColor (aPLColor);
    else
      aBuilder.borderBottomColor (aPLColor);
    if (aBorder.getLength ("width") != null)
      aBuilder.borderWidth (aBorder.getLength ("width").floatValue ());
  }

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
   * Set the look of the blocks and table rows that a template marked with {@code {{class:name}}}. A
   * block class without a style is rendered unstyled.
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

  private void _registerDefaultStyles ()
  {
    // The row with the amount due of the template of an earlier version
    putBlockStyle ("totals-due", PdfBlockStyle.fill (PLColor.gray (0xe8)));

    // The look of the default template
    putBlockStyle ("kind", PdfBlockStyle.builder ().textColor (m_aPalette.getAccent ()).fontSize (Float.valueOf (8.5f)).bold (true).build ());
    putBlockStyle ("label", PdfBlockStyle.builder ().textColor (m_aPalette.getMuted ()).fontSize (Float.valueOf (7.5f)).bold (true).build ());
    putBlockStyle ("muted", PdfBlockStyle.builder ().textColor (m_aPalette.getMuted ()).fontSize (Float.valueOf (8.5f)).build ());
    putBlockStyle ("party-name", PdfBlockStyle.builder ().fontSize (Float.valueOf (11f)).bold (true).build ());
    putBlockStyle ("rule", PdfBlockStyle.builder ().borderTopColor (m_aPalette.getAccent ()).borderWidth (1.5f).build ());
    putBlockStyle ("head-logo", PdfBlockStyle.builder ().widthPercent (Float.valueOf (25f)).build ());
    putBlockStyle ("totals-col", PdfBlockStyle.builder ().widthPercent (Float.valueOf (42f)).build ());
    putBlockStyle ("totals", PdfBlockStyle.builder ().fillColor (m_aPalette.getWash ()).padding (8f).build ());
    putBlockStyle ("due",
                   PdfBlockStyle.builder ()
                                .fillColor (m_aPalette.getAccent ())
                                .textColor (PLColor.WHITE)
                                .padding (8f)
                                .bold (true)
                                .build ());
    putBlockStyle ("pay", PdfBlockStyle.builder ().fillColor (m_aPalette.getAccentWash ()).padding (10f).build ());
    putBlockStyle ("foot", PdfBlockStyle.builder ().textColor (m_aPalette.getMuted ()).fontSize (Float.valueOf (7.5f)).build ());
  }

  /** Create a new context with the default PDF settings. */
  public PdfRenderContext ()
  {
    _registerDefaultStyles ();
  }

  /**
   * Get the colours of the default layout.
   *
   * @return The colours. Never {@code null}.
   */
  @NonNull
  public PdfPalette getPalette ()
  {
    return m_aPalette;
  }

  /**
   * Set the colours of the default layout. This registers the block styles of the default template
   * again and sets the header fonts of the section and the table headers in the new colour, so call
   * it before the block styles and fonts are changed.
   *
   * @param aPalette
   *        The colours. May not be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext setPalette (@NonNull final PdfPalette aPalette)
  {
    ValueEnforcer.notNull (aPalette, "Palette");
    m_aPalette = aPalette;
    m_aHeaderFont = DEFAULT_HEADER_FONT.getCloneWithDifferentColor (aPalette.getMuted ());
    m_aTableHeaderFont = DEFAULT_TABLE_HEADER_FONT.getCloneWithDifferentColor (aPalette.getMuted ());
    _registerDefaultStyles ();
    return this;
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

  private void _applyBlockStyle (@NonNull final String sName, @NonNull final PhloxThemeObject aStyle)
  {
    aStyle.checkKeys ("extends",
                      "reset",
                      "fill",
                      "text",
                      "padding",
                      "borderTop",
                      "borderBottom",
                      "fontSize",
                      "bold",
                      "widthPercent");
    // Start from the style of the same name, from the one that is named or from nothing
    PdfBlockStyle aStart = null;
    final String sExtends = aStyle.getString ("extends");
    if (sExtends != null)
    {
      aStart = getBlockStyle (sExtends);
      if (aStart == null)
        throw new PhloxThemeException ("Theme error at '" + aStyle.getPath () + ".extends': there is no block style '" + sExtends + "'");
    }
    else
      if (!Boolean.TRUE.equals (aStyle.getBoolean ("reset")))
        aStart = getBlockStyle (sName);
    final PdfBlockStyle.Builder aBuilder = aStart == null ? PdfBlockStyle.builder () : PdfBlockStyle.builder (aStart);

    final Color aFill = aStyle.getColor ("fill");
    if (aFill != null)
      aBuilder.fillColor (new PLColor (aFill.getRed (), aFill.getGreen (), aFill.getBlue ()));
    final Color aText = aStyle.getColor ("text");
    if (aText != null)
      aBuilder.textColor (new PLColor (aText.getRed (), aText.getGreen (), aText.getBlue ()));
    if (aStyle.getLength ("padding") != null)
      aBuilder.padding (aStyle.getLength ("padding").floatValue ());
    _setBorder (aBuilder, aStyle, "borderTop", true);
    _setBorder (aBuilder, aStyle, "borderBottom", false);
    if (aStyle.getFloat ("fontSize") != null)
      aBuilder.fontSize (aStyle.getFloat ("fontSize"));
    if (aStyle.getBoolean ("bold") != null)
      aBuilder.bold (aStyle.getBoolean ("bold").booleanValue ());
    if (aStyle.getFloat ("widthPercent") != null)
      aBuilder.widthPercent (aStyle.getFloat ("widthPercent"));
    putBlockStyle (sName, aBuilder.build ());
  }

  /**
   * Apply a theme: its <code>common</code> part, the colours (the variables
   * <code>--phlox-ink</code> and so on, see {@link PdfPalette}) and the <code>pdf</code> part. The
   * colours are applied first, so the block styles and fonts of the <code>pdf</code> part can refer
   * to them. Only the values that are in the theme are changed, except that the colours register
   * the block styles and the section header fonts of the default template again.
   * <p>
   * The keys of <code>pdf</code> are:
   * </p>
   * <ul>
   * <li><code>pageSize</code> - <code>a0</code> to <code>a6</code>, <code>letter</code> or
   * <code>legal</code>; <code>landscape</code> - <code>true</code> or <code>false</code></li>
   * <li><code>margins</code> - a length for all sides or an object with the keys <code>top</code>,
   * <code>right</code>, <code>bottom</code> and <code>left</code>. A length is a number of points or
   * a number with <code>mm</code>, <code>cm</code>, <code>in</code>, <code>pt</code> or
   * <code>px</code></li>
   * <li><code>bodyFont</code>, <code>headerFont</code>, <code>tableHeaderFont</code> - an object
   * with the keys <code>size</code>, <code>bold</code> and <code>color</code></li>
   * <li><code>blockStyles</code> - an object that maps the name of a block class of the template
   * (<code>{{class:name}}</code>, <code>{{group:name}}</code>) to an object with the keys
   * <code>fill</code>, <code>text</code>, <code>padding</code>, <code>borderTop</code> and
   * <code>borderBottom</code> (objects with <code>color</code> and <code>width</code>),
   * <code>fontSize</code>, <code>bold</code> and <code>widthPercent</code>. A style changes the
   * style of the same name; <code>extends</code> names the style to start from and
   * <code>reset: true</code> starts from nothing</li>
   * </ul>
   *
   * @param aTheme
   *        The theme. May not be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PdfRenderContext applyTheme (@NonNull final PhloxTheme aTheme)
  {
    ValueEnforcer.notNull (aTheme, "Theme");
    PhloxThemeApplier.applyCommon (this, aTheme);
    setPalette (PdfPalette.fromTheme (aTheme));

    final PhloxThemeObject aPdf = aTheme.getPdf ();
    aPdf.checkKeys ("pageSize", "landscape", "margins", "bodyFont", "headerFont", "tableHeaderFont", "blockStyles");

    final String sPageSize = aPdf.getString ("pageSize");
    PDRectangle aPageSize = sPageSize == null ? m_aPageSize : _getPageSize (aPdf, sPageSize);
    final Boolean aLandscape = aPdf.getBoolean ("landscape");
    if (aLandscape != null)
    {
      final boolean bIsLandscape = aPageSize.getWidth () > aPageSize.getHeight ();
      if (aLandscape.booleanValue () != bIsLandscape)
        aPageSize = new PDRectangle (aPageSize.getHeight (), aPageSize.getWidth ());
    }
    m_aPageSize = aPageSize;
    m_aMargins = _getMargins (aPdf, m_aMargins);
    m_aBodyFont = _getFont (aPdf, "bodyFont", m_aBodyFont);
    m_aHeaderFont = _getFont (aPdf, "headerFont", m_aHeaderFont);
    m_aTableHeaderFont = _getFont (aPdf, "tableHeaderFont", m_aTableHeaderFont);

    final PhloxThemeObject aStyles = aPdf.getObject ("blockStyles");
    if (aStyles != null)
      for (final String sName : aStyles.getKeys ())
      {
        final PhloxThemeObject aStyle = aStyles.getObject (sName);
        if (aStyle == null)
          throw new PhloxThemeException ("Theme error at '" + aStyles.getPath () + "." + sName + "': an object is expected");
        _applyBlockStyle (sName, aStyle);
      }
    return this;
  }
}
