# norma

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

See the [wiki](wiki/Home.md) for the full specification, including the [domain model](wiki/Domain-Model.md), [maven module structure](wiki/Maven-Module-Structure.md), and the [roadmap](wiki/Roadmap.md).

## Build

```bash
mvn clean package
```

Requires Java 17+.

## License

Apache License 2.0.
