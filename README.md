# Backyard Realms — Backyard Engine 0.2

A phone-first Android action-RPG prototype and purpose-built reusable 2D engine.

## Milestone 0.2 features

- Fixed 60 Hz update loop and scaled 480×270 logical display
- Large 960×540 scrolling backyard
- Smooth camera with a small dead zone
- Accelerated/decelerated analog movement with normalized diagonals
- Axis-separated collision resolution
- PNG sprite-sheet loading and reusable frame animation classes
- Directional three-phase stick swing presentation
- Reusable interactable interface
- First friend NPC near the fort
- Context-sensitive ACTION behavior and dialogue panel
- Collision, interaction, attack, position, and camera debug overlays
- Touch joystick and action button

## Build

Push the repository to GitHub and run `.github/workflows/android-build.yml`. Download the `BackyardRealms-debug` artifact and install `app-debug.apk`.

## Test checklist

1. Walk in every direction and verify diagonal movement is not faster.
2. Release the joystick and verify the player decelerates quickly rather than stopping harshly.
3. Walk around the larger yard and verify the camera follows smoothly.
4. Verify the player cannot enter the fort, tree, shed, garden, sandbox, or porch.
5. Stand near the friend or a landmark and press ACTION to open dialogue.
6. Press ACTION again to close dialogue.
7. Away from interactable objects, press ACTION and verify the stick swings.
8. Verify yellow collision outlines, cyan interaction outline, and red attack box appear correctly.

## Architecture

Reusable systems remain under `engine/`. Backyard-specific content remains under `game/`. See `docs/ARCHITECTURE.md`.
