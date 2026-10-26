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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.font.roboto.EFontResourceRoboto;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.CPhloxVersion;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.cii.CiiInvoiceReader;
import com.helger.phlox.testfiles.MockPhloxTestFiles;
import com.helger.phlox.ubl.UblInvoiceReader;

/**
 * Compares the PDFs of the invoices of {@link MockPhloxTestFiles#getMasterFiles()} with the
 * reference files in <code>src/test/resources/masters</code>, in English and German. A reference
 * file is the description of the layout of a PDF as text: the pages, every piece of text with its
 * position, font and size, and every line, box and colour that is drawn. That makes a difference
 * readable in the version control system, and it does not depend on the date, on the platform or on
 * how a viewer draws the page. If a change is wanted, write the reference files again with
 * <code>mvn test -Dtest=PdfMastersTest -Dphlox.updateMasters=true</code>.
 *
 * @author Philip Helger
 */
public final class PdfMastersTest
{
  private static final String UPDATE_PROPERTY = "phlox.updateMasters";
  private static final File MASTER_DIR = new File ("src/test/resources/masters");
  private static final String LOGO_CASE = "2017/ubl/invoice/ubl-tc434-example1.xml";
  // Drawing operators of the content stream of a page, without the text operators
  private static final Set <String> GRAPHICS_OPERATORS = new CommonsHashSet <> ("re",
                                                                                "m",
                                                                                "l",
                                                                                "c",
                                                                                "h",
                                                                                "S",
                                                                                "s",
                                                                                "f",
                                                                                "F",
                                                                                "f*",
                                                                                "B",
                                                                                "B*",
                                                                                "b",
                                                                                "b*",
                                                                                "rg",
                                                                                "RG",
                                                                                "g",
                                                                                "G",
                                                                                "k",
                                                                                "K",
                                                                                "sc",
                                                                                "scn",
                                                                                "SC",
                                                                                "SCN",
                                                                                "w",
                                                                                "d",
                                                                                "Do");

  private static final class TextCollector extends PDFTextStripper
  {
    private final ICommonsList <String> m_aLines;

    TextCollector (@NonNull final ICommonsList <String> aLines)
    {
      m_aLines = aLines;
    }

    @Override
    protected void writeString (final String sText, final List <TextPosition> aPositions) throws IOException
    {
      if (!aPositions.isEmpty ())
      {
        final TextPosition aFirst = aPositions.get (0);
        m_aLines.add ("T p" +
                      getCurrentPageNo () +
                      " " +
                      _format (aFirst.getXDirAdj ()) +
                      " " +
                      _format (aFirst.getYDirAdj ()) +
                      " " +
                      _format (aFirst.getFontSizeInPt ()) +
                      " " +
                      // The prefix of a subset font is not part of the layout
                      aFirst.getFont ().getName ().replaceFirst ("^[A-Z]{6}\\+", "") +
                      " " +
                      sText.replace (CPhloxVersion.BUILD_VERSION, "@VERSION@"));
      }
    }
  }

  @NonNull
  private static String _format (final float fValue)
  {
    return String.format (Locale.ROOT, "%.1f", Float.valueOf (fValue));
  }

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
  private static String _describe (final byte @NonNull [] aPdf) throws IOException
  {
    final ICommonsList <String> aLines = new CommonsArrayList <> ();
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      aLines.add ("pages " + aDoc.getNumberOfPages ());
      int nPage = 0;
      for (final PDPage aPage : aDoc.getPages ())
      {
        nPage++;
        aLines.add ("page " +
                    nPage +
                    " " +
                    _format (aPage.getMediaBox ().getWidth ()) +
                    "x" +
                    _format (aPage.getMediaBox ().getHeight ()));
        final List <Object> aTokens = new PDFStreamParser (aPage).parse ();
        final StringBuilder aOperands = new StringBuilder ();
        for (final Object aToken : aTokens)
        {
          if (aToken instanceof final Operator aOperator)
          {
            if (GRAPHICS_OPERATORS.contains (aOperator.getName ()))
              aLines.add ("G p" + nPage + " " + aOperator.getName () + aOperands);
            aOperands.setLength (0);
          }
          else
            if (aToken instanceof final COSNumber aNumber)
              aOperands.append (' ')
                       .append (String.format (Locale.ROOT, "%.2f", Float.valueOf (aNumber.floatValue ())));
            else
              if (aToken instanceof final COSName aName)
                aOperands.append (" /").append (aName.getName ());
              else
                if (aToken instanceof final COSBase aBase)
                  aOperands.append (' ').append (aBase.getClass ().getSimpleName ());
        }
      }
      final TextCollector aStripper = new TextCollector (aLines);
      aStripper.getText (aDoc);
    }
    return String.join ("\n", aLines) + "\n";
  }

  @NonNull
  private static String _masterName (@NonNull final String sPath, @NonNull final String sLang, final boolean bLogo)
  {
    return sPath.substring (0, sPath.length () - ".xml".length ()).replace ('/', '_') +
           (bLogo ? "-logo" : "") +
           "-" +
           sLang +
           ".txt";
  }

  private static void _compare (@NonNull final String sPath,
                                @NonNull final String sLang,
                                final boolean bLogo) throws Exception
  {
    final PdfRenderContext aCtx = new PdfRenderContext ().setLocale (Locale.forLanguageTag (sLang));
    // The standard fonts of PDF have no Cyrillic and Greek letters
    if (MockPhloxTestFiles.SYNTHETIC_CYRILLIC_GREEK_INVOICE.equals (sPath))
      aCtx.setFontFamily (PdfFontFamily.of (EFontResourceRoboto.ROBOTO_NORMAL,
                                            EFontResourceRoboto.ROBOTO_BOLD,
                                            EFontResourceRoboto.ROBOTO_NORMAL_ITALIC,
                                            EFontResourceRoboto.ROBOTO_BOLD_ITALIC));
    if (bLogo)
    {
      try (final InputStream aIS = MockPhloxTestFiles.getLogo ().getInputStream ())
      {
        aCtx.setLogo (new PhloxAttachment ().setBinary (aIS.readAllBytes ())
                                            .setMimeCode ("image/png")
                                            .setFilename ("phoss.png"));
      }
    }
    final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ();
    new PdfInvoiceWriter ().writeTo (_read (sPath), aCtx, aBAOS);
    final String sActual = _describe (aBAOS.toByteArray ());

    final File aMaster = new File (MASTER_DIR, _masterName (sPath, sLang, bLogo));
    if (Boolean.getBoolean (UPDATE_PROPERTY))
    {
      MASTER_DIR.mkdirs ();
      Files.writeString (aMaster.toPath (), sActual, StandardCharsets.UTF_8);
      return;
    }
    assertTrue ("There is no reference file " + aMaster.getPath () + " - create it with -D" + UPDATE_PROPERTY + "=true",
                aMaster.isFile ());
    assertEquals ("The PDF differs from the reference file " +
                  aMaster.getPath () +
                  " - if the change is wanted, write the files again with -D" +
                  UPDATE_PROPERTY +
                  "=true",
                  Files.readString (aMaster.toPath (), StandardCharsets.UTF_8),
                  sActual);
  }

  @Test
  public void testMasters () throws Exception
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
          aFailures.add (ex.getMessage ().split ("\n")[0]);
        }
    try
    {
      _compare (LOGO_CASE, "en", true);
    }
    catch (final AssertionError ex)
    {
      aFailures.add (ex.getMessage ().split ("\n")[0]);
    }
    assertTrue (aFailures.size () + " of the reference files differ:\n" + String.join ("\n", aFailures),
                aFailures.isEmpty ());
  }
}
