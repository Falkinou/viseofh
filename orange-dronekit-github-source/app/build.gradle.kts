plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

import org.gradle.api.tasks.Copy

fun String.toBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

fun buildSecret(name: String): String =
    providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orNull
        .orEmpty()

android {
    namespace = "com.djisyncflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.orangesynclog"
        minSdk = 26
        targetSdk = 35
        versionCode = 36
        versionName = "1.10.14"
        manifestPlaceholders["DJI_API_KEY"] = "6c11d5390328c7937d8fd73a"
        buildConfigField(
            "String",
            "EMBEDDED_SMTP_PASSWORD",
            buildSecret("ORANGE_DRONEKIT_SMTP_PASSWORD").toBuildConfigString(),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += setOf(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
            )
            keepDebugSymbols += setOf(
                "**/libconstants.so",
                "**/libdji_innertools.so",
                "**/libdjibase.so",
                "**/libDJICSDKCommon.so",
                "**/libDJIFlySafeCore-CSDK.so",
                "**/libdjifs_jni-CSDK.so",
                "**/libDJIRegister.so",
                "**/libdjisdk_jni.so",
                "**/libDJIUpgradeCore.so",
                "**/libDJIUpgradeJNI.so",
                "**/libDJIWaypointV2Core-CSDK.so",
                "**/libdjiwpv2-CSDK.so",
                "**/libFlightRecordEngine.so",
                "**/libvideo-framing.so",
                "**/libwaes.so",
                "**/libagora-rtsa-sdk.so",
                "**/libc++.so",
                "**/libc++_shared.so",
                "**/libmrtc_28181.so",
                "**/libmrtc_agora.so",
                "**/libmrtc_core.so",
                "**/libmrtc_core_jni.so",
                "**/libmrtc_data.so",
                "**/libmrtc_log.so",
                "**/libmrtc_onvif.so",
                "**/libmrtc_rtmp.so",
                "**/libmrtc_rtsp.so",
            )
        }
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
            )
        }
    }
}

tasks.register<Copy>("packageOrangeDroneKitApk") {
    group = "build"
    description = "Builds and copies the installable APK with the product name."
    dependsOn("assembleRelease")
    doNotTrackState("The dist folder may contain externally managed website files and large APK copies.")

    from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename { "Orange-DroneKit.apk" }
}

tasks.register<Copy>("packageOrangeSyncLogApk") {
    group = "build"
    description = "Compatibility alias for the previous APK task name."
    dependsOn("packageOrangeDroneKitApk")
    doNotTrackState("Compatibility copy only; no incremental state needed.")

    from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename { "Orange-SyncLog.apk" }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("commons-net:commons-net:3.11.1")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.dji:dji-sdk-v5-aircraft:5.18.0")
    compileOnly("com.dji:dji-sdk-v5-aircraft-provided:5.18.0")
    runtimeOnly("com.dji:dji-sdk-v5-networkImp:5.18.0")

    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
