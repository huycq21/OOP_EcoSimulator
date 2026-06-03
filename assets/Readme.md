# EcoSimulator Assets

Read `AssetRules.md` before adding or renaming assets. The project always uses `PascalCase` for asset folders and files.

Place PNG sprites in these folders. The renderer looks for species folders first, then single PNG sprites, and falls back to Java2D shapes when an image is missing.

## Entities

Use `PascalCase` for folders and files:

- `assets/Entities/Hare/HareIdle.png`
- `assets/Entities/Hare/HareWalk.png`
- `assets/Entities/Hare/HareRun.png`
- `assets/Entities/Hare/HareDeath.png`
- `assets/Entities/Hare/HareShadow.png`
- `assets/Entities/Fox/FoxIdle.png`
- `assets/Entities/Fox/FoxWalk.png`
- `assets/Entities/Fox/FoxRun.png`
- `assets/Entities/Fox/FoxDeath.png`
- `assets/Entities/Fox/FoxShadow.png`
- `assets/Entities/Boar/BoarIdle.png`
- `assets/Entities/Boar/BoarWalk.png`
- `assets/Entities/Boar/BoarRun.png`
- `assets/Entities/Boar/BoarDeath.png`
- `assets/Entities/Boar/BoarShadow.png`
- `assets/Entities/Deer/DeerIdle.png`
- `assets/Entities/Deer/DeerWalk.png`
- `assets/Entities/Deer/DeerRun.png`
- `assets/Entities/Deer/DeerDeath.png`
- `assets/Entities/Deer/DeerShadow.png`

## Environment

- `assets/Environment/Grass.png`
- `assets/Environment/Bush.png`
- `assets/Environment/Obstacle.png`

Transparent PNG files work best.

## Spritesheet Format

Entity animated sheets use:

- 4 rows
- row 1: facing down/front
- row 2: facing up/back
- row 3: facing right
- row 4: facing left

Each frame is inferred as a square cell from the sheet height: `frameHeight = imageHeight / 4`, `frameWidth = frameHeight`. This matches the current 32x32 frame assets.

Current class-to-asset mapping:

- `Rabbit` -> `Hare`
- `Fox` -> `Fox`
- `Wolf` -> `Fox` until a wolf folder is added
- `Boar` -> `Boar`
- `Deer` -> `Deer`
