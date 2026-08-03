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
import com.example.backyardrealms.engine.time.WorldTime
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.ambient.AmbientParticle
import com.example.backyardrealms.game.enemy.YardBlob
import com.example.backyardrealms.game.items.TreasureChest
import com.example.backyardrealms.game.items.BackyardItemInteractions
import com.example.backyardrealms.game.items.WorldPickup
import com.example.backyardrealms.game.story.ChapterOneDirector
import com.example.backyardrealms.game.theme.ImaginationTheme
import com.example.backyardrealms.game.theme.ThemeTransition
import com.example.backyardrealms.game.world.Landmark
import com.example.backyardrealms.game.world.LandmarkAppearance
import com.example.backyardrealms.game.world.MoonGate
import kotlin.math.sqrt

class BackyardGame(context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val worldBounds = RectF(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
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
    private val pickups = mutableListOf(
        WorldPickup("favorite_stick_pickup", "stick", 224f, 214f, itemCatalog),
        WorldPickup("garden_berry_pickup", "summer_berry", 625f, 442f, itemCatalog),
        WorldPickup("moon_sigil_pickup", "moon_sigil", 875f, 250f, itemCatalog)
    )
    private val particles = List(18) { i -> AmbientParticle("pollen_$i", WORLD_WIDTH, WORLD_HEIGHT, (i * 61f) % WORLD_WIDTH, 40f + (i * 83f) % (WORLD_HEIGHT - 80f), 4f + (i % 4), i * .7f) }
    private val baseObstacles = landmarks.map { it.collisionBounds }
    private fun activeObstacles(): List<RectF> = if (theme == ImaginationTheme.FANTASY && !moonGate.unlocked) baseObstacles + moonGate.bounds else baseObstacles
    private val interactables: List<Interactable> = landmarks + friend + chest + moonGate
    private var dialogue: String? = null
    private var nearbyInteractable: Interactable? = null
    private var showDebug = true

    init {
        inventory.restore(saved.inventory, saved.equippedItemId)
        questFlags.restore(saved.questFlags)
        // Migrate Chapter 1 Milestone 1 completion into the first-trial flag.
        if (questFlags.has(ChapterOneDirector.CHAPTER_COMPLETE) &&
            !questFlags.has(ChapterOneDirector.SIGIL_QUEST_ACCEPTED)) {
            questFlags.clear(ChapterOneDirector.CHAPTER_COMPLETE)
            questFlags.set(ChapterOneDirector.FIRST_TRIAL_COMPLETE)
        }
        val freshChapter = chapterOne.initializeFreshChapter()
        if (freshChapter) {
            player.setPosition(470f, 214f)
            theme = ImaginationTheme.REAL
        }
        val opened = saved.openedChests.split(',').filter { it.isNotBlank() }.toSet()
        chest.opened = chest.id in opened
        val collected = saved.collectedPickups.split(',').filter { it.isNotBlank() }.toSet()
        pickups.forEach { it.collected = it.id in collected }
        moonGate.unlocked = chapterOne.gateUnlocked()
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
            if (input.actionPressed) inventoryOpen = false
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

        collectTouchingPickups()
        nearbyInteractable = findNearestInteractable()
        when {
            input.actionPressed && dialogue != null -> {
                dialogue = null
                if (firstTransformationPending) {
                    firstTransformationPending = false
                    startThemeSwitch()
                }
            }
            input.actionPressed && nearbyInteractable is TreasureChest -> openChest(nearbyInteractable as TreasureChest)
            input.actionPressed && nearbyInteractable is MoonGate -> interactWithGate()
            input.actionPressed && nearbyInteractable is Landmark && (nearbyInteractable as Landmark).id == "fort" -> {
                when {
                    theme == ImaginationTheme.REAL && !chapterOne.canBeginFantasy() -> {
                        dialogue = chapterOne.fortBlockedMessage()
                    }
                    theme == ImaginationTheme.REAL && !chapterOne.hasEnteredFantasy() -> {
                        beginFirstTransformationScene()
                    }
                    else -> startThemeSwitch()
                }
            }
            input.actionPressed && nearbyInteractable != null -> interactWith(nearbyInteractable!!)
            else -> updateGameplay(dt, input)
        }
        camera.follow(player.centerX, player.centerY, worldBounds, dt)
    }

    private fun updateDeveloper(input: InputSnapshot) {
        if (input.actionPressed) startThemeSwitch()
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
        if (player.x != oldX || player.y != oldY) eventBus.post(GameEvent.PlayerMoved(player.x, player.y))
        if (!wasAttacking && player.isAttacking) eventBus.post(GameEvent.AttackStarted)
        if (input.actionPressed && !canAttack && nearbyInteractable == null) dialogue = "You need something to swing first."

        if (theme != ImaginationTheme.FANTASY) return
        enemies.forEach { enemy ->
            enemy.update(dt, player.centerX, player.centerY, obstacles, worldBounds)
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
        player.setPosition(470f, 214f)
        friend.setPosition(470f, 250f)
        firstTransformationPending = false
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
        pickups.filter { it.collected }.joinToString(",") { it.id }
    ))

    private fun beginFirstTransformationScene() {
        if (firstTransformationPending || transition.isActive) return
        player.setPosition(466f, 166f)
        friend.setPosition(488f, 166f)
        nearbyInteractable = null
        firstTransformationPending = true
        dialogue = chapterOne.firstTransformationSceneText()
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
            }
            eventBus.post(GameEvent.ThemeChanged(theme.name))
            saveGame()
        }
    }

    fun draw(canvas: Canvas) {
        camera.begin(canvas)
        drawGround(canvas)
        particles.forEach { it.draw(canvas) }
        landmarks.forEach { it.draw(canvas) }
        chest.draw(canvas, paint)
        if (theme == ImaginationTheme.FANTASY) moonGate.draw(canvas, paint)
        pickups.filter { it.id != "moon_sigil_pickup" || (theme == ImaginationTheme.FANTASY && chapterOne.gateUnlocked()) }.forEach { it.draw(canvas, paint) }
        friend.draw(canvas)
        if (theme == ImaginationTheme.FANTASY) enemies.forEach { it.draw(canvas) }
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

    private fun findNearestInteractable(): Interactable? = interactables.firstOrNull {
        (it !is MoonGate || theme == ImaginationTheme.FANTASY) && RectF.intersects(player.collisionBounds, it.interactionBounds)
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
                theme == ImaginationTheme.REAL && !chapterOne.canBeginFantasy() -> "ACTION: CHECK FORT"
                theme == ImaginationTheme.REAL && !chapterOne.hasEnteredFantasy() -> "ACTION: ENTER WITH MIA"
                else -> "ACTION: SWITCH WORLD"
            }
            nearbyInteractable is TreasureChest -> "ACTION: OPEN"
            else -> "ACTION: INTERACT"
        }
        canvas.drawText(text, 240f, 240f, paint)
    }

    private fun drawDialogue(canvas: Canvas) {
        val text = dialogue ?: return
        paint.color = 0xEE171717.toInt(); canvas.drawRoundRect(28f, 184f, 452f, 256f, 8f, 8f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.LEFT; paint.textSize = 12f
        canvas.drawText(text.take(68), 44f, 216f, paint)
        if (text.length > 68) canvas.drawText(text.drop(68).take(68), 44f, 233f, paint)
        paint.textAlign = Paint.Align.RIGHT; paint.textSize = 9f; canvas.drawText("Tap ACTION to close", 436f, 248f, paint)
    }

    private fun drawWorldDebug(canvas: Canvas) {
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 1f; paint.color = 0x99FFFF00.toInt()
        landmarks.forEach { canvas.drawRect(it.collisionBounds, paint) }; canvas.drawRect(chest.bounds, paint)
        if (theme == ImaginationTheme.FANTASY && !moonGate.unlocked) canvas.drawRect(moonGate.bounds, paint)
        paint.color = 0x9900FFFF.toInt(); nearbyInteractable?.let { canvas.drawRect(it.interactionBounds, paint) }
        if (player.isAttacking) { paint.color = 0x99FF3333.toInt(); canvas.drawRect(player.attackBounds(), paint) }
        if (theme == ImaginationTheme.FANTASY) enemies.filter { it.active }.forEach { enemy ->
            paint.color = 0x99FF66FF.toInt(); canvas.drawRect(enemy.hurtBounds, paint)
            paint.color = 0x99FF9900.toInt(); canvas.drawRect(enemy.contactBounds, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDebugPanel(canvas: Canvas) {
        paint.color = 0xCC000000.toInt(); canvas.drawRect(6f, 6f, 320f, 80f, paint)
        paint.color = 0xFFFFFFFF.toInt(); paint.textAlign = Paint.Align.LEFT; paint.textSize = 10f
        canvas.drawText("Backyard Realms Ch.1 M2", 12f, 18f, paint)
        canvas.drawText("theme=$theme time=$worldTime", 12f, 30f, paint)
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
        canvas.drawText("ACTION theme  •  UP time  •  DOWN enemy", 240f, 70f, paint)
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
    }
}
