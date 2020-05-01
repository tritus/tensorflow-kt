import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.3.72"
}

group = "org.tritus.tensorflowkt"
version = "0.0.1"

val kotlinNativeDataPath = System.getenv("KONAN_DATA_DIR")?.let { File(it) }
    ?: File(System.getProperty("user.home")).resolve(".konan")

val tensorflowHome = kotlinNativeDataPath.resolve("third-party/tensorflow")

kotlin {
    // Create target for the host platform.
    val hostTarget = when (val hostOs = System.getProperty("os.name")) {
        "Mac OS X" -> macosX64("tensorflow")
        "Linux" -> linuxX64("tensorflow")
        // Windows is not yet supported
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable {
                linkerOpts("-L${tensorflowHome.resolve("lib")}", "-ltensorflow")
                runTask?.environment(
                    "LD_LIBRARY_PATH" to tensorflowHome.resolve("lib"),
                    "DYLD_LIBRARY_PATH" to tensorflowHome.resolve("lib")
                )
            }
        }

        compilations["main"].cinterops {
            val tensorflowInterop by creating  {
                includeDirs(tensorflowHome.resolve("include"))
            }
        }

        dependencies {
            commonMainImplementation(kotlin("stdlib-common"))
        }
    }
}

val downloadTensorflow by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadTensorflow.sh")
}

val tensorflow: KotlinNativeTarget by kotlin.targets
tasks[tensorflow.compilations["main"].cinterops["tensorflowInterop"].interopProcessingTaskName].dependsOn(downloadTensorflow)