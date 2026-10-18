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

import java.util.Currency;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.name.IHasDisplayName;
import com.helger.base.string.StringReplace;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsMap;
import com.helger.en16931.basics.codelist.EEN16931AllowanceReasonCode;
import com.helger.en16931.basics.codelist.EEN16931ChargeReasonCode;
import com.helger.en16931.basics.codelist.EEN16931CountryCode;
import com.helger.en16931.basics.codelist.EEN16931CurrencyCode;
import com.helger.en16931.basics.codelist.EEN16931EASCode;
import com.helger.en16931.basics.codelist.EEN16931ICDCode;
import com.helger.en16931.basics.codelist.EEN16931InvoiceTypeCode;
import com.helger.en16931.basics.codelist.EEN16931ItemTypeCode;
import com.helger.en16931.basics.codelist.EEN16931PaymentMeansCode;
import com.helger.en16931.basics.codelist.EEN16931ReferenceQualifierCode;
import com.helger.en16931.basics.codelist.EEN16931TaxCategoryCode;
import com.helger.en16931.basics.codelist.EEN16931TextSubjectCode;
import com.helger.en16931.basics.codelist.EEN16931VATEXCode;

/**
 * Default {@link IPhloxLabelResolver} backed by two {@link ResourceBundle}s loaded from the
 * {@code phlox-api} classpath: {@code phlox-labels} for BG/BT labels and section titles, and
 * {@code phlox-codelists} for UNTDID code list values.
 * <p>
 * The label of a code is looked up in this order: the {@code phlox-codelists} bundle of the locale,
 * the country and currency names of the JDK in the locale, the English names of the EN 16931 code
 * lists in {@code en16931-basics} (all code lists that are not translated here) and finally the
 * code itself.
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

  /**
   * The English names of the EN 16931 code lists in {@code en16931-basics}, by the logical list name
   * (without dots) that is used in templates and writers.
   */
  private static final ICommonsMap <String, Function <String, String>> ENGLISH_NAMES = new CommonsHashMap <> ();

  private DefaultPhloxLabelResolver ()
  {}

  private static String _nameOrNull (@Nullable final IHasDisplayName aCode)
  {
    return aCode == null ? null : aCode.getDisplayName ();
  }

  /** The name of a country or currency in the language of the locale, as the JDK knows it. */
  @Nullable
  private static String _localizedJdkName (@NonNull final String sListName,
                                           @NonNull final String sCode,
                                           @NonNull final Locale aLocale)
  {
    try
    {
      final String ret;
      if ("country".equals (sListName))
        ret = new Locale.Builder ().setRegion (sCode).build ().getDisplayCountry (aLocale);
      else
        if ("currency".equals (sListName))
          ret = Currency.getInstance (sCode).getDisplayName (aLocale);
        else
          return null;
      // The JDK returns the code itself for codes it does not know
      return ret.isEmpty () || ret.equals (sCode) ? null : ret;
    }
    catch (final IllformedLocaleException | IllegalArgumentException ex)
    {
      return null;
    }
  }

  static
  {
    ENGLISH_NAMES.put ("allowancereason", x -> _nameOrNull (EEN16931AllowanceReasonCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("chargereason", x -> _nameOrNull (EEN16931ChargeReasonCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("country", x -> _nameOrNull (EEN16931CountryCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("currency", x -> _nameOrNull (EEN16931CurrencyCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("doctype", x -> _nameOrNull (EEN16931InvoiceTypeCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("eas", x -> _nameOrNull (EEN16931EASCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("icd", x -> _nameOrNull (EEN16931ICDCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("itemtype", x -> _nameOrNull (EEN16931ItemTypeCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("notesubject", x -> _nameOrNull (EEN16931TextSubjectCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("paymentmeans", x -> _nameOrNull (EEN16931PaymentMeansCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("referencequalifier", x -> _nameOrNull (EEN16931ReferenceQualifierCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("vatcategory", x -> _nameOrNull (EEN16931TaxCategoryCode.getFromIDOrNull (x)));
    ENGLISH_NAMES.put ("vatexemption", x -> _nameOrNull (EEN16931VATEXCode.getFromIDOrNull (x)));
  }

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
    final String sListKey = StringReplace.replaceAll (sListName, ".", "");
    final String sFullKey = sListKey + "." + sCode;
    final ResourceBundle aBundle = ResourceBundle.getBundle (BUNDLE_CODELISTS, aLocale);
    if (aBundle.containsKey (sFullKey))
      return aBundle.getString (sFullKey);

    final String sJdkName = _localizedJdkName (sListKey, sCode, aLocale);
    if (sJdkName != null)
      return sJdkName;

    final Function <String, String> aEnglishNames = ENGLISH_NAMES.get (sListKey);
    final String sEnglishName = aEnglishNames == null ? null : aEnglishNames.apply (sCode);
    if (sEnglishName != null)
      return sEnglishName;

    LOGGER.warn ("No label for code list key '" + sFullKey + "' in locale '" + aLocale + "'");
    return sCode;
  }
}
