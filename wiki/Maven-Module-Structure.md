# Maven Module Structure

## Overview

`norma` is a multi-module Maven project. The root POM is a **BOM** (Bill of Materials) — it pins versions for `ph-commons`, `ph-ubl`, `ph-cii`, `ph-oton-html`, `ph-pdf-layout`, `picocli`, JUnit, etc. — and all submodules inherit from it.

Group ID: `com.helger.norma`. Initial version: `0.1.0-SNAPSHOT`.

## Module dependency diagram

```mermaid
graph TD
    parent[norma-parent]
    api[norma-api]
    ubl[norma-ubl]
    cii[norma-cii]
    html[norma-html]
    pdf[norma-pdf]
    cli[norma-cli]

    api  --> parent
    ubl  --> parent
    cii  --> parent
    html --> parent
    pdf  --> parent
    cli  --> parent

    ubl  --> api
    cii  --> api
    html --> api
    pdf  --> api

    cli  --> ubl
    cli  --> cii
    cli  --> html
    cli  --> pdf
```

Arrows mean "depends on". Notice that **no reader depends on a writer and vice versa** — they only share `norma-api`. This is what lets a caller pull only HTML rendering for UBL onto its classpath, leaving `ph-cii` and `ph-pdf-layout` out.

## Modules

### `norma-parent`

The root POM. Acts as the BOM for all dependency version management. Pins:

- `ph-commons` (via `ph-commons-parent-pom` import scope).
- `ph-ubl` (via `ph-ubl-parent-pom` import scope).
- `ph-cii` (via `ph-cii-parent-pom` import scope).
- `ph-oton-html` directly.
- `ph-pdf-layout` directly.
- `picocli`.
- JUnit 4 and AssertJ for tests.

No source code in this module. Modeled on `en16931-cii2ubl-parent-pom`.

### `norma-api`

The contract layer. Contains:

- **Intermediate model:** `NormaInvoice` and all `Norma*` child types (one per BG, plus reusable value objects). See [[Domain Model]].
- **SPIs:**
  - `IInvoiceReader<SRC>` — implemented by `norma-ubl` and `norma-cii`.
  - `IInvoiceWriter<O>` — implemented by `norma-html` and `norma-pdf`.
  - `INormaLabelResolver` — default implementation included; can be replaced.
- **Render context:** `RenderContext`, `NormaFormatOptions`.
- **Formatting helpers:** `NormaFormat` (date/amount/percent/code-label formatting).
- **Resource bundles:** `norma-labels_en.properties`, `norma-labels_de.properties`, `norma-codelists_en.properties`, `norma-codelists_de.properties`. See [[Localization]].

**Dependencies:** `ph-commons` only. No JAXB, no PDF, no HTML libraries.

### `norma-ubl`

Reads UBL Invoice and CreditNote documents into a `NormaInvoice`. Supports **both** UBL versions that EN 16931 has a syntax binding for:

- **UBL 2.1** — EN 16931:2017 (current edition).
- **UBL 2.5** — EN 16931:2026 (upcoming edition).

No other UBL versions are in scope. Both bindings live in this single module because the reader logic and field-by-field mapping are nearly identical — see [[Source Readers]] for the rationale.

**Contains:**

- `UBLInvoiceReader implements IInvoiceReader<InputStream, NormaInvoice>` with `fromUBL21()`, `fromUBL25()`, and `autoDetect()` factory methods.
- Internal helpers for walking the UBL JAXB tree (shared between the two version bindings).

**Dependencies:** `norma-api`, `com.helger.ubl:ph-ubl21`, `com.helger.ubl:ph-ubl25`.

### `norma-cii`

Reads CII D16B `CrossIndustryInvoice` documents into a `NormaInvoice`.

**Contains:**

- `CIIInvoiceReader implements IInvoiceReader<InputStream>`.
- Internal helpers for walking the CII JAXB tree, code-list-driven dispatch.

**Dependencies:** `norma-api`, `com.helger.cii:ph-cii-d16b`.

### `norma-html`

Renders a `NormaInvoice` to HTML5.

**Contains:**

- `HTMLInvoiceWriter implements IInvoiceWriter<String>` (with overloads that return `HCHtml` or stream to an `OutputStream`).
- Section renderer classes (`HeaderSectionRenderer`, `PartiesSectionRenderer`, `LinesSectionRenderer`, ...).
- A default CSS stylesheet, loaded from a classpath resource.

**Dependencies:** `norma-api`, `com.helger.photon:ph-oton-html`.

### `norma-pdf`

Renders a `NormaInvoice` to PDF.

**Contains:**

- `PDFInvoiceWriter implements IInvoiceWriter<Void>` (writes to a supplied `OutputStream` or `File`).
- Section renderer classes mirroring `norma-html`'s layout.
- Default `FontSpec` set (Helvetica regular + bold).

**Dependencies:** `norma-api`, `com.helger:ph-pdf-layout`.

### `norma-cli`

A command-line wrapper. Picocli-based, shaded into a fat JAR for `java -jar` use.

**Synopsis (proposed):**

```
norma render --in invoice.xml --out invoice.html --format html --lang de
norma render --in invoice.xml --out invoice.pdf  --format pdf  --lang en
norma render --in invoice.xml                    --format html  # to stdout
```

Auto-detects source syntax (UBL vs CII) from the root element.

**Dependencies:** `norma-ubl`, `norma-cii`, `norma-html`, `norma-pdf`, `picocli`. Produces `norma-cli/target/norma-cli-full.jar` via the Maven shade plugin, matching the layout of `en16931-cii2ubl-cli`.

## Why this split

| Decision | Rationale |
|----------|-----------|
| `api` separate from readers/writers | A consumer that only writes HTML for UBL pulls `norma-api` + `norma-ubl` + `norma-html` (≈ ph-commons + ph-ubl21 + ph-oton-html), not the world. |
| Readers split per syntax (not per version) | `norma-ubl` handles UBL 2.1 (EN 16931:2017) and UBL 2.5 (EN 16931:2026) together — those are the only two UBL versions EN 16931 has a binding for, and their reader logic is shared. CII binding lives in its own module. |
| Writers split per format | Adding a text/plain or DOCX writer is one module with no cross-cutting changes. |
| CLI in its own module | The fat JAR is awkward and shouldn't be on consumers' classpaths. Same shape as `en16931-cii2ubl-cli`. |

## Build commands

```bash
mvn clean compile                # Compile all modules
mvn clean test                   # Run all tests
mvn clean package                # Build all JARs + the CLI fat JAR
mvn test -pl norma-ubl           # Run only the UBL reader tests
mvn test -pl norma-pdf -Dtest=PDFInvoiceWriterTest#testHelloWorld
```

Requires Java 17+ (matches `en16931-cii2ubl`).
