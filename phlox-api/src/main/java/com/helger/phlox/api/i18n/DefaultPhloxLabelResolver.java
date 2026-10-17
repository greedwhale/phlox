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
package com.helger.phlox.api.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringReplace;

/**
 * Default {@link IPhloxLabelResolver} backed by two {@link ResourceBundle}s loaded from the
 * {@code phlox-api} classpath: {@code phlox-labels} for BG/BT labels and section titles, and
 * {@code phlox-codelists} for UNTDID code list values.
 * <p>
 * Stateless and exposed as a singleton ({@link #INSTANCE}) so it can serve as a stable cache key in
 * {@link com.helger.phlox.api.template.IPhloxTemplate} localization caches without forcing callers
 * to manage shared instances themselves.
 *
 * @author Philip Helger
 */
public final class DefaultPhloxLabelResolver implements IPhloxLabelResolver
{
  /** Resource bundle base name for BG/BT labels and section titles. */
  public static final String BUNDLE_LABELS = "phlox-labels";
  /** Resource bundle base name for code-list value descriptions. */
  public static final String BUNDLE_CODELISTS = "phlox-codelists";

  /** Process-wide shared instance. */
  public static final DefaultPhloxLabelResolver INSTANCE = new DefaultPhloxLabelResolver ();

  private static final Logger LOGGER = LoggerFactory.getLogger (DefaultPhloxLabelResolver.class);

  private DefaultPhloxLabelResolver ()
  {}

  @Override
  @NonNull
  public String getLabel (@NonNull final String sKey, @NonNull final Locale aLocale)
  {
    try
    {
      return ResourceBundle.getBundle (BUNDLE_LABELS, aLocale).getString (sKey);
    }
    catch (final MissingResourceException ex)
    {
      LOGGER.warn ("No label for key '" + sKey + "' in locale '" + aLocale + "'");
      return sKey;
    }
  }

  @Override
  @NonNull
  public String getCodeLabel (@NonNull final String sListName,
                              @NonNull final String sCode,
                              @NonNull final Locale aLocale)
  {
    // The logical list name may be dotted ("vat.category"), the bundle keys are not ("vatcategory")
    final String sFullKey = StringReplace.replaceAll (sListName, ".", "") + "." + sCode;
    try
    {
      return ResourceBundle.getBundle (BUNDLE_CODELISTS, aLocale).getString (sFullKey);
    }
    catch (final MissingResourceException ex)
    {
      LOGGER.warn ("No label for code list key '" + sFullKey + "' in locale '" + aLocale + "'");
      return sCode;
    }
  }
}
