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
package com.helger.norma.ubl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.resource.ClassPathResource;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.testfiles.MockNormaTestFiles;

/**
 * Smoke tests for {@link UBLInvoiceReader} — make sure every file in the corpus parses without
 * fatal errors and produces a non-empty model.
 */
public final class UBLInvoiceReaderTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (UBLInvoiceReaderTest.class);

  @Test
  public void testReadAllInvoices ()
  {
    final UBLInvoiceReader aReader = UBLInvoiceReader.fromUBL21 ();
    int nCount = 0;
    for (final ClassPathResource aRes : MockNormaTestFiles.getUBL21InvoiceFiles ())
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final ErrorList aErrors = new ErrorList ();
        final NormaInvoice aModel = aReader.read (aIS, aErrors);
        assertNotNull ("Failed to parse " + aRes.getPath () + " — errors: " + aErrors, aModel);
        assertTrue ("Parsed " + aRes.getPath () + " but produced errors: " + aErrors,
                    !aErrors.containsAtLeastOneError ());
        assertNotNull ("Missing invoice number in " + aRes.getPath (), aModel.getNumber ());
        nCount++;
      }
      catch (final java.io.IOException ex)
      {
        throw new RuntimeException ("Failed to open " + aRes.getPath (), ex);
      }
    }
    LOGGER.info ("Parsed " + nCount + " UBL 2.1 Invoice example files");
    assertTrue ("Expected at least 10 invoice examples", nCount >= 10);
  }

  @Test
  public void testReadAllCreditNotes ()
  {
    final UBLInvoiceReader aReader = UBLInvoiceReader.fromUBL21 ();
    int nCount = 0;
    for (final ClassPathResource aRes : MockNormaTestFiles.getUBL21CreditNoteFiles ())
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final ErrorList aErrors = new ErrorList ();
        final NormaInvoice aModel = aReader.read (aIS, aErrors);
        assertNotNull ("Failed to parse " + aRes.getPath () + " — errors: " + aErrors, aModel);
        assertTrue ("Parsed " + aRes.getPath () + " but produced errors: " + aErrors,
                    !aErrors.containsAtLeastOneError ());
        assertTrue ("Should be a credit note: " + aRes.getPath (), aModel.isCreditNote ());
        nCount++;
      }
      catch (final java.io.IOException ex)
      {
        throw new RuntimeException ("Failed to open " + aRes.getPath (), ex);
      }
    }
    LOGGER.info ("Parsed " + nCount + " UBL 2.1 CreditNote example files");
  }
}
