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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.helger.font.roboto.EFontResourceRoboto;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.phlox.theme.PhloxThemeException;
import com.helger.phlox.theme.PhloxThemeReader;

/**
 * Test class for class {@link PdfFontFamily} and the fonts of {@link PdfRenderContext}.
 *
 * @author Philip Helger
 */
public final class PdfFontFamilyTest
{
  private static final String ROBOTO = "classpath:fonts/ttf/Roboto/Roboto-";

  private static PdfFontFamily _roboto ()
  {
    return PdfFontFamily.of (EFontResourceRoboto.ROBOTO_NORMAL,
                             EFontResourceRoboto.ROBOTO_BOLD,
                             EFontResourceRoboto.ROBOTO_NORMAL_ITALIC,
                             EFontResourceRoboto.ROBOTO_BOLD_ITALIC);
  }

  private static PdfRenderContext _apply (final String sJson)
  {
    return new PdfRenderContext ().applyTheme (PhloxThemeReader.readFromString (sJson, null));
  }

  private static String _themeFonts (final String sFonts)
  {
    return "{\"pdf\":{\"fonts\":" + sFonts + "}}";
  }

  @Test
  public void testStandardIsTheDefault ()
  {
    final PdfRenderContext aCtx = new PdfRenderContext ();
    assertSame (PdfFontFamily.STANDARD, aCtx.getFontFamily ());
    assertEquals (PreloadFont.REGULAR, aCtx.getBodyFont ().getPreloadFont ());
    assertEquals (PreloadFont.REGULAR_BOLD, aCtx.getHeaderFont ().getPreloadFont ());
    assertEquals (PreloadFont.REGULAR_BOLD, aCtx.getTableHeaderFont ().getPreloadFont ());
  }

  @Test
  public void testGetSameStyleIn ()
  {
    final PdfFontFamily aRoboto = _roboto ();
    final PdfFontFamily aStd = PdfFontFamily.STANDARD;
    assertSame (aRoboto.getRegular (), aStd.getSameStyleIn (PreloadFont.REGULAR, aRoboto));
    assertSame (aRoboto.getBold (), aStd.getSameStyleIn (PreloadFont.REGULAR_BOLD, aRoboto));
    assertSame (aRoboto.getItalic (), aStd.getSameStyleIn (PreloadFont.REGULAR_ITALIC, aRoboto));
    assertSame (aRoboto.getBoldItalic (), aStd.getSameStyleIn (PreloadFont.REGULAR_BOLD_ITALIC, aRoboto));
    // A font that is not part of the family stays as it is
    assertSame (PreloadFont.MONOSPACE, aStd.getSameStyleIn (PreloadFont.MONOSPACE, aRoboto));
  }

  @Test
  public void testSetFontFamilySwitchesTheFonts ()
  {
    final PdfFontFamily aRoboto = _roboto ();
    final PdfRenderContext aCtx = new PdfRenderContext ().setFontFamily (aRoboto);
    assertSame (aRoboto, aCtx.getFontFamily ());
    assertEquals (aRoboto.getRegular (), aCtx.getBodyFont ().getPreloadFont ());
    assertEquals (aRoboto.getBold (), aCtx.getHeaderFont ().getPreloadFont ());
    assertEquals (aRoboto.getBold (), aCtx.getTableHeaderFont ().getPreloadFont ());
    // The sizes and colours stay
    assertEquals (PdfRenderContext.DEFAULT_BODY_FONT.getFontSize (), aCtx.getBodyFont ().getFontSize (), 0.001f);
    assertEquals (PdfRenderContext.DEFAULT_HEADER_FONT.getFontSize (), aCtx.getHeaderFont ().getFontSize (), 0.001f);
    assertEquals (PdfRenderContext.DEFAULT_HEADER_FONT.getColor (), aCtx.getHeaderFont ().getColor ());

    // A new palette keeps the family
    aCtx.setPalette (PdfPalette.DEFAULT);
    assertEquals (aRoboto.getBold (), aCtx.getHeaderFont ().getPreloadFont ());
    assertEquals (aRoboto.getBold (), aCtx.getTableHeaderFont ().getPreloadFont ());
  }

  @Test
  public void testThemeFonts ()
  {
    final PdfRenderContext aCtx = _apply (_themeFonts ("{\"regular\":\"" +
                                                       ROBOTO +
                                                       "Regular.ttf\",\"bold\":\"" +
                                                       ROBOTO +
                                                       "Bold.ttf\"}"));
    final PdfFontFamily aFamily = aCtx.getFontFamily ();
    assertNotEquals (PreloadFont.REGULAR, aFamily.getRegular ());
    assertNotEquals (aFamily.getRegular (), aFamily.getBold ());
    // A style without a file uses the closest style that has one
    assertSame (aFamily.getRegular (), aFamily.getItalic ());
    assertSame (aFamily.getBold (), aFamily.getBoldItalic ());
    assertEquals (aFamily.getRegular (), aCtx.getBodyFont ().getPreloadFont ());
    assertEquals (aFamily.getBold (), aCtx.getHeaderFont ().getPreloadFont ());
  }

  @Test
  public void testThemeFontsComeBeforeTheFontSettings ()
  {
    final PdfRenderContext aCtx = _apply ("{\"pdf\":{\"fonts\":{\"regular\":\"" +
                                          ROBOTO +
                                          "Regular.ttf\",\"bold\":\"" +
                                          ROBOTO +
                                          "Bold.ttf\"},\"bodyFont\":{\"bold\":true,\"size\":11},"
                                          + "\"headerFont\":{\"bold\":false}}}");
    assertEquals (aCtx.getFontFamily ().getBold (), aCtx.getBodyFont ().getPreloadFont ());
    assertEquals (11f, aCtx.getBodyFont ().getFontSize (), 0.001f);
    assertEquals (aCtx.getFontFamily ().getRegular (), aCtx.getHeaderFont ().getPreloadFont ());
  }

  @Test
  public void testThemeFontErrors ()
  {
    final String [] aInvalid = { _themeFonts ("{\"bold\":\"" + ROBOTO + "Bold.ttf\"}"),
                                 _themeFonts ("{\"regular\":\"" + ROBOTO + "Regular.ttf\",\"medium\":\"x.ttf\"}"),
                                 _themeFonts ("{\"regular\":\"classpath:fonts/does-not-exist.ttf\"}"),
                                 _themeFonts ("{\"regular\":\"classpath:fonts/ttf/Roboto/Roboto-Regular.woff\"}"),
                                 _themeFonts ("true") };
    for (final String sJson : aInvalid)
      try
      {
        _apply (sJson);
        fail ("Expected an error for " + sJson);
      }
      catch (final PhloxThemeException ex)
      {
        assertTrue (ex.getMessage (), ex.getMessage ().contains ("pdf.fonts"));
      }
  }
}
