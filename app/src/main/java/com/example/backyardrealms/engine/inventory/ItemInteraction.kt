package com.example.backyardrealms.engine.inventory

data class ItemUseResult(
    val handled: Boolean,
    val message: String = "",
    val grantedItems: List<Pair<String, Int>> = emptyList(),
    val setFlags: List<String> = emptyList()
)

fun interface ItemInteractionResolver {
    fun use(itemId: String, targetId: String): ItemUseResult
}
