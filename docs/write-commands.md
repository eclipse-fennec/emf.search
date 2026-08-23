# Write commands

Beside `Resource.save()` — "here is an object, store it" — the persistence stack has a
second way to write: a **command**, addressed by a *selector* rather than by an object.
Delete everything cheaper than ten euros; raise the price of every discontinued product.
The vocabulary is the stack's, so the same command runs over JPA, Mongo and this index.

```java
CommandResource commands = (CommandResource) resourceSet.createResource(
        URI.createURI("lucene://catalog/Product"));

DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
delete.setSelector(QueryBuilder.from(product).where(path(price).lt(10.0)).build());

long affected = commands.execute(delete);
```

| Command | Over Lucene |
|---|---|
| `InsertCommand` | each payload object is mapped and written under its id — save semantics, so re-inserting the same id replaces it |
| `DeleteCommand` | the selector is translated like any query, and the matches are removed **as whole blocks** |
| `UpdateCommand` | read, patch, rewrite — **only** for classes whose mapping declares `STORED_OBJECT` materialization |

`execute(command, parameters, options)` binds the selector's parameters, exactly like the
query overload; a selector using `param(...)` without bindings is refused rather than
matched against something arbitrary.

## Why update is conditional

Lucene has no partial update. Changing one field means rewriting the whole document, so the
backend must be able to *reconstruct* what it is about to change — and by default the index
holds the mapped fields alone. Rebuilding from those would silently drop everything the
mapping never stored, and a lossy write is worse than a refusal.

So `UPDATE_BY_SELECTOR` is declared **narrowed**: the backend serves it for the classes whose
mapping declares [`STORED_OBJECT` materialization](./materialization.md), and refuses it for
the rest — before any work, with a diagnostic that names the feature and the way out.

```java
capabilities.command().supports(UPDATE_BY_SELECTOR);            // true — the backend can
capabilities.command().supports(UPDATE_BY_SELECTOR, product);   // depends on the mapping
```

That is the two-level capability contract: the backend-wide answer says what the engine can
do at all, the per-class answer is the routing truth. A consumer asks the second one before
sending an update.

## What a selector may be

A command selector is a **plain filter**: predicates only. Projection, aggregation, ordering,
paging, `distinct`, `countOnly` and `expand` have no meaning when the answer is "what to
write", and are refused rather than ignored — a selector must mean the same thing here as it
does in a query.

Everything else about it behaves exactly like a query, because it *is* one: the same
translator, the same capability refusals, the same three-valued negation. A selector the
query side refuses is refused here too — and refused means **nothing is written**, never
"fell back to everything".

## Deleting takes the whole object

A mapped object can be a block of documents (one per `NESTED` child). Deleting the matched
root documents alone would leave the children behind as orphans, so a delete resolves its
matches to root ids first and removes the blocks under them.

**A delete that would leave a reference pointing at nothing is refused** — the same rule as
[deleting through a resource](./materialization.md): every `ID_ONLY` reference field that
could carry a matched id is probed first. References *between* objects of the same delete do
not block it; they disappear together.

## Updating

Every match is read complete, patched with the `ChangeSet` template and re-mapped **before
the first document is written**. A template that fails on the third of five matches leaves
the index as it was, rather than half-applied.

The template's reference entries address their targets by id: the id is looked up in this
unit, and bound as the same proxy an `ID_ONLY` reference hands back anywhere else. A target
that does not exist is refused as dangling, not written as a broken reference.

## Transactions: refused, and declared as refused

`begin()` throws, with a diagnostic naming `TRANSACTION_BRACKET`.

`IndexWriter.rollback()` discards **every** uncommitted change in the unit, not the calling
thread's share of it. A bracket over that would only be sound while a single writer owns the
unit for its whole duration — a condition this backend cannot enforce, and a promise it
therefore will not make. The store capability is absent, so a consumer can route around it
instead of discovering it at runtime.

If you need several writes to become visible together, the unit's own
[commit policy](./index-units.md) is the tool: writes become visible when the unit refreshes,
and a caller that controls the commit controls the visibility boundary. A per-caller bracket
would require serializing all writes to the unit — the documented upgrade path, unbuilt
until a consumer needs it badly enough to pay for it.

## Visibility

A command writes through the unit's `IndexWriter` like every other write here, so its result
becomes findable when the unit's [refresh policy](./index-units.md) says so — not the moment
`execute()` returns. `execute()` reports how many objects it affected; seeing them is a
question about the unit, not about the command.
