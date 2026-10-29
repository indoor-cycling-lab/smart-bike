plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// TokensSincronizadosTest e IconosSincronizadosTest comparan los derivados con sus fuentes; al declararlas como
// entrada, Gradle repite los tests si cambian.
val tokensJson = rootProject.file("design-system/tokens.json")
val iconsSvg = rootProject.file("design-system/icons.svg")
val drawables = file("src/main/res/drawable")
val recursos = file("src/main/res")
val generador = rootProject.file("scripts/design-system/generar.py")
val identidad = rootProject.file("design-system/identidad")

// Versión desde la etiqueta de la release, `-PetiquetaVersion=vX.Y.Z` (design.md del change `preparar-release`,
// decisión 3): versionCode = X·1 000 000 + Y·1 000 + Z. Sin la propiedad, una versión de desarrollo que nunca se instala
// encima de una publicada.
val etiquetaVersion = providers.gradleProperty("etiquetaVersion").orNull
val (nombreVersion, codigoVersion) = if (etiquetaVersion == null) {
    "0.0.0-dev" to 1
} else {
    val partes = Regex("""^v(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""").matchEntire(etiquetaVersion)?.destructured?.toList()?.map { it.toInt() }
        ?: throw GradleException("etiquetaVersion debe tener la forma vX.Y.Z (tres números de hasta 3 cifras): $etiquetaVersion")
    partes.joinToString(".") to partes[0] * 1_000_000 + partes[1] * 1_000 + partes[2]
}

// Firma de la release (decisión 2): de ~/.gradle/gradle.properties o del entorno, nunca del repositorio. Sin las cuatro
// claves, la release se compila sin firmar.
fun firma(clave: String): String? = providers.gradleProperty(clave).orElse(providers.environmentVariable(clave)).orNull
val almacenFirma = firma("RELEASE_KEYSTORE")
val claveAlmacen = firma("RELEASE_KEYSTORE_PASSWORD")
val aliasFirma = firma("RELEASE_KEY_ALIAS")
val claveAlias = firma("RELEASE_KEY_PASSWORD")
val conFirma = listOf(almacenFirma, claveAlmacen, aliasFirma, claveAlias).all { !it.isNullOrEmpty() }

android {
    namespace = "app.smartbike"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.smartbike"
        minSdk = 28
        targetSdk = 36
        versionCode = codigoVersion
        versionName = nombreVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (conFirma) {
            create("release") {
                storeFile = file(almacenFirma!!)
                storePassword = claveAlmacen
                keyAlias = aliasFirma
                keyPassword = claveAlias
                // minSdk 28: basta con v2 y v3.
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            // R8 reduce y optimiza, sin ofuscar (decisión 1): las trazas del registro siguen legibles.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (conFirma) {
                signingConfig = signingConfigs.getByName("release")
            } else if (gradle.startParameter.taskNames.any { it.contains("Release") }) {
                logger.warn("Sin RELEASE_KEYSTORE, RELEASE_KEYSTORE_PASSWORD, RELEASE_KEY_ALIAS y RELEASE_KEY_PASSWORD: la release se compila sin firmar")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.systemProperty("tokens.json", tokensJson.absolutePath)
            it.inputs.file(tokensJson).withPathSensitivity(PathSensitivity.NONE)
            it.systemProperty("icons.svg", iconsSvg.absolutePath)
            it.systemProperty("drawables", drawables.absolutePath)
            it.inputs.file(iconsSvg).withPathSensitivity(PathSensitivity.NONE)
            it.inputs.dir(drawables).withPathSensitivity(PathSensitivity.RELATIVE)
            it.systemProperty("generador", generador.absolutePath)
            it.systemProperty("res", recursos.absolutePath)
            it.inputs.file(generador).withPathSensitivity(PathSensitivity.NONE)
            it.inputs.dir(identidad).withPathSensitivity(PathSensitivity.RELATIVE)
            it.inputs.dir(recursos).withPathSensitivity(PathSensitivity.RELATIVE)
        }
    }
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    filter {
        exclude("**/build/**")
        exclude("**/Tokens.kt")
    }
}

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.splashscreen)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.nordic.ble)
    implementation(libs.nordic.ble.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    debugImplementation(libs.compose.ui.test.manifest)
}
