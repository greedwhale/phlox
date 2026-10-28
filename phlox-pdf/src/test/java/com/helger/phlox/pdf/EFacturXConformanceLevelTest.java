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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.helger.pdflayout.pdfa.EPdfAFRelationship;

/**
 * Test class for class {@link EFacturXConformanceLevel}.
 *
 * @author Philip Helger
 */
public final class EFacturXConformanceLevelTest
{
  @Test
  public void testBasic ()
  {
    for (final EFacturXConformanceLevel e : EFacturXConformanceLevel.values ())
    {
      assertSame (e, EFacturXConformanceLevel.getFromIDOrNull (e.getID ()));
      assertSame (e, EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull (e.getSpecificationIdentifier ()));
    }
    assertNull (EFacturXConformanceLevel.getFromIDOrNull ("COMFORT"));
  }

  @Test
  public void testSpecificationIdentifier ()
  {
    assertSame (EFacturXConformanceLevel.EN_16931,
                EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull ("urn:cen.eu:en16931:2017"));
    assertSame (EFacturXConformanceLevel.XRECHNUNG,
                EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull ("urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0"));
    assertSame (EFacturXConformanceLevel.XRECHNUNG,
                EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull ("urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0#conformant#urn:xeinkauf.de:kosit:extension:xrechnung_3.0"));
    assertNull (EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull ("urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0"));
    assertNull (EFacturXConformanceLevel.getFromSpecificationIdentifierOrNull (null));
  }

  @Test
  public void testFileNameAndRelationship ()
  {
    assertEquals ("xrechnung.xml", EFacturXConformanceLevel.XRECHNUNG.getFileName ());
    assertEquals ("factur-x.xml", EFacturXConformanceLevel.EN_16931.getFileName ());
    assertSame (EPdfAFRelationship.DATA, EFacturXConformanceLevel.MINIMUM.getDefaultRelationship ());
    assertSame (EPdfAFRelationship.DATA, EFacturXConformanceLevel.BASIC_WL.getDefaultRelationship ());
    assertSame (EPdfAFRelationship.ALTERNATIVE, EFacturXConformanceLevel.EXTENDED.getDefaultRelationship ());
  }
}
