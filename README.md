# Backyard Realms — Chapter 1, Milestone 1

This is the first game-content milestone built on Backyard Engine 1.0. It turns the previously freeform systems into a saved, guided opening sequence while keeping all existing inventory, combat, theme, developer, and world systems available.

## Version

- App version: **1.1.0**
- Milestone: **Chapter 1 — Opening Adventure**

## Playable opening

1. The game begins outside the fort with the title **SUMMER VACATION**.
2. The objective asks you to speak with Mia.
3. Mia asks you to recover your Favorite Stick from beneath the old tree.
4. Collecting the stick equips it automatically and updates the objective.
5. Return to Mia and show her the stick.
6. Go to the fort and begin the imaginary game.
7. The backyard transforms into **THE MOON KINGDOM**.
8. Defeat the Moon Blob.
9. Return to Sir Mia to complete the opening milestone.

Progress is stored through the existing quest-flag save system and survives closing and reopening the app.

## Developer replay control

Open **DEV** and push the joystick **left once** to reset Chapter 1. This clears the opening inventory/story state, restores the stick pickup, closes the chest, returns to the real backyard, and moves the player to the opening position.

Other developer controls remain:

- ACTION: switch theme
- Joystick up: advance time
- Joystick down: respawn enemy
- Joystick right: equip or unequip the stick

## Test checklist

### Build and regression

- [ ] GitHub Actions build succeeds.
- [ ] APK installs and launches.
- [ ] Movement, camera, collision, BAG, DEV, dialogue, and touch controls still work.
- [ ] Existing time-of-day and ambience state still work.

### Opening sequence

- [ ] Use DEV + joystick left to reset Chapter 1 before testing.
- [ ] Closing DEV shows the **SUMMER VACATION** banner.
- [ ] The first objective says to talk to Mia.
- [ ] Mia directs the player to the old tree.
- [ ] Trying the fort too early gives an appropriate story message instead of transforming.
- [ ] The Favorite Stick is present below/right of the old tree.
- [ ] Collecting it equips it and changes the objective.
- [ ] Mia recognizes that the stick was found and directs the player to the fort.
- [ ] The fort now triggers the fade and fantasy transformation.
- [ ] **THE MOON KINGDOM** banner appears after transformation.
- [ ] The objective asks the player to defeat the Moon Blob.
- [ ] Defeating the Moon Blob changes the objective to talking to Sir Mia.
- [ ] Talking to Sir Mia displays the Chapter 1 completion banner.

### Persistence

- [ ] Close and reopen during the stick objective; progress resumes correctly.
- [ ] Close and reopen after entering fantasy; theme and objective resume correctly.
- [ ] Close and reopen after defeating the Moon Blob; the completion step remains available.
- [ ] Complete the milestone, reopen the app, and confirm completion remains saved.

### Existing Engine 1.0 features

- [ ] BAG descriptions remain readable.
- [ ] The garden stick interaction still works.
- [ ] The chest still awards its contents once and remains open after restart.
- [ ] Player health, enemy contact damage, knockback, defeat, and respawn still work.
- [ ] The real backyard remains free of the Moon Blob.

## Architecture

The new `game/story/ChapterOneDirector.kt` is intentionally game-specific. It interprets generic quest flags as objectives, dialogue progression, and title banners. The reusable engine remains unaware of Mia, the backyard, the Favorite Stick, or the Moon Kingdom.
