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
package com.helger.phlox.theme;

import org.jspecify.annotations.NonNull;

import com.helger.phlox.api.PhloxRuntimeException;

/**
 * Thrown if a theme cannot be read or contains an invalid value. The message names the place in the
 * theme, like <code>pdf.bodyFont.size</code>.
 *
 * @author Philip Helger
 */
public class PhloxThemeException extends PhloxRuntimeException
{
  /**
   * Constructor.
   *
   * @param sMessage
   *        The message. May not be <code>null</code>.
   */
  public PhloxThemeException (@NonNull final String sMessage)
  {
    super (sMessage);
  }

  /**
   * Constructor.
   *
   * @param sMessage
   *        The message. May not be <code>null</code>.
   * @param aCause
   *        The cause. May not be <code>null</code>.
   */
  public PhloxThemeException (@NonNull final String sMessage, @NonNull final Throwable aCause)
  {
    super (sMessage, aCause);
  }
}
