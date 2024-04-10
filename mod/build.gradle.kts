plugins {
    alias(catalog.plugins.fabric.loom)

    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)
    alias(catalog.plugins.explosion)
}

val id: String by rootProject.properties
val name: String by rootProject.properties
val author: String by rootProject.properties
val description: String by rootProject.properties

base.archivesName = name

loom {
    splitEnvironmentSourceSets()

    mixin {
        defaultRefmapName = "$id.refmap.json"

        add("main", "$id.refmap.json")
        add("client", "$id.client.refmap.json")
    }

    mods {
        register(id) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runs {
        configureEach { ideConfigGenerated(true) }
        named("client") { name("Fabric Client") }
        named("server") { name("Fabric Server") }
    }
}

val modNeedCopy by configurations.creating { isTransitive = false }

val modClientNeedCopy by
    configurations.creating {
        extendsFrom(modNeedCopy)
        isTransitive = false
    }

kotlin { jvmToolchain(17) }

dependencies {
    minecraft(catalog.minecraft)
    mappings(variantOf(catalog.yarn) { classifier("v2") })

    modImplementation(catalog.fabric.loader)
    modImplementation(catalog.fabric.api)
    modImplementation(catalog.fabric.kotlin)

    val modClientImplementation by configurations
    modClientImplementation(catalog.modmenu)

    modImplementation(explosion.fabric(catalog.heracles.fabric.get().toString()))
    modImplementation(catalog.resourceful.lib.fabric)

    modImplementation(catalog.heracles.blabber)
    modRuntimeOnly(catalog.blabber)
    modImplementation(catalog.cardinal.components.base)
    modImplementation(catalog.cardinal.components.entity)

    modImplementation(catalog.kinecraft.serialization)
    include(catalog.kinecraft.serialization)

    modCompileOnly(catalog.guard.villagers)
    modNeedCopy(catalog.guard.villagers)

    modRuntimeOnly(catalog.jade)
    modRuntimeOnly(catalog.reputation)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

val metadata =
    mapOf(
        "group" to group,
        "author" to author,
        "id" to id,
        "name" to name,
        "version" to version,
        "description" to description,
        "source" to "https://github.com/SettingDust/HeraclesForVillagers",
        "minecraft" to "~1.20",
        "fabric_loader" to ">=0.12",
        "fabric_kotlin" to ">=1.10",
        "modmenu" to "*",
    )

tasks {
    withType<ProcessResources> {
        inputs.properties(metadata)
        filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(metadata) }
    }

    jar { from("LICENSE") }

    val copyClientMods by
        creating(Copy::class) {
            destinationDir = file("${loom.runs.getByName("client").runDir}/mods")
            from(modClientNeedCopy)
        }

    classes { dependsOn(copyClientMods) }

    ideaSyncTask { enabled = true }
}
