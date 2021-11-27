package eu.sebaro.uller

class GeneralDataModels {

    @Serializable
    data class ProductListDto(
        @SerialName("id") val id: List<ProductDto>
    )

    @Serializable
    data class ProductDto(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("description") val description: String,
    )



}