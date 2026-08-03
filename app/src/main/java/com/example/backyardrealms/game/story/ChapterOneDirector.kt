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
        if (bannerTimer == 0f) { bannerTitle = null; bannerSubtitle = null }
    }

    fun objective(theme: ImaginationTheme): String = when {
        !flags.has(TALKED_TO_MIA) -> "Talk to Mia near the fort."
        !flags.has(FOUND_STICK) -> "Find your favorite stick beneath the old tree."
        !flags.has(MIA_APPROVED_STICK) -> "Show Mia that you found the stick."
        !flags.has(ENTERED_FANTASY) -> "Meet Mia at the fort and climb inside together."
        theme == ImaginationTheme.FANTASY && !flags.has(DEFEATED_MOON_BLOB) -> "Defeat the Moon Blob."
        flags.has(DEFEATED_MOON_BLOB) && !flags.has(FIRST_TRIAL_COMPLETE) -> "Talk to Sir Mia."
        !flags.has(SIGIL_QUEST_ACCEPTED) -> "Ask Sir Mia what happened."
        !flags.has(GATE_UNLOCKED) -> "Find the brass key and unlock the Moon Gate."
        !flags.has(MOON_SIGIL_FOUND) -> "Recover the Moon Sigil beyond the gate."
        !flags.has(CHAPTER_COMPLETE) -> "Return the Moon Sigil to Sir Mia."
        flags.has(CHAPTER_COMPLETE) -> "Explore the restored Moon Kingdom."
        else -> "Return to the fantasy realm."
    }

    fun miaDialogue(theme: ImaginationTheme): String {
        if (theme == ImaginationTheme.FANTASY) {
            return when {
                flags.has(MOON_SIGIL_FOUND) && !flags.has(CHAPTER_COMPLETE) -> {
                    flags.set(CHAPTER_COMPLETE)
                    showBanner("MOON SIGIL RESTORED", "The first quest is complete", 3.4f)
                    "You found it! The Moon Sigil is safe, and Moonkeep shines again."
                }
                flags.has(CHAPTER_COMPLETE) -> "Guardian, the Moon Kingdom has many more secrets."
                flags.has(DEFEATED_MOON_BLOB) && !flags.has(SIGIL_QUEST_ACCEPTED) -> {
                    flags.set(FIRST_TRIAL_COMPLETE)
                    flags.set(SIGIL_QUEST_ACCEPTED)
                    showBanner("THE STOLEN SIGIL", "Search beyond the Goblin Fort", 3.0f)
                    "Excellent! But the Moon Goblins stole our silver sigil. The tiny brass key opens their gate."
                }
                flags.has(SIGIL_QUEST_ACCEPTED) -> "The Moon Gate is beside the Goblin Fort. Find the key, then recover our sigil!"
                else -> "Look out! A Moon Blob is guarding the path. Try your stick!"
            }
        }
        return when {
            !flags.has(TALKED_TO_MIA) -> {
                flags.set(TALKED_TO_MIA)
                "You're not seriously going to fight Moon Goblins without your favorite stick. Didn't you leave it by the old tree again?"
            }
            !flags.has(FOUND_STICK) -> "It should still be beneath the old tree. I'll meet you by the fort when you find it."
            !flags.has(MIA_APPROVED_STICK) -> {
                flags.set(MIA_APPROVED_STICK)
                "There it is! Okay, now that's a proper legendary sword. Come on—let's get inside the fort."
            }
            !flags.has(ENTERED_FANTASY) -> "Ready? Climb into the fort with me. We have to decide what we're playing first."
            else -> "Want to visit the Moon Kingdom again?"
        }
    }

    fun onStickCollected() { flags.set(FOUND_STICK) }
    fun canBeginFantasy(): Boolean = flags.has(MIA_APPROVED_STICK) || flags.has(ENTERED_FANTASY)
    fun hasEnteredFantasy(): Boolean = flags.has(ENTERED_FANTASY)

    fun firstTransformationSceneText(): String =
        "Mia crawls into the fort beside you. ‘Okay... this time, we're knights of the Moon Kingdom!’"
    fun fortBlockedMessage(): String = when {
        !flags.has(TALKED_TO_MIA) -> "The fort is quiet. Mia is waiting outside—you should talk to her first."
        !flags.has(FOUND_STICK) -> "You should find your favorite stick before starting the game."
        else -> "Mia wants to see the stick before you both climb inside."
    }
    fun onFantasyEntered() {
        if (!flags.has(ENTERED_FANTASY)) { flags.set(ENTERED_FANTASY); showBanner("THE MOON KINGDOM", "Where the backyard becomes a realm", 3.4f) }
    }
    fun onMoonBlobDefeated(enemyId: String) {
        if (enemyId == "moon_blob" && !flags.has(DEFEATED_MOON_BLOB)) {
            flags.set(DEFEATED_MOON_BLOB); showBanner("PATH CLEARED", "Talk to Sir Mia", 2.4f)
        }
    }
    fun onGateUnlocked() { flags.set(GATE_UNLOCKED); showBanner("MOON GATE OPEN", "The Goblin Fort lies ahead", 2.4f) }
    fun onSigilFound() { flags.set(MOON_SIGIL_FOUND); showBanner("MOON SIGIL FOUND", "Return to Sir Mia", 2.4f) }
    fun questAccepted(): Boolean = flags.has(SIGIL_QUEST_ACCEPTED)
    fun gateUnlocked(): Boolean = flags.has(GATE_UNLOCKED)
    fun sigilFound(): Boolean = flags.has(MOON_SIGIL_FOUND)

    fun reset() {
        listOf(CHAPTER_STARTED,TALKED_TO_MIA,FOUND_STICK,MIA_APPROVED_STICK,ENTERED_FANTASY,
            DEFEATED_MOON_BLOB,FIRST_TRIAL_COMPLETE,SIGIL_QUEST_ACCEPTED,GATE_UNLOCKED,
            MOON_SIGIL_FOUND,CHAPTER_COMPLETE).forEach(flags::clear)
        initializeFreshChapter()
    }

    private fun showBanner(title: String, subtitle: String, duration: Float) {
        bannerTitle = title; bannerSubtitle = subtitle; bannerTimer = duration
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
    }
}
