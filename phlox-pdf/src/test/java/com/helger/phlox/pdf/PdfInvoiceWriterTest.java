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
package com.helger.phlox.pdf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.base.string.StringHelper;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.file.SimpleFileIO;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.testfiles.MockPhloxTestFiles;
import com.helger.phlox.ubl.UblInvoiceReader;

/** End-to-end smoke test: parse UBL → render PDF in EN and DE. */
public final class PdfInvoiceWriterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PdfInvoiceWriterTest.class);

  @Test
  public void testRenderUblToPdf () throws Exception
  {
    final UblInvoiceReader aReader = UblInvoiceReader.fromUbl21 ();
    final PdfInvoiceWriter aWriter = new PdfInvoiceWriter ();
    int nCount = 0;
    for (final ClassPathResource aRes : MockPhloxTestFiles.getUbl21InvoiceFiles ())
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final PhloxInvoice aModel = aReader.read (aIS, new ErrorList ());
        assertNotNull ("Failed to parse " + aRes.getPath (), aModel);
        for (final Locale aLocale : new Locale [] { Locale.ENGLISH, Locale.GERMAN })
        {
          final PdfRenderContext aCtx = new PdfRenderContext ().setLocale (aLocale)
                                                               .setUserWatermark (WatermarkSpec.text ("DRAFT"));
          final NonBlockingByteArrayOutputStream aBaos = new NonBlockingByteArrayOutputStream ();
          aWriter.writeTo (aModel, aCtx, aBaos);
          assertTrue ("Empty PDF for " + aRes.getPath () + " / " + aLocale, aBaos.size () > 500);
          // Confirm header: PDF magic
          final byte [] aBytes = aBaos.toByteArray ();
          assertTrue ("Output is not a PDF: " + aRes.getPath (),
                      aBytes[0] == '%' && aBytes[1] == 'P' && aBytes[2] == 'D' && aBytes[3] == 'F');

          final String sOutputPath = "generated/" +
                                     StringHelper.trimStart (aRes.getPath (), "external/") +
                                     "-" +
                                     aLocale.getLanguage () +
                                     ".pdf";
          SimpleFileIO.writeFile (new File (sOutputPath), aBytes);
        }
        nCount++;
      }
    }
    LOGGER.info ("Rendered " + nCount + " UBL invoices to PDF (×2 locales)");
    assertTrue ("Expected at least 10 invoices rendered", nCount >= 10);
  }
}
