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
package com.helger.norma.api.watermark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;

/**
 * Test class for class {@link WatermarkSpec}.
 *
 * @author Philip Helger
 */
public final class WatermarkSpecTest
{
  @Test
  public void testTextFactoryProducesDefaults ()
  {
    final WatermarkSpec aSpec = WatermarkSpec.text ("DRAFT");
    assertEquals ("DRAFT", aSpec.getText ());
    assertEquals (WatermarkSpec.DEFAULT_OPACITY, aSpec.getOpacity (), 0.0001f);
    assertEquals (WatermarkSpec.DEFAULT_ROTATION, aSpec.getRotation (), 0.0001f);
    assertEquals (WatermarkSpec.DEFAULT_FONT_SIZE_PT, aSpec.getFontSizePt (), 0.0001f);
    assertEquals (EWatermarkPosition.CENTER, aSpec.getPosition ());
    assertTrue (aSpec.isRepeatPerPage ());
    assertNotNull (aSpec.getColor ());
  }

  @Test
  public void testFluentSettersStickValues ()
  {
    final WatermarkSpec aSpec = WatermarkSpec.text ("DRAFT");
    assertSame (aSpec, aSpec.setOpacity (0.5f));
    assertSame (aSpec, aSpec.setRotation (15f));
    assertSame (aSpec, aSpec.setColor (Color.RED));
    assertSame (aSpec, aSpec.setPosition (EWatermarkPosition.FOOTER_RIGHT));
    assertSame (aSpec, aSpec.setFontSizePt (24f));
    assertSame (aSpec, aSpec.setRepeatPerPage (false));
    assertEquals (0.5f, aSpec.getOpacity (), 0.0001f);
    assertEquals (15f, aSpec.getRotation (), 0.0001f);
    assertEquals (Color.RED, aSpec.getColor ());
    assertEquals (EWatermarkPosition.FOOTER_RIGHT, aSpec.getPosition ());
    assertEquals (24f, aSpec.getFontSizePt (), 0.0001f);
    assertFalse (aSpec.isRepeatPerPage ());
  }

  @Test
  public void testCssRgbaCombinesColourAndOpacity ()
  {
    final String sCss = WatermarkSpec.text ("DRAFT").setColor (Color.BLACK).setOpacity (0.25f).getCssRgba ();
    // Format: "rgba(R,G,B,opacity)"
    assertTrue ("expected leading rgba(, got: " + sCss, sCss.startsWith ("rgba("));
    assertTrue ("expected opacity 0.25 in: " + sCss, sCss.contains ("0.25"));
    assertTrue ("expected black (0,0,0) in: " + sCss, sCss.contains ("0,0,0"));
  }

  @Test
  public void testOrNullPassesThrough ()
  {
    final WatermarkSpec aSpec = WatermarkSpec.text ("DRAFT");
    assertSame (aSpec, WatermarkSpec.orNull (aSpec));
    assertNull (WatermarkSpec.orNull (null));
  }
}
