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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.CGlobal;
import com.helger.datetime.format.EDTFormatterMode;
import com.helger.datetime.format.PDTFormatter;
import com.helger.norma.api.NormaFormatOptions;

/**
 * Locale-aware formatting helpers used by both writers. Pure functions — no state.
 *
 * @author Philip Helger
 */
@Immutable
public final class NormaFormat
{
  private NormaFormat ()
  {}

  /**
   * Format a date in the locale's medium style.
   *
   * @param aDate
   *        The date, may be {@code null}.
   * @param aLocale
   *        The target locale.
   * @param aOptions
   *        Optional format overrides; may be {@code null} for defaults.
   * @return Formatted date string, or empty if {@code aDate} is {@code null}.
   */
  @NonNull
  public static String date (@Nullable final LocalDate aDate,
                             @NonNull final Locale aLocale,
                             @Nullable final NormaFormatOptions aOptions)
  {
    if (aDate == null)
      return "";
    final FormatStyle eStyle = aOptions != null && aOptions.getDateStyle () != null ? aOptions.getDateStyle ()
                                                                                    : FormatStyle.MEDIUM;
    return PDTFormatter.getFormatterDate (eStyle, aLocale, EDTFormatterMode.PRINT).format (aDate);
  }

  /**
   * Format an amount with the given currency in the locale's number style.
   *
   * @param aAmount
   *        The amount, may be {@code null}.
   * @param sCurrencyCode
   *        The ISO 4217 currency code (e.g. {@code "EUR"}), may be {@code null} for no currency
   *        symbol.
   * @param aLocale
   *        The target locale.
   * @param aOptions
   *        Optional format overrides; may be {@code null} for defaults.
   * @return Formatted amount, or empty if {@code aAmount} is {@code null}.
   */
  @NonNull
  public static String amount (@Nullable final BigDecimal aAmount,
                               @Nullable final String sCurrencyCode,
                               @NonNull final Locale aLocale,
                               @Nullable final NormaFormatOptions aOptions)
  {
    if (aAmount == null)
      return "";

    final NumberFormat aFmt = NumberFormat.getCurrencyInstance (aLocale);
    if (sCurrencyCode != null)
    {
      try
      {
        aFmt.setCurrency (Currency.getInstance (sCurrencyCode));
      }
      catch (final IllegalArgumentException ex)
      {
        // Unknown currency code — keep the locale default
      }
    }
    if (aOptions != null && aOptions.getAmountFractionDigits () != null)
    {
      aFmt.setMinimumFractionDigits (aOptions.getAmountFractionDigits ().intValue ());
      aFmt.setMaximumFractionDigits (aOptions.getAmountFractionDigits ().intValue ());
    }
    return aFmt.format (aAmount);
  }

  /**
   * Format a percentage value (e.g. {@code 19} → {@code "19.00%"} in en-US, {@code "19,00 %"} in
   * de-DE).
   *
   * @param aRate
   *        The value expressed in whole-percent (i.e. {@code 19} for 19%). May be {@code null}.
   * @param aLocale
   *        The target locale.
   * @param aOptions
   *        Optional format overrides; may be {@code null} for defaults.
   * @return Formatted percentage, or empty if {@code aRate} is {@code null}.
   */
  @NonNull
  public static String percent (@Nullable final BigDecimal aRate,
                                @NonNull final Locale aLocale,
                                @Nullable final NormaFormatOptions aOptions)
  {
    if (aRate == null)
      return "";

    final NumberFormat aFmt = NumberFormat.getPercentInstance (aLocale);
    final int nDigits = aOptions != null ? aOptions.getPercentFractionDigits () : 2;
    aFmt.setMinimumFractionDigits (nDigits);
    aFmt.setMaximumFractionDigits (nDigits);
    // From 19 to 0,19
    return aFmt.format (aRate.divide (CGlobal.BIGDEC_100));
  }
}
