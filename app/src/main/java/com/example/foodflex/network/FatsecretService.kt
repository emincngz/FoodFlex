package com.example.foodflex.network

import com.example.foodflex.data.FoodGetResponse
import com.example.foodflex.data.FoodsSearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit için API arayüzü
interface FatsecretApi {
    @GET("rest/server.api?method=foods.search&format=json")
    suspend fun searchFoods(@Query("search_expression") query: String): FoodsSearchResponse

    @GET("rest/server.api?method=food.get&format=json")
    suspend fun getFoodDetails(@Query("food_id") foodId: String): FoodGetResponse
}

// Retrofit nesnesini oluşturan ve API'ye erişimi sağlayan singleton nesne
object FatsecretService {

    private const val BASE_URL = "https://platform.fatsecret.com/"

    // Interceptor'ları (loglama ve kendi yazdığımız OAuth) içeren bir OkHttpClient oluşturuyoruz
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(OAuthInterceptor()) // Kendi imzalama interceptor'ımızı ekliyoruz
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Ağ isteklerinin detaylarını logcat'te görmek için
        })
        .build()

    // Retrofit nesnesini oluşturuyoruz
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Özel OkHttpClient'ımızı kullanıyoruz
        .addConverterFactory(GsonConverterFactory.create()) // JSON'ı Kotlin nesnelerine çevirmek için
        .build()

    // Arayüzümüzü Retrofit ile hayata geçiriyoruz
    private val api: FatsecretApi = retrofit.create(FatsecretApi::class.java)

    // MainActivity'den çağıracağımız fonksiyonlar
    suspend fun searchFoods(query: String): FoodsSearchResponse {
        return api.searchFoods(query)
    }

    suspend fun getFoodDetails(foodId: String): FoodGetResponse {
        return api.getFoodDetails(foodId)
    }
}