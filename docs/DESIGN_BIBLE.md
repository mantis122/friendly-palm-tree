# Backyard Realms Design Bible

## Core premise
A child aged roughly 10–12 turns a familiar backyard into changing adventure worlds through imaginative play. Each friend contributes a different interpretation, changing enemies, NPC roles, atmosphere, and rules while preserving recognizable physical landmarks.

## Emotional rule
The game never mocks imagination or treats it as something the children must abandon. The imagined adventures are emotionally real, and ambiguity about whether something magical truly happened should remain.

## Engine philosophy
- Build a reusable Android-first engine for top-down 2D action-adventure games.
- Build only what a playable requirement needs.
- Keep engine systems independent of Backyard Realms characters and lore.
- Keep the game playable after every milestone.
- Generalize only after repeated real use cases.

## World rules
- The physical backyard is the authoritative spatial layer.
- Imagination themes reinterpret landmarks and content rather than replacing the map arbitrarily.
- Player and camera position persist through theme changes.
- Friends influence genre, visual language, enemies, and NPC identities.

## Current technical decisions
- Native Android/Kotlin.
- SurfaceView rendering with fixed 60 Hz updates.
- Logical resolution: 480 × 270.
- Current test world: 960 × 540.
- Touch-first input with reusable abstract actions.
- SharedPreferences save backend during early development.
- Placeholder geometric art is acceptable until systems stabilize.

## Naming conventions
- Engine packages describe mechanisms: `engine.events`, `engine.entity`, `engine.save`.
- Game packages describe meaning: `game.theme`, `game.world`, `game.ambient`.
- Stable world objects receive durable lowercase IDs such as `fort`, `tree`, and `sandbox`.

## Milestone status
- 0.1: game loop, touch movement, collision, stick action.
- 0.2: scrolling world, camera, animation framework, interaction, Mia.
- 0.3: imagination switching, themed landmarks, developer panel.
- 0.4: living-world entity groundwork, behaviors, ambience, events, time, save, audio routing.

## Near-term roadmap
- 0.5: combat foundation—health, damage, knockback, hit reactions, enemy state machine.
- Then begin Chapter 1 development with an actual playable progression.

## Combat foundation

Combat exists inside imagined play first; the ordinary backyard is safe. Engine combat is expressed through reusable health, damage, hitbox, hurtbox, knockback, invulnerability, and defeat concepts. Game-specific weapons and creatures configure those mechanisms rather than replacing them.

Combat must remain forgiving on touch screens: generous attack areas, clear recoil, brief invulnerability after damage, readable health, and restrained enemy speed. A single attack may damage a target at most once, regardless of how many simulation frames overlap.

# Engine 1.0 Decisions

## Stable boundary

The engine exposes reusable timing, rendering, camera, input, collision, entities, events, combat, saving, inventory, quest flags, and item-interaction mechanisms. It must not reference Backyard Realms characters, places, themes, enemies, or story terms.

## Content data

Item definitions live in `app/src/main/assets/items.json`. Game rules decide where items appear and what world targets accept them. Adding ordinary items should not require modifying the inventory engine.

## Equipment

Engine 1.0 intentionally supports one equipped-item slot. Additional slots will only be introduced by a concrete game requirement.

## Quest flags

Flags are stable string identifiers and are serialized as save data. Existing flag identifiers must not be renamed after a public game build without a migration.

## Development rule

Every reusable mechanism must have an immediately playable Backyard Realms use case and a developer test path.
