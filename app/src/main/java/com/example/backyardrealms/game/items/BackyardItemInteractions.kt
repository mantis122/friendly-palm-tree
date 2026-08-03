package com.example.backyardrealms.game.items

import com.example.backyardrealms.engine.inventory.ItemInteractionResolver
import com.example.backyardrealms.engine.inventory.ItemUseResult

class BackyardItemInteractions : ItemInteractionResolver {
    override fun use(itemId: String, targetId: String): ItemUseResult = when (itemId to targetId) {
        "stick" to "garden" -> ItemUseResult(
            handled = true,
            message = "You use the stick to push aside the tall weeds. A berry was hiding underneath!",
            grantedItems = listOf("summer_berry" to 1),
            setFlags = listOf("CLEARED_GARDEN_WEEDS")
        )
        else -> ItemUseResult(false)
    }
}
