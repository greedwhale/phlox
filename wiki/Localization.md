# Localization

The intermediate model carries **no human text** — only EN 16931 identifiers and raw values. Writers translate those identifiers into a chosen language at render time. The same translation path is exposed to custom templates through the `{{label:...}}` placeholder — see [[Template Language]] — so a single language-independent template renders correctly in every supported locale.

## Supported languages

| Locale | Status | Resource bundle |
|--------|--------|-----------------|
| `en` (English) | First class | `norma-labels_en.properties` |
| `de` (German) | First class | `norma-labels_de.properties` |
| anything else | Falls back to `en` | — |

Adding a language is a matter of dropping a translated properties file onto the classpath. No code changes.

## Resource bundle structure

There are **two** bundles, both packaged in `norma-api`:

### `norma-labels` — section titles and BG/BT labels

Keyed by EN 16931 identifier:

```properties
# norma-labels_en.properties
BT-1   = Invoice number
BT-2   = Invoice issue date
BT-5   = Currency
BG-4   = Seller
BG-7   = Buyer
BG-22  = Document totals
BG-25  = Invoice lines
# ... one entry per BG and per BT used by writers
```

```properties
# norma-labels_de.properties
BT-1   = Rechnungsnummer
BT-2   = Rechnungsdatum
BT-5   = Währung
BG-4   = Verkäufer
BG-7   = Käufer
BG-22  = Dokumentensummen
BG-25  = Rechnungspositionen
```

Plus a small set of synthetic keys for things that don't correspond to a single BT:

```properties
section.header.title.invoice      = Invoice
section.header.title.creditnote   = Credit Note
section.parties.title             = Parties
section.references.title          = References
section.payment.title             = Payment
section.notes.title               = Notes
section.lines.column.number       = #
section.lines.column.description  = Description
section.lines.column.quantity     = Qty
section.lines.column.unitprice    = Unit price
section.lines.column.linetotal    = Line total
section.totals.subtotal           = Subtotal
section.totals.vat                = VAT
section.totals.total              = Total
section.totals.due                = Amount due
```

### `norma-codelists` — UNTDID / VAT category / payment-means code descriptions

Keyed by `<listname>.<code>`:

```properties
# norma-codelists_en.properties
vat.category.S    = Standard rated
vat.category.Z    = Zero rated
vat.category.E    = Exempt from VAT
vat.category.AE   = VAT Reverse Charge
vat.category.K    = VAT exempt for EEA intra-community supply of goods and services
vat.category.G    = Free export item, VAT not charged
vat.category.O    = Services outside scope of tax
vat.category.L    = Canary Islands general indirect tax
vat.category.M    = Tax for production, services and importation in Ceuta and Melilla
vat.category.B    = Transferred (VAT), In Italy

paymentmeans.10   = In cash
paymentmeans.20   = Cheque
paymentmeans.30   = Credit transfer
paymentmeans.42   = Payment to bank account
paymentmeans.48   = Bank card
paymentmeans.49   = Direct debit
paymentmeans.57   = Standing agreement
paymentmeans.58   = SEPA credit transfer
paymentmeans.59   = SEPA direct debit
paymentmeans.97   = Clearing between partners
# ... continued
```

The full list of values is sourced from the **UNTDID 4461** (payment means), **UNTDID 5305** (VAT category), and **UNTDID 1153** (reference type) code lists used by EN 16931.

## API

```java
package com.helger.norma.api.i18n;

public interface INormaLabelResolver
{
  /** Looks up a section/BG/BT label. Returns the key itself on miss. */
  @Nonnull
  String getLabel (@Nonnull String sKey, @Nonnull Locale aLocale);

  /** Looks up a code-list value description. Returns the code itself on miss. */
  @Nonnull
  String getCodeLabel (@Nonnull String sListName, @Nonnull String sCode, @Nonnull Locale aLocale);
}

public final class DefaultNormaLabelResolver implements INormaLabelResolver
{
  // ResourceBundle-backed; used by both writers when none is supplied.
}
```

A caller who needs labels for branded terminology (e.g. "Invoice #" instead of "Invoice number") can supply a custom `INormaLabelResolver` through `RenderContext.setLabelResolver (...)` without rewriting any rendering code.

## Fallback rules

| Situation | Behaviour |
|-----------|-----------|
| Locale not present in bundle | `ResourceBundle.getBundle` falls back through the standard parent chain (`de_AT` → `de` → `en`). |
| Key missing in selected language but present in `en` | Use the `en` value. |
| Key missing everywhere | Return the key (`"BT-99"`) so the gap is visible in output. Log once at WARN. |

## Date and number formatting

Locale also drives:

- `DateTimeFormatter.ofLocalizedDate (FormatStyle.MEDIUM).withLocale (locale)` for all dates.
- `NumberFormat.getCurrencyInstance (locale)` for amounts, **but** the actual currency comes from `BT-5` (not from the locale), so the formatter is configured per-call with `setCurrency(...)`.
- `NumberFormat.getPercentInstance (locale)` for percentages.

These are the only locale-sensitive operations the writers perform.

## Adding a new language

1. Translate `norma-labels_en.properties` to `norma-labels_<lang>.properties`.
2. Translate `norma-codelists_en.properties` to `norma-codelists_<lang>.properties`.
3. Drop both onto the classpath — no other change is necessary.
4. Pass `new RenderContext().setLocale(new Locale("<lang>"))` to the writer.

Adding a language inside the project (so it ships in the JAR) is a documentation-only PR — the build picks up the new files automatically.
