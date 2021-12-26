package eu.sebaro.uller.data

import androidx.annotation.Keep
import eu.sebaro.uller.*
import kotlinx.serialization.Serializable

val ps5List = Product(
    name = "Playstation 5",
    category = "ps5",
    variant =
    listOf(
        ProductVariant(
            productId = 1,
            name = "PS5 Digital",
            shopList = listOf(
                Shop(
                    shopId = 1,
                    name = "Amazon",
                    key = "amazon",
                    url = "https://www.amazon.de/dp/B08H98GVK8/?coliid=IUNG1CGD0ZQVJ&colid=221GI0MGD81MO&psc=0",
                    available = false,


                    ),
                Shop(
                    shopId = 2,
                    name = "Alternate",
                    key = "alternate",
                    url = "https://www.alternate.de/Sony-Interactive-Entertainment/PlayStation-5-Digital-Edition-Spielkonsole/html/product/1651221",
                    available = false,
                ),
                Shop(
                    shopId = 3,
                    name = "Euronics",
                    key = "euronics",
                    url = "https://www.euronics.de/spiele-und-konsolen-film-und-musik/spiele-und-konsolen/playstation-5/spielekonsole/playstation-5-digital-edition-konsole-4061856837833",
                    available = false
                ),
                Shop(
                    shopId = 4,
                    name = "Saturn",
                    key = "saturn",
                    url = "https://www.saturn.de/de/product/_sony-playstation%C2%AE5-digital-edition-2661939.html",
                    available = false
                ),
                Shop(
                    shopId = 5,
                    name = "Mediamarkt",
                    key = "mediaMarkt",
                    url = "https://www.mediamarkt.de/de/product/_sony-playstation%C2%AE5-digital-edition-2661939.html",
                    available = false,
                )
            )
        )
    )
)

val xboxList = Product(
    name = "Xbox Series X",
    category = "xbox",
    variant =
    listOf(
        ProductVariant(
            productId = 2,
            name = "Xbox Series X",
            shopList = listOf(
                Shop(
                    shopId = 1,
                    name = "Amazon",
                    key = "amazon",
                    url = "https://www.amazon.de/dp/B08H98GVK8/?coliid=IUNG1CGD0ZQVJ&colid=221GI0MGD81MO&psc=0",
                    available = false,


                    )
            )
        )
    )
)

val switchList = Product(
    name = "Nintendo Switch",
    category = "nintendoswitch",
    variant =
    listOf(
        ProductVariant(
            productId = 4,
            name = "Nintendo Switch",
            shopList = listOf(
                Shop(
                    shopId = 1,
                    name = "Test",
                    key = "Test",
                    url = "https://www.amazon.de/dp/B08H98GVK8/?coliid=IUNG1CGD0ZQVJ&colid=221GI0MGD81MO&psc=0",
                    available = true,
                )
            )
        )
    )
)

@Keep
@Serializable
var productList = ScrapItems(
    Products(
        listOf(
            ps5List,
            xboxList,
            switchList,
        )
    )
)

@Keep
fun getMockProductList(): ScrapItems {
    return productList
}