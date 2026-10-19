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
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.io.nonblocking.NonBlockingBufferedInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.io.nonblocking.NonBlockingByteArrayOutputStream;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.phlox.api.AbstractRenderContext;
import com.helger.phlox.api.PhloxRuntimeException;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.cii.CiiInvoiceReader;
import com.helger.phlox.html.HtmlInvoiceWriter;
import com.helger.phlox.html.HtmlRenderContext;
import com.helger.phlox.pdf.PdfInvoiceWriter;
import com.helger.phlox.pdf.PdfRenderContext;
import com.helger.phlox.template.PhloxTemplate;
import com.helger.phlox.theme.PhloxTheme;
import com.helger.phlox.theme.PhloxThemeReader;
import com.helger.phlox.ubl.UblInvoiceReader;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine;

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
  Path m_aInputFile;

  @Option (names = { "-o", "--out" },
           description = "Output file. Written to stdout when omitted (HTML only).",
           required = false)
  Path m_aOutputFile;

  @Option (names = { "-f", "--format" },
           description = "Output format: HTML or PDF. Default: HTML.",
           defaultValue = "HTML")
  EOutputFormat m_eFormat;

  @Option (names = { "-s", "--syntax" },
           description = "Source syntax: AUTO, UBL, or CII. Default: AUTO (sniffs the root element).",
           defaultValue = "AUTO")
  ESourceSyntax m_eSyntax;

  @Option (names = "--en-version",
           description = "EN 16931 edition of the input: auto, 2017 or 2026. Default: auto (reads BT-24 Specification identifier).",
           defaultValue = "auto")
  String m_sEnVersion;

  @Option (names = { "-l", "--lang" },
           description = "Output language code (e.g. 'en', 'de'). Default: the language of the theme or en.")
  String m_sLang;

  @Option (names = { "-t", "--template" },
           description = "The template to render through: 'default' for the template shipped with phlox, 'classpath:<path>' for a template on the classpath, or the path of a .ptpl file. Default: 'default'.")
  String m_sTemplate;

  @Option (names = "--theme",
           description = "A JSON file with the layout parameters of the HTML and PDF output (colours, fonts, page size, language, watermark, ...). The options of the command line override the theme.")
  Path m_aThemeFile;

  @Option (names = "--logo",
           description = "Optional logo image (PNG, JPEG or GIF) that the template shows in the header.")
  Path m_aLogoFile;

  @Option (names = "--watermark", description = "Optional user watermark text (e.g. 'DRAFT').")
  String m_sWatermark;

  @Option (names = "--watermark-opacity",
           description = "Watermark opacity 0.0-1.0. Default: 0.10.",
           defaultValue = "0.10")
  float m_fWatermarkOpacity;

  @Option (names = "--watermark-rotation",
           description = "Watermark rotation in degrees. Default: -45.",
           defaultValue = "-45")
  float m_fWatermarkRotation;

  private static final String TEMPLATE_DEFAULT = "default";
  private static final String TEMPLATE_CLASSPATH_PREFIX = "classpath:";

  /** Create a new CLI instance. */
  public PhloxCli ()
  {}

  @NonNull
  private static String _getMimeCode (@NonNull final String sFilename)
  {
    final String sLower = sFilename.toLowerCase (Locale.ROOT);
    if (sLower.endsWith (".jpg") || sLower.endsWith (".jpeg"))
      return "image/jpeg";
    if (sLower.endsWith (".gif"))
      return "image/gif";
    return "image/png";
  }

  @NonNull
  private static PhloxTemplate _loadTemplate (@Nullable final String sTemplate)
  {
    if (sTemplate == null || TEMPLATE_DEFAULT.equals (sTemplate))
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
   * Apply the theme and then the options of the command line, which win over the theme.
   *
   * @return <code>null</code> if everything is fine, otherwise the exit code.
   */
  @Nullable
  private <CTX extends AbstractRenderContext <CTX>> Integer _prepare (@NonNull final CTX aCtx,
                                                                      @Nullable final Consumer <CTX> aThemeApplier)
  {
    try
    {
      if (aThemeApplier != null)
        aThemeApplier.accept (aCtx);
      if (m_sLang != null)
        aCtx.setLocale (Locale.forLanguageTag (m_sLang.replace ('_', '-')));
      if (m_sTemplate != null)
        aCtx.setTemplate (_loadTemplate (m_sTemplate));
      if (m_aLogoFile != null)
      {
        if (!Files.isRegularFile (m_aLogoFile))
        {
          System.err.println ("Logo file '" + m_aLogoFile + "' does not exist");
          return Integer.valueOf (2);
        }
        final String sFilename = m_aLogoFile.getFileName ().toString ();
        aCtx.setLogo (new PhloxAttachment ().setBinary (Files.readAllBytes (m_aLogoFile))
                                            .setMimeCode (_getMimeCode (sFilename))
                                            .setFilename (sFilename));
      }
      if (m_sWatermark != null)
        aCtx.setUserWatermark (WatermarkSpec.text (m_sWatermark)
                                            .setOpacity (m_fWatermarkOpacity)
                                            .setRotation (m_fWatermarkRotation));
    }
    catch (final PhloxRuntimeException | IOException ex)
    {
      System.err.println ("Failed to prepare the rendering: " + ex.getMessage ());
      return Integer.valueOf (2);
    }
    return null;
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

  public Integer call () throws Exception
  {
    if (m_aInputFile != null && !Files.isRegularFile (m_aInputFile))
    {
      System.err.println ("Input file '" + m_aInputFile + "' does not exist");
      return Integer.valueOf (2);
    }
    final byte [] aBytes = m_aInputFile != null ? Files.readAllBytes (m_aInputFile) : _readAllStdin ();
    final ESourceSyntax eSyntax = m_eSyntax == ESourceSyntax.AUTO ? _sniffSyntax (aBytes) : m_eSyntax;

    if (!"auto".equalsIgnoreCase (m_sEnVersion) && !"2017".equals (m_sEnVersion) && !"2026".equals (m_sEnVersion))
    {
      System.err.println ("Unsupported --en-version '" + m_sEnVersion + "'. Use auto, 2017 or 2026.");
      return Integer.valueOf (2);
    }

    final ErrorList aErrors = new ErrorList ();
    final PhloxInvoice aInvoice;
    if (eSyntax == ESourceSyntax.CII)
    {
      final CiiInvoiceReader aReader = "2017".equals (m_sEnVersion) ? CiiInvoiceReader.fromD16B ()
                                                                 : "2026".equals (m_sEnVersion) ? CiiInvoiceReader.fromD25A ()
                                                                                             : CiiInvoiceReader.autoDetect ();
      aInvoice = aReader.read (new NonBlockingByteArrayInputStream (aBytes), aErrors);
    }
    else
    {
      final UblInvoiceReader aReader = "2017".equals (m_sEnVersion) ? UblInvoiceReader.fromUbl21 ()
                                                                 : "2026".equals (m_sEnVersion) ? UblInvoiceReader.fromUbl25 ()
                                                                                             : UblInvoiceReader.autoDetect ();
      aInvoice = aReader.read (new NonBlockingByteArrayInputStream (aBytes), aErrors);
    }

    if (aInvoice == null || aErrors.containsAtLeastOneError ())
    {
      System.err.println ("Failed to parse input: " + aErrors);
      return Integer.valueOf (1);
    }

    final PhloxTheme aTheme;
    if (m_aThemeFile == null)
      aTheme = null;
    else
    {
      try
      {
        aTheme = PhloxThemeReader.read (m_aThemeFile);
      }
      catch (final PhloxRuntimeException ex)
      {
        System.err.println ("Failed to read the theme '" + m_aThemeFile + "': " + ex.getMessage ());
        return Integer.valueOf (2);
      }
    }

    if (m_eFormat == EOutputFormat.PDF)
    {
      if (m_aOutputFile == null)
      {
        System.err.println ("PDF output requires --out");
        return Integer.valueOf (2);
      }
      final PdfRenderContext aCtx = new PdfRenderContext ();
      final Integer aError = _prepare (aCtx, aTheme == null ? null : (final PdfRenderContext aTarget) -> aTarget.applyTheme (aTheme));
      if (aError != null)
        return aError;
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
      try (final OutputStream aOut = new FileOutputStream (m_aOutputFile.toFile ()))
      {
        aBuffer.writeTo (aOut);
      }
    }
    else
    {
      final HtmlRenderContext aCtx = new HtmlRenderContext ();
      final Integer aError = _prepare (aCtx,
                                       aTheme == null ? null : (final HtmlRenderContext aTarget) -> aTarget.applyTheme (aTheme));
      if (aError != null)
        return aError;
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
      if (m_aOutputFile == null)
        System.out.write (aHtmlBytes);
      else
        try (final OutputStream aOut = new FileOutputStream (m_aOutputFile.toFile ()))
        {
          aOut.write (aHtmlBytes);
        }
    }
    return Integer.valueOf (0);
  }
}
