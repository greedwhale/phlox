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

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.jspecify.annotations.NonNull;

import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.template.NormaTemplateException.EStage;
import com.helger.norma.template.adapter.NormaInvoiceMustacheContext;
import com.helger.norma.template.ast.NormaTemplateAST;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;

/**
 * Locale-baked, JMustache-compiled template — the artifact produced by stages 0 and 0a of the
 * pipeline (see {@code wiki/Template-Processing.md} § Pipeline overview). One
 * {@link NormaLocalizedTemplate} is reusable across many invoices that share the same
 * {@code (template, locale, label-resolver)} key.
 * <p>
 * Immutable after construction and safe to share across threads. Held by {@link NormaTemplate}
 * inside a ph-cache instance; callers who want explicit control can obtain a localized
 * variant via {@link NormaTemplate#localize}.
 *
 * @author Philip Helger
 */
public final class NormaLocalizedTemplate
{
  private final String m_sLocaleBakedSource;
  private final Template m_aMustache;
  private final Parser m_aParser;

  /** Package-private — constructed only by {@link NormaTemplate}. */
  NormaLocalizedTemplate (@NonNull final String sLocaleBakedSource,
                          @NonNull final Template aMustache,
                          @NonNull final Parser aParser)
  {
    m_sLocaleBakedSource = sLocaleBakedSource;
    m_aMustache = aMustache;
    m_aParser = aParser;
  }

  /**
   * @return The locale-baked source — the {@code .ntpl} text after stage 0. Kept for
   *         diagnostics / snapshot tests; never the runtime render path.
   */
  @NonNull
  public String getLocaleBakedSource ()
  {
    return m_sLocaleBakedSource;
  }

  /**
   * Run stages 1 + 2 + 2a — substitute the invoice into the compiled template, parse the
   * result as CommonMark, then run the sentinel + image post-pass.
   *
   * @param aInvoice
   *        Source invoice.
   * @param aCtx
   *        Render context.
   * @return Populated {@link NormaTemplateAST} ready for an emitter.
   */
  @NonNull
  public NormaTemplateAST renderAST (@NonNull final NormaInvoice aInvoice,
                                     @NonNull final AbstractRenderContext <?> aCtx)
  {
    final String sSubstituted = renderSubstituted (aInvoice, aCtx);

    final Node aRoot;
    try
    {
      aRoot = m_aParser.parse (sSubstituted);
    }
    catch (final RuntimeException ex)
    {
      throw new NormaTemplateException (EStage.PARSE, null, "commonmark parse failed: " + ex.getMessage (), ex);
    }

    final NormaTemplatePostParser aPostParser = new NormaTemplatePostParser (aInvoice, aCtx);
    aPostParser.process (aRoot);

    return new NormaTemplateAST (aRoot,
                                 aPostParser.getBlockClassMap (),
                                 aPostParser.getImageMap (),
                                 aInvoice,
                                 aCtx);
  }

  /**
   * Run stage 1 in isolation — substitute the invoice into the compiled template and return
   * the CommonMark string without parsing it. Mainly useful for snapshot testing.
   */
  @NonNull
  public String renderSubstituted (@NonNull final NormaInvoice aInvoice,
                                   @NonNull final AbstractRenderContext <?> aCtx)
  {
    final String [] aOut = new String [1];
    NormaInvoiceMustacheContext.withEnv (aInvoice, aCtx, () -> {
      try
      {
        aOut[0] = m_aMustache.execute (aInvoice);
      }
      catch (final MustacheException ex)
      {
        Throwable aCause = ex.getCause ();
        while (aCause != null)
        {
          if (aCause instanceof NormaTemplateException)
            throw (NormaTemplateException) aCause;
          aCause = aCause.getCause ();
        }
        throw new NormaTemplateException (EStage.SUBSTITUTE,
                                          null,
                                          "JMustache execute failed: " + ex.getMessage (),
                                          ex);
      }
    });
    return aOut[0];
  }
}
