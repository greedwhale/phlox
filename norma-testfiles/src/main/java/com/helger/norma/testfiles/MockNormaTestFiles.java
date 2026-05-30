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

  // EN 16931:2017 / CII D16B — core hand-curated examples (in repo from inception)
  private static final String [] CII_D16B_CORE_FILES = { "2017/cii/CII_business_example_01.xml",
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

  // EN 16931:2017 / CII D16B — XRechnung 1.2.2 example corpus
  // (imported from en16931-cii2ubl/src/test/resources/external/cii/xrechnung/1.2.2)
  private static final String [] CII_D16B_XRECHNUNG_122_FILES = { "2017/cii/xrechnung/1.2.2/01.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.05a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.06a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.07a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.08a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.09a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.10a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.11a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.12a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.13a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.14a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/01.15a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/02.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/02.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/02.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/02.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/03.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/1.2.2/03.02a-INVOICE_uncefact.xml" };

  // EN 16931:2017 / CII D16B — XRechnung 2.0.0 example corpus
  // (imported from en16931-cii2ubl/src/test/resources/external/cii/xrechnung/2.0.0)
  private static final String [] CII_D16B_XRECHNUNG_200_FILES = { "2017/cii/xrechnung/2.0.0/01.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.05a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.06a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.07a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.08a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.09a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.10a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.11a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.12a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.13a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.14a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/01.15a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/02.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/02.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/03.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/2.0.0/03.02a-INVOICE_uncefact.xml" };

  // EN 16931:2017 / CII D16B — XRechnung 3.0.2 example corpus
  // (imported from en16931-cii2ubl/src/test/resources/external/cii/xrechnung/3.0.2)
  private static final String [] CII_D16B_XRECHNUNG_302_FILES = { "2017/cii/xrechnung/3.0.2/01.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.05a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.06a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.07a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.08a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.09a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.10a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.11a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.12a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.13a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.14a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.15a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.17a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.18a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.19a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.20a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/01.21a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.05a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/02.06a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.01a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.02a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.03a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.04a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.05a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.06a-INVOICE_uncefact.xml",
                                                                  "2017/cii/xrechnung/3.0.2/03.07a-INVOICE_uncefact.xml" };

  // EN 16931:2017 / CII D16B — comprehensive + minimal stress-test files
  // (imported from en16931-cii2ubl/src/test/resources/external/cii/xrechnung/technical)
  private static final String [] CII_D16B_TECHNICAL_FILES = { "2017/cii/xrechnung/technical/01.01_comprehensive_test_uncefact.xml",
                                                              "2017/cii/xrechnung/technical/01.02_comprehensive_test_uncefact.xml",
                                                              "2017/cii/xrechnung/technical/01.03_comprehensive_test_uncefact.xml",
                                                              "2017/cii/xrechnung/technical/01.04_comprehensive_test_uncefact.xml",
                                                              "2017/cii/xrechnung/technical/01.05_minimal_test_uncefact.xml",
                                                              "2017/cii/xrechnung/technical/01.06_minimal_test_uncefact.xml" };

  // EN 16931:2017 / CII D16B — bug-regression cases
  // (imported from en16931-cii2ubl/src/test/resources/external/cii/issues).
  // Every file here is well-formed CII; the names are upstream issue numbers from the
  // en16931-cii2ubl tracker. The "-bad" suffix on a couple of files reflects a CII→UBL
  // CONVERSION issue in that project — not a CII validation failure — so they parse
  // cleanly through our reader.
  private static final String [] CII_D16B_ISSUE_FILES = { "2017/cii/issues/issue7.xml",
                                                          "2017/cii/issues/issue12.xml",
                                                          "2017/cii/issues/issue20.xml",
                                                          "2017/cii/issues/issue22.xml",
                                                          "2017/cii/issues/issue23.xml",
                                                          "2017/cii/issues/issue28.xml",
                                                          "2017/cii/issues/issue34.xml",
                                                          "2017/cii/issues/issue38-bad.xml",
                                                          "2017/cii/issues/issue47.xml",
                                                          "2017/cii/issues/issue1121.xml" };

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

  /**
   * @return Classpath resources for the core hand-curated EN 16931:2017 CII D16B example files
   *         that have been in the repo since inception.
   */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BCoreFiles ()
  {
    return _toResources (CII_D16B_CORE_FILES);
  }

  /** @return Classpath resources for the XRechnung 1.2.2 example corpus (21 files). */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BXRechnung122Files ()
  {
    return _toResources (CII_D16B_XRECHNUNG_122_FILES);
  }

  /** @return Classpath resources for the XRechnung 2.0.0 example corpus (19 files). */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BXRechnung200Files ()
  {
    return _toResources (CII_D16B_XRECHNUNG_200_FILES);
  }

  /** @return Classpath resources for the XRechnung 3.0.2 example corpus (33 files). */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BXRechnung302Files ()
  {
    return _toResources (CII_D16B_XRECHNUNG_302_FILES);
  }

  /**
   * @return Classpath resources for the technical stress-test CII files — both comprehensive
   *         (every BG/BT populated) and minimal (smallest valid invoice) shapes (6 files).
   */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BTechnicalFiles ()
  {
    return _toResources (CII_D16B_TECHNICAL_FILES);
  }

  /**
   * @return Classpath resources for the bug-regression CII files (10 files). Each file is a
   *         minimal repro for an issue reported against the upstream {@code en16931-cii2ubl}
   *         project. All are well-formed CII; some are flagged "-bad" in the filename because
   *         they expose a CII→UBL conversion bug in that project, not a CII validation
   *         failure.
   */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BIssueFiles ()
  {
    return _toResources (CII_D16B_ISSUE_FILES);
  }

  /**
   * @return Classpath resources for every EN 16931:2017 CII D16B example file — core +
   *         XRechnung 1.2.2 + 2.0.0 + 3.0.2 + technical + issues. All files are well-formed
   *         CII and should round-trip through {@code CIIInvoiceReader} without errors.
   */
  @NonNull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getCIID16BFiles ()
  {
    final ICommonsList <ClassPathResource> aList = new CommonsArrayList <> ();
    aList.addAll (_toResources (CII_D16B_CORE_FILES));
    aList.addAll (_toResources (CII_D16B_XRECHNUNG_122_FILES));
    aList.addAll (_toResources (CII_D16B_XRECHNUNG_200_FILES));
    aList.addAll (_toResources (CII_D16B_XRECHNUNG_302_FILES));
    aList.addAll (_toResources (CII_D16B_TECHNICAL_FILES));
    aList.addAll (_toResources (CII_D16B_ISSUE_FILES));
    return aList;
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
