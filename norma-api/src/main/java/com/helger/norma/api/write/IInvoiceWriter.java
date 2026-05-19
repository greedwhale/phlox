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

import org.jspecify.annotations.NonNull;

import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.model.NormaInvoice;

/**
 * SPI for output writers. {@code O} is the produced artifact type (e.g. {@code String} for
 * HTML, {@code Void} for stream-writing PDFs). {@code CTX} is the writer-specific context
 * subclass that callers must pass — the compiler then rejects accidental mixups between
 * writers.
 *
 * @author Philip Helger
 * @param <O>
 *        The output artifact type.
 * @param <CTX>
 *        The writer's expected context subtype.
 */
public interface IInvoiceWriter <O, CTX extends AbstractRenderContext <CTX>>
{
  /**
   * Render the given invoice using the given context.
   *
   * @param aInvoice
   *        The invoice to render. Never {@code null}.
   * @param aCtx
   *        The context. Never {@code null}.
   * @return The rendered artifact.
   */
  @NonNull
  O write (@NonNull NormaInvoice aInvoice, @NonNull CTX aCtx);
}
