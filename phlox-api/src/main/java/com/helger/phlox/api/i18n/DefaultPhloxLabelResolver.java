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
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringReplace;
import com.helger.codelists.unit.UnitCodes;
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
import com.helger.text.display.IHasDisplayText;

/**
 * Default {@link IPhloxLabelResolver}. The labels of the BG/BT and the section titles come from the
 * {@link ResourceBundle} {@code phlox-labels} of the {@code phlox-api} classpath. The names of the
 * values of a code list come from the code lists themselves: the enums of {@code en16931-basics}
 * and the unit codes of {@code ph-codelists-unit}. They know the languages that exist for their
 * list and fall back to English, so the translations are maintained once for all projects. A code
 * that is in none of the lists is shown as the code itself.
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

  /** Process-wide shared instance. */
  public static final DefaultPhloxLabelResolver INSTANCE = new DefaultPhloxLabelResolver ();

  private static final Logger LOGGER = LoggerFactory.getLogger (DefaultPhloxLabelResolver.class);

  /**
   * The code lists by the logical list name (without dots) that is used in templates and writers.
   * Each entry finds the code in the list.
   */
  private static final ICommonsMap <String, Function <String, IHasDisplayText>> CODE_LISTS = new CommonsHashMap <> ();

  private DefaultPhloxLabelResolver ()
  {}

  /**
   * Resolve a bundle without falling back to the JVM default locale. An unsupported locale resolves
   * to English.
   */
  @NonNull
  private static ResourceBundle _getBundle (@NonNull final String sBaseName, @NonNull final Locale aLocale)
  {
    final ResourceBundle.Control aControl = ResourceBundle.Control.getNoFallbackControl (ResourceBundle.Control.FORMAT_PROPERTIES);
    try
    {
      return ResourceBundle.getBundle (sBaseName, aLocale, aControl);
    }
    catch (final MissingResourceException ex)
    {
      return ResourceBundle.getBundle (sBaseName, Locale.ENGLISH, aControl);
    }
  }

  static
  {
    CODE_LISTS.put ("allowancereason", EEN16931AllowanceReasonCode::getFromIDOrNull);
    CODE_LISTS.put ("chargereason", EEN16931ChargeReasonCode::getFromIDOrNull);
    CODE_LISTS.put ("country", EEN16931CountryCode::getFromIDOrNull);
    CODE_LISTS.put ("currency", EEN16931CurrencyCode::getFromIDOrNull);
    CODE_LISTS.put ("doctype", EEN16931InvoiceTypeCode::getFromIDOrNull);
    CODE_LISTS.put ("eas", EEN16931EASCode::getFromIDOrNull);
    CODE_LISTS.put ("icd", EEN16931ICDCode::getFromIDOrNull);
    CODE_LISTS.put ("itemtype", EEN16931ItemTypeCode::getFromIDOrNull);
    CODE_LISTS.put ("notesubject", EEN16931TextSubjectCode::getFromIDOrNull);
    CODE_LISTS.put ("paymentmeans", EEN16931PaymentMeansCode::getFromIDOrNull);
    CODE_LISTS.put ("referencequalifier", EEN16931ReferenceQualifierCode::getFromIDOrNull);
    CODE_LISTS.put ("unit", UnitCodes::getFromIDOrNull);
    CODE_LISTS.put ("vatcategory", EEN16931TaxCategoryCode::getFromIDOrNull);
    // The lookup of the VATEX codes ignores the case, like the EN 16931 Schematron rule BR-CL-22
    CODE_LISTS.put ("vatexemption", EEN16931VATEXCode::getFromIDOrNull);
  }

  @NonNull
  public String getLabel (@NonNull final String sKey, @NonNull final Locale aLocale)
  {
    try
    {
      return _getBundle (BUNDLE_LABELS, aLocale).getString (sKey);
    }
    catch (final MissingResourceException ex)
    {
      LOGGER.warn ("No label for key '" + sKey + "' in locale '" + aLocale + "'");
      return sKey;
    }
  }

  @NonNull
  public String getCodeLabel (@NonNull final String sListName,
                              @NonNull final String sCode,
                              @NonNull final Locale aLocale)
  {
    // The logical list name may be dotted ("vat.category"), the keys are not ("vatcategory")
    final String sListKey = StringReplace.replaceAll (sListName, ".", "");
    final Function <String, IHasDisplayText> aFinder = CODE_LISTS.get (sListKey);
    final IHasDisplayText aCode = aFinder == null ? null : aFinder.apply (sCode);
    if (aCode != null)
    {
      final String ret = aCode.getDisplayText (aLocale);
      if (ret != null)
        return ret;
    }

    LOGGER.warn ("No label for code list key '" + sListKey + "." + sCode + "' in locale '" + aLocale + "'");
    return sCode;
  }
}
