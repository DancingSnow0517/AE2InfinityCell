# AE2 Infinity Cell Design

## Goal

Build one Applied Energistics 2 storage cell item for Minecraft 1.7.10 / GTNH that provides real, writable storage for both item and fluid channels with effectively unlimited capacity and unlimited types.

The cell item must not persist its inventory directly in the `ItemStack` NBT. Its NBT stores only a storage UUID. The real inventory is stored in the current world's saved data.

## Selected Behavior

- The storage cell is one item.
- It supports both AE item storage and AE fluid storage.
- It is real storage: inserted amounts are recorded and extracted amounts are decremented.
- Capacity and type limits are intentionally unlimited from AE's point of view.
- Copying the cell item copies the UUID and therefore shares the same backing inventory. This is acceptable and intentional.
- The cell cannot share data across different Minecraft saves. The UUID points into the current save's `WorldSavedData` only.

## AE Integration

Register one custom item, `ItemInfinityStorageCell`.

Register one custom `ICellHandler`, `InfinityCellHandler`, through `AEApi.instance().registries().cell().addCellHandler(...)`.

`InfinityCellHandler` handles this item and returns channel-specific inventory handlers:

- `ITEM_STACK_TYPE` returns an item inventory handler.
- `FLUID_STACK_TYPE` returns a fluid inventory handler.
- Any other stack type returns `null`.

The custom handler is required because AE 1.7.10 storage channels are separate. A normal `IStorageCell` reports one `IAEStackType`, so it cannot cleanly expose one item as both an item cell and a fluid cell.

## Storage Identity

The cell item NBT contains one string UUID at a stable key, for example:

```text
aeinfinitycell.storageId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

When AE asks for a handler on the logical server:

1. If the cell has a valid UUID, use it.
2. If the cell has no UUID, generate one and write it to the cell NBT.
3. Look up or create the matching external record in world saved data.

UUID generation must not happen on the client. If handler creation is requested without a usable server world, return `null` or a non-mutating empty handler rather than creating a new identity.

## External Storage

Create one `WorldSavedData` implementation, `InfinityCellSavedData`, with one map:

```text
UUID -> InfinityCellRecord
```

Use the server overworld's `mapStorage` as the canonical owner. In Minecraft 1.7.10, `MapStorage` saves through the current save's `ISaveHandler#getMapFileFromName(...)`, so the data is stored under that Minecraft save's data files and does not naturally cross saves.

Do not use a static global inventory map as source of truth. A small static lookup helper may exist only to locate the server overworld and its saved data each time.

`InfinityCellRecord` contains:

- an item map: `ItemStackKey -> long count`
- a fluid map: `FluidStackKey -> long amount`

The record is created lazily for a UUID. Empty records may remain after cell deletion; automatic garbage collection is out of scope for the first implementation.

## Runtime Data Structures

`ItemStackKey` is a normalized identity for an AE item stack. It should include:

- item registry name or item id fallback
- metadata
- optional tag identity for NBT-sensitive items

`FluidStackKey` is a normalized identity for an AE fluid stack. It should include:

- fluid registry name
- optional fluid NBT identity

The runtime maps use these keys directly. The maps store counts as `long`.

## Serialization

The external saved data serializes records to NBT in a compact structure. It must not write a full `ItemStack` for every stored entry as the primary representation.

The first implementation may use NBT lists of compact compounds:

Item entry:

```text
id: string
meta: int
count: long
tag: optional compound or optional compressed key data
```

Fluid entry:

```text
id: string
amount: long
tag: optional compound or optional compressed key data
```

The implementation should keep tag storage optional and only write it when the stack type actually has NBT. The store layer should be isolated so a later binary or compressed chunk format can replace the NBT list format without changing the AE handler logic.

## Handler Semantics

For both item and fluid handlers:

- `injectItems(input, Actionable.SIMULATE, source)` returns `null` without mutating storage.
- `injectItems(input, Actionable.MODULATE, source)` adds the full input amount and returns `null`.
- `extractItems(request, Actionable.SIMULATE, source)` returns up to the stored amount without mutating storage.
- `extractItems(request, Actionable.MODULATE, source)` returns up to the stored amount and decrements storage.
- zero or negative incoming amounts are treated as no-op.
- entries with zero count are removed.
- `getAvailableItem(...)` should use a direct map lookup.
- `getAvailableItems(...)` enumerates current entries into the provided AE list.

The handler marks `InfinityCellSavedData` dirty after real mutations. It should call `ISaveProvider.saveChanges(...)` when a host is available so AE's containing drive or chest can also mark itself dirty.

## Capacity Reporting

Capacity should be reported as effectively unlimited:

- total bytes: `Long.MAX_VALUE`
- free bytes: `Long.MAX_VALUE`
- total types: `Long.MAX_VALUE`
- free types: `Long.MAX_VALUE`
- cell status: green/empty when both maps are empty, blue/has contents otherwise

The exact AE status values should follow `ICellHandler#getStatusForCell` conventions used by AE:

- `1` for empty
- `2` for usable with contents

The cell should be non-storable inside other AE storage cells to avoid recursive storage.

## World Lookup

Handler creation needs a server world to access `WorldSavedData`. Prefer server overworld:

```text
DimensionManager.getWorld(0)
```

Fallback only to other loaded server worlds if overworld is unavailable during startup. Never use `WorldClient` for identity generation or mutation.

If no server world exists, return no mutable handler. This prevents client-side UUID generation and prevents external records from being created outside a save.

## Registration and Assets

Register the item during common pre-init or init using the existing Forge 1.7.10 `GameRegistry.registerItem(...)` path.

Set:

- unlocalized name
- texture name under this mod id
- max stack size 1
- creative tab if an appropriate AE or miscellaneous tab is available

Add minimal `en_US.lang` and optionally `zh_CN.lang`. Texture can start as a simple generated or placeholder asset if the project has no asset pipeline yet.

## Testing

Use TDD for implementation.

Test the storage layer first because it is independent from AE drive behavior:

- a new cell receives a UUID only through the server-side identity path
- copied UUIDs resolve to the same record
- item inject and extract preserve exact `long` counts
- fluid inject and extract preserve exact `long` amounts
- simulation does not mutate maps
- zero-count entries are removed
- records serialize and deserialize without writing full cell inventory into the item NBT

Then test AE-facing handlers where practical:

- item handler returns item stack type and item channel
- fluid handler returns fluid stack type and fluid channel
- the custom cell handler returns the right handler for each requested stack type
- unsupported stack types return `null`

Final verification should include:

```text
./gradlew test
./gradlew compileJava
```

If the template has no test framework configured, add focused JUnit tests using the existing GTNH Gradle conventions if available; otherwise run `compileJava` and include manual test notes.

## Risks and Follow-ups

Large saved data files can still be expensive when the world saves. This design removes large inventory data from `ItemStack` NBT and AE item synchronization paths, but it does not make unlimited persisted data free.

NBT-sensitive item keys must be handled carefully. The first implementation should preserve NBT-distinct item identities, even if compact tag handling is conservative.

Orphaned records are expected when all copies of a cell are destroyed. A future admin command can list and remove orphaned UUID records.

Cross-save sharing is intentionally not supported. Copying a cell item NBT to another save copies the UUID string, but that UUID resolves against the other save's `InfinityCellSavedData`; if the record does not exist there, it creates a new empty record with the same UUID in that save.
