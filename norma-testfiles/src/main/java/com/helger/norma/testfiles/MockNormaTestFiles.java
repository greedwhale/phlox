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
package com.helger.norma.testfiles;

import org.jspecify.annotations.NonNull;

import com.helger.annotation.style.ReturnsMutableCopy;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.io.resource.ClassPathResource;

/**
 * Lookup of curated EN 16931 example invoices used by reader and writer tests in this project.
 * Files are stored under {@code external/en16931/&lt;edition&gt;/&lt;syntax&gt;/...} in the
 * classpath of this module.
 *
 * @author Philip Helger
 */
public final class MockNormaTestFiles
{
  /** Base path inside the JAR for all EN 16931 examples. */
  public static final String BASE_PATH = "external/en16931/";

  // EN 16931:2017 / UBL 2.1 — Invoices
  private static final String [] UBL21_INVOICE_FILES = { "2017/ubl/invoice/guide-example1.xml",
                                                         "2017/ubl/invoice/guide-example2.xml",
                                                         "2017/ubl/invoice/guide-example3.xml",
                                                         "2017/ubl/invoice/peppol-Allowance-example.xml",
                                                         "2017/ubl/invoice/peppol-base-example.xml",
                                                         "2017/ubl/invoice/peppol-base-negative-inv-correction.xml",
                                                         "2017/ubl/invoice/peppol-sales-order-example.xml",
                                                         "2017/ubl/invoice/peppol-vat-category-E.xml",
                                                         "2017/ubl/invoice/peppol-vat-category-O.xml",
                                                         "2017/ubl/invoice/peppol-Vat-category-S.xml",
                                                         "2017/ubl/invoice/peppol-vat-category-Z.xml",
                                                         "2017/ubl/invoice/sample-discount-price.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example1.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example2.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example3.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example4.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example5.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example6.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example7.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example8.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example9.xml",
                                                         "2017/ubl/invoice/ubl-tc434-example10.xml" };

  // EN 16931:2017 / UBL 2.1 — Credit Notes
  private static final String [] UBL21_CREDITNOTE_FILES = { "2017/ubl/creditnote/peppol-base-creditnote-correction.xml",
                                                            "2017/ubl/creditnote/ubl-tc434-creditnote1.xml" };

  // EN 16931:2017 / CII D16B
  private static final String [] CII_D16B_FILES = { "2017/cii/CII_business_example_01.xml",
                                                    "2017/cii/CII_business_example_02.xml",
                                                    "2017/cii/CII_business_example_Z.xml",
                                                    "2017/cii/CII_example1-CreditNote.xml",
                                                    "2017/cii/CII_example1.xml",
                                                    "2017/cii/CII_example1a.xml",
                                                    "2017/cii/CII_example2.xml",
                                                    "2017/cii/CII_example3.xml",
                                                    "2017/cii/CII_example4.xml",
                                                    "2017/cii/CII_example5.xml",
                                                    "2017/cii/CII_example6.xml",
                                                    "2017/cii/CII_example7.xml",
                                                    "2017/cii/CII_example8.xml",
                                                    "2017/cii/CII_example9.xml",
                                                    "2017/cii/XRechnung-O.xml" };

  private MockNormaTestFiles ()
  {}

  /** @return Classpath resources for all EN 16931:2017 UBL 2.1 Invoice example files. */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getUBL21InvoiceFiles ()
  {
    return _toResources (UBL21_INVOICE_FILES);
  }

  /** @return Classpath resources for all EN 16931:2017 UBL 2.1 CreditNote example files. */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getUBL21CreditNoteFiles ()
  {
    return _toResources (UBL21_CREDITNOTE_FILES);
  }

  /** @return Classpath resources for all UBL 2.1 example files (Invoice and CreditNote). */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getAllUBL21Files ()
  {
    final ICommonsList <ClassPathResource> aList = _toResources (UBL21_INVOICE_FILES);
    aList.addAll (_toResources (UBL21_CREDITNOTE_FILES));
    return aList;
  }

  /** @return Classpath resources for all EN 16931:2017 CII D16B example files. */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BFiles ()
  {
    return _toResources (CII_D16B_FILES);
  }

  @NonNull
  private static ICommonsList <ClassPathResource> _toResources (@NonNull final String [] aRelativePaths)
  {
    final ICommonsList <ClassPathResource> aList = new CommonsArrayList <> (aRelativePaths.length);
    for (final String sRelative : aRelativePaths)
      aList.add (new ClassPathResource (BASE_PATH + sRelative));
    return aList;
  }
}
