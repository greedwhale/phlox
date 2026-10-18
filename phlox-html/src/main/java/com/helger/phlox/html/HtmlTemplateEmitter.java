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

import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

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

import com.helger.css.property.CCSSProperties;
import com.helger.html.hc.IHCNodeWithChildren;
import com.helger.html.hc.html.IHCElement;
import com.helger.html.hc.html.embedded.HCImg;
import com.helger.html.hc.html.grouping.HCBlockQuote;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCHR;
import com.helger.html.hc.html.grouping.HCOL;
import com.helger.html.hc.html.grouping.HCP;
import com.helger.html.hc.html.grouping.HCPre;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.grouping.IHCList;
import com.helger.html.hc.html.root.HCHtml;
import com.helger.html.hc.html.sections.HCH1;
import com.helger.html.hc.html.sections.HCH2;
import com.helger.html.hc.html.sections.HCH3;
import com.helger.html.hc.html.sections.HCH4;
import com.helger.html.hc.html.sections.HCH5;
import com.helger.html.hc.html.sections.HCH6;
import com.helger.html.hc.html.tabular.HCRow;
import com.helger.html.hc.html.tabular.HCTable;
import com.helger.html.hc.html.tabular.IHCCell;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.html.textlevel.HCBR;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCEM;
import com.helger.html.hc.html.textlevel.HCS;
import com.helger.html.hc.html.textlevel.HCStrong;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.phlox.template.ast.PhloxGroupNode;
import com.helger.phlox.template.ast.PhloxPageBreakNode;
import com.helger.phlox.template.ast.PhloxResolvedImage;
import com.helger.phlox.template.ast.PhloxTemplateAst;
import com.helger.url.SimpleURL;

/**
 * Turns the {@link PhloxTemplateAst} of a rendered template into an HTML document. The document
 * head, the watermarks and the page wrapper are the same as in {@link HtmlInvoiceWriter}; the
 * content is built by walking the CommonMark tree. A block class that was attached with
 * {@code {{class:name}}} is added to the emitted element, prefixed with
 * {@link HtmlRenderContext#getCssClassPrefix()}.
 *
 * @author Philip Helger
 */
public final class HtmlTemplateEmitter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HtmlTemplateEmitter.class);

  private static final String LOGO_URL = "attachment:logo";

  private static final Pattern BR_TAG = Pattern.compile ("<br\\s*/?>", Pattern.CASE_INSENSITIVE);

  private final HtmlRenderContext m_aCtx;

  /**
   * Create a new emitter.
   *
   * @param aCtx
   *        The render context that provides the CSS class prefix, the stylesheet options and the
   *        watermarks. Never {@code null}.
   */
  public HtmlTemplateEmitter (@NonNull final HtmlRenderContext aCtx)
  {
    m_aCtx = aCtx;
  }

  private void _addClass (@NonNull final PhloxTemplateAst aAst,
                          @NonNull final Node aNode,
                          @NonNull final IHCElement <?> aElement)
  {
    final String sBlockClass = aAst.getBlockClass (aNode);
    if (sBlockClass != null)
      aElement.addClass (HtmlDocumentSupport.cls (m_aCtx.getCssClassPrefix () + sBlockClass));
  }

  @Nullable
  private HCImg _createImage (@NonNull final PhloxTemplateAst aAst, @NonNull final Image aImage)
  {
    final PhloxResolvedImage aResolved = aAst.getResolvedImage (aImage);
    final String sSrc;
    if (aResolved != null)
      sSrc = "data:" +
             (aResolved.getMimeCode () == null ? "application/octet-stream" : aResolved.getMimeCode ()) +
             ";base64," +
             Base64.getEncoder ().encodeToString (aResolved.getBinary ());
    else
    {
      if (LOGO_URL.equals (aImage.getDestination ()) || aImage.getDestination ().startsWith ("attachment:"))
      {
        // Nothing to show for an attachment that could not be resolved
        LOGGER.warn ("Template image '" + aImage.getDestination () + "' could not be resolved - skipping it");
        return null;
      }
      sSrc = aImage.getDestination ();
    }
    final HCImg aImg = new HCImg ().setSrc (new SimpleURL (sSrc));
    if (aResolved != null && aResolved.getAltText () != null)
      aImg.setAlt (aResolved.getAltText ());
    if (LOGO_URL.equals (aImage.getDestination ()))
      aImg.addClass (HtmlDocumentSupport.cls (m_aCtx.getCssClassPrefix () + "logo"));
    return aImg;
  }

  private static boolean _isSafeLink (@Nullable final String sDestination)
  {
    if (sDestination == null)
      return false;
    final int nColon = sDestination.indexOf (':');
    if (nColon < 0)
      return true;
    final String sScheme = sDestination.substring (0, nColon).toLowerCase (Locale.ROOT);
    return sScheme.equals ("http") || sScheme.equals ("https") || sScheme.equals ("mailto");
  }

  private void _addChildrenRecursive (@NonNull final PhloxTemplateAst aAst,
                                      @NonNull final Node aParent,
                                      @NonNull final IHCNodeWithChildren <?> aTarget)
  {
    for (Node aChild = aParent.getFirstChild (); aChild != null; aChild = aChild.getNext ())
      _emitNodeRecursive (aAst, aChild, aTarget);
  }

  private void _emitHeading (@NonNull final PhloxTemplateAst aAst,
                             @NonNull final Heading aHeading,
                             @NonNull final IHCNodeWithChildren <?> aTarget)
  {
    final String sPrefix = m_aCtx.getCssClassPrefix ();
    final IHCElement <?> aElement;
    final IHCNodeWithChildren <?> aContent;
    switch (aHeading.getLevel ())
    {
      case 1:
      {
        final HCH1 aH = new HCH1 ().addClass (HtmlDocumentSupport.cls (sPrefix + "title"));
        aElement = aH;
        aContent = aH;
        break;
      }
      case 2:
      {
        final HCH2 aH = new HCH2 ().addClass (HtmlDocumentSupport.cls (sPrefix + "section-title"));
        aElement = aH;
        aContent = aH;
        break;
      }
      case 3:
      {
        final HCH3 aH = new HCH3 ().addClass (HtmlDocumentSupport.cls (sPrefix + "subsection-title"));
        aElement = aH;
        aContent = aH;
        break;
      }
      case 4:
      {
        final HCH4 aH = new HCH4 ();
        aElement = aH;
        aContent = aH;
        break;
      }
      case 5:
      {
        final HCH5 aH = new HCH5 ();
        aElement = aH;
        aContent = aH;
        break;
      }
      default:
      {
        final HCH6 aH = new HCH6 ();
        aElement = aH;
        aContent = aH;
        break;
      }
    }
    _addClass (aAst, aHeading, aElement);
    _addChildrenRecursive (aAst, aHeading, aContent);
    aTarget.addChild (aElement);
  }

  private void _emitTable (@NonNull final PhloxTemplateAst aAst,
                           @NonNull final TableBlock aTable,
                           @NonNull final IHCNodeWithChildren <?> aTarget)
  {
    final HCTable aTbl = new HCTable ().addClass (HtmlDocumentSupport.cls (m_aCtx.getCssClassPrefix () + "table"));
    _addClass (aAst, aTable, aTbl);
    for (Node aPart = aTable.getFirstChild (); aPart != null; aPart = aPart.getNext ())
    {
      final boolean bHead = aPart instanceof TableHead;
      if (!bHead && !(aPart instanceof TableBody))
        continue;

      for (Node aRowNode = aPart.getFirstChild (); aRowNode instanceof TableRow; aRowNode = aRowNode.getNext ())
      {
        final HCRow aRow = bHead ? aTbl.getHead ().addRow () : aTbl.getBody ().addRow ();
        _addClass (aAst, aRowNode, aRow);
        for (Node aCellNode = aRowNode.getFirstChild (); aCellNode instanceof final TableCell aCell; aCellNode = aCellNode.getNext ())
        {
          final IHCCell <?> aHtmlCell = aCell.isHeader () ? aRow.addTH () : aRow.addTD ();
          if (aCell.getAlignment () != null)
            aHtmlCell.addStyle (CCSSProperties.TEXT_ALIGN.newValue (aCell.getAlignment ()
                                                                         .name ()
                                                                         .toLowerCase (Locale.ROOT)));
          _addChildrenRecursive (aAst, aCell, aHtmlCell);
        }
      }
    }
    aTarget.addChild (aTbl);
  }

  private void _emitList (@NonNull final PhloxTemplateAst aAst,
                          @NonNull final Node aList,
                          @NonNull final IHCList <?, ?> aHtmlList,
                          @NonNull final IHCNodeWithChildren <?> aTarget)
  {
    _addClass (aAst, aList, aHtmlList);
    for (Node aItem = aList.getFirstChild (); aItem instanceof ListItem; aItem = aItem.getNext ())
      _addChildrenRecursive (aAst, aItem, aHtmlList.addItem ());
    aTarget.addChild (aHtmlList);
  }

  private void _emitNodeRecursive (@NonNull final PhloxTemplateAst aAst,
                                   @NonNull final Node aNode,
                                   @NonNull final IHCNodeWithChildren <?> aTarget)
  {
    if (aNode instanceof final Text aText)
    {
      aTarget.addChild (new HCTextNode (aText.getLiteral ()));
      return;
    }
    if (aNode instanceof SoftLineBreak || aNode instanceof HardLineBreak)
    {
      // A line break in the template is a line break in the output
      aTarget.addChild (new HCBR ());
      return;
    }
    if (aNode instanceof final Heading aHeading)
    {
      _emitHeading (aAst, aHeading, aTarget);
      return;
    }
    if (aNode instanceof final Paragraph aParagraph)
    {
      final HCP aP = new HCP ();
      _addClass (aAst, aParagraph, aP);
      _addChildrenRecursive (aAst, aParagraph, aP);
      aTarget.addChild (aP);
      return;
    }
    if (aNode instanceof StrongEmphasis)
    {
      final HCStrong aElement = new HCStrong ();
      _addChildrenRecursive (aAst, aNode, aElement);
      aTarget.addChild (aElement);
      return;
    }
    if (aNode instanceof Emphasis)
    {
      final HCEM aElement = new HCEM ();
      _addChildrenRecursive (aAst, aNode, aElement);
      aTarget.addChild (aElement);
      return;
    }
    if (aNode instanceof Strikethrough)
    {
      final HCS aElement = new HCS ();
      _addChildrenRecursive (aAst, aNode, aElement);
      aTarget.addChild (aElement);
      return;
    }
    if (aNode instanceof final Code aCode)
    {
      aTarget.addChild (new HCCode ().addChild (aCode.getLiteral ()));
      return;
    }
    if (aNode instanceof final Link aLink)
    {
      if (_isSafeLink (aLink.getDestination ()))
      {
        final HCA aA = new HCA (new SimpleURL (aLink.getDestination ()));
        _addChildrenRecursive (aAst, aLink, aA);
        aTarget.addChild (aA);
      }
      else
        _addChildrenRecursive (aAst, aLink, aTarget);
      return;
    }
    if (aNode instanceof BulletList)
    {
      _emitList (aAst, aNode, new HCUL (), aTarget);
      return;
    }
    if (aNode instanceof final OrderedList aList)
    {
      final HCOL aOL = new HCOL ();
      if (aList.getMarkerStartNumber () != null && aList.getMarkerStartNumber ().intValue () != 1)
        aOL.setStart (aList.getMarkerStartNumber ());
      _emitList (aAst, aList, aOL, aTarget);
      return;
    }
    if (aNode instanceof BlockQuote)
    {
      final HCBlockQuote aElement = new HCBlockQuote ();
      _addClass (aAst, aNode, aElement);
      _addChildrenRecursive (aAst, aNode, aElement);
      aTarget.addChild (aElement);
      return;
    }
    if (aNode instanceof final FencedCodeBlock aBlock)
    {
      aTarget.addChild (new HCPre ().addChild (new HCCode ().addChild (aBlock.getLiteral ())));
      return;
    }
    if (aNode instanceof final IndentedCodeBlock aBlock)
    {
      aTarget.addChild (new HCPre ().addChild (new HCCode ().addChild (aBlock.getLiteral ())));
      return;
    }
    if (aNode instanceof ThematicBreak)
    {
      final HCHR aHR = new HCHR ();
      _addClass (aAst, aNode, aHR);
      aTarget.addChild (aHR);
      return;
    }
    if (aNode instanceof final TableBlock aTable)
    {
      _emitTable (aAst, aTable, aTarget);
      return;
    }
    if (aNode instanceof final Image aImage)
    {
      final HCImg aImg = _createImage (aAst, aImage);
      if (aImg != null)
        aTarget.addChild (aImg);
      return;
    }
    if (aNode instanceof final PhloxGroupNode aGroup)
    {
      final HCDiv aDiv = new HCDiv ();
      if (aGroup.isRow ())
        aDiv.addClass (HtmlDocumentSupport.cls (m_aCtx.getCssClassPrefix () + "row"));
      _addClass (aAst, aGroup, aDiv);
      _addChildrenRecursive (aAst, aGroup, aDiv);
      aTarget.addChild (aDiv);
      return;
    }
    if (aNode instanceof PhloxPageBreakNode)
    {
      aTarget.addChild (new HCDiv ().addStyle (CCSSProperties.PAGE_BREAK_AFTER.newValue ("always")));
      return;
    }
    // Raw HTML of a template is never passed through, except for a line break, which is the only
    // way
    // to break a line inside a table cell
    if (aNode instanceof final HtmlInline aInline && BR_TAG.matcher (aInline.getLiteral ()).matches ())
    {
      aTarget.addChild (new HCBR ());
      return;
    }
    if (aNode instanceof final HtmlBlock aHtml)
    {
      aTarget.addChild (aHtml.getLiteral ());
      return;
    }
    if (aNode instanceof final HtmlInline aHtml)
    {
      aTarget.addChild (aHtml.getLiteral ());
      return;
    }

    // Unknown container: keep its content
    LOGGER.warn ("Unsupported template node " + aNode.getClass ().getSimpleName ());
    _addChildrenRecursive (aAst, aNode, aTarget);
  }

  /**
   * Convert the template AST to an HTML document.
   *
   * @param aAst
   *        The rendered template. Never {@code null}.
   * @return The populated {@link HCHtml}. Never {@code null}.
   */
  @NonNull
  public HCHtml emit (@NonNull final PhloxTemplateAst aAst)
  {
    final HCHtml aHtml = new HCHtml ();
    final HCDiv aPage = HtmlDocumentSupport.createPage (aHtml,
                                                        HtmlDocumentSupport.titleString (aAst.getInvoice (), m_aCtx),
                                                        m_aCtx);
    _addChildrenRecursive (aAst, aAst.getRoot (), aPage);
    return aHtml;
  }
}
