package com.example.backyardrealms.engine.entity

interface Behavior<T> {
    fun update(owner: T, dt: Float)
}
