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

import java.awt.Color;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.string.StringReplace;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.json.IJson;
import com.helger.json.IJsonObject;

/**
 * A JSON object of a theme with typed access to its values. Text values are returned with the
 * variable references replaced (see {@link PhloxTheme}). A key that starts with <code>$</code> is a
 * comment or other information for the reader of the file and is never returned. Every problem is
 * reported as a {@link PhloxThemeException} that names the place of the value, like
 * <code>pdf.bodyFont.size</code>.
 *
 * @author Philip Helger
 */
public final class PhloxThemeObject
{
  private final IJsonObject m_aJson;
  private final String m_sPath;
  private final PhloxThemeVariables m_aVariables;

  PhloxThemeObject (@NonNull final IJsonObject aJson,
                    @NonNull final String sPath,
                    @NonNull final PhloxThemeVariables aVariables)
  {
    m_aJson = aJson;
    m_sPath = sPath;
    m_aVariables = aVariables;
  }

  @NonNull
  private String _path (@NonNull final String sKey)
  {
    return m_sPath.isEmpty () ? sKey : m_sPath + "." + sKey;
  }

  @NonNull
  private PhloxThemeException _error (@NonNull final String sKey, @NonNull final String sMessage)
  {
    return new PhloxThemeException ("Theme error at '" + _path (sKey) + "': " + sMessage);
  }

  /**
   * @return The value as string, number or boolean, or <code>null</code> if the key is missing or
   *         the value is JSON null.
   */
  @Nullable
  private Object _getRaw (@NonNull final String sKey)
  {
    final IJson aJson = m_aJson.get (sKey);
    if (aJson == null)
      return null;
    if (!aJson.isValue ())
      throw _error (sKey, "a single value is expected, not an object or an array");
    return aJson.getAsValue ().getValue ();
  }

  /**
   * @return The place of this object in the theme, like <code>pdf.bodyFont</code>. Empty for the
   *         whole theme.
   */
  @NonNull
  public String getPath ()
  {
    return m_sPath;
  }

  /**
   * Check if a key is present.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return <code>true</code> if the key is present, also with the value JSON null.
   */
  public boolean containsKey (@NonNull final String sKey)
  {
    return m_aJson.containsKey (sKey);
  }

  /**
   * Get the keys, without the ones that start with <code>$</code>.
   *
   * @return The keys in the order of the file. Never <code>null</code>.
   */
  @NonNull
  public ICommonsList <String> getKeys ()
  {
    final ICommonsList <String> ret = new CommonsArrayList <> ();
    for (final String sKey : m_aJson.keySet ())
      if (!sKey.startsWith ("$"))
        ret.add (sKey);
    return ret;
  }

  /**
   * Fail if the object has a key that is not allowed. This finds typos. Keys that start with
   * <code>$</code> are always allowed.
   *
   * @param aAllowedKeys
   *        The allowed keys. May not be <code>null</code>.
   * @throws PhloxThemeException
   *         If there is an unknown key.
   */
  public void checkKeys (@NonNull final String... aAllowedKeys)
  {
    for (final String sKey : getKeys ())
    {
      boolean bFound = false;
      for (final String sAllowed : aAllowedKeys)
        if (sAllowed.equals (sKey))
        {
          bFound = true;
          break;
        }
      if (!bFound)
        throw _error (sKey, "unknown key, the allowed keys are " + String.join (", ", aAllowedKeys));
    }
  }

  /**
   * Get a text value. Numbers and booleans are returned as text.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The text with the variables replaced or <code>null</code> if there is none.
   */
  @Nullable
  public String getString (@NonNull final String sKey)
  {
    final Object aRaw = _getRaw (sKey);
    if (aRaw == null)
      return null;
    final String sText = aRaw.toString ();
    return aRaw instanceof String ? m_aVariables.resolve (_path (sKey), sText) : sText;
  }

  /**
   * Get a boolean value.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The value or <code>null</code> if there is none.
   */
  @Nullable
  public Boolean getBoolean (@NonNull final String sKey)
  {
    final String sText = getString (sKey);
    if (sText == null)
      return null;
    if ("true".equals (sText))
      return Boolean.TRUE;
    if ("false".equals (sText))
      return Boolean.FALSE;
    throw _error (sKey, "'" + sText + "' is no boolean - use true or false");
  }

  /**
   * Get a decimal number.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The value or <code>null</code> if there is none.
   */
  @Nullable
  public Float getFloat (@NonNull final String sKey)
  {
    final String sText = getString (sKey);
    if (sText == null)
      return null;
    try
    {
      return Float.valueOf (PhloxThemeValues.parseFloat (sText));
    }
    catch (final IllegalArgumentException ex)
    {
      throw _error (sKey, ex.getMessage ());
    }
  }

  /**
   * Get an integer number.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The value or <code>null</code> if there is none.
   */
  @Nullable
  public Integer getInt (@NonNull final String sKey)
  {
    final Float aValue = getFloat (sKey);
    if (aValue == null)
      return null;
    if (aValue.floatValue () != Math.rint (aValue.floatValue ()))
      throw _error (sKey, "'" + aValue + "' is no integer");
    return Integer.valueOf (aValue.intValue ());
  }

  /**
   * Get a colour, see the supported formats of the theme documentation.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The colour or <code>null</code> if there is none.
   */
  @Nullable
  public Color getColor (@NonNull final String sKey)
  {
    final String sText = getString (sKey);
    if (sText == null)
      return null;
    try
    {
      return PhloxThemeValues.parseColor (sText);
    }
    catch (final IllegalArgumentException ex)
    {
      throw _error (sKey, ex.getMessage ());
    }
  }

  /**
   * Get a length.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The length in points or <code>null</code> if there is none.
   */
  @Nullable
  public Float getLength (@NonNull final String sKey)
  {
    final String sText = getString (sKey);
    if (sText == null)
      return null;
    try
    {
      return Float.valueOf (PhloxThemeValues.parseLength (sText));
    }
    catch (final IllegalArgumentException ex)
    {
      throw _error (sKey, ex.getMessage ());
    }
  }

  /**
   * Get the value of an enum. The case is ignored and a minus sign is the same as an underscore.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @param aEnumClass
   *        The enum class. May not be <code>null</code>.
   * @return The value or <code>null</code> if there is none.
   * @param <E>
   *        The enum type.
   */
  @Nullable
  public <E extends Enum <E>> E getEnum (@NonNull final String sKey, @NonNull final Class <E> aEnumClass)
  {
    final String sText = getString (sKey);
    if (sText == null)
      return null;
    final String sName = StringReplace.replaceAll (sText.trim ().toUpperCase (Locale.ROOT), '-', '_');
    final ICommonsList <String> aNames = new CommonsArrayList <> ();
    for (final E eValue : aEnumClass.getEnumConstants ())
    {
      if (eValue.name ().equals (sName))
        return eValue;
      aNames.add (eValue.name ().toLowerCase (Locale.ROOT));
    }
    throw _error (sKey, "'" + sText + "' is unknown, the possible values are " + String.join (", ", aNames));
  }

  /**
   * Get a nested object.
   *
   * @param sKey
   *        The key. May not be <code>null</code>.
   * @return The object or <code>null</code> if there is none.
   */
  @Nullable
  public PhloxThemeObject getObject (@NonNull final String sKey)
  {
    final IJson aJson = m_aJson.get (sKey);
    if (aJson == null)
      return null;
    if (!aJson.isObject ())
      throw _error (sKey, "an object is expected");
    return new PhloxThemeObject (aJson.getAsObject (), _path (sKey), m_aVariables);
  }
}
