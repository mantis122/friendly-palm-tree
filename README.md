# Backyard Realms — Backyard Engine 1.0

This is the first stable, reusable release of the Android-first custom action-RPG engine. It retains every verified Engine 0.5 system and adds the complete inventory/world-interaction milestone that begins the transition into Chapter 1 development.

## New in Engine 1.0

- Data-driven item catalog loaded from `app/src/main/assets/items.json`
- Generic item definitions, stacks, inventory slots, capacity, add/remove, and serialization
- One equipped-item slot
- New **BAG** touch button and inventory panel
- Favorite Stick, Summer Berry, and Tiny Brass Key definitions
- World pickups with bobbing visuals and persistent collection state
- Favorite Stick pickup beneath the old tree
- Automatic stick equipment after pickup
- One-time treasure chest with persistent open state
- Chest containing a key and three berries
- Generic quest-flag service with save integration
- Flags for meeting Mia, finding the stick, entering Fantasy, opening the chest, and clearing the garden weeds
- Generic item-to-world interaction resolver
- First item puzzle: use the equipped stick at the garden to uncover a berry
- Combat now requires the stick to be equipped
- Save migration that preserves old 0.5 position/theme/time data while adding inventory state
- Engine Playground controls for resetting item tests and toggling stick equipment
- First in-app Content Browser page showing every loaded item definition
- Version name `1.0.0`

## Build

Push the project to GitHub and run `.github/workflows/android-build.yml`, then install the generated debug APK.

## Engine 1.0 verification checklist

### Regression

- [ ] GitHub Actions builds successfully.
- [ ] The app launches in landscape full-screen mode.
- [ ] Movement, camera, collision, interaction, dialogue, time tinting, imagination switching, ambience, combat, enemy AI, defeat/respawn, and DEV controls still work.
- [ ] Existing Engine 0.5 save data loads without crashing.

### Inventory and equipment

- [ ] A **BAG** button appears beside **DEV**.
- [ ] BAG opens and closes the backpack panel; ACTION also closes it.
- [ ] A fresh/reset game begins without the stick equipped.
- [ ] Pressing ACTION away from an interactable while unarmed displays a message instead of attacking.
- [ ] The Favorite Stick appears just southeast of the old tree.
- [ ] Walking over it collects it, shows pickup dialogue, sets `HAS_STICK`, and equips it automatically.
- [ ] The backpack lists Favorite Stick with `[E]`.
- [ ] Stick attacks and Moon Blob combat work after equipping it.

### Pickups, chest, and item interaction

- [ ] A berry pickup appears below-left of the garden and can be collected.
- [ ] A chest appears near the lower central path.
- [ ] ACTION near the chest opens it and grants Tiny Brass Key x1 and Summer Berry x3.
- [ ] Reopening the chest says it is empty and grants nothing else.
- [ ] With the stick equipped, interacting with the garden once clears the weeds and grants one berry.
- [ ] Repeating the garden interaction does not grant another berry.

### Persistence

- [ ] Move, collect the stick and berry, open the chest, clear the garden weeds, change theme/time, then fully close the app.
- [ ] Reopening restores position, theme, time, inventory quantities, equipped stick, collected pickups, opened chest, and quest flags.
- [ ] The collected stick and berry do not reappear.
- [ ] The chest remains visibly open.

### Engine Playground / Content Browser

Open DEV:

- [ ] The panel lists the item catalog loaded from JSON, including stack/equip/use/quest/damage properties.
- [ ] ACTION switches theme.
- [ ] Joystick UP advances time.
- [ ] Joystick DOWN respawns the enemy.
- [ ] Joystick LEFT resets all item tests, closes the chest state, and respawns pickups.
- [ ] Joystick RIGHT gives the stick when needed and toggles whether it is equipped.

## Stable engine boundary

Reusable mechanisms live under `engine/`; Backyard-specific content and rules live under `game/`. Engine 1.0 is considered the stable foundation. Future work should be additive unless a verified correctness issue requires a core change.

## Next milestone

**Backyard Realms — Chapter 1, Milestone 1:** opening sequence, objective progression, guided Mia dialogue, waking in the fort, finding the stick, returning to Mia, and the first authored transformation into Fantasy.
