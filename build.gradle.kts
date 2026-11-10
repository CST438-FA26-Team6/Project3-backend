plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	kotlin("plugin.jpa") version "2.3.21"
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
}

group = "com.cst438.team6"
version = "0.0.1-SNAPSHOT"
description = "Anime and Manga Tracker API"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
	testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
	testImplementation("io.rest-assured:rest-assured:6.1.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}
//Jacoco
jacoco {
	toolVersion = "0.8.15"
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	classDirectories.setFrom(
		fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
			include("com/cst438/team6/project3/service/**")
		}
	)
}
//70%
tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	classDirectories.setFrom(
		fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
			include("com/cst438/team6/project3/service/**")
		}
	)
	violationRules {
		rule {
			element = "PACKAGE"
			includes = listOf("com.cst438.team6.project3.service")
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.70".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}