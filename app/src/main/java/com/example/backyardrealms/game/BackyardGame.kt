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
import com.example.backyardrealms.engine.graphics.SpriteSheet
import com.example.backyardrealms.engine.world.Interactable
import com.example.backyardrealms.game.world.Landmark

class BackyardGame(context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val worldBounds = RectF(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    private val camera = Camera2D(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT)
    private val playerSheet = SpriteSheet(
        BitmapFactory.decodeResource(context.resources, R.drawable.player_sheet),
        24,
        32
    )
    private val player = Player(460f, 390f, playerSheet)
    private val friend = FriendNpc(470f, 250f)

    private val landmarks = listOf(
        Landmark(RectF(410f, 92f, 550f, 198f), 0xFF80552E.toInt(), "FORT", "Our fort. This is where every adventure begins."),
        Landmark(RectF(154f, 128f, 218f, 204f), 0xFF486B2A.toInt(), "TREE", "The oldest tree in the yard. It looks almost magical."),
        Landmark(RectF(726f, 116f, 842f, 202f), 0xFF6B645C.toInt(), "SHED", "Dad says not to lose anything behind the shed."),
        Landmark(RectF(650f, 350f, 820f, 438f), 0xFF526C30.toInt(), "GARDEN", "Tomatoes, beans, and several unexplored jungles."),
        Landmark(RectF(190f, 356f, 300f, 442f), 0xFFCDAE6B.toInt(), "SANDBOX", "A desert kingdom waiting for a story."),
        Landmark(RectF(370f, 486f, 590f, 530f), 0xFF96714E.toInt(), "PORCH", "The back door is locked during the prototype.")
    )
    private val obstacles = landmarks.map { it.collisionBounds }
    private val interactables: List<Interactable> = landmarks + friend

    private var dialogue: String? = null
    private var nearbyInteractable: Interactable? = null
    private var showDebug = true

    fun update(dt: Float, input: InputSnapshot) {
        nearbyInteractable = findNearestInteractable()

        if (input.actionPressed && dialogue != null) {
            dialogue = null
        } else if (input.actionPressed && nearbyInteractable != null) {
            dialogue = nearbyInteractable?.interactionText()
        } else {
            player.update(dt, input, obstacles, worldBounds)
        }

        camera.follow(player.centerX, player.centerY, worldBounds, dt)
    }

    fun draw(canvas: Canvas) {
        camera.begin(canvas)
        drawGround(canvas)
        for (landmark in landmarks) landmark.draw(canvas)
        friend.draw(canvas)
        player.draw(canvas)
        if (showDebug) drawWorldDebug(canvas)
        camera.end(canvas)

        drawPrompt(canvas)
        drawDialogue(canvas)
        if (showDebug) drawDebugPanel(canvas)
    }

    private fun findNearestInteractable(): Interactable? {
        val playerBounds = player.collisionBounds
        return interactables.firstOrNull { RectF.intersects(playerBounds, it.interactionBounds) }
    }

    private fun drawGround(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFF79A84D.toInt()
        canvas.drawRect(worldBounds, paint)

        paint.color = 0xFF4C7435.toInt()
        canvas.drawRect(0f, 0f, WORLD_WIDTH, 20f, paint)
        canvas.drawRect(0f, WORLD_HEIGHT - 20f, WORLD_WIDTH, WORLD_HEIGHT, paint)
        canvas.drawRect(0f, 0f, 20f, WORLD_HEIGHT, paint)
        canvas.drawRect(WORLD_WIDTH - 20f, 0f, WORLD_WIDTH, WORLD_HEIGHT, paint)

        paint.color = 0xFFC6A16B.toInt()
        canvas.drawRect(450f, 198f, 510f, 486f, paint)
        canvas.drawRect(300f, 284f, 680f, 326f, paint)

        paint.color = 0xFF6F9A48.toInt()
        for (x in 40..920 step 64) {
            for (y in 44..500 step 58) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), 2f, paint)
            }
        }
    }

    private fun drawPrompt(canvas: Canvas) {
        if (dialogue != null || nearbyInteractable == null) return
        paint.color = 0xCC000000.toInt()
        canvas.drawRoundRect(180f, 222f, 300f, 250f, 6f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f
        canvas.drawText("ACTION: INTERACT", 240f, 240f, paint)
    }

    private fun drawDialogue(canvas: Canvas) {
        val text = dialogue ?: return
        paint.color = 0xEE171717.toInt()
        canvas.drawRoundRect(36f, 188f, 444f, 254f, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFE7D7A8.toInt()
        canvas.drawRoundRect(36f, 188f, 444f, 254f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 13f
        canvas.drawText(text, 52f, 218f, paint)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Tap ACTION to close", 428f, 242f, paint)
    }

    private fun drawWorldDebug(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = 0x99FFFF00.toInt()
        for (landmark in landmarks) canvas.drawRect(landmark.collisionBounds, paint)
        paint.color = 0x9900FFFF.toInt()
        nearbyInteractable?.let { canvas.drawRect(it.interactionBounds, paint) }
        if (player.isAttacking) {
            paint.color = 0x99FF3333.toInt()
            canvas.drawRect(player.attackBounds(), paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDebugPanel(canvas: Canvas) {
        paint.color = 0xCC000000.toInt()
        canvas.drawRect(6f, 6f, 212f, 46f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        canvas.drawText("Backyard Engine 0.2", 12f, 18f, paint)
        canvas.drawText(player.positionText(), 12f, 30f, paint)
        canvas.drawText("camera=${camera.x.toInt()},${camera.y.toInt()}", 12f, 42f, paint)
    }

    companion object {
        private const val WORLD_WIDTH = 960f
        private const val WORLD_HEIGHT = 540f
    }
}
