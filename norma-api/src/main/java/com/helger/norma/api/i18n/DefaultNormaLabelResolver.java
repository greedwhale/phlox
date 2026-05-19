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
package com.helger.norma.api.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link INormaLabelResolver} backed by two {@link ResourceBundle}s loaded from the
 * {@code norma-api} classpath: {@code norma-labels} for BG/BT labels and section titles, and
 * {@code norma-codelists} for UNTDID code list values.
 *
 * @author Philip Helger
 */
public class DefaultNormaLabelResolver implements INormaLabelResolver
{
  public static final String BUNDLE_LABELS = "norma-labels";
  public static final String BUNDLE_CODELISTS = "norma-codelists";

  private static final Logger LOGGER = LoggerFactory.getLogger (DefaultNormaLabelResolver.class);

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
    final String sFullKey = sListName + "." + sCode;
    try
    {
      return ResourceBundle.getBundle (BUNDLE_CODELISTS, aLocale).getString (sFullKey);
    }
    catch (final MissingResourceException ex)
    {
      return sCode;
    }
  }
}
