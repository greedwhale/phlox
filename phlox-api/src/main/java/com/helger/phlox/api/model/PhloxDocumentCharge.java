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
package com.helger.phlox.api.model;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * BG-21 DOCUMENT LEVEL CHARGE (0..n; "charges and taxes" since EN 16931:2026). Carries BT-99
 * (charge amount), BT-100 (base amount), BT-101 (percentage), BT-102 (VAT category code), BT-103
 * (VAT rate), BT-104 (reason text) and BT-105 (reason code). Since EN 16931:2026 also BT-175 /
 * BT-176 (exemption reason text and code), BT-177 / BT-177-1 (non-VAT tax code and its list
 * identifier) and BT-214 (goods/services code). Mirror of {@link PhloxDocumentAllowance}.
 *
 * @author Philip Helger
 */
public class PhloxDocumentCharge
{
  private BigDecimal m_aAmount;
  private BigDecimal m_aBaseAmount;
  private BigDecimal m_aPercentage;
  private String m_sVatCategoryCode;
  private BigDecimal m_aVatRate;
  private String m_sReason;
  private String m_sReasonCode;
  private String m_sExemptionReasonText;
  private String m_sExemptionReasonCode;
  private String m_sNonVatTaxCode;
  private String m_sNonVatTaxCodeListID;
  private String m_sGoodsServicesCode;

  /** Create an empty document-level charge. */
  public PhloxDocumentCharge ()
  {}

  /** @return Charge amount (BT-99). May be {@code null}. */
  @Nullable
  public BigDecimal getAmount ()
  {
    return m_aAmount;
  }

  /**
   * Set the charge amount (BT-99).
   *
   * @param aAmount
   *        Amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setAmount (@Nullable final BigDecimal aAmount)
  {
    m_aAmount = aAmount;
    return this;
  }

  /** @return Charge base amount (BT-100). May be {@code null}. */
  @Nullable
  public BigDecimal getBaseAmount ()
  {
    return m_aBaseAmount;
  }

  /**
   * Set the charge base amount (BT-100).
   *
   * @param aBaseAmount
   *        Base amount; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setBaseAmount (@Nullable final BigDecimal aBaseAmount)
  {
    m_aBaseAmount = aBaseAmount;
    return this;
  }

  /** @return Charge percentage (BT-101). May be {@code null}. */
  @Nullable
  public BigDecimal getPercentage ()
  {
    return m_aPercentage;
  }

  /**
   * Set the charge percentage (BT-101).
   *
   * @param aPercentage
   *        Percentage as a {@link BigDecimal} (e.g. {@code 5} for 5 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setPercentage (@Nullable final BigDecimal aPercentage)
  {
    m_aPercentage = aPercentage;
    return this;
  }

  /** @return VAT category code (BT-102) — UNTDID 5305. May be {@code null}. */
  @Nullable
  public String getVatCategoryCode ()
  {
    return m_sVatCategoryCode;
  }

  /**
   * Set the VAT category code (BT-102).
   *
   * @param sVatCategoryCode
   *        UNTDID 5305 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setVatCategoryCode (@Nullable final String sVatCategoryCode)
  {
    m_sVatCategoryCode = sVatCategoryCode;
    return this;
  }

  /** @return VAT rate (BT-103). May be {@code null}. */
  @Nullable
  public BigDecimal getVatRate ()
  {
    return m_aVatRate;
  }

  /**
   * Set the VAT rate (BT-103).
   *
   * @param aVatRate
   *        Rate as a {@link BigDecimal} (e.g. {@code 19} for 19 %); may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setVatRate (@Nullable final BigDecimal aVatRate)
  {
    m_aVatRate = aVatRate;
    return this;
  }

  /** @return Charge reason text (BT-104). May be {@code null}. */
  @Nullable
  public String getReason ()
  {
    return m_sReason;
  }

  /**
   * Set the charge reason text (BT-104).
   *
   * @param sReason
   *        Reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setReason (@Nullable final String sReason)
  {
    m_sReason = sReason;
    return this;
  }

  /** @return Charge reason code (BT-105) — UNTDID 7161. May be {@code null}. */
  @Nullable
  public String getReasonCode ()
  {
    return m_sReasonCode;
  }

  /**
   * Set the charge reason code (BT-105).
   *
   * @param sReasonCode
   *        UNTDID 7161 code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setReasonCode (@Nullable final String sReasonCode)
  {
    m_sReasonCode = sReasonCode;
    return this;
  }

  /** @return Document level charge or tax exemption reason text (BT-175). May be {@code null}. */
  @Nullable
  public String getExemptionReasonText ()
  {
    return m_sExemptionReasonText;
  }

  /**
   * Set the document level charge or tax exemption reason text (BT-175).
   *
   * @param sExemptionReasonText
   *        Document level charge or tax exemption reason text; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setExemptionReasonText (@Nullable final String sExemptionReasonText)
  {
    m_sExemptionReasonText = sExemptionReasonText;
    return this;
  }

  /** @return VAT exemption reason and specification code of the document level charge or tax (BT-176). May be {@code null}. */
  @Nullable
  public String getExemptionReasonCode ()
  {
    return m_sExemptionReasonCode;
  }

  /**
   * Set the VAT exemption reason and specification code of the document level charge or tax (BT-176).
   *
   * @param sExemptionReasonCode
   *        VAT exemption reason and specification code of the document level charge or tax; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setExemptionReasonCode (@Nullable final String sExemptionReasonCode)
  {
    m_sExemptionReasonCode = sExemptionReasonCode;
    return this;
  }

  /** @return Document level non-VAT tax code (BT-177). May be {@code null}. */
  @Nullable
  public String getNonVatTaxCode ()
  {
    return m_sNonVatTaxCode;
  }

  /**
   * Set the document level non-VAT tax code (BT-177).
   *
   * @param sNonVatTaxCode
   *        Document level non-VAT tax code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setNonVatTaxCode (@Nullable final String sNonVatTaxCode)
  {
    m_sNonVatTaxCode = sNonVatTaxCode;
    return this;
  }

  /** @return Document level non-VAT tax code list identifier (BT-177-1). May be {@code null}. */
  @Nullable
  public String getNonVatTaxCodeListID ()
  {
    return m_sNonVatTaxCodeListID;
  }

  /**
   * Set the document level non-VAT tax code list identifier (BT-177-1).
   *
   * @param sNonVatTaxCodeListID
   *        Document level non-VAT tax code list identifier; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setNonVatTaxCodeListID (@Nullable final String sNonVatTaxCodeListID)
  {
    m_sNonVatTaxCodeListID = sNonVatTaxCodeListID;
    return this;
  }

  /** @return Document level charge goods/services code (BT-214). May be {@code null}. */
  @Nullable
  public String getGoodsServicesCode ()
  {
    return m_sGoodsServicesCode;
  }

  /**
   * Set the document level charge goods/services code (BT-214).
   *
   * @param sGoodsServicesCode
   *        Document level charge goods/services code; may be {@code null}.
   * @return this for chaining.
   */
  @NonNull
  public PhloxDocumentCharge setGoodsServicesCode (@Nullable final String sGoodsServicesCode)
  {
    m_sGoodsServicesCode = sGoodsServicesCode;
    return this;
  }
}
