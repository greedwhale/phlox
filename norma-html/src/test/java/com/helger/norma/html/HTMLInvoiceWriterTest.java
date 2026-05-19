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
package com.helger.norma.html;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.resource.ClassPathResource;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.api.watermark.WatermarkSpec;
import com.helger.norma.testfiles.MockNormaTestFiles;
import com.helger.norma.ubl.UBLInvoiceReader;

/** End-to-end smoke test: parse UBL → render HTML in EN and DE. */
public final class HTMLInvoiceWriterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (HTMLInvoiceWriterTest.class);

  @Test
  public void testRenderUBLToHTML ()
  {
    final UBLInvoiceReader aReader = UBLInvoiceReader.fromUBL21 ();
    final HTMLInvoiceWriter aWriter = new HTMLInvoiceWriter ();
    int nCount = 0;
    for (final ClassPathResource aRes : MockNormaTestFiles.getUBL21InvoiceFiles ())
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final NormaInvoice aModel = aReader.read (aIS, new ErrorList ());
        assertNotNull ("Failed to parse " + aRes.getPath (), aModel);
        for (final Locale aLocale : new Locale [] { Locale.ENGLISH, Locale.GERMAN })
        {
          final HTMLRenderContext aCtx = new HTMLRenderContext ().setLocale (aLocale)
                                                                 .setUserWatermark (WatermarkSpec.text ("DRAFT"));
          final String sHtml = aWriter.writeAsString (aModel, aCtx);
          assertNotNull (sHtml);
          assertTrue ("HTML output empty for " + aRes.getPath () + " / " + aLocale, sHtml.length () > 500);
          assertTrue ("Missing invoice number in HTML for " + aRes.getPath (), sHtml.contains (aModel.getNumber ()));
        }
        nCount++;
      }
      catch (final java.io.IOException ex)
      {
        throw new RuntimeException ("Failed to open " + aRes.getPath (), ex);
      }
    }
    LOGGER.info ("Rendered " + nCount + " UBL invoices to HTML (×2 locales)");
    assertTrue ("Expected at least 10 invoices rendered", nCount >= 10);
  }
}
