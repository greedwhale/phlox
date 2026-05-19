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
package com.helger.norma.cii;

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
 * Smoke tests for {@link CIIInvoiceReader} — make sure every CII file in the corpus parses
 * without fatal errors.
 */
public final class CIIInvoiceReaderTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIInvoiceReaderTest.class);

  @Test
  public void testReadAllCIIFiles ()
  {
    final CIIInvoiceReader aReader = new CIIInvoiceReader ();
    int nCount = 0;
    for (final ClassPathResource aRes : MockNormaTestFiles.getCIID16BFiles ())
    {
      try (final InputStream aIS = aRes.getInputStream ())
      {
        final ErrorList aErrors = new ErrorList ();
        final NormaInvoice aModel = aReader.read (aIS, aErrors);
        assertNotNull ("Failed to parse " + aRes.getPath () + " — errors: " + aErrors, aModel);
        assertNotNull ("Missing invoice number in " + aRes.getPath (), aModel.getNumber ());
        assertNotNull ("Missing issue date in " + aRes.getPath (), aModel.getIssueDate ());
        nCount++;
      }
      catch (final java.io.IOException ex)
      {
        throw new RuntimeException ("Failed to open " + aRes.getPath (), ex);
      }
    }
    LOGGER.info ("Parsed " + nCount + " CII D16B example files");
    assertTrue ("Expected at least 10 CII examples", nCount >= 10);
  }
}
