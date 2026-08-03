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
import com.example.backyardrealms.engine.save.GameSave
import com.example.backyardrealms.engine.save.SaveManager
import com.example.backyardrealms.engine.time.WorldTime
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.ambient.AmbientParticle
import com.example.backyardrealms.game.enemy.YardBlob
import com.example.backyardrealms.game.theme.ImaginationTheme
import com.example.backyardrealms.game.theme.ThemeTransition
import com.example.backyardrealms.game.world.Landmark
import com.example.backyardrealms.game.world.LandmarkAppearance
import kotlin.math.sqrt

class BackyardGame(context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val worldBounds = RectF(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    private val camera = Camera2D(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT)
    private val playerSheet = SpriteSheet(BitmapFactory.decodeResource(context.resources, R.drawable.player_sheet), 24, 32)
    private val saveManager = SaveManager(context)
    private val saved = saveManager.load()
    private val player = Player(saved.playerX, saved.playerY, playerSheet)
    private val friend = FriendNpc(470f, 250f)
    private val enemy = YardBlob("moon_blob", 610f, 285f)
    private val eventBus = EventBus()
    private val audio = GameAudio()
    private var theme = saved.theme
    private var worldTime = saved.worldTime
    private val transition = ThemeTransition()
    private var developerOpen = false
    private var saveTimer = 0f
    private var timeInputLatched = false
    private var respawnInputLatched = false

    private fun a(color: Int, label: String, message: String) = LandmarkAppearance(color, label, message)
    private val landmarks = listOf(
        Landmark("fort", RectF(410f, 92f, 550f, 198f), a(0xFF80552E.toInt(), "FORT", "Step inside and begin an adventure."), a(0xFF6350A8.toInt(), "MOONKEEP", "The Moonkeep hums with imagined magic.")),
        Landmark("tree", RectF(154f, 128f, 218f, 204f), a(0xFF486B2A.toInt(), "TREE", "The oldest tree in the yard."), a(0xFF2E7D50.toInt(), "WORLD TREE", "Its branches hold a thousand kingdoms.")),
        Landmark("shed", RectF(726f, 116f, 842f, 202f), a(0xFF6B645C.toInt(), "SHED", "Dad says not to lose anything behind it."), a(0xFF7A3E38.toInt(), "GOBLIN FORT", "Moon Goblins guard the stolen crown.")),
        Landmark("garden", RectF(650f, 350f, 820f, 438f), a(0xFF526C30.toInt(), "GARDEN", "Tomatoes, beans, and weeds."), a(0xFF285D3A.toInt(), "WHISPERWOOD", "Every leaf sounds like a secret.")),
        Landmark("sandbox", RectF(190f, 356f, 300f, 442f), a(0xFFCDAE6B.toInt(), "SANDBOX", "A desert kingdom waiting for a story."), a(0xFFE0B858.toInt(), "SUNWASTE", "Ancient treasure sleeps beneath the dunes.")),
        Landmark("porch", RectF(370f, 486f, 590f, 530f), a(0xFF96714E.toInt(), "PORCH", "The back door is locked."), a(0xFF74523F.toInt(), "STONE BRIDGE", "Beyond lies the forbidden indoor realm."))
    )
    private val particles = List(18) { i -> AmbientParticle("pollen_$i", WORLD_WIDTH, WORLD_HEIGHT, (i * 61f) % WORLD_WIDTH, 40f + (i * 83f) % (WORLD_HEIGHT - 80f), 4f + (i % 4), i * .7f) }
    private val obstacles = landmarks.map { it.collisionBounds }
    private val interactables: List<Interactable> = landmarks + friend
    private var dialogue: String? = null
    private var nearbyInteractable: Interactable? = null
    private var showDebug = true

    init {
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
        friend.update(dt)
        particles.forEach { it.update(dt) }
        saveTimer += dt
        if (saveTimer >= 5f) { saveGame(); saveTimer = 0f }
        if (input.developerPressed) developerOpen = !developerOpen

        if (developerOpen) {
            if (input.actionPressed) startThemeSwitch()
            if (input.moveY < -0.7f && !timeInputLatched) {
                worldTime = worldTime.next()
                saveGame()
                timeInputLatched = true
            }
            if (input.moveY > -0.3f) timeInputLatched = false
            if (input.moveY > 0.7f && !respawnInputLatched) {
                enemy.forceRespawn()
                respawnInputLatched = true
            }
            if (input.moveY < 0.3f) respawnInputLatched = false
            camera.follow(player.centerX, player.centerY, worldBounds, dt)
            return
        }
        if (transition.isActive) return

        nearbyInteractable = findNearestInteractable()
        when {
            input.actionPressed && dialogue != null -> dialogue = null
            input.actionPressed && nearbyInteractable is Landmark && (nearbyInteractable as Landmark).id == "fort" -> startThemeSwitch()
            input.actionPressed && nearbyInteractable != null -> {
                val id = when (val target = nearbyInteractable) {
                    is Landmark -> target.id
                    is FriendNpc -> target.id
                    else -> "unknown"
                }
                eventBus.post(GameEvent.InteractionStarted(id))
                dialogue = nearbyInteractable?.interactionText()
            }
            else -> updateGameplay(dt, input)
        }
        camera.follow(player.centerX, player.centerY, worldBounds, dt)
    }

    private fun updateGameplay(dt: Float, input: InputSnapshot) {
        val oldX = player.x
        val oldY = player.y
        val wasAttacking = player.isAttacking
        player.update(dt, input, obstacles, worldBounds)
        if (player.x != oldX || player.y != oldY) eventBus.post(GameEvent.PlayerMoved(player.x, player.y))
        if (!wasAttacking && player.isAttacking) eventBus.post(GameEvent.AttackStarted)

        if (theme != ImaginationTheme.FANTASY) return
        enemy.update(dt, player.centerX, player.centerY, obstacles, worldBounds)

        if (player.isAttacking && enemy.active && RectF.intersects(player.attackBounds(), enemy.hurtBounds)) {
            val knockback = player.attackKnockback()
            val wasAlive = enemy.health.isAlive
            if (enemy.receiveHit(DamageHit("player", 1, knockback.first, knockback.second, player.currentAttackId))) {
                eventBus.post(GameEvent.DamageDealt("player", enemy.id, 1))
                if (wasAlive && !enemy.health.isAlive) eventBus.post(GameEvent.EntityDefeated(enemy.id))
            }
        }

        if (enemy.active && RectF.intersects(player.hurtBounds, enemy.contactBounds)) {
            val dx = player.centerX - enemy.centerX
            val dy = player.centerY - enemy.centerY
            val length = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            if (player.receiveHit(DamageHit(enemy.id, 1, dx / length * 145f, dy / length * 145f, 0))) {
                eventBus.post(GameEvent.DamageDealt(enemy.id, "player", 1))
                eventBus.post(GameEvent.PlayerHealthChanged(player.health.current, player.health.maximum))
                if (!player.health.isAlive) respawnPlayer()
            }
        }
    }

    private fun respawnPlayer() {
        player.respawn(470f, 214f)
        eventBus.post(GameEvent.PlayerRespawned)
        eventBus.post(GameEvent.PlayerHealthChanged(player.health.current, player.health.maximum))
        dialogue = "You tumble back to Moonkeep and catch your breath."
        saveGame()
    }

    private fun saveGame() = saveManager.save(GameSave(player.x, player.y, theme, worldTime))

    private fun startThemeSwitch() {
        if (transition.isActive) return
        dialogue = null
        transition.start {
            theme = theme.toggled()
            landmarks.forEach { it.theme = theme }
            friend.theme = theme
            if (theme == ImaginationTheme.FANTASY) enemy.forceRespawn()
            eventBus.post(GameEvent.ThemeChanged(theme.name))
            saveGame()
        }
    }

    fun draw(canvas: Canvas) {
        camera.begin(canvas)
        drawGround(canvas)
        particles.forEach { it.draw(canvas) }
        landmarks.forEach { it.draw(canvas) }
        friend.draw(canvas)
        if (theme == ImaginationTheme.FANTASY) enemy.draw(canvas)
        player.draw(canvas)
        if (showDebug) drawWorldDebug(canvas)
        camera.end(canvas)
        drawTimeOverlay(canvas)
        drawHealthHud(canvas)
        drawPrompt(canvas)
        drawDialogue(canvas)
        if (showDebug) drawDebugPanel(canvas)
        if (developerOpen) drawDeveloperPanel(canvas)
        if (transition.alpha > 0f) {
            paint.color = ((transition.alpha * 255).toInt().coerceIn(0, 255) shl 24)
            canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)
        }
    }

    private fun findNearestInteractable(): Interactable? = interactables.firstOrNull { RectF.intersects(player.collisionBounds, it.interactionBounds) }

    private fun drawGround(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF79A84D.toInt() else 0xFF3C7650.toInt()
        canvas.drawRect(worldBounds, paint)
        paint.color = if (theme == ImaginationTheme.REAL) 0xFF4C7435.toInt() else 0xFF263B57.toInt()
        canvas.drawRect(0f, 0f, WORLD_WIDTH, 20f, paint)
        canvas.drawRect(0f, WORLD_HEIGHT - 20f, WORLD_WIDTH, WORLD_HEIGHT, paint)
        canvas.drawRect(0f, 0f, 20f, WORLD_HEIGHT, paint)
        canvas.drawRect(WORLD_WIDTH - 20f, 0f, WORLD_WIDTH, WORLD_HEIGHT, paint)
        paint.color = if (theme == ImaginationTheme.REAL) 0xFFC6A16B.toInt() else 0xFF8C79A8.toInt()
        canvas.drawRect(450f, 198f, 510f, 486f, paint)
        canvas.drawRect(300f, 284f, 680f, 326f, paint)
    }

    private fun drawTimeOverlay(canvas: Canvas) {
        if (worldTime.overlayColor != 0) {
            paint.color = worldTime.overlayColor
            canvas.drawRect(0f, 0f, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, paint)
        }
    }

    private fun drawHealthHud(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        for (i in 0 until player.health.maximum) {
            paint.color = if (i < player.health.current) 0xFFE84A5F.toInt() else 0xFF542B35.toInt()
            val left = 12f + i * 17f
            canvas.drawCircle(left + 5f, 93f, 5f, paint)
            canvas.drawCircle(left + 10f, 93f, 5f, paint)
            val path = android.graphics.Path()
            path.moveTo(left, 95f)
            path.lineTo(left + 15f, 95f)
            path.lineTo(left + 7.5f, 104f)
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    private fun drawPrompt(canvas: Canvas) {
        if (dialogue != null || nearbyInteractable == null || developerOpen) return
        paint.color = 0xCC000000.toInt()
        canvas.drawRoundRect(170f, 222f, 310f, 250f, 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f
        canvas.drawText(if (nearbyInteractable is Landmark && (nearbyInteractable as Landmark).id == "fort") "ACTION: SWITCH WORLD" else "ACTION: INTERACT", 240f, 240f, paint)
    }

    private fun drawDialogue(canvas: Canvas) {
        val text = dialogue ?: return
        paint.color = 0xEE171717.toInt()
        canvas.drawRoundRect(36f, 188f, 444f, 254f, 8f, 8f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 13f
        canvas.drawText(text, 52f, 218f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 9f
        canvas.drawText("Tap ACTION to close", 428f, 242f, paint)
    }

    private fun drawWorldDebug(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = 0x99FFFF00.toInt()
        landmarks.forEach { canvas.drawRect(it.collisionBounds, paint) }
        paint.color = 0x9900FFFF.toInt()
        nearbyInteractable?.let { canvas.drawRect(it.interactionBounds, paint) }
        if (player.isAttacking) {
            paint.color = 0x99FF3333.toInt()
            canvas.drawRect(player.attackBounds(), paint)
        }
        if (theme == ImaginationTheme.FANTASY && enemy.active) {
            paint.color = 0x99FF66FF.toInt()
            canvas.drawRect(enemy.hurtBounds, paint)
            paint.color = 0x99FF9900.toInt()
            canvas.drawRect(enemy.contactBounds, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDebugPanel(canvas: Canvas) {
        paint.color = 0xCC000000.toInt()
        canvas.drawRect(6f, 6f, 300f, 78f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        canvas.drawText("Backyard Engine 0.5", 12f, 18f, paint)
        canvas.drawText("theme=$theme time=$worldTime", 12f, 30f, paint)
        canvas.drawText("${player.positionText()} hp=${player.health.current}/${player.health.maximum}", 12f, 42f, paint)
        canvas.drawText("enemy=${enemy.state} hp=${enemy.health.current}/${enemy.health.maximum}", 12f, 54f, paint)
        canvas.drawText("audio=${audio.lastCue}", 12f, 66f, paint)
        canvas.drawText("event=${eventBus.lastEvent?.javaClass?.simpleName ?: "none"}", 150f, 66f, paint)
    }

    private fun drawDeveloperPanel(canvas: Canvas) {
        paint.color = 0xEE111111.toInt()
        canvas.drawRoundRect(90f, 42f, 390f, 228f, 10f, 10f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 17f
        canvas.drawText("DEVELOPER MENU", 240f, 70f, paint)
        paint.textSize = 12f
        canvas.drawText("Theme: $theme", 240f, 98f, paint)
        canvas.drawText("Time: $worldTime", 240f, 118f, paint)
        canvas.drawText("Player HP: ${player.health.current}/${player.health.maximum}", 240f, 138f, paint)
        canvas.drawText("ACTION: switch theme", 240f, 164f, paint)
        canvas.drawText("Joystick UP: next time", 240f, 184f, paint)
        canvas.drawText("Joystick DOWN: respawn enemy", 240f, 204f, paint)
        canvas.drawText("DEV: close menu", 240f, 220f, paint)
    }

    companion object {
        private const val WORLD_WIDTH = 960f
        private const val WORLD_HEIGHT = 540f
    }
}
