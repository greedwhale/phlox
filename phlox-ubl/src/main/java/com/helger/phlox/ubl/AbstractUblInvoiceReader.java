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
package com.helger.phlox.ubl;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

import com.helger.annotation.Nonempty;
import com.helger.datetime.xml.XMLOffsetDate;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.phlox.api.model.PhloxIdentifier;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.xsds.ccts.cct.schemamodule.IdentifierType;
import com.helger.xsds.ccts.cct.schemamodule.TextType;

/**
 * Base class of the edition specific UBL readers. It only contains helpers that work on types that
 * are shared between UBL 2.1 and UBL 2.5 (plain strings, dates and the CCTS base types that all UBL
 * basic components extend). Everything that touches the version specific JAXB types of
 * {@code commonaggregatecomponents_21} / {@code _25} has to stay in the edition specific reader,
 * because the two sets of classes are unrelated.
 *
 * @author Philip Helger
 */
abstract class AbstractUblInvoiceReader
{
  private static final String ROOT_INVOICE = "Invoice";
  private static final String ROOT_CREDIT_NOTE = "CreditNote";

  /**
   * Get the UBL version this reader handles.
   *
   * @return The UBL version this reader handles, e.g. {@code 2.1}. Used for messages only.
   */
  @NonNull
  @Nonempty
  protected abstract String _getUblVersion ();

  /**
   * Read the already parsed document as UBL Invoice.
   *
   * @param aDoc
   *        The parsed document, whose root element is {@code Invoice}. Never {@code null}.
   * @param aErrors
   *        The error list to fill. Never {@code null}.
   * @return {@code null} if the document does not match the UBL Invoice schema.
   */
  @Nullable
  protected abstract PhloxInvoice readInvoiceDocument (@NonNull Document aDoc, @NonNull ErrorList aErrors);

  /**
   * Read the already parsed document as UBL CreditNote.
   *
   * @param aDoc
   *        The parsed document, whose root element is {@code CreditNote}. Never {@code null}.
   * @param aErrors
   *        The error list to fill. Never {@code null}.
   * @return {@code null} if the document does not match the UBL CreditNote schema.
   */
  @Nullable
  protected abstract PhloxInvoice readCreditNoteDocument (@NonNull Document aDoc, @NonNull ErrorList aErrors);

  @Nullable
  protected static String str (@Nullable final String s)
  {
    if (s == null)
      return null;

    final String sTrim = s.trim ();
    return sTrim.isEmpty () ? null : sTrim;
  }

  @Nullable
  protected static String strFromCode (@Nullable final String s)
  {
    return str (s);
  }

  @Nullable
  protected static LocalDate d (@Nullable final XMLOffsetDate aDate)
  {
    return aDate == null ? null : aDate.toLocalDate ();
  }

  @Nullable
  protected static String firstText (@NonNull final List <? extends TextType> aTexts)
  {
    return aTexts.isEmpty () ? null : str (aTexts.get (0).getValue ());
  }

  @NonNull
  protected static String joinNotes (@NonNull final List <? extends TextType> aNotes)
  {
    final StringBuilder aSb = new StringBuilder ();
    for (final TextType aN : aNotes)
    {
      if (aN.getValue () == null)
        continue;
      if (aSb.length () > 0)
        aSb.append ('\n');
      aSb.append (aN.getValue ());
    }
    return aSb.toString ();
  }

  /**
   * Convert any UBL identifier element (ID, CompanyID, EndpointID, ...) into a model identifier
   * without touching the value.
   *
   * @param aID
   *        The identifier element. May be {@code null}.
   * @return {@code null} if the element is {@code null}, otherwise the value together with its
   *         scheme ID.
   */
  @Nullable
  protected static PhloxIdentifier identifier (@Nullable final IdentifierType aID)
  {
    return aID == null ? null : new PhloxIdentifier (aID.getValue (), aID.getSchemeID ());
  }

  /**
   * Read an already parsed UBL document. The root element decides whether the document is read as
   * Invoice or as CreditNote, so the document is neither parsed again nor tried against both
   * schemas.
   *
   * @param aDoc
   *        The parsed document. Never {@code null}.
   * @param aErrors
   *        The error list to fill. Never {@code null}.
   * @return The populated model or {@code null} if the document is neither a valid Invoice nor a
   *         valid CreditNote of this reader's UBL version.
   */
  @Nullable
  final PhloxInvoice read (@NonNull final Document aDoc, @NonNull final ErrorList aErrors)
  {
    final String sRootName = aDoc.getDocumentElement ().getLocalName ();
    final PhloxInvoice ret;
    if (ROOT_INVOICE.equals (sRootName))
      ret = readInvoiceDocument (aDoc, aErrors);
    else
      if (ROOT_CREDIT_NOTE.equals (sRootName))
        ret = readCreditNoteDocument (aDoc, aErrors);
      else
        ret = null;

    if (ret == null)
      aErrors.add (SingleError.builderError ()
                              .errorText ("Source could not be parsed as UBL " +
                                          _getUblVersion () +
                                          " Invoice or CreditNote")
                              .build ());
    return ret;
  }
}
