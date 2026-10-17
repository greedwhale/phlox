# Maven Module Structure

## Overview

`phlox` is a multi-module Maven project. The root POM is a **BOM** (Bill of Materials) — it pins versions for `ph-commons`, `ph-ubl`, `ph-cii`, `ph-oton-html`, `ph-pdf-layout`, `picocli`, JUnit, etc. — and all submodules inherit from it.

Group ID: `com.helger.phlox`. Initial version: `0.1.0-SNAPSHOT`.

## Module dependency diagram

```mermaid
graph TD
    parent[phlox-parent]
    api[phlox-api]
    ubl[phlox-ubl]
    cii[phlox-cii]
    template[phlox-template]
    html[phlox-html]
    pdf[phlox-pdf]
    cli[phlox-cli]
    testfiles[phlox-testfiles]

    api       --> parent
    ubl       --> parent
    cii       --> parent
    template  --> parent
    html      --> parent
    pdf       --> parent
    cli       --> parent
    testfiles --> parent

    ubl       --> api
    cii       --> api
    template  --> api
    html      --> api
    html      --> template
    pdf       --> api
    pdf       --> template

    cli  --> ubl
    cli  --> cii
    cli  --> html
    cli  --> pdf

    ubl  -. test scope .-> testfiles
    cii  -. test scope .-> testfiles
    html -. test scope .-> testfiles
    pdf  -. test scope .-> testfiles
```

Arrows mean "depends on" (solid = compile, dotted = test-scope). Notice that **no reader depends on a writer and vice versa** — they only share `phlox-api`. This is what lets a caller pull only HTML rendering for UBL onto its classpath, leaving `ph-cii` and `ph-pdf-layout` out. Writers additionally depend on `phlox-template`, which holds the JMustache + commonmark-java machinery and the default `.ptpl` resource — see [[Template Language]].

## Modules

### `phlox-parent`

The root POM. Acts as the BOM for all dependency version management. Pins:

- `ph-commons` (via `ph-commons-parent-pom` import scope).
- `ph-ubl` (via `ph-ubl-parent-pom` import scope).
- `ph-cii` (via `ph-cii-parent-pom` import scope).
- `ph-oton-html` directly.
- `ph-pdf-layout` directly.
- `picocli`.
- JUnit 4 and AssertJ for tests.

No source code in this module. Modeled on `en16931-cii2ubl-parent-pom`.

### `phlox-api`

The contract layer. Contains:

- **Intermediate model:** `PhloxInvoice` and all `Phlox*` child types (one per BG, plus reusable value objects). See [[Domain Model]].
- **SPIs:**
  - `IInvoiceReader<SRC>` — implemented by `phlox-ubl` and `phlox-cii`.
  - `IInvoiceWriter<O>` — implemented by `phlox-html` and `phlox-pdf`.
  - `IPhloxLabelResolver` — default implementation included; can be replaced.
  - `IPhloxTemplate` — implemented by `phlox-template` (small marker interface so `AbstractRenderContext.setTemplate(...)` stays in `phlox-api` without pulling JMustache or commonmark-java).
- **Render context:** `RenderContext`, `PhloxFormatOptions`.
- **Formatting helpers:** `PhloxFormat` (date/amount/percent/code-label formatting).
- **Resource bundles:** `phlox-labels_en.properties`, `phlox-labels_de.properties`, `phlox-codelists_en.properties`, `phlox-codelists_de.properties`. See [[Localization]].

**Dependencies:** `ph-commons` only. No JAXB, no PDF, no HTML libraries.

### `phlox-ubl`

Reads UBL Invoice and CreditNote documents into a `PhloxInvoice`. Supports **both** UBL versions that EN 16931 has a syntax binding for:

- **UBL 2.1** — EN 16931:2017 (current edition).
- **UBL 2.5** — EN 16931:2026 (upcoming edition).

No other UBL versions are in scope. Both bindings live in this single module because the reader logic and field-by-field mapping are nearly identical — see [[Source Readers]] for the rationale.

**Contains:**

- `UblInvoiceReader implements IInvoiceReader<InputStream, PhloxInvoice>` with `fromUbl21()`, `fromUbl25()`, and `autoDetect()` factory methods.
- Internal helpers for walking the UBL JAXB tree (shared between the two version bindings).

**Dependencies:** `phlox-api`, `com.helger.ubl:ph-ubl21`, `com.helger.ubl:ph-ubl25`.

### `phlox-cii`

Reads CII D16B `CrossIndustryInvoice` documents into a `PhloxInvoice`.

**Contains:**

- `CiiInvoiceReader implements IInvoiceReader<InputStream>`.
- Internal helpers for walking the CII JAXB tree, code-list-driven dispatch.

**Dependencies:** `phlox-api`, `com.helger.cii:ph-cii-d16b`.

### `phlox-template`

Holds everything that turns a `.ptpl` source file plus a `PhloxInvoice` into a renderable AST. See [[Template Language]] for the grammar.

**Contains:**

- `PhloxTemplate implements IPhloxTemplate` with factory methods `fromClasspath(String)`, `fromString(String)`, `fromFile(Path)`.
- The JMustache wiring: a `Mustache.CustomContext` that resolves BT/BG paths and format modifiers against a `PhloxInvoice`, plus the `{{label:...}}` adapter that routes through `IPhloxLabelResolver`.
- The commonmark-java configuration: `Parser` with `TablesExtension` and `StrikethroughExtension` enabled, plus a tiny post-pass that lifts `{{class:...}}` block attributes and `{{pagebreak}}` markers out of the parsed AST into sidecar maps.
- The default template `default.ptpl` (language-independent — all literal strings route through `{{label:...}}` resource bundles in `phlox-api`).
- `PhloxTemplateAst` — the value type the two emitter modules consume.

**Dependencies:** `phlox-api`, `com.samskivert:jmustache`, `org.commonmark:commonmark`, `org.commonmark:commonmark-ext-gfm-tables`, `org.commonmark:commonmark-ext-gfm-strikethrough`. No HTML or PDF libraries — those live in the emitter modules.

**Why a separate module:** code organization and reuse — the parser, the Mustache-to-`PhloxInvoice` adapter, and the default `.ptpl` are shared by both writers, so they belong in one place. It also draws a clean line between the template engine and the model: `phlox-template` depends on `phlox-api` but the reverse is not true, so the model stays free of JMustache/commonmark-java types. A consumer who only reads invoices (`phlox-api` + `phlox-ubl` or `phlox-cii`) does not pick up the template-engine jars at all; a consumer who writes invoices via `phlox-html` or `phlox-pdf` does pick them up transitively (~225 KB total), even if they never call `setTemplate(...)`.

### `phlox-html`

Renders a `PhloxInvoice` to HTML5.

**Contains:**

- `HtmlInvoiceWriter implements IInvoiceWriter<String>` (with overloads that return `HCHtml` or stream to an `OutputStream`).
- Section renderer classes (`HeaderSectionRenderer`, `PartiesSectionRenderer`, `LinesSectionRenderer`, ...) used when no template is set on the context.
- `HTMLTemplateEmitter` — a commonmark-java `AbstractVisitor` subclass that walks a `PhloxTemplateAst` and produces an `HCNode` tree. Used when `RenderContext.getTemplate() != null`.
- A default CSS stylesheet, loaded from a classpath resource.

**Dependencies:** `phlox-api`, `phlox-template`, `com.helger.photon:ph-oton-html`.

### `phlox-pdf`

Renders a `PhloxInvoice` to PDF.

**Contains:**

- `PdfInvoiceWriter implements IInvoiceWriter<Void>` (writes to a supplied `OutputStream` or `File`).
- Section renderer classes mirroring `phlox-html`'s layout, used when no template is set on the context.
- `PDFTemplateEmitter` — a commonmark-java `AbstractVisitor` subclass that walks a `PhloxTemplateAst` and produces `PLPageSet` content; paragraphs feed their inline content through `ph-pdf-layout`'s CommonMark-compatible inline parser.
- Default `FontSpec` set (Helvetica regular + bold).

**Dependencies:** `phlox-api`, `phlox-template`, `com.helger:ph-pdf-layout`.

### `phlox-cli`

A command-line wrapper. Picocli-based, shaded into a fat JAR for `java -jar` use.

**Synopsis (proposed):**

```
phlox render --in invoice.xml --out invoice.html --format html --lang de
phlox render --in invoice.xml --out invoice.pdf  --format pdf  --lang en
phlox render --in invoice.xml                    --format html  # to stdout
```

Auto-detects source syntax (UBL vs CII) from the root element.

**Dependencies:** `phlox-ubl`, `phlox-cii`, `phlox-html`, `phlox-pdf`, `picocli`. Produces `phlox-cli/target/phlox-cli-full.jar` via the Maven shade plugin, matching the layout of `en16931-cii2ubl-cli`.

### `phlox-testfiles`

A shared corpus of example invoices used by every reader and writer test. Modeled directly on `ph-cii-testfiles` from `../ph-cii/ph-cii-testfiles/` — packaging is plain `jar` (not `test-jar`), so consumers depend on it with `<scope>test</scope>`. The files are real EN 16931-conformant instances, hand-curated as "good cases" for regression testing.

**Folder layout** under `src/main/resources/external/en16931/`:

```
phlox-testfiles/src/main/resources/external/en16931/
├── 2017/                            # EN 16931:2017 (current edition)
│   ├── ubl/
│   │   ├── invoice/                 # *.xml — UBL 2.1 Invoice (22 files)
│   │   └── creditnote/              # *.xml — UBL 2.1 CreditNote (2 files)
│   └── cii/                         # CII D16B CrossIndustryInvoice (104 files; see README.md there)
│       ├── *.xml                    # core hand-curated examples (15 files)
│       ├── xrechnung/
│       │   ├── 1.2.2/               # XRechnung 1.2.2 corpus (21 files)
│       │   ├── 2.0.0/               # XRechnung 2.0.0 corpus (19 files)
│       │   ├── 3.0.2/               # XRechnung 3.0.2 corpus (33 files)
│       │   └── technical/           # comprehensive + minimal stress tests (6 files)
│       └── issues/                  # bug-regression fixtures (10 files; all well-formed CII)
├── 2026/                            # EN 16931:2026 (upcoming edition)
│   ├── ubl/
│   │   ├── invoice/                 # *.xml — UBL 2.5 Invoice
│   │   └── creditnote/              # *.xml — UBL 2.5 CreditNote
│   └── cii/                         # *.xml — TBD syntax revision once published
```

Everything under `2017/cii/xrechnung/` and `2017/cii/issues/` is imported from `../en16931-cii2ubl/en16931-cii2ubl/src/test/resources/external/cii/`, preserving filenames and subdirectory structure. Provenance is documented in `2017/cii/README.md`.

**Java side:** `MockPhloxTestFiles` in `com.helger.phlox.testfiles` exposes typed lookups (`getUbl21InvoiceFiles()`, `getCiiD16BCoreFiles()`, `getCiiD16BXRechnung302Files()`, `getCiiD16BIssueFiles()`, `getCiiD16BFiles()` — the last returns every CII file across all sub-tiers). Test classes iterate those lists rather than walking the filesystem directly.

**Dependencies:** `ph-commons` only — this module deliberately has no dependency on `phlox-api`, so it can be consumed even by experiments outside this project.

**Why a separate module:** keeping test data out of `phlox-ubl` / `phlox-cii` test resources avoids duplicating the corpus across modules, keeps reader-test artifacts tiny, and gives the corpus its own version life so adding files doesn't churn the reader Jar.

## Why this split

| Decision | Rationale |
|----------|-----------|
| `api` separate from readers/writers | A consumer that only writes HTML for UBL pulls `phlox-api` + `phlox-ubl` + `phlox-html` (≈ ph-commons + ph-ubl21 + ph-oton-html), not the world. |
| Readers split per syntax (not per version) | `phlox-ubl` handles UBL 2.1 (EN 16931:2017) and UBL 2.5 (EN 16931:2026) together — those are the only two UBL versions EN 16931 has a binding for, and their reader logic is shared. CII binding lives in its own module. |
| Writers split per format | Adding a text/plain or DOCX writer is one module with no cross-cutting changes. |
| `phlox-template` separate from `phlox-api` | JMustache and commonmark-java are pulled onto the classpath only for consumers who actually use templates. Marker `IPhloxTemplate` interface stays in `phlox-api` so `setTemplate(...)` is typed everywhere without dragging the parsers in. |
| CLI in its own module | The fat JAR is awkward and shouldn't be on consumers' classpaths. Same shape as `en16931-cii2ubl-cli`. |

## Build commands

```bash
mvn clean compile                # Compile all modules
mvn clean test                   # Run all tests
mvn clean package                # Build all JARs + the CLI fat JAR
mvn test -pl phlox-ubl           # Run only the UBL reader tests
mvn test -pl phlox-pdf -Dtest=PdfInvoiceWriterTest#testHelloWorld
```

Requires Java 17+ (matches `en16931-cii2ubl`).
