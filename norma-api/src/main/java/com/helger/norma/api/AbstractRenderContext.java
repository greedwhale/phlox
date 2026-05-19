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
package com.helger.norma.api;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.trait.IGenericImplTrait;
import com.helger.norma.api.i18n.DefaultNormaLabelResolver;
import com.helger.norma.api.i18n.INormaLabelResolver;
import com.helger.norma.api.model.NormaAttachment;
import com.helger.norma.api.watermark.WatermarkSpec;

/**
 * Base render context — syntax-neutral, output-format-neutral configuration shared by every
 * writer. Output-specific subclasses ({@code HTMLRenderContext}, {@code PDFRenderContext})
 * add knobs for their medium.
 * <p>
 * The {@code IMPLTYPE} parameter ensures fluent setters return the concrete subtype so
 * call chains stay typed:
 * <pre>
 * new HTMLRenderContext ()
 *     .setLocale (Locale.GERMAN)         // returns HTMLRenderContext
 *     .setEmbeddedStylesheet (false);    // HTML-only setter still in chain
 * </pre>
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        The concrete subclass implementing this context.
 */
public abstract class AbstractRenderContext <IMPLTYPE extends AbstractRenderContext <IMPLTYPE>> implements
                                            IGenericImplTrait <IMPLTYPE>
{
  public static final boolean DEFAULT_ANNOTATE_BT_IDS = false;
  public static final boolean DEFAULT_INCLUDE_EMPTY_SECTIONS = false;

  private Locale m_aLocale = Locale.ENGLISH;
  private NormaFormatOptions m_aFormatOptions = NormaFormatOptions.defaults ();
  private NormaAttachment m_aLogo;
  private INormaLabelResolver m_aLabelResolver = new DefaultNormaLabelResolver ();
  private boolean m_bAnnotateBTIDs = DEFAULT_ANNOTATE_BT_IDS;
  private boolean m_bIncludeEmptySections = DEFAULT_INCLUDE_EMPTY_SECTIONS;
  private WatermarkSpec m_aUserWatermark;

  /** Subclass-only constructor. */
  protected AbstractRenderContext ()
  {}

  /** @return The target locale for labels and locale-driven formatting. Never {@code null}. */
  @NonNull
  public Locale getLocale ()
  {
    return m_aLocale;
  }

  /**
   * Set the target locale.
   *
   * @param aLocale
   *        Locale; never {@code null}.
   * @return this for chaining (typed as the concrete subclass).
   */
  @NonNull
  public IMPLTYPE setLocale (@NonNull final Locale aLocale)
  {
    m_aLocale = aLocale;
    return thisAsT ();
  }

  /** @return The per-call format options. Never {@code null}. */
  @NonNull
  public NormaFormatOptions getFormatOptions ()
  {
    return m_aFormatOptions;
  }

  /**
   * Set the format options.
   *
   * @param aFormatOptions
   *        Format options; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setFormatOptions (@NonNull final NormaFormatOptions aFormatOptions)
  {
    m_aFormatOptions = aFormatOptions;
    return thisAsT ();
  }

  /** @return The optional logo to render in the page header. May be {@code null}. */
  @Nullable
  public NormaAttachment getLogo ()
  {
    return m_aLogo;
  }

  /**
   * Set the optional logo to render in the page header.
   *
   * @param aLogo
   *        Logo image; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setLogo (@Nullable final NormaAttachment aLogo)
  {
    m_aLogo = aLogo;
    return thisAsT ();
  }

  /** @return The label resolver in use. Never {@code null}. */
  @NonNull
  public INormaLabelResolver getLabelResolver ()
  {
    return m_aLabelResolver;
  }

  /**
   * Replace the label resolver.
   *
   * @param aLabelResolver
   *        Resolver; never {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setLabelResolver (@NonNull final INormaLabelResolver aLabelResolver)
  {
    m_aLabelResolver = aLabelResolver;
    return thisAsT ();
  }

  /** @return {@code true} when labels should be suffixed with their BT/BG ID (debug aid). */
  public boolean isAnnotateBTIDs ()
  {
    return m_bAnnotateBTIDs;
  }

  /**
   * Set whether labels should be suffixed with their BT/BG ID (debug aid).
   *
   * @param bAnnotateBTIDs
   *        {@code true} to annotate.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setAnnotateBTIDs (final boolean bAnnotateBTIDs)
  {
    m_bAnnotateBTIDs = bAnnotateBTIDs;
    return thisAsT ();
  }

  /** @return {@code true} when section headings should be rendered even when empty. */
  public boolean isIncludeEmptySections ()
  {
    return m_bIncludeEmptySections;
  }

  /**
   * Set whether section headings should be rendered even when empty.
   *
   * @param bIncludeEmptySections
   *        {@code true} to include empty sections.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setIncludeEmptySections (final boolean bIncludeEmptySections)
  {
    m_bIncludeEmptySections = bIncludeEmptySections;
    return thisAsT ();
  }

  /** @return The optional user watermark (e.g. "DRAFT"). May be {@code null}. */
  @Nullable
  public WatermarkSpec getUserWatermark ()
  {
    return m_aUserWatermark;
  }

  /**
   * Set the optional user watermark drawn on every output page.
   *
   * @param aUserWatermark
   *        Watermark spec; may be {@code null} for no user watermark.
   * @return this for chaining.
   */
  @NonNull
  public IMPLTYPE setUserWatermark (@Nullable final WatermarkSpec aUserWatermark)
  {
    m_aUserWatermark = aUserWatermark;
    return thisAsT ();
  }

  /**
   * Copy all base fields from another context into this one. Used by subclass static factories
   * (e.g. {@code PDFRenderContext.copyFrom(...)}) to share settings across formats.
   *
   * @param aSource
   *        The source context to copy from.
   * @return {@code this} for chaining.
   */
  @NonNull
  public IMPLTYPE copyBaseFieldsFrom (@NonNull final AbstractRenderContext <?> aSource)
  {
    m_aLocale = aSource.m_aLocale;
    m_aFormatOptions = aSource.m_aFormatOptions;
    m_aLogo = aSource.m_aLogo;
    m_aLabelResolver = aSource.m_aLabelResolver;
    m_bAnnotateBTIDs = aSource.m_bAnnotateBTIDs;
    m_bIncludeEmptySections = aSource.m_bIncludeEmptySections;
    m_aUserWatermark = aSource.m_aUserWatermark;
    return thisAsT ();
  }

  /**
   * Convenience: look up a label using the configured resolver and locale.
   *
   * @param sKey
   *        The label key.
   * @return The resolved label, with the BT/BG identifier appended in brackets when
   *         {@link #isAnnotateBTIDs()} is set.
   */
  @NonNull
  public String label (@NonNull final String sKey)
  {
    final String sValue = m_aLabelResolver.getLabel (sKey, m_aLocale);
    if (m_bAnnotateBTIDs && (sKey.startsWith ("BT-") || sKey.startsWith ("BG-")))
      return sValue + " [" + sKey + "]";
    return sValue;
  }

  /** Convenience: look up a code-list label using the configured resolver and locale. */
  @NonNull
  public String codeLabel (@NonNull final String sListName, @NonNull final String sCode)
  {
    return m_aLabelResolver.getCodeLabel (sListName, sCode, m_aLocale);
  }
}
