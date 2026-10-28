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
import com.helger.base.id.IHasID;
import com.helger.base.lang.EnumHelper;
import com.helger.pdflayout.pdfa.EPdfAFRelationship;

/**
 * The Factur-X / ZUGFeRD profile (conformance level) of an embedded CII invoice. The ID is the value
 * of <code>fx:ConformanceLevel</code> in the XMP metadata.
 *
 * @author Philip Helger
 */
public enum EFacturXConformanceLevel implements IHasID <String>
{
  /** MINIMUM - not an invoice in the sense of EN 16931 */
  MINIMUM ("MINIMUM", "urn:factur-x.eu:1p0:minimum"),
  /** BASIC WL - not an invoice in the sense of EN 16931 */
  BASIC_WL ("BASIC WL", "urn:factur-x.eu:1p0:basicwl"),
  /** BASIC - a subset of EN 16931 */
  BASIC ("BASIC", "urn:cen.eu:en16931:2017#compliant#urn:factur-x.eu:1p0:basic"),
  /** EN 16931 (formerly COMFORT) */
  EN_16931 ("EN 16931", "urn:cen.eu:en16931:2017"),
  /** EXTENDED - an extension of EN 16931 */
  EXTENDED ("EXTENDED", "urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended"),
  /** XRECHNUNG - the German CIUS; the specification identifier depends on the XRechnung version */
  XRECHNUNG ("XRECHNUNG", "urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_");

  /** The file name of the embedded XML for all profiles except XRECHNUNG */
  public static final String FILE_NAME_FACTUR_X = "factur-x.xml";
  /** The file name of the embedded XML for the XRECHNUNG profile */
  public static final String FILE_NAME_XRECHNUNG = "xrechnung.xml";

  private final String m_sID;
  private final String m_sSpecificationIdentifier;

  EFacturXConformanceLevel (@NonNull @Nonempty final String sID,
                            @NonNull @Nonempty final String sSpecificationIdentifier)
  {
    m_sID = sID;
    m_sSpecificationIdentifier = sSpecificationIdentifier;
  }

  /**
   * @return The value of <code>fx:ConformanceLevel</code>, e.g. <code>EN 16931</code>.
   */
  @NonNull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The specification identifier (BT-24) of this profile. For {@link #XRECHNUNG} it is the
   *         prefix without the XRechnung version.
   */
  @NonNull
  @Nonempty
  public String getSpecificationIdentifier ()
  {
    return m_sSpecificationIdentifier;
  }

  /**
   * @return The file name of the embedded XML: <code>xrechnung.xml</code> for XRECHNUNG,
   *         <code>factur-x.xml</code> otherwise.
   */
  @NonNull
  @Nonempty
  public String getFileName ()
  {
    return this == XRECHNUNG ? FILE_NAME_XRECHNUNG : FILE_NAME_FACTUR_X;
  }

  /**
   * @return The default relationship of the embedded XML to the PDF: <code>Data</code> for MINIMUM
   *         and BASIC WL, <code>Alternative</code> for all other profiles (the only value allowed in
   *         Germany).
   */
  @NonNull
  public EPdfAFRelationship getDefaultRelationship ()
  {
    return this == MINIMUM || this == BASIC_WL ? EPdfAFRelationship.DATA : EPdfAFRelationship.ALTERNATIVE;
  }

  /**
   * Find the profile with the provided ID.
   *
   * @param sID
   *        The ID, e.g. <code>EN 16931</code>. May be <code>null</code>.
   * @return <code>null</code> if no such profile exists.
   */
  @Nullable
  public static EFacturXConformanceLevel getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EFacturXConformanceLevel.class, sID);
  }

  /**
   * Find the profile from the specification identifier (BT-24) of an invoice.
   *
   * @param sSpecificationIdentifier
   *        The specification identifier. May be <code>null</code>.
   * @return <code>null</code> if the specification identifier is not the one of a Factur-X /
   *         ZUGFeRD profile.
   */
  @Nullable
  public static EFacturXConformanceLevel getFromSpecificationIdentifierOrNull (@Nullable final String sSpecificationIdentifier)
  {
    if (sSpecificationIdentifier == null)
      return null;
    final String sTrimmed = sSpecificationIdentifier.trim ();
    if (sTrimmed.startsWith (XRECHNUNG.m_sSpecificationIdentifier))
      return XRECHNUNG;
    for (final EFacturXConformanceLevel e : values ())
      if (e != XRECHNUNG && e.m_sSpecificationIdentifier.equals (sTrimmed))
        return e;
    return null;
  }
}
