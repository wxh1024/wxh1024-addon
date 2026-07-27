# CODEBUDDY.md This file provides guidance to CodeBuddy when working with code in this repository.

## Commands

### Build
```bash
./gradlew build
```
Compiles the project and produces the JAR at `build/libs/wxh1024-addon-<version>.jar`. Requires JDK 21. Uses Fabric Loom 1.9 with Yarn mappings to resolve Minecraft obfuscated names.

### Clean build
```bash
./gradlew clean build
```
Wipes the `build/` directory before recompiling. Useful after changing `fabric.mod.json` or when Gradle caching causes stale artifacts.

### Update dependencies
```bash
./gradlew --refresh-dependencies
```
Forces re-download of Meteor Client snapshots, Fabric Loader, and Yarn mappings from remote repositories.

## Architecture

### Project Identity
This is a **Meteor Client Addon** for Minecraft 1.21.11 (Java Edition), running on Fabric Loader. It adds a custom module category `wxh1024` to the Meteor Client GUI and provides two gameplay-enhancing modules. The mod ID is `wxh1024-addon`, package root is `com.wxh1024.addon`.

### Entry Point and Registration
`Wxh1024Addon` (extending `MeteorAddon`) is the sole entry point, declared in `fabric.mod.json` under the `meteor` entrypoint key. Meteor Client discovers addons by scanning Fabric mod metadata for this key.

On startup, two lifecycle methods fire:
- `onRegisterCategories()` — registers the `WXH1024` category (static field `Category("wxh1024")`) via `Modules.registerCategory()`. Category instances are compared by name hash; registering a duplicate name will create a visible duplicate in the GUI.
- `onInitialize()` — instantiates and adds both modules to the Meteor module registry via `Modules.get().add()`.

The `getPackage()` method returns `"com.wxh1024.addon"` so Meteor knows where to scan for annotated event handlers within the addon.

### Module Pattern
Every module extends `meteordevelopment.meteorclient.systems.modules.Module`. The constructor calls `super(category, name, description)` passing the custom category. Each module is a singleton: created once in `onInitialize()` and managed by Meteor's module system thereafter.

Modules define settings as fields using Meteor's Settings DSL:
- `settings.getDefaultGroup()` gets the primary `SettingGroup`
- `.add(new XxxSetting.Builder()...)` appends a setting to that group
- `.visible(() -> someCondition)` conditionally hides dependent settings in the GUI
- Setting values are read via `.get()` inside event handlers

### Event System
The addon uses **Orbit** (`meteordevelopment.orbit.EventHandler`), Meteor's event bus. Module methods annotated with `@EventHandler` receive events automatically. Two event types are used:

1. **`InteractEntityEvent`** — fired when the player right-clicks an entity. Used by `ItemFrameChestOpener` to intercept item frame interaction.
2. **`PlayerMoveEvent`** — fired on every player movement tick. Used by `Home` to continuously monitor totem count and height.

Event handlers have access to `mc` (the `MinecraftClient` instance) via the `Module` superclass. Standard null-safety checks on `mc.player`, `mc.world`, `mc.interactionManager`, and `mc.getNetworkHandler()` are required before accessing game state.

### Module: 展示框开箱 (ItemFrameChestOpener)
Intercepts right-clicks on `ItemFrameEntity` entities. On interaction, it:
1. Reads the item frame's `getHorizontalFacing()` direction
2. Computes the block position behind the frame (`getBlockPos().offset(facing.getOpposite())`)
3. Checks whether the `BlockEntity` at that position is one of nine supported container types, filtered by user-configurable boolean settings
4. Calls `event.cancel()` to suppress the original item frame interaction
5. Constructs a `BlockHitResult` targeting the container block's center and invokes `interactionManager.interactBlock()` to open the container

**Critical design detail**: Java Edition item frames are entities (not blocks), which is why `InteractEntityEvent` is used rather than `InteractBlockEvent`. The `isContainerEnabled()` method checks both the block entity type AND the corresponding `BoolSetting` — both must match for the container to open.

Supported container types with their corresponding settings: ChestBlockEntity (箱子), BarrelBlockEntity (木桶), ShulkerBoxBlockEntity (潜影盒), DispenserBlockEntity (发射器/投掷器), HopperBlockEntity (漏斗), FurnaceBlockEntity (熔炉/高炉/烟熏炉), EnderChestBlockEntity (末影箱), BrewingStandBlockEntity (酿造台), CrafterBlockEntity (合成器).

### Module: 自动回家 (Home)
Monitors player state and automatically sends chat commands (like `/home` or `/spawn`) when thresholds are breached. Evaluates two conditions on every `PlayerMoveEvent`:

- **Totem count**: Uses `InvUtils.find(Items.TOTEM_OF_UNDYING)` to scan the entire player inventory and count totems. Triggers when count is below `minTotems` (set to 0 to disable this check).
- **Height check**: Compares `mc.player.getY()` against `minHeight`. Disabled by default (`heightCheck` is `false`).

The module has a `triggered` latch flag — once conditions are met, it triggers once and only resets when conditions return to normal. This prevents repeated triggering on every movement tick. A `cooldown` setting (in seconds) enforces a minimum interval between triggers, using `System.currentTimeMillis()` for timing.

On trigger, the module can optionally: send a chat notification via `Module.info()`, execute a command via `ChatUtils.sendPlayerMsg()`, and toggle itself off. The `getInfoString()` override displays live totem count, height, and cooldown remaining in the module list HUD.

### Build System
**Fabric Loom 1.9** with Gradle 8.12. Version numbers are centralized in `gradle.properties`:
- `minecraft_version=1.21.11` and `yarn_mappings=1.21.11+build.4` — version-locked mapping pair; upgrading Minecraft requires finding the matching Yarn build
- `meteor_version=1.21.11-SNAPSHOT` — points to Meteor's snapshot Maven repository for the latest dev build targeting 1.21.11
- `loader_version=0.19.3` — Fabric Loader version

Two Maven repositories are configured: `maven.meteordev.org/releases` and `maven.meteordev.org/snapshots`. The Meteor Client dependency comes from these repos. Fabric Loader and intermediary mappings come from `maven.fabricmc.net`.

`processResources` expands `${version}` in `fabric.mod.json` from `gradle.properties` at build time. Java source/target is set to 21, matching Minecraft 1.21.x's requirement.

### fabric.mod.json Custom Fields
- `meteor-client:color`: RGB value `"0,255,255"` (cyan) — sets the color of the custom category icon in Meteor's module list
- `modmenu.parent.id`: `"meteor-client"` — when Mod Menu is installed, nests this addon under Meteor Client in the mod list rather than showing it as a top-level entry
