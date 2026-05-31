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
package com.helger.norma.template.adapter;

import java.util.Locale;

import org.jspecify.annotations.NonNull;

import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.NormaFormatOptions;
import com.helger.norma.api.i18n.INormaLabelResolver;
import com.helger.norma.api.model.NormaInvoice;

/**
 * Per-render execution context for the JMustache adapter — bundles the source invoice, the
 * active render context, and a few cached lookups. Created fresh per render and exposed to the
 * Mustache fetcher via a {@link ThreadLocal}; never reused across threads.
 *
 * @author Philip Helger
 */
public final class NormaRenderEnv
{
  private final NormaInvoice m_aInvoice;
  private final AbstractRenderContext <?> m_aRenderContext;
  private final Locale m_aLocale;
  private final INormaLabelResolver m_aLabelResolver;
  private final NormaFormatOptions m_aFormatOptions;

  /**
   * Capture per-render state. The locale, label resolver and format options are pulled from the
   * render context once and cached on the env to keep the hot path lock-free.
   *
   * @param aInvoice
   *        Source invoice.
   * @param aRenderContext
   *        Render context for the current render call.
   */
  public NormaRenderEnv (@NonNull final NormaInvoice aInvoice,
                         @NonNull final AbstractRenderContext <?> aRenderContext)
  {
    m_aInvoice = aInvoice;
    m_aRenderContext = aRenderContext;
    m_aLocale = aRenderContext.getLocale ();
    m_aLabelResolver = aRenderContext.getLabelResolver ();
    m_aFormatOptions = aRenderContext.getFormatOptions ();
  }

  /** @return The source invoice. */
  @NonNull
  public NormaInvoice getInvoice ()
  {
    return m_aInvoice;
  }

  /** @return The render context for this render call. */
  @NonNull
  public AbstractRenderContext <?> getRenderContext ()
  {
    return m_aRenderContext;
  }

  /** @return The locale to use for formatting and label lookups. */
  @NonNull
  public Locale getLocale ()
  {
    return m_aLocale;
  }

  /** @return The label resolver pulled from the render context. */
  @NonNull
  public INormaLabelResolver getLabelResolver ()
  {
    return m_aLabelResolver;
  }

  /** @return The format options pulled from the render context. */
  @NonNull
  public NormaFormatOptions getFormatOptions ()
  {
    return m_aFormatOptions;
  }
}
