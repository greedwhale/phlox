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
package com.helger.norma.api.template;

/**
 * Marker interface for a compiled norma template ({@code .ntpl}). Concrete implementations live
 * in the {@code norma-template} module — {@code norma-api} only carries this marker so
 * {@code AbstractRenderContext} can hold a typed reference without dragging in JMustache or
 * commonmark-java.
 * <p>
 * A {@link com.helger.norma.api.AbstractRenderContext} with {@code getTemplate() == null} falls
 * back to the hard-coded section grammar shipped in the writer modules — this is the default
 * and preserves byte-for-byte the 0.1.0 output.
 *
 * @author Philip Helger
 */
public interface INormaTemplate
{
  /* Marker interface; concrete behaviour lives in norma-template. */
}
