plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "kkkzheli.antirecall.wechat"
    compileSdk = 34

    defaultConfig {
        applicationId = "kkkzheli.antirecall.wechat"
        minSdk = 23
        targetSdk = 34
        versionCode = 10
        versionName = "1.5.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("signing-key.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "kkkzheli123"
                keyAlias = "antirecall"
                keyPassword = "kkkzheli123"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons.extended)

    // Activity & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.activity:activity-compose:1.9.3")

    // Room for persistent storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.ksp)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // AppCompat
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

// Verify trilingual string parity before every build. Skips with a warning
// when no python interpreter is available; fails the build on any mismatch.
val verifyTranslations by tasks.registering {
    group = "verification"
    description = "Ensure zh/en/zh-rTW string resources stay in sync and all UI text is localized"
    doLast {
        val script = rootProject.file("tools/check_translations.py")
        if (!script.exists()) {
            throw GradleException("Missing tools/check_translations.py — run: git pull && see repo tools/")
        }
        var ran = false
        for (py in listOf("python", "python3")) {
            try {
                val proc = ProcessBuilder(py, script.absolutePath).redirectErrorStream(true).start()
                println(proc.inputStream.bufferedReader().readText().trim())
                if (proc.waitFor() != 0) {
                    throw GradleException("Translation check failed — fix string resources before building.")
                }
                ran = true
                break
            } catch (e: Exception) {
                if (e is GradleException) throw e
                // interpreter not found — try the next candidate
            }
        }
        if (!ran) {
            println("WARN: no python interpreter found; skipping translation check")
        }
    }
}

tasks.named("preBuild").configure { dependsOn(verifyTranslations) }
