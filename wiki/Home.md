Welcome to the **norma** wiki!

`norma` is a Java library for **visualizing electronic invoices**. It reads invoices in either UBL or CII syntax, lifts them into a syntax-neutral intermediate model that carries every EN 16931 business term (BG/BT), and serializes that model into HTML or PDF — in multiple human languages.

> **Status:** Specification phase. No code has been written yet. This wiki captures the agreed design before implementation begins.

## At a glance

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│ UBL Invoice  │ ──▶ │                  │ ──▶ │ HTML output  │
│ UBL CredNote │     │  NormaInvoice    │     │              │
│ CII D16B     │ ──▶ │  (EN 16931 BGs)  │ ──▶ │ PDF output   │
└──────────────┘     └──────────────────┘     └──────────────┘
   Source readers      Intermediate model         Writers
```

The intermediate model is the heart of the design: every reader populates the same shape; every writer consumes the same shape. New source syntaxes and new output formats can be added without touching either side of the pipeline.

## Table of contents

* [[News and noteworthy]]
* [[Architecture Overview]]
  * [[Domain Model]] — the EN 16931 BGs and BTs `norma` represents
  * [[Source Readers]] — UBL 2.x and CII D16B
  * [[Output Writers]] — HTML and PDF
  * [[Template Language]] — customize the output without writing Java
  * [[Template Processing]] — how a .ntpl flows through the engine
  * [[Localization]] — English, German, and adding more
* [[Watermarking]] — user watermarks and the program/edition watermark
* [[Maven Module Structure]]
* [[Configuration Properties]]
* [[Roadmap]]
* [[Contributing]]

## Scope

**In scope (initial release):**

| Concern | Supported |
|---------|-----------|
| Source syntax | UBL 2.1 Invoice + CreditNote (EN 16931:2017) and CII D16B; UBL 2.5 (EN 16931:2026) planned for 0.3.0 |
| Source validation | XSD only (Schematron is out of scope — use `phive-rules-en16931`) |
| Output format | HTML5, PDF |
| Languages | English (`en`), German (`de`) |
| EN 16931 coverage | All BGs (BG-1..BG-32) and BTs (BT-1..BT-165) |
| Watermarking | User watermark + program/edition watermark (HTML + PDF) — see [[Watermarking]] |
| Customization | Optional `.ntpl` templates (JMustache + CommonMark) — see [[Template Language]]; lands in 0.4.0 |

**Out of scope (initial release):**

- Writing (creating) invoices — `norma` only reads.
- Validation of business rules — use `phorm` based on `phive-rules-en16931` upstream.
- Conversion between syntaxes — use `en16931-cii2ubl` for that.
- XSLT-based visualizations (e.g. ZUGFeRD, Peppol) — `norma` builds output programmatically, not from stylesheets.

## License

Apache License 2.0.
