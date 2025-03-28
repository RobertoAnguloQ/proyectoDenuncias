plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.proyectodenuncias"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectodenuncias"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX dependencies
    implementation ("com.itextpdf:itext7-core:7.2.3")
    implementation ("com.itextpdf:kernel:7.2.3")
    implementation ("com.itextpdf:layout:7.2.3")
    implementation ("com.itextpdf:io:7.2.3")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.itextpdf:itextpdf:5.5.13.3") // Versión antigua
    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")

    // Android testing dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")

    // iText dependency for PDF creation
    implementation("com.itextpdf:itext7-core:7.1.16")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
}
