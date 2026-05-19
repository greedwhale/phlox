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

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-22 DOCUMENT TOTALS (1..1). Carries the full ten-line ladder of monetary totals defined
 * by EN 16931: BT-106 (sum of line net amounts), BT-107 (sum of allowances), BT-108 (sum of
 * charges), BT-109 (total without VAT), BT-110 (total VAT amount), BT-111 (VAT amount in
 * accounting currency), BT-112 (total with VAT), BT-113 (paid amount), BT-114 (rounding
 * amount), BT-115 (amount due for payment).
 *
 * @author Philip Helger
 */
public class NormaDocumentTotals
{
  private BigDecimal m_aLineNetTotal;
  private BigDecimal m_aAllowanceTotal;
  private BigDecimal m_aChargeTotal;
  private BigDecimal m_aTaxExclusiveTotal;
  private BigDecimal m_aTaxTotal;
  private BigDecimal m_aTaxTotalAccountingCurrency;
  private BigDecimal m_aTaxInclusiveTotal;
  private BigDecimal m_aPrepaidAmount;
  private BigDecimal m_aRoundingAmount;
  private BigDecimal m_aPayableAmount;

  /** Create an empty totals record. */
  public NormaDocumentTotals ()
  {}

  /** @return Sum of invoice line net amounts (BT-106). May be {@code null}. */
  @Nullable
  public BigDecimal getLineNetTotal ()
  {
    return m_aLineNetTotal;
  }

  /**
   * Set the sum of invoice line net amounts (BT-106).
   *
   * @param aLineNetTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setLineNetTotal (@Nullable final BigDecimal aLineNetTotal)
  {
    m_aLineNetTotal = aLineNetTotal;
    return this;
  }

  /** @return Sum of allowances on document level (BT-107). May be {@code null}. */
  @Nullable
  public BigDecimal getAllowanceTotal ()
  {
    return m_aAllowanceTotal;
  }

  /**
   * Set the sum of allowances on document level (BT-107).
   *
   * @param aAllowanceTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setAllowanceTotal (@Nullable final BigDecimal aAllowanceTotal)
  {
    m_aAllowanceTotal = aAllowanceTotal;
    return this;
  }

  /** @return Sum of charges on document level (BT-108). May be {@code null}. */
  @Nullable
  public BigDecimal getChargeTotal ()
  {
    return m_aChargeTotal;
  }

  /**
   * Set the sum of charges on document level (BT-108).
   *
   * @param aChargeTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setChargeTotal (@Nullable final BigDecimal aChargeTotal)
  {
    m_aChargeTotal = aChargeTotal;
    return this;
  }

  /** @return Invoice total amount without VAT (BT-109). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxExclusiveTotal ()
  {
    return m_aTaxExclusiveTotal;
  }

  /**
   * Set the invoice total amount without VAT (BT-109).
   *
   * @param aTaxExclusiveTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setTaxExclusiveTotal (@Nullable final BigDecimal aTaxExclusiveTotal)
  {
    m_aTaxExclusiveTotal = aTaxExclusiveTotal;
    return this;
  }

  /** @return Invoice total VAT amount (BT-110). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxTotal ()
  {
    return m_aTaxTotal;
  }

  /**
   * Set the invoice total VAT amount (BT-110).
   *
   * @param aTaxTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setTaxTotal (@Nullable final BigDecimal aTaxTotal)
  {
    m_aTaxTotal = aTaxTotal;
    return this;
  }

  /** @return Invoice total VAT amount in accounting currency (BT-111). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxTotalAccountingCurrency ()
  {
    return m_aTaxTotalAccountingCurrency;
  }

  /**
   * Set the invoice total VAT amount in accounting currency (BT-111).
   *
   * @param aTaxTotalAccountingCurrency
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setTaxTotalAccountingCurrency (@Nullable final BigDecimal aTaxTotalAccountingCurrency)
  {
    m_aTaxTotalAccountingCurrency = aTaxTotalAccountingCurrency;
    return this;
  }

  /** @return Invoice total amount with VAT (BT-112). May be {@code null}. */
  @Nullable
  public BigDecimal getTaxInclusiveTotal ()
  {
    return m_aTaxInclusiveTotal;
  }

  /**
   * Set the invoice total amount with VAT (BT-112).
   *
   * @param aTaxInclusiveTotal
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setTaxInclusiveTotal (@Nullable final BigDecimal aTaxInclusiveTotal)
  {
    m_aTaxInclusiveTotal = aTaxInclusiveTotal;
    return this;
  }

  /** @return Paid amount (BT-113). May be {@code null}. */
  @Nullable
  public BigDecimal getPrepaidAmount ()
  {
    return m_aPrepaidAmount;
  }

  /**
   * Set the paid amount (BT-113).
   *
   * @param aPrepaidAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setPrepaidAmount (@Nullable final BigDecimal aPrepaidAmount)
  {
    m_aPrepaidAmount = aPrepaidAmount;
    return this;
  }

  /** @return Rounding amount (BT-114). May be {@code null}. */
  @Nullable
  public BigDecimal getRoundingAmount ()
  {
    return m_aRoundingAmount;
  }

  /**
   * Set the rounding amount (BT-114).
   *
   * @param aRoundingAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setRoundingAmount (@Nullable final BigDecimal aRoundingAmount)
  {
    m_aRoundingAmount = aRoundingAmount;
    return this;
  }

  /** @return Amount due for payment (BT-115). May be {@code null}. */
  @Nullable
  public BigDecimal getPayableAmount ()
  {
    return m_aPayableAmount;
  }

  /**
   * Set the amount due for payment (BT-115).
   *
   * @param aPayableAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public NormaDocumentTotals setPayableAmount (@Nullable final BigDecimal aPayableAmount)
  {
    m_aPayableAmount = aPayableAmount;
    return this;
  }
}
