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

import java.util.List;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;

/**
 * Collects the blocks of the plain-text output. A block is a list of lines (a paragraph, a table,
 * a list, ...); the blocks are separated by one empty line.
 *
 * @author Philip Helger
 */
final class TextBlockSink
{
  private final int m_nWidth;
  private final ICommonsList <ICommonsList <String>> m_aBlocks = new CommonsArrayList <> ();

  /**
   * @param nWidth
   *        The width in characters available to the blocks of this sink. 0 means unlimited.
   */
  TextBlockSink (final int nWidth)
  {
    m_nWidth = nWidth;
  }

  /**
   * @return The width in characters available to the blocks of this sink. 0 means unlimited.
   */
  int getWidth ()
  {
    return m_nWidth;
  }

  /**
   * Add a block. Trailing whitespace is removed from every line. A block without any text is
   * ignored.
   *
   * @param aLines
   *        The lines of the block. May not be <code>null</code>.
   */
  void addBlock (@NonNull final List <String> aLines)
  {
    final ICommonsList <String> aBlock = new CommonsArrayList <> (aLines.size ());
    boolean bHasText = false;
    for (final String sLine : aLines)
    {
      final String sStripped = sLine.stripTrailing ();
      aBlock.add (sStripped);
      if (!sStripped.isEmpty ())
        bHasText = true;
    }
    if (bHasText)
      m_aBlocks.add (aBlock);
  }

  /**
   * Add a block that consists of a single line.
   *
   * @param sLine
   *        The line. May not be <code>null</code>.
   */
  void addLine (@NonNull final String sLine)
  {
    addBlock (new CommonsArrayList <> (sLine));
  }

  /**
   * @return All lines of all blocks, with an empty line between two blocks. Never
   *         <code>null</code>.
   */
  @NonNull
  @ReturnsMutableCopy
  ICommonsList <String> getAllLines ()
  {
    final ICommonsList <String> ret = new CommonsArrayList <> ();
    for (final ICommonsList <String> aBlock : m_aBlocks)
    {
      if (ret.isNotEmpty ())
        ret.add ("");
      ret.addAll (aBlock);
    }
    return ret;
  }
}
