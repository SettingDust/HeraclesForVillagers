apply(
    from = "https://github.com/SettingDust/FabricKotlinTemplate/raw/main/common.settings.gradle.kts"
)

val minecraft = settings.extra["minecraft"]
val kotlin = settings.extra["kotlin"]

dependencyResolutionManagement.versionCatalogs.named("catalog") {
    /**
     * ***********
     * Libraries
     * ************
     */
    // https://modrinth.com/mod/heracles/versions
    val heracles = "1.1.13"
    library("heracles-fabric", "maven.modrinth", "heracles").version("$heracles-fabric")
    library("heracles-forge", "maven.modrinth", "heracles").version("$heracles-forge")

    // https://modrinth.com/mod/resourceful-lib/versions
    val resourcefullib = "2.1.23"
    library(
            "resourceful-lib-fabric",
            "com.teamresourceful.resourcefullib",
            "resourcefullib-fabric-$minecraft"
        )
        .version(resourcefullib)
    library(
            "resourceful-lib-forge",
            "com.teamresourceful.resourcefullib",
            "resourcefullib-forge-$minecraft"
        )
        .version(resourcefullib)

    library("kinecraft-serialization", "maven.modrinth", "kinecraft-serialization")
        .version("1.3.0-fabric")

    library("reputation", "maven.modrinth", "your-reputation").version("0.2.4+jade.1.20")
    library("jade", "maven.modrinth", "jade").version("11.4.3")

    // https://modrinth.com/mod/guard-villagers-(fabricquilt)/versions
    library("guard-villagers", "maven.modrinth", "guard-villagers-(fabricquilt)")
        .version("2.0.9-$minecraft")

    library("heracles-blabber", "maven.modrinth", "heracles-for-blabber").version("0.5.2-fabric")

    // https://modrinth.com/mod/blabber/versions
    library("blabber", "org.ladysnake", "blabber").version("1.4.0-mc$minecraft")

    // https://maven.ladysnake.org/#/releases/dev/onyxstudios/cardinal-components-api
    val cardinalComponents = "5.2.2"
    library(
            "cardinal-components-base",
            "dev.onyxstudios.cardinal-components-api",
            "cardinal-components-base"
        )
        .version(cardinalComponents)
    library(
            "cardinal-components-entity",
            "dev.onyxstudios.cardinal-components-api",
            "cardinal-components-entity"
        )
        .version(cardinalComponents)
}

plugins {
    // https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val name: String by settings

rootProject.name = name

include("mod")

include("quilt")

include("forge")
