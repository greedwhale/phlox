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
package com.helger.norma.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringReplace;
import com.helger.collection.commons.ICommonsList;
import com.helger.io.resource.ClassPathResource;
import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.model.NormaAttachment;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.api.model.NormaSupportingDocument;
import com.helger.norma.template.NormaTemplateException.EStage;
import com.helger.norma.template.adapter.NormaInvoiceMustacheContext;
import com.helger.norma.template.ast.NormaPageBreakNode;
import com.helger.norma.template.ast.NormaResolvedImage;

/**
 * Walks the commonmark-java AST after parsing and performs the two norma-specific transforms
 * described in {@code wiki/Template-Processing.md} § Stage 2:
 * <ol>
 * <li>Extracts {@code [[NORMA:CLASS=name]]} sentinels from {@link Text} nodes and attaches the
 * class to the next sibling block via a {@code Map<Node, String>}.</li>
 * <li>Replaces {@code [[NORMA:PAGEBREAK]]} sentinels with {@link NormaPageBreakNode}.</li>
 * <li>Resolves {@link Image} {@code destination} URLs ({@code data:}, {@code attachment:logo},
 * {@code attachment:BG-24[i]}, {@code classpath:/…}) into {@link NormaResolvedImage} entries.
 * Rejects {@code http:}, {@code https:}, {@code file:} schemes with
 * {@link NormaTemplateException}.</li>
 * </ol>
 *
 * @author Philip Helger
 */
public final class NormaTemplatePostParser
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NormaTemplatePostParser.class);

  private static final Pattern CLASS_SENTINEL = Pattern.compile ("\\[\\[NORMA:CLASS=([^\\]]+)\\]\\]");
  private static final String PAGEBREAK_SENTINEL = NormaInvoiceMustacheContext.SENTINEL_PAGEBREAK;
  private static final Pattern ATTACHMENT_BG24_PATTERN = Pattern.compile ("attachment:BG-24\\[(\\d+)\\]");

  private final NormaInvoice m_aInvoice;
  private final AbstractRenderContext <?> m_aContext;
  private final Map <Node, String> m_aBlockClassMap = new HashMap <> ();
  private final Map <Image, NormaResolvedImage> m_aImageMap = new HashMap <> ();

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
  public NormaTemplatePostParser (@NonNull final NormaInvoice aInvoice,
                                  @NonNull final AbstractRenderContext <?> aContext)
  {
    m_aInvoice = aInvoice;
    m_aContext = aContext;
  }

  /** Run both post-passes against the document root. */
  public void process (@NonNull final Node aRoot)
  {
    _processSentinels (aRoot);
    aRoot.accept (new ImageResolver ());
  }

  /** @return Sidecar map of block → CSS class assigned by {@code {{class:…}}} sentinels. */
  @NonNull
  public Map <Node, String> getBlockClassMap ()
  {
    return m_aBlockClassMap;
  }

  /** @return Sidecar map of {@link Image} node → resolved binary payload. */
  @NonNull
  public Map <Image, NormaResolvedImage> getImageMap ()
  {
    return m_aImageMap;
  }

  // ─── Sentinel extraction ──────────────────────────────────────────────
  private void _processSentinels (@NonNull final Node aRoot)
  {
    // Two-pass scan: collect candidate paragraphs first to avoid mutating during iteration.
    Node aChild = aRoot.getFirstChild ();
    while (aChild != null)
    {
      final Node aNext = aChild.getNext ();
      if (aChild instanceof Paragraph)
        _processParagraph ((Paragraph) aChild);
      else
        _processSentinels (aChild); // recurse into block containers (BlockQuote, ListItem, etc.)
      aChild = aNext;
    }
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
      if (aInline instanceof Text)
      {
        final Text aText = (Text) aInline;
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
      final NormaPageBreakNode aBreak = new NormaPageBreakNode ();
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

  private void _attachClassesTo (@NonNull final Node aTarget, @NonNull final List <String> aClasses)
  {
    if (aClasses.isEmpty ())
      return;
    final String sExisting = m_aBlockClassMap.get (aTarget);
    final String sJoined = String.join (" ", aClasses);
    m_aBlockClassMap.put (aTarget, sExisting == null ? sJoined : sExisting + " " + sJoined);
  }

  // ─── Image URL resolution ─────────────────────────────────────────────
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
      final NormaResolvedImage aResolved = _resolveImage (sDest, aImage);
      if (aResolved != null)
        m_aImageMap.put (aImage, aResolved);
      super.visit (aImage);
    }
  }

  @Nullable
  private NormaResolvedImage _resolveImage (@NonNull final String sDest, @NonNull final Image aImage)
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
      final NormaAttachment aLogo = m_aContext.getLogo ();
      if (aLogo == null || aLogo.getBinary () == null)
      {
        LOGGER.warn ("Template references attachment:logo but RenderContext.getLogo() is null");
        return null;
      }
      return new NormaResolvedImage (aLogo.getBinary (), aLogo.getMimeCode (), aLogo.getFilename (), sAlt);
    }
    final Matcher mBG24 = ATTACHMENT_BG24_PATTERN.matcher (sDest);
    if (mBG24.matches ())
    {
      final int nIndex = Integer.parseInt (mBG24.group (1));
      final ICommonsList <NormaSupportingDocument> aDocs = m_aInvoice.supportingDocuments ();
      if (nIndex < 0 || nIndex >= aDocs.size ())
        throw new NormaTemplateException (EStage.PARSE,
                                          sDest,
                                          "attachment:BG-24[" +
                                                 nIndex +
                                                 "] is out of range (have " +
                                                 aDocs.size () +
                                                 " supporting documents)");
      final NormaAttachment aAtt = aDocs.get (nIndex).getAttachment ();
      if (aAtt == null || aAtt.getBinary () == null)
        throw new NormaTemplateException (EStage.PARSE,
                                          sDest,
                                          "attachment:BG-24[" + nIndex + "] has no inline binary");
      return new NormaResolvedImage (aAtt.getBinary (), aAtt.getMimeCode (), aAtt.getFilename (), sAlt);
    }
    if (sDest.startsWith ("classpath:"))
    {
      final String sPath = sDest.substring ("classpath:".length ());
      final ClassPathResource aRes = new ClassPathResource (sPath);
      if (!aRes.exists ())
        throw new NormaTemplateException (EStage.PARSE, sDest, "classpath resource does not exist: " + sPath);
      try (InputStream aIS = aRes.getInputStream ())
      {
        if (aIS == null)
          throw new NormaTemplateException (EStage.PARSE, sDest, "classpath resource has no stream");
        return new NormaResolvedImage (aIS.readAllBytes (), _guessMime (sPath), sPath, sAlt);
      }
      catch (final IOException ex)
      {
        throw new NormaTemplateException (EStage.PARSE, sDest, "failed to read classpath resource", ex);
      }
    }
    if (sDest.startsWith ("http:") || sDest.startsWith ("https:") || sDest.startsWith ("file:"))
      throw new NormaTemplateException (EStage.PARSE,
                                        sDest,
                                        "image URL scheme rejected — writers do not fetch external resources");
    throw new NormaTemplateException (EStage.PARSE,
                                      sDest,
                                      "unsupported image URL scheme; expected one of: data:, attachment:, classpath:");
  }

  @Nullable
  private static String _firstTextOf (@NonNull final Image aImage)
  {
    Node aChild = aImage.getFirstChild ();
    while (aChild != null)
    {
      if (aChild instanceof Text)
        return ((Text) aChild).getLiteral ();
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

  /**
   * Encode the given bytes as a {@code data:} URI. Static helper used by emitters that need to
   * inline a resolved image into an HTML {@code src} attribute.
   */
  @NonNull
  public static String toDataUri (@NonNull final NormaResolvedImage aImage)
  {
    final String sMime = aImage.getMimeCode () == null ? "application/octet-stream" : aImage.getMimeCode ();
    return "data:" + sMime + ";base64," + Base64.getEncoder ().encodeToString (aImage.getBinary ());
  }
}
