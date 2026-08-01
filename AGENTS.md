# AGENTS.md

## Build

Requires **JDK 21**. Build with:

```bash
./gradlew build          # output: build/libs/wxh1024-addon-<version>.jar
./gradlew clean build    # after changing fabric.mod.json or stale cache
```

No tests, linter, or CI exist. `./gradlew build` is the only verification step.

Gradle 9.2.0 is pinned via wrapper. Maven repos use Aliyun mirrors (China) for speed.

## Project structure

Single-module Fabric mod. Source root: `src/main/java/com/wxh1024/addon/`.

| Path | Role |
|------|------|
| `Wxh1024Addon.java` | Entry point (`meteor` entrypoint in `fabric.mod.json`). Registers category + modules + commands. |
| `modules/` | Meteor modules: `Home`, `ItemFrameChestOpener`, `ControlifyCompat`, `DropTest` |
| `commands/` | Meteor commands: `C3uCommand`, `TpCommand`, `TpaCommand`, plus `TpAliasStore` (persistence) and `CmdText` (colored chat formatting) |
| `mixin/` | Mixins: `ItemFrameEntityMixin`, `KeyBindingCompatMixin`, `SlimefunInputCompatMixin` |
| `utils/` | `ControlifyBindings` helper |
| `libs/` | Meteor Client JAR (git-tracked, branch-specific) |

## Key conventions

- **Meteor category**: All modules use `Wxh1024Addon.WXH1024` (name `"wxh1024"`, cyan icon).
- **Module pattern**: Extend `Module`, call `super(WXH1024, name, desc)` in constructor. Use `settings.getDefaultGroup()` for settings DSL. Annotate event handlers with `@EventHandler` (Orbit event bus).
- **Command pattern**: Extend `meteordevelopment.meteorclient.commands.Command`. Registered via `Commands.add()` in `Wxh1024Addon.onInitialize()`.
- **Mixin config**: `wxh1024-addon.mixins.json` lists all mixins; keep in sync when adding/removing mixins.
- **Null safety**: Always check `mc.player`, `mc.world`, `mc.interactionManager`, `mc.getNetworkHandler()` before use.

## Versioning

Multi-branch repo (one branch per Minecraft version). `gradle.properties` controls all version pins:

- `minecraft_version` / `yarn_mappings` — must be upgraded together (find matching Yarn build)
- `meteor_jar` — filename in `libs/` for the branch's Meteor Client JAR
- `mod_version` — the addon's own version

Current branch targets **Minecraft 1.21.11**.

## Adding a module

1. Create `src/main/java/com/wxh1024/addon/modules/NewModule.java` extending `Module`
2. Register it in `Wxh1024Addon.onInitialize()` via `Modules.get().add(new NewModule())`
3. If it needs mixins, add them to `mixin/` and register in `wxh1024-addon.mixins.json`

## Adding a command

1. Create `src/main/java/com/wxh1024/addon/commands/NewCommand.java` extending `Command`
2. Register it in `Wxh1024Addon.onInitialize()` via `Commands.add(new NewCommand())`
