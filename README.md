# phlox

Visualizer for EN 16931 electronic invoices.

Reads invoices in **UBL** or **CII** syntax, lifts them into a syntax-neutral intermediate model, and renders the result to **HTML** or **PDF** in multiple human languages.

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│ UBL Invoice  │ ──▶ │                  │ ──▶ │ HTML output  │
│ UBL CredNote │     │  InvoiceModel    │     │              │
│ CII D16B     │ ──▶ │  (EN 16931 BGs)  │ ──▶ │ PDF output   │
└──────────────┘     └──────────────────┘     └──────────────┘
   Source readers      Intermediate model         Writers
```

See the [wiki](https://github.com/phax/phlox/wiki/Home) for the full specification, including the [domain model](https://github.com/phax/phlox/wiki/Domain-Model), [maven module structure](https://github.com/phax/phlox/wiki/Maven-Module-Structure), and the [roadmap](https://github.com/phax/phlox/wiki/Roadmap).

## Build

```bash
mvn clean package
```

Requires Java 17+.

## License

Apache License 2.0.
