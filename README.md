# Backyard Realms — Engine 0.3

This milestone introduces the game’s defining imagination-switching system.

## Test checklist

1. Confirm movement, camera, collision, interaction, and stick attacks still work.
2. Walk to the fort and press ACTION. The screen should fade to black and return in the Fantasy theme.
3. Verify player position, camera position, and collision remain unchanged across the switch.
4. Verify the ground, paths, landmarks, labels, Mia, and interaction text change.
5. Use the fort again to return to the Real theme.
6. Tap DEV in the upper-right. Movement should pause and the developer panel should open.
7. Press ACTION while the developer panel is open to force a theme switch.
8. Tap DEV again to close the panel.
9. Confirm no input gets stuck after multitouch or opening/closing the developer panel.

## Architecture added

- `ImaginationTheme`: reusable theme identity.
- `ThemeTransition`: reusable fade-out/midpoint/fade-in transition.
- Theme-aware landmarks and NPC presentation.
- Developer input command and overlay.

The physical backyard, collision geometry, player state, and camera remain stable while the active interpretation changes.
