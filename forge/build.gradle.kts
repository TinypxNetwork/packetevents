plugins {
    packetevents.`library-conventions`
    packetevents.`publish-conventions`
}

repositories {
    mavenCentral()
}

dependencies {
    // api(): re-export so consumers' POMs see them transitively.
    api(libs.bundles.adventure)
    api(project(":api", "shadow"))
    api(project(":netty-common"))
    api(project(":forge:mc1201"))
}

tasks {
    jar {
        archiveBaseName = "${rootProject.name}-forge"
        archiveVersion = rootProject.ext["artifactVersion"] as String
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
    }
}