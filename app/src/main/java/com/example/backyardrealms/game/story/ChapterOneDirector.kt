package com.example.backyardrealms.game.story

import com.example.backyardrealms.engine.quest.QuestFlags
import com.example.backyardrealms.game.theme.ImaginationTheme

/** Game-specific direction for Chapter 1. */
class ChapterOneDirector(private val flags: QuestFlags) {
    var bannerTitle: String? = null
        private set
    var bannerSubtitle: String? = null
        private set
    var bannerTimer: Float = 0f
        private set

    fun initializeFreshChapter(): Boolean {
        if (flags.has(CHAPTER_STARTED)) return false
        flags.set(CHAPTER_STARTED)
        showBanner("SUMMER VACATION", "The first day of a new adventure", 3.2f)
        return true
    }

    fun update(dt: Float) {
        if (bannerTimer <= 0f) return
        bannerTimer = (bannerTimer - dt).coerceAtLeast(0f)
        if (bannerTimer == 0f) {
            bannerTitle = null
            bannerSubtitle = null
        }
    }

    fun objective(theme: ImaginationTheme): String = when {
        flags.has(CROWN_FRAGMENT_CLAIMED) -> "Return through the Goblin Fort with Mia."
        flags.has(BLANKET_KING_DEFEATED) && !flags.has(CROWN_FRAGMENT_CLAIMED) ->
            "Claim the first Moon Crown fragment."
        flags.has(DUNGEON_REWARD_CLAIMED) && !flags.has(BOSS_CHAMBER_ENTERED) ->
            "Follow the secret blanket passage behind the royal toy box."
        flags.has(BOSS_CHAMBER_ENTERED) && !flags.has(BLANKET_KING_DEFEATED) ->
            "Defeat the Blanket King. Mia can break his blanket shield."
        flags.has(MOON_SIGIL_FOUND) && theme == ImaginationTheme.FANTASY && !flags.has(GOBLIN_FORT_ENTERED) ->
            "Enter the Goblin Fort beside the shed."
        flags.has(GOBLIN_FORT_ENTERED) && !flags.has(DUNGEON_DOOR_OPEN) ->
            "Stand on the moon switch while Mia holds the sun switch."
        flags.has(DUNGEON_DOOR_OPEN) && !flags.has(GOBLIN_SCOUT_DEFEATED) ->
            "Defeat the Goblin Scout in the treasure room."
        flags.has(GOBLIN_SCOUT_DEFEATED) && !flags.has(DUNGEON_REWARD_CLAIMED) ->
            "Open the royal toy box."
        flags.has(CHAPTER_ENDING_COMPLETE) -> "Enjoy the rest of the afternoon with Mia."
        flags.has(CHAPTER_COMPLETE) && theme == ImaginationTheme.FANTASY ->
            "Return to Moonkeep with Sir Mia."
        !flags.has(TALKED_TO_MIA) -> "Talk to Mia near the fort."
        !flags.has(FOUND_STICK) -> "Find your favorite stick beneath the old tree."
        !flags.has(MIA_APPROVED_STICK) -> "Show Mia that you found the stick."
        !flags.has(ENTERED_FANTASY) -> "Meet Mia at the fort and climb inside together."
        theme == ImaginationTheme.FANTASY && !flags.has(DEFEATED_MOON_BLOB) ->
            "Defeat the Moon Blob."
        flags.has(DEFEATED_MOON_BLOB) && !flags.has(FIRST_TRIAL_COMPLETE) ->
            "Talk to Sir Mia."
        !flags.has(SIGIL_QUEST_ACCEPTED) -> "Ask Sir Mia what happened."
        !flags.has(GATE_UNLOCKED) -> "Find the brass key and unlock the Moon Gate."
        !flags.has(MOON_SIGIL_FOUND) -> "Recover the Moon Sigil beyond the gate."
        !flags.has(CHAPTER_COMPLETE) -> "Return the Moon Sigil to Sir Mia."
        else -> "Explore with Mia."
    }

    fun miaDialogue(theme: ImaginationTheme): String {
        if (theme == ImaginationTheme.FANTASY) {
            return when {
                flags.has(CHAPTER_COMPLETE) ->
                    "We did it. Let's take the Moon Sigil back to Moonkeep, then crawl home."
                flags.has(MOON_SIGIL_FOUND) -> {
                    flags.set(CHAPTER_COMPLETE)
                    showBanner("MOON SIGIL RESTORED", "Return to Moonkeep together", 3.4f)
                    "You found it! Come on—let's carry it back to Moonkeep together."
                }
                flags.has(DEFEATED_MOON_BLOB) && !flags.has(SIGIL_QUEST_ACCEPTED) -> {
                    flags.set(FIRST_TRIAL_COMPLETE)
                    flags.set(SIGIL_QUEST_ACCEPTED)
                    showBanner("THE STOLEN SIGIL", "Search beyond the Goblin Fort", 3.0f)
                    "Nice swing! But while we were fighting, the Moon Goblins stole our silver sigil. That little brass key should open their gate."
                }
                flags.has(SIGIL_QUEST_ACCEPTED) && !flags.has(GATE_UNLOCKED) ->
                    "The Moon Gate is beside the Goblin Fort. I'll stay close—find the key and we'll go together."
                flags.has(GATE_UNLOCKED) && !flags.has(MOON_SIGIL_FOUND) ->
                    "The gate is open. Keep your stick ready; I'll cover you with my shield."
                else ->
                    "Look out! That Moon Blob is coming this way. I'll keep it off you—use your stick!"
            }
        }

        return when {
            flags.has(CHAPTER_ENDING_COMPLETE) ->
                "That was a good one. Tomorrow we should add a dragon. Or two dragons."
            !flags.has(TALKED_TO_MIA) -> {
                flags.set(TALKED_TO_MIA)
                "You're not seriously going to fight Moon Goblins without your favorite stick. Didn't you leave it by the old tree again?"
            }
            !flags.has(FOUND_STICK) ->
                "It should still be beneath the old tree. I'll meet you by the fort when you find it."
            !flags.has(MIA_APPROVED_STICK) -> {
                flags.set(MIA_APPROVED_STICK)
                "There it is! Okay, now that's a proper legendary sword. Come on—let's get inside the fort."
            }
            !flags.has(ENTERED_FANTASY) ->
                "Ready? Climb into the fort with me. We have to decide what we're playing first."
            else ->
                "Want to visit the Moon Kingdom again?"
        }
    }

    fun onStickCollected() {
        flags.set(FOUND_STICK)
    }

    fun canBeginFantasy(): Boolean =
        flags.has(MIA_APPROVED_STICK) || flags.has(ENTERED_FANTASY)

    fun hasEnteredFantasy(): Boolean = flags.has(ENTERED_FANTASY)

    fun firstTransformationSceneText(): String =
        "Mia crawls into the fort beside you. “Okay... this time, we're knights of the Moon Kingdom!”"

    fun fortBlockedMessage(): String = when {
        !flags.has(TALKED_TO_MIA) ->
            "The fort is quiet. Mia is waiting outside—you should talk to her first."
        !flags.has(FOUND_STICK) ->
            "You should find your favorite stick before starting the game."
        else ->
            "Mia wants to see the stick before you both climb inside."
    }

    fun onFantasyEntered() {
        if (!flags.has(ENTERED_FANTASY)) {
            flags.set(ENTERED_FANTASY)
            showBanner("THE MOON KINGDOM", "Where the backyard becomes a realm", 3.4f)
        }
    }

    fun onMoonBlobDefeated(enemyId: String) {
        if (enemyId == "moon_blob" && !flags.has(DEFEATED_MOON_BLOB)) {
            flags.set(DEFEATED_MOON_BLOB)
            showBanner("PATH CLEARED", "Talk to Sir Mia", 2.4f)
        }
    }

    fun onGateUnlocked() {
        flags.set(GATE_UNLOCKED)
        showBanner("MOON GATE OPEN", "Sir Mia follows you beyond the fort", 2.4f)
    }

    fun onSigilFound() {
        flags.set(MOON_SIGIL_FOUND)
        showBanner("MOON SIGIL FOUND", "Return to Sir Mia", 2.4f)
    }

    fun readyToReturnHome(): Boolean = flags.has(CHAPTER_COMPLETE) && flags.has(CROWN_FRAGMENT_CLAIMED)
    fun endingComplete(): Boolean = flags.has(CHAPTER_ENDING_COMPLETE)

    fun returnHomeSceneText(): String =
        "Sir Mia lowers her cardboard shield. “Moon Kingdom saved. Race you through the fort!”"

    fun onReturnedHome() {
        if (flags.has(CHAPTER_ENDING_COMPLETE)) return
        flags.set(CHAPTER_ENDING_COMPLETE)
        showBanner("ADVENTURE COMPLETE", "Tomorrow, the backyard can become anything", 4.0f)
    }

    fun companionGateReaction(): String? {
        if (!flags.has(SIGIL_QUEST_ACCEPTED) || flags.has(GATE_REACTION)) return null
        flags.set(GATE_REACTION)
        return "Sir Mia taps the little lock with her shield. “Definitely the brass key. We left it in a treasure chest, remember?”"
    }

    fun companionSigilReaction(): String? {
        if (!flags.has(GATE_UNLOCKED) || flags.has(SIGIL_REACTION)) return null
        flags.set(SIGIL_REACTION)
        return "Sir Mia points past the Goblin Fort. “There! The Moon Sigil is shining behind them.”"
    }

    fun questAccepted(): Boolean = flags.has(SIGIL_QUEST_ACCEPTED)
    fun gateUnlocked(): Boolean = flags.has(GATE_UNLOCKED)
    fun sigilFound(): Boolean = flags.has(MOON_SIGIL_FOUND)


    fun onGoblinFortEntered() {
        if (!flags.has(GOBLIN_FORT_ENTERED)) {
            flags.set(GOBLIN_FORT_ENTERED)
            showBanner("GOBLIN FORT", "Blanket banners and flashlight torches", 3.0f)
        }
    }

    fun onDungeonDoorOpened() {
        if (!flags.has(DUNGEON_DOOR_OPEN)) {
            flags.set(DUNGEON_DOOR_OPEN)
            showBanner("PASSAGE OPEN", "The two switches click together", 2.4f)
        }
    }

    fun onGoblinScoutDefeated() {
        if (!flags.has(GOBLIN_SCOUT_DEFEATED)) {
            flags.set(GOBLIN_SCOUT_DEFEATED)
            showBanner("FORT CAPTAIN DEFEATED", "Search the royal toy box", 2.4f)
        }
    }

    fun onDungeonRewardClaimed() {
        if (!flags.has(DUNGEON_REWARD_CLAIMED)) {
            flags.set(DUNGEON_REWARD_CLAIMED)
            showBanner("FORT'S SECRET", "A hidden blanket passage opens behind the toy box", 3.2f)
        }
    }

    fun onBossChamberEntered() {
        if (!flags.has(BOSS_CHAMBER_ENTERED)) {
            flags.set(BOSS_CHAMBER_ENTERED)
            showBanner("THE BLANKET THRONE", "Something enormous stirs beneath the quilts", 3.2f)
        }
    }

    fun onBlanketKingDefeated() {
        if (!flags.has(BLANKET_KING_DEFEATED)) {
            flags.set(BLANKET_KING_DEFEATED)
            showBanner("BLANKET KING DEFEATED", "The cardboard crown cracks apart", 3.4f)
        }
    }

    fun onCrownFragmentClaimed() {
        if (!flags.has(CROWN_FRAGMENT_CLAIMED)) {
            flags.set(CROWN_FRAGMENT_CLAIMED)
            showBanner("MOON CROWN FRAGMENT", "The first piece of a much larger mystery", 3.6f)
        }
    }

    fun goblinFortEntered() = flags.has(GOBLIN_FORT_ENTERED)
    fun dungeonDoorOpen() = flags.has(DUNGEON_DOOR_OPEN)
    fun goblinScoutDefeated() = flags.has(GOBLIN_SCOUT_DEFEATED)
    fun dungeonRewardClaimed() = flags.has(DUNGEON_REWARD_CLAIMED)
    fun bossChamberEntered() = flags.has(BOSS_CHAMBER_ENTERED)
    fun blanketKingDefeated() = flags.has(BLANKET_KING_DEFEATED)
    fun crownFragmentClaimed() = flags.has(CROWN_FRAGMENT_CLAIMED)

    fun reset() {
        listOf(
            CHAPTER_STARTED,
            TALKED_TO_MIA,
            FOUND_STICK,
            MIA_APPROVED_STICK,
            ENTERED_FANTASY,
            DEFEATED_MOON_BLOB,
            FIRST_TRIAL_COMPLETE,
            SIGIL_QUEST_ACCEPTED,
            GATE_UNLOCKED,
            MOON_SIGIL_FOUND,
            CHAPTER_COMPLETE,
            CHAPTER_ENDING_COMPLETE,
            GATE_REACTION,
            SIGIL_REACTION,
            GOBLIN_FORT_ENTERED,
            DUNGEON_DOOR_OPEN,
            GOBLIN_SCOUT_DEFEATED,
            DUNGEON_REWARD_CLAIMED,
            BOSS_CHAMBER_ENTERED,
            BLANKET_KING_DEFEATED,
            CROWN_FRAGMENT_CLAIMED
        ).forEach(flags::clear)
        initializeFreshChapter()
    }

    private fun showBanner(title: String, subtitle: String, duration: Float) {
        bannerTitle = title
        bannerSubtitle = subtitle
        bannerTimer = duration
    }

    companion object {
        const val CHAPTER_STARTED = "CH1_STARTED"
        const val TALKED_TO_MIA = "CH1_TALKED_TO_MIA"
        const val FOUND_STICK = "CH1_FOUND_STICK"
        const val MIA_APPROVED_STICK = "CH1_MIA_APPROVED_STICK"
        const val ENTERED_FANTASY = "CH1_ENTERED_FANTASY"
        const val DEFEATED_MOON_BLOB = "CH1_DEFEATED_MOON_BLOB"
        const val FIRST_TRIAL_COMPLETE = "CH1_FIRST_TRIAL_COMPLETE"
        const val SIGIL_QUEST_ACCEPTED = "CH1_SIGIL_QUEST_ACCEPTED"
        const val GATE_UNLOCKED = "CH1_GATE_UNLOCKED"
        const val MOON_SIGIL_FOUND = "CH1_MOON_SIGIL_FOUND"
        const val CHAPTER_COMPLETE = "CH1_COMPLETE"
        const val CHAPTER_ENDING_COMPLETE = "CH1_ENDING_COMPLETE"
        const val GATE_REACTION = "CH1_MIA_GATE_REACTION"
        const val SIGIL_REACTION = "CH1_MIA_SIGIL_REACTION"
        const val GOBLIN_FORT_ENTERED = "CH1_GOBLIN_FORT_ENTERED"
        const val DUNGEON_DOOR_OPEN = "CH1_DUNGEON_DOOR_OPEN"
        const val GOBLIN_SCOUT_DEFEATED = "CH1_GOBLIN_SCOUT_DEFEATED"
        const val DUNGEON_REWARD_CLAIMED = "CH1_DUNGEON_REWARD_CLAIMED"
        const val BOSS_CHAMBER_ENTERED = "CH1_BOSS_CHAMBER_ENTERED"
        const val BLANKET_KING_DEFEATED = "CH1_BLANKET_KING_DEFEATED"
        const val CROWN_FRAGMENT_CLAIMED = "CH1_CROWN_FRAGMENT_CLAIMED"
    }
}
