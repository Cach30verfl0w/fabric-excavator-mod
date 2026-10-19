// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "fabric-excavator-mod"
