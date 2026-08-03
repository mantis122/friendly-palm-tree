package com.example.backyardrealms.engine.inventory

data class ItemDefinition(
    val id: String,
    val name: String,
    val description: String,
    val maxStack: Int,
    val equippable: Boolean,
    val usable: Boolean,
    val questItem: Boolean,
    val damage: Int,
    val iconColor: Int
)

data class InventorySlot(val itemId: String, var quantity: Int)
