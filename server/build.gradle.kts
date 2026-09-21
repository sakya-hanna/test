plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

version = "0.1.0"

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "com.willam.chatnotes.server.MainKt"
    }
    archiveBaseName.set("chatnotes-server")
    archiveClassifier.set("")
    archiveVersion.set("")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

dependencies {
    implementation(project(":shared"))
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-server-auth:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("org.slf4j:slf4j-nop:2.0.13")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    testImplementation("io.ktor:ktor-client-serialization:2.3.12")
}
