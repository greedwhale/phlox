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
package com.helger.norma.api;

import java.time.format.FormatStyle;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Optional fine-tuning of locale-driven formatting. Held by {@link RenderContext} and shared
 * across writers.
 *
 * @author Philip Helger
 */
public class NormaFormatOptions
{
  public static final FormatStyle DEFAULT_DATE_STYLE = FormatStyle.MEDIUM;
  public static final int DEFAULT_PERCENT_FRACTION_DIGITS = 2;
  public static final ENegativeStyle DEFAULT_NEGATIVE_STYLE = ENegativeStyle.MINUS_SIGN;

  private FormatStyle m_eDateStyle = DEFAULT_DATE_STYLE;
  private Integer m_aAmountFractionDigits;
  private int m_nPercentFractionDigits = DEFAULT_PERCENT_FRACTION_DIGITS;
  private ENegativeStyle m_eNegativeStyle = DEFAULT_NEGATIVE_STYLE;

  public NormaFormatOptions ()
  {}

  /** @return A new instance with all options at their defaults. */
  @NonNull
  public static NormaFormatOptions defaults ()
  {
    return new NormaFormatOptions ();
  }

  @NonNull
  public FormatStyle getDateStyle ()
  {
    return m_eDateStyle;
  }

  @NonNull
  public NormaFormatOptions setDateStyle (@NonNull final FormatStyle eDateStyle)
  {
    m_eDateStyle = eDateStyle;
    return this;
  }

  @Nullable
  public Integer getAmountFractionDigits ()
  {
    return m_aAmountFractionDigits;
  }

  @NonNull
  public NormaFormatOptions setAmountFractionDigits (@Nullable final Integer aAmountFractionDigits)
  {
    m_aAmountFractionDigits = aAmountFractionDigits;
    return this;
  }

  public int getPercentFractionDigits ()
  {
    return m_nPercentFractionDigits;
  }

  @NonNull
  public NormaFormatOptions setPercentFractionDigits (final int nPercentFractionDigits)
  {
    m_nPercentFractionDigits = nPercentFractionDigits;
    return this;
  }

  @NonNull
  public ENegativeStyle getNegativeStyle ()
  {
    return m_eNegativeStyle;
  }

  @NonNull
  public NormaFormatOptions setNegativeStyle (@NonNull final ENegativeStyle eNegativeStyle)
  {
    m_eNegativeStyle = eNegativeStyle;
    return this;
  }
}
