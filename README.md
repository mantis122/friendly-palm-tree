# Backyard Realms — Starter Project

This is the first runnable milestone for an Android-first, top-down 2D action-adventure engine and game.

## Included

- Native Android/Kotlin project
- Landscape immersive display
- Dedicated `SurfaceView` game loop
- Fixed 60 Hz simulation updates
- Logical 480 × 270 game resolution with letterboxing
- Multitouch virtual joystick and action button
- Placeholder backyard world
- Player movement
- Rectangle collision against landmarks and yard boundaries
- Temporary stick-swing action
- Debug overlay
- GitHub Actions APK build

## Current source organization

```text
com.example.backyardrealms/
├── MainActivity.kt
├── engine/
│   ├── GameConfig.kt
│   ├── GameSurfaceView.kt
│   ├── InputState.kt
│   ├── TouchControls.kt
│   └── Viewport.kt
└── game/
    ├── BackyardGame.kt
    └── Player.kt
```

The `engine` package must remain unaware of Backyard Realms characters, story, and content. The `game` package uses engine services to implement this particular game.

## Build through GitHub Actions

1. Create a GitHub repository.
2. Upload the contents of this folder to the repository root.
3. Commit and push.
4. Open **Actions → Build Android APK**.
5. Run the workflow, or let it run automatically after the push.
6. Download the `BackyardRealms-debug` artifact.
7. Extract and install `app-debug.apk` on the phone.

## Controls

- Touch and drag on the left half: move.
- Tap the round ACTION button: swing the placeholder stick.

## Important scope rule

Only add an engine feature when a playable game requirement needs it. Do not build a general-purpose editor or engine subsystem speculatively.

## Suggested next milestone

1. Replace the simple world drawing with a tile-grid renderer.
2. Add a reusable `Entity` interface and world entity collection.
3. Add one imaginary enemy with health, hitbox, hurtbox, and knockback.
4. Add an in-app diagnostics screen for phone-only testing.
