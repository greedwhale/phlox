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
package com.helger.norma.template.ast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable resolved image — the binary, MIME type and filename for an {@code <img>} produced
 * from one of the supported template URL schemes ({@code data:}, {@code attachment:logo},
 * {@code attachment:BG-24[i]}, {@code classpath:/…}). Sits in the sidecar map on the
 * {@link NormaTemplateAST}, keyed by the original commonmark {@code Image} node.
 *
 * @author Philip Helger
 */
public final class NormaResolvedImage
{
  private final byte [] m_aBinary;
  private final String m_sMimeCode;
  private final String m_sFilename;
  private final String m_sAltText;

  /**
   * Construct a resolved image. Called from {@link com.helger.norma.template.NormaTemplatePostParser}
   * once it has resolved an image URL to concrete bytes.
   *
   * @param aBinary
   *        Image bytes; never {@code null} or empty.
   * @param sMimeCode
   *        MIME code (e.g. {@code image/png}); may be {@code null} when unknown.
   * @param sFilename
   *        Original filename when known (e.g. for {@code attachment:BG-24[i]}).
   * @param sAltText
   *        Alt text from the source {@code ![alt](url)}; may be {@code null}.
   */
  public NormaResolvedImage (final byte @NonNull [] aBinary,
                             @Nullable final String sMimeCode,
                             @Nullable final String sFilename,
                             @Nullable final String sAltText)
  {
    m_aBinary = aBinary;
    m_sMimeCode = sMimeCode;
    m_sFilename = sFilename;
    m_sAltText = sAltText;
  }

  /** @return The image bytes; never {@code null}, never empty. */
  public byte @NonNull [] getBinary ()
  {
    return m_aBinary;
  }

  /** @return MIME code (e.g. {@code image/png}). May be {@code null} when unknown. */
  @Nullable
  public String getMimeCode ()
  {
    return m_sMimeCode;
  }

  /** @return The original filename (when available, e.g. for {@code attachment:BG-24[i]}). */
  @Nullable
  public String getFilename ()
  {
    return m_sFilename;
  }

  /** @return The CommonMark image alt text ({@code ![alt](url)}). May be {@code null}. */
  @Nullable
  public String getAltText ()
  {
    return m_sAltText;
  }
}
