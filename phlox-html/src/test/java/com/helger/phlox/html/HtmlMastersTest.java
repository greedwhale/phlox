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
package com.helger.phlox.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.base.string.StringHelper;
import com.helger.base.string.StringReplace;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.CPhloxVersion;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.cii.CiiInvoiceReader;
import com.helger.phlox.testfiles.MockPhloxTestFiles;
import com.helger.phlox.ubl.UblInvoiceReader;

/**
 * Compares the HTML of the invoices of {@link MockPhloxTestFiles#getMasterFiles()} with the
 * reference files in <code>src/test/resources/masters</code>, in English and German. A change of
 * the output shows as a difference of one of the files. If the change is wanted, write the
 * reference files again with
 * <code>mvn test -Dtest=HtmlMastersTest -Dphlox.updateMasters=true</code> and look at the
 * difference in the version control system.
 *
 * @author Philip Helger
 */
public final class HtmlMastersTest
{
  private static final String UPDATE_PROPERTY = "phlox.updateMasters";
  private static final File MASTER_DIR = new File ("src/test/resources/masters");
  private static final String LOGO_CASE = "2017/ubl/invoice/ubl-tc434-example1.xml";

  @NonNull
  private static PhloxInvoice _read (@NonNull final String sPath)
  {
    try (final InputStream aIS = new ClassPathResource (MockPhloxTestFiles.BASE_PATH + sPath).getInputStream ())
    {
      final ErrorList aErrors = new ErrorList ();
      // Some of the examples do not contain BT-24, so the edition is selected by the folder
      final boolean b2026 = sPath.startsWith ("2026/");
      final PhloxInvoice aInvoice = sPath.contains ("/cii/") ? (b2026 ? CiiInvoiceReader.fromD25A ()
                                                                      : CiiInvoiceReader.fromD16B ()).read (aIS,
                                                                                                            aErrors)
                                                             : (b2026 ? UblInvoiceReader.fromUbl25 ()
                                                                      : UblInvoiceReader.fromUbl21 ()).read (aIS,
                                                                                                             aErrors);
      assertNotNull ("Failed to read " + sPath + ": " + aErrors, aInvoice);
      return aInvoice;
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to open " + sPath, ex);
    }
  }

  @NonNull
  private static String _masterName (@NonNull final String sPath, @NonNull final String sLang, final boolean bLogo)
  {
    return StringReplace.replaceAll (sPath.substring (0, sPath.length () - ".xml".length ()), '/', '_') +
           (bLogo ? "-logo" : "") +
           "-" +
           sLang +
           ".html";
  }

  private static void _compare (@NonNull final String sPath,
                                @NonNull final String sLang,
                                final boolean bLogo) throws IOException
  {
    final HtmlRenderContext aCtx = new HtmlRenderContext ().setLocale (Locale.forLanguageTag (sLang));
    if (bLogo)
    {
      try (final InputStream aIS = MockPhloxTestFiles.getLogo ().getInputStream ())
      {
        aCtx.setLogo (new PhloxAttachment ().setBinary (aIS.readAllBytes ())
                                            .setMimeCode ("image/png")
                                            .setFilename ("phoss.png"));
      }
    }
    // The version changes with every release
    String sActual = new HtmlInvoiceWriter ().writeAsString (_read (sPath), aCtx);
    sActual = StringReplace.replaceAll (sActual, CPhloxVersion.BUILD_VERSION, "@VERSION@");

    final File aMaster = new File (MASTER_DIR, _masterName (sPath, sLang, bLogo));
    if (Boolean.getBoolean (UPDATE_PROPERTY))
    {
      MASTER_DIR.mkdirs ();
      Files.writeString (aMaster.toPath (), sActual, StandardCharsets.UTF_8);
    }
    else
    {
      assertTrue ("There is no reference file " +
                  aMaster.getPath () +
                  " - create it with -D" +
                  UPDATE_PROPERTY +
                  "=true",
                  aMaster.isFile ());
      assertEquals ("The HTML differs from the reference file " +
                    aMaster.getPath () +
                    " - if the change is wanted, write the files again with -D" +
                    UPDATE_PROPERTY +
                    "=true",
                    Files.readString (aMaster.toPath (), StandardCharsets.UTF_8),
                    sActual);
    }
  }

  @Test
  public void testMasters () throws IOException
  {
    final ICommonsList <String> aFailures = new CommonsArrayList <> ();
    for (final String sPath : MockPhloxTestFiles.getMasterFiles ())
      for (final String sLang : new String [] { "en", "de" })
        try
        {
          _compare (sPath, sLang, false);
        }
        catch (final AssertionError ex)
        {
          aFailures.add (StringHelper.getFirstToken (ex.getMessage (), '\n'));
        }
    try
    {
      _compare (LOGO_CASE, "en", true);
    }
    catch (final AssertionError ex)
    {
      aFailures.add (StringHelper.getFirstToken (ex.getMessage (), '\n'));
    }
    assertTrue (aFailures.size () + " of the reference files differ:\n" + String.join ("\n", aFailures),
                aFailures.isEmpty ());
  }
}
