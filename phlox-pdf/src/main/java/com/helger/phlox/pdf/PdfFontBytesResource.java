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

import java.io.InputStream;
import java.util.Locale;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.font.api.EFontStyle;
import com.helger.font.api.EFontType;
import com.helger.font.api.EFontWeight;
import com.helger.font.api.IFontResource;
import com.helger.font.api.IFontStyle;
import com.helger.font.api.IFontWeight;
import com.helger.io.resource.IReadableResource;
import com.helger.io.resource.inmemory.ReadableResourceByteArray;

/**
 * A font from the bytes of a TTF or OTF file, for example a file that a theme refers to.
 *
 * @author Philip Helger
 */
final class PdfFontBytesResource implements IFontResource
{
  private final String m_sReference;
  private final EFontType m_eFontType;
  private final byte [] m_aBytes;

  /**
   * Constructor.
   *
   * @param sReference
   *        The file name or classpath reference the bytes come from. Its extension selects the font
   *        type. May neither be <code>null</code> nor empty.
   * @param aBytes
   *        The content of the font file. May not be <code>null</code>.
   * @throws IllegalArgumentException
   *         If the extension is neither <code>.ttf</code> nor <code>.otf</code>.
   */
  PdfFontBytesResource (@NonNull @Nonempty final String sReference, final byte @NonNull [] aBytes)
  {
    ValueEnforcer.notEmpty (sReference, "Reference");
    ValueEnforcer.notNull (aBytes, "Bytes");
    final String sLower = sReference.toLowerCase (Locale.ROOT);
    if (sLower.endsWith (".ttf"))
      m_eFontType = EFontType.TTF;
    else
      if (sLower.endsWith (".otf"))
        m_eFontType = EFontType.OTF;
      else
        throw new IllegalArgumentException ("'" + sReference + "' is neither a .ttf nor an .otf file");
    m_sReference = sReference;
    m_aBytes = aBytes;
  }

  @NonNull
  @Nonempty
  public String getID ()
  {
    return m_sReference + ":" + m_aBytes.length;
  }

  @NonNull
  @Nonempty
  public String getFontName ()
  {
    return m_sReference;
  }

  @NonNull
  public EFontType getFontType ()
  {
    return m_eFontType;
  }

  @NonNull
  public IFontStyle getFontStyle ()
  {
    return EFontStyle.REGULAR;
  }

  @NonNull
  public IFontWeight getFontWeight ()
  {
    return EFontWeight.REGULAR;
  }

  @NonNull
  @Nonempty
  public String getPath ()
  {
    return m_sReference;
  }

  @NonNull
  public IReadableResource getResource ()
  {
    return new ReadableResourceByteArray (m_sReference, m_aBytes);
  }

  @NonNull
  public InputStream getInputStream ()
  {
    return new NonBlockingByteArrayInputStream (m_aBytes);
  }

  public boolean isReadMultiple ()
  {
    return true;
  }
}
