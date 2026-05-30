# Template Processing

This page is the **implementation companion** to [[Template Language]]. That page defines the `.ntpl` grammar;
this one describes the engine that turns a `.ntpl` source file plus a `NormaInvoice` into rendered output.

> **Status:** Design proposal. Not yet implemented. Targeted at the 0.4.0 milestone (see [[Roadmap]]).

## Pipeline overview

```
   ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
   │  .ntpl source    │   │   NormaInvoice   │   │  RenderContext   │
   │  (one string)    │   │   (intermediate  │   │  (locale, label  │
   │                  │   │    model)        │   │   resolver, …)   │
   └─────────┬────────┘   └─────────┬────────┘   └────────┬─────────┘
             │                      │                     │
             └──────────────────────┼─────────────────────┘
                                    ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 1 — Substitute  (norma-template)                        │
   │  JMustache + NormaInvoiceMustacheContext                       │
   │  ─ resolves {{BT-X}}, {{BG-X.BT-Y}}, {{label:…}}, modifiers    │
   │  ─ expands {{#BG-X}}…{{/BG-X}} loops                           │
   │  ─ drops {{?BT-X}}…{{/BT-X}} blocks whose target is empty      │
   │  ─ leaves {{class:…}} and {{pagebreak}} as sentinel tokens     │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                       Substituted CommonMark document (String)
                                     │
                                     ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 2 — Parse  (norma-template)                             │
   │  commonmark-java + TablesExtension + StrikethroughExtension    │
   │  ─ parses headings, paragraphs, lists, GFM tables, code,       │
   │    blockquotes, thematic breaks, links, emphasis, images       │
   │  ─ post-pass lifts {{class:…}} and {{pagebreak}} sentinels     │
   │    out of Text nodes and attaches them as Node properties      │
   │  ─ post-pass parses image URLs (data:, attachment:, classpath:)│
   │    and rejects http/https/file at parse time                   │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                        NormaTemplateAST (Node + sidecars)
                                     │
                         ┌───────────┴───────────┐
                         ▼                       ▼
   ┌──────────────────────────────┐ ┌──────────────────────────────┐
   │  STAGE 3a — HTML emit        │ │  STAGE 3b — PDF emit         │
   │  HTMLTemplateEmitter         │ │  PDFTemplateEmitter          │
   │  AbstractVisitor → HCNode    │ │  AbstractVisitor → PLPageSet │
   │  inline: ph-oton-html types  │ │  inline: PLRichText via      │
   │                              │ │  ph-pdf-layout CommonMark    │
   │                              │ │  inline parser               │
   └──────────────────────────────┘ └──────────────────────────────┘
                  │                                │
                  ▼                                ▼
         HCHtml / String                     OutputStream (PDF)
```

Each stage has well-defined inputs and outputs and is independently testable. Stages 1 and 2 are shared by both writers; stage 3 diverges per output format.

## Stage 1 — Substitute

### What it does

JMustache walks the `.ntpl` token stream and substitutes placeholders for actual values. The output is a **plain `String`** that is itself a valid CommonMark document. No HTML, no PDF, no AST — just text.

### `NormaInvoiceMustacheContext`

A `com.samskivert.mustache.Mustache.CustomContext` (or a `Mustache.Lambda` per resolution) that knows how to resolve every placeholder the [[Template Language]] grammar permits. It is created fresh per render and carries:

- The `NormaInvoice` being rendered.
- The active `Locale` (from `RenderContext.getLocale()`).
- The `INormaLabelResolver` (from `RenderContext.getLabelResolver()`).
- The `NormaFormatOptions` (from `RenderContext.getFormatOptions()`).
- A scope stack tracking the current loop position (for `{{#BG-25}}…{{/BG-25}}` and parent-scope lookups via `^.`).

### Resolution algorithm

For a placeholder `{{name | modifier:arg}}`:

1. **Parse the placeholder.** Split on `|`; trim whitespace. First token is the name path; remaining tokens are modifiers.
2. **Dispatch by shape:**
   - `label:foo` → look up `foo` via `INormaLabelResolver.getLabel(foo, locale)`.
   - `class:foo` → emit the sentinel `CLASS=foo` (post-pass picks this up).
   - `pagebreak` → emit the sentinel `PAGEBREAK`.
   - `BT-N` or `BG-N.BT-M` or `BG-N.BG-M.BT-K` → walk the model.
   - `references_present`, `line_extras_present`, `logo` → synthesized booleans (see below).
3. **Walk the model.** Split the path on `.`. Each segment is either a BG name (push into the BG instance) or a BT name (resolve to a field value).
4. **Apply default formatter** based on the EN 16931 semantic type recorded in [[Domain Model]]:
   - `Amount` → `NormaFormat.amount(val, BT-5 currency, locale)`
   - `Unit Price Amount` → same as Amount; subtype hook for extra fraction digits
   - `Date` → `NormaFormat.date(val, locale)` (medium style)
   - `Code` → `NormaFormat.codeListLabel(<inferred-list>, val, locale)`
   - `Percentage` → `NormaFormat.percent(val, locale)`
   - `Identifier`, `Document Reference`, `Text`, `Quantity` → toString, HTML-escaped
   - `Binary Object` → never appears as a value placeholder; only as the target of `attachment:BG-24[i]`
5. **Apply modifiers**, in left-to-right pipe order. Each modifier is a pure function of (value, locale, options) → String. `raw` skips the default formatter entirely.

### Synthesized boolean keys

The Mustache adapter exposes a handful of synthesized keys that are convenient for templates but don't correspond to a single BT. They live alongside the model and are computed once at the start of substitute:

| Key | Truthy when |
|---|---|
| `logo` | `RenderContext.getLogo() != null` — gates the `attachment:logo` image at the top of the default template. |
| `references_present` | Any of BT-11..BT-19 are non-empty, **or** BG-3 is non-empty — gates the References section. |
| `line_extras_present` | Any line in BG-25 has a non-empty BT-127, BG-27, or BG-28 — gates the extras block under the line table. |

These are exposed to the template as ordinary truthy values: `{{?references_present}}…{{/references_present}}` works exactly like a BT presence check.

### Format modifiers

A modifier is `name` or `name:arg`. The adapter ships with:

| Modifier | Implementation |
|---|---|
| `date` / `date:short` / `date:medium` / `date:long` | `DateTimeFormatter.ofLocalizedDate(<style>).withLocale(locale)` |
| `amount` | `NormaFormat.amount(val, getCurrency(BT-5), locale)` |
| `amount:BT-6` | Same but currency comes from BT-6 — used for BT-111. |
| `percent` | `NumberFormat.getPercentInstance(locale)`; two decimals. |
| `code:<listName>` | `NormaFormat.codeListLabel(<listName>, val, locale)` — drives the `norma-codelists_<lang>.properties` lookup. |
| `raw` | Returns the value's `toString()` without HTML-escaping or formatting. |
| `upper` / `lower` / `title` | `String.toUpperCase(locale)` / `.toLowerCase(locale)` / title case via ICU. |

Modifiers are registered via a small SPI so custom format hooks can be added without forking the engine.

### Output of stage 1

A `String` containing valid CommonMark. Looking at one fully-substituted line:

Source (in `.ntpl`):
```
| {{BT-126}} | **{{BG-31.BT-153}}** | {{BT-129}} {{BT-130 | code:unit}} | {{BG-29.BT-146 | amount}} |
```

After substitution (assuming line 1, "Widget A", quantity 10 pieces, unit price €5.00):
```
| 1 | **Widget A** | 10 piece | €5.00 |
```

That second string is what stage 2 receives.

## Stage 2 — Parse

### Standard commonmark-java

```java
final List <Extension> exts = List.of (TablesExtension.create (), StrikethroughExtension.create ());
final Parser parser = Parser.builder ().extensions (exts).build ();
final Node root = parser.parse (sSubstituted);
```

The resulting `Node` tree is standard commonmark-java — `Document`, `Paragraph`, `Heading`, `BulletList`, `OrderedList`, `Table`, `TableRow`, `TableCell`, `Text`, `Emphasis`, `StrongEmphasis`, `Strikethrough`, `Code`, `Link`, `Image`, etc.

### Sentinel post-pass

After the standard parse, a `NormaTemplatePostParser` walks the tree and processes the two norma-specific sentinels stage 1 emitted:

1. **`CLASS=name`** — appears as content of a `Text` node, often standalone in a `Paragraph`. The post-pass:
   - Removes the sentinel from the `Text` node.
   - If the `Text` is now empty, removes the enclosing `Paragraph`.
   - Attaches `name` as the `class` property on the **next sibling block** (via a sidecar map keyed by `Node` identity, since commonmark-java `Node` doesn't expose arbitrary user properties).

2. **`PAGEBREAK`** — same treatment, but produces a synthetic `NormaPageBreakNode` (a custom `Node` subclass) in place of the original paragraph. Emitters recognise this type.

### Image URL post-pass

In the same post-pass, every `Image` node's `destination` is normalised:

| URL scheme | Action |
|---|---|
| `data:` | Left as-is; emitters consume the data URI verbatim. |
| `attachment:logo` | Replaced with a `NormaResolvedImage` placeholder pointing at `RenderContext.getLogo()`. |
| `attachment:BG-24[i]` | Replaced with a `NormaResolvedImage` pointing at `aInvoice.getSupportingDocuments().get(i).getAttachment()`. Out-of-bounds → `NormaTemplateException`. |
| `classpath:/…` | Resolved via `IReadableResource` on the writer's classpath; replaced with a `NormaResolvedImage` that carries the bytes. |
| `http:` / `https:` / `file:` | Rejected at this stage — `NormaTemplateException` with the AST location. |

Resolution happens here, not in stage 3, so the two emitters share the URL-scheme validation and so an out-of-bounds attachment index fails fast for any output format.

### Output of stage 2

A `NormaTemplateAST` value object holding:

- `root` — the (post-processed) commonmark `Node` tree.
- `blockClassMap : Map<Node, String>` — block-attribute classes.
- `imageMap : Map<Image, NormaResolvedImage>` — resolved image references.
- `invoice` — the source `NormaInvoice` (so emitters can re-resolve things like the currency for any explicit fall-back, though most resolution is already done).
- `context` — the `RenderContext` reference (so emitters can read the CSS prefix, font specs, etc.).

`NormaTemplateAST` is immutable after construction.

## Stage 3 — Emit

Each writer module ships an emitter that subclasses commonmark-java's `AbstractVisitor`. They share the visit pattern; they differ only in the output API.

### `HTMLTemplateEmitter` (norma-html)

Walks the AST and builds an `HCNode` tree, the same shape `HTMLInvoiceWriter` produces from its hard-coded section grammar. Mapping:

| AST node | `HCNode` output |
|---|---|
| `Heading(level=1)` | `HCH1` with `addClass("norma-title")` |
| `Heading(level=2)` | `HCH2` with `addClass("norma-section-title")` |
| `Heading(level=3)` | `HCH3` with `addClass("norma-subsection-title")` |
| `Paragraph` | `HCP` |
| `StrongEmphasis` | `HCStrong` |
| `Emphasis` | `HCEM` |
| `Strikethrough` | `HCS` |
| `Code` | `HCCode` |
| `Link(destination=url)` | `HCA` with `setHref` |
| `BulletList` / `OrderedList` | `HCUL` / `HCOL` (`HCLI` for items) |
| `BlockQuote` | `HCBlockQuote` |
| `FencedCodeBlock` / `IndentedCodeBlock` | `HCPre` with nested `HCCode` |
| `ThematicBreak` | `HCHR` |
| `Table` / `TableHead` / `TableBody` / `TableRow` / `TableCell` | `HCTable` etc. with column alignment styles |
| `Image` (resolved via image map) | `HCImg` with a data-URI `src` |
| `NormaPageBreakNode` | `HCDiv` with `style="page-break-after: always"` |
| Text | `HCTextNode` (escapes automatically) |

The block-class map is consulted at each block visit and the corresponding class is prefixed with `HTMLRenderContext.getCSSClassPrefix()` and added to the emitted `HCNode`. The CSS prefix logic is the same as in the hard-coded writer.

### `PDFTemplateEmitter` (norma-pdf)

Walks the AST and builds `PLPageSet` content via `ph-pdf-layout`:

| AST node | `ph-pdf-layout` output |
|---|---|
| `Heading(level=1..3)` | `PLText` with `PDFRenderContext.getHeaderFont()`, size decreasing per level |
| `Paragraph` | A single `PLRichText` constructed by feeding the paragraph's serialised inline markup through ph-pdf-layout's CommonMark inline parser |
| `BulletList` / `OrderedList` | The list-block helper from ph-pdf-layout's CommonMark-compatible block module |
| `BlockQuote` | Indented `PLBox` with a left border |
| `FencedCodeBlock` | Indented `PLText` with a monospace `FontSpec` |
| `Table` | `PLTable`; column count and alignment from the AST; column widths from a `widths=` attribute on the table (default: even split); header row styled with `PDFRenderContext.getTableHeaderFont()` |
| `ThematicBreak` | A thin horizontal `PLBox` border line |
| `Image` (resolved) | `PLImage` (block-level; if it appears inline within a `Paragraph`, the post-pass already split the paragraph around it) |
| `NormaPageBreakNode` | `PLPageBreak` |
| Inline nodes | Folded back into the paragraph's CommonMark source before handing to ph-pdf-layout's inline parser, so `**bold**`, `*italic*`, `~~strike~~`, `` `code` ``, `[link](…)` all render natively |

The block-class map drives `PLTableCell` fill colours and `PLBox` background colours via a style map exposed on `PDFRenderContext`. The default style map provides one entry (`totals-due` → light grey fill); callers add more via `PDFRenderContext.putBlockStyle("name", BlockStyle)`.

### Inline strategy

The PDF emitter does **not** walk inline nodes itself. For each `Paragraph` (or table cell) it serialises the inline AST back to CommonMark, then hands that string to `ph-pdf-layout`'s CommonMark inline parser. This keeps the inline rendering in lock-step between the two writers — both end up parsing the same CommonMark inline grammar — and avoids reimplementing ten kinds of styling per emitter.

The HTML emitter does walk inline nodes itself, because `HCNode` types map straight from commonmark inline types with no lossy intermediate string form.

## Threading and caching

- **`NormaTemplate` is immutable after construction.** A single instance can be used concurrently from many threads without synchronisation.
- **JMustache's compiled `Template`** is cached as a field on `NormaTemplate`. Compile happens once, in the factory method (`fromClasspath`, `fromString`, `fromFile`).
- **commonmark-java's `Parser`** is cached as a field on `NormaTemplate`. Both `Parser` and `AbstractVisitor` subclasses are thread-safe per commonmark-java's documentation.
- **Per-render allocations:** one `NormaInvoiceMustacheContext`, one substituted `String`, one `Node` tree, one `NormaTemplateAST`. Nothing on the hot path requires locking.

For a server rendering many invoices through the same template — the expected workload for an embedded `norma` — the steady-state cost per invoice is one full pipeline pass, with the JMustache compile and Parser construction amortised over the JVM's lifetime.

## Error handling

Errors carry a `NormaTemplateException` that includes:

| Field | Source |
|---|---|
| `stage` | `SUBSTITUTE`, `PARSE`, or `EMIT`. |
| `line`, `column` | Source position in the original `.ntpl`. JMustache exposes this for stage 1; the post-pass tracks AST node positions for stages 2 and 3. |
| `path` | The BG/BT path being resolved when the error fired (e.g. `BG-25[3].BG-29.BT-146`). Empty for non-resolution errors. |
| `message` | Human-readable explanation. |
| `cause` | The underlying exception, if any. |

### What raises an error

| Stage | Trigger | Behaviour |
|---|---|---|
| Substitute | Unknown placeholder name | `NormaTemplateException` — fail fast. |
| Substitute | Format modifier not recognised | `NormaTemplateException`. |
| Substitute | `{{label:foo}}` returns the key (resolver miss) | Logged at WARN once per missing key, output is the key itself — same fallback as the hard-coded writer. **Not an error.** |
| Substitute | BT value is `null` / empty for `{{BT-X}}` | Renders empty (or `—` when `IncludeEmptySections=true`). **Not an error.** |
| Substitute | `{{?BT-X}}` block — target falsy | Skipped silently. **Not an error.** |
| Parse | GFM table malformed (wrong column count, etc.) | commonmark-java drops the offending row. Logged at WARN. |
| Parse | Image URL uses rejected scheme (`http:`, `https:`, `file:`) | `NormaTemplateException`. |
| Parse | `attachment:BG-24[i]` index out of range | `NormaTemplateException`. |
| Emit | Block class referenced but no entry in style map | Logged at WARN; block renders unstyled. **Not an error.** |
| Emit | Image resolution returned empty bytes | `NormaTemplateException`. |

The principle: data-shape errors that originate in the **invoice** (missing BTs, unset optional sections) are silent — that's the normal case. Errors that originate in the **template** (unknown name, bad modifier, bad URL) fail fast — that's a developer bug and we want it visible.

## Integration with writers

```java
public final String write (@Nonnull final NormaInvoice aInvoice,
                            @Nonnull final HTMLRenderContext aCtx)
{
  final INormaTemplate aTpl = aCtx.getTemplate ();
  if (aTpl != null)
  {
    final NormaTemplateAST aAST = ((NormaTemplate) aTpl).renderAST (aInvoice, aCtx);
    final HCHtml aHtml = new HTMLTemplateEmitter (aCtx).emit (aAST);
    return HCRenderer.getAsHTMLString (aHtml, HCSettings.getMutableConversionSettings ());
  }
  // Fall through to the 0.1.0 section renderers
  return _writeWithDefaultGrammar (aInvoice, aCtx);
}
```

The decision is one null-check. When unset, the template engine is not touched — none of `norma-template`'s code paths execute, none of JMustache or commonmark-java is loaded into the class verifier. The hard-coded section grammar from [[Output Writers]] is the unchanged 0.1.0 behaviour.

`PDFInvoiceWriter` follows the same shape with `PDFTemplateEmitter` and `PDFRenderContext`.

## Loading the default template

```java
// Shorthand for the default ntpl shipped in norma-template
final INormaTemplate aDefault = NormaTemplate.getDefault ();

new HTMLInvoiceWriter ().writeAsString (aInvoice,
    new HTMLRenderContext ().setLocale (Locale.GERMAN).setTemplate (aDefault));
```

`getDefault()` returns a singleton `NormaTemplate` built from `classpath:/templates/default.ntpl`. The same instance is shared across all callers (immutable after construction).

For 0.4.0 the writers do **not** fall through to the default template when `getTemplate() == null` — they fall through to the hard-coded section renderers, preserving 0.1.0 byte-for-byte output. A future release may flip the default; that would be a deliberate behaviour change called out in [[News and noteworthy]].

## Performance

Asymptotics per render (template already compiled):

| Step | Cost | Allocations |
|---|---|---|
| Substitute | O(template size) | one `String` of substituted length |
| Parse | O(substituted size) | the `Node` tree (proportional to AST node count) |
| Post-pass | O(node count) | a few `Map` entries per `{{class:…}}` / image |
| Emit (HTML) | O(node count) | the `HCNode` tree |
| Emit (PDF) | O(node count) + ph-pdf-layout's layout pass | `PLPageSet` content + per-page line breaks |

For a typical EN 16931 invoice (~50 lines of source, 15-20 lines in the output), all four phases combined fit in milliseconds on commodity hardware. The cost is dominated by the PDF layout pass; templates add a few percent overhead.

## See also

- [[Template Language]] — the `.ntpl` grammar (Mustache + CommonMark + GFM).
- [[Output Writers]] — the default section grammar that runs when no template is set.
- [[Domain Model]] — the BG/BT inventory the Mustache adapter resolves against, plus the semantic-type vocabulary that picks the default formatter.
- [[Localization]] — `INormaLabelResolver` and the resource bundles `{{label:…}}` reads from.
- [[Configuration Properties]] — `setTemplate(...)` on `AbstractRenderContext`.
- [[Maven Module Structure]] — where each pipeline stage lives (`norma-template`, `norma-html`, `norma-pdf`).
