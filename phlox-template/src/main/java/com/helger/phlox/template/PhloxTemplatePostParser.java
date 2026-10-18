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
package com.helger.phlox.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringReplace;
import com.helger.collection.commons.ICommonsList;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.AbstractRenderContext;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.model.PhloxSupportingDocument;
import com.helger.phlox.template.PhloxTemplateException.EStage;
import com.helger.phlox.template.adapter.PhloxInvoiceMustacheContext;
import com.helger.phlox.template.ast.PhloxGroupNode;
import com.helger.phlox.template.ast.PhloxPageBreakNode;
import com.helger.phlox.template.ast.PhloxResolvedImage;

/**
 * Walks the commonmark-java AST after parsing and performs the two phlox-specific transforms
 * described in {@code wiki/Template-Processing.md} § Stage 2:
 * <ol>
 * <li>Extracts {@code [[PHLOX:CLASS=name]]} sentinels from {@link Text} nodes and attaches the
 * class to the next sibling block via a {@code Map<Node, String>}.</li>
 * <li>Replaces {@code [[PHLOX:PAGEBREAK]]} sentinels with {@link PhloxPageBreakNode}.</li>
 * <li>Resolves {@link Image} {@code destination} URLs ({@code data:}, {@code attachment:logo},
 * {@code attachment:BG-24[i]}, {@code classpath:/…}) into {@link PhloxResolvedImage} entries.
 * Rejects {@code http:}, {@code https:}, {@code file:} schemes with
 * {@link PhloxTemplateException}.</li>
 * </ol>
 *
 * @author Philip Helger
 */
public final class PhloxTemplatePostParser
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PhloxTemplatePostParser.class);

  private static final Pattern CLASS_SENTINEL = Pattern.compile ("\\[\\[PHLOX:CLASS=([^\\]]+)\\]\\]");
  private static final Pattern GROUP_SENTINEL = Pattern.compile ("\\[\\[PHLOX:(GROUP|ROW)=([^\\]]+)\\]\\]");
  private static final String END_SENTINEL = PhloxInvoiceMustacheContext.SENTINEL_END;
  private static final String PAGEBREAK_SENTINEL = PhloxInvoiceMustacheContext.SENTINEL_PAGEBREAK;
  private static final Pattern ATTACHMENT_BG24_PATTERN = Pattern.compile ("attachment:BG-24\\[(\\d+)\\]");

  private final PhloxInvoice m_aInvoice;
  private final AbstractRenderContext <?> m_aContext;
  private final Map <Node, String> m_aBlockClassMap = new HashMap <> ();
  private final Map <Image, PhloxResolvedImage> m_aImageMap = new HashMap <> ();

  private final class ImageResolver extends AbstractVisitor
  {
    @Override
    public void visit (final Image aImage)
    {
      final String sDest = aImage.getDestination ();
      if (sDest == null || sDest.isEmpty ())
      {
        super.visit (aImage);
        return;
      }
      final PhloxResolvedImage aResolved = _resolveImage (sDest, aImage);
      if (aResolved != null)
        m_aImageMap.put (aImage, aResolved);
      super.visit (aImage);
    }
  }

  /**
   * Construct a post-parser bound to the given invoice and render context. Both are needed
   * for image-URL resolution (the invoice supplies BG-24 supporting docs, the render context
   * supplies the optional logo).
   *
   * @param aInvoice
   *        Source invoice.
   * @param aContext
   *        Render context.
   */
  public PhloxTemplatePostParser (@NonNull final PhloxInvoice aInvoice,
                                  @NonNull final AbstractRenderContext <?> aContext)
  {
    m_aInvoice = aInvoice;
    m_aContext = aContext;
  }

  private void _attachClassesTo (@NonNull final Node aTarget, @NonNull final List <String> aClasses)
  {
    if (aClasses.isEmpty ())
      return;
    final String sExisting = m_aBlockClassMap.get (aTarget);
    final String sJoined = String.join (" ", aClasses);
    m_aBlockClassMap.put (aTarget, sExisting == null ? sJoined : sExisting + " " + sJoined);
  }

  private void _processParagraph (@NonNull final Paragraph aParagraph)
  {
    final List <String> aClassesFound = new ArrayList <> ();
    boolean bHasPageBreak = false;
    boolean bHasNonSentinelContent = false;

    Node aInline = aParagraph.getFirstChild ();
    while (aInline != null)
    {
      final Node aNextInline = aInline.getNext ();
      if (aInline instanceof final Text aText)
      {
        final String sOriginal = aText.getLiteral ();
        final StringBuilder aRemaining = new StringBuilder ();
        final Matcher mClass = CLASS_SENTINEL.matcher (sOriginal);
        int nLastEnd = 0;
        while (mClass.find ())
        {
          aRemaining.append (sOriginal, nLastEnd, mClass.start ());
          aClassesFound.add (mClass.group (1));
          nLastEnd = mClass.end ();
        }
        aRemaining.append (sOriginal, nLastEnd, sOriginal.length ());

        String sCleaned = aRemaining.toString ();
        // Strip any PAGEBREAK sentinels too.
        if (sCleaned.contains (PAGEBREAK_SENTINEL))
        {
          bHasPageBreak = true;
          sCleaned = StringReplace.replaceAll (sCleaned, PAGEBREAK_SENTINEL, "");
        }

        if (sCleaned.isEmpty ())
          aText.unlink ();
        else
        {
          aText.setLiteral (sCleaned);
          if (!sCleaned.isBlank ())
            bHasNonSentinelContent = true;
        }
      }
      else
      {
        bHasNonSentinelContent = true;
      }
      aInline = aNextInline;
    }

    // Decide what to do with the paragraph now.
    if (bHasPageBreak && !bHasNonSentinelContent)
    {
      final PhloxPageBreakNode aBreak = new PhloxPageBreakNode ();
      aParagraph.insertBefore (aBreak);
      aParagraph.unlink ();
      _attachClassesTo (aBreak, aClassesFound);
      return;
    }

    if (!bHasNonSentinelContent && aParagraph.getFirstChild () == null)
    {
      // Paragraph is now empty; attach the collected classes to the next sibling block, if any.
      final Node aNextSibling = aParagraph.getNext ();
      aParagraph.unlink ();
      if (aNextSibling != null)
        _attachClassesTo (aNextSibling, aClassesFound);
      return;
    }

    // Paragraph still has content — keep it, attach classes to it directly.
    _attachClassesTo (aParagraph, aClassesFound);
  }

  /**
   * A line {@code {{class:name}}} inside a table is parsed by the GFM extension as a row whose first
   * cell holds the sentinel. Such a row carries the class of the row that follows it.
   *
   * @param aRow
   *        The table row to check. Never {@code null}.
   * @return {@code true} if the row only consists of class sentinels and was removed.
   */
  private boolean _processClassRow (@NonNull final TableRow aRow)
  {
    final List <String> aClassesFound = new ArrayList <> ();
    boolean bFirstCell = true;
    for (Node aCell = aRow.getFirstChild (); aCell instanceof TableCell; aCell = aCell.getNext ())
    {
      if (bFirstCell)
      {
        bFirstCell = false;
        for (Node aInline = aCell.getFirstChild (); aInline != null; aInline = aInline.getNext ())
        {
          if (!(aInline instanceof final Text aText))
            return false;
          final Matcher mClass = CLASS_SENTINEL.matcher (aText.getLiteral ());
          int nLastEnd = 0;
          while (mClass.find ())
          {
            if (!aText.getLiteral ().substring (nLastEnd, mClass.start ()).isBlank ())
              return false;
            aClassesFound.add (mClass.group (1));
            nLastEnd = mClass.end ();
          }
          if (!aText.getLiteral ().substring (nLastEnd).isBlank ())
            return false;
        }
      }
      else
        if (aCell.getFirstChild () != null)
          return false;
    }
    if (aClassesFound.isEmpty ())
      return false;

    final Node aNextRow = aRow.getNext ();
    if (aNextRow != null)
      _attachClassesTo (aNextRow, aClassesFound);
    aRow.unlink ();
    return true;
  }

  /**
   * @return The text of a paragraph that only consists of text, or <code>null</code>.
   */
  @Nullable
  private static String _getPlainText (@NonNull final Node aNode)
  {
    if (!(aNode instanceof Paragraph))
      return null;
    final StringBuilder aSB = new StringBuilder ();
    for (Node aChild = aNode.getFirstChild (); aChild != null; aChild = aChild.getNext ())
    {
      if (aChild instanceof final Text aText)
        aSB.append (aText.getLiteral ());
      else
        if (!(aChild instanceof SoftLineBreak))
          return null;
    }
    return aSB.toString ().strip ();
  }

  /**
   * Turn the paragraphs holding a group, row or end sentinel into {@link PhloxGroupNode}s that
   * contain the blocks up to the matching end. Only the top level of the document is handled.
   *
   * @param aRoot
   *        The document root. Never <code>null</code>.
   */
  private void _buildGroups (@NonNull final Node aRoot)
  {
    final List <Node> aChildren = new ArrayList <> ();
    boolean bAnyMarker = false;
    for (Node aChild = aRoot.getFirstChild (); aChild != null; aChild = aChild.getNext ())
    {
      aChildren.add (aChild);
      final String sText = _getPlainText (aChild);
      if (sText != null && (sText.equals (END_SENTINEL) || GROUP_SENTINEL.matcher (sText).matches ()))
        bAnyMarker = true;
    }
    if (!bAnyMarker)
      return;

    for (final Node aChild : aChildren)
      aChild.unlink ();

    final Deque <Node> aStack = new ArrayDeque <> ();
    Node aCurrent = aRoot;
    for (final Node aChild : aChildren)
    {
      final String sText = _getPlainText (aChild);
      final Matcher aMatcher = sText == null ? null : GROUP_SENTINEL.matcher (sText);
      if (aMatcher != null && aMatcher.matches ())
      {
        final PhloxGroupNode aGroup = new PhloxGroupNode ("ROW".equals (aMatcher.group (1)));
        m_aBlockClassMap.put (aGroup, aMatcher.group (2).strip ());
        aCurrent.appendChild (aGroup);
        aStack.push (aCurrent);
        aCurrent = aGroup;
      }
      else
        if (END_SENTINEL.equals (sText))
        {
          if (aStack.isEmpty ())
            LOGGER.warn ("Template tag '{{end}}' has no matching '{{group:...}}' or '{{row:...}}'");
          else
            aCurrent = aStack.pop ();
        }
        else
          aCurrent.appendChild (aChild);
    }
    if (!aStack.isEmpty ())
      LOGGER.warn ("Template has " + aStack.size () + " '{{group:...}}' or '{{row:...}}' without '{{end}}'");
  }

  private void _processSentinels (@NonNull final Node aRoot)
  {
    // Two-pass scan: collect candidate paragraphs first to avoid mutating during iteration.
    Node aChild = aRoot.getFirstChild ();
    while (aChild != null)
    {
      final Node aNext = aChild.getNext ();
      if (aChild instanceof final Paragraph aParagraph)
        _processParagraph (aParagraph);
      else
        if (aChild instanceof final TableRow aRow && _processClassRow (aRow))
        {
          // Removed - nothing to recurse into
        }
        else
          _processSentinels (aChild); // recurse into block containers (BlockQuote, ListItem, etc.)
      aChild = aNext;
    }
  }

  @Nullable
  private static String _firstTextOf (@NonNull final Image aImage)
  {
    Node aChild = aImage.getFirstChild ();
    while (aChild != null)
    {
      if (aChild instanceof final Text aText)
        return aText.getLiteral ();
      aChild = aChild.getNext ();
    }
    return null;
  }

  @Nullable
  private static String _guessMime (@NonNull final String sPath)
  {
    final String sLower = sPath.toLowerCase (Locale.ROOT);
    if (sLower.endsWith (".png"))
      return "image/png";
    if (sLower.endsWith (".jpg") || sLower.endsWith (".jpeg"))
      return "image/jpeg";
    if (sLower.endsWith (".gif"))
      return "image/gif";
    if (sLower.endsWith (".svg"))
      return "image/svg+xml";
    return null;
  }

  @Nullable
  private PhloxResolvedImage _resolveImage (@NonNull final String sDest, @NonNull final Image aImage)
  {
    final String sAlt = _firstTextOf (aImage);
    if (sDest.startsWith ("data:"))
    {
      // Pass-through: emitters consume the data URI verbatim. We do not eagerly decode here
      // so HTML can stream the URI straight into the <img src> attribute.
      return null;
    }
    if (sDest.equals ("attachment:logo"))
    {
      final PhloxAttachment aLogo = m_aContext.getLogo ();
      if (aLogo == null || aLogo.getBinary () == null)
      {
        LOGGER.warn ("Template references attachment:logo but RenderContext.getLogo() is null");
        return null;
      }
      return new PhloxResolvedImage (aLogo.getBinary (), aLogo.getMimeCode (), aLogo.getFilename (), sAlt);
    }
    final Matcher mBg24 = ATTACHMENT_BG24_PATTERN.matcher (sDest);
    if (mBg24.matches ())
    {
      final int nIndex = Integer.parseInt (mBg24.group (1));
      final ICommonsList <PhloxSupportingDocument> aDocs = m_aInvoice.supportingDocuments ();
      if (nIndex < 0 || nIndex >= aDocs.size ())
        throw new PhloxTemplateException (EStage.PARSE,
                                          sDest,
                                          "attachment:BG-24[" +
                                                 nIndex +
                                                 "] is out of range (have " +
                                                 aDocs.size () +
                                                 " supporting documents)");
      final PhloxAttachment aAtt = aDocs.get (nIndex).getAttachment ();
      if (aAtt == null || aAtt.getBinary () == null)
        throw new PhloxTemplateException (EStage.PARSE,
                                          sDest,
                                          "attachment:BG-24[" + nIndex + "] has no inline binary");
      return new PhloxResolvedImage (aAtt.getBinary (), aAtt.getMimeCode (), aAtt.getFilename (), sAlt);
    }
    if (sDest.startsWith ("classpath:"))
    {
      final String sPath = sDest.substring ("classpath:".length ());
      final ClassPathResource aRes = new ClassPathResource (sPath);
      if (!aRes.exists ())
        throw new PhloxTemplateException (EStage.PARSE, sDest, "classpath resource does not exist: " + sPath);
      try (InputStream aIS = aRes.getInputStream ())
      {
        if (aIS == null)
          throw new PhloxTemplateException (EStage.PARSE, sDest, "classpath resource has no stream");
        return new PhloxResolvedImage (aIS.readAllBytes (), _guessMime (sPath), sPath, sAlt);
      }
      catch (final IOException ex)
      {
        throw new PhloxTemplateException (EStage.PARSE, sDest, "failed to read classpath resource", ex);
      }
    }
    if (sDest.startsWith ("http:") || sDest.startsWith ("https:") || sDest.startsWith ("file:"))
      throw new PhloxTemplateException (EStage.PARSE,
                                        sDest,
                                        "image URL scheme rejected — writers do not fetch external resources");
    throw new PhloxTemplateException (EStage.PARSE,
                                      sDest,
                                      "unsupported image URL scheme; expected one of: data:, attachment:, classpath:");
  }

  /**
   * Run both post-passes against the document root.
   *
   * @param aRoot
   *        The document root. Never {@code null}.
   */
  public void process (@NonNull final Node aRoot)
  {
    _buildGroups (aRoot);
    _processSentinels (aRoot);
    aRoot.accept (new ImageResolver ());
  }

  /**
   * Get the sidecar map of block → CSS class assigned by {@code {{class:…}}} sentinels.
   *
   * @return Sidecar map of block → CSS class assigned by {@code {{class:…}}} sentinels.
   */
  @NonNull
  public Map <Node, String> getBlockClassMap ()
  {
    return m_aBlockClassMap;
  }

  /**
   * Get the sidecar map of {@link Image} node → resolved binary payload.
   *
   * @return Sidecar map of {@link Image} node → resolved binary payload.
   */
  @NonNull
  public Map <Image, PhloxResolvedImage> getImageMap ()
  {
    return m_aImageMap;
  }

  /**
   * Encode the given bytes as a {@code data:} URI. Static helper used by emitters that need to
   * inline a resolved image into an HTML {@code src} attribute.
   *
   * @param aImage
   *        The resolved image. Never {@code null}.
   * @return The {@code data:} URI. Never {@code null}.
   */
  @NonNull
  public static String toDataUri (@NonNull final PhloxResolvedImage aImage)
  {
    final String sMime = aImage.getMimeCode () == null ? "application/octet-stream" : aImage.getMimeCode ();
    return "data:" + sMime + ";base64," + Base64.getEncoder ().encodeToString (aImage.getBinary ());
  }
}
