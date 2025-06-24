package com.example.foodflex.network

import com.example.foodflex.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class OAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        val consumerKey = BuildConfig.FATSECRET_CONSUMER_KEY
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = UUID.randomUUID().toString()

        val queryParams = url.queryParameterNames.mapNotNull { name ->
            url.queryParameter(name)?.let { value -> name to value }
        }.toMap()

        val allParams = mutableMapOf(
            "oauth_consumer_key" to consumerKey,
            "oauth_nonce" to nonce,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestamp,
            "oauth_version" to "1.0"
        )
        allParams.putAll(queryParams)

        val sortedParams = allParams.toSortedMap()

        val parameterString = sortedParams.map { (k, v) ->
            "${k.encode()}=${v.encode()}"
        }.joinToString("&")

        val baseUrl = url.newBuilder().query(null).build().toString()

        // DÜZELTME BURADA: FatSecret'in kendi dokümanına göre parameterString tekrar encode ediliyor.
        val signatureBaseString = "${originalRequest.method.uppercase()}&${baseUrl.encode()}&${parameterString.encode()}"

        val signingKey = "${BuildConfig.FATSECRET_CONSUMER_SECRET.encode()}&"
        val signature = signatureBaseString.hmacSha1(signingKey)

        val newUrl = url.newBuilder()
            .addQueryParameter("oauth_consumer_key", consumerKey)
            .addQueryParameter("oauth_nonce", nonce)
            .addQueryParameter("oauth_signature_method", "HMAC-SHA1")
            .addQueryParameter("oauth_timestamp", timestamp)
            .addQueryParameter("oauth_version", "1.0")
            .addQueryParameter("oauth_signature", signature)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

    private fun String.encode(): String = URLEncoder.encode(this, "UTF-8")

    private fun String.hmacSha1(key: String): String {
        val keySpec = SecretKeySpec(key.toByteArray(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)
        val signatureBytes = mac.doFinal(this.toByteArray())
        return android.util.Base64.encodeToString(signatureBytes, android.util.Base64.NO_WRAP)
    }
}