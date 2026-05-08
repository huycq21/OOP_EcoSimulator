# Asset Rules

These rules are mandatory for every asset folder and file in this project.

## Naming

Always use `PascalCase` for:

- folder names
- file names
- entity names inside file names
- animation/action names inside file names

Do:

- `assets/Entities/BlackGrouse/BlackGrouseIdle.png`
- `assets/Entities/RedFox/RedFoxRun.png`
- `assets/Environment/Grass/GrassTile.png`

Do not:

- `Black_grouse_Idle.png`
- `FoxWalk.png`
- `red-fox-run.png`
- `Red Fox Run.png`
- `assets/entities/hare/hare_idle.png`

## Entity Folder Structure

Each entity should have its own folder:

```text
assets/Entities/<EntityName>/
  <EntityName>Idle.png
  <EntityName>Walk.png
  <EntityName>Run.png
  <EntityName>Death.png
  <EntityName>Hurt.png
  <EntityName>Shadow.png
```

Example:

```text
assets/Entities/Hare/
  HareIdle.png
  HareWalk.png
  HareRun.png
  HareDeath.png
  HareHurt.png
  HareShadow.png
```

## Spritesheet Layout

Animated entity spritesheets should use 4 rows:

- row 1: facing down/front
- row 2: facing up/back
- row 3: facing right
- row 4: facing left

Frames must be square and evenly spaced. The renderer infers frame size from:

```text
frame_height = image_height / 4
frame_width = frame_height
```

## Supported Actions

Use these action names when available:

- `idle`
- `walk`
- `run`
- `attack`
- `hurt`
- `death`
- `flight`
- `shadow`

Do not invent a new action name unless the renderer or game logic is updated to support it.

## File Format

- Use `.png`.
- Use transparent backgrounds for sprites.
- Keep each spritesheet for one entity and one action only.

## Renderer Contract

The Java renderer expects paths in this format:

```text
assets/Entities/<EntityName>/<EntityName><Action>.png
```

If an asset does not follow this rule, it should be renamed before being used in code.
