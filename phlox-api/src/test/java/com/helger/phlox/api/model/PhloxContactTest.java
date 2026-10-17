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
package com.helger.phlox.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for class {@link PhloxContact}.
 *
 * @author Philip Helger
 */
public final class PhloxContactTest
{
  @Test
  public void testEmptyByDefault ()
  {
    final PhloxContact aContact = new PhloxContact ();
    assertTrue (aContact.isEmpty ());
    assertNull (aContact.getName ());
    assertNull (aContact.getTelephone ());
    assertNull (aContact.getEmail ());
  }

  @Test
  public void testNotEmptyWhenAnyFieldSet ()
  {
    assertFalse (new PhloxContact ().setName ("Alice").isEmpty ());
    assertFalse (new PhloxContact ().setTelephone ("+49 30 1234").isEmpty ());
    assertFalse (new PhloxContact ().setEmail ("a@b.c").isEmpty ());
  }

  @Test
  public void testFluentSettersStickValues ()
  {
    final PhloxContact aContact = new PhloxContact ();
    assertSame (aContact, aContact.setName ("Alice"));
    assertSame (aContact, aContact.setTelephone ("+49 30 1234"));
    assertSame (aContact, aContact.setEmail ("a@b.c"));
    assertEquals ("Alice", aContact.getName ());
    assertEquals ("+49 30 1234", aContact.getTelephone ());
    assertEquals ("a@b.c", aContact.getEmail ());
  }
}
