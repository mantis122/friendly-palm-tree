# Architecture Boundary

## Engine-owned responsibilities

- Timing and lifecycle
- Logical viewport and rendering surface
- Input abstraction
- Collision primitives
- Entity lifecycle
- Animation playback
- Audio services
- Scene transitions
- Asset/data loading
- Save infrastructure
- Diagnostics

## Game-owned responsibilities

- Backyard maps and landmarks
- Player rules
- Friends and imagination themes
- Weapons and items
- Enemy definitions
- Dialogue and quests
- Story flags
- Art and audio content

## Reuse test

The engine is reusable when a second top-down action-adventure can replace the maps, rules, entities, UI style, story, and assets without rewriting timing, rendering, input, collision, animation, audio, scene, or save infrastructure.
