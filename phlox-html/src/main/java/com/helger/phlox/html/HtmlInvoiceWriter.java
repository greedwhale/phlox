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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.html.css.DefaultCSSClassProvider;
import com.helger.html.css.ICSSClassProvider;
import com.helger.html.hc.html.embedded.HCImg;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCP;
import com.helger.html.hc.html.metadata.HCStyle;
import com.helger.html.hc.html.root.HCHtml;
import com.helger.html.hc.html.sections.HCBody;
import com.helger.url.SimpleURL;
import com.helger.html.hc.html.sections.HCH1;
import com.helger.html.hc.html.sections.HCH2;
import com.helger.html.hc.html.tabular.HCRow;
import com.helger.html.hc.html.tabular.HCTable;
import com.helger.html.hc.html.tabular.IHCCell;
import com.helger.html.hc.html.textlevel.HCSpan;
import com.helger.html.hc.render.HCRenderer;
import com.helger.phlox.api.i18n.PhloxFormat;
import com.helger.phlox.api.model.EPhloxDocumentKind;
import com.helger.phlox.api.model.PhloxAttachment;
import com.helger.phlox.api.model.PhloxBuyer;
import com.helger.phlox.api.model.PhloxContact;
import com.helger.phlox.api.model.PhloxCreditTransfer;
import com.helger.phlox.api.model.PhloxDeliveryInformation;
import com.helger.phlox.api.model.PhloxDirectDebit;
import com.helger.phlox.api.model.PhloxDocumentAllowance;
import com.helger.phlox.api.model.PhloxDocumentCharge;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.model.PhloxInvoiceLine;
import com.helger.phlox.api.model.PhloxInvoiceNote;
import com.helger.phlox.api.model.PhloxInvoicingPeriod;
import com.helger.phlox.api.model.PhloxItem;
import com.helger.phlox.api.model.PhloxItemAttribute;
import com.helger.phlox.api.model.PhloxLineAllowance;
import com.helger.phlox.api.model.PhloxLineCharge;
import com.helger.phlox.api.model.PhloxPayee;
import com.helger.phlox.api.model.PhloxPaymentCard;
import com.helger.phlox.api.model.PhloxPaymentInstructions;
import com.helger.phlox.api.model.PhloxPostalAddress;
import com.helger.phlox.api.model.PhloxPrecedingInvoiceRef;
import com.helger.phlox.api.model.PhloxSeller;
import com.helger.phlox.api.model.PhloxSellerTaxRepresentative;
import com.helger.phlox.api.model.PhloxSupportingDocument;
import com.helger.phlox.api.model.PhloxVatBreakdown;
import com.helger.phlox.api.watermark.PhloxProgramWatermark;
import com.helger.phlox.api.watermark.WatermarkSpec;
import com.helger.phlox.api.write.IInvoiceWriter;

/**
 * Renders a {@link PhloxInvoice} as an HTML5 document. Built on {@code ph-oton-html}.
 *
 * @author Philip Helger
 */
public class HtmlInvoiceWriter implements IInvoiceWriter <HtmlRenderContext>
{
  public HtmlInvoiceWriter ()
  {}

  /**
   * Render the invoice and write it as UTF-8 HTML5 to the given stream. The stream is closed
   * by the writer.
   *
   * @param aInvoice
   *        The invoice.
   * @param aCtx
   *        The HTML render context.
   * @param aOut
   *        The output stream.
   */
  @Override
  public void writeTo (@NonNull final PhloxInvoice aInvoice,
                       @NonNull final HtmlRenderContext aCtx,
                       @NonNull final OutputStream aOut)
  {
    final String sHtml = writeAsString (aInvoice, aCtx);
    try (final OutputStream aClosing = aOut)
    {
      aClosing.write (sHtml.getBytes (StandardCharsets.UTF_8));
    }
    catch (final IOException ex)
    {
      throw new UncheckedIOException (ex);
    }
  }

  /**
   * Render the invoice and return the HTML markup as a {@code String}. Equivalent to
   * {@link #writeTo(PhloxInvoice, HtmlRenderContext, OutputStream)} but keeps the result in
   * memory.
   *
   * @param aInvoice
   *        The invoice.
   * @param aCtx
   *        The HTML render context.
   * @return The HTML5 document as a {@code String}.
   */
  @NonNull
  public String writeAsString (@NonNull final PhloxInvoice aInvoice, @NonNull final HtmlRenderContext aCtx)
  {
    return HCRenderer.getAsHTMLString (writeAsHcHtml (aInvoice, aCtx));
  }

  /**
   * Build the HTML tree as an {@link HCHtml} object without serializing.
   *
   * @param aInvoice
   *        The invoice to render.
   * @param aCtx
   *        The context.
   * @return The populated {@link HCHtml}.
   */
  @NonNull
  public HCHtml writeAsHcHtml (@NonNull final PhloxInvoice aInvoice, @NonNull final HtmlRenderContext aCtx)
  {
    final HCHtml aHtml = new HCHtml ();
    final String sPrefix = aCtx.getCssClassPrefix ();

    // <head>
    aHtml.head ().setPageTitle (_titleString (aInvoice, aCtx));
    if (aCtx.isEmbeddedStylesheet ())
    {
      final HCStyle aStyle = new HCStyle ();
      aStyle.setStyleContent (_defaultCss (sPrefix) +
                              (aCtx.getCustomStylesheet () == null ? "" : "\n" + aCtx.getCustomStylesheet ()));
      aHtml.head ().addCSS (aStyle);
    }

    // <body>
    final HCBody aBody = aHtml.body ();

    // Watermarks
    final WatermarkSpec aUW = aCtx.getUserWatermark ();
    if (aUW != null)
      aBody.addChild (_renderWatermark (aUW, sPrefix + "watermark-user"));
    final WatermarkSpec aPW = PhloxProgramWatermark.getInstance ();
    if (aPW != null)
      aBody.addChild (_renderWatermark (aPW, sPrefix + "watermark-program"));

    // Page wrapper
    final HCDiv aPage = new HCDiv ().addClass (_cls (sPrefix + "page"));
    aBody.addChild (aPage);

    // Sections
    _renderHeader (aPage, aInvoice, aCtx);
    _renderProcessControl (aPage, aInvoice, aCtx);
    _renderParties (aPage, aInvoice, aCtx);
    _renderOptionalParties (aPage, aInvoice, aCtx);
    _renderReferences (aPage, aInvoice, aCtx);
    _renderInvoiceNotes (aPage, aInvoice, aCtx);
    _renderDelivery (aPage, aInvoice, aCtx);
    _renderInvoicingPeriod (aPage, aInvoice, aCtx);
    _renderLines (aPage, aInvoice, aCtx);
    _renderAllowancesCharges (aPage, aInvoice, aCtx);
    _renderVatBreakdown (aPage, aInvoice, aCtx);
    _renderTotals (aPage, aInvoice, aCtx);
    _renderPayment (aPage, aInvoice, aCtx);
    _renderPaymentTerms (aPage, aInvoice, aCtx);
    _renderSupportingDocuments (aPage, aInvoice, aCtx);

    return aHtml;
  }

  // ===== Sections =====

  private static void _renderHeader (@NonNull final HCDiv aPage,
                                     @NonNull final PhloxInvoice aInvoice,
                                     @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aHdr = new HCDiv ().addClass (_cls (sPrefix + "section-header"));
    aPage.addChild (aHdr);

    if (aCtx.getLogo () != null)
    {
      final HCImg aImg = _logoImg (aCtx.getLogo (), sPrefix);
      if (aImg != null)
        aHdr.addChild (aImg);
    }

    final HCH1 aTitle = new HCH1 ().addClass (_cls (sPrefix + "title"));
    aTitle.addChild (_titleString (aInvoice, aCtx));
    aHdr.addChild (aTitle);

    final HCDiv aMeta = new HCDiv ().addClass (_cls (sPrefix + "header-meta"));
    _kv (aMeta, aCtx.label ("BT-1"), aInvoice.getNumber (), sPrefix);
    _kv (aMeta, aCtx.label ("BT-2"), _date (aInvoice.getIssueDate (), aCtx), sPrefix);
    _kv (aMeta, aCtx.label ("BT-3"), _docTypeText (aInvoice.getTypeCode (), aCtx), sPrefix);
    _kv (aMeta, aCtx.label ("BT-5"), aInvoice.getCurrency (), sPrefix);
    aHdr.addChild (aMeta);
  }

  private static void _renderProcessControl (@NonNull final HCDiv aPage,
                                             @NonNull final PhloxInvoice aInvoice,
                                             @NonNull final HtmlRenderContext aCtx)
  {
    final String sBP = aInvoice.getProcessControl ().getBusinessProcessType ();
    final String sSpec = aInvoice.getProcessControl ().getSpecificationIdentifier ();
    if (sBP == null && sSpec == null && !aCtx.isIncludeEmptySections ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-process"));
    aPage.addChild (aSec);
    final HCDiv aRow = new HCDiv ().addClass (_cls (sPrefix + "kvrow"));
    _kv (aRow, aCtx.label ("BT-23"), sBP, sPrefix);
    _kv (aRow, aCtx.label ("BT-24"), sSpec, sPrefix);
    aSec.addChild (aRow);
  }

  private static void _renderParties (@NonNull final HCDiv aPage,
                                      @NonNull final PhloxInvoice aInvoice,
                                      @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-parties"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.parties.title"), sPrefix);

    final HCDiv aGrid = new HCDiv ().addClass (_cls (sPrefix + "parties-grid"));
    aSec.addChild (aGrid);

    // Seller
    final HCDiv aSeller = new HCDiv ().addClass (_cls (sPrefix + "party-seller"));
    aSeller.addChild (new HCH2 ().addClass (_cls (sPrefix + "party-title")).addChild (aCtx.label ("BG-4")));
    _renderSeller (aSeller, aInvoice.getSeller (), aCtx);
    aGrid.addChild (aSeller);

    // Buyer
    final HCDiv aBuyer = new HCDiv ().addClass (_cls (sPrefix + "party-buyer"));
    aBuyer.addChild (new HCH2 ().addClass (_cls (sPrefix + "party-title")).addChild (aCtx.label ("BG-7")));
    _renderBuyer (aBuyer, aInvoice.getBuyer (), aCtx);
    aGrid.addChild (aBuyer);
  }

  private static void _renderSeller (@NonNull final HCDiv aOut,
                                     @NonNull final PhloxSeller aS,
                                     @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    _line (aOut, aS.getName (), sPrefix + "party-name");
    _line (aOut, aS.getTradingName (), sPrefix + "party-trading-name");
    _renderAddress (aOut, aS.getPostalAddress (), aCtx);
    if (aS.getVatIdentifier () != null)
      _kvLine (aOut, aCtx.label ("BT-31"), aS.getVatIdentifier (), sPrefix);
    if (aS.getTaxRegistration () != null)
      _kvLine (aOut, aCtx.label ("BT-32"), aS.getTaxRegistration (), sPrefix);
    if (aS.getLegalRegistration () != null && aS.getLegalRegistration ().getValue () != null)
      _kvLine (aOut, aCtx.label ("BT-30"), aS.getLegalRegistration ().getValue (), sPrefix);
    if (aS.getElectronicAddress () != null && aS.getElectronicAddress ().getValue () != null)
      _kvLine (aOut, aCtx.label ("BT-34"), aS.getElectronicAddress ().getValue (), sPrefix);
    if (aS.getAdditionalLegalInfo () != null)
      _line (aOut, aS.getAdditionalLegalInfo (), sPrefix + "legal-info");
    _renderContact (aOut, aS.getContact (), aCtx);
  }

  private static void _renderBuyer (@NonNull final HCDiv aOut,
                                    @NonNull final PhloxBuyer aB,
                                    @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    _line (aOut, aB.getName (), sPrefix + "party-name");
    _line (aOut, aB.getTradingName (), sPrefix + "party-trading-name");
    _renderAddress (aOut, aB.getPostalAddress (), aCtx);
    if (aB.getVatIdentifier () != null)
      _kvLine (aOut, aCtx.label ("BT-48"), aB.getVatIdentifier (), sPrefix);
    if (aB.getLegalRegistration () != null && aB.getLegalRegistration ().getValue () != null)
      _kvLine (aOut, aCtx.label ("BT-30"), aB.getLegalRegistration ().getValue (), sPrefix);
    if (aB.getElectronicAddress () != null && aB.getElectronicAddress ().getValue () != null)
      _kvLine (aOut, aCtx.label ("BT-34"), aB.getElectronicAddress ().getValue (), sPrefix);
    _renderContact (aOut, aB.getContact (), aCtx);
  }

  private static void _renderAddress (@NonNull final HCDiv aOut,
                                      @Nullable final PhloxPostalAddress aAddr,
                                      @NonNull final HtmlRenderContext aCtx)
  {
    if (aAddr == null || aAddr.isEmpty ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aA = new HCDiv ().addClass (_cls (sPrefix + "address"));
    _line (aA, aAddr.getLine1 (), null);
    _line (aA, aAddr.getLine2 (), null);
    _line (aA, aAddr.getLine3 (), null);
    final StringBuilder aLast = new StringBuilder ();
    if (aAddr.getPostCode () != null)
      aLast.append (aAddr.getPostCode ()).append (' ');
    if (aAddr.getCity () != null)
      aLast.append (aAddr.getCity ());
    if (aLast.length () > 0)
      aA.addChild (new HCDiv ().addChild (aLast.toString ()));
    if (aAddr.getCountrySubdivision () != null)
      aA.addChild (new HCDiv ().addChild (aAddr.getCountrySubdivision ()));
    if (aAddr.getCountryCode () != null)
      aA.addChild (new HCDiv ().addChild (aAddr.getCountryCode ()));
    aOut.addChild (aA);
  }

  private static void _renderContact (@NonNull final HCDiv aOut,
                                      @Nullable final PhloxContact aC,
                                      @NonNull final HtmlRenderContext aCtx)
  {
    if (aC == null || aC.isEmpty ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aDiv = new HCDiv ().addClass (_cls (sPrefix + "contact"));
    if (aC.getName () != null)
      _kvLine (aDiv, aCtx.label ("BT-41"), aC.getName (), sPrefix);
    if (aC.getTelephone () != null)
      _kvLine (aDiv, aCtx.label ("BT-42"), aC.getTelephone (), sPrefix);
    if (aC.getEmail () != null)
      _kvLine (aDiv, aCtx.label ("BT-43"), aC.getEmail (), sPrefix);
    aOut.addChild (aDiv);
  }

  private static void _renderOptionalParties (@NonNull final HCDiv aPage,
                                              @NonNull final PhloxInvoice aInvoice,
                                              @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    if (aInvoice.getPayee () != null)
    {
      final PhloxPayee aPayee = aInvoice.getPayee ();
      final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-payee"));
      aPage.addChild (aSec);
      _sectionTitle (aSec, aCtx.label ("BG-10"), sPrefix);
      _line (aSec, aPayee.getName (), sPrefix + "party-name");
      if (aPayee.getIdentifier () != null && aPayee.getIdentifier ().getValue () != null)
        _kvLine (aSec, aCtx.label ("BT-60"), aPayee.getIdentifier ().getValue (), sPrefix);
      if (aPayee.getLegalRegistration () != null && aPayee.getLegalRegistration ().getValue () != null)
        _kvLine (aSec, aCtx.label ("BT-30"), aPayee.getLegalRegistration ().getValue (), sPrefix);
    }

    if (aInvoice.getSellerTaxRepresentative () != null)
    {
      final PhloxSellerTaxRepresentative aR = aInvoice.getSellerTaxRepresentative ();
      final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-taxrep"));
      aPage.addChild (aSec);
      _sectionTitle (aSec, aCtx.label ("BG-11"), sPrefix);
      _line (aSec, aR.getName (), sPrefix + "party-name");
      if (aR.getVatIdentifier () != null)
        _kvLine (aSec, aCtx.label ("BT-31"), aR.getVatIdentifier (), sPrefix);
      _renderAddress (aSec, aR.getPostalAddress (), aCtx);
    }
  }

  private static void _renderReferences (@NonNull final HCDiv aPage,
                                         @NonNull final PhloxInvoice aInvoice,
                                         @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-refs"));
    boolean bAny = false;
    final HCDiv aGrid = new HCDiv ().addClass (_cls (sPrefix + "kvgrid"));
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-10"), aInvoice.getBuyerReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-11"), aInvoice.getProjectReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-12"), aInvoice.getContractReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-13"), aInvoice.getPurchaseOrderReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-14"), aInvoice.getSalesOrderReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-15"), aInvoice.getReceivingAdviceReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-16"), aInvoice.getDespatchAdviceReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-17"), aInvoice.getTenderOrLotReference (), sPrefix);
    bAny |= _kvOpt (aGrid, aCtx.label ("BT-19"), aInvoice.getBuyerAccountingReference (), sPrefix);
    if (aInvoice.getInvoicedObjectIdentifier () != null &&
        aInvoice.getInvoicedObjectIdentifier ().getValue () != null)
    {
      bAny = true;
      _kv (aGrid, aCtx.label ("BT-18"), aInvoice.getInvoicedObjectIdentifier ().getValue (), sPrefix);
    }
    if (!bAny && aInvoice.precedingInvoiceRefs ().isEmpty () && !aCtx.isIncludeEmptySections ())
      return;
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.references.title"), sPrefix);
    if (bAny)
      aSec.addChild (aGrid);
    for (final PhloxPrecedingInvoiceRef aRef : aInvoice.precedingInvoiceRefs ())
    {
      final HCDiv aLine = new HCDiv ().addClass (_cls (sPrefix + "preceding-ref"));
      final StringBuilder sb = new StringBuilder (aCtx.label ("BG-3")).append (": ");
      if (aRef.getNumber () != null)
        sb.append (aRef.getNumber ());
      if (aRef.getIssueDate () != null)
        sb.append (" (").append (_date (aRef.getIssueDate (), aCtx)).append (")");
      aLine.addChild (sb.toString ());
      aSec.addChild (aLine);
    }
  }

  private static void _renderInvoiceNotes (@NonNull final HCDiv aPage,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.invoiceNotes ().isEmpty () && !aCtx.isIncludeEmptySections ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-notes"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.notes.title"), sPrefix);
    for (final PhloxInvoiceNote aN : aInvoice.invoiceNotes ())
    {
      final HCP aP = new HCP ().addClass (_cls (sPrefix + "note"));
      if (aN.getSubjectCode () != null)
        aP.addChild (new HCSpan ().addClass (_cls (sPrefix + "note-code"))
                                  .addChild ("[" + aN.getSubjectCode () + "] "));
      aP.addChild (aN.getContent ());
      aSec.addChild (aP);
    }
  }

  private static void _renderDelivery (@NonNull final HCDiv aPage,
                                       @NonNull final PhloxInvoice aInvoice,
                                       @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.getDeliveryInformation () == null)
      return;
    final PhloxDeliveryInformation aD = aInvoice.getDeliveryInformation ();
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-delivery"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.delivery.title"), sPrefix);
    if (aD.getPartyName () != null)
      _kvLine (aSec, aCtx.label ("BG-13"), aD.getPartyName (), sPrefix);
    if (aD.getActualDeliveryDate () != null)
      _kvLine (aSec, aCtx.label ("BT-72"), _date (aD.getActualDeliveryDate (), aCtx), sPrefix);
    _renderAddress (aSec, aD.getPostalAddress (), aCtx);
  }

  private static void _renderInvoicingPeriod (@NonNull final HCDiv aPage,
                                              @NonNull final PhloxInvoice aInvoice,
                                              @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.getInvoicingPeriod () == null || aInvoice.getInvoicingPeriod ().isEmpty ())
      return;
    final PhloxInvoicingPeriod aP = aInvoice.getInvoicingPeriod ();
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-period"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("BG-14"), sPrefix);
    if (aP.getStartDate () != null)
      _kvLine (aSec, aCtx.label ("BT-73"), _date (aP.getStartDate (), aCtx), sPrefix);
    if (aP.getEndDate () != null)
      _kvLine (aSec, aCtx.label ("BT-74"), _date (aP.getEndDate (), aCtx), sPrefix);
  }

  private static void _renderLines (@NonNull final HCDiv aPage,
                                    @NonNull final PhloxInvoice aInvoice,
                                    @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.lines ().isEmpty () && !aCtx.isIncludeEmptySections ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final String sCurrency = aInvoice.getCurrency ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-lines"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.lines.title"), sPrefix);
    final HCTable aTbl = new HCTable ().addClass (_cls (sPrefix + "lines-table"));
    aSec.addChild (aTbl);
    final HCRow aHead = aTbl.getHead ().addRow ();
    aHead.addCell (aCtx.label ("section.lines.column.number"));
    aHead.addCell (aCtx.label ("section.lines.column.description"));
    aHead.addCell (aCtx.label ("section.lines.column.quantity"));
    aHead.addCell (aCtx.label ("section.lines.column.unitprice"));
    aHead.addCell (aCtx.label ("section.lines.column.vat"));
    aHead.addCell (aCtx.label ("section.lines.column.linetotal"));
    for (final PhloxInvoiceLine aLine : aInvoice.lines ())
    {
      final HCRow aRow = aTbl.getBody ().addRow ();
      aRow.addCell (_n (aLine.getID ()));
      final IHCCell <?> aDesc = aRow.addCell ();
      final PhloxItem aItem = aLine.getItem ();
      if (aItem != null)
      {
        if (aItem.getName () != null)
          aDesc.addChild (new HCDiv ().addClass (_cls (sPrefix + "item-name")).addChild (aItem.getName ()));
        if (aItem.getDescription () != null)
          aDesc.addChild (new HCDiv ().addClass (_cls (sPrefix + "item-desc")).addChild (aItem.getDescription ()));
        for (final PhloxItemAttribute aA : aItem.attributes ())
          if (aA.getName () != null || aA.getValue () != null)
            aDesc.addChild (new HCDiv ().addClass (_cls (sPrefix + "item-attr"))
                                        .addChild (_n (aA.getName ()) + ": " + _n (aA.getValue ())));
      }
      if (aLine.getNote () != null)
        aDesc.addChild (new HCDiv ().addClass (_cls (sPrefix + "line-note")).addChild (aLine.getNote ()));
      aRow.addCell (_qty (aLine.getQuantity (), aLine.getQuantityUnit ()));
      aRow.addCell (aLine.getPrice () == null ? "" : _amount (aLine.getPrice ().getNetPrice (), sCurrency, aCtx));
      aRow.addCell (aLine.getLineVat () == null ? "" : _lineVatCell (aLine, aCtx));
      aRow.addCell (_amount (aLine.getNetAmount (), sCurrency, aCtx));
      // Allowances / charges shown as sub-row content
      for (final PhloxLineAllowance aA : aLine.allowances ())
      {
        final HCRow aSub = aTbl.getBody ().addRow ();
        aSub.addClass (_cls (sPrefix + "line-allowance"));
        aSub.addCell ("");
        aSub.addCell (_n (aCtx.label ("BG-27")) + (aA.getReason () == null ? "" : ": " + aA.getReason ()));
        aSub.addCell ("");
        aSub.addCell ("");
        aSub.addCell ("");
        aSub.addCell (_amount (_neg (aA.getAmount ()), sCurrency, aCtx));
      }
      for (final PhloxLineCharge aC : aLine.charges ())
      {
        final HCRow aSub = aTbl.getBody ().addRow ();
        aSub.addClass (_cls (sPrefix + "line-charge"));
        aSub.addCell ("");
        aSub.addCell (_n (aCtx.label ("BG-28")) + (aC.getReason () == null ? "" : ": " + aC.getReason ()));
        aSub.addCell ("");
        aSub.addCell ("");
        aSub.addCell ("");
        aSub.addCell (_amount (aC.getAmount (), sCurrency, aCtx));
      }
    }
  }

  private static String _lineVatCell (@NonNull final PhloxInvoiceLine aLine, @NonNull final HtmlRenderContext aCtx)
  {
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

  private static void _renderAllowancesCharges (@NonNull final HCDiv aPage,
                                                @NonNull final PhloxInvoice aInvoice,
                                                @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.documentAllowances ().isEmpty () && aInvoice.documentCharges ().isEmpty ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final String sCurrency = aInvoice.getCurrency ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-ac"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.allowancescharges.title"), sPrefix);
    final HCTable aTbl = new HCTable ().addClass (_cls (sPrefix + "ac-table"));
    aSec.addChild (aTbl);
    final HCRow aH = aTbl.getHead ().addRow ();
    aH.addCell ("");
    aH.addCell (aCtx.label ("section.lines.column.description"));
    aH.addCell (aCtx.label ("BT-152"));
    aH.addCell (aCtx.label ("section.lines.column.linetotal"));
    for (final PhloxDocumentAllowance aA : aInvoice.documentAllowances ())
    {
      final HCRow aR = aTbl.getBody ().addRow ();
      aR.addCell (aCtx.label ("BG-20"));
      aR.addCell (_n (aA.getReason ()));
      aR.addCell (_n (aA.getVatCategoryCode ()) +
                  (aA.getVatRate () == null
                                            ? ""
                                            : " " +
                                              PhloxFormat.percent (aA.getVatRate (),
                                                                   aCtx.getLocale (),
                                                                   aCtx.getFormatOptions ())));
      aR.addCell (_amount (_neg (aA.getAmount ()), sCurrency, aCtx));
    }
    for (final PhloxDocumentCharge aC : aInvoice.documentCharges ())
    {
      final HCRow aR = aTbl.getBody ().addRow ();
      aR.addCell (aCtx.label ("BG-21"));
      aR.addCell (_n (aC.getReason ()));
      aR.addCell (_n (aC.getVatCategoryCode ()) +
                  (aC.getVatRate () == null
                                            ? ""
                                            : " " +
                                              PhloxFormat.percent (aC.getVatRate (),
                                                                   aCtx.getLocale (),
                                                                   aCtx.getFormatOptions ())));
      aR.addCell (_amount (aC.getAmount (), sCurrency, aCtx));
    }
  }

  private static void _renderVatBreakdown (@NonNull final HCDiv aPage,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.vatBreakdowns ().isEmpty () && !aCtx.isIncludeEmptySections ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final String sCurrency = aInvoice.getCurrency ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-vat"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.vatbreakdown.title"), sPrefix);
    final HCTable aTbl = new HCTable ().addClass (_cls (sPrefix + "vat-table"));
    aSec.addChild (aTbl);
    final HCRow aH = aTbl.getHead ().addRow ();
    aH.addCell (aCtx.label ("BT-151"));
    aH.addCell (aCtx.label ("BT-152"));
    aH.addCell (aCtx.label ("BT-116"));
    aH.addCell (aCtx.label ("BT-117"));
    for (final PhloxVatBreakdown aV : aInvoice.vatBreakdowns ())
    {
      final HCRow aR = aTbl.getBody ().addRow ();
      final String sCat = aV.getCategoryCode ();
      aR.addCell (sCat == null ? "" : sCat + " — " + aCtx.codeLabel ("vat.category", sCat));
      aR.addCell (aV.getRate () == null
                                        ? ""
                                        : PhloxFormat.percent (aV.getRate (),
                                                               aCtx.getLocale (),
                                                               aCtx.getFormatOptions ()));
      aR.addCell (_amount (aV.getTaxableAmount (), sCurrency, aCtx));
      aR.addCell (_amount (aV.getTaxAmount (), sCurrency, aCtx));
    }
  }

  private static void _renderTotals (@NonNull final HCDiv aPage,
                                     @NonNull final PhloxInvoice aInvoice,
                                     @NonNull final HtmlRenderContext aCtx)
  {
    final String sPrefix = aCtx.getCssClassPrefix ();
    final String sCurrency = aInvoice.getCurrency ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-totals"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.totals.title"), sPrefix);
    final HCDiv aBox = new HCDiv ().addClass (_cls (sPrefix + "totals-ladder"));
    aSec.addChild (aBox);
    _totalLine (aBox, aCtx.label ("BT-106"), aInvoice.getDocumentTotals ().getLineNetTotal (), sCurrency, aCtx);
    _totalLine (aBox,
                aCtx.label ("BT-107"),
                _neg (aInvoice.getDocumentTotals ().getAllowanceTotal ()),
                sCurrency,
                aCtx);
    _totalLine (aBox, aCtx.label ("BT-108"), aInvoice.getDocumentTotals ().getChargeTotal (), sCurrency, aCtx);
    _totalLine (aBox, aCtx.label ("BT-109"), aInvoice.getDocumentTotals ().getTaxExclusiveTotal (), sCurrency, aCtx);
    _totalLine (aBox, aCtx.label ("BT-110"), aInvoice.getDocumentTotals ().getTaxTotal (), sCurrency, aCtx);
    _totalLine (aBox, aCtx.label ("BT-112"), aInvoice.getDocumentTotals ().getTaxInclusiveTotal (), sCurrency, aCtx);
    _totalLine (aBox,
                aCtx.label ("BT-113"),
                _neg (aInvoice.getDocumentTotals ().getPrepaidAmount ()),
                sCurrency,
                aCtx);
    _totalLine (aBox, aCtx.label ("BT-114"), aInvoice.getDocumentTotals ().getRoundingAmount (), sCurrency, aCtx);
    _totalLine (aBox.addClass (_cls (sPrefix + "totals-due")),
                aCtx.label ("BT-115"),
                aInvoice.getDocumentTotals ().getPayableAmount (),
                sCurrency,
                aCtx);
  }

  private static void _totalLine (@NonNull final HCDiv aOut,
                                  @NonNull final String sLabel,
                                  @Nullable final BigDecimal aAmount,
                                  @Nullable final String sCurrency,
                                  @NonNull final HtmlRenderContext aCtx)
  {
    if (aAmount == null)
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aRow = new HCDiv ().addClass (_cls (sPrefix + "total-row"));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "total-label")).addChild (sLabel));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "total-amount")).addChild (_amount (aAmount, sCurrency, aCtx)));
    aOut.addChild (aRow);
  }

  private static void _renderPayment (@NonNull final HCDiv aPage,
                                      @NonNull final PhloxInvoice aInvoice,
                                      @NonNull final HtmlRenderContext aCtx)
  {
    final PhloxPaymentInstructions aPI = aInvoice.getPaymentInstructions ();
    if (aPI == null && aInvoice.getPaymentDueDate () == null && !aCtx.isIncludeEmptySections ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-payment"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.payment.title"), sPrefix);
    if (aInvoice.getPaymentDueDate () != null)
      _kvLine (aSec, aCtx.label ("BT-9"), _date (aInvoice.getPaymentDueDate (), aCtx), sPrefix);
    if (aPI != null)
    {
      if (aPI.getPaymentMeansCode () != null)
        _kvLine (aSec,
                 aCtx.label ("BT-81"),
                 aPI.getPaymentMeansCode () + " — " + aCtx.codeLabel ("paymentmeans", aPI.getPaymentMeansCode ()),
                 sPrefix);
      if (aPI.getPaymentMeansText () != null)
        _line (aSec, aPI.getPaymentMeansText (), sPrefix + "payment-text");
      if (aPI.getRemittanceInformation () != null)
        _kvLine (aSec, aCtx.label ("BT-83"), aPI.getRemittanceInformation (), sPrefix);
      for (final PhloxCreditTransfer aCT : aPI.creditTransfers ())
      {
        final HCDiv aCTDiv = new HCDiv ().addClass (_cls (sPrefix + "credit-transfer"));
        if (aCT.getAccountIdentifier () != null)
          _kvLine (aCTDiv, aCtx.label ("BT-84"), aCT.getAccountIdentifier (), sPrefix);
        if (aCT.getAccountName () != null)
          _kvLine (aCTDiv, aCtx.label ("BT-85"), aCT.getAccountName (), sPrefix);
        if (aCT.getServiceProviderID () != null)
          _kvLine (aCTDiv, aCtx.label ("BT-86"), aCT.getServiceProviderID (), sPrefix);
        aSec.addChild (aCTDiv);
      }
      if (aPI.getPaymentCard () != null)
      {
        final PhloxPaymentCard aPC = aPI.getPaymentCard ();
        final HCDiv aCDiv = new HCDiv ().addClass (_cls (sPrefix + "payment-card"));
        aCDiv.addChild (new HCDiv ().addChild (aCtx.label ("BG-18")));
        if (aPC.getPan () != null)
          _kvLine (aCDiv, aCtx.label ("BT-87"), aPC.getPan (), sPrefix);
        if (aPC.getHolderName () != null)
          _kvLine (aCDiv, aCtx.label ("BT-88"), aPC.getHolderName (), sPrefix);
        aSec.addChild (aCDiv);
      }
      if (aPI.getDirectDebit () != null)
      {
        final PhloxDirectDebit aDD = aPI.getDirectDebit ();
        final HCDiv aDDDiv = new HCDiv ().addClass (_cls (sPrefix + "direct-debit"));
        aDDDiv.addChild (new HCDiv ().addChild (aCtx.label ("BG-19")));
        if (aDD.getMandateReferenceID () != null)
          _kvLine (aDDDiv, aCtx.label ("BT-89"), aDD.getMandateReferenceID (), sPrefix);
        if (aDD.getBankAssignedCreditorID () != null)
          _kvLine (aDDDiv, aCtx.label ("BT-90"), aDD.getBankAssignedCreditorID (), sPrefix);
        if (aDD.getDebitedAccountID () != null)
          _kvLine (aDDDiv, aCtx.label ("BT-91"), aDD.getDebitedAccountID (), sPrefix);
        aSec.addChild (aDDDiv);
      }
    }
  }

  private static void _renderPaymentTerms (@NonNull final HCDiv aPage,
                                           @NonNull final PhloxInvoice aInvoice,
                                           @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.getPaymentTerms () == null)
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-paymentterms"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("BT-20"), sPrefix);
    aSec.addChild (new HCP ().addChild (aInvoice.getPaymentTerms ()));
  }

  private static void _renderSupportingDocuments (@NonNull final HCDiv aPage,
                                                  @NonNull final PhloxInvoice aInvoice,
                                                  @NonNull final HtmlRenderContext aCtx)
  {
    if (aInvoice.supportingDocuments ().isEmpty ())
      return;
    final String sPrefix = aCtx.getCssClassPrefix ();
    final HCDiv aSec = new HCDiv ().addClass (_cls (sPrefix + "section-supportingdocs"));
    aPage.addChild (aSec);
    _sectionTitle (aSec, aCtx.label ("section.supportingdocs.title"), sPrefix);
    for (final PhloxSupportingDocument aD : aInvoice.supportingDocuments ())
    {
      final HCDiv aDDiv = new HCDiv ().addClass (_cls (sPrefix + "supporting-doc"));
      if (aD.getReference () != null)
        _kvLine (aDDiv, aCtx.label ("BT-122"), aD.getReference (), sPrefix);
      if (aD.getDescription () != null)
        _kvLine (aDDiv, aCtx.label ("BT-123"), aD.getDescription (), sPrefix);
      if (aD.getExternalLocation () != null)
        _kvLine (aDDiv, aCtx.label ("BT-124"), aD.getExternalLocation (), sPrefix);
      aSec.addChild (aDDiv);
    }
  }

  // ===== Helpers =====

  @NonNull
  private static ICSSClassProvider _cls (@NonNull final String sName)
  {
    return DefaultCSSClassProvider.create (sName);
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

  @NonNull
  private static String _titleString (@NonNull final PhloxInvoice aInvoice, @NonNull final HtmlRenderContext aCtx)
  {
    return aInvoice.getKind () == EPhloxDocumentKind.CREDIT_NOTE
                                                                ? aCtx.label ("section.header.title.creditnote")
                                                                : aCtx.label ("section.header.title.invoice");
  }

  @NonNull
  private static String _date (@Nullable final LocalDate aDate, @NonNull final HtmlRenderContext aCtx)
  {
    return PhloxFormat.date (aDate, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @NonNull
  private static String _amount (@Nullable final BigDecimal aAmount,
                                 @Nullable final String sCurrency,
                                 @NonNull final HtmlRenderContext aCtx)
  {
    return PhloxFormat.amount (aAmount, sCurrency, aCtx.getLocale (), aCtx.getFormatOptions ());
  }

  @NonNull
  private static String _qty (@Nullable final BigDecimal aQty, @Nullable final String sUnit)
  {
    if (aQty == null)
      return "";
    return aQty.stripTrailingZeros ().toPlainString () + (sUnit == null ? "" : " " + sUnit);
  }

  @NonNull
  private static String _docTypeText (@Nullable final String sCode, @NonNull final HtmlRenderContext aCtx)
  {
    if (sCode == null)
      return "";
    return sCode + " — " + aCtx.codeLabel ("doctype", sCode);
  }

  private static void _kv (@NonNull final HCDiv aOut,
                           @NonNull final String sLabel,
                           @Nullable final String sValue,
                           @NonNull final String sPrefix)
  {
    if (sValue == null || sValue.isEmpty ())
      return;
    final HCDiv aRow = new HCDiv ().addClass (_cls (sPrefix + "kv"));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "kv-label")).addChild (sLabel));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "kv-value")).addChild (sValue));
    aOut.addChild (aRow);
  }

  private static boolean _kvOpt (@NonNull final HCDiv aOut,
                                 @NonNull final String sLabel,
                                 @Nullable final String sValue,
                                 @NonNull final String sPrefix)
  {
    if (sValue == null || sValue.isEmpty ())
      return false;
    _kv (aOut, sLabel, sValue, sPrefix);
    return true;
  }

  private static void _kvLine (@NonNull final HCDiv aOut,
                               @NonNull final String sLabel,
                               @Nullable final String sValue,
                               @NonNull final String sPrefix)
  {
    if (sValue == null || sValue.isEmpty ())
      return;
    final HCDiv aRow = new HCDiv ().addClass (_cls (sPrefix + "kv-line"));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "kv-label")).addChild (sLabel + ": "));
    aRow.addChild (new HCSpan ().addClass (_cls (sPrefix + "kv-value")).addChild (sValue));
    aOut.addChild (aRow);
  }

  private static void _line (@NonNull final HCDiv aOut, @Nullable final String sText, @Nullable final String sClass)
  {
    if (sText == null || sText.isEmpty ())
      return;
    final HCDiv aD = new HCDiv ().addChild (sText);
    if (sClass != null)
      aD.addClass (_cls (sClass));
    aOut.addChild (aD);
  }

  private static void _sectionTitle (@NonNull final HCDiv aOut,
                                     @NonNull final String sTitle,
                                     @NonNull final String sPrefix)
  {
    aOut.addChild (new HCH2 ().addClass (_cls (sPrefix + "section-title")).addChild (sTitle));
  }

  @NonNull
  private static HCDiv _renderWatermark (@NonNull final WatermarkSpec aSpec, @NonNull final String sCssClass)
  {
    final HCDiv aD = new HCDiv ().addClass (_cls (sCssClass));
    aD.addChild (aSpec.getText ());
    aD.customAttrs ().putIn ("aria-hidden", "true");
    return aD;
  }

  @Nullable
  private static HCImg _logoImg (@NonNull final PhloxAttachment aLogo, @NonNull final String sPrefix)
  {
    if (aLogo.getBinary () == null || aLogo.getMimeCode () == null)
      return null;
    final String sDataUri = "data:" +
                            aLogo.getMimeCode () +
                            ";base64," +
                            Base64.getEncoder ().encodeToString (aLogo.getBinary ());
    return new HCImg ().setSrc (new SimpleURL (sDataUri)).addClass (_cls (sPrefix + "logo"));
  }

  /** Embedded default stylesheet — minimal, prefix-aware. */
  @NonNull
  private static String _defaultCss (@NonNull final String p)
  {
    return "body{font-family:sans-serif;margin:2rem;color:#222;}"
           + "." + p + "page{max-width:840px;margin:0 auto;}"
           + "." + p + "section-header{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:1rem;}"
           + "." + p + "title{margin:0 0 .5rem 0;font-size:1.6rem;}"
           + "." + p + "logo{max-height:80px;}"
           + "." + p + "header-meta{display:grid;grid-template-columns:auto auto;gap:.25rem 1rem;}"
           + "." + p + "section-title{font-size:1rem;margin:1.5rem 0 .5rem 0;border-bottom:1px solid #ccc;padding-bottom:.2rem;}"
           + "." + p + "parties-grid{display:grid;grid-template-columns:1fr 1fr;gap:1.5rem;}"
           + "." + p + "party-title{font-size:.9rem;color:#555;margin:0 0 .25rem 0;text-transform:uppercase;}"
           + "." + p + "party-name{font-weight:bold;}"
           + "." + p + "address div{line-height:1.4;}"
           + "." + p + "contact{margin-top:.5rem;font-size:.9rem;color:#444;}"
           + "." + p + "kv-label{color:#666;margin-right:.5rem;}"
           + "." + p + "kvgrid{display:grid;grid-template-columns:auto 1fr;column-gap:1rem;row-gap:.2rem;}"
           + "." + p + "lines-table,." + p + "vat-table,." + p + "ac-table{width:100%;border-collapse:collapse;margin-top:.5rem;}"
           + "." + p + "lines-table th,." + p + "vat-table th,." + p + "ac-table th{background:#f5f5f5;text-align:left;padding:.4rem .5rem;border-bottom:1px solid #ccc;}"
           + "." + p + "lines-table td,." + p + "vat-table td,." + p + "ac-table td{padding:.4rem .5rem;border-bottom:1px solid #eee;vertical-align:top;}"
           // Right-align numeric columns in each table, applies to both <th> and <td>:
           //   lines-table cols 3..6 = quantity, unit price, VAT, line total
           //   ac-table cols 3..4 = VAT, amount
           //   vat-table cols 2..4 = rate, taxable amount, tax amount
           + "." + p + "lines-table th:nth-child(n+3),." + p + "lines-table td:nth-child(n+3){text-align:right;font-variant-numeric:tabular-nums;}"
           + "." + p + "ac-table th:nth-child(n+3),." + p + "ac-table td:nth-child(n+3){text-align:right;font-variant-numeric:tabular-nums;}"
           + "." + p + "vat-table th:nth-child(n+2),." + p + "vat-table td:nth-child(n+2){text-align:right;font-variant-numeric:tabular-nums;}"
           + "." + p + "item-name{font-weight:bold;}"
           + "." + p + "item-desc,." + p + "item-attr,." + p + "line-note{color:#555;font-size:.9rem;}"
           + "." + p + "totals-ladder{display:grid;grid-template-columns:1fr auto;row-gap:.25rem;max-width:480px;margin-left:auto;}"
           + "." + p + "total-row{display:contents;}"
           + "." + p + "total-label{padding-right:1rem;}"
           + "." + p + "total-amount{text-align:right;font-variant-numeric:tabular-nums;}"
           + "." + p + "totals-due ." + p + "total-row:last-child ." + p + "total-amount{font-weight:bold;border-top:2px solid #000;padding-top:.25rem;}"
           + "." + p + "watermark-user{position:fixed;top:50%;left:50%;transform:translate(-50%,-50%) rotate(-45deg);font-size:6em;color:rgba(128,128,128,0.12);pointer-events:none;z-index:9999;user-select:none;}"
           + "." + p + "watermark-program{position:fixed;bottom:4mm;right:4mm;font-size:0.5em;color:rgba(128,128,128,0.35);pointer-events:none;z-index:9999;}"
           + "@media print{." + p + "watermark-user{position:fixed;}}";
  }
}
