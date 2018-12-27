import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import moe.nikky.counter.CounterExtension
import net.fabricmc.loom.task.RemapJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.generateconstants.GenerateConstantsTask

plugins {
    idea
    `maven-publish`
    id("constantsGenerator")
    id("moe.nikky.persistentCounter") version "0.0.5"
    kotlin("jvm") version Kotlin.version
    id("kotlinx-serialization") version Kotlin.version
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("fabric-loom") version Fabric.Loom.version
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = Constants.modid
}

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch

counter {
    variable(id = "buildnumber", key = "$major.$minor.$patch${Env.branch}") {
        default = 1
    }
}

val counter: CounterExtension = extensions.getByType()

val buildnumber by counter.map

group = Constants.group
description = Constants.description
version = "$major.$minor.$patch-$buildnumber${Env.branch}"

minecraft {
    //    refmapName = "matterlink.refmap.json"
}

val folder = listOf("matterlink")
configure<ConstantsExtension> {
    constantsObject(
        pkg = folder.joinToString("."),
        className = project.name
            .split("-")
            .joinToString("") {
                it.capitalize()
            } + "Constants"
    ) {
        field("BUILD_NUMBER") value buildnumber
        field("JENKINS_BUILD_NUMBER") value Env.buildNumber
        field("BUILD") value Env.versionSuffix
        field("MAJOR_VERSION") value major
        field("MINOR_VERSION") value minor
        field("PATCH_VERSION") value patch
        field("VERSION") value "$major.$minor.$patch"
        field("FULL_VERSION") value "$major.$minor.$patch-${Env.versionSuffix}"
        field("MC_VERSION") value "1.14"
        field("FABRIC_API_VERSION") value Fabric.FabricAPI.version
    }
}

val generateConstants by tasks.getting(GenerateConstantsTask::class) {
    kotlin.sourceSets["main"].kotlin.srcDir(outputFolder)
}

// TODO depend on kotlin tasks in the plugin
tasks.withType<KotlinCompile> {
    dependsOn(generateConstants)
}

repositories {
    //    mavenLocal()
    maven(url = "https://maven.fabricmc.net") {
        name = "fabricmc"
    }
    maven(url = "https://kotlin.bintray.com/kotlinx") {
        name = "kotlinx"
    }
    maven(url = "https://jitpack.io") {
        name = "jitpack"
    }
    maven(url = "https://repo.elytradev.com") {
        name = "elytradev"
    }
//    mavenCentral()
    jcenter()
}

configurations.runtimeOnly.extendsFrom(configurations.modCompile)
configurations.api.extendsFrom(configurations.shadow)

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Minecraft.version)

    mappings(group = "net.fabricmc", name = "yarn", version = "${Minecraft.version}.${Fabric.Yarn.version}")

    modCompile(group = "net.fabricmc", name = "fabric-loader", version = Fabric.version)

    modCompile(group = "net.fabricmc", name = "fabric-language-kotlin", version = Fabric.LanguageKotlin.version) {
        isTransitive = true
    }

    modCompile(group = "net.fabricmc", name = "fabric", version = Fabric.FabricAPI.version)

    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-serialization-runtime",
        version = KotlinX.Serialization.version
    )

    shadow(group = "com.github.kittinunf.Fuel", name = "fuel", version = Fuel.version)

    shadow(group = "com.github.kittinunf.Fuel", name = "fuel-coroutines", version = Fuel.version)

    shadow(group = "com.github.kittinunf.Fuel", name = "fuel-kotlinx-serialization", version = Fuel.version)

    shadow(group = "com.github.kittinunf.result", name = "result", version = "2.0.0")

    shadow(group = "blue.endless", name = "jankson", version = "1.0.0-7")
}

configurations {
    getByName("shadow") {
        isTransitive = false
    }
//    project.configurations.runtimeOnlyDependenciesMetadata.apply {
//        exclude(group = "net.minecraft", module = "minecraft")
//        resolvedConfiguration.firstLevelModuleDependencies.forEach {
//            println("runtimeOnlyDependenciesMetadata $it")
//            configurations.shadow.exclude(group = it.moduleGroup, module = it.moduleName)
//        }
//    }
}


tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "fabricKotlin" to Fabric.LanguageKotlin.version,
                "version" to version
            )
        )
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    classifier = ""
    configurations = project.configurations.run { listOf(shadow) }

//    dependencies {
//        println("configuring dependencies")
//        project.configurations.runtimeOnlyDependenciesMetadata.resolvedConfiguration.firstLevelModuleDependencies.forEach {
//            println("runtimeOnlyDependenciesMetadata: $it")
//            exclude(dependency("${it.moduleGroup}:${it.moduleName}"))
//        }
//    }


//    project.configurations.runtime.allDependencies.forEach {
//        println("runtime: $it")
//    }

    relocate("com.github", "matterlink.repack.com.github") { }
    relocate("org.jetbrains", "matterlink.repack.org.jetbrains") { }
    relocate("blue.endless", "matterlink.repack.blue.endless") { }
    relocate("kotlinx.io", "matterlink.repack.kotlinx.io") { }
    relocate("kotlinx.serialization", "matterlink.repack.kotlinx.serialization") { }
//    relocate("kotlinx", "matterlink.repack.kotlinx") { }
}

val remapJar = tasks.getByName<RemapJar>("remapJar") {
    (this as Task).dependsOn(shadowJar)
    jar = shadowJar.archivePath
}

fun shadowComponents(publication: MavenPublication, vararg configurations: Configuration) {
    publication.pom.withXml {
        val dependenciesNode = asNode().appendNode("dependencies")

        project.configurations.shadow.allDependencies.forEach {
            if (it !is SelfResolvingDependency) {
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", it.group)
                dependencyNode.appendNode("artifactId", it.name)
                dependencyNode.appendNode("version", it.version)
                dependencyNode.appendNode("scope", "runtime")
            }
        }
        configurations.forEach { configuration ->
            println("processing: $configuration")
            configuration.dependencies.forEach inner@{ dependency ->
                if (dependency !is SelfResolvingDependency) {
                    if (dependency is ModuleDependency && !dependency.isTransitive) {
                        return@inner
                    }

                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", dependency.group)
                    dependencyNode.appendNode("artifactId", dependency.name)
                    dependencyNode.appendNode("version", dependency.version)
                    dependencyNode.appendNode("scope", configuration.name)
                }
            }
        }
    }
}

publishing {
    publications {
        create("default", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()

            artifact(shadowJar)
            shadowComponents(this, configurations.modCompile)
        }
    }
    repositories {
        maven(url = "http://mavenupload.modmuss50.me/") {
            val mavenPass: String? = project.properties["mavenPass"] as String?
            mavenPass?.let {
                credentials {
                    username = "buildslave"
                    password = mavenPass
                }
            }
        }
    }
}