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
package com.helger.norma.api.model;

/**
 * Kind of EN 16931 document represented by a {@link NormaInvoice}. Derived from BT-3 (Invoice type
 * code) via the EN 16931 invoice/credit-note type-code lists.
 *
 * @author Philip Helger
 */
public enum ENormaDocumentKind
{
  /** A regular invoice (BT-3 in the EN 16931 invoice type code set). */
  INVOICE,
  /** A credit note (BT-3 in the EN 16931 credit-note type code set). */
  CREDIT_NOTE;

  public boolean isInvoice ()
  {
    return this == INVOICE;
  }

  public boolean isCreditNote ()
  {
    return this == CREDIT_NOTE;
  }
}
