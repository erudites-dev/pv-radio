import me.modmuss50.mpp.PublishModTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

plugins {
    id("me.modmuss50.mod-publish-plugin")
}

val projectProviders = providers

fun env(name: String, default: String): String = projectProviders.environmentVariable(name)
    .orElse(default)
    .get()

fun envFlag(name: String, default: Boolean): Boolean = env(name, default.toString()).toBoolean()

fun <T> envChoice(name: String, default: String, options: Map<String, T>): T {
    val value = env(name, default)
    return options[value] ?: error("Unknown $name: $value")
}

class LoaderArtifact(
    val id: String,
    val displayName: String,
    val modLoaderIds: List<String>,
    val modrinthRequires: List<String> = emptyList(),
    val curseforgeRequires: List<String> = emptyList(),
    val curseforge: Boolean = true,
) {
    val projectPath: String = ":$id"
}

val loaderArtifacts = listOf(
    LoaderArtifact(
        "fabric", "Fabric", listOf("fabric", "quilt"),
        modrinthRequires = listOf("fabric-api", "plasmo-voice"),
        curseforgeRequires = listOf("fabric-api", "plasmo-voice"),
    ),
    LoaderArtifact(
        "neoforge", "NeoForge", listOf("neoforge"),
        modrinthRequires = listOf("plasmo-voice"),
        curseforgeRequires = listOf("plasmo-voice"),
    ),
    LoaderArtifact(
        "paper", "Paper", listOf("paper"),
        modrinthRequires = listOf("plasmo-voice"),
        curseforge = false,
    ),
).filter { findProject(it.projectPath) != null }

fun loaderJar(loader: LoaderArtifact): Provider<RegularFile> =
    project(loader.projectPath).tasks.named<Jar>("jar").flatMap { it.archiveFile }

gradle.projectsEvaluated {
    // https://github.com/modmuss50/mod-publish-plugin
    publishMods {
        //dryRun = true

        changelog = env("CHANGELOG_TEXT", "").ifEmpty {
            projectProviders.fileContents(layout.projectDirectory.file("changelog.md")).asText.getOrElse("")
        }

        type = when (env("VERSION_TYPE", "release")) {
            "alpha" -> ALPHA
            "beta" -> BETA
            else -> STABLE
        }
        val includeSnapshot = envFlag("INCLUDE_SNAPSHOTS", false)
        val publishEnabled = envFlag("PUBLISH_ENABLED", true)

        val (modrinthSelected, curseforgeSelected) = envChoice("PUBLISH_PLATFORM", "all", mapOf(
            "all" to (true to true),
            "modrinth" to (true to false),
            "curseforge" to (false to true),
            "github" to (false to false),
        ))
        val modrinthEnabled = modrinthSelected && BuildConfig.MODRINTH_PROJECT_ID.isNotEmpty()
        val curseforgeEnabled = curseforgeSelected && BuildConfig.CURSEFORGE_PROJECT_ID.isNotEmpty()

        val loaderId = env("LOADER", "all")
        val loaders = loaderArtifacts.filter { loaderId == "all" || it.id == loaderId }
        if (loaders.isEmpty()) {
            error("Unknown LOADER: $loaderId")
        }

        if (!publishEnabled) {
            println("Publishing is disabled. Set PUBLISH_ENABLED=true to enable.")
            return@publishMods
        }

        val cfOptions = curseforgeOptions {
            accessToken = projectProviders.environmentVariable("CURSEFORGE_TOKEN")
            projectId = BuildConfig.CURSEFORGE_PROJECT_ID
            minecraftVersionRange {
                start = BuildConfig.MINECRAFT_VERSION_MIN
                end = BuildConfig.MINECRAFT_PUBLISH_END
            }
            javaVersions.add(JavaVersion.toVersion(BuildConfig.JAVA_VERSION))
            client = true
            server = true
        }

        val mrOptions = modrinthOptions {
            accessToken = projectProviders.environmentVariable("MODRINTH_TOKEN")
            projectId = BuildConfig.MODRINTH_PROJECT_ID
            minecraftVersionRange {
                start = BuildConfig.MINECRAFT_VERSION_MIN
                end = BuildConfig.MINECRAFT_PUBLISH_END
                includeSnapshots = includeSnapshot
            }
        }

        val minecraftVersion = BuildConfig.MINECRAFT_VERSION.substringBefore('-')

        for (loader in loaders) {
            val platformSuffix = loader.id.replaceFirstChar(Char::uppercaseChar)
            val artifactVersion = "mc${minecraftVersion}-${BuildConfig.MOD_VERSION}-${loader.id}"
            val artifactDisplayName = "${BuildConfig.MOD_NAME} ${BuildConfig.MOD_VERSION} for ${loader.displayName} ${BuildConfig.MINECRAFT_VERSION}"

            if (curseforgeEnabled && loader.curseforge) {
                curseforge("curseforge${platformSuffix}") {
                    from(cfOptions)
                    file = loaderJar(loader)
                    modLoaders.addAll(loader.modLoaderIds)
                    version = artifactVersion
                    displayName = artifactDisplayName
                    loader.curseforgeRequires.forEach { requires(it) }
                }
            }

            if (modrinthEnabled) {
                modrinth("modrinth${platformSuffix}") {
                    from(mrOptions)
                    file = loaderJar(loader)
                    modLoaders.addAll(loader.modLoaderIds)
                    version = artifactVersion
                    displayName = artifactDisplayName
                    loader.modrinthRequires.forEach { requires(it) }
                }
            }
        }

        github {
            val releaseVersion = "${BuildConfig.MOD_VERSION}+mc${BuildConfig.MINECRAFT_VERSION}"

            accessToken = projectProviders.environmentVariable("GITHUB_TOKEN")
            repository = BuildConfig.GITHUB_REPOSITORY
            commitish = projectProviders.environmentVariable("GITHUB_SHA").orElse("main")
            tagName = releaseVersion
            version = releaseVersion
            displayName = "${BuildConfig.MOD_NAME} ${BuildConfig.MOD_VERSION} for Minecraft ${BuildConfig.MINECRAFT_VERSION}"

            file = loaderJar(loaders.first())
            loaders.drop(1).forEach { additionalFiles.from(loaderJar(it)) }
        }
    }

    val publishTasks = tasks.withType<PublishModTask>()
    fun publishTasksMatching(predicate: (String) -> Boolean) = publishTasks.matching { predicate(it.platform.name) }

    loaderArtifacts.zipWithNext().forEach { (previous, next) ->
        publishTasksMatching { it.contains(next.id, ignoreCase = true) }.configureEach {
            mustRunAfter(publishTasksMatching { it.contains(previous.id, ignoreCase = true) })
        }
    }
    publishTasksMatching { it == "github" }.configureEach {
        mustRunAfter(publishTasksMatching { it != "github" })
    }
}