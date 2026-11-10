plugins {
    id("java-library")
    id("idea")
}

group = BuildConfig.GROUP
version = BuildConfig.createVersionString(project)

base {
    archivesName = "${BuildConfig.MOD_ID}-${project.name}"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(BuildConfig.JAVA_VERSION)

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(BuildConfig.JAVA_VERSION)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Plasmo"
                url = uri("https://repo.plasmoverse.com/releases")
            }
        }
        filter {
            includeGroup("su.plo.voice")
            includeGroup("su.plo.voice.api")
            includeGroup("su.plo.slib")
        }
    }
}
