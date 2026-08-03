# Backyard Realms — Engine 0.5 Combat Foundation

This Android-first custom engine milestone adds the first complete reusable combat loop while retaining all verified Engine 0.4 systems.

## New in 0.5

- Reusable `HealthComponent`, `Damageable`, and `DamageHit` combat types
- Player health HUD with five hearts
- Directional attack hitbox and one-hit-per-swing behavior
- Damage, recoil, invulnerability frames, and flashing feedback
- Reusable fantasy enemy with wander, notice, chase, hurt, defeated, and respawn states
- Enemy health bar
- Contact damage and directional player knockback
- Player defeat and automatic Moonkeep respawn
- Combat events for damage, defeat, health changes, and respawn
- Combat hitbox overlays and state reporting in the developer panel
- Fantasy-only enemies; the real backyard remains safe

## Build

Push the project to GitHub and run `.github/workflows/android-build.yml`, then install the generated debug APK.

## Engine 0.5 verification checklist

### Regression

- [ ] Project builds successfully in GitHub Actions.
- [ ] App launches in landscape full-screen mode.
- [ ] Movement, camera, collisions, interaction, dialogue, time tinting, theme switching, and save/load still work.
- [ ] DEV opens and closes correctly.

### Combat

- [ ] No enemy is visible or harmful in the real backyard.
- [ ] Entering Fantasy reveals a purple Moon Blob near the central path.
- [ ] The blob wanders while distant and chases when approached.
- [ ] A stick swing damages the blob only when the red attack box overlaps it.
- [ ] One swing removes only one health point, even if the boxes overlap for multiple frames.
- [ ] The blob recoils, flashes, and briefly ignores repeated damage.
- [ ] Three successful hits defeat it.
- [ ] It disappears, then respawns automatically after roughly four seconds.
- [ ] Touching it removes one heart and knocks the player away.
- [ ] Player flashing prevents rapid repeated contact damage.
- [ ] Losing all five hearts returns the player to Moonkeep with restored health.
- [ ] Combat events and enemy state appear in the debug panel.

### Developer tools

- [ ] DEV + ACTION switches themes.
- [ ] DEV + joystick UP advances time.
- [ ] DEV + joystick DOWN immediately respawns the enemy.

## Next milestone

Engine 0.6 will turn these mechanics into the beginning of Chapter 1: acquiring the stick, a simple objective/flag system, a chest or pickup, guided Mia dialogue, and the first short playable quest loop.
