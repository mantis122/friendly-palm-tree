# Architecture — Engine 0.2

## Reusable engine layer

- `GameSurfaceView`: Android lifecycle, fixed update loop, frame drawing
- `Viewport`: logical-resolution scaling and touch-coordinate conversion
- `InputState` / `TouchControls`: platform input abstraction
- `Camera2D`: world-space camera follow and visible viewport
- `SpriteSheet`: PNG frame extraction and rendering
- `AnimatedSprite`: reusable frame timing
- `Interactable`: minimal interaction extension point

## Backyard Realms game layer

- `BackyardGame`: current world composition and game-specific rules
- `Player`: temporary Backyard player implementation
- `FriendNpc`: first story-facing NPC
- `Landmark`: backyard obstacle and interaction content

## Current deliberate limitations

- The map is still authored in Kotlin rather than loaded from data.
- Dialogue is one line and not yet data-driven.
- The player sprite is an intentionally simple placeholder.
- No enemies, damage, health, inventory, room transitions, saving, or audio yet.
- Debug overlays are always enabled in this development build.

## Next architectural extraction

After the interaction and camera behavior are validated on-device, the next milestone should add generic entity, animation-state, combat hitbox/hurtbox, and developer-menu systems. The imagination theme must remain game-specific while using reusable engine hooks.

## Engine 1.0 modules

- `engine/inventory`: item definitions, JSON catalog, inventory slots, equipment, and item-use contracts.
- `engine/quest`: generic persistent string flags.
- `game/items`: Backyard-specific pickups, chest content, and item/target rules.
- `assets/items.json`: data-driven item content.

The save layer persists serialized inventories and flags without importing game-specific item classes.
