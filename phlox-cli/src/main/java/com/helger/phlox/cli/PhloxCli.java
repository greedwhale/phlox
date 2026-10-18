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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingBufferedInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.phlox.api.PhloxRuntimeException;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.cii.CiiInvoiceReader;
import com.helger.phlox.html.HtmlInvoiceWriter;
import com.helger.phlox.html.HtmlRenderContext;
import com.helger.phlox.pdf.PdfInvoiceWriter;
import com.helger.phlox.pdf.PdfRenderContext;
import com.helger.phlox.template.PhloxTemplate;
import com.helger.phlox.ubl.UblInvoiceReader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command-line entry point for {@code phlox}. Reads an electronic invoice from XML and renders it
 * to HTML or PDF.
 *
 * @author Philip Helger
 */
@Command (name = "phlox",
          mixinStandardHelpOptions = true,
          version = "phlox 0.1.0",
          description = "Render EN 16931 electronic invoices (UBL / CII) to HTML or PDF.")
public class PhloxCli implements Callable <Integer>
{
  /** The supported output formats. */
  public enum EOutputFormat
  {
    /** HTML5 output. */
    HTML,
    /** PDF output. */
    PDF
  }

  /** The supported source syntaxes. */
  public enum ESourceSyntax
  {
    /** Detect the syntax from the document. */
    AUTO,
    /** UBL Invoice or CreditNote. */
    UBL,
    /** UN/CEFACT Cross Industry Invoice. */
    CII
  }

  @Option (names = { "-i", "--in" },
           description = "Input invoice XML file. Read from stdin when omitted.",
           required = false)
  Path inputFile;

  @Option (names = { "-o", "--out" },
           description = "Output file. Written to stdout when omitted (HTML only).",
           required = false)
  Path outputFile;

  @Option (names = { "-f", "--format" },
           description = "Output format: HTML or PDF. Default: HTML.",
           defaultValue = "HTML")
  EOutputFormat format;

  @Option (names = { "-s", "--syntax" },
           description = "Source syntax: AUTO, UBL, or CII. Default: AUTO (sniffs the root element).",
           defaultValue = "AUTO")
  ESourceSyntax syntax;

  @Option (names = "--en-version",
           description = "EN 16931 edition of the input: auto, 2017 or 2026. Default: auto (reads BT-24 Specification identifier).",
           defaultValue = "auto")
  String enVersion;

  @Option (names = { "-l", "--lang" },
           description = "Output language code (e.g. 'en', 'de'). Default: en.",
           defaultValue = "en")
  String lang;

  @Option (names = { "-t", "--template" },
           description = "Render through a template instead of the built-in layout: 'default' for the template shipped with phlox, 'classpath:<path>' for a template on the classpath, or the path of a .ptpl file. Default: the built-in layout.")
  String template;

  @Option (names = "--watermark", description = "Optional user watermark text (e.g. 'DRAFT').")
  String watermark;

  @Option (names = "--watermark-opacity",
           description = "Watermark opacity 0.0-1.0. Default: 0.10.",
           defaultValue = "0.10")
  float watermarkOpacity;

  @Option (names = "--watermark-rotation",
           description = "Watermark rotation in degrees. Default: -45.",
           defaultValue = "-45")
  float watermarkRotation;

  private static final String TEMPLATE_DEFAULT = "default";
  private static final String TEMPLATE_CLASSPATH_PREFIX = "classpath:";

  /** Create a new CLI instance. */
  public PhloxCli ()
  {}

  @Nullable
  private static PhloxTemplate _loadTemplate (@Nullable final String sTemplate)
  {
    if (sTemplate == null)
      return null;
    if (TEMPLATE_DEFAULT.equals (sTemplate))
      return PhloxTemplate.getDefault ();
    if (sTemplate.startsWith (TEMPLATE_CLASSPATH_PREFIX))
      return PhloxTemplate.fromClasspath (sTemplate.substring (TEMPLATE_CLASSPATH_PREFIX.length ()));
    return PhloxTemplate.fromFile (new File (sTemplate));
  }

  private static byte [] _readAllStdin () throws IOException
  {
    try (final NonBlockingBufferedInputStream aIn = new NonBlockingBufferedInputStream (System.in))
    {
      return aIn.readAllBytes ();
    }
  }

  /**
   * Command line entry point.
   *
   * @param args
   *        The command line arguments.
   */
  public static void main (final String [] args)
  {
    final int nExit = new CommandLine (new PhloxCli ()).execute (args);
    System.exit (nExit);
  }

  /** Decide source syntax by sniffing the root element / namespace. */
  static ESourceSyntax _sniffSyntax (final byte [] aBytes)
  {
    final String sHead = new String (aBytes, StandardCharsets.UTF_8);
    if (sHead.contains ("<CrossIndustryInvoice") ||
        sHead.contains (":CrossIndustryInvoice") ||
        sHead.contains ("uncefact:data:standard"))
      return ESourceSyntax.CII;
    return ESourceSyntax.UBL;
  }

  @Override
  public Integer call () throws Exception
  {
    if (inputFile != null && !Files.isRegularFile (inputFile))
    {
      System.err.println ("Input file '" + inputFile + "' does not exist");
      return Integer.valueOf (2);
    }
    final byte [] aBytes = inputFile != null ? Files.readAllBytes (inputFile) : _readAllStdin ();
    final ESourceSyntax eSyntax = syntax == ESourceSyntax.AUTO ? _sniffSyntax (aBytes) : syntax;

    if (!"auto".equalsIgnoreCase (enVersion) && !"2017".equals (enVersion) && !"2026".equals (enVersion))
    {
      System.err.println ("Unsupported --en-version '" + enVersion + "'. Use auto, 2017 or 2026.");
      return Integer.valueOf (2);
    }

    final ErrorList aErrors = new ErrorList ();
    final PhloxInvoice aInvoice;
    if (eSyntax == ESourceSyntax.CII)
    {
      final CiiInvoiceReader aReader = "2017".equals (enVersion) ? CiiInvoiceReader.fromD16B ()
                                                                 : "2026".equals (enVersion) ? CiiInvoiceReader.fromD25A ()
                                                                                             : CiiInvoiceReader.autoDetect ();
      aInvoice = aReader.read (new NonBlockingByteArrayInputStream (aBytes), aErrors);
    }
    else
    {
      final UblInvoiceReader aReader = "2017".equals (enVersion) ? UblInvoiceReader.fromUbl21 ()
                                                                 : "2026".equals (enVersion) ? UblInvoiceReader.fromUbl25 ()
                                                                                             : UblInvoiceReader.autoDetect ();
      aInvoice = aReader.read (new NonBlockingByteArrayInputStream (aBytes), aErrors);
    }

    if (aInvoice == null || aErrors.containsAtLeastOneError ())
    {
      System.err.println ("Failed to parse input: " + aErrors);
      return Integer.valueOf (1);
    }

    final PhloxTemplate aTemplate;
    try
    {
      aTemplate = _loadTemplate (template);
    }
    catch (final PhloxRuntimeException ex)
    {
      System.err.println ("Failed to load the template '" + template + "': " + ex.getMessage ());
      return Integer.valueOf (2);
    }

    final Locale aLocale = Locale.forLanguageTag (lang.replace ('_', '-'));
    final WatermarkSpec aWM = watermark == null ? null
                                                : WatermarkSpec.text (watermark)
                                                               .setOpacity (watermarkOpacity)
                                                               .setRotation (watermarkRotation);

    if (format == EOutputFormat.PDF)
    {
      if (outputFile == null)
      {
        System.err.println ("PDF output requires --out");
        return Integer.valueOf (2);
      }
      final PdfRenderContext aCtx = new PdfRenderContext ().setLocale (aLocale)
                                                           .setUserWatermark (aWM)
                                                           .setTemplate (aTemplate);
      // Render completely before touching the output file, so a failure leaves no empty file
      final NonBlockingByteArrayOutputStream aBuffer = new NonBlockingByteArrayOutputStream ();
      try
      {
        new PdfInvoiceWriter ().writeTo (aInvoice, aCtx, aBuffer);
      }
      catch (final PhloxRuntimeException ex)
      {
        System.err.println ("Failed to render the invoice: " + ex.getMessage ());
        return Integer.valueOf (2);
      }
      try (final OutputStream aOut = new FileOutputStream (outputFile.toFile ()))
      {
        aBuffer.writeTo (aOut);
      }
    }
    else
    {
      final HtmlRenderContext aCtx = new HtmlRenderContext ().setLocale (aLocale)
                                                             .setUserWatermark (aWM)
                                                             .setTemplate (aTemplate);
      final HtmlInvoiceWriter aHtmlWriter = new HtmlInvoiceWriter ();
      final String sHtml;
      try
      {
        sHtml = aHtmlWriter.writeAsString (aInvoice, aCtx);
      }
      catch (final PhloxRuntimeException ex)
      {
        System.err.println ("Failed to render the invoice: " + ex.getMessage ());
        return Integer.valueOf (2);
      }
      final byte [] aHtmlBytes = sHtml.getBytes (StandardCharsets.UTF_8);
      if (outputFile == null)
        System.out.write (aHtmlBytes);
      else
        try (final OutputStream aOut = new FileOutputStream (outputFile.toFile ()))
        {
          aOut.write (aHtmlBytes);
        }
    }
    return Integer.valueOf (0);
  }
}
