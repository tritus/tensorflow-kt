import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests

plugins {
    kotlin("multiplatform") version "1.3.72"
}

repositories {
    mavenCentral()
}

kotlin {
    val nativeTargets = listOf(
        macosX64(),
        mingwX64(),
        linuxX64()
    )

    nativeTargets.forEach {
        it.compilations.all {
            val tensorflowInterop by cinterops.creating
        }
    }
}