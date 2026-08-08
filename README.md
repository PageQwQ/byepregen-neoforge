# ByePregen (NeoForge 26.1.2)

A server-side performance mod for Minecraft that accelerates chunk pregeneration and optimizes
world generation and lighting. It changes no game content — it only makes chunk and worldgen
processing faster.

This repository is a **port of [MoePus/Bye-Pregen](https://github.com/MoePus/Bye-Pregen) to
NeoForge 26.1.2 (Minecraft 26.1.2)**. Upstream targets NeoForge 1.21.1; this fork ports the code
to the 26.1.2 chunk pipeline and adds fixes found through real-client verification.

## License

**GNU Lesser General Public License v3.0** (see [LICENSE](LICENSE)).

This is a derivative work of MoePus/Bye-Pregen, Copyright (c) MoePus and AzureCrab.
Portions are adapted from C2ME (see `src/main/resources/META-INF/C2ME-MIT-LICENSE.txt`).

## Features

| Feature | Config key | Default |
|---|---|---|
| Arena palette — allocation-free block-state batching during worldgen | `enableArenaPalette` | `true` |
| Fast chunk ticking (zero-allocation `tickChunks` rewrite) | `enableFastTickChunks` | `false` |
| YA light engine (custom lighting, useful for relighting) | `enableYALightEngine` | `false` |
| Server-runtime arena containers (keep arena palettes in loaded chunks) | `enableServerRuntimeArenaPalette` | `false` |
| Client-side arena containers | `enableClientArenaPalette` | `false` |
| GC-free chunk serialization (C2ME integration) | `enableGcFreeWorldgenSave` | `true` *(currently a no-op, see Known Issues)* |
| Fast placed-feature placement | `enablePlacedFeatureMixin` | `false` |

Works alongside C2ME, Lithium, Sodium, Chunky, Architectury and ScalableLux. ZFastNoise and
Sable compat were removed (no 26.1 builds).

## Requirements

- Minecraft **26.1.2** with **NeoForge 26.1.2.94**
- **JDK 25**

## Building

```bash
./gradlew build
```

The Gradle daemon must run on a JVM ≤ 25 (`org.gradle.java.home` is preset in `gradle.properties`).

Some dev dependencies are vendored in `libs/` (gitignored): C2ME module jars, Voxy, and
MixinSquared. They are only needed for the compatibility mixins and dev runs; the built jar
bundles MixinSquared via JarJar.

## Configuration

Create `config/byepregen.json` in the game directory (the file is generated with defaults on
first run):

```json
{
  "enablePlacedFeatureMixin": false,
  "enableArenaPalette": true,
  "enableServerRuntimeArenaPalette": false,
  "enableFastTickChunks": false,
  "enableYALightEngine": false,
  "enableGcFreeWorldgenSave": true
}
```

Typical usage: install on the server together with Chunky and run `/chunky start` — worldgen is
faster and the server stays more stable.

## Testing

The dev test suite (`gradle/byepregen-test-runs.gradle`) provides:

- `runLightFuzzVanilla` / `runLightFuzzYA` / `runDiffLightFuzz` — deterministic light-engine
  fuzzing and vanilla-vs-YA NBT comparison
- `runLightGoldenSource` → `runPrepareLightGoldenRelight` → `runLightGoldenRelightVanilla/YA/Lux`
  → `runDiffLightGolden` — golden light comparison on identical terrain (relight chain)
- `runTestWorldGen` — Chunky worldgen benchmark with JFR output

The diff/prepare tools run **in-game** (as server modes), because 26.1's game bootstrap requires
the FML environment.

## Porting notes (1.21.1 → 26.1.2)

Highlights of what changed in the port (verified against the 26.1.2 jars):

- `ChunkPos` is a record (`x()`/`z()`, `pack()`/`unpack()`); `CompoundTag` is final with
  Optional-returning getters; `Tag` is sealed
- `ChunkSerializer`/`ChunkStorage` → `SerializableChunkData`/`SimpleRegionStorage`
  (`ChunkMap extends SimpleRegionStorage`)
- `PalettedContainer` rework: top-level `Strategy`, `(T, Strategy)` constructor
- Arena containers are materialized on every `LevelChunk` constructor plus a network-write
  fallback (fixes client disconnects caused by the custom network format leaking to the wire)
- Client: `readSectionList` removed → `queueLightUpdate` injection; `getLightColor` →
  `getLightCoords`

## Known issues

- `enableGcFreeWorldgenSave` is currently a no-op (the raw serializer is not yet reworked for
  `SerializableChunkData`)
- YA light engine has a ±1 block-light edge difference in the blackout fuzz variant
  (round-trip after blackout clearing)
- `enableFastTickChunks` needs validation on a real player server (spawn logic was ported with
  inferred 26.1 semantics)

## Upstream

- Upstream repository: https://github.com/MoePus/Bye-Pregen (NeoForge 1.21.1)
- Local git remote `upstream` tracks it for syncing.
