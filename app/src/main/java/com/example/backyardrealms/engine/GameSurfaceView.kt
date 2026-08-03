package com.example.backyardrealms.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.backyardrealms.game.BackyardGame
import kotlin.math.min

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private val input = InputState()
    private val viewport = Viewport()
    private val controls = TouchControls(input)
    private val game = BackyardGame()

    @Volatile private var running = false
    @Volatile private var surfaceReady = false
    private var gameThread: Thread? = null

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        startThreadIfNeeded()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceReady = false
        stopThread()
    }

    fun resumeGame() {
        running = true
        startThreadIfNeeded()
    }

    fun pauseGame() {
        running = false
        stopThread()
        input.clear()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = controls.onTouch(event, viewport)

    override fun run() {
        var previous = System.nanoTime()
        var accumulator = 0.0

        while (running && surfaceReady) {
            val now = System.nanoTime()
            val frameSeconds = min(
                (now - previous) / 1_000_000_000.0,
                GameConfig.MAX_FRAME_SECONDS
            )
            previous = now
            accumulator += frameSeconds

            while (accumulator >= GameConfig.FIXED_STEP_SECONDS) {
                game.update(GameConfig.FIXED_STEP_SECONDS.toFloat(), input.snapshot())
                accumulator -= GameConfig.FIXED_STEP_SECONDS
            }

            drawFrame()
        }
    }

    private fun drawFrame() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas() ?: return
            canvas.drawColor(Color.BLACK)
            viewport.update(width, height)
            viewport.begin(canvas)
            game.draw(canvas)
            controls.draw(canvas)
            viewport.end(canvas)
        } finally {
            if (canvas != null) holder.unlockCanvasAndPost(canvas)
        }
    }

    @Synchronized
    private fun startThreadIfNeeded() {
        if (!surfaceReady || !running || gameThread?.isAlive == true) return
        gameThread = Thread(this, "BackyardGameLoop").also { it.start() }
    }

    @Synchronized
    private fun stopThread() {
        val thread = gameThread ?: return
        gameThread = null
        try {
            thread.join(500)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
