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
package com.helger.phlox.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;

import com.helger.phlox.api.PhloxRuntimeException;
import com.helger.phlox.api.model.PhloxInvoice;
import com.helger.phlox.api.template.IPhloxTemplate;
import com.helger.phlox.api.write.IInvoiceWriter;
import com.helger.phlox.template.PhloxTemplate;

/**
 * Renders a {@link PhloxInvoice} as plain text, e.g. for accessibility or for the preview of an
 * email. The content comes from a template; if the render context has none, the default template
 * is used. See {@link TextTemplateEmitter} for how the template is turned into text.
 *
 * @author Philip Helger
 */
public class TextInvoiceWriter implements IInvoiceWriter <TextRenderContext>
{
  /** Create a new plain-text invoice writer. */
  public TextInvoiceWriter ()
  {}

  /**
   * Render the invoice and return the text as a {@code String}. Equivalent to
   * {@link #writeTo(PhloxInvoice, TextRenderContext, OutputStream)} but keeps the result in
   * memory.
   *
   * @param aInvoice
   *        The invoice. Never {@code null}.
   * @param aCtx
   *        The plain-text render context. Never {@code null}.
   * @return The text with <code>\n</code> as line separator. Never {@code null}.
   */
  @NonNull
  public String writeAsString (@NonNull final PhloxInvoice aInvoice, @NonNull final TextRenderContext aCtx)
  {
    final IPhloxTemplate aTemplate = aCtx.getTemplate () != null ? aCtx.getTemplate () : PhloxTemplate.getDefault ();
    if (!(aTemplate instanceof final PhloxTemplate aPhloxTemplate))
      throw new PhloxRuntimeException ("Unsupported template implementation " + aTemplate.getClass ().getName ());
    return new TextTemplateEmitter (aCtx).emit (aPhloxTemplate.renderAst (aInvoice, aCtx));
  }

  /**
   * Render the invoice and write it as UTF-8 text to the given stream. The stream is closed by the
   * writer.
   *
   * @param aInvoice
   *        The invoice. Never {@code null}.
   * @param aCtx
   *        The plain-text render context. Never {@code null}.
   * @param aOut
   *        The output stream. Never {@code null}.
   */
  public void writeTo (@NonNull final PhloxInvoice aInvoice,
                       @NonNull final TextRenderContext aCtx,
                       @NonNull final OutputStream aOut)
  {
    final String sText = writeAsString (aInvoice, aCtx);
    try (final OutputStream aClosing = aOut)
    {
      aClosing.write (sText.getBytes (StandardCharsets.UTF_8));
    }
    catch (final IOException ex)
    {
      throw new UncheckedIOException (ex);
    }
  }
}
