// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

plugins {
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.kotlin.jvm)
	`maven-publish`
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("fabric-excavator-mod") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets["client"])
		}
	}
}

dependencies {
	minecraft(libs.minecraft)
	mappings(loom.officialMojangMappings())
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)
	modImplementation(libs.fabric.language.kotlin)
}

tasks.processResources {
	val projectVersion = project.version.toString()
	inputs.property("version", projectVersion)

	filesMatching("fabric.mod.json") {
		expand("version" to projectVersion)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
	jvmToolchain(21)
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}
}
