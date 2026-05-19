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
package com.helger.norma.api.write;

import java.io.OutputStream;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.WillClose;
import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.model.NormaInvoice;

/**
 * SPI for output writers. Writers serialize a {@link NormaInvoice} to a target {@link OutputStream}
 * (UTF-8 encoded for text formats). {@code CTX} is the writer's expected context subtype, so the
 * compiler rejects accidental mixups between writers.
 * <p>
 * Concrete writers may expose additional convenience methods (e.g. returning a {@code String} for
 * HTML, taking a {@link java.io.File} for PDF) on top of this base contract.
 *
 * @author Philip Helger
 * @param <CTX>
 *        The writer's expected context subtype.
 */
public interface IInvoiceWriter <CTX extends AbstractRenderContext <CTX>>
{
  /**
   * Render the given invoice and stream the result to the given output stream. For text-based
   * formats the output is UTF-8 encoded. The stream is closed by the writer.
   *
   * @param aInvoice
   *        The invoice to render. Never {@code null}.
   * @param aCtx
   *        The context. Never {@code null}.
   * @param aOut
   *        The output stream to write to. Closed by the writer.
   * @throws Exception
   *         If rendering fails. Concrete writers may throw more specific subtypes.
   */
  void writeTo (@NonNull NormaInvoice aInvoice, @NonNull CTX aCtx, @NonNull @WillClose OutputStream aOut)
                                                                                                       throws Exception;
}
