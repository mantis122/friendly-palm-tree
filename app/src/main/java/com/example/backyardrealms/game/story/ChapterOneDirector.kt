package com.example.backyardrealms.game.story

import com.example.backyardrealms.engine.quest.QuestFlags
import com.example.backyardrealms.game.theme.ImaginationTheme

/**
 * Game-specific story direction for the opening of Backyard Realms.
 *
 * The engine owns flags and persistence. This class only interprets those flags
 * as Chapter 1 objectives and dialogue.
 */
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
        !flags.has(TALKED_TO_MIA) -> "Talk to Mia near the fort."
        !flags.has(FOUND_STICK) -> "Find your favorite stick beneath the old tree."
        !flags.has(MIA_APPROVED_STICK) -> "Show Mia that you found the stick."
        !flags.has(ENTERED_FANTASY) -> "Meet Mia at the fort and begin the game."
        theme == ImaginationTheme.FANTASY && !flags.has(DEFEATED_MOON_BLOB) -> "Defeat the Moon Blob."
        flags.has(DEFEATED_MOON_BLOB) && !flags.has(CHAPTER_COMPLETE) -> "Talk to Sir Mia."
        flags.has(CHAPTER_COMPLETE) -> "Explore the Moon Kingdom."
        else -> "Return to the fantasy realm."
    }

    fun miaDialogue(theme: ImaginationTheme): String {
        if (theme == ImaginationTheme.FANTASY) {
            return when {
                flags.has(DEFEATED_MOON_BLOB) && !flags.has(CHAPTER_COMPLETE) -> {
                    flags.set(CHAPTER_COMPLETE)
                    showBanner("CHAPTER 1", "The adventure has begun", 3.4f)
                    "You did it! Sir Mia officially names you Guardian of Moonkeep."
                }
                flags.has(CHAPTER_COMPLETE) -> "Guardian! The Moon Kingdom is waiting for us."
                else -> "Look out! A Moon Blob is guarding the path. Try your stick!"
            }
        }

        return when {
            !flags.has(TALKED_TO_MIA) -> {
                flags.set(TALKED_TO_MIA)
                "Come on! I thought we were going to play today. Your favorite stick is by the old tree."
            }
            !flags.has(FOUND_STICK) -> "Your stick should still be beneath the old tree. I'll wait here."
            !flags.has(MIA_APPROVED_STICK) -> {
                flags.set(MIA_APPROVED_STICK)
                "Perfect! That's definitely a legendary sword. Meet me at the fort!"
            }
            !flags.has(ENTERED_FANTASY) -> "Ready? Use the fort and we'll start the adventure."
            else -> "Want to visit the Moon Kingdom again?"
        }
    }

    fun onStickCollected() {
        if (!flags.has(FOUND_STICK)) flags.set(FOUND_STICK)
    }

    fun canBeginFantasy(): Boolean = flags.has(MIA_APPROVED_STICK) || flags.has(ENTERED_FANTASY)

    fun fortBlockedMessage(): String = when {
        !flags.has(TALKED_TO_MIA) -> "Mia is waiting outside. You should talk to her first."
        !flags.has(FOUND_STICK) -> "You promised to find your favorite stick first."
        else -> "Show Mia the stick before you begin."
    }

    fun onFantasyEntered() {
        if (!flags.has(ENTERED_FANTASY)) {
            flags.set(ENTERED_FANTASY)
            showBanner("THE MOON KINGDOM", "Where the backyard becomes a realm", 3.4f)
        }
    }

    fun onMoonBlobDefeated() {
        if (!flags.has(DEFEATED_MOON_BLOB)) {
            flags.set(DEFEATED_MOON_BLOB)
            showBanner("PATH CLEARED", "Talk to Sir Mia", 2.4f)
        }
    }

    fun reset() {
        listOf(
            CHAPTER_STARTED,
            TALKED_TO_MIA,
            FOUND_STICK,
            MIA_APPROVED_STICK,
            ENTERED_FANTASY,
            DEFEATED_MOON_BLOB,
            CHAPTER_COMPLETE
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
        const val CHAPTER_COMPLETE = "CH1_COMPLETE"
    }
}
