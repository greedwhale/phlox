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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringReplace;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.pdflayout.base.IPLRenderableObject;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.box.PLBox;
import com.helger.pdflayout.element.hbox.PLHBox;
import com.helger.pdflayout.element.image.PLImage;
import com.helger.pdflayout.element.link.AbstractPLExternalLink;
import com.helger.pdflayout.element.list.BulletPointCreatorNumeric;
import com.helger.pdflayout.element.list.BulletPointCreatorSymbol;
import com.helger.pdflayout.element.list.IBulletPointCreator;
import com.helger.pdflayout.element.list.PLBulletPointList;
import com.helger.pdflayout.element.special.PLPageBreak;
import com.helger.pdflayout.element.special.PLSpacerX;
import com.helger.pdflayout.element.special.PLSpacerY;
import com.helger.pdflayout.element.table.PLTable;
import com.helger.pdflayout.element.table.PLTableCell;
import com.helger.pdflayout.element.text.PLText;
import com.helger.pdflayout.element.vbox.PLVBox;
import com.helger.pdflayout.richtext.element.PLRichText;
import com.helger.pdflayout.richtext.run.PLFontFamily;
import com.helger.pdflayout.spec.BorderStyleSpec;
import com.helger.pdflayout.spec.EHorzAlignment;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.PaddingSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.pdflayout.spec.WidthSpec;
import com.helger.phlox.template.ast.PhloxGroupNode;
import com.helger.phlox.template.ast.PhloxPageBreakNode;
import com.helger.phlox.template.ast.PhloxResolvedImage;
import com.helger.phlox.template.ast.PhloxTemplateAst;

/**
 * Turns the {@link PhloxTemplateAst} of a rendered template into the content of a PDF page set.
 * <p>
 * ph-pdf-layout itself has one style per text element. The inline nodes of a paragraph, heading or
 * table cell are therefore folded into the markup of {@code ph-pdf-layout-richtext}
 * (<code>**bold**</code>, <code>*italic*</code>, <code>~~strikethrough~~</code>,
 * <code>`code`</code> for the monospace font,
 * <code>{link[uri]}text{link}</code>), which {@code PLRichText} wraps and draws. Inline images are
 * skipped. A block class that was attached with
 * {@code {{class:name}}} is looked up with {@link PdfRenderContext#getBlockStyle(String)}.
 *
 * @author Philip Helger
 */
public final class PdfTemplateEmitter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PdfTemplateEmitter.class);

  private static final String LOGO_URL = "attachment:logo";
  private static final Pattern BR_TAG = Pattern.compile ("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
  private static final float PARAGRAPH_GAP = 4f;
  private static final float ROW_GAP = 14f;
  private static final PLColor HEADER_RULE = new PLColor (0x1d, 0x24, 0x33);
  private static final PLColor ROW_RULE = new PLColor (0xe4, 0xe7, 0xec);
  private static final float LOGO_HEIGHT = 40f;
  private static final float MAX_IMAGE_WIDTH = 200f;

  private final PdfRenderContext m_aCtx;

  /** <code>true</code> while the text of a heading or table header is set in capitals. */
  private boolean m_bUppercase;

  /** The width in points that is available for a block at the current position. */
  private float m_fContentWidth;

  /** The text colour of the enclosing group, if it has one. */
  private PLColor m_aInheritedTextColor;

  /**
   * Create a new emitter.
   *
   * @param aCtx
   *        The render context that provides the page size, the margins, the fonts and the block
   *        styles. Never {@code null}.
   */
  public PdfTemplateEmitter (@NonNull final PdfRenderContext aCtx)
  {
    m_aCtx = aCtx;
    m_fContentWidth = aCtx.getPageSize ().getWidth () - aCtx.getMargins ().getXSum ();
  }

  @NonNull
  private FontSpec _styled (@NonNull final FontSpec aFont, @Nullable final PdfBlockStyle aStyle)
  {
    FontSpec ret = aFont;
    final PLColor aColor = aStyle != null && aStyle.getTextColor () != null ? aStyle.getTextColor ()
                                                                            : m_aInheritedTextColor;
    if (aColor != null)
      ret = ret.getCloneWithDifferentColor (aColor);
    if (aStyle != null)
    {
      if (aStyle.getFontSize () != null)
        ret = ret.getCloneWithDifferentFontSize (aStyle.getFontSize ().floatValue ());
      if (aStyle.isBold ())
        ret = ret.getCloneWithDifferentFont (PreloadFont.REGULAR_BOLD);
    }
    return ret;
  }

  /** Apply the background, the padding and the lines of a block style to a box. */
  @NonNull
  private static PLBox _decorate (@NonNull final PLBox aBox, @Nullable final PdfBlockStyle aStyle)
  {
    if (aStyle == null)
      return aBox;
    if (aStyle.getFillColor () != null)
      aBox.setFillColor (aStyle.getFillColor ());
    if (aStyle.getPadding () > 0)
      aBox.setPadding (aStyle.getPadding ());
    else
      if (aStyle.getFillColor () != null)
        aBox.setPadding (2);
    if (aStyle.getBorderTopColor () != null)
      aBox.setBorderTop (new BorderStyleSpec (aStyle.getBorderTopColor (), aStyle.getBorderWidth ()));
    if (aStyle.getBorderBottomColor () != null)
      aBox.setBorderBottom (new BorderStyleSpec (aStyle.getBorderBottomColor (), aStyle.getBorderWidth ()));
    return aBox;
  }

  @Nullable
  private PdfBlockStyle _getStyle (@NonNull final PhloxTemplateAst aAst, @NonNull final Node aNode)
  {
    final String sBlockClass = aAst.getBlockClass (aNode);
    if (sBlockClass == null)
      return null;
    final PdfBlockStyle aStyle = m_aCtx.getBlockStyle (sBlockClass);
    if (aStyle == null)
      LOGGER.debug ("No PDF block style registered for template class '" + sBlockClass + "' - rendering it unstyled");
    return aStyle;
  }

  /**
   * PDF only allows links to a few URI schemes; other targets, like attachments, stay plain text.
   */
  private static boolean _isSupportedLink (@Nullable final String sDestination)
  {
    if (sDestination == null)
      return false;
    final int nColon = sDestination.indexOf (':');
    return nColon > 0 &&
           AbstractPLExternalLink.getAllowedURISchemes ()
                                 .contains (sDestination.substring (0, nColon).toLowerCase (Locale.ROOT));
  }

  @NonNull
  private static String _encodeLinkDestination (@NonNull final String sDestination)
  {
    String ret = StringReplace.replaceAll (sDestination, "[", "%5B");
    ret = StringReplace.replaceAll (ret, "]", "%5D");
    ret = StringReplace.replaceAll (ret, "{", "%7B");
    return StringReplace.replaceAll (ret, "}", "%7D");
  }

  /** Append the inline nodes as ph-pdf-layout markup; a line break starts a new line. */
  private void _appendMarkupRecursive (@NonNull final Node aParent, @NonNull final List <StringBuilder> aLines)
  {
    for (Node aChild = aParent.getFirstChild (); aChild != null; aChild = aChild.getNext ())
    {
      final StringBuilder aLine = aLines.get (aLines.size () - 1);
      if (aChild instanceof final Text aText)
        aLine.append (PdfTextSupport.escapeMarkup (PdfTextSupport.separateCurrencySymbol (m_bUppercase ? aText.getLiteral ().toUpperCase (m_aCtx.getLocale ()) : aText.getLiteral ())));
      else
        if (aChild instanceof SoftLineBreak || aChild instanceof HardLineBreak)
          aLines.add (new StringBuilder ());
        else
          if (aChild instanceof final Code aCode)
            aLine.append ('`').append (PdfTextSupport.escapeMarkup (aCode.getLiteral ())).append ('`');
          else
            if (aChild instanceof StrongEmphasis)
            {
              aLine.append ("**");
              _appendMarkupRecursive (aChild, aLines);
              aLines.get (aLines.size () - 1).append ("**");
            }
            else
              if (aChild instanceof Emphasis)
              {
                aLine.append ('*');
                _appendMarkupRecursive (aChild, aLines);
                aLines.get (aLines.size () - 1).append ('*');
              }
              else
                if (aChild instanceof Strikethrough)
                {
                  aLine.append ("~~");
                  _appendMarkupRecursive (aChild, aLines);
                  aLines.get (aLines.size () - 1).append ("~~");
                }
                else
                  if (aChild instanceof final Link aLink && _isSupportedLink (aLink.getDestination ()))
                  {
                    aLine.append ("{link[").append (PdfTextSupport.escapeMarkup (_encodeLinkDestination (aLink.getDestination ()))).append ("]}");
                    _appendMarkupRecursive (aChild, aLines);
                    aLines.get (aLines.size () - 1).append ("{link}");
                  }
                  else
                    if (aChild instanceof final HtmlInline aInline)
                    {
                      if (BR_TAG.matcher (aInline.getLiteral ()).matches ())
                        aLines.add (new StringBuilder ());
                    }
                    else
                      if (aChild instanceof Image)
                      {
                        // Inline images are not supported - only block level images are
                      }
                      else
                        // Links to unsupported targets and unknown inline nodes: keep the text
                        _appendMarkupRecursive (aChild, aLines);
    }
  }

  @NonNull
  private static PLFontFamily _fontFamily (@NonNull final FontSpec aBase)
  {
    if (aBase.getPreloadFont () == PreloadFont.REGULAR_BOLD)
      return new PLFontFamily (PreloadFont.REGULAR_BOLD,
                               PreloadFont.REGULAR_BOLD,
                               PreloadFont.REGULAR_BOLD_ITALIC,
                               PreloadFont.REGULAR_BOLD_ITALIC);
    return PLFontFamily.regular ();
  }

  /**
   * Create the content of a block of text (paragraph, heading, table cell) from its inline nodes.
   */
  @Nullable
  private IPLRenderableObject <?> _createInlineBlock (@NonNull final Node aParent,
                                                             @NonNull final FontSpec aBase,
                                                             @NonNull final EHorzAlignment eAlign,
                                                             final boolean bVertSplittable)
  {
    final ICommonsList <StringBuilder> aLines = new CommonsArrayList <> ();
    aLines.add (new StringBuilder ());
    _appendMarkupRecursive (aParent, aLines);

    final ICommonsList <String> aNonBlank = new CommonsArrayList <> ();
    for (final StringBuilder aLine : aLines)
    {
      final String sLine = aLine.toString ().strip ();
      if (!sLine.isEmpty ())
        aNonBlank.add (sLine);
    }
    if (aNonBlank.isEmpty ())
      return null;

    // A backslash in front of the line ending is a hard line break in the markup
    final PLRichText aRichText = PLRichText.createFromMarkup (String.join ("\\\n", aNonBlank),
                                                              _fontFamily (aBase),
                                                              aBase.getFontSize (),
                                                              aBase.getColor ());
    aRichText.setHorzAlign (eAlign);
    aRichText.setVertSplittable (bVertSplittable);
    return aRichText;
  }

  private void _addHeading (@NonNull final PhloxTemplateAst aAst,
                            @NonNull final Heading aHeading,
                            @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final PdfBlockStyle aStyle = _getStyle (aAst, aHeading);
    final FontSpec aBody = m_aCtx.getBodyFont ();
    final FontSpec aFont;
    final PLBox aBox;
    aFont = switch (aHeading.getLevel ())
    {
      case 1 -> new FontSpec (PreloadFont.REGULAR_BOLD, 18f);
      case 2 -> m_aCtx.getHeaderFont ();
      default -> aBody.getCloneWithDifferentFont (PreloadFont.REGULAR_BOLD)
                      .getCloneWithDifferentFontSize (aBody.getFontSize () + (aHeading.getLevel () == 3 ? 1f : 0f));
    };
    m_bUppercase = aHeading.getLevel () == 2;
    final IPLRenderableObject <?> aContent;
    try
    {
      aContent = _createInlineBlock (aHeading, _styled (aFont, aStyle), EHorzAlignment.LEFT, false);
    }
    finally
    {
      m_bUppercase = false;
    }
    if (aContent == null)
      return;
    aBox = new PLBox (aContent);
    if (aHeading.getLevel () == 1)
      aBox.setMarginBottom (6);
    else
      if (aHeading.getLevel () == 2)
        aBox.setMarginTop (8).setMarginBottom (3);
      else
        aBox.setMarginTop (3).setMarginBottom (2);
    aSink.accept (_decorate (aBox, aStyle));
  }

  @Nullable
  private static BufferedImage _readImage (@NonNull final PhloxResolvedImage aResolved)
  {
    try
    {
      return ImageIO.read (new NonBlockingByteArrayInputStream (aResolved.getBinary ()));
    }
    catch (final IOException ex)
    {
      return null;
    }
  }

  private static void _addImage (@NonNull final PhloxTemplateAst aAst,
                                 @NonNull final Image aImage,
                                 @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final PhloxResolvedImage aResolved = aAst.getResolvedImage (aImage);
    if (aResolved == null)
    {
      LOGGER.warn ("Template image '" + aImage.getDestination () + "' could not be resolved - skipping it");
      return;
    }
    final BufferedImage aBufferedImage = _readImage (aResolved);
    if (aBufferedImage == null)
    {
      LOGGER.warn ("Template image '" +
                   aImage.getDestination () +
                   "' is not a raster image PDF can embed - skipping it");
      return;
    }
    final float fRatio = (float) aBufferedImage.getWidth () / aBufferedImage.getHeight ();
    float fHeight = LOGO_URL.equals (aImage.getDestination ()) ? LOGO_HEIGHT : aBufferedImage.getHeight ();
    float fWidth = fHeight * fRatio;
    if (fWidth > MAX_IMAGE_WIDTH)
    {
      fWidth = MAX_IMAGE_WIDTH;
      fHeight = fWidth / fRatio;
    }
    aSink.accept (new PLImage (aBufferedImage, fWidth, fHeight).setMarginBottom (PARAGRAPH_GAP));
  }

  private void _addParagraph (@NonNull final PhloxTemplateAst aAst,
                              @NonNull final Paragraph aParagraph,
                              @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    // A paragraph that only holds an image is a block level image
    if (aParagraph.getFirstChild () instanceof final Image aImage && aImage.getNext () == null)
    {
      _addImage (aAst, aImage, aSink);
      return;
    }

    final PdfBlockStyle aStyle = _getStyle (aAst, aParagraph);
    final IPLRenderableObject <?> aContent = _createInlineBlock (aParagraph,
                                                                 _styled (m_aCtx.getBodyFont (), aStyle),
                                                                 EHorzAlignment.LEFT,
                                                                 true);
    if (aContent == null)
      return;
    aSink.accept (_decorate (new PLBox (aContent).setMarginBottom (PARAGRAPH_GAP), aStyle));
  }

  @NonNull
  private IPLRenderableObject <?> _createCell (@NonNull final TableCell aCell,
                                                      @NonNull final FontSpec aFont,
                                                      @NonNull final EHorzAlignment eAlign)
  {
    final IPLRenderableObject <?> aContent = _createInlineBlock (aCell, aFont, eAlign, true);
    if (aContent == null)
      return new PLSpacerX (1f);
    // The box aligns its content inside the width of the cell
    return new PLBox (aContent).setPadding (new PaddingSpec (3)).setHorzAlign (eAlign);
  }

  @NonNull
  private static EHorzAlignment _align (final TableCell.@Nullable Alignment eAlignment)
  {
    if (eAlignment == TableCell.Alignment.RIGHT)
      return EHorzAlignment.RIGHT;
    if (eAlignment == TableCell.Alignment.CENTER)
      return EHorzAlignment.CENTER;
    return EHorzAlignment.LEFT;
  }

  private static int _textLength (@NonNull final Node aNode)
  {
    int nLen = 0;
    for (Node aChild = aNode.getFirstChild (); aChild != null; aChild = aChild.getNext ())
      nLen += aChild instanceof final Text aText ? aText.getLiteral ().length () : _textLength (aChild);
    return nLen;
  }

  private static void _collectWordLengths (@NonNull final Node aNode, @NonNull final int [] aLongestWord)
  {
    for (Node aChild = aNode.getFirstChild (); aChild != null; aChild = aChild.getNext ())
    {
      if (aChild instanceof final Text aText)
      {
        for (final String sWord : StringHelper.getExploded (' ', aText.getLiteral ()))
          aLongestWord[0] = Math.max (aLongestWord[0], sWord.length ());
      }
      else
        _collectWordLengths (aChild, aLongestWord);
    }
  }

  /**
   * The columns get the width of their longest word first, so that amounts do not wrap. The rest
   * of the width is shared in proportion to the longest content of the columns, within sensible
   * limits.
   */
  @NonNull
  private float [] _columnWidths (@NonNull final TableBlock aTable, final int nColumns)
  {
    final float [] aWeights = new float [nColumns];
    final int [] aLongestWords = new int [nColumns];
    for (int i = 0; i < nColumns; ++i)
      aWeights[i] = 9f;
    for (Node aPart = aTable.getFirstChild (); aPart != null; aPart = aPart.getNext ())
      for (Node aRow = aPart.getFirstChild (); aRow != null; aRow = aRow.getNext ())
      {
        int nCol = 0;
        for (Node aCell = aRow.getFirstChild (); aCell != null && nCol < nColumns; aCell = aCell.getNext ())
        {
          aWeights[nCol] = Math.max (aWeights[nCol], Math.min (_textLength (aCell), 40));
          final int [] aWord = new int [1];
          _collectWordLengths (aCell, aWord);
          aLongestWords[nCol] = Math.max (aLongestWords[nCol], aWord[0]);
          nCol++;
        }
      }

    // Width in points that the longest word needs, plus the padding of the cell
    final float fCharWidth = m_aCtx.getBodyFont ().getFontSize () * 0.56f;
    final float [] aMin = new float [nColumns];
    float fMinSum = 0;
    float fWeightSum = 0;
    for (int i = 0; i < nColumns; ++i)
    {
      aMin[i] = aLongestWords[i] * fCharWidth + 8f;
      fMinSum += aMin[i];
      fWeightSum += aWeights[i];
    }
    final float fRest = Math.max (0f, m_fContentWidth - fMinSum);
    final float [] ret = new float [nColumns];
    float fSum = 0;
    for (int i = 0; i < nColumns; ++i)
    {
      ret[i] = aMin[i] + fRest * aWeights[i] / fWeightSum;
      fSum += ret[i];
    }
    for (int i = 0; i < nColumns; ++i)
      ret[i] = ret[i] * 100f / fSum;
    return ret;
  }

  private void _addTable (@NonNull final PhloxTemplateAst aAst,
                          @NonNull final TableBlock aTable,
                          @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final Node aHead = aTable.getFirstChild ();
    final Node aFirstRow = aHead == null ? null : aHead.getFirstChild ();
    if (aFirstRow == null)
      return;
    int nColumns = 0;
    for (Node aCell = aFirstRow.getFirstChild (); aCell != null; aCell = aCell.getNext ())
      nColumns++;
    if (nColumns == 0)
      return;

    final PLTable aTbl = PLTable.createWithPercentage (_columnWidths (aTable, nColumns));
    // A header row without any text (as in a table of key-value pairs) is not shown
    final boolean bShowHeader = _textLength (aFirstRow) > 0;
    if (bShowHeader)
    {
      // The header row is repeated on every page
      aTbl.setHeaderRowCount (1);
    }
    for (Node aPart = aTable.getFirstChild (); aPart != null; aPart = aPart.getNext ())
    {
      final boolean bHead = aPart instanceof TableHead;
      if (!bHead && !(aPart instanceof TableBody))
        continue;
      if (bHead && !bShowHeader)
        continue;
      for (Node aRowNode = aPart.getFirstChild (); aRowNode instanceof TableRow; aRowNode = aRowNode.getNext ())
      {
        final PdfBlockStyle aStyle = _getStyle (aAst, aRowNode);
        final FontSpec aBaseFont = bHead ? m_aCtx.getTableHeaderFont () : m_aCtx.getBodyFont ();
        final ICommonsList <PLTableCell> aCells = new CommonsArrayList <> ();
        for (Node aCellNode = aRowNode.getFirstChild (); aCellNode instanceof final TableCell aCell; aCellNode = aCellNode.getNext ())
        {
          m_bUppercase = bHead;
          final PLTableCell aPLCell;
          try
          {
            aPLCell = new PLTableCell (_createCell (aCell, _styled (aBaseFont, aStyle), _align (aCell.getAlignment ())));
          }
          finally
          {
            m_bUppercase = false;
          }
          if (bHead)
            aPLCell.setBorderBottom (new BorderStyleSpec (HEADER_RULE, 1f));
          else
            aPLCell.setBorderBottom (new BorderStyleSpec (ROW_RULE, 0.5f));
          if (aStyle != null && aStyle.getFillColor () != null)
            aPLCell.setFillColor (aStyle.getFillColor ());
          aCells.add (aPLCell);
        }
        // Rows may have fewer cells than the table has columns
        while (aCells.size () < nColumns)
          aCells.add (PLTableCell.createEmptyCell ());
        aTbl.addRow (aCells);
      }
    }
    aSink.accept (aTbl);
  }

  private void _addCodeBlock (@NonNull final String sLiteral, @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final FontSpec aFont = new FontSpec (PreloadFont.MONOSPACE, m_aCtx.getBodyFont ().getFontSize () - 1f);
    final PLVBox aLines = new PLVBox ();
    // The line breaks of the CommonMark literal are normalised to LF
    for (final String sLine : StringHelper.getExploded ('\n', sLiteral.stripTrailing ()))
      aLines.addRow (new PLText (sLine.isEmpty () ? " " : sLine, aFont));
    aSink.accept (new PLBox (aLines).setFillColor (PLColor.gray (0xf5)).setPadding (4).setMarginBottom (PARAGRAPH_GAP));
  }

  private void _addList (@NonNull final PhloxTemplateAst aAst,
                         @NonNull final Node aList,
                         @NonNull final IBulletPointCreator aCreator,
                         @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final PLBulletPointList aBullets = new PLBulletPointList (WidthSpec.abs (16f), aCreator);
    for (Node aItem = aList.getFirstChild (); aItem instanceof ListItem; aItem = aItem.getNext ())
    {
      final PLVBox aContent = new PLVBox ();
      _addBlocksRecursive (aAst, aItem, aContent::addRow);
      aBullets.addBulletPoint (aContent);
    }
    aSink.accept (new PLBox (aBullets).setMarginBottom (PARAGRAPH_GAP));
  }

  /**
   * Add the blocks of a group below each other, or the cells of a row side by side.
   */
  private void _addGroup (@NonNull final PhloxTemplateAst aAst,
                          @NonNull final PhloxGroupNode aGroup,
                          @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    final PdfBlockStyle aStyle = _getStyle (aAst, aGroup);
    final PLColor aOldColor = m_aInheritedTextColor;
    if (aStyle != null && aStyle.getTextColor () != null)
      m_aInheritedTextColor = aStyle.getTextColor ();
    try
    {
      final IPLRenderableObject <?> aContent;
      if (aGroup.isRow ())
      {
        // A row (parties, totals) is moved to the next page as a whole
        final PLHBox aRow = new PLHBox ().setVertSplittable (false);
        // Work out the width of the cells, so that the tables in them can plan their columns
        final float fRowWidth = m_fContentWidth;
        float fFixedPercent = 0;
        int nStarCells = 0;
        for (Node aCell = aGroup.getFirstChild (); aCell != null; aCell = aCell.getNext ())
        {
          final PdfBlockStyle aCellStyle = aCell instanceof PhloxGroupNode ? _getStyle (aAst, aCell) : null;
          if (aCellStyle != null && aCellStyle.getWidthPercent () != null)
            fFixedPercent += aCellStyle.getWidthPercent ().floatValue ();
          else
            nStarCells++;
        }
        for (Node aCell = aGroup.getFirstChild (); aCell != null; aCell = aCell.getNext ())
        {
          final PLVBox aCellContent = new PLVBox ();
          final PdfBlockStyle aWidthStyle = aCell instanceof PhloxGroupNode ? _getStyle (aAst, aCell) : null;
          final float fCellPercent = aWidthStyle != null && aWidthStyle.getWidthPercent () != null ? aWidthStyle.getWidthPercent ()
                                                                                                    .floatValue ()
                                                                                                  : (100f - fFixedPercent) /
                                                                                                    Math.max (1, nStarCells);
          m_fContentWidth = fRowWidth * fCellPercent / 100f - ROW_GAP;
          if (aCell instanceof final PhloxGroupNode aCellGroup && !aCellGroup.isRow ())
            _addBlocksRecursive (aAst, aCellGroup, aCellContent::addRow);
          else
            _addBlock (aAst, aCell, aCellContent::addRow);
          m_fContentWidth = fRowWidth;
          final PdfBlockStyle aCellStyle = aCell instanceof PhloxGroupNode ? _getStyle (aAst, aCell) : null;
          final PLBox aCellBox = _decorate (new PLBox (aCellContent), aCellStyle);
          if (aCell.getNext () != null)
            aCellBox.setMarginRight (ROW_GAP);
          final Float aWidth = aCellStyle == null ? null : aCellStyle.getWidthPercent ();
          aRow.addColumn (aCellBox, aWidth == null ? WidthSpec.star () : WidthSpec.perc (aWidth.floatValue ()));
        }
        aContent = aRow;
      }
      else
      {
        final PLVBox aBlocks = new PLVBox ();
        _addBlocksRecursive (aAst, aGroup, aBlocks::addRow);
        aContent = aBlocks;
      }
      aSink.accept (_decorate (new PLBox (aContent).setMarginBottom (PARAGRAPH_GAP), aGroup.isRow () ? null : aStyle));
    }
    finally
    {
      m_aInheritedTextColor = aOldColor;
    }
  }

  private void _addBlock (@NonNull final PhloxTemplateAst aAst,
                          @NonNull final Node aNode,
                          @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    if (aNode instanceof final PhloxGroupNode aGroup)
    {
      _addGroup (aAst, aGroup, aSink);
      return;
    }
      if (aNode instanceof final Heading aHeading)
      _addHeading (aAst, aHeading, aSink);
    else
      if (aNode instanceof final Paragraph aParagraph)
        _addParagraph (aAst, aParagraph, aSink);
      else
        if (aNode instanceof final TableBlock aTable)
          _addTable (aAst, aTable, aSink);
        else
          if (aNode instanceof BulletList)
            _addList (aAst,
                      aNode,
                      BulletPointCreatorSymbol.createFilledDot (m_aCtx.getBodyFont ().getFontSize ()),
                      aSink);
          else
            if (aNode instanceof final OrderedList aList)
            {
              final int nStart = aList.getMarkerStartNumber () == null ? 1
                                                                       : aList.getMarkerStartNumber ().intValue ();
              _addList (aAst,
                        aList,
                        new BulletPointCreatorNumeric (nIndex -> (nStart + nIndex) + ".", m_aCtx.getBodyFont ()),
                        aSink);
            }
            else
              if (aNode instanceof BlockQuote)
              {
                final PLVBox aContent = new PLVBox ();
                _addBlocksRecursive (aAst, aNode, aContent::addRow);
                aSink.accept (new PLBox (aContent).setBorderLeft (new BorderStyleSpec (PLColor.GRAY, 2f))
                                                  .setPaddingLeft (6)
                                                  .setMarginBottom (PARAGRAPH_GAP));
              }
              else
                if (aNode instanceof final FencedCodeBlock aBlock)
                  _addCodeBlock (aBlock.getLiteral (), aSink);
                else
                  if (aNode instanceof final IndentedCodeBlock aBlock)
                    _addCodeBlock (aBlock.getLiteral (), aSink);
                  else
                    if (aNode instanceof ThematicBreak)
                    {
                      final PdfBlockStyle aRuleStyle = _getStyle (aAst, aNode);
                      final boolean bStyled = aRuleStyle != null && aRuleStyle.getBorderTopColor () != null;
                      aSink.accept (new PLBox (new PLSpacerY (1f)).setBorderTop (new BorderStyleSpec (bStyled ? aRuleStyle.getBorderTopColor ()
                                                                                                              : PLColor.GRAY,
                                                                                                      bStyled ? aRuleStyle.getBorderWidth ()
                                                                                                              : 0.75f))
                                                                  .setMarginY (4));
                    }
                    else
                      if (aNode instanceof PhloxPageBreakNode)
                        aSink.accept (new PLPageBreak (true));
                      else
                        if (aNode instanceof final Image aImage)
                          _addImage (aAst, aImage, aSink);
                        else
                          if (aNode instanceof final HtmlBlock aHtml)
                            aSink.accept (new PLText (aHtml.getLiteral ().strip (), m_aCtx.getBodyFont ()));
                          else
                          {
                            LOGGER.warn ("Unsupported template node " + aNode.getClass ().getSimpleName ());
                            _addBlocksRecursive (aAst, aNode, aSink);
                          }
  }

  private void _addBlocksRecursive (@NonNull final PhloxTemplateAst aAst,
                                    @NonNull final Node aParent,
                                    @NonNull final Consumer <IPLRenderableObject <?>> aSink)
  {
    for (Node aNode = aParent.getFirstChild (); aNode != null; aNode = aNode.getNext ())
      _addBlock (aAst, aNode, aSink);
  }

  /**
   * Convert the template AST to the content of a PDF page set.
   *
   * @param aAst
   *        The rendered template. Never {@code null}.
   * @return The page set with the size and the margins of the render context. Never {@code null}.
   */
  @NonNull
  public PLPageSet emit (@NonNull final PhloxTemplateAst aAst)
  {
    final PLPageSet aPS = new PLPageSet (m_aCtx.getPageSize ()).setMargin (m_aCtx.getMargins ());
    _addBlocksRecursive (aAst, aAst.getRoot (), aPS::addElement);
    return aPS;
  }
}
