plugins {
    id("multiloader-platform")
    id("net.fabricmc.fabric-loom") version("1.17.+")
}

val configurationCommonModJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = true
}
val configurationCommonModResources: Configuration = configurations.create("commonResources") {
    isCanBeResolved = true
}

repositories {
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))
    configurationCommonModResources(project(path = ":common", configuration = "commonMainResources"))
}

sourceSets.apply {
    main {
        compileClasspath += configurationCommonModJava
        runtimeClasspath += configurationCommonModJava
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    implementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    implementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.FABRIC_API_VERSION}")

    compileOnly("su.plo.voice.api:client:${BuildConfig.PLASMOVOICE_VERSION}")
    compileOnly("su.plo.voice.api:server:${BuildConfig.PLASMOVOICE_VERSION}")
}

loom {
    mixin {
        useLegacyMixinAp = false
    }

    runs {
        named("client") {
            client()
        }
    }
}

tasks {
    val outputDir = rootProject.layout.buildDirectory.dir("output")

    jar {
        from(configurationCommonModJava)
        destinationDirectory.set(outputDir)
    }

    sourcesJar {
        from(configurationCommonModJava)
        destinationDirectory.set(outputDir.map { it.dir("sources") })
    }

    processResources {
        from(configurationCommonModResources)
    }
}