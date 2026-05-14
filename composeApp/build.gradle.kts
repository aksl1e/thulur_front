import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val jcefNativeVersion = libs.versions.jcefmavenNatives.get()
val jcefNativeArtifact = currentJcefNativeArtifact()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.koin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.credential.secure.storage)
            implementation(libs.jcefmaven)
            implementation("me.friwi:$jcefNativeArtifact:$jcefNativeVersion")
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
    }
}

fun currentJcefNativeArtifact(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val isArm64 = osArch.contains("aarch64") || osArch.contains("arm64")

    return when {
        osName.contains("win") -> if (isArm64) "jcef-natives-windows-arm64" else "jcef-natives-windows-amd64"
        osName.contains("mac") -> if (isArm64) "jcef-natives-macosx-arm64" else "jcef-natives-macosx-amd64"
        osName.contains("linux") -> if (isArm64) "jcef-natives-linux-arm64" else "jcef-natives-linux-amd64"
        else -> error("Unsupported JCEF platform: $osName / $osArch")
    }
}


compose.desktop {
    application {
        mainClass = "com.example.thulur.MainKt"
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
        jvmArgs(
            "--add-exports=java.base/java.lang=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED"
        )

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Thulur"
            packageVersion = "1.0.0"
            modules("jdk.httpserver")
        }
    }
}
