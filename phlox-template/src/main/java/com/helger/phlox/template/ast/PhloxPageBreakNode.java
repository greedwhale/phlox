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
 * Custom commonmark block node produced by the post-pass when a {@code {{pagebreak}}} sentinel
 * is found in the substituted template. Writer emitters pattern-match on this type to insert
 * the medium-specific page break (CSS {@code page-break-after} for HTML, {@code PLPageBreak}
 * for PDF).
 *
 * @author Philip Helger
 */
public final class PhloxPageBreakNode extends CustomBlock
{
  /**
   * Default commonmark dispatch — the standard {@code AbstractVisitor.visit(CustomBlock)} hook
   * fires. Emitters override that hook and instance-check for this type.
   */
  @Override
  public void accept (final Visitor aVisitor)
  {
    aVisitor.visit (this);
  }
}
