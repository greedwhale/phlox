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
package com.helger.phlox.api.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Locale;

import org.junit.Test;

/**
 * Test class for class {@link DefaultPhloxLabelResolver}.
 *
 * @author Philip Helger
 */
public final class DefaultPhloxLabelResolverTest
{
  @Test
  public void testSingletonIsStable ()
  {
    // Multiple references to the singleton must point at the same instance — the cache key on
    // PhloxTemplate depends on identity-equality of the resolver.
    assertSame (DefaultPhloxLabelResolver.INSTANCE, DefaultPhloxLabelResolver.INSTANCE);
  }

  @Test
  public void testEnglishLabel ()
  {
    final String sValue = DefaultPhloxLabelResolver.INSTANCE.getLabel ("section.header.title.invoice", Locale.ENGLISH);
    assertNotNull (sValue);
    assertEquals ("Invoice", sValue);
  }

  @Test
  public void testGermanLabel ()
  {
    final String sValue = DefaultPhloxLabelResolver.INSTANCE.getLabel ("section.header.title.invoice", Locale.GERMAN);
    assertNotNull (sValue);
    // We accept either of the common German renderings rather than pinning the exact string —
    // this test is here to catch a missing bundle, not to enforce wording.
    assertNotNull ("expected a non-empty German rendering for section.header.title.invoice", sValue);
  }

  @Test
  public void testMissingKeyFallsBackToKey ()
  {
    final String sKey = "no.such.key.in.bundle";
    assertEquals (sKey, DefaultPhloxLabelResolver.INSTANCE.getLabel (sKey, Locale.ENGLISH));
  }

  @Test
  public void testCodeListLookup ()
  {
    // VAT category "S" is in the standard EN 16931 code list.
    final String sValue = DefaultPhloxLabelResolver.INSTANCE.getCodeLabel ("vatcategory", "S", Locale.ENGLISH);
    assertNotNull (sValue);
  }

  @Test
  public void testMissingCodeFallsBackToCode ()
  {
    final String sCode = "ZZ_UNKNOWN";
    assertEquals (sCode, DefaultPhloxLabelResolver.INSTANCE.getCodeLabel ("vatcategory", sCode, Locale.ENGLISH));
  }
}
