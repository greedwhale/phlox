# phlox

Visualizer for EN 16931 electronic invoices.

Reads invoices in **UBL** or **CII** syntax, lifts them into a syntax-neutral intermediate model, and renders the result to **HTML** or **PDF** in English or German.

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│ UBL Invoice  │ ──▶ │                  │ ──▶ │ HTML output  │
│ UBL CredNote │     │  PhloxInvoice    │     │              │
│ CII D16B/D25A│ ──▶ │  (EN 16931 BGs)  │ ──▶ │ PDF output   │
└──────────────┘     └──────────────────┘     └──────────────┘
   Source readers      Intermediate model         Writers
```

## Features

- **Both editions of the standard:** EN 16931:2017 (UBL 2.1, CII D16B) and EN 16931:2026 (UBL 2.5, CII D25A). The edition is detected from BT-24 or chosen explicitly.
- **Complete model:** every EN 16931 business group and business term is represented; terms that only exist in 2026 stay empty for 2017 documents.
- **Template driven output:** HTML and PDF are rendered from a `.ptpl` template (JMustache plus CommonMark). The default template ships with phlox; your own replaces it without writing Java.
- **Themes:** colours, fonts, page layout, block styles, language, logo and watermark come from one JSON file. See [Styling](https://github.com/phax/phlox/wiki/Styling).
- **Any script in PDF:** embed TTF or OTF fonts (for example Roboto for Cyrillic and Greek) in HTML and PDF with `--font`, the theme or the API. The standard PDF fonts only contain Western European characters.
- **Watermarks:** a user watermark such as `DRAFT`, plus the program watermark.
- **Command line:** `phlox-cli` renders an invoice file to HTML or PDF.
- **No validation:** phlox only visualizes. Use `phive-rules-en16931` to check business rules.

## Modules

| Module | Purpose |
|--------|---------|
| `phlox-api` | Intermediate model, SPIs and localization |
| `phlox-ubl` | UBL 2.1 and 2.5 reader |
| `phlox-cii` | CII D16B and D25A reader |
| `phlox-template` | Template engine and the default template |
| `phlox-theme` | JSON theme reader |
| `phlox-html` | HTML5 writer |
| `phlox-pdf` | PDF writer |
| `phlox-cli` | Command line tool |
| `phlox-testfiles` | Shared example invoices for tests |

Readers and writers depend only on `phlox-api` (and `phlox-template`), never on each other, so you can depend on, for example, the HTML writer without pulling in the PDF libraries.

## Build

```bash
mvn clean package
```

Requires Java 17+ and Maven 3.

The command line tool is `phlox-cli/target/phlox-cli-full.jar` (the shaded jar):

```bash
java -jar phlox-cli/target/phlox-cli-full.jar -i invoice.xml -f PDF -o invoice.pdf
```

| Option | Meaning |
|--------|---------|
| `-i`, `--in` | Input invoice XML file (stdin if omitted) |
| `-o`, `--out` | Output file (stdout if omitted, HTML only) |
| `-f`, `--format` | `HTML` (default) or `PDF` |
| `-s`, `--syntax` | `AUTO` (default), `UBL` or `CII` |
| `--en-version` | `auto` (default), `2017` or `2026` |
| `-l`, `--lang` | Output language, `en` or `de` |
| `-t`, `--template` | `default`, `classpath:<path>` or the path of a `.ptpl` file |
| `--theme` | JSON theme file |
| `--logo` | Logo image (PNG, JPEG or GIF) |
| `--font` | Regular font file (TTF or OTF) for HTML and PDF; also `--font-bold`, `--font-italic` and `--font-bold-italic` |
| `--watermark` | User watermark text; see also `--watermark-opacity` and `--watermark-rotation` |

## Documentation

See the [wiki](https://github.com/phax/phlox/wiki/Home) for the full documentation, including the [domain model](https://github.com/phax/phlox/wiki/Domain-Model), the [template language](https://github.com/phax/phlox/wiki/Template-Language), [themes](https://github.com/phax/phlox/wiki/Themes), the [maven module structure](https://github.com/phax/phlox/wiki/Maven-Module-Structure), the [roadmap](https://github.com/phax/phlox/wiki/Roadmap) and the [news and noteworthy](https://github.com/phax/phlox/wiki/News-and-noteworthy).

## License

Apache License 2.0.
