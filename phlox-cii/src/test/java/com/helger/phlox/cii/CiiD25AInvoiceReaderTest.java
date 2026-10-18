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
package com.helger.phlox.cii;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.basics.EEN16931Edition;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.model.PhloxDirectDebit;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.model.PhloxInvoiceLine;
import com.helger.phlox.api.model.PhloxPaymentTerms;
import com.helger.phlox.testfiles.MockPhloxTestFiles;

/**
 * Test class for reading CII D25A (EN 16931:2026) with {@link CiiInvoiceReader}.
 *
 * @author Philip Helger
 */
public final class CiiD25AInvoiceReaderTest
{
  @NonNull
  private static PhloxInvoice _read (@NonNull final CiiInvoiceReader aReader, @NonNull final String sFilename)
  {
    final ClassPathResource aRes = new ClassPathResource (MockPhloxTestFiles.BASE_PATH + "2026/cii/" + sFilename);
    try (final InputStream aIS = aRes.getInputStream ())
    {
      final ErrorList aErrors = new ErrorList ();
      final PhloxInvoice ret = aReader.read (aIS, aErrors);
      assertNotNull ("Reader returned null for " + sFilename + " — errors: " + aErrors, ret);
      assertTrue ("Unexpected errors in " + sFilename + ": " + aErrors, aErrors.isEmpty ());
      return ret;
    }
    catch (final IOException ex)
    {
      throw new RuntimeException ("Failed to open " + sFilename, ex);
    }
  }

  private static void _readAll (@NonNull final CiiInvoiceReader aReader)
  {
    final ICommonsList <ClassPathResource> aFiles = MockPhloxTestFiles.getCiiD25AFiles ();
    assertEquals (16, aFiles.size ());
    for (final ClassPathResource aRes : aFiles)
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final ErrorList aErrors = new ErrorList ();
        final PhloxInvoice aModel = aReader.read (aIS, aErrors);
        final String sPath = aRes.getPath ();
        assertNotNull ("Reader returned null for " + sPath + " — errors: " + aErrors, aModel);
        assertTrue ("Unexpected errors in " + sPath + ": " + aErrors, aErrors.isEmpty ());
        assertEquals ("Edition of " + sPath, EEN16931Edition.EN2026, aModel.getEdition ());
        assertNotNull ("BT-1 missing in " + sPath, aModel.getNumber ());
        assertNotNull ("BT-2 missing in " + sPath, aModel.getIssueDate ());
        assertNotNull ("BT-5 missing in " + sPath, aModel.getCurrency ());
        assertNotNull ("BT-27 missing in " + sPath, aModel.getSeller ().getName ());
        assertNotNull ("BT-44 missing in " + sPath, aModel.getBuyer ().getName ());
        assertFalse ("BG-25 empty in " + sPath, aModel.lines ().isEmpty ());
        assertEquals ("BT-24 of " + sPath,
                      "urn:cen.eu:en16931:2026",
                      aModel.getProcessControl ().getSpecificationIdentifier ());
      }
      catch (final IOException ex)
      {
        throw new RuntimeException ("Failed to open " + aRes.getPath (), ex);
      }
    }
  }

  @Test
  public void testReadAllExplicit ()
  {
    _readAll (CiiInvoiceReader.fromD25A ());
  }

  @Test
  public void testReadAllAutoDetect ()
  {
    _readAll (CiiInvoiceReader.autoDetect ());
    _readAll (new CiiInvoiceReader ());
  }

  @Test
  public void testAutoDetect2017 () throws IOException
  {
    final ClassPathResource aRes = new ClassPathResource (MockPhloxTestFiles.BASE_PATH + "2017/cii/CII_example1.xml");
    try (final InputStream aIS = aRes.getInputStream ())
    {
      final ErrorList aErrors = new ErrorList ();
      final PhloxInvoice aModel = CiiInvoiceReader.autoDetect ().read (aIS, aErrors);
      assertNotNull ("Errors: " + aErrors, aModel);
      assertEquals (EEN16931Edition.EN2017, aModel.getEdition ());
    }
  }

  @Test
  public void testAutoDetectWithoutBT24Fails ()
  {
    final String sXml = "<rsm:CrossIndustryInvoice xmlns:rsm='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'>" +
                        "<rsm:ExchangedDocument/></rsm:CrossIndustryInvoice>";
    final ErrorList aErrors = new ErrorList ();
    final PhloxInvoice aModel = CiiInvoiceReader.autoDetect ()
                                                .read (new NonBlockingByteArrayInputStream (sXml.getBytes (StandardCharsets.UTF_8)),
                                                       aErrors);
    assertNull (aModel);
    assertEquals (1, aErrors.size ());
    assertTrue (aErrors.getFirstOrNull ().getErrorText (Locale.ROOT).contains ("BT-24"));
  }

  @Test
  public void testNewHeaderTerms ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-new-header-invoice.xml");
    // BT-2 + BT-166
    assertEquals (LocalDate.of (2026, 1, 15), aModel.getIssueDate ());
    assertEquals (OffsetTime.parse ("12:05:03+01:00"), aModel.getIssueTime ());
    // BT-10 + BT-10-1
    assertEquals (1, aModel.buyerReferences ().size ());
    assertEquals ("BUYER-REF-4711", aModel.buyerReferences ().get (0).getValue ());
    assertEquals ("ADE", aModel.buyerReferences ().get (0).getSchemeID ());
    // BT-167 + BT-167-1 + BT-167-2
    assertEquals (0, new BigDecimal ("1.1000").compareTo (aModel.getVatExchangeRate ()));
    assertEquals ("EUR", aModel.getVatExchangeRateTargetCurrency ());
    assertEquals ("USD", aModel.getVatExchangeRateSourceCurrency ());
    // BT-197
    assertEquals ("DELNOTE-8", aModel.getDeliveryNoteReference ());
    // BG-3 + BT-202
    assertEquals (2, aModel.precedingInvoiceRefs ().size ());
    assertEquals ("PREV-INV-1", aModel.precedingInvoiceRefs ().get (0).getNumber ());
    assertEquals ("380", aModel.precedingInvoiceRefs ().get (0).getTypeCode ());
    assertNull (aModel.precedingInvoiceRefs ().get (1).getTypeCode ());
    // BT-215 + BT-216
    assertEquals ("SPSBAT2SXXX", aModel.getPaymentInstructions ().getDirectDebit ().getDebitedAccountProviderID ());
    assertEquals ("Buyer Account", aModel.getPaymentInstructions ().getDirectDebit ().getDebitedAccountName ());
  }

  @Test
  public void testPaymentTerms ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-new-paymentterms-invoice.xml");
    assertEquals (1, aModel.paymentTerms ().size ());
    final PhloxPaymentTerms aPT = aModel.paymentTerms ().get (0);
    assertEquals ("Net 30 days", aPT.getText ());
    // BG-35
    assertEquals (1, aPT.earlyDiscounts ().size ());
    assertEquals (LocalDate.of (2026, 1, 25), aPT.earlyDiscounts ().get (0).getEndDate ());
    assertEquals (0, new BigDecimal ("2.00").compareTo (aPT.earlyDiscounts ().get (0).getPercentage ()));
    assertEquals (0, new BigDecimal ("2.64").compareTo (aPT.earlyDiscounts ().get (0).getAmount ()));
    // BG-36
    assertEquals (1, aPT.latePenalties ().size ());
    assertEquals (LocalDate.of (2026, 2, 15), aPT.latePenalties ().get (0).getStartDate ());
    assertEquals (0, new BigDecimal ("9.20").compareTo (aPT.latePenalties ().get (0).getYearlyInterestPercentage ()));
    assertEquals (0, new BigDecimal ("12.50").compareTo (aPT.latePenalties ().get (0).getAmount ()));
    // BT-9
    assertEquals (LocalDate.of (2026, 2, 14), aModel.getPaymentDueDate ());
  }

  @Test
  public void testThirdPartyCharges ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-new-bg34-invoice.xml");
    assertEquals (2, aModel.getDocumentTotals ().thirdPartyCharges ().size ());
    assertEquals (0, new BigDecimal ("3.20").compareTo (aModel.getDocumentTotals ().thirdPartyCharges ().get (0).getAmount ()));
    assertEquals ("Copyright levy", aModel.getDocumentTotals ().thirdPartyCharges ().get (0).getSpecification ());
    assertEquals ("Recycling fee", aModel.getDocumentTotals ().thirdPartyCharges ().get (1).getSpecification ());
  }

  @Test
  public void testLineDelivery ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-new-linedelivery-invoice.xml");
    final PhloxInvoiceLine aLine = aModel.lines ().get (0);
    assertNotNull (aLine.getDelivery ());
    // BT-185 + BT-186 + BT-186-1 + BT-187
    assertEquals ("Line Delivery Site", aLine.getDelivery ().getPartyName ());
    assertEquals ("4035813333333", aLine.getDelivery ().getLocationIdentifier ().getValue ());
    assertEquals ("0088", aLine.getDelivery ().getLocationIdentifier ().getSchemeID ());
    assertEquals (LocalDate.of (2026, 1, 13), aLine.getDelivery ().getActualDeliveryDate ());
    // BG-38
    assertEquals ("Line Delivery Road 1", aLine.getDelivery ().getPostalAddress ().getLine1 ());
    assertEquals ("Gate 5", aLine.getDelivery ().getPostalAddress ().getLine2 ());
    assertEquals ("Dock C", aLine.getDelivery ().getPostalAddress ().getLine3 ());
    assertEquals ("Innsbruck", aLine.getDelivery ().getPostalAddress ().getCity ());
    assertEquals ("6020", aLine.getDelivery ().getPostalAddress ().getPostCode ());
    assertEquals ("Tirol", aLine.getDelivery ().getPostalAddress ().getCountrySubdivision ());
    assertEquals ("AT", aLine.getDelivery ().getPostalAddress ().getCountryCode ());
    // BT-194 + BT-195 + BT-196
    assertEquals ("Item exempt", aLine.getLineVat ().getExemptionReasonText ());
    assertEquals ("VATEX-EU-G", aLine.getLineVat ().getExemptionReasonCode ());
    assertEquals ("SUPPLY-L", aLine.getItem ().getGoodsServicesCode ());
  }

  @Test
  public void testLineReferences ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-new-lineref-invoice.xml");
    final PhloxInvoiceLine aLine = aModel.lines ().get (0);
    assertEquals ("LINE-PO-1", aLine.getPurchaseOrderReference ());
    assertEquals ("PO-LINE-5", aLine.getPurchaseOrderLineRef ());
    assertEquals ("LINE-SO-1", aLine.getSalesOrderReference ());
    assertEquals ("SO-LINE-9", aLine.getSalesOrderLineReference ());
    assertEquals ("LINE-DESP-1", aLine.getDespatchAdviceReference ());
    assertEquals ("DESP-LINE-2", aLine.getDespatchAdviceLineReference ());
    assertEquals ("LINE-RECV-1", aLine.getReceivingAdviceReference ());
    assertEquals ("RECV-LINE-3", aLine.getReceivingAdviceLineReference ());
    assertEquals ("LINE-DELN-1", aLine.getDeliveryNoteReference ());
    assertEquals ("DELN-LINE-4", aLine.getDeliveryNoteLineReference ());
    // BG-39
    assertEquals (1, aLine.precedingInvoiceRefs ().size ());
    assertEquals ("LINE-PREV-INV-1", aLine.precedingInvoiceRefs ().get (0).getNumber ());
    assertEquals (LocalDate.of (2025, 12, 10), aLine.precedingInvoiceRefs ().get (0).getIssueDate ());
    assertEquals ("380", aLine.precedingInvoiceRefs ().get (0).getTypeCode ());
    assertEquals ("PREV-LINE-7", aLine.precedingInvoiceRefs ().get (0).getLineReference ());
  }

  @Test
  public void testVatAccountingAmountIsBt111 ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-edge-payment-invoice.xml");
    assertEquals ("USD", aModel.getVatAccountingCurrency ());
    assertNotNull (aModel.getDocumentTotals ().getTaxTotal ());
    assertNotNull (aModel.getDocumentTotals ().getTaxTotalAccountingCurrency ());
  }

  @Test
  public void testDirectDebitWithoutMandateReference ()
  {
    final PhloxInvoice aModel = _read (CiiInvoiceReader.fromD25A (), "d25a-edge-directdebit-invoice.xml");
    final PhloxDirectDebit aDD = aModel.getPaymentInstructions ().getDirectDebit ();
    assertNotNull (aDD);
    assertNull (aDD.getMandateReferenceID ());
    assertEquals ("SEPA-CRED-1", aDD.getBankAssignedCreditorID ());
    assertEquals ("DEBTOR-12345", aDD.getDebitedAccountID ());
    assertEquals ("Buyer Account", aDD.getDebitedAccountName ());
  }
}
