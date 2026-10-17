# Template Processing

This page is the **implementation companion** to [[Template Language]]. That page defines the `.ptpl` grammar;
this one describes the engine that turns a `.ptpl` source file plus a `PhloxInvoice` into rendered output.

> **Status:** Stage 1 (substitute), 2 (parse + post-pass) and the rendering AST are implemented in 0.4.0; emitters (stage 3) are pending. Stage 0 (localize) is a design refinement intended to land alongside the emitters.

## Pipeline overview

```
   ┌──────────────────┐                          ┌──────────────────┐
   │  .ptpl source    │                          │  RenderContext   │
   │  (one string)    │                          │  (locale, label  │
   │                  │                          │   resolver, …)   │
   └─────────┬────────┘                          └────────┬─────────┘
             │                                            │
             └────────────────────┬───────────────────────┘
                                  ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 0 — Localize  (phlox-template)                          │
   │  ─ strips {{! comment }} blocks                                │
   │  ─ substitutes every {{label:KEY}} via IPhloxLabelResolver     │
   │  ─ encodes {{?X}}…{{/X}} presence-check directives             │
   │  ─ no Mustache compile yet; output is still a String           │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                       Locale-baked source (String)
                                     │
                                     ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 0a — Compile  (phlox-template)                          │
   │  ─ JMustache.compile(localeBaked) → Template                   │
   │  ─ output is a compiled, locale-baked, thread-safe artifact    │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                  PhloxLocalizedTemplate  (compiled, cacheable)
                                     │
   ┌──────────────────┐              │
   │  PhloxInvoice    │──────────────┤
   └──────────────────┘              ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 1 — Substitute  (phlox-template)                        │
   │  Template.execute(invoice) + PhloxInvoiceMustacheContext       │
   │  ─ resolves {{BT-X}}, {{BG-X.BT-Y}}, format modifiers          │
   │  ─ expands {{#BG-X}}…{{/BG-X}} loops                           │
   │  ─ drops {{?BT-X}}…{{/BT-X}} blocks whose target is empty      │
   │  ─ leaves {{class:…}} and {{pagebreak}} as sentinel tokens     │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                  Substituted CommonMark document (String)
                                     │
                                     ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 2 — Parse  (phlox-template)                             │
   │  commonmark-java + TablesExtension + StrikethroughExtension    │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                            commonmark Node tree
                                     │
                                     ▼
   ┌────────────────────────────────────────────────────────────────┐
   │  STAGE 2a — Post-pass  (phlox-template)                        │
   │  ─ lifts {{class:…}}/{{pagebreak}} sentinels out of Text nodes │
   │    and attaches them as sidecar Node properties                │
   │  ─ resolves image URLs (data:, attachment:, classpath:);       │
   │    rejects http/https/file at parse time                       │
   └─────────────────────────────────┬──────────────────────────────┘
                                     ▼
                       PhloxTemplateAst (Node + sidecars)
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

Each stage has well-defined inputs and outputs and is independently testable. Stages 0/0a depend only on the template + locale; stages 1, 2 and 2a depend on those + the invoice; stage 3 diverges per output format.

## Artifacts and caching

Every stage produces an immutable, cacheable artifact. The shape of the cache key tells you when each cache helps:

| Artifact | Produced by | Cache key | Hit rate in practice |
|---|---|---|---|
| `PhloxTemplate` | `PhloxTemplate.fromString/Classpath/File` | the source path | per process — typically one per template |
| `PhloxLocalizedTemplate` | `PhloxTemplate.localize(Locale)` (stage 0 + 0a) | template + locale + label-resolver identity | very high for a server: N templates × M locales is small |
| substituted `String` (CommonMark) | `localized.renderSubstituted(invoice, ctx)` (stage 1) | localized + invoice + context | low — invoice-specific. Cache only when an upstream layer pre-computes invoices that will be re-rendered |
| `PhloxTemplateAst` | `localized.renderAst(invoice, ctx)` (stages 1 + 2 + 2a) | same as above + image-bag identity | low for the same reason; high when the same invoice is rendered to BOTH HTML and PDF — the AST is reused across both emitters |
| rendered HTML / PDF | `HtmlInvoiceWriter` / `PdfInvoiceWriter` (stage 3) | same as AST + format flags | one-shot; callers cache the bytes themselves if needed |

The biggest practical win is `PhloxLocalizedTemplate`: every `{{label:…}}` lookup happens once at `localize()` time and the result is interpolated into the source. A server rendering the same template in the same locale for N invoices pays the label-resolver cost once instead of N × `labelCount` times.

`PhloxTemplate.renderAst(invoice, ctx)` is a one-line convenience that calls `localize(ctx.getLocale())` and then `renderAst(invoice, ctx)`. The engine keeps a small internal cache keyed by (this template, locale, label-resolver identity) so callers who don't manage the localized variant explicitly still get the benefit. Callers who want explicit control hold on to a `PhloxLocalizedTemplate` and call `renderAst(invoice, ctx)` on it directly — bypassing the cache lookup.

## Stage 0 — Localize

### What it does

The localize stage is a pure string transformation. It walks the raw `.ptpl` source and:

1. **Strips `{{! comment }}` blocks**, both inline single-line and block (closing `}}` on its own line). Removing comments up front avoids a parser pitfall — a multi-line comment that contains a literal `{{label:…}}` example would otherwise terminate at the inner `}}` and leak the rest of the comment text into the substitution stream.
2. **Substitutes every `{{label:KEY}}`** by looking `KEY` up via `IPhloxLabelResolver.getLabel(KEY, locale)`. The lookup uses the locale and resolver from the supplied `RenderContext`. Missing keys log a WARN and emit the key itself, matching the fall-back rule used in the hard-coded 0.1.0 writers.
3. **Encodes `{{?X}}…{{/X}}` directives** into `{{#?ENCODED}}…{{/?ENCODED}}` so JMustache's section-name parser sees a single name even when the encoded path contains a dot. The decode is undone by the runtime fetcher when it evaluates the presence check during stage 1.

Output: a plain `String` that is still a `.ptpl`-shaped document — placeholders, loops, conditionals all intact — but with no `{{label:…}}` calls left and no `{{! … }}` comment regions. The string is then handed to stage 0a.

### Why bake at this stage

Labels are the **only** template construct that depends on locale but **not** on the invoice. Pulling them into a dedicated stage gives them a cache key that doesn't include the invoice, which is the largest source of cache-miss noise. Pre-baking also lets the rest of the engine treat the substituted text as a plain Mustache template — no special-case handling for dotted label keys, no need to escape `.` inside `{{label:section.foo.bar}}`.

## Stage 0a — Compile

JMustache compiles the locale-baked source into a `Template`. The `Template` is internally a re-entrant interpreter state; one instance can serve many threads.

The compilation step is **separate** from stage 0 because the locale-baked source is itself a useful artifact (snapshot tests, debugging, hot-reload). Callers who want only the substituted text can stop after stage 0; the engine API exposes both.

`PhloxLocalizedTemplate` wraps both the locale-baked source (kept for diagnostics) and the compiled `Template`. It is immutable after construction.

## Stage 1 — Substitute

### What it does

JMustache walks the compiled `Template` and substitutes placeholders for actual values, producing a **plain `String`** that is itself a valid CommonMark document.

Because labels were already resolved in stage 0, the only constructs the runtime fetcher sees are:

- `{{BT-X}}`, `{{BG-X.BT-Y}}` — model paths.
- `{{BT-X | modifier}}` — formatted leaves.
- `{{#BG-X}}…{{/BG-X}}` — iteration / truthy sections.
- `{{?ENCODED}}…{{/ENCODED}}` — presence checks emitted by stage 0.
- `{{class:…}}`, `{{pagebreak}}` — sentinels.
- `logo`, `references_present`, `line_extras_present` — synthesized booleans.

### `PhloxInvoiceMustacheContext`

The runtime fetcher held by every compiled `Template`. It receives `(ctx, name)` from JMustache and dispatches by name shape, walking the BG/BT registry against `ctx`. Per-render state (invoice, render context, locale, label resolver) lives in a `PhloxRenderEnv` bound to the thread via a `ThreadLocal` during `Template.execute(…)`.

### Resolution algorithm

For a placeholder `{{name | modifier:arg}}`:

1. **Parse the placeholder.** Split on `|`; trim whitespace. First token is the name path; remaining tokens are modifiers.
2. **Dispatch by shape:**
   - `class:foo` → emit the sentinel `[[PHLOX:CLASS=foo]]` (post-pass picks this up).
   - `pagebreak` → emit the sentinel `[[PHLOX:PAGEBREAK]]`.
   - `BT-N` or `BG-N.BT-M` or `BG-N.BG-M.BT-K` → walk the model.
   - `references_present`, `line_extras_present`, `logo` → synthesized booleans (see below).
3. **Walk the model.** Split the path on `.`. Each segment is either a BG name (push into the BG instance) or a BT name (resolve to a field value).
4. **Apply default formatter** based on the EN 16931 semantic type recorded in [[Domain Model]]:
   - `Amount` → `PhloxFormat.amount(val, BT-5 currency, locale)`
   - `Unit Price Amount` → same as Amount; subtype hook for extra fraction digits
   - `Date` → `PhloxFormat.date(val, locale)` (medium style)
   - `Code` → `PhloxFormat.codeListLabel(<inferred-list>, val, locale)`
   - `Percentage` → `PhloxFormat.percent(val, locale)`
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
| `amount` | `PhloxFormat.amount(val, getCurrency(BT-5), locale)` |
| `amount:BT-6` | Same but currency comes from BT-6 — used for BT-111. |
| `percent` | `NumberFormat.getPercentInstance(locale)`; two decimals. |
| `code:<listName>` | `PhloxFormat.codeListLabel(<listName>, val, locale)` — drives the `phlox-codelists_<lang>.properties` lookup. |
| `raw` | Returns the value's `toString()` without HTML-escaping or formatting. |
| `upper` / `lower` / `title` | `String.toUpperCase(locale)` / `.toLowerCase(locale)` / title case via ICU. |

Modifiers are registered via a small SPI so custom format hooks can be added without forking the engine.

### Output of stage 1

A `String` containing valid CommonMark. Looking at one fully-substituted line:

Source (in `.ptpl`):
```
| {{BT-126}} | **{{BG-31.BT-153}}** | {{BT-129}} {{BT-130 | code:unit}} | {{BG-29.BT-146 | amount}} |
```

After substitution (assuming line 1, "Widget A", quantity 10 pieces, unit price €5.00):
```
| 1 | **Widget A** | 10 piece | €5.00 |
```

That second string is what stage 2 receives.

## Stage 2 — Parse, and Stage 2a — Post-pass

The two stages share a class (`PhloxTemplatePostParser`) and are run back-to-back; they appear separately in the diagram because they have different cache-key shapes (stage 2 depends only on the substituted string; stage 2a additionally depends on the invoice's image attachments and the render context's logo).

### Standard commonmark-java

```java
final List <Extension> exts = List.of (TablesExtension.create (), StrikethroughExtension.create ());
final Parser parser = Parser.builder ().extensions (exts).build ();
final Node root = parser.parse (sSubstituted);
```

The resulting `Node` tree is standard commonmark-java — `Document`, `Paragraph`, `Heading`, `BulletList`, `OrderedList`, `Table`, `TableRow`, `TableCell`, `Text`, `Emphasis`, `StrongEmphasis`, `Strikethrough`, `Code`, `Link`, `Image`, etc.

### Sentinel post-pass

After the standard parse, a `PhloxTemplatePostParser` walks the tree and processes the two phlox-specific sentinels stage 1 emitted:

1. **`CLASS=name`** — appears as content of a `Text` node, often standalone in a `Paragraph`. The post-pass:
   - Removes the sentinel from the `Text` node.
   - If the `Text` is now empty, removes the enclosing `Paragraph`.
   - Attaches `name` as the `class` property on the **next sibling block** (via a sidecar map keyed by `Node` identity, since commonmark-java `Node` doesn't expose arbitrary user properties).

2. **`PAGEBREAK`** — same treatment, but produces a synthetic `PhloxPageBreakNode` (a custom `Node` subclass) in place of the original paragraph. Emitters recognise this type.

### Image URL post-pass

In the same post-pass, every `Image` node's `destination` is normalised:

| URL scheme | Action |
|---|---|
| `data:` | Left as-is; emitters consume the data URI verbatim. |
| `attachment:logo` | Replaced with a `PhloxResolvedImage` placeholder pointing at `RenderContext.getLogo()`. |
| `attachment:BG-24[i]` | Replaced with a `PhloxResolvedImage` pointing at `aInvoice.getSupportingDocuments().get(i).getAttachment()`. Out-of-bounds → `PhloxTemplateException`. |
| `classpath:/…` | Resolved via `IReadableResource` on the writer's classpath; replaced with a `PhloxResolvedImage` that carries the bytes. |
| `http:` / `https:` / `file:` | Rejected at this stage — `PhloxTemplateException` with the AST location. |

Resolution happens here, not in stage 3, so the two emitters share the URL-scheme validation and so an out-of-bounds attachment index fails fast for any output format.

### Output of stage 2

A `PhloxTemplateAst` value object holding:

- `root` — the (post-processed) commonmark `Node` tree.
- `blockClassMap : Map<Node, String>` — block-attribute classes.
- `imageMap : Map<Image, PhloxResolvedImage>` — resolved image references.
- `invoice` — the source `PhloxInvoice` (so emitters can re-resolve things like the currency for any explicit fall-back, though most resolution is already done).
- `context` — the `RenderContext` reference (so emitters can read the CSS prefix, font specs, etc.).

`PhloxTemplateAst` is immutable after construction.

## Stage 3 — Emit

Each writer module ships an emitter that subclasses commonmark-java's `AbstractVisitor`. They share the visit pattern; they differ only in the output API.

### `HTMLTemplateEmitter` (phlox-html)

Walks the AST and builds an `HCNode` tree, the same shape `HtmlInvoiceWriter` produces from its hard-coded section grammar. Mapping:

| AST node | `HCNode` output |
|---|---|
| `Heading(level=1)` | `HCH1` with `addClass("phlox-title")` |
| `Heading(level=2)` | `HCH2` with `addClass("phlox-section-title")` |
| `Heading(level=3)` | `HCH3` with `addClass("phlox-subsection-title")` |
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
| `PhloxPageBreakNode` | `HCDiv` with `style="page-break-after: always"` |
| Text | `HCTextNode` (escapes automatically) |

The block-class map is consulted at each block visit and the corresponding class is prefixed with `HtmlRenderContext.getCssClassPrefix()` and added to the emitted `HCNode`. The CSS prefix logic is the same as in the hard-coded writer.

### `PDFTemplateEmitter` (phlox-pdf)

Walks the AST and builds `PLPageSet` content via `ph-pdf-layout`:

| AST node | `ph-pdf-layout` output |
|---|---|
| `Heading(level=1..3)` | `PLText` with `PdfRenderContext.getHeaderFont()`, size decreasing per level |
| `Paragraph` | A single `PLRichText` constructed by feeding the paragraph's serialised inline markup through ph-pdf-layout's CommonMark inline parser |
| `BulletList` / `OrderedList` | The list-block helper from ph-pdf-layout's CommonMark-compatible block module |
| `BlockQuote` | Indented `PLBox` with a left border |
| `FencedCodeBlock` | Indented `PLText` with a monospace `FontSpec` |
| `Table` | `PLTable`; column count and alignment from the AST; column widths from a `widths=` attribute on the table (default: even split); header row styled with `PdfRenderContext.getTableHeaderFont()` |
| `ThematicBreak` | A thin horizontal `PLBox` border line |
| `Image` (resolved) | `PLImage` (block-level; if it appears inline within a `Paragraph`, the post-pass already split the paragraph around it) |
| `PhloxPageBreakNode` | `PLPageBreak` |
| Inline nodes | Folded back into the paragraph's CommonMark source before handing to ph-pdf-layout's inline parser, so `**bold**`, `*italic*`, `~~strike~~`, `` `code` ``, `[link](…)` all render natively |

The block-class map drives `PLTableCell` fill colours and `PLBox` background colours via a style map exposed on `PdfRenderContext`. The default style map provides one entry (`totals-due` → light grey fill); callers add more via `PdfRenderContext.putBlockStyle("name", BlockStyle)`.

### Inline strategy

The PDF emitter does **not** walk inline nodes itself. For each `Paragraph` (or table cell) it serialises the inline AST back to CommonMark, then hands that string to `ph-pdf-layout`'s CommonMark inline parser. This keeps the inline rendering in lock-step between the two writers — both end up parsing the same CommonMark inline grammar — and avoids reimplementing ten kinds of styling per emitter.

The HTML emitter does walk inline nodes itself, because `HCNode` types map straight from commonmark inline types with no lossy intermediate string form.

## Threading and caching

- **`PhloxTemplate` is immutable after construction.** A single instance can be used concurrently from many threads without synchronisation. It holds the raw `.ptpl` source plus a commonmark-java `Parser`; the compiled JMustache `Template` lives one layer down, on `PhloxLocalizedTemplate`.
- **`PhloxLocalizedTemplate` is immutable after construction.** Produced by `PhloxTemplate.localize(Locale)`; carries the locale-baked source string and the compiled JMustache `Template`. Safe to share across threads.
- **Internal localization cache.** `PhloxTemplate` keeps a small `ConcurrentHashMap<CacheKey, PhloxLocalizedTemplate>` keyed by (locale, label-resolver identity). The default label resolver is a singleton so the key is effectively just `Locale`; callers using a custom resolver must pass the same instance to benefit from caching. The map is bounded by an `IPhloxTemplateCachePolicy` (default: unbounded, since the cardinality is small).
- **commonmark-java's `Parser`** is cached as a field on `PhloxTemplate`. Both `Parser` and `AbstractVisitor` subclasses are thread-safe per commonmark-java's documentation.
- **Per-render allocations:** one `PhloxRenderEnv`, one substituted `String`, one `Node` tree, one `PhloxTemplateAst`. Nothing on the hot path requires locking once the localized template is cached.

For a server rendering many invoices through the same template in the same locale — the expected workload for an embedded `phlox` — the steady-state cost per invoice is **one stage 1 + 2 + 2a pass**, with stages 0 / 0a and the Parser construction amortised over the JVM's lifetime. Cross-locale rendering for the same template costs one localize per locale, paid lazily on first use.

## Error handling

Errors carry a `PhloxTemplateException` that includes:

| Field | Source |
|---|---|
| `stage` | `SUBSTITUTE`, `PARSE`, or `EMIT`. |
| `line`, `column` | Source position in the original `.ptpl`. JMustache exposes this for stage 1; the post-pass tracks AST node positions for stages 2 and 3. |
| `path` | The BG/BT path being resolved when the error fired (e.g. `BG-25[3].BG-29.BT-146`). Empty for non-resolution errors. |
| `message` | Human-readable explanation. |
| `cause` | The underlying exception, if any. |

### What raises an error

| Stage | Trigger | Behaviour |
|---|---|---|
| Substitute | Unknown placeholder name | `PhloxTemplateException` — fail fast. |
| Substitute | Format modifier not recognised | `PhloxTemplateException`. |
| Substitute | `{{label:foo}}` returns the key (resolver miss) | Logged at WARN once per missing key, output is the key itself — same fallback as the hard-coded writer. **Not an error.** |
| Substitute | BT value is `null` / empty for `{{BT-X}}` | Renders empty (or `—` when `IncludeEmptySections=true`). **Not an error.** |
| Substitute | `{{?BT-X}}` block — target falsy | Skipped silently. **Not an error.** |
| Parse | GFM table malformed (wrong column count, etc.) | commonmark-java drops the offending row. Logged at WARN. |
| Parse | Image URL uses rejected scheme (`http:`, `https:`, `file:`) | `PhloxTemplateException`. |
| Parse | `attachment:BG-24[i]` index out of range | `PhloxTemplateException`. |
| Emit | Block class referenced but no entry in style map | Logged at WARN; block renders unstyled. **Not an error.** |
| Emit | Image resolution returned empty bytes | `PhloxTemplateException`. |

The principle: data-shape errors that originate in the **invoice** (missing BTs, unset optional sections) are silent — that's the normal case. Errors that originate in the **template** (unknown name, bad modifier, bad URL) fail fast — that's a developer bug and we want it visible.

## Integration with writers

```java
public final String write (@Nonnull final PhloxInvoice aInvoice,
                            @Nonnull final HtmlRenderContext aCtx)
{
  final IPhloxTemplate aTpl = aCtx.getTemplate ();
  if (aTpl != null)
  {
    // Convenience entry point: PhloxTemplate.renderAst(invoice, ctx) localizes
    // on demand and uses the engine's internal (locale → PhloxLocalizedTemplate)
    // cache. A caller wanting explicit control can call
    // `aTpl.localize(aCtx.getLocale())` once, hold the result, and feed every
    // invoice through it directly — bypassing the cache lookup.
    final PhloxTemplateAst aAst = ((PhloxTemplate) aTpl).renderAst (aInvoice, aCtx);
    final HCHtml aHtml = new HTMLTemplateEmitter (aCtx).emit (aAst);
    return HCRenderer.getAsHTMLString (aHtml, HCSettings.getMutableConversionSettings ());
  }
  // Fall through to the 0.1.0 section renderers
  return _writeWithDefaultGrammar (aInvoice, aCtx);
}
```

The decision is one null-check. When unset, the template engine is not touched — none of `phlox-template`'s code paths execute, none of JMustache or commonmark-java is loaded into the class verifier. The hard-coded section grammar from [[Output Writers]] is the unchanged 0.1.0 behaviour.

`PdfInvoiceWriter` follows the same shape with `PDFTemplateEmitter` and `PdfRenderContext`. When the same invoice is rendered into both HTML and PDF, callers can build the `PhloxTemplateAst` once and pass it to both emitters explicitly via `HTMLTemplateEmitter.emit(ast)` / `PDFTemplateEmitter.emit(ast, out)`, paying stages 1 and 2 only once.

## Loading the default template

```java
// Shorthand for the default ntpl shipped in phlox-template
final IPhloxTemplate aDefault = PhloxTemplate.getDefault ();

new HtmlInvoiceWriter ().writeAsString (aInvoice,
    new HtmlRenderContext ().setLocale (Locale.GERMAN).setTemplate (aDefault));
```

`getDefault()` returns a singleton `PhloxTemplate` built from `classpath:/templates/default.ptpl`. The same instance is shared across all callers (immutable after construction).

For 0.4.0 the writers do **not** fall through to the default template when `getTemplate() == null` — they fall through to the hard-coded section renderers, preserving 0.1.0 byte-for-byte output. A future release may flip the default; that would be a deliberate behaviour change called out in [[News and noteworthy]].

## Performance

Asymptotics per pipeline stage:

| Stage | Per | Cost | Allocations |
|---|---|---|---|
| 0 Localize | (template, locale) | O(template size) + N label lookups (N = number of `{{label:…}}` occurrences) | one locale-baked `String` |
| 0a Compile | (template, locale) | O(locale-baked size) — the JMustache compile | one `Template` |
| 1 Substitute | (template, locale, invoice) | O(locale-baked size) | one substituted `String` |
| 2 Parse | substituted string | O(substituted size) | the `Node` tree |
| 2a Post-pass | (Node tree, invoice) | O(node count) | a few `Map` entries per `{{class:…}}` / image |
| 3 Emit (HTML) | `PhloxTemplateAst` | O(node count) | the `HCNode` tree |
| 3 Emit (PDF) | `PhloxTemplateAst` | O(node count) + ph-pdf-layout's layout pass | `PLPageSet` content + per-page line breaks |

For a typical EN 16931 invoice (~50 lines of source, 15-20 lines in the output) all stages combined fit in milliseconds on commodity hardware. The cost is dominated by the PDF layout pass; templates add a few percent overhead.

Stages 0 and 0a are amortised by the localize cache — for a server rendering the same template repeatedly in the same locale, those stages run once at first use and never again.

## See also

- [[Template Language]] — the `.ptpl` grammar (Mustache + CommonMark + GFM).
- [[Output Writers]] — the default section grammar that runs when no template is set.
- [[Domain Model]] — the BG/BT inventory the Mustache adapter resolves against, plus the semantic-type vocabulary that picks the default formatter.
- [[Localization]] — `IPhloxLabelResolver` and the resource bundles `{{label:…}}` reads from.
- [[Configuration Properties]] — `setTemplate(...)` on `AbstractRenderContext`.
- [[Maven Module Structure]] — where each pipeline stage lives (`phlox-template`, `phlox-html`, `phlox-pdf`).
