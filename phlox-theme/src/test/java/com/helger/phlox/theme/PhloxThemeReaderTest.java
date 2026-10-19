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
package com.helger.phlox.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.FormatStyle;

import org.junit.Test;

import com.helger.io.resource.ClassPathResource;

/**
 * Test class for class {@link PhloxThemeReader}, {@link PhloxTheme} and {@link PhloxThemeObject}.
 *
 * @author Philip Helger
 */
public final class PhloxThemeReaderTest
{
  private static PhloxTheme _read (final String sJson)
  {
    return PhloxThemeReader.readFromString (sJson, null);
  }

  private static void _assertError (final String sJson, final String sExpectedInMessage)
  {
    try
    {
      final PhloxTheme aTheme = _read (sJson);
      // The values are checked when they are read
      aTheme.getCommon ().getKeys ().forEach (sKey -> aTheme.getCommon ().getString (sKey));
      aTheme.getPdf ().getKeys ().forEach (sKey -> aTheme.getPdf ().getString (sKey));
      fail ("Expected an error with '" + sExpectedInMessage + "'");
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains (sExpectedInMessage));
    }
  }

  @Test
  public void testSampleTheme () throws IOException
  {
    try (final InputStream aIS = new ClassPathResource ("sample-theme.json").getInputStream ())
    {
      final PhloxTheme aTheme = PhloxThemeReader.read (aIS, null);
      assertEquals ("#8a1538", aTheme.getVariable ("--phlox-accent"));
      // As written in the file
      assertEquals ("var(--brand)", aTheme.getVariables ().get ("--phlox-accent"));
      assertEquals ("de", aTheme.getCommon ().getString ("locale"));
      assertEquals ("#8a1538", aTheme.getCommon ().getObject ("watermark").getString ("color"));
      assertEquals (Color.decode ("#8a1538"), aTheme.getCommon ().getObject ("watermark").getColor ("color"));
      assertEquals (Float.valueOf (9.5f), aTheme.getPdf ().getObject ("bodyFont").getFloat ("size"));
    }
  }

  @Test
  public void testDollarKeysAreIgnored ()
  {
    final PhloxTheme aTheme = _read ("{\"$comment\":\"top\",\"common\":{\"$comment\":\"x\",\"locale\":\"en\"},\"variables\":{\"$c\":\"y\",\"--a\":\"1\"}}");
    assertEquals (1, aTheme.getCommon ().getKeys ().size ());
    assertEquals ("en", aTheme.getCommon ().getString ("locale"));
    assertEquals (1, aTheme.getVariables ().size ());
    aTheme.getCommon ().checkKeys ("locale");
  }

  @Test
  public void testEmptyParts ()
  {
    final PhloxTheme aTheme = _read ("{}");
    assertTrue (aTheme.getCommon ().getKeys ().isEmpty ());
    assertTrue (aTheme.getHtml ().getKeys ().isEmpty ());
    assertTrue (aTheme.getPdf ().getKeys ().isEmpty ());
    assertNull (aTheme.getVariable ("--x"));
    assertNull (aTheme.getColorVariable ("--x", null));
    assertEquals (Color.WHITE, aTheme.getColorVariable ("--x", Color.WHITE));
  }

  @Test
  public void testVariables ()
  {
    final PhloxTheme aTheme = _read ("{\"variables\":{\"--a\":\"#112233\",\"--b\":\"var(--a)\",\"--c\":\"var(--b)\",\"--d\":\"1px solid var(--a)\"},"
                                     + "\"common\":{\"x\":\"var(--c)\",\"y\":\"var(--missing, var(--a))\",\"z\":\"var(--d)\"}}");
    assertEquals ("#112233", aTheme.getVariable ("--c"));
    assertEquals ("#112233", aTheme.getCommon ().getString ("x"));
    assertEquals ("#112233", aTheme.getCommon ().getString ("y"));
    assertEquals ("1px solid #112233", aTheme.getCommon ().getString ("z"));
    assertEquals (new Color (0x11, 0x22, 0x33), aTheme.getColorVariable ("--b", null));
  }

  @Test
  public void testVariableErrors ()
  {
    _assertError ("{\"common\":{\"x\":\"var(--missing)\"}}", "'--missing' is not defined");
    _assertError ("{\"variables\":{\"--a\":\"var(--b)\",\"--b\":\"var(--a)\"}}", "refers to itself");
    _assertError ("{\"variables\":{\"a\":\"1\"}}", "starts with --");
  }

  @Test
  public void testStructureErrors ()
  {
    _assertError ("[]", "must be a JSON object");
    _assertError ("{broken", "no valid JSON");
    _assertError ("{\"unknown\":1}", "unknown key");
    _assertError ("{\"common\":{\"a\":{}}}", "single value");
  }

  @Test
  public void testTypedValues ()
  {
    final PhloxTheme aTheme = _read ("{\"pdf\":{\"n\":\"12\",\"b\":true,\"c\":\"#abc\",\"l\":\"20mm\",\"i\":3,\"f\":1.5,\"e\":\"footer-right\"}}");
    final PhloxThemeObject aPdf = aTheme.getPdf ();
    assertEquals (Float.valueOf (12f), aPdf.getFloat ("n"));
    assertEquals (Boolean.TRUE, aPdf.getBoolean ("b"));
    assertEquals (new Color (0xaa, 0xbb, 0xcc), aPdf.getColor ("c"));
    assertEquals (20 * 72 / 25.4f, aPdf.getLength ("l").floatValue (), 0.001f);
    assertEquals (Integer.valueOf (3), aPdf.getInt ("i"));
    assertEquals (Float.valueOf (1.5f), aPdf.getFloat ("f"));
    assertEquals (FormatStyle.MEDIUM, _read ("{\"pdf\":{\"e\":\"Medium\"}}").getPdf ().getEnum ("e", FormatStyle.class));
    assertNull (aPdf.getString ("missing"));
    assertFalse (aPdf.containsKey ("missing"));
    assertNotNull (aPdf.getPath ());
  }

  @Test
  public void testTypedValueErrors ()
  {
    final PhloxThemeObject aPdf = _read ("{\"pdf\":{\"c\":\"red-ish\",\"l\":\"20 miles\",\"b\":\"yes\",\"i\":1.5,\"e\":\"huge\"}}").getPdf ();
    for (final String sKey : new String [] { "c", "l", "b", "i" })
      try
      {
        switch (sKey)
        {
          case "c":
            aPdf.getColor (sKey);
            break;
          case "l":
            aPdf.getLength (sKey);
            break;
          case "b":
            aPdf.getBoolean (sKey);
            break;
          default:
            aPdf.getInt (sKey);
            break;
        }
        fail ("Expected an error for " + sKey);
      }
      catch (final PhloxThemeException ex)
      {
        assertTrue (ex.getMessage (), ex.getMessage ().contains ("pdf." + sKey));
      }
    try
    {
      aPdf.getEnum ("e", FormatStyle.class);
      fail ();
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("possible values are full, long, medium, short"));
    }
  }

  @Test
  public void testCheckKeys ()
  {
    final PhloxThemeObject aPdf = _read ("{\"pdf\":{\"pageSzie\":\"a4\"}}").getPdf ();
    try
    {
      aPdf.checkKeys ("pageSize", "margins");
      fail ();
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("pdf.pageSzie"));
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("pageSize, margins"));
    }
  }
}
