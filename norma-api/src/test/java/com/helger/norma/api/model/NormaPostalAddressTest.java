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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for class {@link NormaPostalAddress}.
 *
 * @author Philip Helger
 */
public final class NormaPostalAddressTest
{
  @Test
  public void testEmptyByDefault ()
  {
    assertTrue (new NormaPostalAddress ().isEmpty ());
  }

  @Test
  public void testNotEmptyWhenAnyFieldSet ()
  {
    assertFalse (new NormaPostalAddress ().setLine1 ("Foostr. 1").isEmpty ());
    assertFalse (new NormaPostalAddress ().setCity ("Berlin").isEmpty ());
    assertFalse (new NormaPostalAddress ().setCountryCode ("DE").isEmpty ());
  }

  @Test
  public void testFluentSettersStickValues ()
  {
    final NormaPostalAddress aAddr = new NormaPostalAddress ();
    assertSame (aAddr, aAddr.setLine1 ("L1"));
    assertSame (aAddr, aAddr.setLine2 ("L2"));
    assertSame (aAddr, aAddr.setLine3 ("L3"));
    assertSame (aAddr, aAddr.setCity ("City"));
    assertSame (aAddr, aAddr.setPostCode ("12345"));
    assertSame (aAddr, aAddr.setCountrySubdivision ("BE"));
    assertSame (aAddr, aAddr.setCountryCode ("DE"));
    assertEquals ("L1", aAddr.getLine1 ());
    assertEquals ("L2", aAddr.getLine2 ());
    assertEquals ("L3", aAddr.getLine3 ());
    assertEquals ("City", aAddr.getCity ());
    assertEquals ("12345", aAddr.getPostCode ());
    assertEquals ("BE", aAddr.getCountrySubdivision ());
    assertEquals ("DE", aAddr.getCountryCode ());
  }
}
