plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
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
    // Modelos de configuracion.json y estado.json (arquitectura.md § 10).
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
