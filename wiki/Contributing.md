# Contributing

`norma` follows the same conventions as the rest of the Helger Java stack (`ph-commons`, `ph-ubl`, `ph-cii`, `en16931-cii2ubl`, …). Read those projects' code first if you have not already — that is the canonical source for style decisions; this page only highlights the points specific to `norma`.

## Build & test

```bash
mvn clean compile      # Compile all modules
mvn clean test         # Run all unit tests
mvn clean package      # Build all JARs (including norma-cli fat JAR)
```

Requires **Java 17+**.

## Coding style

- **Hungarian notation** for variables (`m_sName`, `m_aList`, `m_nCount`, `s_aSingleton`).
- **Type prefixes:** `I` for interfaces (`IInvoiceReader`), `E` for enums (`ENormaDocumentKind`), `Abstract` for abstract classes.
- **`final` on all parameters.** `@NonNull` / `@Nullable` / `@Nonempty` JSpecify annotations on non-primitive parameters.
- **Private methods** use an underscore prefix: `_walkLines()`.
- **"ID" stays uppercase** in identifiers: `getSellerID()`, not `getSellerId()`.
- **Space before parentheses** in calls, constructors and control flow: `method ()`, `if (cond)`, `for (...)`.
- **Logging:** Inline string concatenation, not SLF4J `{}` placeholders. `LOGGER` (not `s_aLogger`) for the logger field.
- **License header:** Apache 2.0, copyright Philip Helger, at the top of every Java file.

See the project root coding rules for the complete list.

## Where things go

| You want to … | … do this |
|---------------|-----------|
| Add a new BT/BG to the model | Edit `norma-api`, update [[Domain Model]] and the relevant resource bundles. |
| Add a new source syntax | New `norma-<syntax>` module that depends on `norma-api`. Don't touch existing readers. |
| Add a new output format | New `norma-<format>` module that depends on `norma-api`. Don't touch existing writers. |
| Add a new language | Drop two `*_<lang>.properties` files into `norma-api/src/main/resources`. See [[Localization]]. |
| Add a "good case" example invoice | Drop the XML into the right folder in `norma-testfiles/src/main/resources/external/en16931/{2017,2026}/{ubl,cii}/...`, then register it in `MockNormaTestFiles`. Reader tests pick it up automatically. |
| Change layout (e.g. parties side-by-side vs. stacked) | Edit the corresponding `*SectionRenderer` in `norma-html` and `norma-pdf` together — keep them visually consistent. |
| Tweak watermark appearance | Edit the rendering paths in `HTMLInvoiceWriter` (CSS template) and `PDFInvoiceWriter` (page-decorator) together. See [[Watermarking]]. |

## Tests

- **Unit tests:** JUnit 4 (matches the rest of the Helger stack).
- **Reader tests:** Golden-file JSON snapshots of `NormaInvoice` for every source corpus file. Diff failures usually mean an XML-mapping bug.
- **Writer tests:** Golden-file HTML strings for `norma-html`; PDFBox-rendered PNG visual diffs for `norma-pdf`. Re-baseline only on intentional layout changes — review the new image before committing.
- **End-to-end smoke:** Read corpus file → render in EN and DE for both formats → assert no exceptions and non-empty output. Wired into CI.

## PRs

- Don't commit unless explicitly asked.
- Don't push unless explicitly asked.
- Prefer specific `git add` over `git add -A`.
- Add an entry to [[News and noteworthy]] for user-visible changes.

## License

By contributing you agree your work is licensed under the Apache License 2.0.
