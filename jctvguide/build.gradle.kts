import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosSimulatorArm64()
    iosArm64()
    iosX64()
    jvm("desktop")

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(libs.androidx.core.ktx)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

    }


}


android {
    namespace = "dev.sajidali.jctvguide"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

//dependencies {
//
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.junit.ext)
//    androidTestImplementation(libs.androidx.espresso.core)
//    implementation(libs.joda.time)
//
//    implementation(platform(libs.compose.bom))
//    api(libs.compose.ui)
//    api(libs.compose.ui.graphics)
//    api(libs.compose.ui.tooling)
//    api(libs.material3)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//}