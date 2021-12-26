package eu.sebaro.uller

import kotlinx.serialization.Serializable

@Serializable
data class ScrapItems(
    val products: Products
)

@Serializable
data class Products(
    val products: List<Product>
)

@Serializable
data class Product(
    val name: String,
    val category: String,
    val variant: List<ProductVariant>
)

@Serializable
data class StatusDTO(
    val status: String
)

@Serializable
data class ProductVariant(
    val productId: Int,
    val name: String,
    val shopList: List<Shop>
)

@Serializable
data class Shop(
    val shopId: Int,
    val name: String,
    val key: String,
    val url: String,
    val available: Boolean,
)