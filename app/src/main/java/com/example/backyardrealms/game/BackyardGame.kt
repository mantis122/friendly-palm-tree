package com.example.backyardrealms.game

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.backyardrealms.R
import com.example.backyardrealms.engine.Camera2D
import com.example.backyardrealms.engine.GameConfig
import com.example.backyardrealms.engine.InputSnapshot
import com.example.backyardrealms.engine.audio.GameAudio
import com.example.backyardrealms.engine.combat.DamageHit
import com.example.backyardrealms.engine.events.EventBus
import com.example.backyardrealms.engine.events.GameEvent
import com.example.backyardrealms.engine.graphics.SpriteSheet
import com.example.backyardrealms.engine.inventory.Inventory
import com.example.backyardrealms.engine.inventory.ItemCatalog
import com.example.backyardrealms.engine.quest.QuestFlags
import com.example.backyardrealms.engine.save.GameSave
import com.example.backyardrealms.engine.save.SaveManager
import com.example.backyardrealms.engine.story.SequenceRunner
import com.example.backyardrealms.engine.time.WorldTime
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.ambient.AmbientParticle
import com.example.backyardrealms.game.enemy.YardBlob
import com.example.backyardrealms.game.enemy.GoblinScout
import com.example.backyardrealms.game.enemy.BlanketKing
import com.example.backyardrealms.game.items.TreasureChest
import com.example.backyardrealms.game.items.BackyardItemInteractions
import com.example.backyardrealms.game.items.WorldPickup
import com.example.backyardrealms.game.story.ChapterOneDirector
import com.example.backyardrealms.game.theme.ImaginationTheme
import com.example.backyardrealms.game.theme.ThemeTransition
import com.example.backyardrealms.game.world.Landmark
import com.example.backyardrealms.game.world.LandmarkAppearance
import com.example.backyardrealms.game.world.MoonGate
import com.example.backyardrealms.game.world.RoomId
import com.example.backyardrealms.game.world.RoomPortal
import com.example.backyardrealms.game.world.DungeonReward
import com.example.backyardrealms.game.world.CrownFragmentReward
import com.example.backyardrealms.game.puzzle.FloorSwitch
import com.example.backyardrealms.game.puzzle.PuzzleDoor
import kotlin.math.sqrt

class BackyardGame(context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val worldBounds = RectF(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    private var currentRoom = RoomId.BACKYARD
    private val camera = Camera2D(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT)
    private val playerSheet = SpriteSheet(BitmapFactory.decodeResource(context.resources, R.drawable.player_sheet), 24, 32)
    private val saveManager = SaveManager(context)
    private val saved = saveManager.load()
    private val itemCatalog = ItemCatalog(context)
    private val inventory = Inventory(itemCatalog)
    private val questFlags = QuestFlags()
    private val chapterOne = ChapterOneDirector(questFlags)
    private val itemInteractions = BackyardItemInteractions()
    private val player = Player(saved.playerX, saved.playerY, playerSheet)
    private val friend = FriendNpc(470f, 250f)
    private val enemies = listOf(
        YardBlob("moon_blob", 610f, 285f),
        YardBlob("gate_blob_a", 785f, 270f),
        YardBlob("gate_blob_b", 855f, 315f)
    )
    private val eventBus = EventBus()
    private val audio = GameAudio()
    private var theme = saved.theme
    private var worldTime = saved.worldTime
    private val transition = ThemeTransition()
    private var developerOpen = false
    private var inventoryOpen = false
    private var inventoryPage = 0
    private var inventoryInputLatched = false
    private var saveTimer = 0f
    private var verticalInputLatched = false
    private var horizontalInputLatched = false
    private var firstTransformationPending = false
    private val sequence = SequenceRunner()

    private fun a(color: Int, label: String, message: String) = LandmarkAppearance(color, label, message)
    private val landmarks = listOf(
        Landmark("fort", RectF(410f, 92f, 550f, 198f), a(0xFF80552E.toInt(), "FORT", "Step inside and begin an adventure."), a(0xFF6350A8.toInt(), "MOONKEEP", "The Moonkeep hums with imagined magic.")),
        Landmark("tree", RectF(154f, 128f, 218f, 204f), a(0xFF486B2A.toInt(), "TREE", "The oldest tree in the yard."), a(0xFF2E7D50.toInt(), "WORLD TREE", "Its branches hold a thousand kingdoms.")),
        Landmark("shed", RectF(726f, 116f, 842f, 202f), a(0xFF6B645C.toInt(), "SHED", "Dad says not to lose anything behind it."), a(0xFF7A3E38.toInt(), "GOBLIN FORT", "Moon Goblins guard the stolen crown.")),
        Landmark("garden", RectF(650f, 350f, 820f, 438f), a(0xFF526C30.toInt(), "GARDEN", "Tomatoes, beans, and weeds."), a(0xFF285D3A.toInt(), "WHISPERWOOD", "Every leaf sounds like a secret.")),
        Landmark("sandbox", RectF(190f, 356f, 300f, 442f), a(0xFFCDAE6B.toInt(), "SANDBOX", "A desert kingdom waiting for a story."), a(0xFFE0B858.toInt(), "SUNWASTE", "Ancient treasure sleeps beneath the dunes.")),
        Landmark("porch", RectF(370f, 486f, 590f, 530f), a(0xFF96714E.toInt(), "PORCH", "The back door is locked."), a(0xFF74523F.toInt(), "STONE BRIDGE", "Beyond lies the forbidden indoor realm."))
    )
    private val chest = TreasureChest("first_chest", 580f, 370f, listOf("brass_key" to 1, "summer_berry" to 3))
    private val moonGate = MoonGate("moon_gate", RectF(690f, 202f, 714f, 350f))
    private val fortEntrance = RoomPortal("goblin_fort_entrance", RectF(760f, 202f, 808f, 232f), "ENTER", "The Goblin Fort doorway is made from two old porch cushions.")
    private val dungeonExit = RoomPortal("goblin_fort_exit", RectF(64f, 236f, 92f, 300f), "EXIT", "A strip of daylight leads back to the Moon Kingdom.")
    private val treasurePassage = RoomPortal("treasure_passage", RectF(876f, 226f, 914f, 306f), "PASSAGE", "The blanket passage leads deeper into the fort.")
    private val treasureReturn = RoomPortal("treasure_return", RectF(52f, 226f, 90f, 306f), "RETURN", "The blanket doorway leads back to the first room.")
    private val entryPlayerSwitch = FloorSwitch("moon_switch", RectF(400f, 290f, 438f, 310f))
    private val entryMiaSwitch = FloorSwitch("sun_switch", RectF(510f, 290f, 548f, 310f))
    private val entryDoor = PuzzleDoor("double_switch_door", RectF(696f, 184f, 724f, 356f))
    private val dungeonReward = DungeonReward("moon_charm_chest", 760f, 250f)
    private val goblinScout = GoblinScout("goblin_scout", 560f, 260f)
    private val bossEntrance = RoomPortal("blanket_throne_entrance", RectF(846f, 212f, 894f, 300f), "SECRET", "A blanket wall has folded aside, revealing a flashlight-lit passage.")
    private val bossExit = RoomPortal("blanket_throne_exit", RectF(52f, 220f, 86f, 316f), "RETURN", "The passage leads back to the royal toy box.")
    private val crownFragment = CrownFragmentReward("moon_crown_fragment", 760f, 260f)
    private val blanketKing = BlanketKing("blanket_king", 610f, 238f)
    private val dungeonEntryObstacles = listOf(
        RectF(0f, 0f, WORLD_WIDTH, 28f), RectF(0f, WORLD_HEIGHT - 28f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(0f, 0f, 28f, WORLD_HEIGHT), RectF(WORLD_WIDTH - 28f, 0f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(220f, 100f, 300f, 230f), RectF(220f, 340f, 300f, 470f),
        RectF(610f, 80f, 690f, 210f), RectF(610f, 330f, 690f, 470f)
    )
    private val dungeonTreasureObstacles = listOf(
        RectF(0f, 0f, WORLD_WIDTH, 28f), RectF(0f, WORLD_HEIGHT - 28f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(0f, 0f, 28f, WORLD_HEIGHT), RectF(WORLD_WIDTH - 28f, 0f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(290f, 120f, 380f, 210f), RectF(290f, 330f, 380f, 420f),
        RectF(680f, 90f, 900f, 145f), RectF(680f, 395f, 900f, 450f)
    )

    private val bossArenaObstacles = listOf(
        RectF(0f, 0f, WORLD_WIDTH, 28f), RectF(0f, WORLD_HEIGHT - 28f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(0f, 0f, 28f, WORLD_HEIGHT), RectF(WORLD_WIDTH - 28f, 0f, WORLD_WIDTH, WORLD_HEIGHT),
        RectF(250f, 90f, 320f, 180f), RectF(250f, 360f, 320f, 450f),
        RectF(730f, 70f, 900f, 120f), RectF(730f, 420f, 900f, 470f)
    )

    private val pickups = mutableListOf(
        WorldPickup("favorite_stick_pickup", "stick", 224f, 214f, itemCatalog),
        WorldPickup("garden_berry_pickup", "summer_berry", 625f, 442f, itemCatalog),
        WorldPickup("moon_sigil_pickup", "moon_sigil", 875f, 250f, itemCatalog)
    )
    private val particles = List(18) { i -> AmbientParticle("pollen_$i", WORLD_WIDTH, WORLD_HEIGHT, (i * 61f) % WORLD_WIDTH, 40f + (i * 83f) % (WORLD_HEIGHT - 80f), 4f + (i % 4), i * .7f) }
    private val baseObstacles = landmarks.map { it.collisionBounds }
    private fun activeObstacles(): List<RectF> = when (currentRoom) {
        RoomId.BACKYARD -> if (theme == ImaginationTheme.FANTASY && !moonGate.unlocked) baseObstacles + moonGate.bounds else baseObstacles
        RoomId.GOBLIN_FORT_ENTRY -> dungeonEntryObstacles + entryDoor.collisionBounds()
        RoomId.GOBLIN_FORT_TREASURE -> dungeonTreasureObstacles
        RoomId.GOBLIN_FORT_BOSS -> bossArenaObstacles
    }
    private fun activeInteractables(): List<Interactable> = when (currentRoom) {
        RoomId.BACKYARD -> landmarks +
            (if (!chapterOne.miaWentHome()) listOf(friend) else emptyList()) +
            chest + moonGate +
            (if (theme == ImaginationTheme.FANTASY && chapterOne.sigilFound()) listOf(fortEntrance) else emptyList())
        RoomId.GOBLIN_FORT_ENTRY -> listOf(friend, dungeonExit) + if (entryDoor.open) listOf(treasurePassage) else emptyList()
        RoomId.GOBLIN_FORT_TREASURE -> listOf(friend, treasureReturn, dungeonReward) + if (dungeonReward.claimed) listOf(bossEntrance) else emptyList()
        RoomId.GOBLIN_FORT_BOSS -> listOf(friend) +
            (if (!blanketKing.active) listOf(crownFragment) else emptyList()) +
            (if (crownFragment.claimed) listOf(bossExit) else emptyList())
    }
    private var dialogue: String? = null
    private var nearbyInteractable: Interactable? = null
    private var portalInteractionLocked: Boolean = false
    private var showDebug = true

    init {
        inventory.restore(saved.inventory, saved.equippedItemId)
        questFlags.restore(saved.questFlags)
        currentRoom = RoomId.from(saved.roomId)
        // Migrate Chapter 1 Milestone 1 completion into the first-trial flag.
        if (questFlags.has(ChapterOneDirector.CHAPTER_COMPLETE) &&
            !questFlags.has(ChapterOneDirector.SIGIL_QUEST_ACCEPTED)) {
            questFlags.clear(ChapterOneDirector.CHAPTER_COMPLETE)
            questFlags.set(ChapterOneDirector.FIRST_TRIAL_COMPLETE)
        }
        // Milestone 5.1 migration: older builds marked the chapter complete
        // as soon as the Moon Sigil was shown to Mia. The chapter now remains
        // active through the Goblin Fort and Blanket King encounter.
        if (questFlags.has(ChapterOneDirector.CHAPTER_COMPLETE) &&
            questFlags.has(ChapterOneDirector.MOON_SIGIL_FOUND) &&
            !questFlags.has(ChapterOneDirector.CROWN_FRAGMENT_CLAIMED)) {
            questFlags.clear(ChapterOneDirector.CHAPTER_COMPLETE)
        }
        val freshChapter = chapterOne.initializeFreshChapter()
        if (freshChapter) {
            currentRoom = RoomId.BACKYARD
            player.setPosition(470f, 214f)
            theme = ImaginationTheme.REAL
        }
        val opened = saved.openedChests.split(',').filter { it.isNotBlank() }.toSet()
        chest.opened = chest.id in opened
        val collected = saved.collectedPickups.split(',').filter { it.isNotBlank() }.toSet()
        pickups.forEach { it.collected = it.id in collected }
        moonGate.unlocked = chapterOne.gateUnlocked()
        entryDoor.open = chapterOne.dungeonDoorOpen()
        dungeonReward.claimed = chapterOne.dungeonRewardClaimed()
        crownFragment.claimed = chapterOne.crownFragmentClaimed()
        if (chapterOne.goblinScoutDefeated()) goblinScout.receiveHit(DamageHit("load", 999, 0f, 0f, 999999))
        if (chapterOne.blanketKingDefeated()) blanketKing.markDefeated()
        pickups.firstOrNull { it.id == "moon_sigil_pickup" }?.collected = chapterOne.sigilFound()
        if (freshChapter) {
            inventory.remove("stick", inventory.quantity("stick"))
            inventory.remove("summer_berry", inventory.quantity("summer_berry"))
            inventory.remove("brass_key", inventory.quantity("brass_key"))
            inventory.remove("moon_sigil", inventory.quantity("moon_sigil"))
            inventory.equip(null)
            listOf("HAS_STICK", "MET_MIA", "OPENED_FIRST_CHEST", "CLEARED_GARDEN_WEEDS", "ENTERED_FANTASY")
                .forEach(questFlags::clear)
            chest.opened = false
            pickups.forEach { it.collected = false }
            moonGate.unlocked = false
        }
        landmarks.forEach { it.theme = theme }
        friend.theme = theme
        audio.setTheme(theme)
        eventBus.subscribe { event ->
            when (event) {
                is GameEvent.ThemeChanged -> audio.setTheme(theme)
                is GameEvent.InteractionStarted -> audio.interaction(event.targetId)
                GameEvent.AttackStarted -> audio.attack()
                else -> Unit
            }
        }
    }

    fun update(dt: Float, input: InputSnapshot) {
        transition.update(dt)
        chapterOne.update(dt)
        friend.update(dt)
        particles.forEach { it.update(dt) }
        pickups.forEach { it.update(dt) }
        saveTimer += dt
        if (saveTimer >= 5f) { saveGame(); saveTimer = 0f }

        if (sequence.isActive) {
            dialogue = sequence.message
            if (input.interactPressed) {
                sequence.interact()
                dialogue = sequence.message
            }
            camera.follow(player.centerX, player.centerY, worldBounds, dt)
            return
        }

        if (input.developerPressed) {
            developerOpen = !developerOpen
            inventoryOpen = false
        }
        if (input.inventoryPressed && !developerOpen) {
            inventoryOpen = !inventoryOpen
            dialogue = null
        }

        if (developerOpen) {
            updateDeveloper(input)
            camera.follow(player.centerX, player.centerY, worldBounds, dt)
            return
        }
        if (inventoryOpen) {
            if (input.interactPressed) inventoryOpen = false
            val pageCount = kotlin.math.max(1, (inventory.allSlots().size + 2) / 3)
            if (kotlin.math.abs(input.moveY) > 0.7f && !inventoryInputLatched) {
                inventoryPage = if (input.moveY < 0f) {
                    (inventoryPage - 1 + pageCount) % pageCount
                } else {
                    (inventoryPage + 1) % pageCount
                }
                inventoryInputLatched = true
            } else if (kotlin.math.abs(input.moveY) < 0.3f) {
                inventoryInputLatched = false
            }
            inventoryPage = inventoryPage.coerceIn(0, pageCount - 1)
            camera.follow(player.centerX, player.centerY, worldBounds, dt)
            return
        }
        if (transition.isActive) return

        updatePortalInteractionLock()

        if (currentRoom != RoomId.BACKYARD) {
            updateDungeon(dt, input)
            camera.follow(player.centerX, player.centerY, worldBounds, dt)
            return
        }

        collectTouchingPickups()
        nearbyInteractable = findNearestInteractable()
        when {
            input.interactPressed && dialogue != null -> {
                dialogue = null
                if (firstTransformationPending) {
                    firstTransformationPending = false
                    startThemeSwitch()
                }
            }
            input.interactPressed && nearbyInteractable is TreasureChest -> openChest(nearbyInteractable as TreasureChest)
            input.interactPressed && nearbyInteractable is MoonGate -> interactWithGate()
            input.interactPressed && nearbyInteractable is RoomPortal && !portalInteractionLocked ->
                interactWithPortal(nearbyInteractable as RoomPortal)
            input.interactPressed && nearbyInteractable is DungeonReward -> claimDungeonReward()
            input.interactPressed && nearbyInteractable is Landmark && (nearbyInteractable as Landmark).id == "fort" -> {
                when {
                    theme == ImaginationTheme.REAL && !chapterOne.canBeginFantasy() -> {
                        dialogue = chapterOne.fortBlockedMessage()
                    }
                    theme == ImaginationTheme.REAL && !chapterOne.hasEnteredFantasy() -> {
                        beginFirstTransformationScene()
                    }
                    theme == ImaginationTheme.FANTASY && chapterOne.readyToReturnHome() &&
                        !chapterOne.returnedToReality() -> {
                        beginChapterEndingScene()
                    }
                    theme == ImaginationTheme.REAL && chapterOne.miaWentHome() &&
                        !chapterOne.crescentMarkFound() -> discoverCrescentMark()
                    theme == ImaginationTheme.REAL && chapterOne.returnedToReality() &&
                        !chapterOne.miaWentHome() -> {
                        dialogue = "The fort can wait. Mia is still here—you should talk to her before she heads home."
                    }
                    chapterOne.endingComplete() ->
                        dialogue = "Inside the fort, the tiny crescent scratch is still exactly where you found it."
                    else -> startThemeSwitch()
                }
            }
            input.interactPressed && nearbyInteractable != null -> interactWith(nearbyInteractable!!)
            else -> updateGameplay(dt, input)
        }
        camera.follow(player.centerX, player.centerY, worldBounds, dt)
    }

    private fun updateDeveloper(input: InputSnapshot) {
        if (input.interactPressed) startThemeSwitch()
        if (kotlin.math.abs(input.moveY) > 0.7f && !verticalInputLatched) {
            if (input.moveY < 0f) worldTime = worldTime.next() else enemies.forEach { it.forceRespawn() }
            verticalInputLatched = true
            saveGame()
        }
        if (kotlin.math.abs(input.moveY) < 0.3f) verticalInputLatched = false
        if (kotlin.math.abs(input.moveX) > 0.7f && !horizontalInputLatched) {
            if (input.moveX < 0f) resetItemTests() else toggleStickEquipment()
            horizontalInputLatched = true
        }
        if (kotlin.math.abs(input.moveX) < 0.3f) horizontalInputLatched = false
    }

    private fun updateGameplay(dt: Float, input: InputSnapshot) {
        val oldX = player.x
        val oldY = player.y
        val wasAttacking = player.isAttacking
        val canAttack = inventory.equippedItemId == "stick"
        val obstacles = activeObstacles()
        player.update(dt, input, obstacles, worldBounds, canAttack)
        val playerVelocityX = if (dt > 0f) (player.x - oldX) / dt else 0f
        val playerVelocityY = if (dt > 0f) (player.y - oldY) / dt else 0f
        if (theme == ImaginationTheme.FANTASY && chapterOne.hasEnteredFantasy()) {
            val facing = player.facingDirection()
            val enemyNearby = enemies.any { enemy ->
                if (!enemy.active) false else {
                    val dx = enemy.centerX - player.centerX
                    val dy = enemy.centerY - player.centerY
                    dx * dx + dy * dy <= COMPANION_COMBAT_NOTICE_DISTANCE * COMPANION_COMBAT_NOTICE_DISTANCE
                }
            }
            friend.updateCompanion(
                dt = dt,
                playerX = player.centerX,
                playerY = player.centerY,
                playerVelocityX = playerVelocityX,
                playerVelocityY = playerVelocityY,
                playerFacingX = facing.first,
                playerFacingY = facing.second,
                playerAttacking = player.isAttacking,
                enemyNearby = enemyNearby,
                obstacles = obstacles,
                worldBounds = worldBounds
            )
        }
        if (player.x != oldX || player.y != oldY) eventBus.post(GameEvent.PlayerMoved(player.x, player.y))
        if (!wasAttacking && player.isAttacking) eventBus.post(GameEvent.AttackStarted)
        if (input.attackPressed && !canAttack) dialogue = "You need something to swing first."

        if (theme != ImaginationTheme.FANTASY) return

        if (dialogue == null && !moonGate.unlocked &&
            RectF.intersects(friend.interactionBounds, moonGate.interactionBounds)) {
            chapterOne.companionGateReaction()?.let {
                dialogue = it
                saveGame()
            }
        }
        val sigilPickup = pickups.firstOrNull { it.id == "moon_sigil_pickup" }
        if (dialogue == null && sigilPickup != null && !sigilPickup.collected &&
            chapterOne.gateUnlocked() &&
            RectF.intersects(friend.interactionBounds, sigilPickup.bounds)) {
            chapterOne.companionSigilReaction()?.let {
                dialogue = it
                saveGame()
            }
        }

        enemies.forEach { enemy ->
            enemy.update(dt, player.centerX, player.centerY, obstacles, worldBounds)
            if (enemy.active && friend.tryShieldBash(enemy.centerX, enemy.centerY)) {
                if (enemy.stun()) {
                    eventBus.post(GameEvent.InteractionStarted("mia_shield_bash"))
                }
            }
            if (player.isAttacking && enemy.active && RectF.intersects(player.attackBounds(), enemy.hurtBounds)) {
                val knockback = player.attackKnockback()
                val wasAlive = enemy.health.isAlive
                if (enemy.receiveHit(DamageHit("player", itemCatalog["stick"]?.damage ?: 1, knockback.first, knockback.second, player.currentAttackId))) {
                    eventBus.post(GameEvent.DamageDealt("player", enemy.id, 1))
                    if (wasAlive && !enemy.health.isAlive) {
                        eventBus.post(GameEvent.EntityDefeated(enemy.id))
                        chapterOne.onMoonBlobDefeated(enemy.id)
                        saveGame()
                    }
                }
            }
            if (enemy.active && RectF.intersects(player.hurtBounds, enemy.contactBounds)) {
                val dx = player.centerX - enemy.centerX; val dy = player.centerY - enemy.centerY
                val length = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                if (player.receiveHit(DamageHit(enemy.id, 1, dx / length * 145f, dy / length * 145f, 0))) {
                    eventBus.post(GameEvent.DamageDealt(enemy.id, "player", 1))
                    eventBus.post(GameEvent.PlayerHealthChanged(player.health.current, player.health.maximum))
                    if (!player.health.isAlive) respawnPlayer()
                }
            }
        }
    }

    private fun collectTouchingPickups() {
        pickups.filter { !it.collected && RectF.intersects(player.collisionBounds, it.bounds) }.forEach { pickup ->
            if (pickup.id == "moon_sigil_pickup" && (theme != ImaginationTheme.FANTASY || !chapterOne.gateUnlocked())) return@forEach
            if (inventory.add(pickup.itemId) == 0) {
                pickup.collected = true
                val item = itemCatalog[pickup.itemId]
                if (pickup.itemId == "stick") {
                    inventory.equip("stick")
                    questFlags.set("HAS_STICK")
                    chapterOne.onStickCollected()
                } else if (pickup.itemId == "moon_sigil") {
                    chapterOne.onSigilFound()
                }
                dialogue = "Picked up ${item?.name ?: pickup.itemId}."
                saveGame()
            }
        }
    }

    private fun openChest(target: TreasureChest) {
        eventBus.post(GameEvent.InteractionStarted(target.id))
        if (target.opened) { dialogue = target.interactionText(); return }
        val received = mutableListOf<String>()
        target.contents.forEach { (itemId, quantity) ->
            val remaining = inventory.add(itemId, quantity)
            val accepted = quantity - remaining
            if (accepted > 0) received += "${itemCatalog[itemId]?.name ?: itemId} x$accepted"
        }
        target.opened = true
        questFlags.set("OPENED_FIRST_CHEST")
        dialogue = if (received.isEmpty()) "The chest opens, but your bag is full." else "Found ${received.joinToString(" and ")}!"
        saveGame()
    }

    private fun interactWithGate() {
        eventBus.post(GameEvent.InteractionStarted(moonGate.id))
        if (moonGate.unlocked) {
            dialogue = moonGate.interactionText()
            return
        }
        if (!chapterOne.questAccepted()) {
            dialogue = "Sir Mia has not sent you beyond the Goblin Fort yet."
            return
        }
        if (!inventory.contains("brass_key")) {
            dialogue = "The Moon Gate needs a tiny brass key. Search the nearby chest."
            return
        }
        inventory.remove("brass_key", 1)
        moonGate.unlocked = true
        chapterOne.onGateUnlocked()
        dialogue = "The Tiny Brass Key clicks. The Moon Gate swings open!"
        saveGame()
    }

    private fun interactWith(target: Interactable) {
        val id = when (target) { is Landmark -> target.id; is FriendNpc -> target.id; is TreasureChest -> target.id; else -> "unknown" }
        eventBus.post(GameEvent.InteractionStarted(id))
        val equipped = inventory.equippedItemId
        if (equipped != null && !questFlags.has("CLEARED_GARDEN_WEEDS")) {
            val result = itemInteractions.use(equipped, id)
            if (result.handled) {
                result.grantedItems.forEach { (itemId, amount) -> inventory.add(itemId, amount) }
                result.setFlags.forEach { questFlags.set(it) }
                dialogue = result.message
                saveGame()
                return
            }
        }
        if (target is FriendNpc) {
            questFlags.set("MET_MIA")
            if (theme == ImaginationTheme.REAL && chapterOne.returnedToReality() && !chapterOne.miaWentHome()) {
                beginMiaFarewellScene()
                return
            }
            dialogue = chapterOne.miaDialogue(theme)
        } else {
            dialogue = target.interactionText()
        }
        saveGame()
    }

    private fun resetItemTests() {
        inventory.remove("stick", inventory.quantity("stick"))
        inventory.remove("summer_berry", inventory.quantity("summer_berry"))
        inventory.remove("brass_key", inventory.quantity("brass_key"))
        inventory.remove("moon_sigil", inventory.quantity("moon_sigil"))
        inventory.equip(null)
        questFlags.clear("HAS_STICK"); questFlags.clear("OPENED_FIRST_CHEST"); questFlags.clear("CLEARED_GARDEN_WEEDS")
        chest.opened = false
        pickups.forEach { it.collected = false }
        moonGate.unlocked = false
        chapterOne.reset()
        currentRoom = RoomId.BACKYARD
        entryDoor.open = false
        dungeonReward.claimed = false
        goblinScout.restore()
        player.setPosition(470f, 214f)
        friend.setPosition(470f, 250f)
        firstTransformationPending = false
        sequence.clear()
        theme = ImaginationTheme.REAL
        landmarks.forEach { it.theme = theme }
        friend.theme = theme
        audio.setTheme(theme)
        dialogue = "Chapter 1 reset. Close DEV to replay the opening."
        saveGame()
    }

    private fun toggleStickEquipment() {
        if (!inventory.contains("stick")) inventory.add("stick")
        if (inventory.equippedItemId == "stick") inventory.equip(null) else inventory.equip("stick")
        questFlags.set("HAS_STICK")
        dialogue = "Equipped: ${inventory.equippedItemId ?: "nothing"}."
        saveGame()
    }

    private fun respawnPlayer() {
        player.respawn(470f, 214f)
        eventBus.post(GameEvent.PlayerRespawned)
        eventBus.post(GameEvent.PlayerHealthChanged(player.health.current, player.health.maximum))
        dialogue = "You tumble back to Moonkeep and catch your breath."
        saveGame()
    }

    private fun saveGame() = saveManager.save(GameSave(
        player.x, player.y, theme, worldTime,
        inventory.serialize(), inventory.equippedItemId, questFlags.serialize(),
        if (chest.opened) chest.id else "",
        pickups.filter { it.collected }.joinToString(",") { it.id },
        currentRoom.name
    ))

    private fun interactWithPortal(portal: RoomPortal) {
        when (portal.id) {
            "goblin_fort_entrance" -> enterRoom(RoomId.GOBLIN_FORT_ENTRY, 150f, 260f, 180f, 286f)
            "goblin_fort_exit" -> enterRoom(RoomId.BACKYARD, 720f, 272f, 690f, 292f)
            "treasure_passage" -> enterRoom(RoomId.GOBLIN_FORT_TREASURE, 150f, 260f, 182f, 286f)
            "treasure_return" -> enterRoom(RoomId.GOBLIN_FORT_ENTRY, 810f, 260f, 780f, 286f)
            "blanket_throne_entrance" -> enterRoom(RoomId.GOBLIN_FORT_BOSS, 150f, 260f, 182f, 286f)
            "blanket_throne_exit" -> enterRoom(RoomId.GOBLIN_FORT_TREASURE, 790f, 340f, 756f, 366f)
        }
    }

    private fun enterRoom(room: RoomId, playerX: Float, playerY: Float, miaX: Float, miaY: Float) {
        if (transition.isActive) return
        transition.start {
            currentRoom = room
            portalInteractionLocked = true
            player.setPosition(playerX, playerY)
            friend.setPosition(miaX, miaY)
            camera.reset()
            nearbyInteractable = null
            if (room != RoomId.BACKYARD) chapterOne.onGoblinFortEntered()
            if (room == RoomId.GOBLIN_FORT_BOSS) {
                chapterOne.onBossChamberEntered()
                if (!chapterOne.blanketKingDefeated()) blanketKing.restore()
            }
            saveGame()
        }
    }

    private fun updateDungeon(dt: Float, input: InputSnapshot) {
        nearbyInteractable = findNearestInteractable()
        when {
            input.interactPressed && dialogue != null -> dialogue = null
            input.interactPressed && nearbyInteractable is RoomPortal && !portalInteractionLocked ->
                interactWithPortal(nearbyInteractable as RoomPortal)
            input.interactPressed && nearbyInteractable is DungeonReward -> claimDungeonReward()
            input.interactPressed && nearbyInteractable is CrownFragmentReward -> claimCrownFragment()
            input.interactPressed && nearbyInteractable is FriendNpc -> {
                dialogue = chapterOne.miaDungeonDialogue(
                    room = currentRoom,
                    blanketShieldUp = currentRoom == RoomId.GOBLIN_FORT_BOSS && blanketKing.needsShieldBreak
                )
            }
            else -> updateDungeonGameplay(dt, input)
        }
    }

    private fun updateDungeonGameplay(dt: Float, input: InputSnapshot) {
        val obstacles = activeObstacles()
        val oldX = player.x; val oldY = player.y
        val wasAttacking = player.isAttacking
        player.update(dt, input, obstacles, worldBounds, inventory.equippedItemId == "stick")
        val vx = if (dt > 0f) (player.x - oldX) / dt else 0f
        val vy = if (dt > 0f) (player.y - oldY) / dt else 0f

        when (currentRoom) {
            RoomId.GOBLIN_FORT_ENTRY -> {
                entryPlayerSwitch.update(player.collisionBounds)
                if (entryPlayerSwitch.pressed && !entryDoor.open) {
                    friend.updateScriptedPosition(dt, entryMiaSwitch.bounds.centerX(), entryMiaSwitch.bounds.centerY(), obstacles, worldBounds)
                } else {
                    friend.updateCompanion(dt, player.centerX, player.centerY, vx, vy, player.facingDirection().first, player.facingDirection().second, player.isAttacking, false, obstacles, worldBounds)
                }
                entryMiaSwitch.update(friend.interactionBounds)
                if (entryPlayerSwitch.pressed && entryMiaSwitch.pressed && !entryDoor.open) {
                    entryDoor.open = true
                    chapterOne.onDungeonDoorOpened()
                    dialogue = "Both painted switches sink into the floor. The blanket gate slides aside!"
                    saveGame()
                }
            }
            RoomId.GOBLIN_FORT_TREASURE -> {
                friend.updateCompanion(dt, player.centerX, player.centerY, vx, vy, player.facingDirection().first, player.facingDirection().second, player.isAttacking, goblinScout.active, obstacles, worldBounds)
                goblinScout.update(dt, player.centerX, player.centerY, obstacles, worldBounds)
                if (player.isAttacking && goblinScout.active && RectF.intersects(player.attackBounds(), goblinScout.hurtBounds)) {
                    val kb = player.attackKnockback()
                    val alive = goblinScout.health.isAlive
                    if (goblinScout.receiveHit(DamageHit("player", itemCatalog["stick"]?.damage ?: 1, kb.first, kb.second, player.currentAttackId)) && alive && !goblinScout.health.isAlive) {
                        chapterOne.onGoblinScoutDefeated(); saveGame()
                    }
                }
                if (goblinScout.active && RectF.intersects(player.hurtBounds, goblinScout.contactBounds)) {
                    damagePlayerFrom(goblinScout.id, goblinScout.centerX, goblinScout.centerY, 112f, 260f)
                }
            }
            RoomId.GOBLIN_FORT_BOSS -> {
                val bossActive = blanketKing.active
                blanketKing.update(dt, player.centerX, player.centerY, obstacles, worldBounds)

                if (blanketKing.needsShieldBreak) {
                    // During the blanket-shield phase Mia must actively move into
                    // shield-bash range. Normal companion following keeps her near
                    // the player and can otherwise leave the boss permanently
                    // shielded if he stops outside her bash radius.
                    val awayX = friend.centerX - blanketKing.centerX
                    val awayY = friend.centerY - blanketKing.centerY
                    val awayLength = sqrt(awayX * awayX + awayY * awayY).coerceAtLeast(0.001f)
                    val bashStandOff = 34f
                    val bashTargetX = blanketKing.centerX + awayX / awayLength * bashStandOff
                    val bashTargetY = blanketKing.centerY + awayY / awayLength * bashStandOff

                    friend.updateScriptedPosition(
                        dt,
                        bashTargetX,
                        bashTargetY,
                        obstacles,
                        worldBounds
                    )

                    if (friend.tryShieldBash(blanketKing.centerX, blanketKing.centerY)) {
                        if (blanketKing.shieldBash()) {
                            dialogue = "Sir Mia charges in and slams her cardboard shield into the Blanket King. The quilt slips—attack now!"
                        }
                    }
                } else {
                    friend.updateCompanion(
                        dt,
                        player.centerX,
                        player.centerY,
                        vx,
                        vy,
                        player.facingDirection().first,
                        player.facingDirection().second,
                        player.isAttacking,
                        bossActive,
                        obstacles,
                        worldBounds
                    )
                }
                if (player.isAttacking && blanketKing.active && RectF.intersects(player.attackBounds(), blanketKing.hurtBounds)) {
                    val kb = player.attackKnockback()
                    val alive = blanketKing.health.isAlive
                    if (blanketKing.receiveHit(DamageHit("player", itemCatalog["stick"]?.damage ?: 1, kb.first, kb.second, player.currentAttackId)) && alive && !blanketKing.health.isAlive) {
                        chapterOne.onBlanketKingDefeated(); saveGame()
                    }
                }
                if (blanketKing.active && RectF.intersects(player.hurtBounds, blanketKing.contactBounds)) {
                    damagePlayerFrom(blanketKing.id, blanketKing.centerX, blanketKing.centerY, 112f, 260f, bossRetry = true)
                }
                blanketKing.activeHazards().forEach { (hazard, attackId) ->
                    if (RectF.intersects(player.hurtBounds, hazard)) {
                        val dx = player.centerX - hazard.centerX(); val dy = player.centerY - hazard.centerY()
                        val l = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
                        if (player.receiveHit(DamageHit(blanketKing.id, 1, dx / l * 125f, dy / l * 125f, attackId)) && !player.health.isAlive) {
                            player.respawn(112f, 260f); friend.setPosition(144f, 286f)
                            if (!chapterOne.blanketKingDefeated()) blanketKing.restore()
                        }
                    }
                }
            }
            else -> Unit
        }
        if (!wasAttacking && player.isAttacking) eventBus.post(GameEvent.AttackStarted)
    }

    private fun damagePlayerFrom(sourceId: String, sourceX: Float, sourceY: Float, respawnX: Float, respawnY: Float, bossRetry: Boolean = false) {
        val dx = player.centerX - sourceX; val dy = player.centerY - sourceY
        val l = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
        if (player.receiveHit(DamageHit(sourceId, 1, dx / l * 145f, dy / l * 145f, 0)) && !player.health.isAlive) {
            player.respawn(respawnX, respawnY)
            friend.setPosition(respawnX + 32f, respawnY + 26f)
            if (bossRetry && !chapterOne.blanketKingDefeated()) blanketKing.restore()
        }
    }

    private fun claimDungeonReward() {
        if (currentRoom != RoomId.GOBLIN_FORT_TREASURE) return
        if (!chapterOne.goblinScoutDefeated()) { dialogue = "The Goblin Scout is still guarding the toy box."; return }
        if (dungeonReward.claimed) { dialogue = dungeonReward.interactionText(); return }
        dungeonReward.claimed = true
        inventory.add("summer_berry", 5)
        player.health.restore()
        chapterOne.onDungeonRewardClaimed()
        dialogue = "Inside is a cardboard Moon Charm and five Summer Berries. You feel completely refreshed!"
        saveGame()
    }

    private fun claimCrownFragment() {
        if (currentRoom != RoomId.GOBLIN_FORT_BOSS) return
        if (blanketKing.active) { dialogue = "The Blanket King still guards the broken crown."; return }
        if (crownFragment.claimed) { dialogue = crownFragment.interactionText(); return }
        crownFragment.claimed = true
        chapterOne.onCrownFragmentClaimed()
        dialogue = "You lift the silver cardboard shard. Mia goes quiet. ‘That wasn't part of our game... was it?’"
        saveGame()
    }

    private fun drawDungeon(canvas: Canvas) {
        paint.color = when (currentRoom) {
            RoomId.GOBLIN_FORT_ENTRY -> 0xFF4C372D.toInt()
            RoomId.GOBLIN_FORT_TREASURE -> 0xFF3D2E35.toInt()
            RoomId.GOBLIN_FORT_BOSS -> 0xFF2A2234.toInt()
            else -> 0xFF4C372D.toInt()
        }
        canvas.drawRect(worldBounds, paint)
        paint.color = if (currentRoom == RoomId.GOBLIN_FORT_BOSS) 0xFF59445F.toInt() else 0xFF6E503E.toInt()
        canvas.drawRect(28f, 28f, WORLD_WIDTH - 28f, WORLD_HEIGHT - 28f, paint)
        paint.color = 0xFF8A6A50.toInt()
        for (x in 40..920 step 48) canvas.drawRect(x.toFloat(), 28f, x + 5f, WORLD_HEIGHT - 28f, paint)
        paint.color = 0xFF72527C.toInt()
        canvas.drawRect(170f, 42f, 310f, 72f, paint); canvas.drawRect(650f, 42f, 790f, 72f, paint)

        when (currentRoom) {
            RoomId.GOBLIN_FORT_ENTRY -> {
                dungeonExit.draw(canvas, paint)
                entryPlayerSwitch.draw(canvas, paint); entryMiaSwitch.draw(canvas, paint); entryDoor.draw(canvas, paint)
                if (entryDoor.open) treasurePassage.draw(canvas, paint)
            }
            RoomId.GOBLIN_FORT_TREASURE -> {
                treasureReturn.draw(canvas, paint); dungeonReward.draw(canvas, paint); goblinScout.draw(canvas)
                if (dungeonReward.claimed) bossEntrance.draw(canvas, paint)
            }
            RoomId.GOBLIN_FORT_BOSS -> {
                if (crownFragment.claimed) bossExit.draw(canvas, paint)
                blanketKing.draw(canvas)
                crownFragment.draw(canvas, paint)
            }
            else -> Unit
        }
        friend.draw(canvas)
    }

    private fun beginFirstTransformationScene() {
        if (firstTransformationPending || transition.isActive) return
        player.setPosition(466f, 166f)
        friend.setPosition(488f, 166f)
        nearbyInteractable = null
        firstTransformationPending = true
        dialogue = chapterOne.firstTransformationSceneText()
    }

    private fun beginChapterEndingScene() {
        if (sequence.isActive || transition.isActive) return
        player.setPosition(466f, 166f)
        friend.setPosition(488f, 166f)
        nearbyInteractable = null
        chapterOne.beginEndingRevelation()
        saveGame()
        sequence.start(listOf(
            SequenceRunner.Step.Message("Sir Mia sets her cardboard shield down. “Okay. Moon Kingdom saved.”"),
            SequenceRunner.Step.Message("She turns the silver crown fragment over in her hands. “Wait... did you make this part?”"),
            SequenceRunner.Step.Message("You shake your head. Mia stops smiling. “I didn't either. We never made this.”"),
            SequenceRunner.Step.Message("For a moment Moonkeep is completely quiet. Then Mia says, “Let's take it outside.”"),
            SequenceRunner.Step.Action { startThemeSwitch() }
        ))
        dialogue = sequence.message
    }

    private fun beginMiaFarewellScene() {
        if (sequence.isActive) return
        sequence.start(listOf(
            SequenceRunner.Step.Message("In daylight the Moon Crown fragment looks like a dull piece of tarnished metal, with a tiny crescent scratched into it."),
            SequenceRunner.Step.Message("Mia looks from the metal to you. “...Did you put that there?”"),
            SequenceRunner.Step.Message("“No.” Mia shakes her head. “Me neither.” From the house, someone calls that lunch is ready."),
            SequenceRunner.Step.Message("Mia backs toward the gate. “I have to go. Tomorrow?”"),
            SequenceRunner.Step.Message("“Definitely.”"),
            SequenceRunner.Step.Action {
                chapterOne.onMiaWentHome()
                friend.setPosition(-200f, -200f)
                saveGame()
            }
        ))
        dialogue = sequence.message
    }

    private fun discoverCrescentMark() {
        if (sequence.isActive || chapterOne.crescentMarkFound()) return
        sequence.start(listOf(
            SequenceRunner.Step.Message("Alone, you crawl back into the fort. Something catches your eye beneath the old wooden shelf."),
            SequenceRunner.Step.Message("A tiny crescent has been scratched into the wood—the same shape as the mark on the metal fragment."),
            SequenceRunner.Step.Message("You know every inch of this fort. That mark wasn't there this morning."),
            SequenceRunner.Step.Action {
                chapterOne.onCrescentMarkFound()
                saveGame()
            }
        ))
        dialogue = sequence.message
    }

    private fun startThemeSwitch() {
        if (transition.isActive) return
        dialogue = null
        transition.start {
            theme = theme.toggled()
            landmarks.forEach { it.theme = theme }
            friend.theme = theme
            if (theme == ImaginationTheme.FANTASY) {
                if (!chapterOne.hasEnteredFantasy()) {
                    player.setPosition(470f, 214f)
                    friend.setPosition(470f, 250f)
                }
                enemies.forEach { it.forceRespawn() }
                questFlags.set("ENTERED_FANTASY")
                chapterOne.onFantasyEntered()
            } else {
                friend.setPosition(470f, 250f)
                if (chapterOne.readyToReturnHome() && !chapterOne.returnedToReality()) {
                    player.setPosition(470f, 214f)
                    chapterOne.onReturnedToReality()
                    saveGame()
                    sequence.start(listOf(
                        SequenceRunner.Step.Message("The purple towers are boards again. Sir Mia's shield is cardboard. The Goblin Fort is just the old shed."),
                        SequenceRunner.Step.Message("But when you reach into your pocket, the crown fragment is still there."),
                        SequenceRunner.Step.Message("It isn't shining anymore—just a strange piece of tarnished metal with a crescent scratched into it.")
                    ))
                    dialogue = sequence.message
                }
            }
            eventBus.post(GameEvent.ThemeChanged(theme.name))
            saveGame()
        }
    }

    fun isInteractionAvailable(): Boolean =
        dialogue != null || (!developerOpen && !inventoryOpen && nearbyInteractable != null)

    fun draw(canvas: Canvas) {
        camera.begin(canvas)
        if (currentRoom == RoomId.BACKYARD) {
            drawGround(canvas)
            particles.forEach { it.draw(canvas) }
            landmarks.forEach { it.draw(canvas) }
            if (theme == ImaginationTheme.REAL && chapterOne.miaWentHome() && !chapterOne.crescentMarkFound()) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = 0xFFB8A77A.toInt()
                canvas.drawCircle(472f, 150f, 7f, paint)
                paint.color = 0xFF80552E.toInt()
                canvas.drawCircle(475f, 148f, 7f, paint)
                paint.style = Paint.Style.FILL
            }
            chest.draw(canvas, paint)
            if (theme == ImaginationTheme.FANTASY) moonGate.draw(canvas, paint)
            if (theme == ImaginationTheme.FANTASY && chapterOne.sigilFound()) fortEntrance.draw(canvas, paint)
            pickups.filter { it.id != "moon_sigil_pickup" || (theme == ImaginationTheme.FANTASY && chapterOne.gateUnlocked()) }.forEach { it.draw(canvas, paint) }
            if (!chapterOne.miaWentHome()) friend.draw(canvas)
            if (theme == ImaginationTheme.FANTASY) enemies.forEach { it.draw(canvas) }
        } else {
            drawDungeon(canvas)
        }
        player.draw(canvas)
        if (showDebug) drawWorldDebug(canvas)
        camera.end(canvas)
        drawTimeOverlay(canvas)
        drawHealthHud(canvas)
        drawEquippedHud(canvas)
        drawObjectiveHud(canvas)
        drawPrompt(canvas)
        drawDialogue(canvas)
        if (showDebug) drawDebugPanel(canvas)
        if (inventoryOpen) drawInventoryPanel(canvas)
        if (developerOpen) drawDeveloperPanel(canvas)
        drawStoryBanner(canvas)
        if (transition.alpha > 0f) {
            paint.color = ((transition.alpha * 255).toInt().coerceIn(0, 255) shl 24)
            canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)
        }
    }

    private fun findNearestInteractable(): Interactable? {
        return activeInteractables()
            .asSequence()
            .filter {
                (it !is MoonGate || theme == ImaginationTheme.FANTASY) &&
                    RectF.intersects(player.collisionBounds, it.interactionBounds)
            }
            .minWithOrNull(
                compareBy<Interactable>(
                    { interactionPriority(it) },
                    {
                        val bounds = it.interactionBounds
                        val dx = bounds.centerX() - player.centerX
                        val dy = bounds.centerY() - player.centerY
                        dx * dx + dy * dy
                    }
                )
            )
    }

    /**
     * Room transitions temporarily lock portal interaction. The lock is only
     * released after the player has physically left every active portal's
     * interaction zone, preventing an arrival doorway from immediately sending
     * the player back where they came from.
     */
    private fun updatePortalInteractionLock() {
        if (!portalInteractionLocked) return

        val touchingPortal = activeInteractables()
            .filterIsInstance<RoomPortal>()
            .any { RectF.intersects(player.collisionBounds, it.interactionBounds) }

        if (!touchingPortal) {
            portalInteractionLocked = false
        }
    }

    /**
     * Smaller values win when multiple interaction zones overlap.
     *
     * Large landmarks deliberately have generous interaction bounds, so a
     * doorway, gate, chest, reward, or NPC inside that area must take
     * precedence over the landmark itself.
     */
    private fun interactionPriority(target: Interactable): Int = when (target) {
        is RoomPortal -> 0
        is MoonGate -> 1
        is TreasureChest -> 2
        is DungeonReward -> 3
        is CrownFragmentReward -> 3
        is FriendNpc -> 4
        is Landmark -> 10
        else -> 5
    }

    private fun drawGround(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF79A84D.toInt() else 0xFF3C7650.toInt(); canvas.drawRect(worldBounds, paint)
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF4C7435.toInt() else 0xFF263B57.toInt()
        canvas.drawRect(0f, 0f, WORLD_WIDTH, 20f, paint); canvas.drawRect(0f, WORLD_HEIGHT - 20f, WORLD_WIDTH, WORLD_HEIGHT, paint)
        canvas.drawRect(0f, 0f, 20f, WORLD_HEIGHT, paint); canvas.drawRect(WORLD_WIDTH - 20f, 0f, WORLD_WIDTH, WORLD_HEIGHT, paint)
        paint.color = if (theme == ImaginationTheme.REAL) 0xFFC6A16B.toInt() else 0xFF8C79A8.toInt()
        canvas.drawRect(450f, 198f, 510f, 486f, paint); canvas.drawRect(300f, 284f, 680f, 326f, paint)
    }

    private fun drawTimeOverlay(canvas: Canvas) { if (worldTime.overlayColor != 0) { paint.color = worldTime.overlayColor; canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint) } }

    private fun drawHealthHud(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT; paint.textSize = 12f
        for (i in 0 until player.health.maximum) {
            paint.color = if (i < player.health.current) 0xFFE84A5F.toInt() else 0xFF542B35.toInt()
            val left = 12f + i * 17f; canvas.drawCircle(left + 5f, 93f, 5f, paint); canvas.drawCircle(left + 10f, 93f, 5f, paint)
            val path = android.graphics.Path(); path.moveTo(left, 95f); path.lineTo(left + 15f, 95f); path.lineTo(left + 7.5f, 104f); path.close(); canvas.drawPath(path, paint)
        }
    }

    private fun drawEquippedHud(canvas: Canvas) {
        paint.color = 0xCC111111.toInt(); canvas.drawRoundRect(400f, 44f, 468f, 78f, 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 8f; canvas.drawText("EQUIPPED", 434f, 55f, paint)
        val equipped = inventory.equippedItemId?.let { itemCatalog[it]?.name } ?: "None"
        paint.textSize = 9f; canvas.drawText(equipped, 434f, 69f, paint)
    }

    private fun drawObjectiveHud(canvas: Canvas) {
        if (developerOpen || inventoryOpen || chapterOne.bannerTimer > 0f) return
        paint.color = 0xC9141414.toInt()
        canvas.drawRoundRect(108f, 82f, 372f, 108f, 6f, 6f, paint)
        paint.color = 0xFFFFE9A8.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        canvas.drawText("OBJECTIVE: ${chapterOne.objective(theme)}", 240f, 99f, paint)
    }

    private fun drawStoryBanner(canvas: Canvas) {
        val title = chapterOne.bannerTitle ?: return
        paint.color = 0xD9000000.toInt()
        canvas.drawRect(0f, 88f, GameConfig.LOGICAL_WIDTH, 178f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 23f
        canvas.drawText(title, 240f, 125f, paint)
        paint.color = 0xFFFFE2A1.toInt()
        paint.textSize = 11f
        canvas.drawText(chapterOne.bannerSubtitle.orEmpty(), 240f, 149f, paint)
    }

    private fun drawPrompt(canvas: Canvas) {
        if (dialogue != null || nearbyInteractable == null || developerOpen || inventoryOpen) return
        paint.color = 0xCC000000.toInt(); canvas.drawRoundRect(170f, 222f, 310f, 250f, 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 11f
        val text = when {
            nearbyInteractable is Landmark && (nearbyInteractable as Landmark).id == "fort" -> when {
                theme == ImaginationTheme.REAL && !chapterOne.canBeginFantasy() -> "INTERACT: CHECK FORT"
                theme == ImaginationTheme.REAL && !chapterOne.hasEnteredFantasy() -> "INTERACT: ENTER WITH MIA"
                theme == ImaginationTheme.FANTASY && chapterOne.readyToReturnHome() &&
                    !chapterOne.returnedToReality() -> "INTERACT: RETURN WITH MIA"
                theme == ImaginationTheme.REAL && chapterOne.miaWentHome() &&
                    !chapterOne.crescentMarkFound() -> "INTERACT: CHECK FORT"
                chapterOne.endingComplete() -> "INTERACT: CHECK MARK"
                else -> "INTERACT: SWITCH WORLD"
            }
            nearbyInteractable is TreasureChest || nearbyInteractable is DungeonReward -> "INTERACT: OPEN"
            nearbyInteractable is RoomPortal -> "INTERACT: ENTER"
            else -> "INTERACT"
        }
        canvas.drawText(text, 240f, 240f, paint)
    }

    private fun drawDialogue(canvas: Canvas) {
        val text = dialogue ?: return
        paint.color = 0xEE171717.toInt(); canvas.drawRoundRect(28f, 184f, 452f, 256f, 8f, 8f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.LEFT; paint.textSize = 12f
        canvas.drawText(text.take(68), 44f, 216f, paint)
        if (text.length > 68) canvas.drawText(text.drop(68).take(68), 44f, 233f, paint)
        paint.textAlign = Paint.Align.RIGHT; paint.textSize = 9f; canvas.drawText("Tap INTERACT to close", 436f, 248f, paint)
    }

    private fun drawWorldDebug(canvas: Canvas) {
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 1f; paint.color = 0x99FFFF00.toInt()
        if (currentRoom == RoomId.BACKYARD) { landmarks.forEach { canvas.drawRect(it.collisionBounds, paint) }; canvas.drawRect(chest.bounds, paint) } else { activeObstacles().forEach { canvas.drawRect(it, paint) } }
        if (theme == ImaginationTheme.FANTASY && !moonGate.unlocked) canvas.drawRect(moonGate.bounds, paint)
        paint.color = 0x9900FFFF.toInt(); nearbyInteractable?.let { canvas.drawRect(it.interactionBounds, paint) }
        if (player.isAttacking) { paint.color = 0x99FF3333.toInt(); canvas.drawRect(player.attackBounds(), paint) }
        if (theme == ImaginationTheme.FANTASY) enemies.filter { it.active }.forEach { enemy ->
            paint.color = 0x99FF66FF.toInt(); canvas.drawRect(enemy.hurtBounds, paint)
            paint.color = 0x99FF9900.toInt(); canvas.drawRect(enemy.contactBounds, paint)
        }
        if (currentRoom == RoomId.GOBLIN_FORT_BOSS && blanketKing.active) {
            paint.color = 0x99FF66FF.toInt(); canvas.drawRect(blanketKing.hurtBounds, paint)
            paint.color = 0x99FF9900.toInt(); canvas.drawRect(blanketKing.contactBounds, paint)
            blanketKing.activeHazards().forEach { hazard ->
                paint.color = 0x99FF3333.toInt(); canvas.drawRect(hazard.first, paint)
            }
        }
        if (theme == ImaginationTheme.FANTASY && chapterOne.hasEnteredFantasy()) {
            paint.color = if (friend.debugCombatMode) 0x99FF5555.toInt() else 0x9966FFAA.toInt()
            canvas.drawCircle(player.centerX, player.centerY, friend.debugKeepOutRadius, paint)
            canvas.drawCircle(friend.debugPreferredX, friend.debugPreferredY, 5f, paint)
            canvas.drawLine(friend.centerX, friend.centerY, friend.debugPreferredX, friend.debugPreferredY, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDebugPanel(canvas: Canvas) {
        paint.color = 0xCC000000.toInt(); canvas.drawRect(6f, 6f, 320f, 80f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.LEFT; paint.textSize = 10f
        canvas.drawText("Backyard Realms Ch.1 M5", 12f, 18f, paint)
        canvas.drawText("room=$currentRoom theme=$theme time=$worldTime", 12f, 30f, paint)
        canvas.drawText("${player.positionText()} hp=${player.health.current}/${player.health.maximum}", 12f, 42f, paint)
        canvas.drawText("equipped=${inventory.equippedItemId ?: "none"} slots=${inventory.allSlots().size}", 12f, 54f, paint)
        canvas.drawText("flags=${questFlags.all().size} chest=${if (chest.opened) "open" else "closed"}", 12f, 66f, paint)
        canvas.drawText("event=${eventBus.lastEvent?.javaClass?.simpleName ?: "none"}", 170f, 66f, paint)
    }

    private fun drawInventoryPanel(canvas: Canvas) {
        paint.color = 0xF0181818.toInt()
        canvas.drawRoundRect(52f, 24f, 428f, 248f, 12f, 12f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 18f
        canvas.drawText("BACKPACK", 240f, 49f, paint)

        val slots = inventory.allSlots()
        val itemsPerPage = 3
        val pageCount = kotlin.math.max(1, (slots.size + itemsPerPage - 1) / itemsPerPage)
        inventoryPage = inventoryPage.coerceIn(0, pageCount - 1)
        val visibleSlots = slots.drop(inventoryPage * itemsPerPage).take(itemsPerPage)

        if (slots.isEmpty()) {
            paint.textSize = 12f
            canvas.drawText("Your backpack is empty.", 240f, 132f, paint)
        }

        visibleSlots.forEachIndexed { index, slot ->
            val definition = itemCatalog[slot.itemId] ?: return@forEachIndexed
            val left = 70f
            val top = 62f + index * 51f
            val right = 410f
            val bottom = top + 44f

            paint.color = 0xFF333333.toInt()
            canvas.drawRoundRect(left, top, right, bottom, 7f, 7f, paint)
            paint.color = definition.iconColor
            canvas.drawRoundRect(left + 8f, top + 8f, left + 34f, top + 34f, 4f, 4f, paint)

            paint.color = 0xFFFFFFFF.toInt()
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 10f
            val marker = if (inventory.equippedItemId == slot.itemId) " [E]" else ""
            canvas.drawText("${definition.name}$marker   x${slot.quantity}", left + 44f, top + 14f, paint)

            paint.textSize = 8.5f
            val descriptionLines = wrapText(definition.description, 52, 2)
            descriptionLines.forEachIndexed { lineIndex, line ->
                canvas.drawText(line, left + 44f, top + 27f + lineIndex * 10f, paint)
            }
        }

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        if (pageCount > 1) {
            canvas.drawText("Joystick up/down: page  ${inventoryPage + 1}/$pageCount", 240f, 224f, paint)
        }
        canvas.drawText("BAG or ACTION: close", 240f, 239f, paint)
    }

    private fun wrapText(text: String, maxCharacters: Int, maxLines: Int): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (word in words) {
            val candidateLength = if (current.isEmpty()) word.length else current.length + 1 + word.length
            if (candidateLength <= maxCharacters) {
                if (current.isNotEmpty()) current.append(' ')
                current.append(word)
            } else {
                if (current.isNotEmpty()) lines += current.toString()
                current = StringBuilder(word)
                if (lines.size == maxLines - 1) break
            }
        }
        if (lines.size < maxLines && current.isNotEmpty()) lines += current.toString()
        return lines.take(maxLines)
    }

    private fun drawDeveloperPanel(canvas: Canvas) {
        paint.color = 0xF0111111.toInt(); canvas.drawRoundRect(42f, 30f, 438f, 248f, 10f, 10f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.CENTER; paint.textSize = 16f; canvas.drawText("ENGINE PLAYGROUND / CONTENT", 240f, 52f, paint)
        paint.textSize = 10f
        canvas.drawText("INTERACT theme  •  UP time  •  DOWN enemy", 240f, 70f, paint)
        canvas.drawText("LEFT reset Chapter 1  •  RIGHT equip/unequip stick", 240f, 84f, paint)
        canvas.drawText("ITEM CATALOG", 240f, 105f, paint)
        itemCatalog.all().forEachIndexed { index, item ->
            val y = 124f + index * 30f
            paint.color = item.iconColor; canvas.drawRoundRect(68f, y - 12f, 88f, y + 8f, 3f, 3f, paint)
            paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.LEFT; paint.textSize = 10f
            canvas.drawText("${item.id}: ${item.name}", 98f, y - 2f, paint)
            paint.textSize = 8f
            canvas.drawText("stack=${item.maxStack} equip=${item.equippable} use=${item.usable} quest=${item.questItem} dmg=${item.damage}", 98f, y + 10f, paint)
        }
        paint.textAlign = Paint.Align.CENTER; paint.textSize = 9f
        canvas.drawText("flags: ${questFlags.all().joinToString().ifEmpty { "none" }}", 240f, 222f, paint)
        canvas.drawText("DEV: close", 240f, 239f, paint)
    }

    companion object {
        private const val WORLD_WIDTH = 960f
        private const val WORLD_HEIGHT = 540f
        private const val COMPANION_COMBAT_NOTICE_DISTANCE = 125f
    }
}
