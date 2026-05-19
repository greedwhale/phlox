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
package com.helger.norma.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.WillClose;
import com.helger.norma.api.i18n.NormaFormat;
import com.helger.norma.api.model.*;
import com.helger.norma.api.watermark.EWatermarkPosition;
import com.helger.norma.api.watermark.NormaProgramWatermark;
import com.helger.norma.api.watermark.WatermarkSpec;
import com.helger.norma.api.write.IInvoiceWriter;
import com.helger.pdflayout.IPDDocumentCustomizer;
import com.helger.pdflayout.PDFCreationException;
import com.helger.pdflayout.PageLayoutPDF;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.hbox.PLHBox;
import com.helger.pdflayout.element.special.PLSpacerX;
import com.helger.pdflayout.element.special.PLSpacerY;
import com.helger.pdflayout.element.table.PLTable;
import com.helger.pdflayout.element.table.PLTableCell;
import com.helger.pdflayout.element.text.PLText;
import com.helger.pdflayout.element.vbox.PLVBox;
import com.helger.pdflayout.pdfbox.PDPageContentStreamExt;
import com.helger.pdflayout.spec.BorderStyleSpec;
import com.helger.pdflayout.spec.EHorzAlignment;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.PaddingSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.pdflayout.spec.WidthSpec;

/**
 * Renders a {@link NormaInvoice} as a PDF using {@code ph-pdf-layout}.
 *
 * @author Philip Helger
 */
public class PDFInvoiceWriter implements IInvoiceWriter <PDFRenderContext>
{
  public PDFInvoiceWriter ()
  {}

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
  public void writeTo (@NonNull final NormaInvoice aInvoice,
                       @NonNull final PDFRenderContext aCtx,
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
  @Override
  public void writeTo (@NonNull final NormaInvoice aInvoice,
                       @NonNull final PDFRenderContext aCtx,
                       @NonNull @WillClose final OutputStream aOut) throws PDFCreationException
  {
    _build (aInvoice, aCtx).renderTo (aOut);
  }

  @NonNull
  private PageLayoutPDF _build (@NonNull final NormaInvoice aInvoice, @NonNull final PDFRenderContext aCtx)
  {
    final PageLayoutPDF aPDF = new PageLayoutPDF ();
    final PLPageSet aPS = new PLPageSet (aCtx.getPageSize ()).setMargin (aCtx.getMargins ());

    _renderHeader (aPS, aInvoice, aCtx);
    _renderProcessControl (aPS, aInvoice, aCtx);
    _renderParties (aPS, aInvoice, aCtx);
    _renderOptionalParties (aPS, aInvoice, aCtx);
    _renderReferences (aPS, aInvoice, aCtx);
    _renderInvoiceNotes (aPS, aInvoice, aCtx);
    _renderDelivery (aPS, aInvoice, aCtx);
    _renderInvoicingPeriod (aPS, aInvoice, aCtx);
    _renderLines (aPS, aInvoice, aCtx);
    _renderAllowancesCharges (aPS, aInvoice, aCtx);
    _renderVATBreakdown (aPS, aInvoice, aCtx);
    _renderTotals (aPS, aInvoice, aCtx);
    _renderPayment (aPS, aInvoice, aCtx);
    _renderPaymentTerms (aPS, aInvoice, aCtx);
    _renderSupportingDocuments (aPS, aInvoice, aCtx);

    aPDF.addPageSet (aPS);

    // Watermarks are drawn after all pages are laid out, via a document customizer that
    // prepends content (so the watermark appears behind the page text). Pattern lifted from
    // ph-pdf-layout's PLPageSetTest.testCreateWatermark.
    final WatermarkSpec aUW = aCtx.getUserWatermark ();
    final WatermarkSpec aPW = NormaProgramWatermark.getInstance ();
    if (aUW != null || aPW != null)
      aPDF.setDocumentCustomizer (_buildWatermarkCustomizer (aUW, aPW));

    return aPDF;
  }

  @NonNull
  private static IPDDocumentCustomizer _buildWatermarkCustomizer (@Nullable final WatermarkSpec aUserWM,
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
            _drawWatermark (aCS, aPage, aUserWM);
          if (aProgramWM != null)
            _drawWatermark (aCS, aPage, aProgramWM);
        }
      }
    };
  }

  private static void _drawWatermark (@NonNull final PDPageContentStreamExt aCS,
                                      @NonNull final PDPage aPage,
                                      @NonNull final WatermarkSpec aSpec) throws IOException
  {
    final PDRectangle aPageSize = aPage.getMediaBox ();
    final float fPageWidth = aPageSize.getWidth ();
    final float fPageHeight = aPageSize.getHeight ();

    // Font: bold for centered/large watermarks, regular for the small program attribution.
    final PreloadFont eFontKind = aSpec.getPosition () == EWatermarkPosition.CENTER ? PreloadFont.REGULAR_BOLD
                                                                                    : PreloadFont.REGULAR;
    final PDFont aFont = new PDType1Font (eFontKind == PreloadFont.REGULAR_BOLD
                                                                                ? Standard14Fonts.FontName.HELVETICA_BOLD
                                                                                : Standard14Fonts.FontName.HELVETICA);
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
    final java.awt.Color aRGB = aSpec.getColor ();
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

  // ===== Sections =====

  private static void _renderHeader (@NonNull final PLPageSet aPS,
                                     @NonNull final NormaInvoice aInvoice,
                                     @NonNull final PDFRenderContext aCtx)
  {
    final String sTitle = aInvoice.getKind () == ENormaDocumentKind.CREDIT_NOTE ? aCtx.label (
                                                                                              "section.header.title.creditnote")
                                                                                : aCtx.label ("section.header.title.invoice");
    aPS.addElement (new PLText (sTitle, new FontSpec (com.helger.pdflayout.spec.PreloadFont.REGULAR_BOLD, 18f))
                                                                                                               .setMarginBottom (6));

    final PLVBox aMeta = new PLVBox ();
    _kvRow (aMeta, aCtx.label ("BT-1"), aInvoice.getNumber (), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-2"), _date (aInvoice.getIssueDate (), aCtx), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-3"), _docTypeText (aInvoice.getTypeCode (), aCtx), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-5"), aInvoice.getCurrency (), aCtx);
    aPS.addElement (aMeta);
    aPS.addElement (new PLSpacerY (8));
  }

  private static void _renderProcessControl (@NonNull final PLPageSet aPS,
                                             @NonNull final NormaInvoice aInvoice,
                                             @NonNull final PDFRenderContext aCtx)
  {
    final String sBP = aInvoice.getProcessControl ().getBusinessProcessType ();
    final String sSpec = aInvoice.getProcessControl ().getSpecificationIdentifier ();
    if (sBP == null && sSpec == null && !aCtx.isIncludeEmptySections ())
      return;
    final PLVBox aBox = new PLVBox ();
    _kvRow (aBox, aCtx.label ("BT-23"), sBP, aCtx);
    _kvRow (aBox, aCtx.label ("BT-24"), sSpec, aCtx);
    aPS.addElement (aBox);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderParties (@NonNull final PLPageSet aPS,
                                      @NonNull final NormaInvoice aInvoice,
                                      @NonNull final PDFRenderContext aCtx)
  {
    _sectionTitle (aPS, aCtx.label ("section.parties.title"), aCtx);
    final PLHBox aBox = new PLHBox ();
    aBox.addColumn (_partyBox (aCtx.label ("BG-4"), _sellerText (aInvoice.getSeller (), aCtx), aCtx),
                    WidthSpec.perc (50));
    aBox.addColumn (_partyBox (aCtx.label ("BG-7"), _buyerText (aInvoice.getBuyer (), aCtx), aCtx),
                    WidthSpec.perc (50));
    aPS.addElement (aBox);
    aPS.addElement (new PLSpacerY (6));
  }

  @NonNull
  private static PLVBox _partyBox (@NonNull final String sTitle,
                                   @NonNull final String sBody,
                                   @NonNull final PDFRenderContext aCtx)
  {
    final PLVBox aBox = new PLVBox ();
    aBox.addRow (new PLText (sTitle, aCtx.getHeaderFont ()).setMarginBottom (3));
    aBox.addRow (new PLText (sBody, aCtx.getBodyFont ()));
    return aBox;
  }

  @NonNull
  private static String _sellerText (@NonNull final NormaSeller aS, @NonNull final PDFRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendLine (sb, aS.getName ());
    _appendLine (sb, aS.getTradingName ());
    _appendAddress (sb, aS.getPostalAddress ());
    if (aS.getVATIdentifier () != null)
      sb.append (aCtx.label ("BT-31")).append (": ").append (aS.getVATIdentifier ()).append ('\n');
    if (aS.getTaxRegistration () != null)
      sb.append (aCtx.label ("BT-32")).append (": ").append (aS.getTaxRegistration ()).append ('\n');
    if (aS.getLegalRegistration () != null && aS.getLegalRegistration ().getValue () != null)
      sb.append (aCtx.label ("BT-30")).append (": ").append (aS.getLegalRegistration ().getValue ()).append ('\n');
    if (aS.getElectronicAddress () != null && aS.getElectronicAddress ().getValue () != null)
      sb.append (aCtx.label ("BT-34")).append (": ").append (aS.getElectronicAddress ().getValue ()).append ('\n');
    if (aS.getAdditionalLegalInfo () != null)
      _appendLine (sb, aS.getAdditionalLegalInfo ());
    _appendContact (sb, aS.getContact (), aCtx);
    return _trim (sb);
  }

  @NonNull
  private static String _buyerText (@NonNull final NormaBuyer aB, @NonNull final PDFRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendLine (sb, aB.getName ());
    _appendLine (sb, aB.getTradingName ());
    _appendAddress (sb, aB.getPostalAddress ());
    if (aB.getVATIdentifier () != null)
      sb.append (aCtx.label ("BT-48")).append (": ").append (aB.getVATIdentifier ()).append ('\n');
    if (aB.getLegalRegistration () != null && aB.getLegalRegistration ().getValue () != null)
      sb.append (aCtx.label ("BT-30")).append (": ").append (aB.getLegalRegistration ().getValue ()).append ('\n');
    if (aB.getElectronicAddress () != null && aB.getElectronicAddress ().getValue () != null)
      sb.append (aCtx.label ("BT-34")).append (": ").append (aB.getElectronicAddress ().getValue ()).append ('\n');
    _appendContact (sb, aB.getContact (), aCtx);
    return _trim (sb);
  }

  private static void _appendAddress (@NonNull final StringBuilder sb, @Nullable final NormaPostalAddress aA)
  {
    if (aA == null)
      return;
    _appendLine (sb, aA.getLine1 ());
    _appendLine (sb, aA.getLine2 ());
    _appendLine (sb, aA.getLine3 ());
    final StringBuilder aRow = new StringBuilder ();
    if (aA.getPostCode () != null)
      aRow.append (aA.getPostCode ()).append (' ');
    if (aA.getCity () != null)
      aRow.append (aA.getCity ());
    if (aRow.length () > 0)
      sb.append (aRow).append ('\n');
    _appendLine (sb, aA.getCountrySubdivision ());
    _appendLine (sb, aA.getCountryCode ());
  }

  private static void _appendContact (@NonNull final StringBuilder sb,
                                      @Nullable final NormaContact aC,
                                      @NonNull final PDFRenderContext aCtx)
  {
    if (aC == null || aC.isEmpty ())
      return;
    if (aC.getName () != null)
      sb.append (aCtx.label ("BT-41")).append (": ").append (aC.getName ()).append ('\n');
    if (aC.getTelephone () != null)
      sb.append (aCtx.label ("BT-42")).append (": ").append (aC.getTelephone ()).append ('\n');
    if (aC.getEmail () != null)
      sb.append (aCtx.label ("BT-43")).append (": ").append (aC.getEmail ()).append ('\n');
  }

  private static void _renderOptionalParties (@NonNull final PLPageSet aPS,
                                              @NonNull final NormaInvoice aInvoice,
                                              @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.getPayee () != null)
    {
      final NormaPayee aPayee = aInvoice.getPayee ();
      final StringBuilder sb = new StringBuilder ();
      _appendLine (sb, aPayee.getName ());
      if (aPayee.getIdentifier () != null && aPayee.getIdentifier ().getValue () != null)
        sb.append (aCtx.label ("BT-60")).append (": ").append (aPayee.getIdentifier ().getValue ()).append ('\n');
      _sectionTitle (aPS, aCtx.label ("BG-10"), aCtx);
      aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
      aPS.addElement (new PLSpacerY (6));
    }
    if (aInvoice.getSellerTaxRepresentative () != null)
    {
      final NormaSellerTaxRepresentative aR = aInvoice.getSellerTaxRepresentative ();
      final StringBuilder sb = new StringBuilder ();
      _appendLine (sb, aR.getName ());
      if (aR.getVATIdentifier () != null)
        sb.append (aCtx.label ("BT-31")).append (": ").append (aR.getVATIdentifier ()).append ('\n');
      _appendAddress (sb, aR.getPostalAddress ());
      _sectionTitle (aPS, aCtx.label ("BG-11"), aCtx);
      aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
      aPS.addElement (new PLSpacerY (6));
    }
  }

  private static void _renderReferences (@NonNull final PLPageSet aPS,
                                         @NonNull final NormaInvoice aInvoice,
                                         @NonNull final PDFRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendRefIf (sb, aCtx.label ("BT-10"), aInvoice.getBuyerReference ());
    _appendRefIf (sb, aCtx.label ("BT-11"), aInvoice.getProjectReference ());
    _appendRefIf (sb, aCtx.label ("BT-12"), aInvoice.getContractReference ());
    _appendRefIf (sb, aCtx.label ("BT-13"), aInvoice.getPurchaseOrderReference ());
    _appendRefIf (sb, aCtx.label ("BT-14"), aInvoice.getSalesOrderReference ());
    _appendRefIf (sb, aCtx.label ("BT-15"), aInvoice.getReceivingAdviceReference ());
    _appendRefIf (sb, aCtx.label ("BT-16"), aInvoice.getDespatchAdviceReference ());
    _appendRefIf (sb, aCtx.label ("BT-17"), aInvoice.getTenderOrLotReference ());
    _appendRefIf (sb, aCtx.label ("BT-19"), aInvoice.getBuyerAccountingReference ());
    if (aInvoice.getInvoicedObjectIdentifier () != null && aInvoice.getInvoicedObjectIdentifier ().getValue () != null)
      _appendRefIf (sb, aCtx.label ("BT-18"), aInvoice.getInvoicedObjectIdentifier ().getValue ());
    for (final NormaPrecedingInvoiceRef aRef : aInvoice.precedingInvoiceRefs ())
    {
      final StringBuilder row = new StringBuilder (aCtx.label ("BG-3")).append (": ");
      if (aRef.getNumber () != null)
        row.append (aRef.getNumber ());
      if (aRef.getIssueDate () != null)
        row.append (" (").append (_date (aRef.getIssueDate (), aCtx)).append (')');
      sb.append (row).append ('\n');
    }
    if (sb.length () == 0)
      return;
    _sectionTitle (aPS, aCtx.label ("section.references.title"), aCtx);
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderInvoiceNotes (@NonNull final PLPageSet aPS,
                                           @NonNull final NormaInvoice aInvoice,
                                           @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.invoiceNotes ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.notes.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    for (final NormaInvoiceNote aN : aInvoice.invoiceNotes ())
    {
      if (aN.getSubjectCode () != null)
        sb.append ('[').append (aN.getSubjectCode ()).append ("] ");
      if (aN.getContent () != null)
        sb.append (aN.getContent ());
      sb.append ('\n');
    }
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderDelivery (@NonNull final PLPageSet aPS,
                                       @NonNull final NormaInvoice aInvoice,
                                       @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.getDeliveryInformation () == null)
      return;
    final NormaDeliveryInformation aD = aInvoice.getDeliveryInformation ();
    _sectionTitle (aPS, aCtx.label ("section.delivery.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    if (aD.getPartyName () != null)
      sb.append (aD.getPartyName ()).append ('\n');
    _appendAddress (sb, aD.getPostalAddress ());
    if (aD.getActualDeliveryDate () != null)
      sb.append (aCtx.label ("BT-72")).append (": ").append (_date (aD.getActualDeliveryDate (), aCtx)).append ('\n');
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderInvoicingPeriod (@NonNull final PLPageSet aPS,
                                              @NonNull final NormaInvoice aInvoice,
                                              @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.getInvoicingPeriod () == null || aInvoice.getInvoicingPeriod ().isEmpty ())
      return;
    final NormaInvoicingPeriod aP = aInvoice.getInvoicingPeriod ();
    _sectionTitle (aPS, aCtx.label ("BG-14"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    if (aP.getStartDate () != null)
      sb.append (aCtx.label ("BT-73")).append (": ").append (_date (aP.getStartDate (), aCtx)).append ('\n');
    if (aP.getEndDate () != null)
      sb.append (aCtx.label ("BT-74")).append (": ").append (_date (aP.getEndDate (), aCtx)).append ('\n');
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderLines (@NonNull final PLPageSet aPS,
                                    @NonNull final NormaInvoice aInvoice,
                                    @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.lines ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.lines.title"), aCtx);
    final String sCurrency = aInvoice.getCurrency ();
    final FontSpec aTH = aCtx.getTableHeaderFont ();
    final FontSpec aBF = aCtx.getBodyFont ();
    final PLTable aTbl = PLTable.createWithPercentage (6, 44, 12, 14, 10, 14);
    aTbl.setHeaderRowCount (1);
    aTbl.addRow (_th (aCtx.label ("section.lines.column.number"), aTH),
                 _th (aCtx.label ("section.lines.column.description"), aTH),
                 _thRight (aCtx.label ("section.lines.column.quantity"), aTH),
                 _thRight (aCtx.label ("section.lines.column.unitprice"), aTH),
                 _thRight (aCtx.label ("section.lines.column.vat"), aTH),
                 _thRight (aCtx.label ("section.lines.column.linetotal"), aTH));
    for (final NormaInvoiceLine aLine : aInvoice.lines ())
    {
      final StringBuilder aDesc = new StringBuilder ();
      final NormaItem aItem = aLine.getItem ();
      if (aItem != null)
      {
        if (aItem.getName () != null)
          aDesc.append (aItem.getName ());
        if (aItem.getDescription () != null)
          aDesc.append ('\n').append (aItem.getDescription ());
        for (final NormaItemAttribute aA : aItem.attributes ())
          if (aA.getName () != null || aA.getValue () != null)
            aDesc.append ('\n').append (_n (aA.getName ())).append (": ").append (_n (aA.getValue ()));
      }
      if (aLine.getNote () != null)
        aDesc.append ('\n').append (aLine.getNote ());
      aTbl.addRow (_td (_n (aLine.getID ()), aBF),
                   _td (aDesc.toString (), aBF),
                   _tdRight (_qty (aLine.getQuantity (), aLine.getQuantityUnit ()), aBF),
                   _tdRight (aLine.getPrice () == null ? "" : _amount (aLine.getPrice ().getNetPrice (), sCurrency, aCtx),
                             aBF),
                   _tdRight (_lineVATCell (aLine, aCtx), aBF),
                   _tdRight (_amount (aLine.getNetAmount (), sCurrency, aCtx), aBF));
    }
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (8));
  }

  @NonNull
  private static String _lineVATCell (@NonNull final NormaInvoiceLine aLine, @NonNull final PDFRenderContext aCtx)
  {
    if (aLine.getLineVAT () == null)
      return "";
    final StringBuilder sb = new StringBuilder ();
    if (aLine.getLineVAT ().getCategoryCode () != null)
      sb.append (aLine.getLineVAT ().getCategoryCode ());
    if (aLine.getLineVAT ().getRate () != null)
    {
      if (sb.length () > 0)
        sb.append (' ');
      sb.append (NormaFormat.percent (aLine.getLineVAT ().getRate (), aCtx.getLocale (), aCtx.getFormatOptions ()));
    }
    return sb.toString ();
  }

  private static void _renderAllowancesCharges (@NonNull final PLPageSet aPS,
                                                @NonNull final NormaInvoice aInvoice,
                                                @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.documentAllowances ().isEmpty () && aInvoice.documentCharges ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.allowancescharges.title"), aCtx);
    final String sCurrency = aInvoice.getCurrency ();
    final FontSpec aBF = aCtx.getBodyFont ();
    final PLTable aTbl = PLTable.createWithPercentage (12, 48, 20, 20);
    aTbl.setHeaderRowCount (1);
    aTbl.addRow (_th ("", aCtx.getTableHeaderFont ()),
                 _th (aCtx.label ("section.lines.column.description"), aCtx.getTableHeaderFont ()),
                 _thRight (aCtx.label ("BT-152"), aCtx.getTableHeaderFont ()),
                 _thRight (aCtx.label ("section.lines.column.linetotal"), aCtx.getTableHeaderFont ()));
    for (final NormaDocumentAllowance aA : aInvoice.documentAllowances ())
      aTbl.addRow (_td (aCtx.label ("BG-20"), aBF),
                   _td (_n (aA.getReason ()), aBF),
                   _tdRight (_n (aA.getVATCategoryCode ()) +
                             (aA.getVATRate () == null ? "" : " " +
                                                              NormaFormat.percent (aA.getVATRate (),
                                                                                   aCtx.getLocale (),
                                                                                   aCtx.getFormatOptions ())),
                             aBF),
                   _tdRight (_amount (_neg (aA.getAmount ()), sCurrency, aCtx), aBF));
    for (final NormaDocumentCharge aC : aInvoice.documentCharges ())
      aTbl.addRow (_td (aCtx.label ("BG-21"), aBF),
                   _td (_n (aC.getReason ()), aBF),
                   _tdRight (_n (aC.getVATCategoryCode ()) +
                             (aC.getVATRate () == null ? "" : " " +
                                                              NormaFormat.percent (aC.getVATRate (),
                                                                                   aCtx.getLocale (),
                                                                                   aCtx.getFormatOptions ())),
                             aBF),
                   _tdRight (_amount (aC.getAmount (), sCurrency, aCtx), aBF));
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderVATBreakdown (@NonNull final PLPageSet aPS,
                                           @NonNull final NormaInvoice aInvoice,
                                           @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.vatBreakdowns ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.vatbreakdown.title"), aCtx);
    final String sCurrency = aInvoice.getCurrency ();
    final FontSpec aBF = aCtx.getBodyFont ();
    final PLTable aTbl = PLTable.createWithPercentage (30, 20, 25, 25);
    aTbl.setHeaderRowCount (1);
    aTbl.addRow (_th (aCtx.label ("BT-151"), aCtx.getTableHeaderFont ()),
                 _th (aCtx.label ("BT-152"), aCtx.getTableHeaderFont ()),
                 _th (aCtx.label ("BT-116"), aCtx.getTableHeaderFont ()),
                 _th (aCtx.label ("BT-117"), aCtx.getTableHeaderFont ()));
    for (final NormaVATBreakdown aV : aInvoice.vatBreakdowns ())
    {
      final String sCat = aV.getCategoryCode ();
      aTbl.addRow (_td (sCat == null ? "" : sCat + " — " + aCtx.codeLabel ("vat.category", sCat), aBF),
                   _td (aV.getRate () == null ? "" : NormaFormat.percent (aV.getRate (),
                                                                          aCtx.getLocale (),
                                                                          aCtx.getFormatOptions ()), aBF),
                   _td (_amount (aV.getTaxableAmount (), sCurrency, aCtx), aBF),
                   _td (_amount (aV.getTaxAmount (), sCurrency, aCtx), aBF));
    }
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderTotals (@NonNull final PLPageSet aPS,
                                     @NonNull final NormaInvoice aInvoice,
                                     @NonNull final PDFRenderContext aCtx)
  {
    _sectionTitle (aPS, aCtx.label ("section.totals.title"), aCtx);
    final String sCurrency = aInvoice.getCurrency ();
    final PLTable aTbl = PLTable.createWithPercentage (60, 40);
    final FontSpec aBF = aCtx.getBodyFont ();
    _totRow (aTbl, aCtx.label ("BT-106"), aInvoice.getDocumentTotals ().getLineNetTotal (), sCurrency, aCtx, aBF);
    _totRow (aTbl,
             aCtx.label ("BT-107"),
             _neg (aInvoice.getDocumentTotals ().getAllowanceTotal ()),
             sCurrency,
             aCtx,
             aBF);
    _totRow (aTbl, aCtx.label ("BT-108"), aInvoice.getDocumentTotals ().getChargeTotal (), sCurrency, aCtx, aBF);
    _totRow (aTbl, aCtx.label ("BT-109"), aInvoice.getDocumentTotals ().getTaxExclusiveTotal (), sCurrency, aCtx, aBF);
    _totRow (aTbl, aCtx.label ("BT-110"), aInvoice.getDocumentTotals ().getTaxTotal (), sCurrency, aCtx, aBF);
    _totRow (aTbl, aCtx.label ("BT-112"), aInvoice.getDocumentTotals ().getTaxInclusiveTotal (), sCurrency, aCtx, aBF);
    _totRow (aTbl,
             aCtx.label ("BT-113"),
             _neg (aInvoice.getDocumentTotals ().getPrepaidAmount ()),
             sCurrency,
             aCtx,
             aBF);
    _totRow (aTbl, aCtx.label ("BT-114"), aInvoice.getDocumentTotals ().getRoundingAmount (), sCurrency, aCtx, aBF);
    _totRow (aTbl,
             aCtx.label ("BT-115"),
             aInvoice.getDocumentTotals ().getPayableAmount (),
             sCurrency,
             aCtx,
             aCtx.getHeaderFont ());
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _totRow (@NonNull final PLTable aTbl,
                               @NonNull final String sLabel,
                               @Nullable final BigDecimal aAmount,
                               @Nullable final String sCurrency,
                               @NonNull final PDFRenderContext aCtx,
                               @NonNull final FontSpec aFont)
  {
    if (aAmount == null)
      return;
    aTbl.addRow (_td (sLabel, aFont),
                 new PLTableCell (new PLText (_amount (aAmount, sCurrency, aCtx), aFont).setHorzAlign (
                                                                                                       EHorzAlignment.RIGHT)));
  }

  private static void _renderPayment (@NonNull final PLPageSet aPS,
                                      @NonNull final NormaInvoice aInvoice,
                                      @NonNull final PDFRenderContext aCtx)
  {
    final NormaPaymentInstructions aPI = aInvoice.getPaymentInstructions ();
    if (aPI == null && aInvoice.getPaymentDueDate () == null)
      return;
    _sectionTitle (aPS, aCtx.label ("section.payment.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    if (aInvoice.getPaymentDueDate () != null)
      sb.append (aCtx.label ("BT-9")).append (": ").append (_date (aInvoice.getPaymentDueDate (), aCtx)).append ('\n');
    if (aPI != null)
    {
      if (aPI.getPaymentMeansCode () != null)
        sb.append (aCtx.label ("BT-81"))
          .append (": ")
          .append (aPI.getPaymentMeansCode ())
          .append (" — ")
          .append (aCtx.codeLabel ("paymentmeans", aPI.getPaymentMeansCode ()))
          .append ('\n');
      if (aPI.getPaymentMeansText () != null)
        sb.append (aPI.getPaymentMeansText ()).append ('\n');
      if (aPI.getRemittanceInformation () != null)
        sb.append (aCtx.label ("BT-83")).append (": ").append (aPI.getRemittanceInformation ()).append ('\n');
      for (final NormaCreditTransfer aCT : aPI.creditTransfers ())
      {
        if (aCT.getAccountIdentifier () != null)
          sb.append (aCtx.label ("BT-84")).append (": ").append (aCT.getAccountIdentifier ()).append ('\n');
        if (aCT.getAccountName () != null)
          sb.append (aCtx.label ("BT-85")).append (": ").append (aCT.getAccountName ()).append ('\n');
        if (aCT.getServiceProviderID () != null)
          sb.append (aCtx.label ("BT-86")).append (": ").append (aCT.getServiceProviderID ()).append ('\n');
      }
      if (aPI.getPaymentCard () != null)
      {
        final NormaPaymentCard aPC = aPI.getPaymentCard ();
        sb.append (aCtx.label ("BG-18")).append (":\n");
        if (aPC.getPAN () != null)
          sb.append ("  ").append (aCtx.label ("BT-87")).append (": ").append (aPC.getPAN ()).append ('\n');
        if (aPC.getHolderName () != null)
          sb.append ("  ").append (aCtx.label ("BT-88")).append (": ").append (aPC.getHolderName ()).append ('\n');
      }
      if (aPI.getDirectDebit () != null)
      {
        final NormaDirectDebit aDD = aPI.getDirectDebit ();
        sb.append (aCtx.label ("BG-19")).append (":\n");
        if (aDD.getMandateReferenceID () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-89"))
            .append (": ")
            .append (aDD.getMandateReferenceID ())
            .append ('\n');
        if (aDD.getBankAssignedCreditorID () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-90"))
            .append (": ")
            .append (aDD.getBankAssignedCreditorID ())
            .append ('\n');
        if (aDD.getDebitedAccountID () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-91"))
            .append (": ")
            .append (aDD.getDebitedAccountID ())
            .append ('\n');
      }
    }
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderPaymentTerms (@NonNull final PLPageSet aPS,
                                           @NonNull final NormaInvoice aInvoice,
                                           @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.getPaymentTerms () == null)
      return;
    _sectionTitle (aPS, aCtx.label ("BT-20"), aCtx);
    aPS.addElement (new PLText (aInvoice.getPaymentTerms (), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderSupportingDocuments (@NonNull final PLPageSet aPS,
                                                  @NonNull final NormaInvoice aInvoice,
                                                  @NonNull final PDFRenderContext aCtx)
  {
    if (aInvoice.supportingDocuments ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.supportingdocs.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    for (final NormaSupportingDocument aD : aInvoice.supportingDocuments ())
    {
      if (aD.getReference () != null)
        sb.append (aCtx.label ("BT-122")).append (": ").append (aD.getReference ()).append ('\n');
      if (aD.getDescription () != null)
        sb.append (aCtx.label ("BT-123")).append (": ").append (aD.getDescription ()).append ('\n');
      if (aD.getExternalLocation () != null)
        sb.append (aCtx.label ("BT-124")).append (": ").append (aD.getExternalLocation ()).append ('\n');
      sb.append ('\n');
    }
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
  }

  // ===== Helpers =====

  private static void _sectionTitle (@NonNull final PLPageSet aPS,
                                     @NonNull final String sTitle,
                                     @NonNull final PDFRenderContext aCtx)
  {
    final PLText aT = new PLText (sTitle, aCtx.getHeaderFont ());
    aT.setMarginTop (4).setMarginBottom (2).setBorderBottom (new BorderStyleSpec (PLColor.GRAY));
    aPS.addElement (aT);
  }

  @NonNull
  private static PLTableCell _th (@NonNull final String sText, @NonNull final FontSpec aFont)
  {
    // For empty header cells, wrap a 1-pt PLSpacerX so PLTable still has something to lay out.
    // PLText with an empty string is treated as "no element" by PLBox#render.
    if (sText.isEmpty ())
      return new PLTableCell (new PLSpacerX (1f)).setFillColor (PLColor.gray (0xf0));
    return new PLTableCell (new PLText (sText, aFont).setPadding (new PaddingSpec (3))).setFillColor (PLColor.gray (
                                                                                                                    0xf0));
  }

  @NonNull
  private static PLTableCell _td (@NonNull final String sText, @NonNull final FontSpec aFont)
  {
    // Empty cells use PLSpacerX so PLTable doesn't log "no element is contained" for the
    // original layout pass. Note: PLTable still emits the same INFO when splitting a row
    // across page boundaries (its internal split-cell carries a null element by design).
    if (sText.isEmpty ())
      return new PLTableCell (new PLSpacerX (1f));
    return new PLTableCell (new PLText (sText, aFont).setPadding (new PaddingSpec (3)));
  }

  /** Right-aligned body cell — used for amounts, quantities and percentages. */
  @NonNull
  private static PLTableCell _tdRight (@NonNull final String sText, @NonNull final FontSpec aFont)
  {
    if (sText.isEmpty ())
      return new PLTableCell (new PLSpacerX (1f));
    return new PLTableCell (new PLText (sText, aFont).setPadding (new PaddingSpec (3))
                                                     .setHorzAlign (EHorzAlignment.RIGHT));
  }

  /** Right-aligned header cell — used for numeric column headers above amounts/quantities/rates. */
  @NonNull
  private static PLTableCell _thRight (@NonNull final String sText, @NonNull final FontSpec aFont)
  {
    if (sText.isEmpty ())
      return new PLTableCell (new PLSpacerX (1f)).setFillColor (PLColor.gray (0xf0));
    return new PLTableCell (new PLText (sText, aFont).setPadding (new PaddingSpec (3))
                                                     .setHorzAlign (EHorzAlignment.RIGHT)).setFillColor (PLColor.gray (0xf0));
  }

  private static void _kvRow (@NonNull final PLVBox aVBox,
                              @NonNull final String sLabel,
                              @Nullable final String sValue,
                              @NonNull final PDFRenderContext aCtx)
  {
    if (sValue == null || sValue.isEmpty ())
      return;
    aVBox.addRow (new PLText (sLabel + ": " + sValue, aCtx.getBodyFont ()));
  }

  @NonNull
  private static String _date (@Nullable final LocalDate aDate, @NonNull final PDFRenderContext aCtx)
  {
    return NormaFormat.date (aDate, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @NonNull
  private static String _amount (@Nullable final BigDecimal aAmount,
                                 @Nullable final String sCurrency,
                                 @NonNull final PDFRenderContext aCtx)
  {
    return NormaFormat.amount (aAmount, sCurrency, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @NonNull
  private static String _qty (@Nullable final BigDecimal aQty, @Nullable final String sUnit)
  {
    if (aQty == null)
      return "";
    return aQty.stripTrailingZeros ().toPlainString () + (sUnit == null ? "" : " " + sUnit);
  }

  @NonNull
  private static String _docTypeText (@Nullable final String sCode, @NonNull final PDFRenderContext aCtx)
  {
    return sCode == null ? "" : sCode + " — " + aCtx.codeLabel ("doctype", sCode);
  }

  @NonNull
  private static String _n (@Nullable final String s)
  {
    return s == null ? "" : s;
  }

  @Nullable
  private static BigDecimal _neg (@Nullable final BigDecimal aBD)
  {
    return aBD == null ? null : aBD.negate ();
  }

  private static void _appendLine (@NonNull final StringBuilder sb, @Nullable final String s)
  {
    if (s != null && !s.isEmpty ())
      sb.append (s).append ('\n');
  }

  private static void _appendRefIf (@NonNull final StringBuilder sb,
                                    @NonNull final String sLabel,
                                    @Nullable final String sValue)
  {
    if (sValue != null && !sValue.isEmpty ())
      sb.append (sLabel).append (": ").append (sValue).append ('\n');
  }

  @NonNull
  private static String _trim (@NonNull final StringBuilder sb)
  {
    String s = sb.toString ();
    while (s.endsWith ("\n"))
      s = s.substring (0, s.length () - 1);
    return s;
  }
}
