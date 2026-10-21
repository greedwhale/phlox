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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.WillClose;
import com.helger.pdflayout.IPDDocumentCustomizer;
import com.helger.pdflayout.PDFCreationException;
import com.helger.pdflayout.PageLayoutPDF;
import com.helger.pdflayout.base.IPLRenderableObject;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.box.PLBox;
import com.helger.pdflayout.pdfbox.PDPageContentStreamExt;
import com.helger.pdflayout.richtext.element.PLRichText;
import com.helger.pdflayout.spec.EHorzAlignment;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.phlox.api.PhloxDocumentProperties;
import com.helger.phlox.api.PhloxRuntimeException;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.template.IPhloxTemplate;
import com.helger.phlox.api.watermark.EWatermarkPosition;
import com.helger.phlox.api.watermark.PhloxProgramWatermark;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.api.write.IInvoiceWriter;
import com.helger.phlox.template.PhloxTemplate;

/**
 * Renders a {@link PhloxInvoice} as a PDF using {@code ph-pdf-layout}. The content comes from a
 * template; if the render context has none, the default template is used.
 *
 * @author Philip Helger
 */
public class PdfInvoiceWriter implements IInvoiceWriter <PdfRenderContext>
{
  /** Create a new PDF invoice writer. */
  public PdfInvoiceWriter ()
  {}

  private static void _drawWatermark (@NonNull final PDDocument aDoc,
                                      @NonNull final PDPageContentStreamExt aCS,
                                      @NonNull final PDPage aPage,
                                      @NonNull final PdfFontFamily aFamily,
                                      @NonNull final WatermarkSpec aSpec) throws IOException
  {
    final PDRectangle aPageSize = aPage.getMediaBox ();
    final float fPageWidth = aPageSize.getWidth ();
    final float fPageHeight = aPageSize.getHeight ();

    // Font: bold for centered/large watermarks, regular for the small program attribution.
    final PreloadFont aFontKind = aSpec.getPosition () == EWatermarkPosition.CENTER ? aFamily.getBold ()
                                                                                    : aFamily.getRegular ();
    final PDFont aFont = aFontKind.loadPDFont (aDoc);
    final float fFontSize = aSpec.getFontSizePt ();
    final String sText = aSpec.getText ();
    final float fTextWidth = aFont.getStringWidth (sText) / 1000f * fFontSize;
    // Approximate baseline-to-cap-height ratio for visual centering.
    final float fTextHeight = fFontSize * 0.72f;

    aCS.saveGraphicsState ();
    aCS.beginText ();
    aCS.setFont (aFont, fFontSize);

    // PLColor is RGB-only — approximate opacity by lightening toward white. Pixel-true alpha
    // would need a PDExtendedGraphicsState, which is heavier and not required for our use.
    final Color aRGB = aSpec.getColor ();
    final float fO = Math.max (0f, Math.min (1f, aSpec.getOpacity ()));
    final int nR = Math.round (255 - (255 - aRGB.getRed ()) * fO);
    final int nG = Math.round (255 - (255 - aRGB.getGreen ()) * fO);
    final int nB = Math.round (255 - (255 - aRGB.getBlue ()) * fO);
    aCS.setNonStrokingColor (nR, nG, nB);

    switch (aSpec.getPosition ())
    {
      case CENTER:
      {
        final float fCx = fPageWidth / 2f;
        final float fCy = fPageHeight / 2f;
        final double dAngle = Math.toRadians (aSpec.getRotation ());
        // Anchor: shift back by half-text-width along the rotation direction so the text is
        // visually centered on (fCx, fCy).
        final float fAnchorX = fCx -
                               (float) (Math.cos (dAngle) * fTextWidth / 2f) +
                               (float) (Math.sin (dAngle) * fTextHeight / 2f);
        final float fAnchorY = fCy -
                               (float) (Math.sin (dAngle) * fTextWidth / 2f) -
                               (float) (Math.cos (dAngle) * fTextHeight / 2f);
        aCS.setTextMatrix (Matrix.getRotateInstance (dAngle, fAnchorX, fAnchorY));
        break;
      }
      case TOP:
        aCS.setTextMatrix (Matrix.getTranslateInstance ((fPageWidth - fTextWidth) / 2f, fPageHeight - fFontSize - 12f));
        break;
      case BOTTOM:
        aCS.setTextMatrix (Matrix.getTranslateInstance ((fPageWidth - fTextWidth) / 2f, 12f));
        break;
      case FOOTER_RIGHT:
      default:
        aCS.setTextMatrix (Matrix.getTranslateInstance (fPageWidth - fTextWidth - 10f, 10f));
        break;
    }
    aCS.showText (sText);
    aCS.endText ();
    aCS.restoreGraphicsState ();
  }

  /**
   * Create the footer with the attribution of the program. The text links to the phlox project.
   */
  @NonNull
  private static IPLRenderableObject <?> _createProgramFooter (@NonNull final PdfFontFamily aFamily,
                                                               @NonNull final WatermarkSpec aSpec)
  {
    // PLColor is RGB-only - approximate the opacity by lightening the colour towards white
    final Color aRGB = aSpec.getColor ();
    final float fO = Math.max (0f, Math.min (1f, aSpec.getOpacity ()));
    final PLColor aColor = new PLColor (Math.round (255 - (255 - aRGB.getRed ()) * fO),
                                        Math.round (255 - (255 - aRGB.getGreen ()) * fO),
                                        Math.round (255 - (255 - aRGB.getBlue ()) * fO));
    final PLRichText aText = PLRichText.createFromMarkup ("{link[" +
                                                          PhloxDocumentProperties.PROJECT_URL +
                                                          "]}" +
                                                          PdfTextSupport.escapeMarkup (aSpec.getText ()) +
                                                          "{link}",
                                                          aFamily.getAsPLFontFamily (),
                                                          aSpec.getFontSizePt (),
                                                          aColor);
    aText.setHorzAlign (EHorzAlignment.RIGHT);
    return new PLBox (aText).setMarginTop (4).setHorzAlign (EHorzAlignment.RIGHT);
  }

  @NonNull
  private static IPDDocumentCustomizer _buildWatermarkCustomizer (@NonNull final PdfFontFamily aFamily,
                                                                  @Nullable final WatermarkSpec aUserWM,
                                                                  @Nullable final WatermarkSpec aProgramWM)
  {
    return aDoc -> {
      for (final PDPage aPage : aDoc.getPages ())
      {
        try (final PDPageContentStreamExt aCS = new PDPageContentStreamExt (aDoc,
                                                                            aPage,
                                                                            PDPageContentStream.AppendMode.PREPEND,
                                                                            true,
                                                                            true))
        {
          if (aUserWM != null)
            _drawWatermark (aDoc, aCS, aPage, aFamily, aUserWM);
          if (aProgramWM != null)
            _drawWatermark (aDoc, aCS, aPage, aFamily, aProgramWM);
        }
      }
    };
  }

  /**
   * Set the metadata of the PDF: title, author, subject, keywords, language and the creator
   * (phlox).
   */
  private static void _setDocumentProperties (@NonNull final PageLayoutPDF aPdf,
                                              @NonNull final PhloxInvoice aInvoice,
                                              @NonNull final PdfRenderContext aCtx)
  {
    aPdf.setDocumentTitle (PhloxDocumentProperties.getTitle (aInvoice, aCtx));
    aPdf.setDocumentAuthor (PhloxDocumentProperties.getAuthor (aInvoice));
    aPdf.setDocumentSubject (PhloxDocumentProperties.getSubject (aInvoice, aCtx));
    aPdf.setDocumentKeywords (PhloxDocumentProperties.getKeywords (aInvoice, aCtx));
    aPdf.setDocumentLanguage (aCtx.getLocale ().toLanguageTag ());
    aPdf.setDocumentCreator (PhloxDocumentProperties.getCreator ());
  }

  @NonNull
  private PageLayoutPDF _build (@NonNull final PhloxInvoice aInvoice, @NonNull final PdfRenderContext aCtx)
  {
    final PageLayoutPDF aPdf = new PageLayoutPDF ();
    _setDocumentProperties (aPdf, aInvoice, aCtx);

    final IPhloxTemplate aTemplate = aCtx.getTemplate () != null ? aCtx.getTemplate () : PhloxTemplate.getDefault ();
    if (!(aTemplate instanceof final PhloxTemplate aPhloxTemplate))
      throw new PhloxRuntimeException ("Unsupported template implementation " + aTemplate.getClass ().getName ());
    final PLPageSet aPS = new PdfTemplateEmitter (aCtx).emit (aPhloxTemplate.renderAst (aInvoice, aCtx));

    aPdf.addPageSet (aPS);

    // Watermarks are drawn after all pages are laid out, via a document customizer that
    // prepends content (so the watermark appears behind the page text). Pattern lifted from
    // ph-pdf-layout's PLPageSetTest.testCreateWatermark.
    final WatermarkSpec aUW = aCtx.getUserWatermark ();
    if (aUW != null)
      aPdf.setDocumentCustomizer (_buildWatermarkCustomizer (aCtx.getFontFamily (), aUW, null));

    // The attribution of the program is a link to the project, so it is part of the layout
    final WatermarkSpec aPW = PhloxProgramWatermark.getInstance ();
    if (aPW != null)
      aPS.setPageFooter (_createProgramFooter (aCtx.getFontFamily (), aPW));

    return aPdf;
  }

  /**
   * Render the given invoice into a PDF written to the given file.
   *
   * @param aInvoice
   *        The invoice. Never {@code null}.
   * @param aCtx
   *        The PDF render context. Never {@code null}.
   * @param aFile
   *        The output file. Never {@code null}.
   * @throws PDFCreationException
   *         If PDF creation fails.
   */
  public void writeTo (@NonNull final PhloxInvoice aInvoice,
                       @NonNull final PdfRenderContext aCtx,
                       @NonNull final File aFile) throws PDFCreationException
  {
    _build (aInvoice, aCtx).renderTo (aFile);
  }

  /**
   * Render the given invoice into a PDF written to the given stream.
   *
   * @param aInvoice
   *        The invoice.
   * @param aCtx
   *        The PDF render context.
   * @param aOut
   *        The output stream (closed by the renderer).
   * @throws PDFCreationException
   *         If PDF creation fails.
   */
  public void writeTo (@NonNull final PhloxInvoice aInvoice,
                       @NonNull final PdfRenderContext aCtx,
                       @NonNull @WillClose final OutputStream aOut) throws PDFCreationException
  {
    _build (aInvoice, aCtx).renderTo (aOut);
  }
}
