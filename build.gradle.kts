plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.echo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.echo"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"djk93al88\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"482883311769531\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"3wnDOT1sGqBlA1jrptGkIymooHQ\"")
            buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"SWFM_unsigned\"")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"djk93al88\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"482883311769531\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"3wnDOT1sGqBlA1jrptGkIymooHQ\"")
            buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"SWFM_unsigned\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    aaptOptions {
        noCompress("tflite")
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    // Firebase BOM to manage compatible versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    // TensorFlow Lite for model inference
    implementation("org.tensorflow:tensorflow-lite:2.17.0")

    // Core AndroidX dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.9.3")
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.volley)
    implementation(libs.preference)
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.work.runtime)
    implementation(libs.play.services.location)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.cast.tv)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Additional libraries
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-places:17.0.0")

    // ImagePicker and Cloudinary
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("com.cloudinary:cloudinary-android:2.5.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // JSON
    implementation("org.json:json:20231013")

    // Guava for ListenableFuture
    implementation("com.google.guava:guava:33.2.0-android")

    // Latest stable Google API Client Libraries (compatible with Android)
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.api-client:google-api-client-jackson2:2.2.0")

    // Latest stable YouTube API Services - September 2024 release
    implementation("com.google.apis:google-api-services-youtube:v3-rev20240916-2.0.0")

    // HTTP transport and JSON factory dependencies (latest stable)
    implementation("com.google.http-client:google-http-client-android:1.44.2")
    implementation("com.google.http-client:google-http-client-jackson2:1.44.2")

    // OAuth2 for authentication (if needed)
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }

    // Jackson for JSON processing (required by YouTube API)
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")

    // Modern video playback alternatives
    // ExoPlayer for advanced video playback (Google's recommended solution)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")

    // Picasso for image loading
    implementation("com.squareup.picasso:picasso:2.71828")
}