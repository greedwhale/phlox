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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.concurrent.Immutable;
import com.helger.annotation.style.ReturnsMutableObject;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.tostring.ToStringGenerator;
import com.helger.pdflayout.pdfa.EPdfAFRelationship;

/**
 * The source XML of an invoice that is embedded into the PDF. phlox does not change the XML; it is
 * embedded as given. Two kinds exist:
 * <ul>
 * <li>{@link #ofSource(byte[], String)} - any source (UBL or CII), embedded with the relationship
 * <code>Source</code>.</li>
 * <li>{@link #ofFacturX(byte[])} - a CII invoice of EN 16931:2017, embedded as ZUGFeRD / Factur-X
 * hybrid invoice: file name <code>factur-x.xml</code> (or <code>xrechnung.xml</code>), the
 * relationship of the profile and the Factur-X XMP metadata. The profile is derived from the
 * specification identifier (BT-24) of the invoice unless it is set explicitly.</li>
 * </ul>
 * Both kinds require PDF/A-3, see {@link PdfRenderContext#setPdfALevel(com.helger.pdflayout.pdfa.EPdfALevel)}.
 *
 * @author Philip Helger
 */
@Immutable
public final class PdfEmbeddedSource
{
  /** The MIME type of the embedded XML */
  public static final String MIME_TYPE_XML = "text/xml";

  private final byte [] m_aContent;
  private final String m_sFileName;
  private final boolean m_bFacturX;
  private final EFacturXConformanceLevel m_eConformanceLevel;
  private final EPdfAFRelationship m_eRelationship;

  private PdfEmbeddedSource (final byte @NonNull [] aContent,
                             @Nullable final String sFileName,
                             final boolean bFacturX,
                             @Nullable final EFacturXConformanceLevel eConformanceLevel,
                             @Nullable final EPdfAFRelationship eRelationship)
  {
    m_aContent = aContent;
    m_sFileName = sFileName;
    m_bFacturX = bFacturX;
    m_eConformanceLevel = eConformanceLevel;
    m_eRelationship = eRelationship;
  }

  /**
   * @return The XML. Never {@code null}. The array is not copied.
   */
  @ReturnsMutableObject
  public byte @NonNull [] getContent ()
  {
    return m_aContent;
  }

  /**
   * @return The file name of a plain source. {@code null} for Factur-X, where the file name follows
   *         from the profile.
   */
  @Nullable
  public String getFileName ()
  {
    return m_sFileName;
  }

  /**
   * @return {@code true} if the XML is embedded as ZUGFeRD / Factur-X hybrid invoice.
   */
  public boolean isFacturX ()
  {
    return m_bFacturX;
  }

  /**
   * @return The explicitly set Factur-X profile. {@code null} to derive it from BT-24.
   */
  @Nullable
  public EFacturXConformanceLevel getConformanceLevel ()
  {
    return m_eConformanceLevel;
  }

  /**
   * @return The explicitly set relationship of the XML to the PDF. {@code null} for the default:
   *         <code>Source</code> for a plain source, the default of the profile for Factur-X.
   */
  @Nullable
  public EPdfAFRelationship getRelationship ()
  {
    return m_eRelationship;
  }

  /**
   * Get a copy with a different Factur-X profile. Only allowed for Factur-X.
   *
   * @param eConformanceLevel
   *        The profile. May be {@code null} to derive it from BT-24.
   * @return A new object. Never {@code null}.
   */
  @NonNull
  public PdfEmbeddedSource withConformanceLevel (@Nullable final EFacturXConformanceLevel eConformanceLevel)
  {
    ValueEnforcer.isTrue (m_bFacturX, "The profile can only be set for Factur-X");
    return new PdfEmbeddedSource (m_aContent, m_sFileName, m_bFacturX, eConformanceLevel, m_eRelationship);
  }

  /**
   * Get a copy with a different relationship of the XML to the PDF.
   *
   * @param eRelationship
   *        The relationship. May be {@code null} for the default.
   * @return A new object. Never {@code null}.
   */
  @NonNull
  public PdfEmbeddedSource withRelationship (@Nullable final EPdfAFRelationship eRelationship)
  {
    return new PdfEmbeddedSource (m_aContent, m_sFileName, m_bFacturX, m_eConformanceLevel, eRelationship);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("ContentLength", m_aContent.length)
                                       .append ("FileName", m_sFileName)
                                       .append ("FacturX", m_bFacturX)
                                       .append ("ConformanceLevel", m_eConformanceLevel)
                                       .append ("Relationship", m_eRelationship)
                                       .getToString ();
  }

  /**
   * Embed the source XML (UBL or CII) with the relationship <code>Source</code>.
   *
   * @param aContent
   *        The XML. May not be {@code null}. The array is not copied.
   * @param sFileName
   *        The file name shown in the PDF viewer, e.g. <code>invoice.xml</code>. May neither be
   *        {@code null} nor empty.
   * @return A new object. Never {@code null}.
   */
  @NonNull
  public static PdfEmbeddedSource ofSource (final byte @NonNull [] aContent, @NonNull @Nonempty final String sFileName)
  {
    ValueEnforcer.notNull (aContent, "Content");
    ValueEnforcer.notEmpty (sFileName, "FileName");
    return new PdfEmbeddedSource (aContent, sFileName, false, null, null);
  }

  /**
   * Embed a CII invoice of EN 16931:2017 as ZUGFeRD / Factur-X hybrid invoice.
   *
   * @param aCiiContent
   *        The CII XML. May not be {@code null}. The array is not copied.
   * @return A new object. Never {@code null}.
   */
  @NonNull
  public static PdfEmbeddedSource ofFacturX (final byte @NonNull [] aCiiContent)
  {
    ValueEnforcer.notNull (aCiiContent, "CiiContent");
    return new PdfEmbeddedSource (aCiiContent, null, true, null, null);
  }
}
