# Localization

The intermediate model carries **no human text** — only EN 16931 identifiers and raw values. Writers translate those identifiers into a chosen language at render time. The same translation path is exposed to custom templates through the `{{label:...}}` placeholder — see [[Template Language]] — so a single language-independent template renders correctly in every supported locale.

## Supported languages

| Locale | Status | Resource bundle |
|--------|--------|-----------------|
| `en` (English) | First class | `phlox-labels_en.properties` |
| `de` (German) | First class | `phlox-labels_de.properties` |
| anything else | Falls back to `en` | — |

Adding a language is a matter of dropping a translated properties file onto the classpath. No code changes.

## Resource bundle structure

There are **two** bundles, both packaged in `phlox-api`:

### `phlox-labels` — section titles and BG/BT labels

Keyed by EN 16931 identifier:

```properties
# phlox-labels_en.properties
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
# phlox-labels_de.properties
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

### `phlox-codelists` — UNTDID / VAT category / payment-means code descriptions

Keyed by `<listname>.<code>`:

```properties
# phlox-codelists_en.properties
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

This bundle only holds the code lists that are translated into German here: **UNTDID 1001** (document type, `doctype`), **UNTDID 4461** (payment means, `paymentmeans`) and **UNTDID 5305** (VAT category, `vatcategory`). Every other code list is resolved through the chain below.

## API

```java
package com.helger.phlox.api.i18n;

public interface IPhloxLabelResolver
{
  /** Looks up a section/BG/BT label. Returns the key itself on miss. */
  @Nonnull
  String getLabel (@Nonnull String sKey, @Nonnull Locale aLocale);

  /** Looks up a code-list value description. Returns the code itself on miss. */
  @Nonnull
  String getCodeLabel (@Nonnull String sListName, @Nonnull String sCode, @Nonnull Locale aLocale);
}

public final class DefaultPhloxLabelResolver implements IPhloxLabelResolver
{
  // ResourceBundle-backed; used by both writers when none is supplied.
}
```

A caller who needs labels for branded terminology (e.g. "Invoice #" instead of "Invoice number") can supply a custom `IPhloxLabelResolver` through `RenderContext.setLabelResolver (...)` without rewriting any rendering code.

## Code-list lookup chain

`DefaultPhloxLabelResolver.getCodeLabel (list, code, locale)` tries, in this order:

| Step | Source | Covers |
|------|--------|--------|
| 1 | `phlox-codelists_<lang>.properties` (key `<list>.<code>`; a dotted list name such as `vat.category` is the same as `vatcategory`) | the translated lists above |
| 2 | The JDK, in the language of the locale | `country` (e.g. `AT` → "Österreich" / "Austria") and `currency` |
| 3 | The English names of the EN 16931 code lists in [`en16931-basics`](https://github.com/phax/en16931-basics) (`EEN16931*Code`) | `allowancereason`, `chargereason`, `doctype`, `eas`, `icd`, `itemtype`, `notesubject`, `paymentmeans`, `referencequalifier`, `vatcategory`, `vatexemption` (VATEX) and the country and currency lists |
| 4 | The code itself, with a WARN in the log | everything else, e.g. the UN/ECE Recommendation 20 units (`unit`) |

`en16931-basics` only carries English names, so in German output those code lists show the English text until a German translation is added to the German bundle (step 1 wins).

## Label policy for the German bundle

The German BG/BT labels follow the German draft of EN 16931-1. Terms that the draft does not contain (BG-39, BT-215 to BT-220, the scheme and list identifier sub-terms BT-167-1/-2, BT-177-1, BT-193-1) are proposals that follow the wording of related terms (for example *Vorhergehende Rechnungsreferenz* for BG-3, *Zahlungsdienstleister* and *Belastetes Konto* for BG-19, *Auftragsreferenz* for sales orders). Replace them when the official German text is published.

## Fallback rules

| Situation | Behaviour |
|-----------|-----------|
| Locale not present in bundle | `ResourceBundle.getBundle` falls back through the standard parent chain (`de_AT` → `de` → `en`). |
| Key missing in selected language but present in `en` | Use the `en` value. |
| Key missing everywhere | Return the key (`"BT-99"`) so the gap is visible in output. Log once at WARN. |
| Code missing in the code-list bundle | Continue with the chain above; only a code that no step knows is returned as is. |

## Date and number formatting

Locale also drives:

- `DateTimeFormatter.ofLocalizedDate (FormatStyle.MEDIUM).withLocale (locale)` for all dates.
- `NumberFormat.getCurrencyInstance (locale)` for amounts, **but** the actual currency comes from `BT-5` (not from the locale), so the formatter is configured per-call with `setCurrency(...)`.
- `NumberFormat.getPercentInstance (locale)` for percentages.

These are the only locale-sensitive operations the writers perform.

## Adding a new language

1. Translate `phlox-labels_en.properties` to `phlox-labels_<lang>.properties`.
2. Translate `phlox-codelists_en.properties` to `phlox-codelists_<lang>.properties`.
3. Drop both onto the classpath — no other change is necessary.
4. Pass `new RenderContext().setLocale(new Locale("<lang>"))` to the writer.

Adding a language inside the project (so it ships in the JAR) is a documentation-only PR — the build picks up the new files automatically.
