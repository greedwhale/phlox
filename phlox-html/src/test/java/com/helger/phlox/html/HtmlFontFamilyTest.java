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
package com.helger.phlox.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.helger.phlox.theme.PhloxThemeException;
import com.helger.phlox.theme.PhloxThemeReader;

/**
 * Test class for class {@link HtmlFontFamily}.
 *
 * @author Philip Helger
 */
public final class HtmlFontFamilyTest
{
  private static final byte [] BYTES = "font".getBytes (StandardCharsets.UTF_8);

  @Test
  public void testCss ()
  {
    final String sCss = new HtmlFontFamily ("a.ttf", BYTES, "b.otf", BYTES, null, null, "d.woff2", BYTES).getCss ();
    assertTrue (sCss.contains ("font-weight:400;font-style:normal;src:url(data:font/ttf;base64,Zm9udA==) format(\"truetype\")"));
    assertTrue (sCss.contains ("font-weight:700;font-style:normal;src:url(data:font/otf;base64,Zm9udA==) format(\"opentype\")"));
    assertFalse (sCss.contains ("font-style:italic;src:url(data:font/ttf"));
    assertTrue (sCss.contains ("font-weight:700;font-style:italic;src:url(data:font/woff2;base64,Zm9udA==) format(\"woff2\")"));
    assertTrue (sCss.contains ("body{font-family:\"Phlox Custom\","));
  }

  @Test
  public void testUnsupportedExtension ()
  {
    try
    {
      new HtmlFontFamily ("a.txt", BYTES, null, null, null, null, null, null);
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {
      assertTrue (ex.getMessage ().contains ("a.txt"));
    }
  }

  @Test
  public void testThemeFontIsReported ()
  {
    assertNull (new HtmlRenderContext ().getFontFamily ());
    try
    {
      new HtmlRenderContext ().applyTheme (PhloxThemeReader.readFromString ("{\"html\":{\"fonts\":{\"regular\":\"classpath:missing.ttf\"}}}",
                                                                               null));
      fail ();
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("html.fonts"));
    }
    try
    {
      new HtmlRenderContext ().applyTheme (PhloxThemeReader.readFromString ("{\"html\":{\"fonts\":{\"bold\":\"classpath:x.ttf\"}}}",
                                                                               null));
      fail ();
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("html.fonts.regular"));
    }
    assertEquals (null, new HtmlRenderContext ().setFontFamily (null).getFontFamily ());
  }
}
