import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "dev.sajidali"
version = "0.0.1"

kotlin {
    androidLibrary {
        namespace = "dev.sajidali.tvguide"
        compileSdk = 36
        minSdk = 23

        withJava()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
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
//                implementation(libs.androidx.core.ktx)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

    }


}