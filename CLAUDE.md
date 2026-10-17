# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

**phlox** is a visualizer for EN 16931 electronic invoices (the EU structured e-invoicing standard). It reads UBL (2.1 / 2.5) or CII (D16B) XML into a syntax-neutral intermediate model, then writes HTML or PDF in EN/DE.

## Architecture (read this before changing code)

Three-stage pipeline: **Read** (source XML → intermediate model) → **Hold** (in-memory model) → **Write** (model → HTML / PDF).

Module dependency rules are strict:
- Readers and writers depend **only** on `phlox-api`, never on each other.
- `phlox-cli` is the only module that wires readers and writers together.
- Do not add cross-module deps to enable shortcuts — consumers must be able to depend on, e.g., HTML-for-UBL without pulling PDF libs.

Modules:
- `phlox-parent-pom` — root BOM, no sources
- `phlox-api` — intermediate model + SPIs + I18N (and the `IPhloxTemplate` marker)
- `phlox-testfiles` — shared EN 16931 example invoices; regular `jar` (not `test-jar`), consumed with `<scope>test</scope>`
- `phlox-ubl` — UBL 2.1 **and** 2.5 reader (factories: `fromUbl21()`, `fromUbl25()`, `autoDetect()`)
- `phlox-cii` — CII D16B reader
- `phlox-template` — JMustache + commonmark-java parser, the `PhloxTemplate` factories, and the default `.ptpl` resource. Compile-scope dep of `phlox-html` and `phlox-pdf` (both writers gain a `*TemplateEmitter` that walks the AST). See `wiki/Template-Language.md`.
- `phlox-html` — HTML5 writer
- `phlox-pdf` — PDF writer (PDFBox via ph-pdf-layout)
- `phlox-cli` — picocli CLI; produces `phlox-cli-full.jar` (shaded fat JAR, main class `PhloxCli`)

## Hard constraints

- **No validation in phlox.** Business-rule checks (e.g. VAT consistency) belong in `phive-rules-en16931`. Do not add them here.
- **Stateless library.** No config files, no environment variables, no system properties at startup. All tuning flows through typed `RenderContext` parameters. Never reach for `System.getProperty(...)` or `System.getenv(...)`.
- **Intermediate model is the contract.** Every EN 16931 Business Group (BG) and Business Term (BT) is always populated. Use empty collections, never `null`. Writers must not have "field not present" branches.
- **`RenderContext` type hierarchy is load-bearing.** `HtmlRenderContext` and `PdfRenderContext` both extend `AbstractRenderContext`. Writers accept their specific subclass — this is intentional, do not widen to the base type.
- **JSpecify annotations** (`@NonNull` / `@Nullable`) on public types — used in 47 of 51 Java files.
- **forbiddenapis** plugin runs at compile time using `ph-forbidden-apis` signatures. If it fails, fix the call site — do not exclude the rule.

## Build / test

Standard Maven (Java 17+, Maven 3.x). Worth knowing:
- Single module: `mvn -pl phlox-ubl test`
- Single test method: `mvn -Dtest=PdfInvoiceWriterTest#testHelloWorld test`
- CLI deliverable is `phlox-cli/target/phlox-cli-full.jar` (shaded), **not** `phlox-cli.jar`.
- No CI yet. No checkstyle, no formatter config — style comes from `~/.claude/rules/` (Hungarian notation, spaces before parens, Apache license header, ph-commons collections, etc.).

## Tests

- Test fixtures live in `phlox-testfiles/src/main/resources/external/en16931/{2017,2026}/{ubl,cii}/...`.
- Adding regression coverage usually means adding an XML fixture there, not a new Java test class.
- JUnit 4 + AssertJ.

## Localization

- Labels in `phlox-api/src/main/resources/phlox-labels_{en,de}.properties`.
- Codelists in `phlox-api/src/main/resources/phlox-codelists_{en,de}.properties`.
- Default resolver: `DefaultPhloxLabelResolver`. Callers override via `RenderContext.setLabelResolver(...)`.
- Adding a language = adding the `.properties` files plus a resource-bundle provider.

## Other

- `PhloxProgramWatermark.setInstance(...)` is a singleton set once at JVM startup (used for attribution in OSS vs. commercial builds) — not a per-render option.
- Shade plugin in `phlox-cli` uses `ServicesResourceTransformer` to merge SPI registries; preserve that when touching the CLI build.
