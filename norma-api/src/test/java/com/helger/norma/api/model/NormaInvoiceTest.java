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
package com.helger.norma.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

/**
 * Test class for class {@link NormaInvoice}.
 *
 * @author Philip Helger
 */
public final class NormaInvoiceTest
{
  @Test
  public void testDefaultsAreInvoice ()
  {
    final NormaInvoice aInv = new NormaInvoice ();
    assertEquals (ENormaDocumentKind.INVOICE, aInv.getKind ());
    assertTrue (aInv.isInvoice ());
    assertFalse (aInv.isCreditNote ());
  }

  @Test
  public void testKindSwitch ()
  {
    final NormaInvoice aInv = new NormaInvoice ().setKind (ENormaDocumentKind.CREDIT_NOTE);
    assertEquals (ENormaDocumentKind.CREDIT_NOTE, aInv.getKind ());
    assertTrue (aInv.isCreditNote ());
    assertFalse (aInv.isInvoice ());
  }

  @Test
  public void testHeaderBTsStickThroughFluentSetters ()
  {
    final LocalDate aDate = LocalDate.of (2026, 3, 15);
    final NormaInvoice aInv = new NormaInvoice ().setNumber ("INV-001")
                                                 .setIssueDate (aDate)
                                                 .setTypeCode ("380")
                                                 .setCurrency ("EUR");
    assertEquals ("INV-001", aInv.getNumber ());
    assertEquals (aDate, aInv.getIssueDate ());
    assertEquals ("380", aInv.getTypeCode ());
    assertEquals ("EUR", aInv.getCurrency ());
  }

  @Test
  public void testBGContainersAreInitialised ()
  {
    // Mandatory BGs (BG-2, BG-4, BG-7, BG-22) are populated by the constructor — never null.
    final NormaInvoice aInv = new NormaInvoice ();
    assertNotNull (aInv.getProcessControl ());
    assertNotNull (aInv.getSeller ());
    assertNotNull (aInv.getBuyer ());
    assertNotNull (aInv.getDocumentTotals ());
  }

  @Test
  public void testListAccessorsReturnSameInstance ()
  {
    // Mutating the returned list must mutate the invoice's underlying state — i.e. the accessor
    // returns the live list rather than a copy. This is the convention documented for the
    // ICommonsList getters in NormaInvoice.
    final NormaInvoice aInv = new NormaInvoice ();
    final NormaInvoiceLine aLine = new NormaInvoiceLine ();
    aInv.lines ().add (aLine);
    assertEquals (1, aInv.lines ().size ());
    assertSame (aLine, aInv.lines ().get (0));
  }

  @Test
  public void testInvoiceNotesListWorks ()
  {
    final NormaInvoice aInv = new NormaInvoice ();
    aInv.invoiceNotes ().add (new NormaInvoiceNote ().setContent ("hello"));
    assertEquals (1, aInv.invoiceNotes ().size ());
    assertEquals ("hello", aInv.invoiceNotes ().get (0).getContent ());
  }
}
