package com.example.backyardrealms.engine.entity

import android.graphics.Canvas

interface Entity {
    val id: String
    var active: Boolean
    fun update(dt: Float) {}
    fun draw(canvas: Canvas)
}

abstract class CharacterEntity : Entity
abstract class WorldObjectEntity : Entity
abstract class EffectEntity : Entity
abstract class ProjectileEntity : Entity
