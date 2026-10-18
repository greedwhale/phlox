# Contributing

`phlox` follows the same conventions as the rest of the Helger Java stack (`ph-commons`, `ph-ubl`, `ph-cii`, `en16931-cii2ubl`, …). Read those projects' code first if you have not already — that is the canonical source for style decisions; this page only highlights the points specific to `phlox`.

## Build & test

```bash
mvn clean compile      # Compile all modules
mvn clean test         # Run all unit tests
mvn clean package      # Build all JARs (including phlox-cli fat JAR)
```

Requires **Java 17+**. The code is compiled with `release 17`.

## Coding style

- **Hungarian notation** for variables (`m_sName`, `m_aList`, `m_nCount`, `s_aSingleton`).
- **Type prefixes:** `I` for interfaces (`IInvoiceReader`), `E` for enums (`EPhloxDocumentKind`), `Abstract` for abstract classes.
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
| Add a new BT/BG to the model | Edit `phlox-api`, update [[Domain Model]] and the relevant resource bundles (EN and DE). Then show it in `HtmlInvoiceWriter`, `PdfInvoiceWriter` **and** the template path (`PhloxModelRegistry`, `default.ptpl`), and fill it in the readers of both editions. |
| Add a new source syntax | New `phlox-<syntax>` module that depends on `phlox-api`. Don't touch existing readers. |
| Add a new output format | New `phlox-<format>` module that depends on `phlox-api`. Don't touch existing writers. |
| Add a new language | Drop two `*_<lang>.properties` files into `phlox-api/src/main/resources`. See [[Localization]]. |
| Add a "good case" example invoice | Drop the XML into the right folder in `phlox-testfiles/src/main/resources/external/en16931/{2017,2026}/{ubl,cii}/...`, then register it in `MockPhloxTestFiles`. Reader tests pick it up automatically. |
| Change layout (e.g. parties side-by-side vs. stacked) | Edit the corresponding `*SectionRenderer` in `phlox-html` and `phlox-pdf` together — keep them visually consistent. |
| Tweak watermark appearance | Edit the rendering paths in `HtmlInvoiceWriter` (CSS template) and `PdfInvoiceWriter` (page-decorator) together. See [[Watermarking]]. |

## Tests

- **Unit tests:** JUnit 4 (matches the rest of the Helger stack).
- **Reader tests:** every example file of the corpus must parse without errors; the 2026 terms are spot-checked on the `d25a-new-*` files.
- **Writer tests:** corpus smoke tests for HTML and PDF, with and without a template (EN and DE), plus content assertions. The PDF tests read the PDF back with PDFBox.
- **Label tests:** every BG/BT key exists in the English and the German bundle.
- **CLI tests:** `PhloxCliTest` runs the command line through picocli.
- Golden-file HTML and PDF visual-regression baselines do not exist yet (see [[Roadmap]]).

## PRs

- Don't commit unless explicitly asked.
- Don't push unless explicitly asked.
- Prefer specific `git add` over `git add -A`.
- Add an entry to [[News and noteworthy]] for user-visible changes.

## License

By contributing you agree your work is licensed under the Apache License 2.0.
