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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.font.roboto.EFontResourceRoboto;
import com.helger.io.file.SimpleFileIO;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.testfiles.MockPhloxTestFiles;
import com.helger.phlox.theme.PhloxThemeReader;
import com.helger.phlox.ubl.UblInvoiceReader;

/**
 * Test that renders the synthetic invoices of {@link MockPhloxTestFiles} (many lines, Cyrillic and
 * Greek, a currency without decimals) and an invoice with a logo to PDF. The PDFs are written to
 * <code>generated/synthetic</code>.
 *
 * @author Philip Helger
 */
public final class PdfSyntheticInvoiceTest
{
  private static PhloxInvoice _read (final String sPath)
  {
    try (final InputStream aIS = new ClassPathResource (MockPhloxTestFiles.BASE_PATH + sPath).getInputStream ())
    {
      final PhloxInvoice aModel = UblInvoiceReader.fromUbl21 ().read (aIS, new ErrorList ());
      assertNotNull ("Failed to parse " + sPath, aModel);
      return aModel;
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to open " + sPath, ex);
    }
  }

  private static byte [] _render (final PhloxInvoice aModel, final PdfRenderContext aCtx, final String sName)
  {
    try
    {
      final NonBlockingByteArrayOutputStream aBaos = new NonBlockingByteArrayOutputStream ();
      new PdfInvoiceWriter ().writeTo (aModel, aCtx, aBaos);
      final byte [] ret = aBaos.toByteArray ();
      SimpleFileIO.writeFile (new File ("generated/synthetic/" + sName + ".pdf"), ret);
      return ret;
    }
    catch (final Exception ex)
    {
      throw new IllegalStateException ("Failed to render the PDF " + sName, ex);
    }
  }

  private static String _text (final byte [] aPdf)
  {
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      return new PDFTextStripper ().getText (aDoc);
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to read the PDF", ex);
    }
  }

  private static int _pageCount (final byte [] aPdf)
  {
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      return aDoc.getNumberOfPages ();
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to read the PDF", ex);
    }
  }

  private static ICommonsList <String> _fontNames (final byte [] aPdf)
  {
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      final ICommonsList <String> ret = new CommonsArrayList <> ();
      for (final PDPage aPage : aDoc.getPages ())
      {
        final PDResources aResources = aPage.getResources ();
        for (final COSName aName : aResources.getFontNames ())
          ret.add (aResources.getFont (aName).getName ());
      }
      return ret;
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to read the PDF", ex);
    }
  }

  private static PdfFontFamily _roboto ()
  {
    return PdfFontFamily.of (EFontResourceRoboto.ROBOTO_NORMAL,
                             EFontResourceRoboto.ROBOTO_BOLD,
                             EFontResourceRoboto.ROBOTO_NORMAL_ITALIC,
                             EFontResourceRoboto.ROBOTO_BOLD_ITALIC);
  }

  private static int _imageCount (final byte [] aPdf, final int nPage)
  {
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      int nCount = 0;
      final PDResources aResources = aDoc.getPage (nPage).getResources ();
      for (final COSName aName : aResources.getXObjectNames ())
        if (aResources.isImageXObject (aName))
          nCount++;
      return nCount;
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to read the PDF", ex);
    }
  }

  private static String _pageText (final byte [] aPdf, final int nPage)
  {
    try (final PDDocument aDoc = Loader.loadPDF (aPdf))
    {
      final PDFTextStripper aStripper = new PDFTextStripper ();
      aStripper.setStartPage (nPage);
      aStripper.setEndPage (nPage);
      return aStripper.getText (aDoc);
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException ("Failed to read the PDF", ex);
    }
  }

  @Test
  public void testLongInvoice ()
  {
    final PhloxInvoice aModel = _read (MockPhloxTestFiles.SYNTHETIC_LONG_INVOICE);
    assertEquals (120, aModel.lines ().size ());
    for (final Locale aLocale : new Locale [] { Locale.ENGLISH, Locale.GERMAN })
    {
      final byte [] aPdf = _render (aModel,
                                    new PdfRenderContext ().setLocale (aLocale),
                                    "long-" + aLocale.getLanguage ());
      final int nPages = _pageCount (aPdf);
      assertTrue ("Pages: " + nPages, nPages >= 5);
      assertTrue (_pageText (aPdf, 1).contains ("PHLOX-LONG-1"));
      // The first and the last line are there, and the totals are after the last line
      final String sText = _text (aPdf);
      assertTrue (sText.contains ("Hex bolt type A1"));
      assertTrue (sText.contains ("Relay type P5"));
      assertTrue (sText.indexOf ("Relay type P5") < sText.lastIndexOf ("83,276.64") ||
                  sText.indexOf ("Relay type P5") < sText.lastIndexOf ("83.276,64"));

      // The header of the table is repeated on every page that continues it
      final String sHeader = aLocale.equals (Locale.GERMAN) ? "BESCHREIBUNG" : "DESCRIPTION";
      for (int nPage = 2; nPage < nPages - 1; nPage++)
        assertTrue ("Page " + nPage + " has no table header", _pageText (aPdf, nPage).contains (sHeader));
    }
  }

  @Test
  public void testCyrillicAndGreekNeedAnEmbeddedFont ()
  {
    final PhloxInvoice aModel = _read (MockPhloxTestFiles.SYNTHETIC_CYRILLIC_GREEK_INVOICE);

    // The standard fonts of PDF contain neither of the scripts
    final byte [] aStandard = _render (aModel, new PdfRenderContext (), "cyrillic-greek-standard-fonts");
    assertFalse (_text (aStandard).contains ("Фотон"));
    assertFalse (_text (aStandard).contains ("Ελληνική"));

    // A font with the characters, set in the API
    final byte [] aApi = _render (aModel, new PdfRenderContext ().setFontFamily (_roboto ()), "cyrillic-greek-en");
    final String sApi = _text (aApi);
    assertTrue (sApi, sApi.contains ("Фотон Електроникс ООД"));
    assertTrue (sApi, sApi.contains ("ул. Околовръстен път 118"));
    assertTrue (sApi, sApi.contains ("Ελληνική Βιομηχανία Αισθητήρων Α.Ε."));
    assertTrue (sApi, sApi.contains ("Αισθητήρας θερμοκρασίας"));
    assertTrue (sApi, sApi.contains ("Доставка до склада на купувача. / Παράδοση στην αποθήκη του αγοραστή."));
    assertFalse (sApi, sApi.contains ("?"));
    final ICommonsList <String> aFonts = _fontNames (aApi);
    assertTrue (aFonts.toString (), aFonts.containsAny (x -> x.endsWith ("Roboto-Regular")));
    assertTrue (aFonts.toString (), aFonts.containsAny (x -> x.endsWith ("Roboto-Bold")));

    // The same from the font files of a theme, in German
    final String sRoboto = "classpath:fonts/ttf/Roboto/Roboto-";
    final PdfRenderContext aCtx = new PdfRenderContext ().applyTheme (PhloxThemeReader.readFromString ("{\"common\":{\"locale\":\"de\"},"
                                                                                                       + "\"pdf\":{\"fonts\":{\"regular\":\"" + sRoboto + "Regular.ttf\","
                                                                                                       + "\"bold\":\"" + sRoboto + "Bold.ttf\","
                                                                                                       + "\"italic\":\"" + sRoboto + "Italic.ttf\","
                                                                                                       + "\"boldItalic\":\"" + sRoboto + "BoldItalic.ttf\"}}}",
                                                                                                       null));
    final String sTheme = _text (_render (aModel, aCtx, "cyrillic-greek-de"));
    assertTrue (sTheme, sTheme.contains ("Фотон Електроникс ООД"));
    assertTrue (sTheme, sTheme.contains ("Ελληνική Βιομηχανία Αισθητήρων Α.Ε."));
    assertTrue (sTheme, sTheme.contains ("Rechnung"));
  }

  @Test
  public void testYenHasNoDecimals ()
  {
    final PhloxInvoice aModel = _read (MockPhloxTestFiles.SYNTHETIC_JPY_INVOICE);
    final String sEn = _text (_render (aModel, new PdfRenderContext ().setLocale (Locale.ENGLISH), "jpy-en"));
    assertTrue (sEn, sEn.contains ("1,250,000"));
    assertTrue (sEn, sEn.contains ("2,245,608"));
    assertFalse (sEn, sEn.contains ("1,250,000.00"));

    final String sDe = _text (_render (aModel, new PdfRenderContext ().setLocale (Locale.GERMAN), "jpy-de"));
    assertTrue (sDe, sDe.contains ("1.250.000"));
    assertTrue (sDe, sDe.contains ("2.245.608"));
    assertFalse (sDe, sDe.contains ("1.250.000,00"));
  }

  @Test
  public void testLogo () throws IOException
  {
    final PhloxInvoice aModel = _read ("2017/ubl/invoice/ubl-tc434-example1.xml");
    final PhloxAttachment aLogo;
    try (final InputStream aIS = MockPhloxTestFiles.getLogo ().getInputStream ())
    {
      aLogo = new PhloxAttachment ().setBinary (aIS.readAllBytes ()).setMimeCode ("image/png").setFilename ("phoss.png");
    }
    final byte [] aWithout = _render (aModel, new PdfRenderContext (), "logo-without");
    final byte [] aWith = _render (aModel, new PdfRenderContext ().setLogo (aLogo), "logo-with");
    assertEquals (0, _imageCount (aWithout, 0));
    assertEquals (1, _imageCount (aWith, 0));
  }
}
