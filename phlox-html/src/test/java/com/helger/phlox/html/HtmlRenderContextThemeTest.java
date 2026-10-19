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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Test;

import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.resource.ClassPathResource;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.testfiles.MockPhloxTestFiles;
import com.helger.phlox.theme.PhloxThemeException;
import com.helger.phlox.theme.PhloxThemeReader;
import com.helger.phlox.ubl.UblInvoiceReader;

/**
 * Test class for {@link HtmlRenderContext#applyTheme(com.helger.phlox.theme.PhloxTheme)}.
 *
 * @author Philip Helger
 */
public final class HtmlRenderContextThemeTest
{
  private static PhloxInvoice _read ()
  {
    try (final InputStream aIS = new ClassPathResource (MockPhloxTestFiles.BASE_PATH +
                                                        "2017/ubl/invoice/ubl-tc434-example1.xml").getInputStream ())
    {
      return UblInvoiceReader.fromUbl21 ().read (aIS, new ErrorList ());
    }
    catch (final IOException ex)
    {
      throw new IllegalStateException (ex);
    }
  }

  @Test
  public void testApplyTheme ()
  {
    final HtmlRenderContext aCtx = new HtmlRenderContext ().applyTheme (PhloxThemeReader.readFromString ("{"
                                                                                                          + "\"variables\":{\"--brand\":\"#8a1538\",\"--phlox-accent\":\"var(--brand)\"},"
                                                                                                          + "\"common\":{\"locale\":\"de\"},"
                                                                                                          + "\"html\":{\"cssClassPrefix\":\"inv-\",\"customStylesheet\":\".inv-title{color:red}\"}}",
                                                                                                          null));
    assertEquals (Locale.GERMAN, aCtx.getLocale ());
    assertEquals ("inv-", aCtx.getCssClassPrefix ());
    assertEquals ("var(--brand)", aCtx.getCssVariables ().get ("--phlox-accent"));

    final String sHtml = new HtmlInvoiceWriter ().writeAsString (_read (), aCtx);
    // The variables are written after the default stylesheet and before the custom one
    final int nDefault = sHtml.indexOf ("--phlox-ink:");
    final int nVariables = sHtml.indexOf ("\n:root{--brand:#8a1538;--phlox-accent:var(--brand);}");
    final int nCustom = sHtml.indexOf (".inv-title{color:red}");
    assertTrue (nDefault >= 0);
    assertTrue (nVariables > nDefault);
    assertTrue (nCustom > nVariables);
    assertTrue (sHtml.contains ("class=\"inv-page\""));
    assertTrue (sHtml.contains ("Rechnung"));
  }

  @Test
  public void testNoVariablesNoRootRule ()
  {
    final String sHtml = new HtmlInvoiceWriter ().writeAsString (_read (), new HtmlRenderContext ());
    assertFalse (sHtml.contains ("\n:root{--"));
  }

  @Test
  public void testUnknownKeyIsAnError ()
  {
    try
    {
      new HtmlRenderContext ().applyTheme (PhloxThemeReader.readFromString ("{\"html\":{\"cssClasPrefix\":\"x\"}}", null));
      fail ();
    }
    catch (final PhloxThemeException ex)
    {
      assertTrue (ex.getMessage (), ex.getMessage ().contains ("html.cssClasPrefix"));
    }
  }

  @Test
  public void testInvalidCssVariableIsRejected ()
  {
    try
    {
      new HtmlRenderContext ().putCssVariable ("--x", "red;} body{display:none");
      fail ();
    }
    catch (final IllegalArgumentException ex)
    {
      // expected
    }
  }
}
