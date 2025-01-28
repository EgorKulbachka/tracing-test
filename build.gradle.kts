plugins {
    kotlin("jvm") version "2.1.0"
}

group = "org.finmid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.micrometer:micrometer-tracing:1.4.2")
    implementation("io.micrometer:micrometer-tracing-bridge-otel:1.4.2")
    implementation("io.micrometer:micrometer-observation:1.14.3")
    implementation("io.micrometer:micrometer-core:1.14.3")

    implementation("io.opentelemetry:opentelemetry-sdk-trace:1.46.0")
    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-context-data-2.17-autoconfigure:2.12.0-alpha")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.46.0")

    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}