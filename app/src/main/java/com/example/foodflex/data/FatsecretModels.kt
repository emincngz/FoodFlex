package com.example.foodflex.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
// JsonObject importu kullanılmıyorsa kaldırılabilir, ancak genellikle deserializer içinde lazım olur.
// import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// foods.search metodunun ana yanıtı
data class FoodsSearchResponse(
    @SerializedName("foods") val foods: FoodsContainer
)

// "foods" nesnesinin içini temsil eden ara sınıf
data class FoodsContainer(
    @SerializedName("food") @JsonAdapter(FoodListDeserializer::class) val foodList: List<Food>
)

// Tek bir yiyecek öğesi
data class Food(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("food_description") val foodDescription: String
)

// food.get metodunun ana yanıtı
data class FoodGetResponse(
    @SerializedName("food") val food: FoodDetails
)

data class FoodDetails(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("servings") @JsonAdapter(ServingListDeserializer::class) val servings: List<Serving> // Tipini List<Serving> yaptık
)

// Bu 'Servings' ara sınıfı artık gereksizleşti çünkü FoodDetails.servings doğrudan List<Serving> oldu.
// Eğer bu sınıfı başka bir yerde kullanmıyorsak silebiliriz.
// Şimdilik bırakıyorum ama @JsonAdapter'ını kaldırabiliriz ya da sınıfı tamamen silebiliriz.
// Kullanılmıyorsa kafa karışıklığı yaratmasın diye siliyorum.
// data class Servings(
//    @SerializedName("serving") @JsonAdapter(ServingListDeserializer::class.java) val servingList: List<Serving>
// )

data class Serving(
    @SerializedName("serving_id") val servingId: String,
    @SerializedName("serving_description") val servingDescription: String?, // Nullable yapıldı
    @SerializedName("metric_serving_amount") val metricServingAmount: String?, // Zaten nullable idi
    @SerializedName("metric_serving_unit") val metricServingUnit: String?,   // Zaten nullable idi
    @SerializedName("calories") val calories: String?,                       // Nullable yapıldı
    @SerializedName("carbohydrate") val carbohydrate: String?,               // Nullable yapıldı
    @SerializedName("protein") val protein: String?,                       // Nullable yapıldı
    @SerializedName("fat") val fat: String?                                 // Nullable yapıldı
)

// FatSecret'in Hata Yanıt Modeli
data class FatsecretErrorResponse(
    @SerializedName("error") val error: FatsecretError
)

data class FatsecretError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)


// "food" alanı için özel Deserializer
class FoodListDeserializer : JsonDeserializer<List<Food>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Food> {
        val foodList = mutableListOf<Food>()
        // json ve context artık null olamayacağı için null check'e gerek yok.
        if (json.isJsonArray) {
            json.asJsonArray.forEach { jsonElement ->
                foodList.add(context.deserialize(jsonElement, Food::class.java))
            }
        } else if (json.isJsonObject) {
            foodList.add(context.deserialize(json, Food::class.java))
        }
        return foodList
    }
}

// "serving" alanı için özel Deserializer
class ServingListDeserializer : JsonDeserializer<List<Serving>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Serving> {
        val servingList = mutableListOf<Serving>()

        // json elementi aslında "servings" JSON nesnesidir.
        // Bu nesnenin içinden "serving" anahtarının değerini almalıyız.
        if (json.isJsonObject) {
            val servingsObject = json.asJsonObject
            if (servingsObject.has("serving")) {
                val servingJsonElement = servingsObject.get("serving") // "serving" anahtarının değerini al

                if (servingJsonElement.isJsonArray) {
                    // Eğer "serving" bir dizi ise, her bir elemanı Serving olarak parse et
                    servingJsonElement.asJsonArray.forEach { element ->
                        servingList.add(context.deserialize(element, Serving::class.java))
                    }
                } else if (servingJsonElement.isJsonObject) {
                    // Eğer "serving" tek bir nesne ise, onu Serving olarak parse et ve listeye ekle
                    servingList.add(context.deserialize(servingJsonElement, Serving::class.java))
                }
            }
        }
        // Eğer "servings" nesnesi içinde "serving" alanı yoksa veya formatı uygun değilse boş liste döner.
        return servingList
    }
}