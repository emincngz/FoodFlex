import java.util.Properties

// local.properties dosyasını okumak için
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.foodflex" // Paket adınız
    compileSdk = 35 // DEĞİŞTİRİLDİ

    defaultConfig {
        applicationId = "com.example.foodflex"
        minSdk = 24
        targetSdk = 35 // DEĞİŞTİRİLDİ
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties'den API anahtarını alıp buildConfigField'e ekle
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            properties.getProperty("GEMINI_API_KEY", "")
        )
        // FatSecret API anahtarları
        buildConfigField(
            "String",
            "FATSECRET_CONSUMER_KEY",
            properties.getProperty("FATSECRET_CONSUMER_KEY", "")
        )
        buildConfigField(
            "String",
            "FATSECRET_CONSUMER_SECRET",
            properties.getProperty("FATSECRET_CONSUMER_SECRET", "")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        // buildConfig'i etkinleştiriyoruz (genellikle zaten etkindir)
        buildConfig = true
    }
}

// build.gradle.kts içindeki dependencies bloğu

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.activity)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Google Gemini API için
    implementation("com.google.ai.client.generativeai:generativeai:0.8.0")

    // Coroutine'ler (Asenkron işlemler için)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // lifecycleScope için
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3") // ViewModel'ın kendisi ve Kotlin eklentileri için
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")  // LiveData ve Kotlin eklentileri için
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.3") // Java 8 dil özellikleriyle uyumluluk ve bazı ek faydalar için (iyi bir pratiktir)

    // Retrofit (Ağ istekleri için)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Room Veritabanı Kütüphaneleri - YENİ EKLENEN BLOK
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Coroutine desteği için
    ksp("androidx.room:room-compiler:$roomVersion")     // Annotation processor KSP ile
    // implementation("androidx.room:room-common:$roomVersion") // Gerekirse eklenebilir

    val nav_version = "2.7.7" // Güncel stabil versiyon
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
}