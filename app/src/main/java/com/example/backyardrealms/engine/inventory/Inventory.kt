package com.example.backyardrealms.engine.inventory

class Inventory(private val catalog: ItemCatalog, private val capacity: Int = 12) {
    private val slots = mutableListOf<InventorySlot>()
    var equippedItemId: String? = null
        private set

    fun allSlots(): List<InventorySlot> = slots.map { it.copy() }
    fun quantity(itemId: String): Int = slots.firstOrNull { it.itemId == itemId }?.quantity ?: 0
    fun contains(itemId: String): Boolean = quantity(itemId) > 0

    fun add(itemId: String, amount: Int = 1): Int {
        val definition = catalog[itemId] ?: return amount
        var remaining = amount.coerceAtLeast(0)
        slots.filter { it.itemId == itemId && it.quantity < definition.maxStack }.forEach { slot ->
            if (remaining <= 0) return@forEach
            val accepted = minOf(remaining, definition.maxStack - slot.quantity)
            slot.quantity += accepted
            remaining -= accepted
        }
        while (remaining > 0 && slots.size < capacity) {
            val accepted = minOf(remaining, definition.maxStack)
            slots += InventorySlot(itemId, accepted)
            remaining -= accepted
        }
        return remaining
    }

    fun remove(itemId: String, amount: Int = 1): Boolean {
        if (quantity(itemId) < amount) return false
        var remaining = amount
        val iterator = slots.listIterator(slots.size)
        while (iterator.hasPrevious() && remaining > 0) {
            val slot = iterator.previous()
            if (slot.itemId != itemId) continue
            val removed = minOf(remaining, slot.quantity)
            slot.quantity -= removed
            remaining -= removed
            if (slot.quantity <= 0) iterator.remove()
        }
        if (!contains(itemId) && equippedItemId == itemId) equippedItemId = null
        return true
    }

    fun equip(itemId: String?): Boolean {
        if (itemId == null) { equippedItemId = null; return true }
        val definition = catalog[itemId] ?: return false
        if (!definition.equippable || !contains(itemId)) return false
        equippedItemId = itemId
        return true
    }

    fun restore(serialized: String, equipped: String?) {
        slots.clear()
        serialized.split(';').filter { it.isNotBlank() }.forEach { token ->
            val parts = token.split(':')
            val id = parts.getOrNull(0) ?: return@forEach
            val quantity = parts.getOrNull(1)?.toIntOrNull() ?: return@forEach
            add(id, quantity)
        }
        equip(equipped)
    }

    fun serialize(): String = slots.joinToString(";") { "${it.itemId}:${it.quantity}" }
}
