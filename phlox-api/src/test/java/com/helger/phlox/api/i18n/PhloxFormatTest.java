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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Test;

import com.helger.phlox.api.PhloxFormatOptions;

/**
 * Test class for class {@link PhloxFormat}.
 *
 * @author Philip Helger
 */
public final class PhloxFormatTest
{
  @Test
  public void testDateUsesLocaleAndDefaultStyle ()
  {
    final LocalDate aDate = LocalDate.of (2026, 3, 15);
    final PhloxFormatOptions aOpts = PhloxFormatOptions.defaults ();
    final String sEn = PhloxFormat.date (aDate, Locale.US, aOpts);
    final String sDe = PhloxFormat.date (aDate, Locale.GERMANY, aOpts);
    assertNotNull (sEn);
    assertNotNull (sDe);
    // Locale formatting must actually differ — catches a regression where the format options
    // override the locale-driven style.
    assertTrue ("expected en-US and de-DE to format differently for medium-style date, got '" +
                sEn +
                "' vs '" +
                sDe +
                "'",
                !sEn.equals (sDe));
  }

  @Test
  public void testAmountUsesCurrencyAndLocale ()
  {
    final BigDecimal aValue = new BigDecimal ("123.45");
    final PhloxFormatOptions aOpts = PhloxFormatOptions.defaults ();
    final String sUsd = PhloxFormat.amount (aValue, "USD", Locale.US, aOpts);
    final String sEur = PhloxFormat.amount (aValue, "EUR", Locale.GERMANY, aOpts);
    assertNotNull (sUsd);
    assertNotNull (sEur);
    assertTrue ("expected US-formatted USD amount '" + sUsd + "' to contain 123.45", sUsd.contains ("123.45"));
    assertTrue ("expected DE-formatted EUR amount '" + sEur + "' to contain 123,45", sEur.contains ("123,45"));
  }

  @Test
  public void testAmountFractionDigitsOverride ()
  {
    final BigDecimal aValue = new BigDecimal ("1.234");
    final PhloxFormatOptions aOpts = PhloxFormatOptions.defaults ().setAmountFractionDigits (Integer.valueOf (3));
    final String sOut = PhloxFormat.amount (aValue, "EUR", Locale.US, aOpts);
    assertTrue ("expected fraction-digit override to keep 3 decimals, got: " + sOut, sOut.contains ("1.234"));
  }

  @Test
  public void testPercentUsesConfiguredFractionDigits ()
  {
    final BigDecimal aRate = new BigDecimal ("19");
    final PhloxFormatOptions aOpts = PhloxFormatOptions.defaults ();
    final String sEn = PhloxFormat.percent (aRate, Locale.US, aOpts);
    assertNotNull (sEn);
    // BT-X percentages are stored as whole-percent (19 == 19%), so the rendered text must
    // contain "19" — not "1900" (raw multiplication) and not "0.19" (raw fraction).
    assertTrue ("expected '19' in formatted percent, got: " + sEn, sEn.contains ("19"));
    assertEquals ("19.00%", sEn);
    assertEquals ("19,00\u00a0%", PhloxFormat.percent (aRate, Locale.GERMANY, aOpts));
  }
}
