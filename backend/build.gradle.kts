// =======================================================
// build.gradle.kts — Ecommerce Adilson Machado Books (Spring Boot + Kotlin + Flyway)
// Compatível com PostgreSQL 18 e Flyway 11.17.0
// =======================================================

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.noarg") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"

    id("org.springframework.boot") version "3.4.10"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "11.17.0"
}

group = "com.nicolaskempes"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories { mavenCentral() }

dependencies {
    // BOM do Spring Boot
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.10"))

    // Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // HTTP (gerenciados pelo BOM)
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.core5:httpcore5")

    // SDK EFI
    implementation("br.com.efipay.efisdk:sdk-java-apis-efi:1.2.2")

    // Validation
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    // Lombok (opcional)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Metadata de @ConfigurationProperties
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // JDBC driver (runtime da APP)
    runtimeOnly("org.postgresql:postgresql:42.7.7")

    // Flyway na APP (runtime)
    implementation("org.flywaydb:flyway-core:11.17.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.17.0")

    // Testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mitigações de segurança
    constraints {
        implementation("ch.qos.logback:logback-classic:1.5.20")
        implementation("ch.qos.logback:logback-core:1.5.20")
    }
}

/* ---------- Kotlin / Toolchain / Compiler (compatível com IntelliJ) ---------- */
kotlin { jvmToolchain(21) }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.test { useJUnitPlatform() }

/** JPA via allOpen + noArg */
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

/** Recursos: não empacotar templates operacionais */
sourceSets {
    named("main") {
        resources {
            exclude("db/migration/T__*.sql")
            exclude("db/migration/*/T__.sql")
        }
    }
}

/* ====================== Flyway via ENV (OCP-friendly) ====================== */

private object FlywayEnv {
    val url = listOf("FLYWAY_URL", "JDBC_DATABASE_URL", "DATABASE_URL", "SPRING_DATASOURCE_URL")
    val user = listOf("FLYWAY_USER", "JDBC_DATABASE_USERNAME", "DB_USERNAME", "SPRING_DATASOURCE_USERNAME")
    val pass = listOf("FLYWAY_PASSWORD", "JDBC_DATABASE_PASSWORD", "DB_PASSWORD", "SPRING_DATASOURCE_PASSWORD")

    const val schemas = "FLYWAY_SCHEMAS"
    const val locations = "FLYWAY_LOCATIONS"
    const val cleanDisabled = "FLYWAY_CLEAN_DISABLED"

    const val phName = "SITE_AUTHOR_NAME"
    const val phEmail = "SITE_AUTHOR_EMAIL"
    const val phPixKey = "SITE_AUTHOR_PIX_KEY"
}

private fun firstEnv(keys: List<String>): String? =
    keys.firstNotNullOfOrNull { k -> System.getenv(k)?.takeIf { it.isNotBlank() } }

private fun envOr(default: String, key: String): String =
    System.getenv(key)?.takeIf { it.isNotBlank() } ?: default

private fun parseBooleanEnv(key: String, default: Boolean): Boolean {
    val v = System.getenv(key)?.trim()?.lowercase() ?: return default
    return when (v) {
        "true", "1", "yes", "y", "on" -> true
        "false", "0", "no", "n", "off" -> false
        else -> default
    }
}

/** Converte "postgres://user:pass@host:5432/db?..." → "jdbc:postgresql://host:5432/db?..."  */
private fun normalizeToJdbc(raw: String): String {
    if (raw.isBlank()) return raw

    return when {
        raw.startsWith("postgres://") -> {
            val withoutScheme = raw.removePrefix("postgres://")
            val hostPart = withoutScheme.substringAfter("@", withoutScheme)
            val base = "jdbc:postgresql://$hostPart"
            if ('?' in base) {
                if (base.contains("sslmode=")) base else "$base&sslmode=require"
            } else {
                "$base?sslmode=require"
            }
        }
        raw.startsWith("jdbc:postgresql://") -> raw
        else -> "jdbc:postgresql://$raw"
    }
}

private fun parseUserFromUrl(url: String): String? = try {
    val afterScheme = url.substringAfter("://", url)
    val creds = afterScheme.substringBefore('@', "")
    creds.substringBefore(':').ifBlank { null }
} catch (_: Throwable) { null }

private fun parsePassFromUrl(url: String): String? = try {
    val afterScheme = url.substringAfter("://", url)
    val creds = afterScheme.substringBefore('@', "")
    creds.substringAfter(':').ifBlank { null }
} catch (_: Throwable) { null }

private fun parseLocations(raw: String?): Array<String>? =
    raw
        ?.takeIf { it.isNotBlank() }
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.map { loc ->
            when {
                loc.startsWith("filesystem:") || loc.startsWith("classpath:") -> loc
                else -> "filesystem:$loc"
            }
        }
        ?.toTypedArray()

private val isFlywayTaskRequested: Boolean =
    gradle.startParameter.taskNames.any { it.startsWith("flyway", ignoreCase = true) && it != "bootRun" }

/* -------- URL -------- */
val rawUrl = firstEnv(FlywayEnv.url)
val urlValue = when {
    isFlywayTaskRequested && rawUrl == null ->
        throw GradleException("Missing Flyway URL. Set ONE of: ${FlywayEnv.url.joinToString(" | ")}")
    rawUrl != null -> normalizeToJdbc(rawUrl)
    else -> "jdbc:postgresql://localhost:5432/placeholder"
}

/* -------- USER / PASS (prefer envs; fallback: extrai da URL) -------- */
val urlForParsing = rawUrl ?: ""

val userValue =
    firstEnv(FlywayEnv.user)
        ?: parseUserFromUrl(urlForParsing)
        ?: if (isFlywayTaskRequested)
            throw GradleException("Missing Flyway user. Set env FLYWAY_USER (ou alias) ou inclua na URL.")
        else "placeholder"

val passValue =
    firstEnv(FlywayEnv.pass)
        ?: parsePassFromUrl(urlForParsing)
        ?: if (isFlywayTaskRequested)
            throw GradleException("Missing Flyway password. Set env FLYWAY_PASSWORD (ou alias) ou inclua na URL.")
        else "placeholder"

/* -------- SCHEMAS -------- */
val schemasValue = envOr("public", FlywayEnv.schemas)

/* -------- LOCATIONS -------- */
val effectiveLocations: Array<String> =
    parseLocations(System.getenv(FlywayEnv.locations))
        ?: arrayOf("filesystem:src/main/resources/db/migration", "classpath:db/migration")

/* -------- CLEAN DISABLED -------- */
val cleanDisabledValue = parseBooleanEnv(FlywayEnv.cleanDisabled, default = true)

/* -------- Placeholders -------- */
val placeholderName = envOr("", FlywayEnv.phName)
val placeholderEmail = envOr("", FlywayEnv.phEmail)
val placeholderPix = envOr("", FlywayEnv.phPixKey)

flyway {
    url = urlValue
    user = userValue
    password = passValue
    schemas = arrayOf(schemasValue)
    locations = effectiveLocations
    cleanDisabled = cleanDisabledValue

    placeholders = mapOf(
        "SITE_AUTHOR_NAME" to placeholderName,
        "SITE_AUTHOR_EMAIL" to placeholderEmail,
        "SITE_AUTHOR_PIX_KEY" to placeholderPix
    )
}

// ====== BootJar com nome previsível ======
tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("e-commerce-adilson")
}