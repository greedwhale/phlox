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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.phlox.api.AbstractRenderContext;

/**
 * HTML-specific render context. Adds CSS prefix and stylesheet options on top of the shared
 * {@link AbstractRenderContext} knobs.
 *
 * @author Philip Helger
 */
public class HtmlRenderContext extends AbstractRenderContext <HtmlRenderContext>
{
  /** Default prefix of all CSS classes emitted by the writer. */
  public static final String DEFAULT_CSS_CLASS_PREFIX = "phlox-";
  /** Default for embedding the stylesheet into the document. */
  public static final boolean DEFAULT_EMBEDDED_STYLESHEET = true;
  /** Default for emitting the HTML doctype. */
  public static final boolean DEFAULT_EMIT_DOCTYPE = true;

  private String m_sCssClassPrefix = DEFAULT_CSS_CLASS_PREFIX;
  private boolean m_bEmbeddedStylesheet = DEFAULT_EMBEDDED_STYLESHEET;
  private String m_sCustomStylesheet;
  private boolean m_bEmitDoctype = DEFAULT_EMIT_DOCTYPE;

  /** Create a new context with the default HTML settings. */
  public HtmlRenderContext ()
  {}

  /**
   * Create a new context pre-populated with the shared settings of another context.
   *
   * @param aSource
   *        The context to copy the shared settings from. Never {@code null}.
   * @return A new context. Never {@code null}.
   */
  @NonNull
  public static HtmlRenderContext copyFrom (@NonNull final AbstractRenderContext <?> aSource)
  {
    return new HtmlRenderContext ().copyBaseFieldsFrom (aSource);
  }

  /**
   * Get the CSS class prefix.
   *
   * @return The CSS class prefix.
   */
  @NonNull
  public String getCssClassPrefix ()
  {
    return m_sCssClassPrefix;
  }

  /**
   * Set the CSS class prefix.
   *
   * @param sCssClassPrefix
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public HtmlRenderContext setCssClassPrefix (@NonNull final String sCssClassPrefix)
  {
    m_sCssClassPrefix = sCssClassPrefix;
    return this;
  }

  /**
   * Check if the default stylesheet is embedded into the document.
   *
   * @return {@code true} if the default stylesheet is embedded into the document.
   */
  public boolean isEmbeddedStylesheet ()
  {
    return m_bEmbeddedStylesheet;
  }

  /**
   * Set the embedded stylesheet flag.
   *
   * @param bEmbeddedStylesheet
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public HtmlRenderContext setEmbeddedStylesheet (final boolean bEmbeddedStylesheet)
  {
    m_bEmbeddedStylesheet = bEmbeddedStylesheet;
    return this;
  }

  /**
   * Get the custom stylesheet.
   *
   * @return The custom stylesheet. May be {@code null}.
   */
  @Nullable
  public String getCustomStylesheet ()
  {
    return m_sCustomStylesheet;
  }

  /**
   * Set the custom stylesheet.
   *
   * @param sCustomStylesheet
   *        The new value. May be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public HtmlRenderContext setCustomStylesheet (@Nullable final String sCustomStylesheet)
  {
    m_sCustomStylesheet = sCustomStylesheet;
    return this;
  }

  /**
   * Check if the HTML doctype is emitted.
   *
   * @return {@code true} if the HTML doctype is emitted.
   */
  public boolean isEmitDoctype ()
  {
    return m_bEmitDoctype;
  }

  /**
   * Set the emit doctype flag.
   *
   * @param bEmitDoctype
   *        The new value. Never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public HtmlRenderContext setEmitDoctype (final boolean bEmitDoctype)
  {
    m_bEmitDoctype = bEmitDoctype;
    return this;
  }
}
