pluginManagement {
    repositories {
        maven(url="https://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        mavenLocal()
        jcenter()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if(requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
rootProject.name = Constants.name