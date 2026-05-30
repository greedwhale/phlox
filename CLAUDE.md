# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

**norma** is a visualizer for EN 16931 electronic invoices (the EU structured e-invoicing standard). It reads UBL (2.1 / 2.5) or CII (D16B) XML into a syntax-neutral intermediate model, then writes HTML or PDF in EN/DE.

## Architecture (read this before changing code)

Three-stage pipeline: **Read** (source XML → intermediate model) → **Hold** (in-memory model) → **Write** (model → HTML / PDF).

Module dependency rules are strict:
- Readers and writers depend **only** on `norma-api`, never on each other.
- `norma-cli` is the only module that wires readers and writers together.
- Do not add cross-module deps to enable shortcuts — consumers must be able to depend on, e.g., HTML-for-UBL without pulling PDF libs.

Modules:
- `norma-parent-pom` — root BOM, no sources
- `norma-api` — intermediate model + SPIs + I18N (and the `INormaTemplate` marker)
- `norma-testfiles` — shared EN 16931 example invoices; regular `jar` (not `test-jar`), consumed with `<scope>test</scope>`
- `norma-ubl` — UBL 2.1 **and** 2.5 reader (factories: `fromUBL21()`, `fromUBL25()`, `autoDetect()`)
- `norma-cii` — CII D16B reader
- `norma-template` — JMustache + commonmark-java parser, the `NormaTemplate` factories, and the default `.ntpl` resource. Compile-scope dep of `norma-html` and `norma-pdf` (both writers gain a `*TemplateEmitter` that walks the AST). See `wiki/Template-Language.md`.
- `norma-html` — HTML5 writer
- `norma-pdf` — PDF writer (PDFBox via ph-pdf-layout)
- `norma-cli` — picocli CLI; produces `norma-cli-full.jar` (shaded fat JAR, main class `NormaCLI`)

## Hard constraints

- **No validation in norma.** Business-rule checks (e.g. VAT consistency) belong in `phive-rules-en16931`. Do not add them here.
- **Stateless library.** No config files, no environment variables, no system properties at startup. All tuning flows through typed `RenderContext` parameters. Never reach for `System.getProperty(...)` or `System.getenv(...)`.
- **Intermediate model is the contract.** Every EN 16931 Business Group (BG) and Business Term (BT) is always populated. Use empty collections, never `null`. Writers must not have "field not present" branches.
- **`RenderContext` type hierarchy is load-bearing.** `HTMLRenderContext` and `PDFRenderContext` both extend `AbstractRenderContext`. Writers accept their specific subclass — this is intentional, do not widen to the base type.
- **JSpecify annotations** (`@NonNull` / `@Nullable`) on public types — used in 47 of 51 Java files.
- **forbiddenapis** plugin runs at compile time using `ph-forbidden-apis` signatures. If it fails, fix the call site — do not exclude the rule.

## Build / test

Standard Maven (Java 17+, Maven 3.x). Worth knowing:
- Single module: `mvn -pl norma-ubl test`
- Single test method: `mvn -Dtest=PDFInvoiceWriterTest#testHelloWorld test`
- CLI deliverable is `norma-cli/target/norma-cli-full.jar` (shaded), **not** `norma-cli.jar`.
- No CI yet. No checkstyle, no formatter config — style comes from `~/.claude/rules/` (Hungarian notation, spaces before parens, Apache license header, ph-commons collections, etc.).

## Tests

- Test fixtures live in `norma-testfiles/src/main/resources/external/en16931/{2017,2026}/{ubl,cii}/...`.
- Adding regression coverage usually means adding an XML fixture there, not a new Java test class.
- JUnit 4 + AssertJ.

## Localization

- Labels in `norma-api/src/main/resources/norma-labels_{en,de}.properties`.
- Codelists in `norma-api/src/main/resources/norma-codelists_{en,de}.properties`.
- Default resolver: `DefaultNormaLabelResolver`. Callers override via `RenderContext.setLabelResolver(...)`.
- Adding a language = adding the `.properties` files plus a resource-bundle provider.

## Other

- `NormaProgramWatermark.setInstance(...)` is a singleton set once at JVM startup (used for attribution in OSS vs. commercial builds) — not a per-render option.
- Shade plugin in `norma-cli` uses `ServicesResourceTransformer` to merge SPI registries; preserve that when touching the CLI build.
