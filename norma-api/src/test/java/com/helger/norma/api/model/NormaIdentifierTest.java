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
package com.helger.norma.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Test class for class {@link NormaIdentifier}.
 *
 * @author Philip Helger
 */
public final class NormaIdentifierTest
{
  @Test
  public void testEmptyConstructor ()
  {
    final NormaIdentifier aID = new NormaIdentifier ();
    assertNull (aID.getValue ());
    assertNull (aID.getSchemeID ());
  }

  @Test
  public void testValueAndSchemeConstructor ()
  {
    final NormaIdentifier aID = new NormaIdentifier ("DE123456789", "VAT");
    assertEquals ("DE123456789", aID.getValue ());
    assertEquals ("VAT", aID.getSchemeID ());
  }

  @Test
  public void testFluentSetters ()
  {
    final NormaIdentifier aID = new NormaIdentifier ();
    assertSame (aID, aID.setValue ("X"));
    assertSame (aID, aID.setSchemeID ("Y"));
    assertEquals ("X", aID.getValue ());
    assertEquals ("Y", aID.getSchemeID ());
  }
}
