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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

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
import com.helger.base.string.StringHelper;
import com.helger.phlox.api.PhloxRuntimeException;
import com.helger.phlox.api.i18n.PhloxFormat;
import com.helger.phlox.api.model.*;
import com.helger.phlox.api.template.IPhloxTemplate;
import com.helger.phlox.api.watermark.EWatermarkPosition;
import com.helger.phlox.api.watermark.PhloxProgramWatermark;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.api.write.IInvoiceWriter;
import com.helger.phlox.template.PhloxTemplate;
import com.helger.pdflayout.IPDDocumentCustomizer;
import com.helger.pdflayout.PDFCreationException;
import com.helger.pdflayout.PageLayoutPDF;
import com.helger.pdflayout.base.IPLRenderableObject;
import com.helger.pdflayout.base.PLColor;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.box.PLBox;
import com.helger.pdflayout.element.hbox.PLHBox;
import com.helger.pdflayout.element.special.PLSpacerX;
import com.helger.pdflayout.element.special.PLSpacerY;
import com.helger.pdflayout.element.table.PLTable;
import com.helger.pdflayout.element.table.PLTableCell;
import com.helger.pdflayout.element.text.PLText;
import com.helger.pdflayout.element.vbox.PLVBox;
import com.helger.pdflayout.pdfbox.PDPageContentStreamExt;
import com.helger.pdflayout.richtext.element.PLRichText;
import com.helger.pdflayout.richtext.run.PLFontFamily;
import com.helger.pdflayout.spec.BorderStyleSpec;
import com.helger.pdflayout.spec.EHorzAlignment;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.PaddingSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.pdflayout.spec.WidthSpec;

/**
 * Renders a {@link PhloxInvoice} as a PDF using {@code ph-pdf-layout}.
 *
 * @author Philip Helger
 */
public class PdfInvoiceWriter implements IInvoiceWriter <PdfRenderContext>
{
  private static final String PROJECT_URL = "https://github.com/phax/phlox";

  /** Create a new PDF invoice writer. */
  public PdfInvoiceWriter ()
  {}

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

  /**
   * Create the footer with the attribution of the program. The text links to the phlox project.
   */
  @NonNull
  private static IPLRenderableObject <?> _createProgramFooter (@NonNull final WatermarkSpec aSpec)
  {
    // PLColor is RGB-only - approximate the opacity by lightening the colour towards white
    final Color aRGB = aSpec.getColor ();
    final float fO = Math.max (0f, Math.min (1f, aSpec.getOpacity ()));
    final PLColor aColor = new PLColor (Math.round (255 - (255 - aRGB.getRed ()) * fO),
                                        Math.round (255 - (255 - aRGB.getGreen ()) * fO),
                                        Math.round (255 - (255 - aRGB.getBlue ()) * fO));
    final PLRichText aText = PLRichText.createFromMarkup ("{link[" +
                                                          PROJECT_URL +
                                                          "]}" +
                                                          PdfTextSupport.escapeMarkup (aSpec.getText ()) +
                                                          "{link}",
                                                          PLFontFamily.regular (),
                                                          aSpec.getFontSizePt (),
                                                          aColor);
    aText.setHorzAlign (EHorzAlignment.RIGHT);
    return new PLBox (aText).setMarginTop (4).setHorzAlign (EHorzAlignment.RIGHT);
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

  @NonNull
  private static PLVBox _partyBox (@NonNull final String sTitle,
                                   @NonNull final String sBody,
                                   @NonNull final PdfRenderContext aCtx)
  {
    final PLVBox aBox = new PLVBox ();
    aBox.addRow (new PLText (sTitle, aCtx.getHeaderFont ()).setMarginBottom (3));
    aBox.addRow (new PLText (sBody, aCtx.getBodyFont ()));
    return aBox;
  }

  private static void _appendContact (@NonNull final StringBuilder sb,
                                      @Nullable final PhloxContact aC,
                                      @NonNull final PdfRenderContext aCtx)
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

  private static void _appendExtra (@NonNull final StringBuilder sb,
                                    @NonNull final String sLabel,
                                    @Nullable final String sValue)
  {
    if (sValue != null && !sValue.isEmpty ())
      sb.append ('\n').append (sLabel).append (": ").append (sValue);
  }

  @NonNull
  private static String _lineVatCell (@NonNull final PhloxInvoiceLine aLine, @NonNull final PdfRenderContext aCtx)
  {
    if (aLine.getLineVat () == null)
      return "";
    final StringBuilder sb = new StringBuilder ();
    if (aLine.getLineVat ().getCategoryCode () != null)
      sb.append (aLine.getLineVat ().getCategoryCode ());
    if (aLine.getLineVat ().getRate () != null)
    {
      if (sb.length () > 0)
        sb.append (' ');
      sb.append (PhloxFormat.percent (aLine.getLineVat ().getRate (), aCtx.getLocale (), aCtx.getFormatOptions ()));
    }
    return sb.toString ();
  }

  @NonNull
  private static String _allowanceExtras (@NonNull final PhloxDocumentAllowance aA, @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendExtra (sb, aCtx.label ("BT-173"), aA.getExemptionReasonText ());
    _appendExtra (sb, aCtx.label ("BT-174"), aA.getExemptionReasonCode ());
    _appendExtra (sb, aCtx.label ("BT-213"), aA.getGoodsServicesCode ());
    return sb.toString ();
  }

  @NonNull
  private static String _chargeExtras (@NonNull final PhloxDocumentCharge aC, @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendExtra (sb, aCtx.label ("BT-175"), aC.getExemptionReasonText ());
    _appendExtra (sb, aCtx.label ("BT-176"), aC.getExemptionReasonCode ());
    _appendExtra (sb, aCtx.label ("BT-177"), aC.getNonVatTaxCode ());
    _appendExtra (sb, aCtx.label ("BT-214"), aC.getGoodsServicesCode ());
    return sb.toString ();
  }

  private static void _sectionTitle (@NonNull final PLPageSet aPS,
                                     @NonNull final String sTitle,
                                     @NonNull final PdfRenderContext aCtx)
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
                              @NonNull final PdfRenderContext aCtx)
  {
    if (StringHelper.isNotEmpty (sValue))
      aVBox.addRow (new PLText (sLabel + ": " + sValue, aCtx.getBodyFont ()));
  }

  private static void _renderProcessControl (@NonNull final PLPageSet aPS,
                                             @NonNull final PhloxInvoice aInvoice,
                                             @NonNull final PdfRenderContext aCtx)
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

  @NonNull
  private static String _date (@Nullable final LocalDate aDate, @NonNull final PdfRenderContext aCtx)
  {
    return PhloxFormat.date (aDate, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @NonNull
  private static String _amount (@Nullable final BigDecimal aAmount,
                                 @Nullable final String sCurrency,
                                 @NonNull final PdfRenderContext aCtx)
  {
    final String sAmount = PhloxFormat.amount (aAmount, sCurrency, aCtx.getLocale (), aCtx.getFormatOptions ());
    return PdfTextSupport.separateCurrencySymbol (sAmount);
  }

  private static void _renderVatBreakdown (@NonNull final PLPageSet aPS,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.vatBreakdowns ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.vatbreakdown.title"), aCtx);
    final String sCurrency = aInvoice.getCurrency ();
    final FontSpec aBF = aCtx.getBodyFont ();
    final PLTable aTbl = PLTable.createWithPercentage (30, 20, 25, 25);
    aTbl.setHeaderRowCount (1);
    aTbl.addRow (_th (aCtx.label ("BT-151"), aCtx.getTableHeaderFont ()),
                 _thRight (aCtx.label ("BT-152"), aCtx.getTableHeaderFont ()),
                 _thRight (aCtx.label ("BT-116"), aCtx.getTableHeaderFont ()),
                 _thRight (aCtx.label ("BT-117"), aCtx.getTableHeaderFont ()));
    for (final PhloxVatBreakdown aV : aInvoice.vatBreakdowns ())
    {
      final String sCat = aV.getCategoryCode ();
      final StringBuilder aCatText = new StringBuilder (sCat == null ? "" : sCat + " — " + aCtx.codeLabel ("vat.category", sCat));
      _appendExtra (aCatText, aCtx.label ("BT-184"), aV.getCurrency ());
      _appendExtra (aCatText, aCtx.label ("BT-210"), aV.getGoodsServicesCode ());
      aTbl.addRow (_td (aCatText.toString (), aBF),
                   _tdRight (aV.getRate () == null ? "" : PhloxFormat.percent (aV.getRate (),
                                                                               aCtx.getLocale (),
                                                                               aCtx.getFormatOptions ()), aBF),
                   _tdRight (_amount (aV.getTaxableAmount (), sCurrency, aCtx), aBF),
                   _tdRight (_amount (aV.getTaxAmount (), sCurrency, aCtx), aBF));
    }
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _totRow (@NonNull final PLTable aTbl,
                               @NonNull final String sLabel,
                               @Nullable final BigDecimal aAmount,
                               @Nullable final String sCurrency,
                               @NonNull final PdfRenderContext aCtx,
                               @NonNull final FontSpec aFont)
  {
    if (aAmount == null)
      return;
    aTbl.addRow (_td (sLabel, aFont),
                 new PLTableCell (new PLText (_amount (aAmount, sCurrency, aCtx), aFont).setHorzAlign (
                                                                                                       EHorzAlignment.RIGHT)));
  }

  @NonNull
  private static String _qty (@Nullable final BigDecimal aQty, @Nullable final String sUnit)
  {
    if (aQty == null)
      return "";
    return aQty.stripTrailingZeros ().toPlainString () + (sUnit == null ? "" : " " + sUnit);
  }

  @NonNull
  private static String _docTypeText (@Nullable final String sCode, @NonNull final PdfRenderContext aCtx)
  {
    return sCode == null ? "" : sCode + " — " + aCtx.codeLabel ("doctype", sCode);
  }

  @NonNull
  private static String _time (@Nullable final OffsetTime aTime)
  {
    return aTime == null ? "" : aTime.format (DateTimeFormatter.ISO_OFFSET_TIME);
  }

  private static void _renderHeader (@NonNull final PLPageSet aPS,
                                     @NonNull final PhloxInvoice aInvoice,
                                     @NonNull final PdfRenderContext aCtx)
  {
    final String sTitle = aInvoice.getKind ().isCreditNote () ? aCtx.label ("section.header.title.creditnote") : aCtx
                                                                                                                     .label ("section.header.title.invoice");
    aPS.addElement (new PLText (sTitle, new FontSpec (PreloadFont.REGULAR_BOLD, 18f)).setMarginBottom (6));

    final PLVBox aMeta = new PLVBox ();
    _kvRow (aMeta, aCtx.label ("BT-1"), aInvoice.getNumber (), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-2"), _date (aInvoice.getIssueDate (), aCtx), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-166"), _time (aInvoice.getIssueTime ()), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-3"), _docTypeText (aInvoice.getTypeCode (), aCtx), aCtx);
    _kvRow (aMeta, aCtx.label ("BT-5"), aInvoice.getCurrency (), aCtx);
    aPS.addElement (aMeta);
    aPS.addElement (new PLSpacerY (8));
  }

  @NonNull
  private static String _percent (@NonNull final BigDecimal aPercent, @NonNull final PdfRenderContext aCtx)
  {
    return PhloxFormat.percent (aPercent, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @Nullable
  private static String _identifier (@Nullable final PhloxIdentifier aID)
  {
    if (aID == null || aID.getValue () == null || aID.getValue ().isEmpty ())
      return null;
    return aID.getSchemeID () == null || aID.getSchemeID ().isEmpty () ? aID.getValue ()
                                                                       : aID.getValue () + " (" + aID.getSchemeID () + ")";
  }

  @Nullable
  private static String _buyerReferences (@NonNull final PhloxInvoice aInvoice)
  {
    final StringBuilder sb = new StringBuilder ();
    for (final PhloxIdentifier aID : aInvoice.buyerReferences ())
    {
      final String sText = _identifier (aID);
      if (sText != null)
      {
        if (sb.length () > 0)
          sb.append (", ");
        sb.append (sText);
      }
    }
    return sb.length () == 0 ? null : sb.toString ();
  }

  @Nullable
  private static String _addressText (@Nullable final PhloxPostalAddress aAddr)
  {
    if (aAddr == null || aAddr.isEmpty ())
      return null;
    final StringBuilder sb = new StringBuilder ();
    for (final String s : new String [] { aAddr.getLine1 (),
                                          aAddr.getLine2 (),
                                          aAddr.getLine3 (),
                                          aAddr.getPostCode () == null ? aAddr.getCity ()
                                                                       : aAddr.getPostCode () + (aAddr.getCity () == null ? "" : " " + aAddr.getCity ()),
                                          aAddr.getCountrySubdivision (),
                                          aAddr.getCountryCode () })
      if (s != null && !s.isEmpty ())
      {
        if (sb.length () > 0)
          sb.append (", ");
        sb.append (s);
      }
    return sb.length () == 0 ? null : sb.toString ();
  }

  private static void _appendLineExtras (@NonNull final StringBuilder sb,
                                         @NonNull final PhloxInvoiceLine aLine,
                                         @NonNull final PdfRenderContext aCtx)
  {
    if (aLine.getItem () != null)
      _appendExtra (sb, aCtx.label ("BT-196"), aLine.getItem ().getGoodsServicesCode ());
    if (aLine.getLineVat () != null)
    {
      _appendExtra (sb, aCtx.label ("BT-194"), aLine.getLineVat ().getExemptionReasonText ());
      _appendExtra (sb, aCtx.label ("BT-195"), aLine.getLineVat ().getExemptionReasonCode ());
    }
    _appendExtra (sb, aCtx.label ("BT-188"), aLine.getPurchaseOrderReference ());
    _appendExtra (sb, aCtx.label ("BT-200"), aLine.getSalesOrderReference ());
    _appendExtra (sb, aCtx.label ("BT-201"), aLine.getSalesOrderLineReference ());
    _appendExtra (sb, aCtx.label ("BT-189"), aLine.getDespatchAdviceReference ());
    _appendExtra (sb, aCtx.label ("BT-190"), aLine.getDespatchAdviceLineReference ());
    _appendExtra (sb, aCtx.label ("BT-191"), aLine.getReceivingAdviceReference ());
    _appendExtra (sb, aCtx.label ("BT-192"), aLine.getReceivingAdviceLineReference ());
    _appendExtra (sb, aCtx.label ("BT-198"), aLine.getDeliveryNoteReference ());
    _appendExtra (sb, aCtx.label ("BT-199"), aLine.getDeliveryNoteLineReference ());
    for (final PhloxLinePrecedingInvoiceRef aRef : aLine.precedingInvoiceRefs ())
    {
      final StringBuilder aRow = new StringBuilder ();
      if (aRef.getNumber () != null)
        aRow.append (aRef.getNumber ());
      if (aRef.getIssueDate () != null)
        aRow.append (" (").append (_date (aRef.getIssueDate (), aCtx)).append (')');
      if (aRef.getTypeCode () != null)
        aRow.append (" [").append (aRef.getTypeCode ()).append (']');
      if (aRef.getLineReference () != null)
        aRow.append (", ").append (aCtx.label ("BT-220")).append (' ').append (aRef.getLineReference ());
      _appendExtra (sb, aCtx.label ("BG-39"), aRow.toString ());
    }
    final PhloxLineDelivery aDel = aLine.getDelivery ();
    if (aDel != null)
    {
      _appendExtra (sb, aCtx.label ("BT-185"), aDel.getPartyName ());
      _appendExtra (sb, aCtx.label ("BT-186"), _identifier (aDel.getLocationIdentifier ()));
      _appendExtra (sb,
                    aCtx.label ("BT-187"),
                    aDel.getActualDeliveryDate () == null ? null : _date (aDel.getActualDeliveryDate (), aCtx));
      _appendExtra (sb, aCtx.label ("BG-38"), _addressText (aDel.getPostalAddress ()));
    }
  }

  @NonNull
  private static String _n (@Nullable final String s)
  {
    return s == null ? "" : s;
  }

  @NonNull
  private static String _exchangeRateText (@NonNull final PhloxInvoice aInvoice)
  {
    final StringBuilder sb = new StringBuilder (aInvoice.getVatExchangeRate ().stripTrailingZeros ().toPlainString ());
    if (aInvoice.getVatExchangeRateSourceCurrency () != null || aInvoice.getVatExchangeRateTargetCurrency () != null)
      sb.append (" (")
        .append (_n (aInvoice.getVatExchangeRateSourceCurrency ()))
        .append (" → ")
        .append (_n (aInvoice.getVatExchangeRateTargetCurrency ()))
        .append (')');
    return sb.toString ();
  }

  @NonNull
  private static String _attributeText (@NonNull final PhloxItemAttribute aA)
  {
    final StringBuilder sb = new StringBuilder (_n (aA.getName ()));
    if (aA.getCode () != null)
      sb.append (sb.length () > 0 ? " [" : "[").append (aA.getCode ()).append (']');
    sb.append (": ").append (_n (aA.getValue ()));
    if (aA.getValueUnit () != null)
      sb.append (' ').append (aA.getValueUnit ());
    return sb.toString ();
  }

  private static void _renderLines (@NonNull final PLPageSet aPS,
                                    @NonNull final PhloxInvoice aInvoice,
                                    @NonNull final PdfRenderContext aCtx)
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
    for (final PhloxInvoiceLine aLine : aInvoice.lines ())
    {
      final StringBuilder aDesc = new StringBuilder ();
      final PhloxItem aItem = aLine.getItem ();
      if (aItem != null)
      {
        if (aItem.getName () != null)
          aDesc.append (aItem.getName ());
        if (aItem.getDescription () != null)
          aDesc.append ('\n').append (aItem.getDescription ());
        for (final PhloxItemAttribute aA : aItem.attributes ())
          if (aA.getName () != null || aA.getValue () != null || aA.getCode () != null)
            aDesc.append ('\n').append (_attributeText (aA));
      }
      if (aLine.getNote () != null)
        aDesc.append ('\n').append (aLine.getNote ());
      _appendLineExtras (aDesc, aLine, aCtx);
      aTbl.addRow (_td (_n (aLine.getID ()), aBF),
                   _td (aDesc.toString (), aBF),
                   _tdRight (_qty (aLine.getQuantity (), aLine.getQuantityUnit ()), aBF),
                   _tdRight (aLine.getPrice () == null ? "" : _amount (aLine.getPrice ().getNetPrice (),
                                                                       sCurrency,
                                                                       aCtx), aBF),
                   _tdRight (_lineVatCell (aLine, aCtx), aBF),
                   _tdRight (_amount (aLine.getNetAmount (), sCurrency, aCtx), aBF));
    }
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (8));
  }

  @Nullable
  private static BigDecimal _neg (@Nullable final BigDecimal aBD)
  {
    return aBD == null ? null : aBD.negate ();
  }

  private static void _renderAllowancesCharges (@NonNull final PLPageSet aPS,
                                                @NonNull final PhloxInvoice aInvoice,
                                                @NonNull final PdfRenderContext aCtx)
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
    for (final PhloxDocumentAllowance aA : aInvoice.documentAllowances ())
      aTbl.addRow (_td (aCtx.label ("BG-20"), aBF),
                   _td (_n (aA.getReason ()) + _allowanceExtras (aA, aCtx), aBF),
                   _tdRight (_n (aA.getVatCategoryCode ()) +
                             (aA.getVatRate () == null ? "" : " " +
                                                              PhloxFormat.percent (aA.getVatRate (),
                                                                                   aCtx.getLocale (),
                                                                                   aCtx.getFormatOptions ())),
                             aBF),
                   _tdRight (_amount (_neg (aA.getAmount ()), sCurrency, aCtx), aBF));
    for (final PhloxDocumentCharge aC : aInvoice.documentCharges ())
      aTbl.addRow (_td (aCtx.label ("BG-21"), aBF),
                   _td (_n (aC.getReason ()) + _chargeExtras (aC, aCtx), aBF),
                   _tdRight (_n (aC.getVatCategoryCode ()) +
                             (aC.getVatRate () == null ? "" : " " +
                                                              PhloxFormat.percent (aC.getVatRate (),
                                                                                   aCtx.getLocale (),
                                                                                   aCtx.getFormatOptions ())),
                             aBF),
                   _tdRight (_amount (aC.getAmount (), sCurrency, aCtx), aBF));
    aPS.addElement (aTbl);
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderTotals (@NonNull final PLPageSet aPS,
                                     @NonNull final PhloxInvoice aInvoice,
                                     @NonNull final PdfRenderContext aCtx)
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
    for (final PhloxThirdPartyCharge aTPC : aInvoice.getDocumentTotals ().thirdPartyCharges ())
      _totRow (aTbl,
               aCtx.label ("BG-34") + (aTPC.getSpecification () == null ? "" : ": " + aTPC.getSpecification ()),
               aTPC.getAmount (),
               sCurrency,
               aCtx,
               aBF);
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
    if (aInvoice.getVatExchangeRate () != null)
      aPS.addElement (new PLText (aCtx.label ("BT-167") + ": " + _exchangeRateText (aInvoice), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _appendLine (@NonNull final StringBuilder sb, @Nullable final String s)
  {
    if (StringHelper.isNotEmpty (s))
      sb.append (s).append ('\n');
  }

  private static void _appendAddress (@NonNull final StringBuilder sb, @Nullable final PhloxPostalAddress aA)
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
    return StringHelper.trimEndRepeatedly (sb.toString (), '\n');
  }

  @NonNull
  private static String _sellerText (@NonNull final PhloxSeller aS, @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendLine (sb, aS.getName ());
    _appendLine (sb, aS.getTradingName ());
    _appendAddress (sb, aS.getPostalAddress ());
    if (aS.getVatIdentifier () != null)
      sb.append (aCtx.label ("BT-31")).append (": ").append (aS.getVatIdentifier ()).append ('\n');
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
  private static String _buyerText (@NonNull final PhloxBuyer aB, @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendLine (sb, aB.getName ());
    _appendLine (sb, aB.getTradingName ());
    _appendAddress (sb, aB.getPostalAddress ());
    if (aB.getVatIdentifier () != null)
      sb.append (aCtx.label ("BT-48")).append (": ").append (aB.getVatIdentifier ()).append ('\n');
    if (aB.getLegalRegistration () != null && aB.getLegalRegistration ().getValue () != null)
      sb.append (aCtx.label ("BT-30")).append (": ").append (aB.getLegalRegistration ().getValue ()).append ('\n');
    if (aB.getElectronicAddress () != null && aB.getElectronicAddress ().getValue () != null)
      sb.append (aCtx.label ("BT-34")).append (": ").append (aB.getElectronicAddress ().getValue ()).append ('\n');
    _appendContact (sb, aB.getContact (), aCtx);
    return _trim (sb);
  }

  private static void _renderParties (@NonNull final PLPageSet aPS,
                                      @NonNull final PhloxInvoice aInvoice,
                                      @NonNull final PdfRenderContext aCtx)
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

  private static void _renderOptionalParties (@NonNull final PLPageSet aPS,
                                              @NonNull final PhloxInvoice aInvoice,
                                              @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.getPayee () != null)
    {
      final PhloxPayee aPayee = aInvoice.getPayee ();
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
      final PhloxSellerTaxRepresentative aR = aInvoice.getSellerTaxRepresentative ();
      final StringBuilder sb = new StringBuilder ();
      _appendLine (sb, aR.getName ());
      if (aR.getVatIdentifier () != null)
        sb.append (aCtx.label ("BT-31")).append (": ").append (aR.getVatIdentifier ()).append ('\n');
      _appendAddress (sb, aR.getPostalAddress ());
      _sectionTitle (aPS, aCtx.label ("BG-11"), aCtx);
      aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
      aPS.addElement (new PLSpacerY (6));
    }
  }

  private static void _renderReferences (@NonNull final PLPageSet aPS,
                                         @NonNull final PhloxInvoice aInvoice,
                                         @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    _appendRefIf (sb, aCtx.label ("BT-10"), _buyerReferences (aInvoice));
    _appendRefIf (sb, aCtx.label ("BT-11"), aInvoice.getProjectReference ());
    _appendRefIf (sb, aCtx.label ("BT-12"), aInvoice.getContractReference ());
    _appendRefIf (sb, aCtx.label ("BT-13"), aInvoice.getPurchaseOrderReference ());
    _appendRefIf (sb, aCtx.label ("BT-14"), aInvoice.getSalesOrderReference ());
    _appendRefIf (sb, aCtx.label ("BT-15"), aInvoice.getReceivingAdviceReference ());
    _appendRefIf (sb, aCtx.label ("BT-16"), aInvoice.getDespatchAdviceReference ());
    _appendRefIf (sb, aCtx.label ("BT-197"), aInvoice.getDeliveryNoteReference ());
    _appendRefIf (sb, aCtx.label ("BT-17"), aInvoice.getTenderOrLotReference ());
    _appendRefIf (sb, aCtx.label ("BT-19"), aInvoice.getBuyerAccountingReference ());
    if (aInvoice.getInvoicedObjectIdentifier () != null && aInvoice.getInvoicedObjectIdentifier ().getValue () != null)
      _appendRefIf (sb, aCtx.label ("BT-18"), aInvoice.getInvoicedObjectIdentifier ().getValue ());
    for (final PhloxPrecedingInvoiceRef aRef : aInvoice.precedingInvoiceRefs ())
    {
      final StringBuilder row = new StringBuilder (aCtx.label ("BG-3")).append (": ");
      if (aRef.getNumber () != null)
        row.append (aRef.getNumber ());
      if (aRef.getIssueDate () != null)
        row.append (" (").append (_date (aRef.getIssueDate (), aCtx)).append (')');
      if (aRef.getTypeCode () != null)
        row.append (" [").append (aRef.getTypeCode ()).append (']');
      sb.append (row).append ('\n');
    }
    if (sb.length () == 0)
      return;
    _sectionTitle (aPS, aCtx.label ("section.references.title"), aCtx);
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderInvoiceNotes (@NonNull final PLPageSet aPS,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.invoiceNotes ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.notes.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    for (final PhloxInvoiceNote aN : aInvoice.invoiceNotes ())
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
                                       @NonNull final PhloxInvoice aInvoice,
                                       @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.getDeliveryInformation () == null)
      return;
    final PhloxDeliveryInformation aD = aInvoice.getDeliveryInformation ();
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
                                              @NonNull final PhloxInvoice aInvoice,
                                              @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.getInvoicingPeriod () == null || aInvoice.getInvoicingPeriod ().isEmpty ())
      return;
    final PhloxInvoicingPeriod aP = aInvoice.getInvoicingPeriod ();
    _sectionTitle (aPS, aCtx.label ("BG-14"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    if (aP.getStartDate () != null)
      sb.append (aCtx.label ("BT-73")).append (": ").append (_date (aP.getStartDate (), aCtx)).append ('\n');
    if (aP.getEndDate () != null)
      sb.append (aCtx.label ("BT-74")).append (": ").append (_date (aP.getEndDate (), aCtx)).append ('\n');
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderPayment (@NonNull final PLPageSet aPS,
                                      @NonNull final PhloxInvoice aInvoice,
                                      @NonNull final PdfRenderContext aCtx)
  {
    final PhloxPaymentInstructions aPI = aInvoice.getPaymentInstructions ();
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
      for (final PhloxCreditTransfer aCT : aPI.creditTransfers ())
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
        final PhloxPaymentCard aPC = aPI.getPaymentCard ();
        sb.append (aCtx.label ("BG-18")).append (":\n");
        if (aPC.getPan () != null)
          sb.append ("  ").append (aCtx.label ("BT-87")).append (": ").append (aPC.getPan ()).append ('\n');
        if (aPC.getHolderName () != null)
          sb.append ("  ").append (aCtx.label ("BT-88")).append (": ").append (aPC.getHolderName ()).append ('\n');
      }
      if (aPI.getDirectDebit () != null)
      {
        final PhloxDirectDebit aDD = aPI.getDirectDebit ();
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
        if (aDD.getDebitedAccountProviderID () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-215"))
            .append (": ")
            .append (aDD.getDebitedAccountProviderID ())
            .append ('\n');
        if (aDD.getDebitedAccountName () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-216"))
            .append (": ")
            .append (aDD.getDebitedAccountName ())
            .append ('\n');
      }
    }
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderPaymentTerms (@NonNull final PLPageSet aPS,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final PdfRenderContext aCtx)
  {
    final StringBuilder sb = new StringBuilder ();
    for (final PhloxPaymentTerms aPT : aInvoice.paymentTerms ())
    {
      if (aPT.getText () != null)
        sb.append (aPT.getText ()).append ('\n');
      for (final PhloxEarlyPaymentDiscount aED : aPT.earlyDiscounts ())
      {
        sb.append (aCtx.label ("BG-35")).append (":\n");
        if (aED.getEndDate () != null)
          sb.append ("  ").append (aCtx.label ("BT-170")).append (": ").append (_date (aED.getEndDate (), aCtx)).append ('\n');
        if (aED.getPercentage () != null)
          sb.append ("  ").append (aCtx.label ("BT-171")).append (": ").append (_percent (aED.getPercentage (), aCtx)).append ('\n');
        if (aED.getAmount () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-172"))
            .append (": ")
            .append (_amount (aED.getAmount (), aInvoice.getCurrency (), aCtx))
            .append ('\n');
      }
      for (final PhloxLatePaymentPenalty aLP : aPT.latePenalties ())
      {
        sb.append (aCtx.label ("BG-36")).append (":\n");
        if (aLP.getStartDate () != null)
          sb.append ("  ").append (aCtx.label ("BT-181")).append (": ").append (_date (aLP.getStartDate (), aCtx)).append ('\n');
        if (aLP.getYearlyInterestPercentage () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-182"))
            .append (": ")
            .append (_percent (aLP.getYearlyInterestPercentage (), aCtx))
            .append ('\n');
        if (aLP.getAmount () != null)
          sb.append ("  ")
            .append (aCtx.label ("BT-183"))
            .append (": ")
            .append (_amount (aLP.getAmount (), aInvoice.getCurrency (), aCtx))
            .append ('\n');
      }
    }
    if (sb.length () == 0)
      return;
    _sectionTitle (aPS, aCtx.label ("BG-33"), aCtx);
    aPS.addElement (new PLText (_trim (sb), aCtx.getBodyFont ()));
    aPS.addElement (new PLSpacerY (6));
  }

  private static void _renderSupportingDocuments (@NonNull final PLPageSet aPS,
                                                  @NonNull final PhloxInvoice aInvoice,
                                                  @NonNull final PdfRenderContext aCtx)
  {
    if (aInvoice.supportingDocuments ().isEmpty ())
      return;
    _sectionTitle (aPS, aCtx.label ("section.supportingdocs.title"), aCtx);
    final StringBuilder sb = new StringBuilder ();
    for (final PhloxSupportingDocument aD : aInvoice.supportingDocuments ())
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

  @NonNull
  private PageLayoutPDF _build (@NonNull final PhloxInvoice aInvoice, @NonNull final PdfRenderContext aCtx)
  {
    final PageLayoutPDF aPdf = new PageLayoutPDF ();

    final IPhloxTemplate aTemplate = aCtx.getTemplate ();
    final PLPageSet aPS;
    if (aTemplate != null)
    {
      if (!(aTemplate instanceof final PhloxTemplate aPhloxTemplate))
        throw new PhloxRuntimeException ("Unsupported template implementation " + aTemplate.getClass ().getName ());
      aPS = new PdfTemplateEmitter (aCtx).emit (aPhloxTemplate.renderAst (aInvoice, aCtx));
    }
    else
    {
      aPS = new PLPageSet (aCtx.getPageSize ()).setMargin (aCtx.getMargins ());

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
      _renderVatBreakdown (aPS, aInvoice, aCtx);
      _renderTotals (aPS, aInvoice, aCtx);
      _renderPayment (aPS, aInvoice, aCtx);
      _renderPaymentTerms (aPS, aInvoice, aCtx);
      _renderSupportingDocuments (aPS, aInvoice, aCtx);
    }

    aPdf.addPageSet (aPS);

    // Watermarks are drawn after all pages are laid out, via a document customizer that
    // prepends content (so the watermark appears behind the page text). Pattern lifted from
    // ph-pdf-layout's PLPageSetTest.testCreateWatermark.
    final WatermarkSpec aUW = aCtx.getUserWatermark ();
    if (aUW != null)
      aPdf.setDocumentCustomizer (_buildWatermarkCustomizer (aUW, null));

    // The attribution of the program is a link to the project, so it is part of the layout
    final WatermarkSpec aPW = PhloxProgramWatermark.getInstance ();
    if (aPW != null)
      aPS.setPageFooter (_createProgramFooter (aPW));

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
  @Override
  public void writeTo (@NonNull final PhloxInvoice aInvoice,
                       @NonNull final PdfRenderContext aCtx,
                       @NonNull @WillClose final OutputStream aOut) throws PDFCreationException
  {
    _build (aInvoice, aCtx).renderTo (aOut);
  }
}
