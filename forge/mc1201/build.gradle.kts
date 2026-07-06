plugins {
    id("net.minecraftforge.gradle")
    packetevents.`library-conventions`
}

repositories {
    mavenCentral()
    maven("https://repo.viaversion.com/")
}

val minecraft_version: String by project
val forge_version: String by project

minecraft {
    mappings("official", minecraft_version)
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

    compileOnly(project(":api", "shadow"))
    compileOnly(project(":netty-common"))
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.via.version)
}

tasks {
    withType<JavaCompile> {
        options.release = 17
    }

    processResources {
        val projectVersion = project.version
        inputs.property("version", projectVersion)
        filesMatching("META-INF/mods.toml") {
            expand("version" to projectVersion)
        }
    }

    jar {
        archiveBaseName = "${rootProject.name}-forge-${project.name}"
        archiveVersion = rootProject.ext["artifactVersion"] as String
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}