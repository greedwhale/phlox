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
package com.helger.phlox.template.ast;

import org.commonmark.node.CustomBlock;
import org.commonmark.node.Visitor;

/**
 * Custom commonmark block node produced by the post-pass for the template tags
 * <code>{{group:name}}</code> / <code>{{row:name}}</code> up to the matching
 * <code>{{end}}</code>. The blocks in between become the children of this node. The name of the
 * group is the block class of the node. A row lays out its children (the cells) side by side; a
 * group stacks them.
 *
 * @author Philip Helger
 */
public final class PhloxGroupNode extends CustomBlock
{
  private final boolean m_bRow;

  /**
   * Create a new group node.
   *
   * @param bRow
   *        <code>true</code> if the children are laid out side by side, <code>false</code> if they
   *        are stacked.
   */
  public PhloxGroupNode (final boolean bRow)
  {
    m_bRow = bRow;
  }

  /**
   * @return <code>true</code> if the children are laid out side by side, <code>false</code> if they
   *         are stacked.
   */
  public boolean isRow ()
  {
    return m_bRow;
  }

  /**
   * Default commonmark dispatch - emitters instance-check for this type.
   */
  @Override
  public void accept (final Visitor aVisitor)
  {
    aVisitor.visit (this);
  }
}
