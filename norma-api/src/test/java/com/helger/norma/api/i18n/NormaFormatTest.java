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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Test;

import com.helger.norma.api.NormaFormatOptions;

/**
 * Test class for class {@link NormaFormat}.
 *
 * @author Philip Helger
 */
public final class NormaFormatTest
{
  @Test
  public void testDateUsesLocaleAndDefaultStyle ()
  {
    final LocalDate aDate = LocalDate.of (2026, 3, 15);
    final NormaFormatOptions aOpts = NormaFormatOptions.defaults ();
    final String sEN = NormaFormat.date (aDate, Locale.US, aOpts);
    final String sDE = NormaFormat.date (aDate, Locale.GERMANY, aOpts);
    assertNotNull (sEN);
    assertNotNull (sDE);
    // Locale formatting must actually differ — catches a regression where the format options
    // override the locale-driven style.
    assertTrue ("expected en-US and de-DE to format differently for medium-style date, got '" +
                sEN +
                "' vs '" +
                sDE +
                "'",
                !sEN.equals (sDE));
  }

  @Test
  public void testAmountUsesCurrencyAndLocale ()
  {
    final BigDecimal aValue = new BigDecimal ("123.45");
    final NormaFormatOptions aOpts = NormaFormatOptions.defaults ();
    final String sUSD = NormaFormat.amount (aValue, "USD", Locale.US, aOpts);
    final String sEUR = NormaFormat.amount (aValue, "EUR", Locale.GERMANY, aOpts);
    assertNotNull (sUSD);
    assertNotNull (sEUR);
    assertTrue ("expected US-formatted USD amount '" + sUSD + "' to contain 123.45", sUSD.contains ("123.45"));
    assertTrue ("expected DE-formatted EUR amount '" + sEUR + "' to contain 123,45", sEUR.contains ("123,45"));
  }

  @Test
  public void testAmountFractionDigitsOverride ()
  {
    final BigDecimal aValue = new BigDecimal ("1.234");
    final NormaFormatOptions aOpts = NormaFormatOptions.defaults ().setAmountFractionDigits (Integer.valueOf (3));
    final String sOut = NormaFormat.amount (aValue, "EUR", Locale.US, aOpts);
    assertTrue ("expected fraction-digit override to keep 3 decimals, got: " + sOut, sOut.contains ("1.234"));
  }

  @Test
  public void testPercentUsesConfiguredFractionDigits ()
  {
    final BigDecimal aRate = new BigDecimal ("19");
    final NormaFormatOptions aOpts = NormaFormatOptions.defaults ();
    final String sEN = NormaFormat.percent (aRate, Locale.US, aOpts);
    assertNotNull (sEN);
    // BT-X percentages are stored as whole-percent (19 == 19%), so the rendered text must
    // contain "19" — not "1900" (raw multiplication) and not "0.19" (raw fraction).
    assertTrue ("expected '19' in formatted percent, got: " + sEN, sEN.contains ("19"));
    assertEquals ("19.00%", sEN);
    assertEquals ("19,00\u00a0%", NormaFormat.percent (aRate, Locale.GERMANY, aOpts));
  }
}
