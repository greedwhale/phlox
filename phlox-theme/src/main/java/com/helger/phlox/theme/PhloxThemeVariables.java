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

import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.collection.commons.CommonsHashSet;
import com.helger.collection.commons.ICommonsOrderedMap;
import com.helger.collection.commons.ICommonsSet;

/**
 * The variables of a theme. A variable is named like a CSS custom property (<code>--name</code>)
 * and is used with <code>var(--name)</code> or <code>var(--name, fallback)</code> inside any text
 * value, also inside the value of another variable. The fallback may contain balanced parentheses,
 * for example another <code>var(...)</code>.
 *
 * @author Philip Helger
 */
final class PhloxThemeVariables
{
  private static final String VAR_START = "var(";
  private static final Pattern NAME = Pattern.compile ("--[A-Za-z0-9_-]+");

  private final ICommonsOrderedMap <String, String> m_aRaw;

  PhloxThemeVariables (@NonNull final ICommonsOrderedMap <String, String> aRaw)
  {
    m_aRaw = aRaw;
  }

  /**
   * @return The index of the parenthesis that closes the one in front of <code>nStart</code>, or
   *         -1.
   */
  private static int _findClosingParenthesis (@NonNull final String sValue, final int nStart)
  {
    int nDepth = 1;
    for (int i = nStart; i < sValue.length (); ++i)
    {
      final char c = sValue.charAt (i);
      if (c == '(')
        nDepth++;
      else
        if (c == ')' && --nDepth == 0)
          return i;
    }
    return -1;
  }

  @NonNull
  private String _resolveRecursive (@NonNull final String sPath,
                                    @NonNull final String sValue,
                                    @NonNull final ICommonsSet <String> aActive)
  {
    final StringBuilder aSB = new StringBuilder ();
    int nLast = 0;
    int nVar = sValue.indexOf (VAR_START);
    while (nVar >= 0)
    {
      final int nContent = nVar + VAR_START.length ();
      final int nEnd = _findClosingParenthesis (sValue, nContent);
      if (nEnd < 0)
        throw new PhloxThemeException ("Theme error at '" +
                                       sPath +
                                       "': the parenthesis of '" +
                                       sValue.substring (nVar) +
                                       "' is not closed");

      final String sContent = sValue.substring (nContent, nEnd);
      final int nComma = sContent.indexOf (',');
      final String sName = (nComma < 0 ? sContent : sContent.substring (0, nComma)).trim ();
      final String sFallback = nComma < 0 ? null : sContent.substring (nComma + 1).trim ();
      if (!NAME.matcher (sName).matches ())
        throw new PhloxThemeException ("Theme error at '" +
                                       sPath +
                                       "': '" +
                                       sName +
                                       "' is no variable name, it starts with --");

      aSB.append (sValue, nLast, nVar);
      final String sRaw = m_aRaw.get (sName);
      if (sRaw != null)
      {
        if (!aActive.add (sName))
          throw new PhloxThemeException ("Theme error at '" +
                                         sPath +
                                         "': the variable '" +
                                         sName +
                                         "' refers to itself");
        aSB.append (_resolveRecursive (sPath, sRaw, aActive));
        aActive.remove (sName);
      }
      else
        if (sFallback != null)
          aSB.append (_resolveRecursive (sPath, sFallback, aActive));
        else
          throw new PhloxThemeException ("Theme error at '" + sPath + "': the variable '" + sName + "' is not defined");
      nLast = nEnd + 1;
      nVar = sValue.indexOf (VAR_START, nLast);
    }
    aSB.append (sValue, nLast, sValue.length ());
    return aSB.toString ();
  }

  /**
   * Replace all variable references in a value.
   *
   * @param sPath
   *        The place of the value in the theme, for error messages.
   * @param sValue
   *        The value. May not be <code>null</code>.
   * @return The value without variable references. Never <code>null</code>.
   */
  @NonNull
  String resolve (@NonNull final String sPath, @NonNull final String sValue)
  {
    return _resolveRecursive (sPath, sValue, new CommonsHashSet <> ());
  }

  /**
   * Resolve the value of a variable.
   *
   * @param sName
   *        The name of the variable, including the leading <code>--</code>. May not be
   *        <code>null</code>.
   * @return The value without variable references or <code>null</code> if there is no such
   *         variable.
   */
  @Nullable
  String resolveVariable (@NonNull final String sName)
  {
    final String sRaw = m_aRaw.get (sName);
    if (sRaw == null)
      return null;

    final ICommonsSet <String> aActive = new CommonsHashSet <> ();
    aActive.add (sName);
    return _resolveRecursive ("variables." + sName, sRaw, aActive);
  }

  /**
   * @return The variables exactly as written in the theme. Never <code>null</code>.
   */
  @NonNull
  ICommonsOrderedMap <String, String> getRaw ()
  {
    return m_aRaw;
  }
}
