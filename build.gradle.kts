import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseUploadTask
import com.matthewprenger.cursegradle.Options
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.generateconstants.GenerateConstantsTask

plugins {
    idea
    `maven-publish`
    kotlin("jvm") version Kotlin.version
    id("kotlinx-serialization") version Kotlin.version
    id("fabric-loom") version Fabric.Loom.version
    id("constantsGenerator")
    id("moe.nikky.persistentCounter") version "0.0.8-SNAPSHOT"
//    id("moe.nikky.loom-production-env") version "0.0.1-SNAPSHOT"
    id("com.matthewprenger.cursegradle") version "1.1.2"
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

val branch = System.getenv("GIT_BRANCH")
    ?.takeUnless { it == "master" }
    ?.let { "-$it" }
    ?: ""

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch

val buildnumber = counter.variable(id = "buildnumber", key = "$major.$minor.$patch$branch")

group = Constants.group
description = Constants.description
version = System.getenv("BUILD_NUMBER")?.let { "${Constants.modVersion}+build.$buildnumber" }
    ?: "${Constants.modVersion}+local"

//loom {
//    //    refmapName = "matterlink.refmap.json"
//}

//production {
//    server {
//        workingDirectory = file("run")
////        gui = false
//    }
//    buildTasks += tasks.getByName("remapJar")
//}

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
        field("MC_VERSION") value Minecraft.version
        field("FABRIC_API_VERSION") value Fabric.API.version
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
//    maven(url = "https://jitpack.io") {
//        name = "jitpack"
//    }
    maven(url = "https://dl.bintray.com/kittinunf/maven/") {
        name = "bintray-fuel"
    }
    mavenCentral()
    jcenter()
}

//configurations.implementation.get().extendsFrom(configurations.include.get())
//configurations.modCompile.get().extendsFrom(configurations.include.get())
//configurations.include.get().isTransitive = true

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Minecraft.version)

    mappings(group = "net.fabricmc", name = "yarn", version = Fabric.Yarn.version, classifier = "v2")

//    api(kotlin("stdlib", Kotlin.version))

//    loom.deobf(implementation(group = "net.fabricmc", name = "fabric-loader", version = Fabric.Loader.version))
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = Fabric.Loader.version)

    api(group = "net.fabricmc", name = "fabric-language-kotlin", version = Fabric.LanguageKotlin.version)

    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api-base", version = "0.1.3+")
    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-events-lifecycle-v0", version = "0.1.2+")

    // only in dev env ?
//    include(group = "net.fabricmc", name = "fabric-language-kotlin", version = Fabric.LanguageKotlin.version)
//    include(group = "net.fabricmc.fabric-api", name = "fabric-api-base", version = "0.1.3+")
//    include(group = "net.fabricmc.fabric-api", name = "fabric-events-lifecycle-v0", version = "0.1.2+")

    api(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = KotlinX.Serialization.version)
    include(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = KotlinX.Serialization.version)

    api(group = "com.github.kittinunf.fuel", name = "fuel", version = Fuel.version)
    api(group = "com.github.kittinunf.fuel", name = "fuel-kotlinx-serialization", version = Fuel.version)
    api(group = "com.github.kittinunf.result", name = "result", version = "2.2.0")
//    api(group = "com.github.kittinunf.fuel", name = "fuel-coroutines", version = Fuel.version)
    include(group = "com.github.kittinunf.fuel", name = "fuel", version = Fuel.version)
    include(group = "com.github.kittinunf.fuel", name = "fuel-kotlinx-serialization", version = Fuel.version)
    include(group = "com.github.kittinunf.result", name = "result", version = "2.2.0")
//    include(group = "com.github.kittinunf.fuel", name = "fuel-coroutines", version = Fuel.version)

    api(group = "blue.endless", name = "jankson", version = "1.1.0")
    include(group = "blue.endless", name = "jankson", version = "1.1.0")
}

tasks.getByName<ProcessResources>("processResources") {
    outputs.upToDateWhen { false }
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "kotlinVersion" to Kotlin.version,
                "version" to version
            )
        )
    }
}

val jar = tasks.getByName<Jar>("jar") {
    outputs.upToDateWhen { false }
}
val remapJar = tasks.getByName<RemapJarTask>("remapJar") {
    //    setOutput(jar.archiveFile.get().asFile)
}

publishing {
    publications {
        create("main", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()

            artifact(remapJar)
        }
    }
    repositories {
        val mavenPass: String? = project.properties["mavenPass"] as String?
        mavenPass?.let {
            maven(url = "http://mavenupload.modmuss50.me/") {
                credentials {
                    username = "buildslave"
                    password = mavenPass
                }
            }
        }
    }
}

val curse_api_key: String? by project
if (curse_api_key != null && project.hasProperty("release")) {
    val CURSEFORGE_RELEASE_TYPE: String by project
    val CURSEFORGE_ID: String by project
    curseforge {
        options(closureOf<Options> {
            forgeGradleIntegration = false
        })
        apiKey = curse_api_key
        project(closureOf<CurseProject> {
            id = CURSEFORGE_ID
            releaseType = CURSEFORGE_RELEASE_TYPE
            addGameVersion(Minecraft.version)

            val changelog_file: String? by project
            if (changelog_file != null) {
                println("changelog = $changelog_file")
                changelogType = "markdown"
                changelog = file(changelog_file as String)
            }
            mainArtifact(
                remapJar.archiveFile,
                closureOf<CurseArtifact> {
                    displayName = "MatterLink ${Minecraft.version}-$version"
                })
        })
    }
    project.afterEvaluate {
        tasks.getByName<CurseUploadTask>("curseforge${CURSEFORGE_ID}") {
            dependsOn(remapJar)
        }
    }
}

task<DefaultTask>("depsize") {
    group = "help"
    description = "prints dependency sizes"
    doLast {
        val formatStr = "%,10.2f"
        val size = configurations.modCompile.resolve()
            .map { it.length() / (1024.0 * 1024.0) }.sum()

        val out = buildString {
            append("Total dependencies size:".padEnd(45))
            append("${String.format(formatStr, size)} Mb\n\n")
            configurations
                .default
                .resolve()
                .sortedWith(compareBy { -it.length() })
                .forEach {
                    append(it.name.padEnd(45))
                    append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
                }
        }
        println(out)
    }
}