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

import org.junit.Test;

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
    assertEquals (0, _run ("-i", _copyInvoice ().toString (), "-f", "PDF", "-o", aOut.toString (), "--template", "default"));
    final byte [] aBytes = Files.readAllBytes (aOut);
    assertTrue (aBytes.length > 500 && aBytes[0] == '%' && aBytes[1] == 'P' && aBytes[2] == 'D' && aBytes[3] == 'F');
  }

  @Test
  public void testMissingTemplateIsAnError () throws IOException
  {
    final Path aOut = _tempFile (".html");
    assertEquals (2, _run ("-i", _copyInvoice ().toString (), "-o", aOut.toString (), "--template", "/no/such/template.ptpl"));
  }
}
