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
package com.helger.phlox.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.font.roboto.EFontResourceRoboto;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.testfiles.MockPhloxTestFiles;

import picocli.CommandLine;

/**
 * Test class for class {@link PhloxCli}.
 *
 * @author Philip Helger
 */
public final class PhloxCliTest
{
  @NonNull
  private static Path _copyInvoice () throws IOException
  {
    final Path aFile = Files.createTempFile ("phlox-cli-invoice-", ".xml");
    aFile.toFile ().deleteOnExit ();
    try (final InputStream aIS = new ClassPathResource (MockPhloxTestFiles.BASE_PATH +
                                                        "2017/ubl/invoice/ubl-tc434-example1.xml").getInputStream ())
    {
      Files.write (aFile, aIS.readAllBytes ());
    }
    return aFile;
  }

  @NonNull
  private static Path _tempFile (final String sSuffix) throws IOException
  {
    final Path aFile = Files.createTempFile ("phlox-cli-", sSuffix);
    aFile.toFile ().deleteOnExit ();
    return aFile;
  }

  private static int _run (final String... aArgs)
  {
    return new CommandLine (new PhloxCli ()).execute (aArgs);
  }

  @Test
  public void testDefaultTemplateIsTheDefault () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString ()));
    final String sHtml = Files.readString (aOut, StandardCharsets.UTF_8);
    assertTrue (sHtml.contains ("class=\"phlox-table\""));
  }

  @Test
  public void testDefaultTemplate () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString (), "--template", "default"));
    final String sHtml = Files.readString (aOut, StandardCharsets.UTF_8);
    assertTrue (sHtml.contains ("class=\"phlox-table\""));
  }

  @Test
  public void testTemplateFile () throws IOException
  {
    final Path aTemplate = _tempFile (".ptpl");
    Files.writeString (aTemplate, "# Mine {{BT-1}}\n", StandardCharsets.UTF_8);
    final Path aOut = _tempFile (".html");
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString (), "-t", aTemplate.toString ()));
    assertTrue (Files.readString (aOut, StandardCharsets.UTF_8).contains (">Mine 12115118</h1>"));
  }

  @Test
  public void testClasspathTemplate () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (0,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "-o",
                        aOut.toString (),
                        "--template",
                        "classpath:templates/default.ptpl"));
    assertTrue (Files.readString (aOut, StandardCharsets.UTF_8).contains ("class=\"phlox-table\""));
  }

  @Test
  public void testTemplateForPdf () throws IOException
  {
    final Path aOut = _tempFile (".pdf");
    assertEquals (0,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "-f",
                        "PDF",
                        "-o",
                        aOut.toString (),
                        "--template",
                        "default"));
    final byte [] aBytes = Files.readAllBytes (aOut);
    assertTrue (aBytes.length > 500 && aBytes[0] == '%' && aBytes[1] == 'P' && aBytes[2] == 'D' && aBytes[3] == 'F');
  }

  @Test
  public void testMissingTemplateIsAnError () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (2,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "-o",
                        aOut.toString (),
                        "--template",
                        "/no/such/template.ptpl"));
  }

  @Test
  public void testTheme () throws IOException
  {
    final Path aTheme = _tempFile (".json");
    Files.writeString (aTheme,
                       "{\"$comment\":\"test\",\"variables\":{\"--phlox-accent\":\"#8a1538\"},\"common\":{\"locale\":\"de\"}}",
                       StandardCharsets.UTF_8);
    final Path aOut = _tempFile (".html");
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString (), "--theme", aTheme.toString ()));
    final String sHtml = Files.readString (aOut, StandardCharsets.UTF_8);
    // The language and the colour come from the theme
    assertTrue (sHtml.contains ("Rechnung"));
    assertTrue (sHtml.contains ("--phlox-accent:#8a1538;"));

    // The command line wins over the theme
    final Path aOutEn = _tempFile (".html");
    assertEquals (0,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "-o",
                        aOutEn.toString (),
                        "--theme",
                        aTheme.toString (),
                        "-l",
                        "en"));
    assertTrue (Files.readString (aOutEn, StandardCharsets.UTF_8).contains ("Invoice"));
  }

  @Test
  public void testInvalidTheme () throws IOException
  {
    final Path aTheme = _tempFile (".json");
    Files.writeString (aTheme, "{\"common\":{\"colour\":\"red\"}}", StandardCharsets.UTF_8);
    final Path aOut = _tempFile (".html");
    Files.deleteIfExists (aOut);
    assertEquals (2, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString (), "--theme", aTheme.toString ()));
    assertEquals (2, _run ("-i", _copyInvoice ().toString (), "--theme", "does-not-exist.json"));
  }

  @NonNull
  private static Path _fontFile (final EFontResourceRoboto eFont, final String sName) throws IOException
  {
    final Path aFile = Files.createTempFile ("phlox-cli-font-", "-" + sName);
    aFile.toFile ().deleteOnExit ();
    try (final InputStream aIS = eFont.getFontResource ().getInputStream ())
    {
      Files.write (aFile, aIS.readAllBytes ());
    }
    return aFile;
  }

  @NonNull
  private static ICommonsList <String> _getFontNames (@NonNull final Path aPdf) throws IOException
  {
    final ICommonsList <String> ret = new CommonsArrayList <> ();
    try (final PDDocument aDoc = Loader.loadPDF (aPdf.toFile ()))
    {
      for (final PDPage aPage : aDoc.getPages ())
        for (final COSName aName : aPage.getResources ().getFontNames ())
          ret.add (aPage.getResources ().getFont (aName).getName ());
    }
    return ret;
  }

  @Test
  public void testFontIsUsedInHtml () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (0,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "--font",
                        _fontFile (EFontResourceRoboto.ROBOTO_NORMAL, "Regular.ttf").toString (),
                        "--font-bold",
                        _fontFile (EFontResourceRoboto.ROBOTO_BOLD, "Bold.ttf").toString (),
                        "-o",
                        aOut.toString ()));
    final String sHtml = Files.readString (aOut, StandardCharsets.UTF_8);
    assertTrue (sHtml.contains ("@font-face{font-family:\"Phlox Custom\";font-weight:400;font-style:normal;src:url(data:font/ttf;base64,"));
    assertTrue (sHtml.contains ("font-weight:700;font-style:normal"));
    assertTrue (sHtml.contains ("body{font-family:\"Phlox Custom\""));
  }

  @Test
  public void testFontIsUsedInPdf () throws IOException
  {
    final Path aWithout = _tempFile (".pdf");
    final Path aWith = _tempFile (".pdf");
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-f", "PDF", "-o", aWithout.toString ()));
    assertEquals (0,
                  _run ("-i",
                        _copyInvoice ().toString (),
                        "-f",
                        "PDF",
                        "--font",
                        _fontFile (EFontResourceRoboto.ROBOTO_NORMAL, "Regular.ttf").toString (),
                        "-o",
                        aWith.toString ()));
    assertTrue (_getFontNames (aWithout).stream ().noneMatch (x -> x.contains ("Roboto")));
    assertTrue (_getFontNames (aWith).stream ().anyMatch (x -> x.contains ("Roboto")));
  }

  @Test
  public void testFontErrors () throws IOException
  {
    final Path aOut = _tempFile (".html");
    final Path aInvoice = _copyInvoice ();
    assertEquals (2, _run ("-i", aInvoice.toString (), "--font-bold", aInvoice.toString (), "-o", aOut.toString ()));
    assertEquals (2, _run ("-i", aInvoice.toString (), "--font", "does-not-exist.ttf", "-o", aOut.toString ()));
    // Not a font file
    assertEquals (2, _run ("-i", aInvoice.toString (), "--font", aInvoice.toString (), "-o", aOut.toString ()));
    assertEquals (2, _run ("-i", aInvoice.toString (), "-f", "PDF", "--font", aInvoice.toString (), "-o", aOut.toString ()));
  }
}
