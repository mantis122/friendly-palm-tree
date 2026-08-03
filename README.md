# Backyard Realms — Chapter 1, Milestone 2

Version 1.2.0 extends the verified opening into the game's first complete quest.

## New playable sequence

After completing the original Moon Blob trial:

1. Talk to Sir Mia.
2. She reveals that the Moon Sigil was stolen and taken beyond the Goblin Fort.
3. Open the existing chest near the lower path to obtain the Tiny Brass Key.
4. Find the Moon Gate beside the Goblin Fort in Fantasy.
5. Interact with the locked gate. The key is consumed and the gate opens.
6. Pass through and defeat the two Moon Blobs guarding the far side.
7. Collect the Moon Sigil.
8. Return it to Sir Mia to complete the first full quest.

## New systems/content

- Fantasy-only Moon Gate with collision and persistent unlocked state
- Tiny Brass Key now has a required story use
- Moon Sigil data-driven quest item
- Moon Sigil world pickup and persistence
- Two additional Moon Blob encounters beyond the gate
- Expanded Chapter 1 objective and dialogue flow
- Story banners for quest acceptance, gate opening, sigil recovery, and completion
- Save migration from Chapter 1 Milestone 1
- Version 1.2.0

## Test checklist

### Build and regression

- [ ] GitHub Actions builds successfully.
- [ ] App launches without crashing.
- [ ] Movement, camera, collision, BAG, DEV, theme switching, combat, and save/load still work.
- [ ] Existing Milestone 1 save loads without resetting the opening.

### Continued-save path

- [ ] A save that completed Milestone 1 has the objective `Ask Sir Mia what happened.`
- [ ] Talking to Sir Mia begins **THE STOLEN SIGIL** quest.

### Gate and key

- [ ] Moon Gate appears only in Fantasy beside the Goblin Fort.
- [ ] Gate blocks physical passage while locked.
- [ ] Interacting before accepting the quest gives a story-appropriate message.
- [ ] Interacting without the key instructs the player to search the chest.
- [ ] Opening the chest provides the Tiny Brass Key if it was not previously opened.
- [ ] Using the key opens the gate and removes the key from BAG.
- [ ] The gate remains open after switching themes and after restarting the app.

### Sigil encounter

- [ ] Two additional Moon Blobs are present beyond the gate.
- [ ] They use the established chase, damage, knockback, and defeat behavior.
- [ ] The Moon Sigil appears beyond the gate.
- [ ] Collecting it adds `Moon Sigil` to BAG and updates the objective.
- [ ] The sigil remains collected after restarting the app.

### Quest completion

- [ ] Returning to Sir Mia with the sigil displays **MOON SIGIL RESTORED**.
- [ ] Objective changes to `Explore the restored Moon Kingdom.`
- [ ] Completion remains saved after restarting.

### Full replay

Open DEV, push the joystick left once, then close DEV.

- [ ] Chapter resets to the Summer Vacation opening.
- [ ] Stick, key, Moon Sigil, chest, gate, and story flags reset.
- [ ] Entire Milestone 1 and Milestone 2 sequence can be completed from a clean state.

## Developer controls

- **DEV + ACTION:** switch theme
- **DEV + joystick up:** advance time
- **DEV + joystick down:** respawn all Moon Blobs
- **DEV + joystick left:** reset all Chapter 1 progress/content
- **DEV + joystick right:** equip or unequip the stick
