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
package com.helger.norma.html;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.norma.api.AbstractRenderContext;

/**
 * HTML-specific render context. Adds CSS prefix and stylesheet options on top of the shared
 * {@link AbstractRenderContext} knobs.
 *
 * @author Philip Helger
 */
public class HTMLRenderContext extends AbstractRenderContext <HTMLRenderContext>
{
  public static final String DEFAULT_CSS_CLASS_PREFIX = "norma-";
  public static final boolean DEFAULT_EMBEDDED_STYLESHEET = true;
  public static final boolean DEFAULT_EMIT_DOCTYPE = true;

  private String m_sCSSClassPrefix = DEFAULT_CSS_CLASS_PREFIX;
  private boolean m_bEmbeddedStylesheet = DEFAULT_EMBEDDED_STYLESHEET;
  private String m_sCustomStylesheet;
  private boolean m_bEmitDoctype = DEFAULT_EMIT_DOCTYPE;

  public HTMLRenderContext ()
  {}

  /** Create a {@link HTMLRenderContext} pre-populated with another context's shared settings. */
  @NonNull
  public static HTMLRenderContext copyFrom (@NonNull final AbstractRenderContext <?> aSource)
  {
    return new HTMLRenderContext ().copyBaseFieldsFrom (aSource);
  }

  @NonNull
  public String getCSSClassPrefix ()
  {
    return m_sCSSClassPrefix;
  }

  @NonNull
  public HTMLRenderContext setCSSClassPrefix (@NonNull final String sCSSClassPrefix)
  {
    m_sCSSClassPrefix = sCSSClassPrefix;
    return this;
  }

  public boolean isEmbeddedStylesheet ()
  {
    return m_bEmbeddedStylesheet;
  }

  @NonNull
  public HTMLRenderContext setEmbeddedStylesheet (final boolean bEmbeddedStylesheet)
  {
    m_bEmbeddedStylesheet = bEmbeddedStylesheet;
    return this;
  }

  @Nullable
  public String getCustomStylesheet ()
  {
    return m_sCustomStylesheet;
  }

  @NonNull
  public HTMLRenderContext setCustomStylesheet (@Nullable final String sCustomStylesheet)
  {
    m_sCustomStylesheet = sCustomStylesheet;
    return this;
  }

  public boolean isEmitDoctype ()
  {
    return m_bEmitDoctype;
  }

  @NonNull
  public HTMLRenderContext setEmitDoctype (final boolean bEmitDoctype)
  {
    m_bEmitDoctype = bEmitDoctype;
    return this;
  }
}
