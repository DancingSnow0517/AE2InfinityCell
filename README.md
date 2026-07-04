# AE2 Infinity Cell

AE2 Infinity Cell adds a single Applied Energistics 2 storage cell for
Minecraft 1.7.10 / GTNH-era modpacks. The cell mounts item, fluid, and essentia
storage channels in AE2 drives while keeping the actual contents in
save-scoped external storage instead of writing a large inventory directly into
the item stack.

## Features

- One `Infinity Storage Cell` item for AE2 storage systems.
- Item, fluid, and Thaumic Energistics essentia channel support.
- Effectively unlimited storage capacity and type count from AE2's point of view.
- Lightweight item NBT: the cell stores only a UUID reference.
- Contents are persisted in the current world save and written through the
  mod's saved-data storage.
- Copied cells keep the same UUID and intentionally share the same backing
  inventory inside that save.

## Requirements

Runtime dependencies:

- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1614
- Applied Energistics 2 Unofficial
- Thaumic Energistics
- Thaumcraft
- Avaritia

The development workspace also uses the GTNH Gradle convention plugin and GTNH
dependency catalog entries.

## Usage Notes

Place the Infinity Storage Cell in an AE2 drive or compatible AE storage host.
AE2 will ask the custom cell handler for the requested channel and the cell will
serve item, fluid, or essentia storage from the same backing record.

The backing storage is scoped to the Minecraft save. Copying the item stack
within the same save copies the UUID and shares the same contents. Moving a cell
NBT into another save only carries the UUID string; it does not carry the saved
record unless the external saved data is moved too.

This repository currently registers the item and assets, but does not define a
crafting recipe.

## Building

On Windows:

```powershell
.\gradlew.bat build
```

On Unix-like shells:

```sh
./gradlew build
```

Useful focused checks:

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat processResources
```

## Project Layout

- `src/main/java/cn/dancingsnow/aeinfinitycell/item` - item registration and
  cell UUID helpers.
- `src/main/java/cn/dancingsnow/aeinfinitycell/ae` - AE2 cell handler and
  channel-specific inventory handlers.
- `src/main/java/cn/dancingsnow/aeinfinitycell/storage` - external storage
  records, keys, and saved-data access.
- `src/main/resources/assets/aeinfinitycell` - item language and texture assets.
- `src/test/java/cn/dancingsnow/aeinfinitycell` - focused storage tests.

## License

See the bundled `LICENSE` file for the packaged license text.
