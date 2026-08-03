package com.example.backyardrealms.engine.inventory

import android.content.Context
import android.graphics.Color
import org.json.JSONArray

class ItemCatalog(context: Context) {
    private val definitions: LinkedHashMap<String, ItemDefinition> = linkedMapOf()

    init {
        val json = context.assets.open("items.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val definition = ItemDefinition(
                id = item.getString("id"),
                name = item.getString("name"),
                description = item.getString("description"),
                maxStack = item.optInt("maxStack", 1).coerceAtLeast(1),
                equippable = item.optBoolean("equippable", false),
                usable = item.optBoolean("usable", false),
                questItem = item.optBoolean("questItem", false),
                damage = item.optInt("damage", 0),
                iconColor = Color.parseColor(item.optString("iconColor", "#FFFFFF"))
            )
            definitions[definition.id] = definition
        }
    }

    operator fun get(id: String): ItemDefinition? = definitions[id]
    fun all(): List<ItemDefinition> = definitions.values.toList()
}
