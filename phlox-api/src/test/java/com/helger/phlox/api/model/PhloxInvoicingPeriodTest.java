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
package com.helger.phlox.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

/**
 * Test class for class {@link PhloxInvoicingPeriod}.
 *
 * @author Philip Helger
 */
public final class PhloxInvoicingPeriodTest
{
  @Test
  public void testEmptyByDefault ()
  {
    assertTrue (new PhloxInvoicingPeriod ().isEmpty ());
  }

  @Test
  public void testNotEmptyWhenAnyDateSet ()
  {
    assertFalse (new PhloxInvoicingPeriod ().setStartDate (LocalDate.of (2026, 1, 1)).isEmpty ());
    assertFalse (new PhloxInvoicingPeriod ().setEndDate (LocalDate.of (2026, 1, 31)).isEmpty ());
  }

  @Test
  public void testFluentSettersStickValues ()
  {
    final LocalDate aStart = LocalDate.of (2026, 1, 1);
    final LocalDate aEnd = LocalDate.of (2026, 1, 31);
    final PhloxInvoicingPeriod aPeriod = new PhloxInvoicingPeriod ();
    assertSame (aPeriod, aPeriod.setStartDate (aStart));
    assertSame (aPeriod, aPeriod.setEndDate (aEnd));
    assertEquals (aStart, aPeriod.getStartDate ());
    assertEquals (aEnd, aPeriod.getEndDate ());
  }
}
