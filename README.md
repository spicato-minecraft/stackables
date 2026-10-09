# Stackables

Fabric mod for Minecraft **26.1** that lets identical potions (drinkable, splash, lingering) and enchanted books stack up to **16**.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 26.1              |
| Fabric Loader | 0.19.3+           |
| Fabric API    | 0.145.1+26.1      |
| Java          | 25+               |

## Build

From this directory:

```bash
./gradlew build
```

The output JAR is at `build/libs/stackables-1.0.0.jar` (version from `gradle.properties`).

## Test

```bash
./gradlew test          # unit tests (JUnit + Fabric Loader bootstrap)
./gradlew runGameTest   # server GameTests
./gradlew build         # full CI gate (includes both)
```

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.1.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.1 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder.

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `26.1`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/stackables.git
cd stackables
git checkout 26.1
```

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `26.1-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `26.1`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose                                                         |
|------------|-----------------------------------------------------------------|
| Unit tests | Target item list and stack cap constants                        |
| GameTests  | Max stack; mismatched potions; anvil/grindstone consume one book; brewing converts one potion from a stack |

Behavior tests are tracked in [Notion Tasks](https://app.notion.com/p/3b716e8d4da380adbd93cf0a73b39d5c). Manual UAT items live in the Mod Catalog.

## Architecture

Raises vanilla `minecraft:max_stack_size` to 16 via Fabric `DefaultItemComponentEvents` on potion, splash potion, lingering potion, and enchanted book. Stack identity is vanilla data components (`potion_contents`, `stored_enchantments`).

Vanilla anvil and grindstone still clear a whole input slot on take; mixins shrink by the number of items produced instead. Book + book combines run `min(left, right)` operations: result count and XP cost use a flat multiplier (`AnvilStackOperations`) so other cost mods can keep computing the 1+1 unit cost. Brewing converts one bottle per slot per cycle and drops leftover unbrewed potions.
