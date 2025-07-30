package ecommerce.model

import java.time.LocalDateTime

data class Cart(
    val id: Long? = null, //TODO: 0
    val memberId: Long, //TODO: consider `UUID`
    val productId: Long,
    val quantity: Int,
    val addedAt: LocalDateTime? = null,
)
