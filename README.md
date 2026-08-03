# Backyard Realms — Engine 0.4: Living Backyard

This milestone turns the stable 0.3 prototype into a more reusable living-world foundation.

## Added

- Base entity hierarchy: entity, character, world object, effect, projectile
- Generic behavior interface, used by Mia's idle behavior
- Event bus with theme, interaction, movement, and attack events
- Ambient drifting pollen/butterflies
- Mia idle bobbing and blinking
- World time framework: morning, afternoon, evening, night
- Visual time-of-day tinting
- Save/load using Android SharedPreferences
- Saved player position, imagination theme, and world time
- Asset-independent audio routing with ambient and action cue reporting
- Expanded developer and debug panels

## Test checklist

1. Build through GitHub Actions and install the APK.
2. Confirm the prior 0.3 movement, camera, collision, dialogue, attack, and theme-switch behavior still works.
3. Confirm small ambient particles drift through the yard.
4. Watch Mia and confirm she subtly bobs and periodically blinks.
5. Open DEV and push the joystick upward once; confirm time advances and the screen tint changes.
6. Repeat to cycle MORNING → AFTERNOON → EVENING → NIGHT.
7. In DEV, press ACTION and confirm theme switching still works.
8. Close and relaunch the app; confirm player position, theme, and time are restored.
9. Confirm the debug panel updates `audio=` after walking interactions, attacking, and switching themes.
10. Confirm the debug panel updates `event=` after movement, attack, interaction, and theme switching.
11. Confirm controls remain responsive and no severe frame-rate regression is visible.

No finished audio assets are included yet. The audio router is intentionally asset-independent so future WAV/OGG files can be assigned without changing gameplay systems.
