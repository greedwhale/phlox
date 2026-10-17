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
package com.helger.phlox.cii;

import org.jspecify.annotations.Nullable;

/**
 * Base class of the edition specific CII readers. It only contains helpers that do not depend on
 * the JAXB types of a specific CII release. The CII D16B and CII D25A JAXB classes (identifier,
 * text, date, party, ...) are generated independently and share no base classes, so everything that
 * touches them has to stay in the edition specific reader.
 *
 * @author Philip Helger
 */
abstract class AbstractCiiInvoiceReader
{
  @Nullable
  protected static String str (@Nullable final String s)
  {
    if (s == null)
      return null;

    final String sTrimmed = s.trim ();
    return sTrimmed.isEmpty () ? null : sTrimmed;
  }
}
