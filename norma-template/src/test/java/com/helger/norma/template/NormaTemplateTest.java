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
package com.helger.norma.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.norma.api.AbstractRenderContext;
import com.helger.norma.api.i18n.INormaLabelResolver;
import com.helger.norma.api.model.NormaInvoice;
import com.helger.norma.api.model.NormaInvoiceLine;
import com.helger.norma.api.model.NormaItem;
import com.helger.norma.api.model.NormaLineVAT;
import com.helger.norma.api.model.NormaPriceDetails;
import com.helger.norma.api.model.NormaVATBreakdown;
import com.helger.norma.template.ast.NormaTemplateAST;

/**
 * Smoke test: compile {@code default.ntpl}, render a minimal invoice through the full
 * substitute → parse → post-pass pipeline, and assert the result is structurally sane.
 */
public final class NormaTemplateTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (NormaTemplateTest.class);

  /** Concrete subclass purely so the abstract base can be instantiated in this test. */
  private static final class TestCtx extends AbstractRenderContext <TestCtx>
  {}

  @Test
  public void testRenderASTAgainstDefaultTemplate ()
  {
    final NormaInvoice aInvoice = _buildMinimalInvoice ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);

    final NormaTemplate aTpl = NormaTemplate.getDefault ();
    final NormaTemplateAST aAST = aTpl.renderAST (aInvoice, aCtx);

    assertNotNull (aAST);
    assertNotNull (aAST.getRoot ());

    // The default template opens with `# {{label:section.header.title.invoice}}: {{BT-1}}`,
    // so the very first child block must be a level-1 Heading.
    Node aFirstHeading = null;
    for (Node n = aAST.getRoot ().getFirstChild (); n != null; n = n.getNext ())
    {
      if (n instanceof Heading)
      {
        aFirstHeading = n;
        break;
      }
    }
    assertNotNull ("expected at least one Heading in the rendered AST", aFirstHeading);
    assertEquals (1, ((Heading) aFirstHeading).getLevel ());
  }

  @Test
  public void testSubstitutionContainsInvoiceNumberAndSellerName ()
  {
    final NormaInvoice aInvoice = _buildMinimalInvoice ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);

    final NormaTemplate aTpl = NormaTemplate.getDefault ();
    final String sCommonMark = aTpl.renderSubstituted (aInvoice, aCtx);
    LOGGER.debug ("Substituted CommonMark (" + sCommonMark.length () + " chars)");

    assertTrue ("expected INV-001 in output", sCommonMark.contains ("INV-001"));
    assertTrue ("expected seller name in output", sCommonMark.contains ("ACME Sellers Inc."));
    assertTrue ("expected buyer name in output", sCommonMark.contains ("Smith Co."));
    // The synthesized boolean `logo` is false → the attachment:logo line must be absent.
    assertTrue ("expected no attachment:logo when no logo set",
                !sCommonMark.contains ("attachment:logo"));
  }

  @Test
  public void testFromStringSimpleTemplateRoundTrips ()
  {
    final NormaInvoice aInvoice = _buildMinimalInvoice ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);

    final NormaTemplate aTpl = NormaTemplate.fromString ("# {{BT-1}}\n\n**Seller:** {{BG-4.BT-27}}\n");
    final String sOut = aTpl.renderSubstituted (aInvoice, aCtx);
    assertTrue ("expected '# INV-001' in: " + sOut, sOut.contains ("# INV-001"));
    assertTrue ("expected seller line in: " + sOut, sOut.contains ("**Seller:** ACME Sellers Inc."));
  }

  @Test
  public void testPresenceCheckSkipsEmptyOptional ()
  {
    // BT-10 (buyer reference) is NOT set on the minimal invoice.
    final NormaInvoice aInvoice = _buildMinimalInvoice ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);

    final NormaTemplate aTpl = NormaTemplate.fromString ("Before{{?BT-10}}HIT{{/BT-10}}After");
    assertEquals ("BeforeAfter", aTpl.renderSubstituted (aInvoice, aCtx));

    final NormaTemplate aTpl2 = NormaTemplate.fromString ("Before{{?BT-1}}HIT{{/BT-1}}After");
    assertEquals ("BeforeHITAfter", aTpl2.renderSubstituted (aInvoice, aCtx));
  }

  @Test
  public void testAmountModifierUsesInvoiceCurrency ()
  {
    final NormaInvoice aInvoice = _buildMinimalInvoice ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.US);

    final NormaTemplate aTpl = NormaTemplate.fromString ("Due: {{BG-22.BT-115 | amount}}");
    final String sOut = aTpl.renderSubstituted (aInvoice, aCtx);
    // EUR + US locale → "€119.00" or "EUR119.00" depending on JVM tables. Either prefix
    // is acceptable; assert just that the numeric portion appears.
    assertTrue ("expected '119.00' in: " + sOut, sOut.contains ("119.00"));
  }

  @Test
  public void testLocalizeCachesAcrossRenders ()
  {
    // A counting resolver records every label lookup. With caching working, all label lookups
    // happen exactly once per locale at the first localize(...) call; subsequent renders in
    // the same locale must not touch the resolver.
    final CountingResolver aSpy = new CountingResolver ();
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);
    aCtx.setLabelResolver (aSpy);

    final NormaTemplate aTpl = NormaTemplate.fromString ("# {{label:section.header.title.invoice}}: {{BT-1}}");
    final NormaInvoice aInvoice = _buildMinimalInvoice ();

    aTpl.renderSubstituted (aInvoice, aCtx);
    final int nAfterFirst = aSpy.count ();
    assertTrue ("expected at least one label lookup on first render", nAfterFirst >= 1);

    aTpl.renderSubstituted (aInvoice, aCtx);
    aTpl.renderSubstituted (aInvoice, aCtx);
    assertEquals ("label resolver must not be invoked on cache hit", nAfterFirst, aSpy.count ());
  }

  @Test
  public void testLocalizeIsSameInstanceForSameKey ()
  {
    final NormaTemplate aTpl = NormaTemplate.fromString ("# {{label:section.header.title.invoice}}: {{BT-1}}");
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);

    final NormaLocalizedTemplate aFirst = aTpl.localize (aCtx);
    final NormaLocalizedTemplate aSecond = aTpl.localize (aCtx);
    assertSame ("cache should return identical NormaLocalizedTemplate for same key", aFirst, aSecond);
  }

  @Test
  public void testLocalizeBakedSourceHasLabelsResolved ()
  {
    // Labels are pre-substituted in stage 0 — the locale-baked source contains no {{label:...}}.
    final NormaTemplate aTpl = NormaTemplate.fromString ("# {{label:section.header.title.invoice}}: {{BT-1}}");
    final TestCtx aCtx = new TestCtx ();
    aCtx.setLocale (Locale.ENGLISH);
    final NormaLocalizedTemplate aLocalized = aTpl.localize (aCtx);
    assertTrue ("locale-baked source must not contain any {{label:...}} markers",
                !aLocalized.getLocaleBakedSource ().contains ("{{label:"));
  }

  /** Spy resolver that counts label lookups. */
  private static final class CountingResolver implements INormaLabelResolver
  {
    private final AtomicInteger m_aLabelCount = new AtomicInteger ();
    private final AtomicInteger m_aCodeCount = new AtomicInteger ();

    @Override
    public String getLabel (final String sKey, final Locale aLocale)
    {
      m_aLabelCount.incrementAndGet ();
      return "L(" + sKey + ")";
    }

    @Override
    public String getCodeLabel (final String sListName, final String sCode, final Locale aLocale)
    {
      m_aCodeCount.incrementAndGet ();
      return sCode;
    }

    int count ()
    {
      return m_aLabelCount.get ();
    }
  }

  // ─────────────────────────────────────────────────────────────────────
  // Fixture builder
  // ─────────────────────────────────────────────────────────────────────

  private static NormaInvoice _buildMinimalInvoice ()
  {
    final NormaInvoice aInv = new NormaInvoice ();
    aInv.setNumber ("INV-001");
    aInv.setIssueDate (LocalDate.of (2026, 3, 15));
    aInv.setTypeCode ("380");
    aInv.setCurrency ("EUR");

    aInv.getSeller ().setName ("ACME Sellers Inc.");
    aInv.getBuyer ().setName ("Smith Co.");

    aInv.getDocumentTotals ().setLineNetTotal (new BigDecimal ("100.00"));
    aInv.getDocumentTotals ().setTaxExclusiveTotal (new BigDecimal ("100.00"));
    aInv.getDocumentTotals ().setTaxTotal (new BigDecimal ("19.00"));
    aInv.getDocumentTotals ().setTaxInclusiveTotal (new BigDecimal ("119.00"));
    aInv.getDocumentTotals ().setPayableAmount (new BigDecimal ("119.00"));

    final NormaVATBreakdown aVAT = new NormaVATBreakdown ();
    aVAT.setCategoryCode ("S");
    aVAT.setRate (new BigDecimal ("19"));
    aVAT.setTaxableAmount (new BigDecimal ("100.00"));
    aVAT.setTaxAmount (new BigDecimal ("19.00"));
    aInv.vatBreakdowns ().add (aVAT);

    final NormaInvoiceLine aLine = new NormaInvoiceLine ();
    aLine.setID ("1");
    aLine.setQuantity (new BigDecimal ("2"));
    aLine.setQuantityUnit ("EA");
    aLine.setNetAmount (new BigDecimal ("100.00"));
    final NormaItem aItem = new NormaItem ();
    aItem.setName ("Widget");
    aLine.setItem (aItem);
    final NormaPriceDetails aPrice = new NormaPriceDetails ();
    aPrice.setNetPrice (new BigDecimal ("50.00"));
    aLine.setPrice (aPrice);
    final NormaLineVAT aLineVAT = new NormaLineVAT ();
    aLineVAT.setCategoryCode ("S");
    aLineVAT.setRate (new BigDecimal ("19"));
    aLine.setLineVAT (aLineVAT);
    aInv.lines ().add (aLine);

    return aInv;
  }
}
