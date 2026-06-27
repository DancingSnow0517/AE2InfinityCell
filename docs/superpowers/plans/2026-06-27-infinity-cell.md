# AE2 Infinity Cell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement one real-storage AE2 cell item that supports both items and fluids with UUID-backed per-save external storage.

**Architecture:** The cell item NBT stores only a UUID. `InfinityCellSavedData` stores UUID records in the current save, and channel-specific AE inventory handlers mutate those records. Compact key classes convert item/fluid stacks to stable map keys and back.

**Tech Stack:** Minecraft Forge 1.7.10, GTNH Gradle convention, Applied Energistics 2 rv3 GTNH API, JUnit 4 tests.

---

## File Structure

- Modify `dependencies.gradle`: add JUnit 4 test dependency.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/item/ModItems.java`: item singleton and registration.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/item/ItemInfinityStorageCell.java`: storage cell item and UUID NBT helpers.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/ItemStackKey.java`: compact item identity and item stack reconstruction.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/FluidStackKey.java`: compact fluid identity and fluid stack reconstruction.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/NbtKey.java`: deterministic copied NBT identity for map keys.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellRecord.java`: item/fluid maps, inject/extract, NBT serialization.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellSavedData.java`: `WorldSavedData` root map.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellDataAccess.java`: server-world lookup and record acquisition.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityCellHandler.java`: AE cell handler registration adapter.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/ae/AbstractInfinityInventoryHandler.java`: shared AE inventory behavior.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityItemInventoryHandler.java`: item channel handler.
- Create `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityFluidInventoryHandler.java`: fluid channel handler.
- Modify `src/main/java/cn/dancingsnow/aeinfinitycell/CommonProxy.java`: register item and cell handler.
- Create `src/main/resources/assets/aeinfinitycell/lang/en_US.lang`: display name and tooltip.
- Create focused tests under `src/test/java/cn/dancingsnow/aeinfinitycell/storage/`.

## Tasks

### Task 1: Test Dependency and Storage Key Tests

**Files:**
- Modify: `dependencies.gradle`
- Create: `src/test/java/cn/dancingsnow/aeinfinitycell/storage/NbtKeyTest.java`
- Create: `src/test/java/cn/dancingsnow/aeinfinitycell/storage/FluidStackKeyTest.java`

- [ ] Add `testImplementation("junit:junit:4.13.2")` to `dependencies.gradle`.
- [ ] Write failing tests that prove equal copied NBT tags produce equal `NbtKey` values, mutated source tags do not mutate the key, and fluid keys serialize by fluid name and optional NBT.
- [ ] Run `.\gradlew test --tests cn.dancingsnow.aeinfinitycell.storage.NbtKeyTest --tests cn.dancingsnow.aeinfinitycell.storage.FluidStackKeyTest`; expected failure is missing production classes.
- [ ] Implement `NbtKey` and `FluidStackKey`.
- [ ] Re-run the targeted tests and confirm they pass.
- [ ] Commit with message `test: add compact storage key coverage`.

### Task 2: Record Storage Tests

**Files:**
- Create: `src/test/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellRecordTest.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellRecord.java`

- [ ] Write failing tests for item/fluid inject, simulated extract behavior through map methods, zero-entry cleanup, and NBT round-trip.
- [ ] Run `.\gradlew test --tests cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecordTest`; expected failure is missing `InfinityCellRecord`.
- [ ] Implement `InfinityCellRecord` using `Map<ItemStackKey, Long>` and `Map<FluidStackKey, Long>`.
- [ ] Re-run the targeted test and confirm it passes.
- [ ] Commit with message `feat: add infinity cell record storage`.

### Task 3: Saved Data and UUID Identity Tests

**Files:**
- Create: `src/test/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellSavedDataTest.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellSavedData.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/storage/InfinityCellDataAccess.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/item/ItemInfinityStorageCell.java`

- [ ] Write failing tests for UUID parsing, UUID assignment helper, copied UUID sharing, saved-data record lookup, dirty marking, and saved-data NBT round-trip.
- [ ] Run `.\gradlew test --tests cn.dancingsnow.aeinfinitycell.storage.InfinityCellSavedDataTest`; expected failure is missing classes.
- [ ] Implement UUID helpers in `ItemInfinityStorageCell`, saved-data root map, and data-access helper.
- [ ] Re-run targeted tests and confirm they pass.
- [ ] Commit with message `feat: add uuid backed saved data`.

### Task 4: AE Handler Tests

**Files:**
- Create: `src/test/java/cn/dancingsnow/aeinfinitycell/ae/InfinityInventoryHandlerTest.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/ae/AbstractInfinityInventoryHandler.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityItemInventoryHandler.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityFluidInventoryHandler.java`
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/ae/InfinityCellHandler.java`

- [ ] Write failing tests for item inject/extract simulation, fluid inject/extract simulation, direct available lookup, cell cache status, and handler type routing.
- [ ] Run `.\gradlew test --tests cn.dancingsnow.aeinfinitycell.ae.InfinityInventoryHandlerTest`; expected failure is missing handlers.
- [ ] Implement channel-specific inventory handlers and `InfinityCellHandler`.
- [ ] Re-run targeted tests and confirm they pass.
- [ ] Commit with message `feat: add ae infinity cell handlers`.

### Task 5: Registration and Resources

**Files:**
- Create: `src/main/java/cn/dancingsnow/aeinfinitycell/item/ModItems.java`
- Modify: `src/main/java/cn/dancingsnow/aeinfinitycell/CommonProxy.java`
- Create: `src/main/resources/assets/aeinfinitycell/lang/en_US.lang`
- Create: `src/main/resources/assets/aeinfinitycell/textures/items/infinity_storage_cell.png`

- [ ] Write or update a compile-facing test if registration helpers expose state; otherwise rely on compile verification.
- [ ] Register `ItemInfinityStorageCell` through `GameRegistry.registerItem`.
- [ ] Register `InfinityCellHandler` through AE's cell registry during init.
- [ ] Add language and a minimal item texture.
- [ ] Run `.\gradlew compileJava`; expected success.
- [ ] Commit with message `feat: register infinity storage cell`.

### Task 6: Final Verification

**Files:**
- All changed files.

- [ ] Run `.\gradlew test`.
- [ ] Run `.\gradlew compileJava`.
- [ ] Run `.\gradlew build` if `test` and `compileJava` pass.
- [ ] Run `git status --short`.
- [ ] Commit any remaining generated docs or resource changes with a focused message.
